package com.payoyo.working.dtos;

import java.math.BigDecimal;

/**
 * DTO de respuesta después de inscribir un estudiante en un curso.
 * Devuelve información actualizada sobre la inscripción.
 * 
 * Uso:
 * - POST /api/courses/{id}/enroll
 * - Confirma inscripción con precio a pagar
 * - Muestra contador actualizado de estudiantes
 */
public record CourseEnrollmentDTO(
        /**
         * ID del curso en el que se realizó la inscripción.
         */
        Long courseId,
        
        /**
         * Número actualizado de estudiantes inscritos.
         * Se incrementa en 1 con cada inscripción.
         */
        Integer enrolledStudents,
        
        /**
         * Precio final que debe pagar el estudiante.
         * Incluye descuento aplicado al momento de la inscripción.
         */
        BigDecimal finalPrice
) {}