package com.payoyo.working.exceptions;

/**
 * Excepción lanzada cuando no se encuentra una cita solicitada.
 * 
 * Se mapea a HTTP 404 Not Found en el GlobalExceptionHandler.
 * 
 * Casos de uso:
 * - GET /api/appointments/{id} con ID inexistente
 * - GET /api/appointments/codigo/{codigo} con código inválido
 * - PUT/PATCH/DELETE de una cita que no existe
 * 
 * Extiende RuntimeException (unchecked) porque:
 * - No es recuperable por el llamador
 * - El controlador la maneja globalmente con @ControllerAdvice
 * - Evita contaminar las firmas de métodos con throws
 */
public class AppointmentNotFoundException extends RuntimeException {
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje descriptivo del error (ej: "Cita con ID 123 no encontrada")
     */
    public AppointmentNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa raíz.
     * 
     * Útil cuando la excepción es resultado de otra excepción
     * (ej: error de BD subyacente).
     * 
     * @param message Mensaje descriptivo
     * @param cause Excepción que causó este error
     */
    public AppointmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}