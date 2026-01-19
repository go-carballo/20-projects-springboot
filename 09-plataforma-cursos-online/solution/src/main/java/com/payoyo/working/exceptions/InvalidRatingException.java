package com.payoyo.working.exceptions;

/**
 * Excepción lanzada cuando se intenta establecer una calificación fuera del rango válido.
 * 
 * Rango válido: 0.0 a 5.0 (estrellas)
 * 
 * Caso de uso:
 * - PUT /api/courses/{id}/rating?rating=6.0 (fuera de rango)
 * - PUT /api/courses/{id}/rating?rating=-1.0 (negativo)
 */
public class InvalidRatingException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje descriptivo del error
     */
    public InvalidRatingException(String message) {
        super(message);
    }

    /**
     * Constructor conveniente que genera mensaje con el rating inválido.
     * 
     * @param rating Valor de rating que causó el error
     */
    public InvalidRatingException(Double rating) {
        super("Rating inválido: " + rating + ". Debe estar entre 0.0 y 5.0");
    }
}
