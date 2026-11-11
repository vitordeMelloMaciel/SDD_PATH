package br.org.germinare.equipamentos.equipment;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {
    List<Equipment> findAllByOrderByAssetCodeAsc();
}
