package br.org.germinare.equipamentos.user;

import java.util.Arrays;

public enum UserRole {
    COLABORADOR("colaborador"),
    OPERACOES("operacoes");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static UserRole fromValue(String value) {
        return Arrays.stream(values())
            .filter(role -> role.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown user role"));
    }
}
