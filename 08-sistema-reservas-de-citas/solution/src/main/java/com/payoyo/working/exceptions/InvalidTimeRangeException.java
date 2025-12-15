package com.payoyo.working.exceptions;

/**
 * Excepción lanzada cuando el rango de horario de una cita es inválido.
 * 
 * Se mapea a HTTP 400 Bad Request en el GlobalExceptionHandler.
 * 
 * Casos de uso:
 * - Hora de fin anterior o igual a hora de inicio
 * - Duración menor a 15 minutos
 * - Duración mayor a 8 horas
 * - Horario fuera del rango laboral (08:00 - 20:00)
 * - Anticipación mínima no cumplida (< 2 horas antes)
 * 
 * Nota: Las validaciones básicas (@NotNull, @FutureOrPresent, @AssertTrue)
 * se manejan automáticamente por Bean Validation.
 * Esta excepción es para reglas de negocio adicionales validadas en Service.
 */
public class InvalidTimeRangeException extends RuntimeException {
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * Ejemplos de mensajes:
     * - "La cita debe iniciar entre las 08:00 y las 20:00"
     * - "Las citas deben crearse con al menos 2 horas de anticipación"
     * 
     * @param message Mensaje descriptivo del error de validación
     */
    public InvalidTimeRangeException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa raíz.
     * 
     * @param message Mensaje descriptivo
     * @param cause Excepción que causó este error
     */
    public InvalidTimeRangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
