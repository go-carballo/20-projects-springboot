package com.payoyo.working.exceptions;

/**
 * Excepción lanzada cuando se intenta crear un libro con un ISBN que ya existe.
 * 
 * Contexto de uso:
 * - POST /api/books cuando se intenta crear un libro con un ISBN duplicado
 * 
 * El ISBN es la clave primaria (PK) de la entidad Book, por lo tanto
 * debe ser único en la base de datos. Esta excepción se lanza a nivel
 * de Service antes de intentar persistir, para dar un mensaje claro
 * al cliente en lugar de dejar que falle con una excepción genérica
 * de constraint violation de JPA.
 * 
 * Esta excepción se captura en el Controller o en un @ControllerAdvice
 * para devolver un código HTTP 400 Bad Request o 409 Conflict al cliente.
 * 
 * Hereda de RuntimeException (unchecked) para no forzar try-catch
 * en cada método que la lance.
 */
public class DuplicateIsbnException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje descriptivo del error
     */
    public DuplicateIsbnException(String message) {
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
    public DuplicateIsbnException(String message, Throwable cause) {
        super(message, cause);
    }
}