package br.org.germinare.equipamentos.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.org.germinare.equipamentos.user.UserRole;

@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "server.servlet.session.cookie.secure=true"
)
class AuthenticationFoundationIntegrationTest {
    private static final String VALID_PASSWORD = "senha-segura-para-teste";
    private static final Instant LOGIN_TIME = Instant.parse("2026-01-01T00:00:00Z");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    TestRestTemplate restTemplate;

    @MockBean
    Clock clock;

    @LocalServerPort
    int port;

    @BeforeEach
    void cleanDatabase() {
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
        when(clock.instant()).thenReturn(LOGIN_TIME);
        jdbc.update("delete from loan");
        jdbc.update("delete from equipment");
        jdbc.update("delete from app_user");
    }

    @Test
    void ca02AndCa06AuthenticateNormalizedEmailAndExposePersistedIdentity() {
        UUID userId = createUser("pessoa@example.org", VALID_PASSWORD, "Pessoa Teste", "operacoes", true);

        var authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated("  PESSOA@EXAMPLE.ORG ", VALID_PASSWORD)
        );

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthenticatedUser.class);

        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        assertThat(principal.id()).isEqualTo(userId);
        assertThat(principal.fullName()).isEqualTo("Pessoa Teste");
        assertThat(principal.email()).isEqualTo("pessoa@example.org");
        assertThat(principal.role()).isEqualTo(UserRole.OPERACOES);
        assertThat(principal.getAuthorities()).extracting("authority").containsExactly("ROLE_OPERACOES");
        assertThat(principal.toString()).doesNotContain(VALID_PASSWORD, principal.getPassword());
    }

    @Test
    void ca03RejectsUnknownEmailWrongPasswordAndInactiveUser() {
        createUser("ativa@example.org", VALID_PASSWORD, "Pessoa Ativa", "colaborador", true);
        createUser("inativa@example.org", VALID_PASSWORD, "Pessoa Inativa", "colaborador", false);

        assertAuthenticationRejected("inexistente@example.org", VALID_PASSWORD);
        assertAuthenticationRejected("ativa@example.org", "senha-incorreta");
        assertAuthenticationRejected("inativa@example.org", VALID_PASSWORD);
    }

    @Test
    void ca04ProtectedRequestWithoutSessionReturnsJsonContract() {
        var response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/auth/session",
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsExactlyInAnyOrderEntriesOf(Map.of(
            "code", "UNAUTHENTICATED",
            "message", "Sessao invalida ou expirada."
        ));
    }

    @Test
    void ca02Ca04Ca05Ca10LoginSessionAndRepeatedLogoutRequireCsrf() {
        UUID id = createUser("ativa@example.org", VALID_PASSWORD, "Pessoa Ativa", "colaborador", true);
        var credentials = Map.of("email", "ativa@example.org", "password", VALID_PASSWORD);
        var denied = authRequest(HttpMethod.POST, "/login", new HttpHeaders(), credentials);
        assertThat(denied.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(denied.getBody()).containsEntry("code", "CSRF_INVALID");

        var login = authRequest(HttpMethod.POST, "/login", csrfHeaders(null), credentials);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getBody()).containsOnlyKeys("user");
        assertThat(login.getBody().get("user")).isEqualTo(Map.of(
            "id", id.toString(), "fullName", "Pessoa Ativa", "email", "ativa@example.org",
            "role", "colaborador"
        ));
        String setCookie = cookieHeader(login, "SESSION");
        assertThat(setCookie).contains("HttpOnly", "Secure", "SameSite=Lax", "Path=/")
            .doesNotContain("Max-Age", "Expires", VALID_PASSWORD);
        String sessionCookie = setCookie.split(";", 2)[0];
        HttpHeaders sessionHeaders = new HttpHeaders();
        sessionHeaders.set(HttpHeaders.COOKIE, sessionCookie);
        var session = authRequest(HttpMethod.GET, "/session", sessionHeaders, null);
        assertThat(session.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(session.getBody()).isEqualTo(login.getBody());

        var logout = authRequest(HttpMethod.POST, "/logout", csrfHeaders(sessionCookie), null);
        assertThat(logout.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(cookieHeader(logout, "SESSION")).contains("Max-Age=0");
        assertThat(authRequest(HttpMethod.GET, "/session", sessionHeaders, null).getStatusCode())
            .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(authRequest(HttpMethod.POST, "/logout", csrfHeaders(null), null).getStatusCode())
            .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void ca03InvalidCredentialsShareOneHttpResponseAndNeverIssueSessionCookies() {
        createUser("ativa@example.org", VALID_PASSWORD, "Pessoa Ativa", "colaborador", true);
        createUser("inativa@example.org", VALID_PASSWORD, "Pessoa Inativa", "colaborador", false);

        for (var credentials : java.util.List.of(
            Map.of("email", "ausente@example.org", "password", VALID_PASSWORD),
            Map.of("email", "ativa@example.org", "password", "senha-incorreta"),
            Map.of("email", "inativa@example.org", "password", VALID_PASSWORD)
        )) {
            var response = authRequest(HttpMethod.POST, "/login", csrfHeaders(null), credentials);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isEqualTo(Map.of(
                "code", "INVALID_CREDENTIALS", "message", "E-mail ou senha invalidos."
            ));
            assertThat(response.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE))
                .noneMatch(cookie -> cookie.startsWith("SESSION="));
        }
    }

    @Test
    void ca02MalformedOrIncompleteLoginReturnsInvalidRequest() {
        for (Object body : java.util.List.of("{", Map.of("email", "ativa@example.org"))) {
            var response = authRequest(HttpMethod.POST, "/login", csrfHeaders(null), body);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo(Map.of(
                "code", "INVALID_REQUEST", "message", "Informe e-mail e senha."
            ));
        }
    }

    private HttpHeaders csrfHeaders(String sessionCookie) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionCookie != null) {
            headers.set(HttpHeaders.COOKIE, sessionCookie);
        }
        var csrf = authRequest(HttpMethod.GET, "/csrf", headers, null);
        assertThat(csrf.getStatusCode()).isEqualTo(HttpStatus.OK);
        String csrfCookie = cookieHeader(csrf, "XSRF-TOKEN").split(";", 2)[0];
        headers.set(HttpHeaders.COOKIE, sessionCookie == null ? csrfCookie : sessionCookie + "; " + csrfCookie);
        headers.set("X-XSRF-TOKEN", (String) csrf.getBody().get("token"));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void ca09PublicAndRejectedRequestsDoNotExtendTheEightHourSession() {
        createUser("ativa@example.org", VALID_PASSWORD, "Pessoa Ativa", "colaborador", true);
        var credentials = Map.of("email", "ativa@example.org", "password", VALID_PASSWORD);
        var login = authRequest(HttpMethod.POST, "/login", csrfHeaders(null), credentials);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        String sessionCookie = cookieHeader(login, "SESSION").split(";", 2)[0];
        HttpHeaders sessionHeaders = new HttpHeaders();
        sessionHeaders.set(HttpHeaders.COOKIE, sessionCookie);

        when(clock.instant()).thenReturn(LOGIN_TIME.plus(Duration.ofHours(7)));
        assertThat(authRequest(HttpMethod.GET, "/session", sessionHeaders, null).getStatusCode())
            .isEqualTo(HttpStatus.OK);
        when(clock.instant()).thenReturn(LOGIN_TIME.plus(Duration.ofHours(14)));
        csrfHeaders(sessionCookie);
        assertThat(authRequest(HttpMethod.POST, "/session", sessionHeaders, null).getStatusCode())
            .isEqualTo(HttpStatus.FORBIDDEN);
        when(clock.instant()).thenReturn(LOGIN_TIME.plus(Duration.ofHours(15)));
        var expired = authRequest(HttpMethod.GET, "/session", sessionHeaders, null);
        assertThat(expired.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(expired.getBody()).containsEntry("code", "UNAUTHENTICATED");
    }

    private String cookieHeader(ResponseEntity<?> response, String name) {
        return response.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE).stream()
            .filter(cookie -> cookie.startsWith(name + "="))
            .findFirst()
            .orElseThrow();
    }

    private ResponseEntity<Map> authRequest(HttpMethod method, String path, HttpHeaders headers, Object body) {
        return restTemplate.exchange(
            "http://localhost:" + port + "/api/auth" + path, method, new HttpEntity<>(body, headers), Map.class
        );
    }

    private void assertAuthenticationRejected(String email, String password) {
        assertThatThrownBy(() -> authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(email, password)
        )).isInstanceOf(AuthenticationException.class);
    }

    private UUID createUser(String email, String password, String fullName, String role, boolean active) {
        return jdbc.queryForObject(
            """
            insert into app_user (email, password_hash, full_name, role, active)
            values (?, ?, ?, ?, ?)
            returning id
            """,
            UUID.class,
            email,
            passwordEncoder.encode(password),
            fullName,
            role,
            active
        );
    }
}
