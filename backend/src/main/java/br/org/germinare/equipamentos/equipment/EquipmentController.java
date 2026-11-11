package br.org.germinare.equipamentos.equipment;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.org.germinare.equipamentos.loan.Loan;
import br.org.germinare.equipamentos.loan.LoanRepository;
import br.org.germinare.equipamentos.user.AppUser;
import br.org.germinare.equipamentos.user.AppUserRepository;

/**
 * Spec 004: catálogo com exatamente seis campos públicos (CA-07). A resposta
 * não lê empréstimos, então nunca expõe dono, datas ou ID de empréstimo.
 */
@RestController
class EquipmentController {
    private final EquipmentRepository equipment;
    private final EquipmentImageUrls imageUrls;
    private final LoanRepository loans;
    private final AppUserRepository users;

    EquipmentController(EquipmentRepository equipment, EquipmentImageUrls imageUrls, LoanRepository loans, AppUserRepository users) {
        this.equipment = equipment;
        this.imageUrls = imageUrls;
        this.loans = loans;
        this.users = users;
    }

    @GetMapping("/api/equipment")
    List<EquipmentResponse> catalog() {
        Map<UUID, String> holders = loans.findAll().stream()
            .filter(loan -> loan.returnedAt() == null)
            .collect(Collectors.toMap(Loan::equipmentId,
                loan -> users.findById(loan.borrowerId()).map(AppUser::fullName).orElse("")));
        return equipment.findAllByOrderByAssetCodeAsc().stream()
            .map(item -> new EquipmentResponse(
                item.id(),
                item.assetCode(),
                item.name(),
                item.category(),
                item.status(),
                imageUrls.urlFor(item.imageObjectKey()),
                holders.get(item.id())
            ))
            .toList();
    }

    record EquipmentResponse(UUID id, String assetCode, String name, String category, String status, String imageUrl,
                             String borrowerName) {
    }
}
