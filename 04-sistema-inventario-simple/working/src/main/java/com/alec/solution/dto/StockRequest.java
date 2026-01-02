package com.alec.solution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request para movimientos de stock (entrada/salida)")
public record StockRequest(
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        @Schema(description = "Cantidad a mover", example = "10")
        Integer cantidad,

        @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
        @Schema(description = "Motivo del movimiento", example = "Compra a proveedor")
        String motivo
) {}
