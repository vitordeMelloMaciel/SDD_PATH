package br.org.germinare.equipamentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Spec 003, T002 · CA-01, CA-02 e CA-06: carga V4 e repetição do mesmo arquivo. */
@Testcontainers
class InitialEquipmentLoadIntegrationTest {
    private static final String LOAD_FILE = "db/migration/V4__carga_inicial_equipamentos.sql";

    /** Referência: .agents/skills/imagens-equipamentos/references/equipment.json (commit 8052d1b). */
    private static final List<String> EXPECTED = List.of(
        "TI-0001|Notebook 14 polegadas|notebook|disponivel|equipment/TI-0001.jpg",
        "TI-0002|Notebook 15 polegadas|notebook|disponivel|equipment/TI-0002.jpg",
        "TI-0003|Notebook 13 polegadas|notebook|disponivel|equipment/TI-0003.jpg",
        "TI-0004|Notebook 15 polegadas|notebook|manutencao|equipment/TI-0004.jpg",
        "TI-0005|Monitor 24 polegadas|monitor|disponivel|equipment/TI-0005.jpg",
        "TI-0006|Monitor 27 polegadas|monitor|disponivel|equipment/TI-0006.jpg",
        "TI-0007|Monitor 22 polegadas|monitor|manutencao|equipment/TI-0007.jpg",
        "TI-0008|Cabo HDMI 2 m|cabo|disponivel|equipment/TI-0008.jpg",
        "TI-0009|Cabo USB-C 1 m|cabo|disponivel|equipment/TI-0009.jpg",
        "TI-0010|Cabo de rede 3 m|cabo|manutencao|equipment/TI-0010.jpg",
        "TI-0011|Webcam Full HD|camera|disponivel|equipment/TI-0011.jpg",
        "TI-0012|Câmera compacta|camera|disponivel|equipment/TI-0012.jpg"
    );

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void ca01EmptyDatabaseReceivesTheTwelveReferenceItems() throws SQLException {
        String url = migratedDatabase();

        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            assertThat(referenceRows(connection)).containsExactlyElementsOf(EXPECTED);
            assertThat(count(connection, "select count(*) from equipment where status = 'manutencao'")).isEqualTo(3);
            assertThat(count(connection, "select count(*) from equipment where status = 'disponivel'")).isEqualTo(9);
            assertThat(count(connection, "select count(*) from app_user")).isZero();
            assertThat(count(connection, "select count(*) from loan")).isZero();
        }
    }

    @Test
    void ca01FirstLoadKeepsPreexistingMatchingItemAndItemsOutsideTheList() throws SQLException {
        String url = MigrationTestDatabases.createEmptyDatabase(postgres);
        MigrationTestDatabases.migrate(postgres, url, "3");

        UUID preexistingId;
        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            preexistingId = insertEquipment(connection, "TI-0004", "Notebook 15 polegadas", "notebook", "disponivel");
            insertEquipment(connection, "ANTIGO-1", "Projetor", "projetor", "manutencao");
        }

        MigrationTestDatabases.migrate(postgres, url, "latest");

        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            Map<String, UUID> ids = idsByAsset(connection);
            assertThat(ids.get("TI-0004")).isEqualTo(preexistingId);
            assertThat(row(connection, "TI-0004"))
                .isEqualTo("TI-0004|Notebook 15 polegadas|notebook|disponivel|equipment/TI-0004.jpg");
            assertThat(row(connection, "ANTIGO-1")).isEqualTo("ANTIGO-1|Projetor|projetor|manutencao|null");
            assertThat(referenceRows(connection)).hasSize(12);
        }
    }

    @Test
    void ca02RerunningTheSameFilePreservesIdsLoansAndOutsideItemsAndFillsMissingPhoto() throws Exception {
        String url = migratedDatabase();
        Map<String, UUID> idsBefore;
        UUID loanId;

        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            idsBefore = idsByAsset(connection);
            UUID borrower = insertUser(connection, "carga@example.com");
            loanId = openLoan(connection, borrower, idsBefore.get("TI-0001"));
            insertEquipment(connection, "ANTIGO-1", "Projetor", "projetor", "disponivel");
            execute(connection, "update equipment set image_object_key = null where asset_code = 'TI-0002'");
        }

        runLoadFileInSingleTransaction(url);

        String afterFirstRerun;
        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            Map<String, UUID> idsAfter = idsByAsset(connection);
            idsBefore.forEach((asset, id) -> assertThat(idsAfter.get(asset)).as(asset).isEqualTo(id));
            assertThat(row(connection, "TI-0001"))
                .isEqualTo("TI-0001|Notebook 14 polegadas|notebook|emprestado|equipment/TI-0001.jpg");
            assertThat(row(connection, "TI-0002")).endsWith("|equipment/TI-0002.jpg");
            assertThat(row(connection, "TI-0004")).contains("|manutencao|");
            assertThat(row(connection, "ANTIGO-1")).isEqualTo("ANTIGO-1|Projetor|projetor|disponivel|null");
            assertThat(count(connection, "select count(*) from loan where returned_at is null")).isEqualTo(1);
            assertThat(count(connection, "select count(*) from loan where id = '" + loanId + "' and returned_at is null")).isEqualTo(1);
            afterFirstRerun = snapshot(connection);
        }

        runLoadFileInSingleTransaction(url);

        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            assertThat(snapshot(connection)).isEqualTo(afterFirstRerun);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "update equipment set name = 'Notebook antigo' where asset_code = 'TI-0003'",
        "update equipment set category = 'periferico' where asset_code = 'TI-0003'",
        "update equipment set image_object_key = 'equipment/outra.jpg' where asset_code = 'TI-0003'"
    })
    void ca02RerunRefusesIdentityCollisionWithoutPartialWrite(String divergence) throws Exception {
        String url = migratedDatabase();
        String before;

        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            execute(connection, divergence);
            // Uma foto ausente em outro item mostra que nada é preenchido quando a carga é recusada.
            execute(connection, "update equipment set image_object_key = null where asset_code = 'TI-0005'");
            before = snapshot(connection);
        }

        assertThatThrownBy(() -> runLoadFileInSingleTransaction(url))
            .isInstanceOf(SQLException.class)
            .hasMessageContaining("colisao de identidade nos patrimonios: TI-0003");

        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            assertThat(snapshot(connection)).isEqualTo(before);
        }
    }

    @Test
    void ca02FirstApplicationWithCollisionFailsWithoutWriting() throws SQLException {
        String url = MigrationTestDatabases.createEmptyDatabase(postgres);
        MigrationTestDatabases.migrate(postgres, url, "3");
        String before;

        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            insertEquipment(connection, "TI-0005", "Monitor antigo", "monitor", "disponivel");
            before = snapshot(connection);
        }

        assertThatThrownBy(() -> MigrationTestDatabases.migrate(postgres, url, "latest"))
            .isInstanceOf(FlywayException.class)
            .hasMessageContaining("colisao de identidade nos patrimonios: TI-0005");

        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            assertThat(snapshot(connection)).isEqualTo(before);
            assertThat(count(connection, "select count(*) from flyway_schema_history where version = '4' and success")).isZero();
        }
    }

    @Test
    void ca02AndCa06LoadFileHasNoTransactionControlNorSecrets() throws IOException {
        String load = readClasspath(LOAD_FILE).toLowerCase(Locale.ROOT);
        String schema = readClasspath("db/migration/V3__equipment_image_object_key.sql").toLowerCase(Locale.ROOT);

        assertThat(load).doesNotContain("commit", "rollback", "start transaction");
        assertThat(load.lines().map(String::strip)).doesNotContain("begin;", "begin transaction;");
        for (String content : List.of(load, schema)) {
            assertThat(content).doesNotContain("password", "senha", "token", "secret", "$2a$", "$2b$", "$2y$", "@");
        }
    }

    private static String migratedDatabase() throws SQLException {
        String url = MigrationTestDatabases.createEmptyDatabase(postgres);
        MigrationTestDatabases.migrate(postgres, url, "latest");
        return url;
    }

    /** Executa o arquivo versionado sem alteração, numa única transação, como o psql --single-transaction. */
    private static void runLoadFileInSingleTransaction(String url) throws Exception {
        String sql = readClasspath(LOAD_FILE);
        try (Connection connection = MigrationTestDatabases.connect(postgres, url)) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
                connection.commit();
            } catch (SQLException failure) {
                connection.rollback();
                throw failure;
            }
        }
    }

    private static String readClasspath(String path) throws IOException {
        try (InputStream input = InitialEquipmentLoadIntegrationTest.class.getClassLoader().getResourceAsStream(path)) {
            assertThat(input).as(path).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static List<String> referenceRows(Connection connection) throws SQLException {
        List<String> rows = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "select asset_code, name, category, status, image_object_key from equipment where asset_code like 'TI-%' order by asset_code")) {
            while (result.next()) {
                rows.add(format(result));
            }
        }
        return rows;
    }

    private static String row(Connection connection, String assetCode) throws SQLException {
        try (PreparedStatement select = connection.prepareStatement(
                 "select asset_code, name, category, status, image_object_key from equipment where asset_code = ?")) {
            select.setString(1, assetCode);
            try (ResultSet result = select.executeQuery()) {
                assertThat(result.next()).as(assetCode).isTrue();
                return format(result);
            }
        }
    }

    /** Estado completo da tabela, incluindo IDs e updated_at, para provar ausência de escrita. */
    private static String snapshot(Connection connection) throws SQLException {
        StringBuilder state = new StringBuilder();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 "select id, asset_code, name, category, status, image_object_key, updated_at from equipment order by asset_code")) {
            while (result.next()) {
                for (int column = 1; column <= 7; column++) {
                    state.append(result.getString(column)).append('|');
                }
                state.append('\n');
            }
        }
        return state.toString();
    }

    private static String format(ResultSet result) throws SQLException {
        return String.join("|",
            result.getString("asset_code"),
            result.getString("name"),
            result.getString("category"),
            result.getString("status"),
            String.valueOf(result.getString("image_object_key")));
    }

    private static Map<String, UUID> idsByAsset(Connection connection) throws SQLException {
        Map<String, UUID> ids = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("select asset_code, id from equipment order by asset_code")) {
            while (result.next()) {
                ids.put(result.getString(1), result.getObject(2, UUID.class));
            }
        }
        return ids;
    }

    private static UUID insertEquipment(Connection connection, String asset, String name, String category, String status)
            throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                 "insert into equipment (asset_code, name, category, status) values (?, ?, ?, ?) returning id")) {
            insert.setString(1, asset);
            insert.setString(2, name);
            insert.setString(3, category);
            insert.setString(4, status);
            try (ResultSet result = insert.executeQuery()) {
                result.next();
                return result.getObject(1, UUID.class);
            }
        }
    }

    private static UUID insertUser(Connection connection, String email) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                 "insert into app_user (email, password_hash, full_name, role) values (?, 'hash-de-teste', 'Pessoa Teste', 'colaborador') returning id")) {
            insert.setString(1, email);
            try (ResultSet result = insert.executeQuery()) {
                result.next();
                return result.getObject(1, UUID.class);
            }
        }
    }

    private static UUID openLoan(Connection connection, UUID borrower, UUID equipment) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                 "insert into loan (borrower_id, equipment_id, due_at) values (?, ?, current_timestamp + interval '14 days') returning id")) {
            insert.setObject(1, borrower);
            insert.setObject(2, equipment);
            try (ResultSet result = insert.executeQuery()) {
                result.next();
                return result.getObject(1, UUID.class);
            }
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static long count(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getLong(1);
        }
    }
}
