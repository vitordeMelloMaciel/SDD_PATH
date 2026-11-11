package br.org.germinare.equipamentos.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
class AuthExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<AuthErrorResponse> invalidJson() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new AuthErrorResponse("INVALID_REQUEST", "Informe e-mail e senha.")
        );
    }
}
