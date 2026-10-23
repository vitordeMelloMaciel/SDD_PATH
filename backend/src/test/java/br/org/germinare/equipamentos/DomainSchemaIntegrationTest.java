package br.org.germinare.equipamentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class DomainSchemaIntegrationTest {
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

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("delete from loan");
        jdbc.update("delete from equipment");
        jdbc.update("delete from app_user");
    }

    @Test
    void ca01CreatesDomainTables() {
        assertThat(tableExists("app_user")).isTrue();
        assertThat(tableExists("equipment")).isTrue();
        assertThat(tableExists("loan")).isTrue();
    }

    @Test
    void ca02AndCa03RejectDuplicatesAndUnknownDomainValues() {
        createUser("unico@example.com");

        assertThatThrownBy(() -> createUser("unico@example.com"))
            .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbc.update(
            "insert into app_user (email, password_hash, full_name, role) values ('papel@example.com', 'hash', 'Pessoa', 'admin')"
        )).isInstanceOf(DataAccessException.class);

        createEquipment("PATRIMONIO-1");
        assertThatThrownBy(() -> createEquipment("PATRIMONIO-1"))
            .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbc.update(
            "insert into equipment (asset_code, name, category, status) values ('PATRIMONIO-2', 'Camera', 'camera', 'sumido')"
        )).isInstanceOf(DataAccessException.class);
    }

    @Test
    void ca04RejectsFourthOpenLoan() {
        UUID borrowerId = createUser("colaborador@example.com");

        for (int index = 1; index <= 3; index++) {
            createLoan(borrowerId, createEquipment("PAT-" + index));
        }

        UUID fourthEquipment = createEquipment("PAT-4");
        assertThatThrownBy(() -> createLoan(borrowerId, fourthEquipment))
            .isInstanceOf(DataAccessException.class)
            .hasMessageContaining("three open loans");
    }

    @Test
    void ca05RejectsNewLoanWhenBorrowerHasOverdueItem() {
        UUID borrowerId = createUser("atraso@example.com");
        UUID overdueEquipment = createEquipment("ATR-1");
        OffsetDateTime loanedAt = OffsetDateTime.now().minusDays(20);

        jdbc.queryForObject(
            "insert into loan (borrower_id, equipment_id, loaned_at, due_at) values (?, ?, ?, ?) returning id",
            UUID.class,
            borrowerId,
            overdueEquipment,
            loanedAt,
            loanedAt.plusDays(14)
        );

        UUID nextEquipment = createEquipment("ATR-2");
        assertThatThrownBy(() -> createLoan(borrowerId, nextEquipment))
            .isInstanceOf(DataAccessException.class)
            .hasMessageContaining("overdue loan");
    }

    @Test
    void ca06AndCa07KeepOneOpenLoanAndSynchronizeEquipmentStatus() {
        UUID borrowerId = createUser("primeiro@example.com");
        UUID secondBorrowerId = createUser("segundo@example.com");
        UUID equipmentId = createEquipment("UNICO-1");
        UUID loanId = createLoan(borrowerId, equipmentId);

        assertThat(equipmentStatus(equipmentId)).isEqualTo("emprestado");
        assertThatThrownBy(() -> createLoan(secondBorrowerId, equipmentId))
            .isInstanceOf(DataAccessException.class);

        jdbc.update(
            "update loan set returned_at = current_timestamp, return_registered_by_id = ? where id = ?",
            borrowerId,
            loanId
        );

        assertThat(equipmentStatus(equipmentId)).isEqualTo("disponivel");
    }

    private boolean tableExists(String tableName) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
            "select to_regclass('public.' || ?) is not null",
            Boolean.class,
            tableName
        ));
    }

    private UUID createUser(String email) {
        return jdbc.queryForObject(
            "insert into app_user (email, password_hash, full_name, role) values (?, 'hash', 'Pessoa Teste', 'colaborador') returning id",
            UUID.class,
            email
        );
    }

    private UUID createEquipment(String assetCode) {
        return jdbc.queryForObject(
            "insert into equipment (asset_code, name, category) values (?, 'Notebook', 'notebook') returning id",
            UUID.class,
            assetCode
        );
    }

    private UUID createLoan(UUID borrowerId, UUID equipmentId) {
        return jdbc.queryForObject(
            "insert into loan (borrower_id, equipment_id, due_at) values (?, ?, current_timestamp + interval '14 days') returning id",
            UUID.class,
            borrowerId,
            equipmentId
        );
    }

    private String equipmentStatus(UUID equipmentId) {
        return jdbc.queryForObject(
            "select status from equipment where id = ?",
            String.class,
            equipmentId
        );
    }
}
