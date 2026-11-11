package br.org.germinare.equipamentos.loan;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.org.germinare.equipamentos.equipment.Equipment;
import br.org.germinare.equipamentos.equipment.EquipmentRepository;
import br.org.germinare.equipamentos.user.AppUser;
import br.org.germinare.equipamentos.user.AppUserRepository;
import jakarta.persistence.EntityManager;

/**
 * Spec 004: solicitação de empréstimo com bloqueio da conta e depois do
 * equipamento, na mesma transação da inserção e na ordem da trigger da V2.
 * A recusa é classificada pela leitura sob bloqueio, nunca pelo texto da
 * exceção do PostgreSQL. O status do equipamento muda só pela trigger.
 */
@Service
class LoanService {
    private final AppUserRepository users;
    private final EquipmentRepository equipment;
    private final LoanRepository loans;
    private final EntityManager entityManager;

    LoanService(AppUserRepository users, EquipmentRepository equipment, LoanRepository loans, EntityManager entityManager) {
        this.users = users;
        this.equipment = equipment;
        this.loans = loans;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    List<LoanView> openLoans(UUID borrowerId) {
        return loans.findOpenViews(borrowerId);
    }

    @Transactional
    LoanView request(UUID borrowerId, UUID equipmentId) {
        users.findLockedById(borrowerId)
            .filter(AppUser::active)
            .orElseThrow(LoanRejection::inactiveAccount);
        Equipment item = equipment.findLockedById(equipmentId)
            .orElseThrow(LoanRejection::equipmentNotFound);

        if ("manutencao".equals(item.status())) {
            throw LoanRejection.maintenance();
        }
        if (!"disponivel".equals(item.status())) {
            throw LoanRejection.borrowed();
        }
        if (loans.countByBorrowerIdAndReturnedAtIsNull(borrowerId) >= 3) {
            throw LoanRejection.loanLimit();
        }
        if (loans.hasOverdueOpenLoan(borrowerId)) {
            throw LoanRejection.overdueLoan();
        }

        Loan loan = loans.saveAndFlush(new Loan(borrowerId, equipmentId));
        entityManager.refresh(loan);
        return new LoanView(loan.id(), item.assetCode(), item.name(), loan.dueAt(), false);
    }
}
