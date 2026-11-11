package br.org.germinare.equipamentos.loan;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import br.org.germinare.equipamentos.ApiIntegrationTestSupport;

/** Spec 004, T005 · CA-05 (API): papel, CSRF e conta desativada ou excluída. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoanAuthorizationIntegrationTest extends ApiIntegrationTestSupport {
    private static final List<String> GET_ROUTES = List.of("/api/equipment", "/api/loans/me");

    @BeforeEach
    void setUp() {
        resetData();
    }

    @Test
    void ca05RoutesReturn401WithoutSession() {
        for (String route : GET_ROUTES) {
            assertError(get(route, null, Map.class), HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Sessao invalida ou expirada.");
        }
        assertError(post(csrfHeaders(null)), HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Sessao invalida ou expirada.");
        assertThat(loanCount()).isZero();
    }

    @Test
    void ca05OperationsRoleReceives403OnTheThreeRoutes() {
        createUser("ops@example.org", "Pessoa Operacoes", "operacoes");
        String session = login("ops@example.org");

        for (String route : GET_ROUTES) {
            assertError(get(route, session, Map.class), HttpStatus.FORBIDDEN, "FORBIDDEN", "Acesso negado.");
        }
        assertError(post(csrfHeaders(session)), HttpStatus.FORBIDDEN, "FORBIDDEN", "Acesso negado.");
        assertThat(loanCount()).isZero();
    }

    @Test
    void ca05MissingOrInvalidCsrfTokenReturns403WithAndWithoutSession() {
        createUser("pessoa@example.org", "Pessoa Teste", "colaborador");
        String session = login("pessoa@example.org");

        HttpHeaders noTokenWithSession = sessionHeaders(session);
        noTokenWithSession.setContentType(MediaType.APPLICATION_JSON);
        HttpHeaders noTokenWithoutSession = new HttpHeaders();
        noTokenWithoutSession.setContentType(MediaType.APPLICATION_JSON);
        HttpHeaders wrongTokenWithSession = csrfHeaders(session);
        wrongTokenWithSession.set("X-XSRF-TOKEN", "token-invalido");
        HttpHeaders wrongTokenWithoutSession = csrfHeaders(null);
        wrongTokenWithoutSession.set("X-XSRF-TOKEN", "token-invalido");

        for (HttpHeaders headers : List.of(noTokenWithSession, noTokenWithoutSession, wrongTokenWithSession, wrongTokenWithoutSession)) {
            assertError(post(headers), HttpStatus.FORBIDDEN, "CSRF_INVALID", "Atualize a pagina e tente novamente.");
        }
        assertThat(loanCount()).isZero();
    }

    @ParameterizedTest(name = "conta {0} com papel {1}")
    @CsvSource({"desativada,colaborador", "desativada,operacoes", "excluida,colaborador", "excluida,operacoes"})
    void ca05InactiveOrDeletedAccountGets401OnEveryRouteAndTheSession(String state, String role) {
        for (String route : List.of("/api/equipment", "/api/loans/me", "/api/auth/session", "POST")) {
            String email = "conta-" + UUID.randomUUID() + "@example.org";
            UUID user = createUser(email, "Pessoa " + state, role);
            String session = login(email);
            disable(state, user);

            ResponseEntity<Map> response = "POST".equals(route) ? post(csrfHeaders(session)) : get(route, session, Map.class);

            assertError(response, HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Sessao invalida ou expirada.");
            assertThat(response.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE))
                .as("cookie expirado em %s", route)
                .anyMatch(cookie -> cookie.startsWith("SESSION=;") && cookie.contains("Max-Age=0"));
            assertThat(loanCount()).isZero();

            if ("desativada".equals(state)) {
                jdbc.update("update app_user set active = true where id = ?", user);
            }
            assertError(get("/api/auth/session", session, Map.class),
                HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Sessao invalida ou expirada.");
        }
    }

    @Test
    void ca05CsrfIsStillEvaluatedFirstForAnInactiveAccount() {
        UUID user = createUser("pessoa@example.org", "Pessoa Teste", "colaborador");
        String session = login("pessoa@example.org");
        jdbc.update("update app_user set active = false where id = ?", user);
        HttpHeaders noToken = sessionHeaders(session);
        noToken.setContentType(MediaType.APPLICATION_JSON);

        assertError(post(noToken), HttpStatus.FORBIDDEN, "CSRF_INVALID", "Atualize a pagina e tente novamente.");
    }

    @Test
    void ca05MyLoansExcludesReturnedLoansAndOtherAccounts() {
        UUID owner = createUser("dona@example.org", "Pessoa Dona", "colaborador");
        UUID other = createUser("outra@example.org", "Outra Pessoa", "colaborador");
        jdbc.update("insert into loan (borrower_id, equipment_id) values (?, ?)", owner, equipmentId("TI-0001"));
        UUID returned = jdbc.queryForObject(
            "insert into loan (borrower_id, equipment_id) values (?, ?) returning id", UUID.class, owner, equipmentId("TI-0002"));
        jdbc.update("update loan set returned_at = current_timestamp, return_registered_by_id = ? where id = ?", other, returned);
        jdbc.update("insert into loan (borrower_id, equipment_id) values (?, ?)", other, equipmentId("TI-0003"));

        Map<?, ?>[] ownerLoans = get("/api/loans/me", login("dona@example.org"), Map[].class).getBody();
        Map<?, ?>[] otherLoans = get("/api/loans/me", login("outra@example.org"), Map[].class).getBody();

        assertThat(ownerLoans).extracting(item -> (Object) item.get("assetCode")).containsExactly("TI-0001");
        assertThat(otherLoans).extracting(item -> (Object) item.get("assetCode")).containsExactly("TI-0003");
    }

    private void disable(String state, UUID user) {
        if ("desativada".equals(state)) {
            jdbc.update("update app_user set active = false where id = ?", user);
        } else {
            jdbc.update("delete from app_user where id = ?", user);
        }
    }

    private ResponseEntity<Map> post(HttpHeaders headers) {
        return restTemplate.exchange(url("/api/loans"), HttpMethod.POST,
            new HttpEntity<>(Map.of("equipmentId", equipmentId("TI-0001")), headers), Map.class);
    }

    private static void assertError(ResponseEntity<Map> response, HttpStatus status, String code, String message) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).containsEntry("code", code).containsEntry("message", message);
    }

    private long loanCount() {
        return jdbc.queryForObject("select count(*) from loan", Long.class);
    }
}
