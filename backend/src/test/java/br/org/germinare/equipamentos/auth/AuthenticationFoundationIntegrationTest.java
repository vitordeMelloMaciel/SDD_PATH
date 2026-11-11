package br.org.germinare.equipamentos.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationFoundationIntegrationTest {
    private static final String VALID_PASSWORD = "senha-segura-para-teste";

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

    @LocalServerPort
    int port;

    @BeforeEach
    void cleanDatabase() {
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
