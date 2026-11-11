package br.org.germinare.equipamentos;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Spec 003, T001 · CA-01: V3 em base vazia e evolução de uma base na V2. */
@Testcontainers
class EquipmentImageKeyMigrationIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void ca01EmptyDatabaseReceivesNullableImageKeyColumn() throws SQLException {
        String url = MigrationTestDatabases.createEmptyDatabase(postgres);
        MigrationTestDatabases.migrate(postgres, url, "3");

        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            assertImageKeyColumn(connection);
        }
    }

    @Test
    void ca01V2DatabaseEvolvesWithoutChangingExistingRows() throws SQLException {
        String url = MigrationTestDatabases.createEmptyDatabase(postgres);
        MigrationTestDatabases.migrate(postgres, url, "2");

        UUID equipmentId;
        try (Connection connection = MigrationTestDatabases.connect(postgres, url);
             PreparedStatement insert = connection.prepareStatement(
                 "insert into equipment (asset_code, name, category, status) values ('ANTIGO-1', 'Projetor', 'projetor', 'manutencao') returning id")) {
            try (ResultSet result = insert.executeQuery()) {
                result.next();
                equipmentId = result.getObject(1, UUID.class);
            }
        }

        MigrationTestDatabases.migrate(postgres, url, "3");

        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            assertImageKeyColumn(connection);
            try (PreparedStatement select = connection.prepareStatement(
                     "select asset_code, name, category, status, image_object_key from equipment where id = ?")) {
                select.setObject(1, equipmentId);
                try (ResultSet row = select.executeQuery()) {
                    assertThat(row.next()).isTrue();
                    assertThat(row.getString("asset_code")).isEqualTo("ANTIGO-1");
                    assertThat(row.getString("name")).isEqualTo("Projetor");
                    assertThat(row.getString("category")).isEqualTo("projetor");
                    assertThat(row.getString("status")).isEqualTo("manutencao");
                    assertThat(row.getString("image_object_key")).isNull();
                }
            }
        }
    }

    private static void assertImageKeyColumn(Connection connection) throws SQLException {
        try (PreparedStatement select = connection.prepareStatement("""
                select data_type, character_maximum_length, is_nullable, column_default
                  from information_schema.columns
                 where table_schema = 'public' and table_name = 'equipment' and column_name = 'image_object_key'
                """);
             ResultSet column = select.executeQuery()) {
            assertThat(column.next()).isTrue();
            assertThat(column.getString("data_type")).isEqualTo("character varying");
            assertThat(column.getInt("character_maximum_length")).isEqualTo(255);
            assertThat(column.getString("is_nullable")).isEqualTo("YES");
            assertThat(column.getString("column_default")).isNull();
        }
    }
}
