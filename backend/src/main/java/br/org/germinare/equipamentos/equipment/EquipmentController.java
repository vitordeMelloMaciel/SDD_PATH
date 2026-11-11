package br.org.germinare.equipamentos.equipment;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spec 004: catálogo com exatamente seis campos públicos (CA-07). A resposta
 * não lê empréstimos, então nunca expõe dono, datas ou ID de empréstimo.
 */
@RestController
class EquipmentController {
    private final EquipmentRepository equipment;
    private final EquipmentImageUrls imageUrls;

    EquipmentController(EquipmentRepository equipment, EquipmentImageUrls imageUrls) {
        this.equipment = equipment;
        this.imageUrls = imageUrls;
    }

    @GetMapping("/api/equipment")
    List<EquipmentResponse> catalog() {
        return equipment.findAllByOrderByAssetCodeAsc().stream()
            .map(item -> new EquipmentResponse(
                item.id(),
                item.assetCode(),
                item.name(),
                item.category(),
                item.status(),
                imageUrls.urlFor(item.imageObjectKey())
            ))
            .toList();
    }

    record EquipmentResponse(UUID id, String assetCode, String name, String category, String status, String imageUrl) {
    }
}
