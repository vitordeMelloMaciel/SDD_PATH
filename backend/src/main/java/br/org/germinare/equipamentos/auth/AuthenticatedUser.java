package br.org.germinare.equipamentos.auth;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.org.germinare.equipamentos.user.AppUser;
import br.org.germinare.equipamentos.user.UserRole;

public final class AuthenticatedUser implements UserDetails {
    private final UUID id;
    private final String fullName;
    private final String email;
    private final UserRole role;
    private final String passwordHash;
    private final boolean active;

    private AuthenticatedUser(
        UUID id,
        String fullName,
        String email,
        UserRole role,
        String passwordHash,
        boolean active
    ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.passwordHash = passwordHash;
        this.active = active;
    }

    public static AuthenticatedUser from(AppUser user) {
        return new AuthenticatedUser(
            user.id(),
            user.fullName(),
            user.email(),
            user.role(),
            user.passwordHash(),
            user.active()
        );
    }

    public UUID id() {
        return id;
    }

    public String fullName() {
        return fullName;
    }

    public String email() {
        return email;
    }

    public UserRole role() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public String toString() {
        return "AuthenticatedUser[id=" + id + ", email=" + email + ", role=" + role.value() + "]";
    }
}
