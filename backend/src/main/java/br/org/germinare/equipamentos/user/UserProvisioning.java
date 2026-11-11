package br.org.germinare.equipamentos.user;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserProvisioning {
    private final JdbcTemplate jdbc;
    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public UserProvisioning(JdbcTemplate jdbc, AppUserRepository users, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.users = users;
        this.encoder = encoder;
    }

    public int run(Map<String, String> environment, PrintStream output) {
        String email = environment.getOrDefault("PROVISION_USER_EMAIL", "").trim().toLowerCase(Locale.ROOT);
        String password = environment.getOrDefault("PROVISION_USER_PASSWORD", "");
        String fullName = environment.getOrDefault("PROVISION_USER_FULL_NAME", "").trim();
        String requestedRole = environment.getOrDefault("PROVISION_USER_ROLE", "");
        if (!email.matches("[^\\s@]+@[^\\s@]+") || email.length() < 4 || email.length() > 254
            || fullName.isEmpty() || fullName.length() > 160 || password.isBlank()
            || password.codePointCount(0, password.length()) < 12
            || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            output.println("Confira e-mail, nome e senha de pelo menos 12 caracteres e ate 72 bytes.");
            return 2;
        }

        UserRole role;
        try {
            role = UserRole.fromValue(requestedRole);
        } catch (IllegalArgumentException exception) {
            output.println("Papel invalido. Use colaborador ou operacoes.");
            return 2;
        }

        try {
            int inserted = jdbc.update("""
                insert into app_user (email, password_hash, full_name, role)
                values (?, ?, ?, ?) on conflict (email) do nothing
                """, email, encoder.encode(password), fullName, role.value());
            if (inserted == 1) {
                output.println("Usuario provisionado.");
                return 0;
            }
            boolean matches = users.findByEmail(email)
                .filter(user -> user.active() && user.fullName().equals(fullName) && user.role() == role)
                .filter(user -> encoder.matches(password, user.passwordHash()))
                .isPresent();
            if (matches) {
                output.println("Usuario ja estava provisionado.");
                return 0;
            }
            output.println("Usuario existente com dados divergentes. Use o fluxo administrativo quando disponivel.");
            return 3;
        } catch (DataAccessException exception) {
            output.println("Nao foi possivel acessar o cadastro. Confira a conexao e o schema.");
            return 1;
        }
    }
}
