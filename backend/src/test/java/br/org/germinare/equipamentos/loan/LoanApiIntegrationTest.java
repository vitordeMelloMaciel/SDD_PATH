package br.org.germinare.equipamentos.loan;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import br.org.germinare.equipamentos.ApiIntegrationTestSupport;

/** Spec 004, T003 · CA-02 e CA-03: solicitação, Meus empréstimos e recusas. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoanApiIntegrationTest extends ApiIntegrationTestSupport {
    private UUID borrower;
    private String session;

    @BeforeEach
    void setUp() {
        resetData();
        borrower = createUser("pessoa@example.org", "Pessoa Teste", "colaborador");
        session = login("pessoa@example.org");
    }

    @Test
    void ca02ValidRequestCreatesLoanWithFourteenCalendarDaysAndShowsItInMyLoans() {
        var response = request(session, Map.of("equipmentId", equipmentId("TI-0001")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = response.getBody();
        assertThat(body).containsOnlyKeys("id", "assetCode", "equipmentName", "dueAt", "overdue");
        assertThat(body).containsEntry("assetCode", "TI-0001")
            .containsEntry("equipmentName", "Notebook 14 polegadas")
            .containsEntry("overdue", false);
        assertThat((String) body.get("dueAt")).matches("\\d{4}-\\d{2}-\\d{2}T.*(Z|[+-]\\d{2}:\\d{2})");

        assertThat(jdbc.queryForObject("""
            select count(*) from loan
             where id = ?::uuid and borrower_id = ? and due_at = loaned_at + interval '14 days' and returned_at is null
            """, Long.class, body.get("id"), borrower)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select status from equipment where asset_code = 'TI-0001'", String.class))
            .isEqualTo("emprestado");

        var mine = get("/api/loans/me", session, Map[].class);
        assertThat(mine.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mine.getBody()).hasSize(1);
        assertThat(mine.getBody()[0]).isEqualTo(body);
    }

    @Test
    void ca02MyLoansIsOrderedByDueDateAndFlagsOverdue() {
        insertLoan(borrower, "TI-0002", "now() - interval '1 day'");
        insertLoan(borrower, "TI-0001", "now() - interval '20 days'");

        Map<?, ?>[] mine = get("/api/loans/me", session, Map[].class).getBody();

        assertThat(mine).extracting(item -> (Object) item.get("assetCode")).containsExactly("TI-0001", "TI-0002");
        assertThat(mine).extracting(item -> (Object) item.get("overdue")).containsExactly(true, false);
    }

    @Test
    void ca03MaintenanceIsRefusedWithoutInsertingLoan() {
        assertRefused(Map.of("equipmentId", equipmentId("TI-0004")), "EQUIPMENT_MAINTENANCE", "Equipamento em manutenção.");
    }

    @Test
    void ca03BorrowedItemIsRefusedWithoutInsertingLoan() {
        UUID other = createUser("outra@example.org", "Outra Pessoa", "colaborador");
        insertLoan(other, "TI-0001", "now()");

        assertRefused(Map.of("equipmentId", equipmentId("TI-0001")), "EQUIPMENT_BORROWED", "Equipamento já emprestado.");
    }

    @Test
    void ca03FourthOpenLoanIsRefusedWithoutInsertingLoan() {
        for (String asset : List.of("TI-0001", "TI-0002", "TI-0003")) {
            insertLoan(borrower, asset, "now()");
        }

        assertRefused(Map.of("equipmentId", equipmentId("TI-0005")), "LOAN_LIMIT", "Você já tem três empréstimos em aberto.");
    }

    @Test
    void ca03OverdueLoanBlocksANewRequestWithoutInsertingLoan() {
        insertLoan(borrower, "TI-0001", "now() - interval '20 days'");

        assertRefused(Map.of("equipmentId", equipmentId("TI-0002")), "OVERDUE_LOAN", "Você tem um empréstimo em atraso.");
    }

    @Test
    void ca03InvalidBodiesReturn400AndUnknownEquipmentReturns404() {
        long before = loanCount();
        HttpHeaders headers = csrfHeaders(session);

        for (String body : List.of("{", "{}", "{\"equipmentId\":\"nao-e-uuid\"}", "{\"equipmentId\":null}")) {
            assertError(post(headers, body), HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Informe um equipamento válido.");
        }
        HttpHeaders plainText = csrfHeaders(session);
        plainText.setContentType(MediaType.TEXT_PLAIN);
        assertError(post(plainText, "{\"equipmentId\":\"" + equipmentId("TI-0001") + "\"}"),
            HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Informe um equipamento válido.");

        assertError(request(session, Map.of("equipmentId", UUID.randomUUID())),
            HttpStatus.NOT_FOUND, "EQUIPMENT_NOT_FOUND", "Equipamento não encontrado.");
        assertThat(loanCount()).isEqualTo(before);
    }

    private void assertRefused(Map<String, Object> body, String code, String message) {
        long before = loanCount();
        assertError(request(session, body), HttpStatus.CONFLICT, code, message);
        assertThat(loanCount()).isEqualTo(before);
    }

    private static void assertError(ResponseEntity<Map> response, HttpStatus status, String code, String message) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).containsOnlyKeys("code", "message")
            .containsEntry("code", code).containsEntry("message", message);
    }

    private ResponseEntity<Map> request(String sessionCookie, Map<String, Object> body) {
        return restTemplate.exchange(url("/api/loans"), HttpMethod.POST, new HttpEntity<>(body, csrfHeaders(sessionCookie)), Map.class);
    }

    private ResponseEntity<Map> post(HttpHeaders headers, String rawBody) {
        return restTemplate.exchange(url("/api/loans"), HttpMethod.POST, new HttpEntity<>(rawBody, headers), Map.class);
    }

    private void insertLoan(UUID user, String assetCode, String loanedAtSql) {
        jdbc.update("insert into loan (borrower_id, equipment_id, loaned_at) values (?, ?, " + loanedAtSql + ")",
            user, equipmentId(assetCode));
    }

    private long loanCount() {
        return jdbc.queryForObject("select count(*) from loan", Long.class);
    }
}
