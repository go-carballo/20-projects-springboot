package com.alec.solution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request para ajuste de inventario")
public record AjusteStockRequest(
        @NotNull(message = "La nueva cantidad es obligatoria")
        @Min(value = 0, message = "La cantidad no puede ser negativa")
        @Schema(description = "Nueva cantidad en stock", example = "100")
        Integer nuevaCantidad,

        @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
        @Schema(description = "Motivo del ajuste", example = "Correccion por inventario fisico")
        String motivo
) {}
