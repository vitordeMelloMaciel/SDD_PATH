package br.org.germinare.equipamentos.loan;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Representação de Meus empréstimos (Spec 004): também é o corpo do 201. */
public record LoanView(UUID id, String assetCode, String equipmentName, OffsetDateTime dueAt, boolean overdue) {
}
