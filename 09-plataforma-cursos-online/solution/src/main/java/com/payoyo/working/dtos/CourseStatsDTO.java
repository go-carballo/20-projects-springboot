package com.payoyo.working.dtos;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO que contiene estadísticas agregadas de todos los cursos de la plataforma.
 * Combina datos de múltiples queries de repositorio y cálculos.
 * 
 * Uso:
 * - GET /api/courses/stats
 * - Dashboard de administración
 * - Reportes y analytics
 * 
 * Características:
 * - Contiene un Map para agrupar cursos por nivel
 * - Incluye un nested DTO (CourseCardDTO) del curso mejor valorado
 * - Todos los valores son calculados en tiempo real
 */
public record CourseStatsDTO(
        /**
         * Número total de cursos en la plataforma.
         */
        Long totalCourses,
        
        /**
         * Suma de todos los estudiantes inscritos en todos los cursos.
         */
        Integer totalEnrolledStudents,
        
        /**
         * Calificación promedio global de todos los cursos (0.0 a 5.0).
         */
        Double averageRating,
        
        /**
         * Precio promedio de todos los cursos.
         */
        BigDecimal averagePrice,
        
        /**
         * Map que agrupa cursos por nivel de dificultad.
         * Key: Nombre del nivel ("BEGINNER", "INTERMEDIATE", "ADVANCED")
         * Value: Cantidad de cursos en ese nivel
         * 
         * Ejemplo: {"BEGINNER": 5, "INTERMEDIATE": 7, "ADVANCED": 3}
         */
        Map<String, Long> coursesByLevel,
        
        /**
         * DTO completo del curso mejor valorado en la plataforma.
         * Nested DTO que permite acceder a toda la información del top curso.
         * Puede ser null si no hay cursos.
         */
        CourseCardDTO topRatedCourse
) {}