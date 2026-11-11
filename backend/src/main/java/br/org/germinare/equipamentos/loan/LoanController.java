package br.org.germinare.equipamentos.loan;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.org.germinare.equipamentos.auth.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;

/** Spec 004: `POST /api/loans` e `GET /api/loans/me` (CA-02 e CA-03). */
@RestController
class LoanController {
    private final LoanService service;

    LoanController(LoanService service) {
        this.service = service;
    }

    @GetMapping("/api/loans/me")
    List<LoanView> mine(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.openLoans(user.id());
    }

    @PostMapping("/api/loans")
    ResponseEntity<LoanView> request(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody LoanRequest body) {
        if (body == null || body.equipmentId() == null) {
            throw LoanRejection.invalidRequest();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.request(user.id(), body.equipmentId()));
    }

    /** JSON inválido, UUID inválido ou corpo que não é JSON: 400 INVALID_REQUEST. */
    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
    ResponseEntity<LoanError> unreadableBody() {
        return rejection(LoanRejection.invalidRequest(), null);
    }

    @ExceptionHandler(LoanRejection.class)
    ResponseEntity<LoanError> rejection(LoanRejection failure, HttpServletRequest request) {
        if (failure.status() == HttpStatus.UNAUTHORIZED && request != null) {
            var session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            SecurityContextHolder.clearContext();
        }
        return ResponseEntity.status(failure.status()).body(new LoanError(failure.code(), failure.getMessage()));
    }

    record LoanRequest(UUID equipmentId) {
    }

    record LoanError(String code, String message) {
    }
}
