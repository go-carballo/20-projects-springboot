package com.payoyo.working.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.payoyo.working.model.EstadoCita;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para devolver información completa de una cita al cliente.
 * 
 * Incluye todos los datos de la cita más campos calculados (duracionMinutos)
 * y metadatos de auditoría (timestamps).
 * 
 * Este DTO se usa en:
 * - GET /api/appointments/{id}
 * - GET /api/appointments (lista completa)
 * - PUT /api/appointments/{id} (tras actualizar)
 * - PATCH /api/appointments/{id}/confirmar|cancelar|completar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDTO {
    
    /**
     * Identificador único de la cita.
     */
    private Long id;
    
    /**
     * Nombre completo del cliente.
     */
    private String nombreCliente;
    
    /**
     * Email de contacto del cliente.
     */
    private String email;
    
    /**
     * Teléfono de contacto del cliente.
     */
    private String telefono;
    
    /**
     * Fecha de la cita.
     */
    private LocalDate fecha;
    
    /**
     * Hora de inicio del servicio.
     */
    private LocalTime horaInicio;
    
    /**
     * Hora de finalización del servicio.
     */
    private LocalTime horaFin;
    
    /**
     * Tipo de servicio.
     */
    private String servicio;
    
    /**
     * Estado actual de la cita.
     */
    private EstadoCita estado;
    
    /**
     * Precio del servicio.
     */
    private BigDecimal precio;
    
    /**
     * Notas adicionales (puede ser null).
     */
    private String notas;
    
    /**
     * Código único de confirmación.
     * Formato: "APT-XXXX"
     * 
     * El cliente puede usar este código para:
     * - Confirmar su cita
     * - Consultar detalles
     * - Cancelar la cita
     */
    private String codigoConfirmacion;
    
    /**
     * Duración calculada de la cita en minutos.
     * 
     * Este campo NO existe en la entidad, se calcula en el mapeo:
     * ChronoUnit.MINUTES.between(horaInicio, horaFin)
     * 
     * Útil para mostrar al cliente sin que tenga que calcular la diferencia.
     */
    private Long duracionMinutos;
    
    /**
     * Timestamp de creación de la cita.
     * No se actualiza tras modificaciones.
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp de última actualización.
     * Se actualiza automáticamente en cada modificación.
     */
    private LocalDateTime updatedAt;
}
