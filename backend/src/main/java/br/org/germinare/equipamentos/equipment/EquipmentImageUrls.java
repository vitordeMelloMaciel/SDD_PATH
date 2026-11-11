package br.org.germinare.equipamentos.equipment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Spec 003, CA-04: monta a URL pública da foto a partir de
 * {@code EQUIPMENT_IMAGE_BASE_URL} e da chave {@code equipment/<patrimonio>.jpg}.
 */
@Component
public class EquipmentImageUrls {
    private final String baseUrl;

    public EquipmentImageUrls(@Value("${EQUIPMENT_IMAGE_BASE_URL:}") String baseUrl) {
        this.baseUrl = normalize(baseUrl);
    }

    /** Devolve {@code base + "/" + chave}, ou {@code null} quando falta a base ou a chave. */
    public String urlFor(String objectKey) {
        if (baseUrl == null || objectKey == null || objectKey.isBlank()) {
            return null;
        }
        return baseUrl + "/" + objectKey;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String base = value.strip();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base.isEmpty() ? null : base;
    }
}
