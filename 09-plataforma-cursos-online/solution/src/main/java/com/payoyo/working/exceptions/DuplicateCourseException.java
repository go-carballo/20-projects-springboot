package com.payoyo.working.exceptions;

/**
 * Excepción lanzada cuando se intenta crear un curso con un título que ya existe.
 * 
 * La entidad Course tiene constraint UNIQUE en el campo 'title',
 * por lo que este error previene duplicados a nivel de aplicación
 * antes de que falle la base de datos.
 * 
 * Caso de uso:
 * - POST /api/courses con título duplicado
 */
public class DuplicateCourseException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje descriptivo del error
     */
    public DuplicateCourseException(String message) {
        super(message);
    }
}