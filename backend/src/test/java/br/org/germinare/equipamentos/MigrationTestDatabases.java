package br.org.germinare.equipamentos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Cria um banco vazio por teste dentro do mesmo container, para que cada
 * cenário de migration comece do zero sem depender de Flyway clean.
 */
final class MigrationTestDatabases {
    private MigrationTestDatabases() {
    }

    static String createEmptyDatabase(PostgreSQLContainer<?> postgres) throws SQLException {
        String name = "mig_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection admin = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = admin.createStatement()) {
            statement.execute("create database " + name);
        }
        return postgres.getJdbcUrl().replace("/" + postgres.getDatabaseName(), "/" + name);
    }

    static void migrate(PostgreSQLContainer<?> postgres, String url, String target) {
        Flyway.configure()
            .dataSource(url, postgres.getUsername(), postgres.getPassword())
            .locations("classpath:db/migration")
            .target(target)
            .load()
            .migrate();
    }

    static Connection connect(PostgreSQLContainer<?> postgres, String url) throws SQLException {
        return DriverManager.getConnection(url, postgres.getUsername(), postgres.getPassword());
    }
}
