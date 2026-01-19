package com.payoyo.working.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.payoyo.working.entity.Level;


/**
 * DTO para actualizar cursos existentes.
 * Similar a CourseCreateDTO pero TODOS los campos son opcionales.
 * Solo se actualizan los campos proporcionados (no-null).
 * 
 * Uso:
 * - PUT /api/courses/{id}
 * - Actualización parcial de datos
 */
public record CourseUpdateDTO(
        
        /**
         * Todos los campos son opcionales (pueden ser null).
         * Las validaciones solo se aplican si el campo está presente.
         */
        @Size(max = 200, message = "El título no puede superar 200 caracteres")
        String title,
        
        @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres")
        String description,
        
        @Size(max = 150, message = "El instructor no puede superar 150 caracteres")
        String instructor,
        
        @Min(value = 1, message = "La duración mínima es 1 hora")
        @Max(value = 500, message = "La duración máxima es 500 horas")
        Integer durationHours,
        
        Level level,
        
        @DecimalMin(value = "0.00", message = "El precio mínimo es 0")
        @DecimalMax(value = "9999.99", message = "El precio máximo es 9999.99")
        BigDecimal price,
        
        @Min(value = 0, message = "El descuento mínimo es 0%")
        @Max(value = 100, message = "El descuento máximo es 100%")
        Integer discount,
        
        @Size(max = 50, message = "La categoría no puede superar 50 caracteres")
        String category,
        
        @Size(max = 500, message = "La URL del video no puede superar 500 caracteres")
        String videoUrl,
        
        @Size(max = 500, message = "La URL del thumbnail no puede superar 500 caracteres")
        String thumbnail,
        
        LocalDate publishedDate
) {}