package br.org.germinare.equipamentos.auth;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
class SessionCookieService {
    private final boolean secure;

    SessionCookieService(
        @Value("${server.servlet.session.cookie.secure:false}") boolean secure
    ) {
        this.secure = secure;
    }

    void expire(HttpServletResponse response) {
        ResponseCookie expiredCookie = ResponseCookie.from("SESSION", "")
            .path("/")
            .httpOnly(true)
            .secure(secure)
            .sameSite("Lax")
            .maxAge(Duration.ZERO)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }
}
