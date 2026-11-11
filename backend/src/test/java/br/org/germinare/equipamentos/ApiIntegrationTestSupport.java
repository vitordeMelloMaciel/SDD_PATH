package br.org.germinare.equipamentos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Apoio dos testes HTTP da Spec 004. Um único PostgreSQL real fica no ar
 * durante a suíte. Cada teste devolve os 12 itens da V4 ao estado inicial.
 */
public abstract class ApiIntegrationTestSupport {
    protected static final String PASSWORD = "senha-segura-para-teste";

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    PasswordEncoder passwordEncoder;

    @LocalServerPort
    protected int port;

    /** Remove contas e empréstimos e devolve a situação inicial dos 12 itens. */
    protected void resetData() {
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
        jdbc.update("delete from loan");
        jdbc.update("delete from app_user");
        jdbc.update("""
            update equipment
               set status = case when asset_code in ('TI-0004', 'TI-0007', 'TI-0010')
                                 then 'manutencao' else 'disponivel' end
            """);
    }

    protected UUID createUser(String email, String fullName, String role) {
        return jdbc.queryForObject("""
            insert into app_user (email, password_hash, full_name, role)
            values (?, ?, ?, ?)
            returning id
            """, UUID.class, email, passwordEncoder.encode(PASSWORD), fullName, role);
    }

    protected UUID equipmentId(String assetCode) {
        return jdbc.queryForObject("select id from equipment where asset_code = ?", UUID.class, assetCode);
    }

    /** Faz login pelo contrato da Spec 001 e devolve o cookie de sessão. */
    protected String login(String email) {
        var response = restTemplate.exchange(url("/api/auth/login"), HttpMethod.POST,
            new HttpEntity<>(Map.of("email", email, "password", PASSWORD), csrfHeaders(null)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return cookie(response, "SESSION");
    }

    /** Cabeçalhos com token CSRF válido e, se houver, o cookie de sessão. */
    protected HttpHeaders csrfHeaders(String sessionCookie) {
        HttpHeaders headers = sessionHeaders(sessionCookie);
        var csrf = restTemplate.exchange(url("/api/auth/csrf"), HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        assertThat(csrf.getStatusCode()).isEqualTo(HttpStatus.OK);
        String csrfCookie = cookie(csrf, "XSRF-TOKEN");
        headers.set(HttpHeaders.COOKIE, sessionCookie == null ? csrfCookie : sessionCookie + "; " + csrfCookie);
        headers.set("X-XSRF-TOKEN", (String) csrf.getBody().get("token"));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected HttpHeaders sessionHeaders(String sessionCookie) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionCookie != null) {
            headers.set(HttpHeaders.COOKIE, sessionCookie);
        }
        return headers;
    }

    protected <T> ResponseEntity<T> get(String path, String sessionCookie, Class<T> type) {
        return restTemplate.exchange(url(path), HttpMethod.GET, new HttpEntity<>(sessionHeaders(sessionCookie)), type);
    }

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    protected static String cookie(ResponseEntity<?> response, String name) {
        return response.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE).stream()
            .filter(value -> value.startsWith(name + "="))
            .findFirst()
            .orElseThrow()
            .split(";", 2)[0];
    }
}
