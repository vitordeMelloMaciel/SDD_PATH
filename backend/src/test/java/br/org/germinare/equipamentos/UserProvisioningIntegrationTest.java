package br.org.germinare.equipamentos;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.org.germinare.equipamentos.user.UserProvisioning;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserProvisioningIntegrationTest {
    private static final String TEST_PASSWORD = "senha-exclusiva-de-teste";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @Autowired UserProvisioning provisioning;

    private final ByteArrayOutputStream messages = new ByteArrayOutputStream();
    private final PrintStream output = new PrintStream(messages, true, StandardCharsets.UTF_8);
    private final Map<String, String> environment = new HashMap<>(Map.of(
        "PROVISION_USER_EMAIL", " Pessoa@EXAMPLE.org ",
        "PROVISION_USER_PASSWORD", TEST_PASSWORD,
        "PROVISION_USER_FULL_NAME", "Pessoa Teste",
        "PROVISION_USER_ROLE", "colaborador"
    ));

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("delete from app_user");
    }

    @Test
    void ca07Ca08CreatesOnceAndRefusesToOverwriteDivergentData() {
        assertThat(provisioning.run(environment, output)).isZero();
        var original = jdbc.queryForMap("select * from app_user where email = 'pessoa@example.org'");
        String hash = (String) original.get("password_hash");
        assertThat(encoder.matches(TEST_PASSWORD, hash)).isTrue();
        assertThat(original).containsEntry("active", true).containsEntry("role", "colaborador");

        assertThat(provisioning.run(environment, output)).isZero();
        environment.put("PROVISION_USER_PASSWORD", "senha-divergente-de-teste");
        assertThat(provisioning.run(environment, output)).isEqualTo(3);
        assertThat(jdbc.queryForList("select * from app_user")).containsExactly(original);
        assertThat(jdbc.queryForObject("select count(*) from loan", Long.class)).isZero();
        assertThat(messages.toString(StandardCharsets.UTF_8))
            .contains("Usuario ja estava provisionado.", "dados divergentes")
            .doesNotContain(TEST_PASSWORD, "senha-divergente-de-teste", hash);
    }

    @Test
    void ca08InvalidPasswordAndRoleCreateNoUser() {
        environment.put("PROVISION_USER_PASSWORD", "curta");
        assertThat(provisioning.run(environment, output)).isEqualTo(2);
        environment.put("PROVISION_USER_PASSWORD", TEST_PASSWORD);
        environment.put("PROVISION_USER_ROLE", "admin");
        assertThat(provisioning.run(environment, output)).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from app_user", Long.class)).isZero();
        assertThat(messages.toString(StandardCharsets.UTF_8)).doesNotContain(TEST_PASSWORD);
    }
}
