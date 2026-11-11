package br.org.germinare.equipamentos.loan;

import org.springframework.http.HttpStatus;

/** Recusa de negócio com o envelope {code, message} da Spec 004. */
final class LoanRejection extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    private LoanRejection(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    static LoanRejection invalidRequest() {
        return new LoanRejection(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Informe um equipamento válido.");
    }

    static LoanRejection equipmentNotFound() {
        return new LoanRejection(HttpStatus.NOT_FOUND, "EQUIPMENT_NOT_FOUND", "Equipamento não encontrado.");
    }

    static LoanRejection maintenance() {
        return new LoanRejection(HttpStatus.CONFLICT, "EQUIPMENT_MAINTENANCE", "Equipamento em manutenção.");
    }

    static LoanRejection borrowed() {
        return new LoanRejection(HttpStatus.CONFLICT, "EQUIPMENT_BORROWED", "Equipamento já emprestado.");
    }

    static LoanRejection loanLimit() {
        return new LoanRejection(HttpStatus.CONFLICT, "LOAN_LIMIT", "Você já tem três empréstimos em aberto.");
    }

    static LoanRejection overdueLoan() {
        return new LoanRejection(HttpStatus.CONFLICT, "OVERDUE_LOAN", "Você tem um empréstimo em atraso.");
    }

    /** Conta desativada ou excluída descoberta sob bloqueio (Spec 004, spec.md:77-79). */
    static LoanRejection inactiveAccount() {
        return new LoanRejection(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Sessao invalida ou expirada.");
    }

    HttpStatus status() {
        return status;
    }

    String code() {
        return code;
    }
}
