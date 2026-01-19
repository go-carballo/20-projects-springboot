package com.payoyo.working.entity;

/**
 * Enum que representa los niveles de dificultad disponibles para los cursos.
 * Se almacena como STRING en la base de datos para mejor legibilidad.
 */
public enum Level {
    /**
     * Curso para principiantes sin conocimientos previos
     */
    BEGINNER,
    
    /**
     * Curso para estudiantes con conocimientos básicos
     */
    INTERMEDIATE,
    
    /**
     * Curso para estudiantes avanzados con experiencia previa
     */
    ADVANCED
}