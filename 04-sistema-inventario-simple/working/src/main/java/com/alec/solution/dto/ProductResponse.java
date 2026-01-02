package com.alec.solution.dto;

import com.alec.solution.entity.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Respuesta con informacion del producto")
public record ProductResponse(
        @Schema(description = "ID unico del producto", example = "1")
        Long id,

        @Schema(description = "Nombre del producto", example = "Laptop HP Pavilion")
        String nombre,

        @Schema(description = "Descripcion detallada del producto", example = "Laptop con procesador Intel i7, 16GB RAM")
        String descripcion,

        @Schema(description = "Cantidad actual en stock", example = "50")
        Integer cantidad,

        @Schema(description = "Cantidad minima antes de alerta de reabastecimiento", example = "10")
        Integer stockMinimo,

        @Schema(description = "Precio unitario del producto", example = "1299.99")
        BigDecimal precio,

        @Schema(description = "Codigo SKU unico del producto", example = "PRD-0001")
        String sku,

        @Schema(description = "Categoria del producto", example = "ELECTRONICA")
        Categoria categoria,

        @Schema(description = "Indica si el producto esta activo", example = "true")
        Boolean activo,

        @Schema(description = "Indica si el producto requiere reabastecimiento", example = "false")
        Boolean requiereReabastecimiento,

        @Schema(description = "Fecha y hora de la ultima actualizacion")
        LocalDateTime ultimaActualizacion
) {}
