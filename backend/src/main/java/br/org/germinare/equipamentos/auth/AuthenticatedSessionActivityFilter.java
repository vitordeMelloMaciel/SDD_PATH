package br.org.germinare.equipamentos.auth;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class AuthenticatedSessionActivityFilter extends OncePerRequestFilter {
    static final String LAST_ACCEPTED_ACCESS = AuthenticatedSessionActivityFilter.class.getName()
        + ".LAST_ACCEPTED_ACCESS";

    private final Clock clock;
    private final Duration idleTimeout;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final SessionCookieService sessionCookieService;
    private final RequestMatcher publicRequest = new OrRequestMatcher(
        new AntPathRequestMatcher("/health"),
        new AntPathRequestMatcher("/actuator/health"),
        new AntPathRequestMatcher("/api/auth/csrf"),
        new AntPathRequestMatcher("/api/auth/login"),
        new AntPathRequestMatcher("/api/auth/logout")
    );

    AuthenticatedSessionActivityFilter(
        Clock clock,
        Duration idleTimeout,
        JsonAuthenticationEntryPoint authenticationEntryPoint,
        SessionCookieService sessionCookieService
    ) {
        this.clock = clock;
        this.idleTimeout = idleTimeout;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.sessionCookieService = sessionCookieService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (publicRequest.matches(request) || !isAuthenticated(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        Instant lastAcceptedAccess = session == null
            ? null
            : (Instant) session.getAttribute(LAST_ACCEPTED_ACCESS);
        Instant now = clock.instant();

        if (lastAcceptedAccess == null || !now.isBefore(lastAcceptedAccess.plus(idleTimeout))) {
            if (session != null) {
                session.invalidate();
            }
            SecurityContextHolder.clearContext();
            sessionCookieService.expire(response);
            authenticationEntryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("Session expired")
            );
            return;
        }

        filterChain.doFilter(request, response);
        if (response.getStatus() < HttpServletResponse.SC_BAD_REQUEST) {
            try {
                session.setAttribute(LAST_ACCEPTED_ACCESS, clock.instant());
            } catch (IllegalStateException ignored) {
                // The request itself may have invalidated the session.
            }
        }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof AuthenticatedUser;
    }
}
