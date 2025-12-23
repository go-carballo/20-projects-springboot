package com.payoyo.working.dtos;

import java.math.BigDecimal;

/**
 * DTO para vista compacta de cursos en listados y catálogos.
 * Contiene únicamente la información esencial para mostrar en tarjetas (cards).
 * 
 * Uso típico:
 * - Listado general de cursos
 * - Resultados de búsqueda
 * - Cursos relacionados
 * - Top rankings
 */
public record CourseCardDTO(
        Long id,
        String title,
        String instructor,
        Integer durationHours,
        
        /**
         * Nivel del curso como String para facilitar serialización JSON.
         * Valores: "BEGINNER", "INTERMEDIATE", "ADVANCED"
         */
        String level,
        
        /**
         * Precio final ya calculado con descuento aplicado.
         * Se calcula en Service: price - (price * discount / 100)
         */
        BigDecimal finalPrice,
        
        String thumbnail,
        Integer enrolledStudents,
        
        /**
         * Calificación promedio del curso (0.0 a 5.0).
         */
        Double averageRating
) {}