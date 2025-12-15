package com.payoyo.working.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

import com.payoyo.working.model.EstadoCita;

/**
 * DTO simplificado para respuesta tras crear una cita.
 * 
 * Proporciona información esencial al cliente inmediatamente después
 * de realizar la reserva, junto con un mensaje de confirmación.
 * 
 * Este DTO se usa exclusivamente en:
 * - POST /api/appointments (respuesta 201 Created)
 * 
 * Beneficios:
 * - Respuesta más ligera que AppointmentResponseDTO
 * - Mensaje personalizado para UX mejorada
 * - Enfoca al cliente en el código de confirmación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentConfirmationDTO {
    
    /**
     * ID de la cita creada.
     */
    private Long id;
    
    /**
     * Código único de confirmación.
     * El cliente debe guardarlo para gestionar su cita.
     */
    private String codigoConfirmacion;
    
    /**
     * Nombre del cliente (confirmación de que se guardó correctamente).
     */
    private String nombreCliente;
    
    /**
     * Fecha de la cita reservada.
     */
    private LocalDate fecha;
    
    /**
     * Hora de inicio.
     */
    private LocalTime horaInicio;
    
    /**
     * Hora de finalización.
     */
    private LocalTime horaFin;
    
    /**
     * Servicio reservado.
     */
    private String servicio;
    
    /**
     * Estado inicial de la cita (siempre será PENDIENTE tras crear).
     */
    private EstadoCita estado;
    
    /**
     * Mensaje descriptivo para el cliente.
     * 
     * Se genera en el Service con formato:
     * "Cita reservada con éxito. Código de confirmación: APT-XXXX. 
     *  Por favor, confirme su asistencia."
     * 
     * Este mensaje mejora la UX al guiar al cliente sobre los siguientes pasos.
     */
    private String mensaje;
}
