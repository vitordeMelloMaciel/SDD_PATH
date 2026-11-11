package br.org.germinare.equipamentos.user;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {
    @Override
    public String convertToDatabaseColumn(UserRole role) {
        return role == null ? null : role.value();
    }

    @Override
    public UserRole convertToEntityAttribute(String value) {
        return value == null ? null : UserRole.fromValue(value);
    }
}
