package br.org.germinare.equipamentos.auth;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.org.germinare.equipamentos.user.AppUserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository users;

    public AppUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        String normalizedEmail = normalizeEmail(email);
        return users.findByEmail(normalizedEmail)
            .map(AuthenticatedUser::from)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new UsernameNotFoundException("Invalid credentials");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
