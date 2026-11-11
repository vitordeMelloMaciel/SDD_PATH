package br.org.germinare.equipamentos.auth;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final CsrfTokenRepository csrfTokenRepository;
    private final SessionCookieService sessionCookieService;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
    private final Clock clock;

    public AuthController(
        AuthenticationManager authenticationManager,
        SecurityContextRepository securityContextRepository,
        CsrfTokenRepository csrfTokenRepository,
        SessionCookieService sessionCookieService,
        Clock clock
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.csrfTokenRepository = csrfTokenRepository;
        this.sessionCookieService = sessionCookieService;
        this.clock = clock;
    }

    @GetMapping("/csrf")
    Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken());
    }

    @PostMapping("/login")
    ResponseEntity<?> login(
        @RequestBody LoginRequest body,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        if (body == null || isBlank(body.email()) || isBlank(body.password())) {
            return invalidRequest();
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(body.email(), body.password())
            );
        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new AuthErrorResponse("INVALID_CREDENTIALS", "E-mail ou senha invalidos.")
            );
        }

        HttpSession previousSession = request.getSession(false);
        if (previousSession != null) {
            request.changeSessionId();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        request.getSession(false).setAttribute(
            AuthenticatedSessionActivityFilter.LAST_ACCEPTED_ACCESS,
            clock.instant()
        );
        csrfTokenRepository.saveToken(null, request, response);

        return ResponseEntity.ok(UserEnvelope.from((AuthenticatedUser) authentication.getPrincipal()));
    }

    @GetMapping("/session")
    UserEnvelope session(@AuthenticationPrincipal AuthenticatedUser user) {
        return UserEnvelope.from(user);
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) {
        logoutHandler.logout(request, response, authentication);
        csrfTokenRepository.saveToken(null, request, response);
        sessionCookieService.expire(response);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<AuthErrorResponse> invalidRequest() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new AuthErrorResponse("INVALID_REQUEST", "Informe e-mail e senha.")
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

record LoginRequest(String email, String password) {
}

record UserEnvelope(AuthUserResponse user) {
    static UserEnvelope from(AuthenticatedUser user) {
        return new UserEnvelope(new AuthUserResponse(
            user.id(),
            user.fullName(),
            user.email(),
            user.role().value()
        ));
    }
}

record AuthUserResponse(UUID id, String fullName, String email, String role) {
}
