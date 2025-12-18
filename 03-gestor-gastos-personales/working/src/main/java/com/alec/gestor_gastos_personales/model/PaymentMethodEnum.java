package com.alec.gestor_gastos_personales.model;

/**
 * Enumeración que representa los métodos de pago disponibles.
 */
public enum PaymentMethodEnum {
    CASH("Efectivo"),
    DEBIT_CARD("Tarjeta de débito"),
    CREDIT_CARD("Tarjeta de crédito"),
    BANK_TRANSFER("Transferencia bancaria"),
    DIGITAL_WALLET("Billetera digital");

    private final String displayName;

    PaymentMethodEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
