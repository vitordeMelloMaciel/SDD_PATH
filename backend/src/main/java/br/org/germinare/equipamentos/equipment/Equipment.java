package br.org.germinare.equipamentos.equipment;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Equipamento da tabela existente (Specs 002 e 003). A aplicação nunca grava
 * esta tabela: o status muda só pela trigger da V2.
 */
@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    private UUID id;

    @Column(name = "asset_code", nullable = false, insertable = false, updatable = false)
    private String assetCode;

    @Column(nullable = false, insertable = false, updatable = false)
    private String name;

    @Column(nullable = false, insertable = false, updatable = false)
    private String category;

    @Column(nullable = false, insertable = false, updatable = false)
    private String status;

    @Column(name = "image_object_key", insertable = false, updatable = false)
    private String imageObjectKey;

    protected Equipment() {
    }

    public UUID id() {
        return id;
    }

    public String assetCode() {
        return assetCode;
    }

    public String name() {
        return name;
    }

    public String category() {
        return category;
    }

    public String status() {
        return status;
    }

    public String imageObjectKey() {
        return imageObjectKey;
    }
}
