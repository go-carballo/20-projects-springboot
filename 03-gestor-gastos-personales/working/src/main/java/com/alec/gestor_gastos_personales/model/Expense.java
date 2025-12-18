package com.alec.gestor_gastos_personales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa un gasto personal.
 */
@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(min = 3, max = 200, message = "La descripción debe tener entre 3 y 200 caracteres")
    @Column(nullable = false, length = 200)
    private String description;

    @NotNull(message = "El monto no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "El monto debe tener como máximo 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "La categoría no puede ser nula")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryEnum category;

    @NotNull(message = "La fecha no puede ser nula")
    @PastOrPresent(message = "La fecha no puede ser futura")
    @Column(nullable = false)
    private LocalDate date;

    @NotNull(message = "El método de pago no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethodEnum paymentMethod;
}
