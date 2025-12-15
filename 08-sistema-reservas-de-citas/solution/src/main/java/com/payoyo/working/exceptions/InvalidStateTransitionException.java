package com.payoyo.working.exceptions;

/**
 * Excepción lanzada cuando se intenta una transición de estado inválida.
 * 
 * Se mapea a HTTP 400 Bad Request en el GlobalExceptionHandler.
 * 
 * Máquina de estados implementada:
 * 
 *     PENDIENTE
 *       ↓    ↓
 *    CONF   CANC
 *     ↓      ✗
 *   COMP    
 * 
 * Transiciones VÁLIDAS:
 * - PENDIENTE   → CONFIRMADA   (cliente confirma)
 * - PENDIENTE   → CANCELADA    (cancelación temprana)
 * - CONFIRMADA  → CANCELADA    (cancelación tras confirmar)
 * - CONFIRMADA  → COMPLETADA   (servicio prestado)
 * 
 * Transiciones INVÁLIDAS:
 * - CANCELADA   → CONFIRMADA   ✗ (no se puede reactivar cita cancelada)
 * - CANCELADA   → COMPLETADA   ✗ (no se puede completar cita cancelada)
 * - COMPLETADA  → cualquiera   ✗ (estado final, no permite cambios)
 * - CANCELADA   → CANCELADA    ✗ (ya está cancelada)
 * 
 * Casos de uso:
 * - PATCH /api/appointments/{id}/confirmar con estado ≠ PENDIENTE
 * - PATCH /api/appointments/{id}/cancelar con estado = COMPLETADA
 * - PATCH /api/appointments/{id}/completar con estado ≠ CONFIRMADA
 */
public class InvalidStateTransitionException extends RuntimeException {
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * Ejemplos de mensajes:
     * - "Solo se pueden confirmar citas en estado PENDIENTE"
     * - "No se puede cancelar una cita completada"
     * - "La cita ya está cancelada"
     * 
     * @param message Mensaje descriptivo de la transición inválida
     */
    public InvalidStateTransitionException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa raíz.
     * 
     * @param message Mensaje descriptivo
     * @param cause Excepción que causó este error
     */
    public InvalidStateTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
