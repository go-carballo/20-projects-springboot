package com.payoyo.working.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un libro por su ISBN.
 * 
 * Contextos de uso:
 * - GET /api/books/{isbn} cuando el ISBN no existe
 * - PUT /api/books/{isbn} al intentar actualizar un libro inexistente
 * - PATCH /api/books/{isbn}/stock al intentar actualizar stock de libro inexistente
 * - DELETE /api/books/{isbn} al intentar eliminar un libro inexistente
 * 
 * Esta excepción se captura en el Controller o en un @ControllerAdvice
 * para devolver un código HTTP 404 Not Found al cliente.
 * 
 * Hereda de RuntimeException (unchecked) para no forzar try-catch
 * en cada método que la lance.
 */
public class BookNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje descriptivo del error
     */
    public BookNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa raíz.
     * 
     * Útil cuando el error se origina en una excepción más profunda
     * de JPA o base de datos.
     * 
     * @param message Mensaje descriptivo del error
     * @param cause Excepción que originó este error
     */
    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}