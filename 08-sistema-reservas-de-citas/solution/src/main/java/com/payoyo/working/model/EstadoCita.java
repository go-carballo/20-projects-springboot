package com.payoyo.working.model;

/**
 * Enum que representa los posibles estados de una cita.
 * 
 * Transiciones válidas:
 * - PENDIENTE → CONFIRMADA (cliente confirma)
 * - PENDIENTE → CANCELADA (cancelación temprana)
 * - CONFIRMADA → CANCELADA (cancelación tras confirmar)
 * - CONFIRMADA → COMPLETADA (servicio prestado)
 * 
 * Transiciones NO permitidas:
 * - CANCELADA no puede pasar a CONFIRMADA o COMPLETADA
 * - COMPLETADA no puede cambiar a ningún otro estado
 */
public enum EstadoCita {
    
    /**
     * Estado inicial cuando se crea la cita.
     * Requiere confirmación del cliente.
     */
    PENDIENTE,
    
    /**
     * El cliente ha confirmado su asistencia.
     */
    CONFIRMADA,
    
    /**
     * La cita fue cancelada por el cliente o el sistema.
     */
    CANCELADA,
    
    /**
     * El servicio fue prestado exitosamente.
     * Estado final, no permite cambios posteriores.
     */
    COMPLETADA
}
