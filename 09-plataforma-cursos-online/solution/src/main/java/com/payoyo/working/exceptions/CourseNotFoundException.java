package com.payoyo.working.exceptions;

/**
 * Excepción lanzada cuando se intenta acceder a un curso que no existe en la base de datos.
 * 
 * Casos de uso:
 * - GET /api/courses/{id} con ID inexistente
 * - PUT /api/courses/{id} con ID inexistente
 * - DELETE /api/courses/{id} con ID inexistente
 * - POST /api/courses/{id}/enroll con ID inexistente
 */
public class CourseNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje descriptivo del error
     */
    public CourseNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor conveniente que genera mensaje estándar con el ID.
     * 
     * @param id ID del curso no encontrado
     */
    public CourseNotFoundException(Long id) {
        super("No se encontró el curso con ID: " + id);
    }
}