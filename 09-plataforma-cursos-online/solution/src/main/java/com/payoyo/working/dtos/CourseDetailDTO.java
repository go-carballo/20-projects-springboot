package com.payoyo.working.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para vista detallada de un curso específico.
 * Incluye toda la información disponible del curso.
 * 
 * Uso típico:
 * - Página de detalle de curso
 * - Respuesta después de crear/actualizar
 * - Vista de administración
 */
public record CourseDetailDTO(
        Long id,
        String title,
        String description,
        String instructor,
        Integer durationHours,
        
        /**
         * Nivel del curso como String.
         */
        String level,
        
        /**
         * Precio original sin descuento.
         */
        BigDecimal price,
        
        /**
         * Porcentaje de descuento aplicado (0-100).
         */
        Integer discount,
        
        /**
         * Precio final con descuento calculado.
         * Este campo NO se persiste en DB, se calcula en runtime.
         */
        BigDecimal finalPrice,
        
        String category,
        String videoUrl,
        String thumbnail,
        LocalDate publishedDate,
        Integer enrolledStudents,
        Double averageRating
) {}