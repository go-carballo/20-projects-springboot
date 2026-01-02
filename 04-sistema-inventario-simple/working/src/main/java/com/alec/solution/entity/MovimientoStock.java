package com.alec.solution.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Registro de movimiento de stock")
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del movimiento", example = "1")
    private Long id;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @Schema(description = "Producto afectado por el movimiento")
    private Product producto;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(description = "Tipo de movimiento", example = "ENTRADA")
    private TipoMovimiento tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    @Schema(description = "Cantidad del movimiento", example = "10")
    private Integer cantidad;

    @Column(nullable = false)
    @Schema(description = "Stock antes del movimiento", example = "50")
    private Integer stockAnterior;

    @Column(nullable = false)
    @Schema(description = "Stock después del movimiento", example = "60")
    private Integer stockNuevo;

    @Column(length = 500)
    @Schema(description = "Motivo o descripción del movimiento", example = "Compra a proveedor")
    private String motivo;

    @Column(nullable = false)
    @Schema(description = "Fecha y hora del movimiento")
    private LocalDateTime fechaMovimiento;

    @PrePersist
    protected void onCreate() {
        fechaMovimiento = LocalDateTime.now();
    }
}
