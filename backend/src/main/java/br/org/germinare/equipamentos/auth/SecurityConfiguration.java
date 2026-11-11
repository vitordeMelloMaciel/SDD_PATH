package br.org.germinare.equipamentos.auth;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    CsrfTokenRepository csrfTokenRepository(
        @Value("${server.servlet.session.cookie.secure:false}") boolean secureCookie
    ) {
        CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
        repository.setCookiePath("/");
        repository.setHeaderName("X-XSRF-TOKEN");
        repository.setCookieCustomizer(cookie -> cookie
            .httpOnly(false)
            .secure(secureCookie)
            .sameSite("Lax")
        );
        return repository;
    }

    @Bean
    AuthenticationManager authenticationManager(
        AppUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setHideUserNotFoundExceptions(true);
        return new ProviderManager(provider);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JsonAuthenticationEntryPoint authenticationEntryPoint,
        JsonAccessDeniedHandler accessDeniedHandler,
        SecurityContextRepository securityContextRepository,
        CsrfTokenRepository csrfTokenRepository,
        Clock clock,
        @Value("${app.security.session-idle-timeout:8h}") Duration idleTimeout,
        SessionCookieService sessionCookieService
    ) throws Exception {
        return http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/health",
                    "/actuator/health",
                    "/api/auth/csrf",
                    "/api/auth/login",
                    "/api/auth/logout"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .requestCache(cache -> cache.disable())
            .logout(logout -> logout.disable())
            .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository)
                .requireExplicitSave(true)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .addFilterAfter(new AuthenticatedSessionActivityFilter(
                clock, idleTimeout, authenticationEntryPoint, sessionCookieService
            ), SecurityContextHolderFilter.class)
            .build();
    }
}
