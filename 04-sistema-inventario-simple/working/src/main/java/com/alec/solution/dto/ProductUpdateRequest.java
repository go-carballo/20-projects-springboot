package com.alec.solution.dto;

import com.alec.solution.entity.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Request para actualizar un producto existente")
public record ProductUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
        @Schema(description = "Nombre del producto", example = "Laptop HP Pavilion")
        String nombre,

        @Size(max = 1000, message = "La descripcion no puede exceder 1000 caracteres")
        @Schema(description = "Descripcion detallada del producto", example = "Laptop con procesador Intel i7, 16GB RAM")
        String descripcion,

        @NotNull(message = "El stock minimo es obligatorio")
        @Min(value = 0, message = "El stock minimo no puede ser negativo")
        @Schema(description = "Cantidad minima antes de alerta de reabastecimiento", example = "10")
        Integer stockMinimo,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El precio debe tener maximo 10 digitos enteros y 2 decimales")
        @Schema(description = "Precio unitario del producto", example = "1299.99")
        BigDecimal precio,

        @NotNull(message = "La categoria es obligatoria")
        @Schema(description = "Categoria del producto", example = "ELECTRONICA")
        Categoria categoria
) {}
