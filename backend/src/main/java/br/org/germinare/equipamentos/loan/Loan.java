package br.org.germinare.equipamentos.loan;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

/**
 * Empréstimo da tabela existente (Spec 002). A aplicação só insere conta e
 * equipamento: `loaned_at` vem do default do banco, e `due_at` vem da
 * trigger da V2 (`loaned_at + 14 days`).
 */
@Entity
@Table(name = "loan")
public class Loan {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "borrower_id", nullable = false, updatable = false)
    private UUID borrowerId;

    @Column(name = "equipment_id", nullable = false, updatable = false)
    private UUID equipmentId;

    @Column(name = "loaned_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime loanedAt;

    @Column(name = "due_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dueAt;

    @Column(name = "returned_at", insertable = false, updatable = false)
    private OffsetDateTime returnedAt;

    protected Loan() {
    }

    Loan(UUID borrowerId, UUID equipmentId) {
        this.borrowerId = borrowerId;
        this.equipmentId = equipmentId;
    }

    public UUID id() {
        return id;
    }

    public UUID borrowerId() {
        return borrowerId;
    }

    public UUID equipmentId() {
        return equipmentId;
    }

    public OffsetDateTime loanedAt() {
        return loanedAt;
    }

    public OffsetDateTime dueAt() {
        return dueAt;
    }

    public OffsetDateTime returnedAt() {
        return returnedAt;
    }
}
