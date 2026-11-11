package br.org.germinare.equipamentos.auth;

import java.io.IOException;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import br.org.germinare.equipamentos.user.AppUser;
import br.org.germinare.equipamentos.user.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Spec 004 (CA-05) e Spec 001 (linha de 2026-11-11): recarrega a conta da
 * sessão em cada requisição autenticada e não pública. Conta inexistente ou
 * inativa recebe 401 `UNAUTHENTICATED`, com a sessão encerrada e o cookie
 * expirado.
 *
 * <p>Fica depois do `CsrfFilter`, para o CSRF continuar sendo avaliado
 * primeiro, e antes da autorização, para a conta inativa receber 401 antes
 * da checagem de papel. É instanciado com `new` na cadeia, como o filtro de
 * inatividade, e não é registrado como bean de servlet.
 */
final class ActiveAccountFilter extends OncePerRequestFilter {
    private final AppUserRepository users;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final SessionCookieService sessionCookieService;

    ActiveAccountFilter(
        AppUserRepository users,
        AuthenticationEntryPoint authenticationEntryPoint,
        SessionCookieService sessionCookieService
    ) {
        this.users = users;
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
        if (AuthenticatedSessionActivityFilter.PUBLIC_REQUESTS.matches(request)
            || authentication == null
            || !authentication.isAuthenticated()
            || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean active = users.findById(principal.id()).map(AppUser::active).orElse(false);
        if (!active) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            SecurityContextHolder.clearContext();
            sessionCookieService.expire(response);
            authenticationEntryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("Account inactive or removed")
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
