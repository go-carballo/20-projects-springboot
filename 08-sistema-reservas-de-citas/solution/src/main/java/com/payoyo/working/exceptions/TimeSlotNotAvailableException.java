package com.payoyo.working.exceptions;

/**
 * Excepción lanzada cuando se intenta reservar un horario ya ocupado.
 * 
 * Se mapea a HTTP 409 Conflict en el GlobalExceptionHandler.
 * 
 * Casos de uso:
 * - POST /api/appointments con horario que solapa con otra cita activa
 * - PUT /api/appointments/{id} actualizando a horario ocupado
 * 
 * 409 Conflict indica que la solicitud es válida, pero no se puede procesar
 * debido a un conflicto con el estado actual del recurso (horario ocupado).
 * 
 * Diferencia con 400 Bad Request:
 * - 400: Los datos son intrínsecamente inválidos (formato, validaciones)
 * - 409: Los datos son válidos pero conflictúan con el estado del sistema
 * 
 * Algoritmo de detección:
 * Se usa el query findOverlappingAppointments() del repository para detectar
 * si el nuevo horario solapa con citas en estado PENDIENTE o CONFIRMADA.
 */
public class TimeSlotNotAvailableException extends RuntimeException {
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * Ejemplo de mensaje:
     * "El horario solicitado (10:00 - 11:00) ya está ocupado"
     * 
     * @param message Mensaje descriptivo del conflicto de horario
     */
    public TimeSlotNotAvailableException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa raíz.
     * 
     * @param message Mensaje descriptivo
     * @param cause Excepción que causó este error
     */
    public TimeSlotNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
