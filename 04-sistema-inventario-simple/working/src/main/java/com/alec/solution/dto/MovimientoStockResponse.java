package com.alec.solution.dto;

import com.alec.solution.entity.TipoMovimiento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Respuesta con informacion del movimiento de stock")
public record MovimientoStockResponse(
        @Schema(description = "ID unico del movimiento", example = "1")
        Long id,

        @Schema(description = "ID del producto", example = "1")
        Long productoId,

        @Schema(description = "Nombre del producto", example = "Laptop HP Pavilion")
        String productoNombre,

        @Schema(description = "SKU del producto", example = "PRD-0001")
        String productoSku,

        @Schema(description = "Tipo de movimiento", example = "ENTRADA")
        TipoMovimiento tipo,

        @Schema(description = "Cantidad del movimiento", example = "10")
        Integer cantidad,

        @Schema(description = "Stock antes del movimiento", example = "50")
        Integer stockAnterior,

        @Schema(description = "Stock despues del movimiento", example = "60")
        Integer stockNuevo,

        @Schema(description = "Motivo del movimiento", example = "Compra a proveedor")
        String motivo,

        @Schema(description = "Fecha y hora del movimiento")
        LocalDateTime fechaMovimiento
) {}
