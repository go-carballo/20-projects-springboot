package com.alec.gestor_gastos_personales.model;

/**
 * Enumeración que representa las categorías de gastos disponibles.
 */
public enum CategoryEnum {
    FOOD("Alimentación"),
    TRANSPORT("Transporte"),
    ENTERTAINMENT("Entretenimiento"),
    HEALTH("Salud"),
    EDUCATION("Educación"),
    UTILITIES("Servicios"),
    SHOPPING("Compras"),
    OTHER("Otros");

    private final String displayName;

    CategoryEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
