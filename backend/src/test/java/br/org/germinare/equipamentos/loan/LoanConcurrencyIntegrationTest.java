package br.org.germinare.equipamentos.loan;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.org.germinare.equipamentos.ApiIntegrationTestSupport;

/** Spec 004, T004 · CA-04: concorrência em PostgreSQL real e rejeição inesperada do banco. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoanConcurrencyIntegrationTest extends ApiIntegrationTestSupport {
    @BeforeEach
    void setUp() {
        resetData();
    }

    @RepeatedTest(3)
    void ca04ConcurrentRequestsForTheSameItemKeepExactlyOneOpenLoan() throws Exception {
        createUser("a@example.org", "Pessoa A", "colaborador");
        createUser("b@example.org", "Pessoa B", "colaborador");
        UUID item = equipmentId("TI-0001");
        HttpHeaders first = csrfHeaders(login("a@example.org"));
        HttpHeaders second = csrfHeaders(login("b@example.org"));

        List<ResponseEntity<Map>> responses = concurrently(
            () -> request(first, item),
            () -> request(second, item));

        assertThat(responses).extracting(ResponseEntity::getStatusCode)
            .containsExactlyInAnyOrder(HttpStatus.CREATED, HttpStatus.CONFLICT);
        assertThat(conflict(responses).getBody()).containsEntry("code", "EQUIPMENT_BORROWED");
        assertThat(jdbc.queryForObject(
            "select count(*) from loan where equipment_id = ? and returned_at is null", Long.class, item)).isEqualTo(1);
    }

    @RepeatedTest(3)
    void ca04ConcurrentRequestsForDistinctItemsRespectTheLimitOfThree() throws Exception {
        UUID borrower = createUser("a@example.org", "Pessoa A", "colaborador");
        for (String asset : List.of("TI-0001", "TI-0002")) {
            jdbc.update("insert into loan (borrower_id, equipment_id) values (?, ?)", borrower, equipmentId(asset));
        }
        String session = login("a@example.org");
        HttpHeaders first = csrfHeaders(session);
        HttpHeaders second = csrfHeaders(session);

        List<ResponseEntity<Map>> responses = concurrently(
            () -> request(first, equipmentId("TI-0003")),
            () -> request(second, equipmentId("TI-0005")));

        assertThat(responses).extracting(ResponseEntity::getStatusCode)
            .containsExactlyInAnyOrder(HttpStatus.CREATED, HttpStatus.CONFLICT);
        assertThat(conflict(responses).getBody()).containsEntry("code", "LOAN_LIMIT");
        assertThat(jdbc.queryForObject(
            "select count(*) from loan where borrower_id = ? and returned_at is null", Long.class, borrower)).isEqualTo(3);
    }

    @Test
    void ca04UniqueOpenLoanViolationAfterTheLockedReadBecomesEquipmentBorrowed() {
        UUID other = createUser("outra@example.org", "Outra Pessoa", "colaborador");
        createUser("a@example.org", "Pessoa A", "colaborador");
        UUID item = equipmentId("TI-0001");
        jdbc.update("insert into loan (borrower_id, equipment_id) values (?, ?)", other, item);
        // Estado inconsistente fora da API: item marcado como disponível com empréstimo aberto.
        jdbc.update("update equipment set status = 'disponivel' where id = ?", item);

        var response = request(csrfHeaders(login("a@example.org")), item);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("code", "EQUIPMENT_BORROWED")
            .containsEntry("message", "Equipamento já emprestado.");
        assertThat(jdbc.queryForObject("select count(*) from loan where equipment_id = ?", Long.class, item)).isEqualTo(1);
    }

    @Test
    void ca04OtherDatabaseRejectionReturns500WithoutCreatingLoan() {
        createUser("a@example.org", "Pessoa A", "colaborador");
        jdbc.execute("""
            create function test_reject_loan() returns trigger language plpgsql as $$
            begin raise exception 'rejeicao de teste'; end $$
            """);
        jdbc.execute("create trigger trg_test_reject_loan before insert on loan for each row execute function test_reject_loan()");
        try {
            var response = request(csrfHeaders(login("a@example.org")), equipmentId("TI-0001"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).containsOnlyKeys("code", "message")
                .containsEntry("code", "INTERNAL_ERROR")
                .containsEntry("message", "Não foi possível registrar o empréstimo.");
            assertThat(jdbc.queryForObject("select count(*) from loan", Long.class)).isZero();
            assertThat(jdbc.queryForObject("select status from equipment where asset_code = 'TI-0001'", String.class))
                .isEqualTo("disponivel");
        } finally {
            jdbc.execute("drop trigger trg_test_reject_loan on loan");
            jdbc.execute("drop function test_reject_loan()");
        }
    }

    private ResponseEntity<Map> request(HttpHeaders headers, UUID equipmentId) {
        return restTemplate.exchange(url("/api/loans"), HttpMethod.POST,
            new HttpEntity<>(Map.of("equipmentId", equipmentId), headers), Map.class);
    }

    @SafeVarargs
    private static List<ResponseEntity<Map>> concurrently(Callable<ResponseEntity<Map>>... calls) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(calls.length);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<ResponseEntity<Map>>> futures = new ArrayList<>();
            for (Callable<ResponseEntity<Map>> call : calls) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return call.call();
                }));
            }
            start.countDown();
            List<ResponseEntity<Map>> responses = new ArrayList<>();
            for (Future<ResponseEntity<Map>> future : futures) {
                responses.add(future.get(30, TimeUnit.SECONDS));
            }
            return responses;
        } finally {
            pool.shutdownNow();
        }
    }

    private static ResponseEntity<Map> conflict(List<ResponseEntity<Map>> responses) {
        return responses.stream().filter(response -> response.getStatusCode() == HttpStatus.CONFLICT).findFirst().orElseThrow();
    }
}
