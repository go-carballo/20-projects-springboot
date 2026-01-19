package com.payoyo.working.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.payoyo.working.entity.Level;

/**
 * DTO para crear nuevos cursos.
 * Contiene validaciones Bean Validation que se activan con @Valid en el Controller.
 * 
 * Validaciones aplicadas:
 * - Campos obligatorios con @NotNull/@NotBlank
 * - Límites de tamaño con @Size
 * - Rangos numéricos con @Min/@Max
 * - Precisión de decimales con @DecimalMin/@DecimalMax
 */
public record CourseCreateDTO(
        
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar 200 caracteres")
        String title,
        
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres")
        String description,
        
        @NotBlank(message = "El instructor es obligatorio")
        @Size(max = 150, message = "El instructor no puede superar 150 caracteres")
        String instructor,
        
        @NotNull(message = "La duración es obligatoria")
        @Min(value = 1, message = "La duración mínima es 1 hora")
        @Max(value = 500, message = "La duración máxima es 500 horas")
        Integer durationHours,
        
        /**
         * Enum Level - Spring lo convierte automáticamente desde JSON.
         * Valores aceptados: BEGINNER, INTERMEDIATE, ADVANCED
         */
        @NotNull(message = "El nivel es obligatorio")
        Level level,
        
        /**
         * Precio del curso con validación de rango.
         * Se usa BigDecimal para precisión monetaria.
         */
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio mínimo es 0")
        @DecimalMax(value = "9999.99", message = "El precio máximo es 9999.99")
        BigDecimal price,
        
        /**
         * Descuento opcional (0-100 representa porcentaje).
         * Si no se proporciona, se establecerá a 0 en el Service.
         */
        @Min(value = 0, message = "El descuento mínimo es 0%")
        @Max(value = 100, message = "El descuento máximo es 100%")
        Integer discount,
        
        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 50, message = "La categoría no puede superar 50 caracteres")
        String category,
        
        /**
         * Campos opcionales - pueden ser null.
         */
        @Size(max = 500, message = "La URL del video no puede superar 500 caracteres")
        String videoUrl,
        
        @Size(max = 500, message = "La URL del thumbnail no puede superar 500 caracteres")
        String thumbnail,
        
        /**
         * Fecha de publicación opcional.
         * Si no se proporciona, se usará la fecha actual en el Service.
         */
        LocalDate publishedDate
) {}