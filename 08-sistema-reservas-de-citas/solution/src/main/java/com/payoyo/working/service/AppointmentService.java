package com.payoyo.working.service;

import com.payoyo.working.dtos.AppointmentConfirmationDTO;
import com.payoyo.working.dtos.AppointmentRequestDTO;
import com.payoyo.working.dtos.AppointmentResponseDTO;
import com.payoyo.working.dtos.AvailabilityDTO;
import com.payoyo.working.model.EstadoCita;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz de servicio para la gestión de citas.
 * 
 * Define el contrato de operaciones de negocio para el sistema de reservas.
 * La implementación contiene toda la lógica de negocio, validaciones,
 * y transformaciones entre DTOs y entidades.
 * 
 * Beneficios de usar interfaz:
 * - Desacoplamiento: El controller depende de la abstracción, no de la implementación
 * - Testabilidad: Facilita crear mocks para testing
 * - Flexibilidad: Permite múltiples implementaciones si es necesario
 * - Documentación: La interfaz actúa como contrato claro del servicio
 */
public interface AppointmentService {
    
    // ==================== OPERACIONES CRUD ====================
    
    /**
     * Crea una nueva cita en el sistema.
     * 
     * Proceso:
     * 1. Valida horario laboral (08:00 - 20:00)
     * 2. Valida que no haya solapamiento con otras citas
     * 3. Valida anticipación mínima (2 horas)
     * 4. Genera código de confirmación único
     * 5. Establece estado inicial como PENDIENTE
     * 6. Guarda en base de datos
     * 
     * @param dto Datos de la cita a crear
     * @return DTO de confirmación con código único y mensaje
     * @throws InvalidTimeRangeException si el horario es inválido
     * @throws TimeSlotNotAvailableException si el horario está ocupado
     */
    AppointmentConfirmationDTO createAppointment(AppointmentRequestDTO dto);
    
    /**
     * Obtiene todas las citas del sistema.
     * 
     * @return Lista de todas las citas (vacía si no hay ninguna)
     */
    List<AppointmentResponseDTO> getAllAppointments();
    
    /**
     * Obtiene una cita por su ID.
     * 
     * @param id Identificador de la cita
     * @return DTO con los datos completos de la cita
     * @throws AppointmentNotFoundException si no existe la cita
     */
    AppointmentResponseDTO getAppointmentById(Long id);
    
    /**
     * Actualiza los datos de una cita existente.
     * 
     * Validaciones aplicadas:
     * - Horario laboral
     * - No solapamiento (excluyendo la propia cita)
     * - Anticipación mínima
     * 
     * NOTA: El código de confirmación y estado NO se modifican en actualización.
     * 
     * @param id ID de la cita a actualizar
     * @param dto Nuevos datos de la cita
     * @return DTO con los datos actualizados
     * @throws AppointmentNotFoundException si no existe la cita
     * @throws InvalidTimeRangeException si el nuevo horario es inválido
     * @throws TimeSlotNotAvailableException si el nuevo horario está ocupado
     */
    AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO dto);
    
    /**
     * Elimina una cita del sistema.
     * 
     * @param id ID de la cita a eliminar
     * @throws AppointmentNotFoundException si no existe la cita
     */
    void deleteAppointment(Long id);
    
    // ==================== BÚSQUEDAS Y FILTROS ====================
    
    /**
     * Busca una cita por su código de confirmación único.
     * 
     * Útil para que los clientes consulten su cita sin necesidad de ID.
     * 
     * @param codigo Código de confirmación (formato: APT-XXXX)
     * @return DTO con los datos de la cita
     * @throws AppointmentNotFoundException si no existe cita con ese código
     */
    AppointmentResponseDTO getAppointmentByCodigo(String codigo);
    
    /**
     * Obtiene todas las citas de un cliente por su email.
     * 
     * Ordenadas por fecha descendente (más recientes primero).
     * 
     * @param email Email del cliente
     * @return Lista de citas del cliente (vacía si no tiene ninguna)
     */
    List<AppointmentResponseDTO> getAppointmentsByEmail(String email);
    
    /**
     * Filtra citas por estado.
     * 
     * Casos de uso:
     * - PENDIENTE: Citas que requieren confirmación
     * - CONFIRMADA: Agenda de citas confirmadas
     * - CANCELADA: Historial de cancelaciones
     * - COMPLETADA: Historial de servicios prestados
     * 
     * @param estado Estado a filtrar
     * @return Lista de citas con ese estado
     */
    List<AppointmentResponseDTO> getAppointmentsByEstado(EstadoCita estado);
    
    /**
     * Obtiene todas las citas de una fecha específica.
     * 
     * Ordenadas por hora de inicio (cronológico).
     * Útil para ver la agenda del día.
     * 
     * @param fecha Fecha a consultar
     * @return Lista de citas de ese día
     */
    List<AppointmentResponseDTO> getAppointmentsByFecha(LocalDate fecha);
    
    // ==================== DISPONIBILIDAD ====================
    
    /**
     * Calcula y devuelve la disponibilidad de horarios para una fecha.
     * 
     * Algoritmo:
     * 1. Genera slots de 30 minutos entre 08:00 y 20:00 (24 slots)
     * 2. Obtiene citas activas (PENDIENTE o CONFIRMADA) de la fecha
     * 3. Marca slots ocupados por cada cita
     * 4. Calcula disponibles = todos - ocupados
     * 
     * @param fecha Fecha a consultar
     * @return DTO con horarios disponibles, ocupados y total
     */
    AvailabilityDTO getAvailability(LocalDate fecha);
    
    // ==================== TRANSICIONES DE ESTADO ====================
    
    /**
     * Confirma una cita (PENDIENTE → CONFIRMADA).
     * 
     * Solo se pueden confirmar citas en estado PENDIENTE.
     * 
     * @param id ID de la cita a confirmar
     * @return DTO con los datos actualizados (estado CONFIRMADA)
     * @throws AppointmentNotFoundException si no existe la cita
     * @throws InvalidStateTransitionException si no está en estado PENDIENTE
     */
    AppointmentResponseDTO confirmarCita(Long id);
    
    /**
     * Cancela una cita.
     * 
     * Transiciones válidas:
     * - PENDIENTE → CANCELADA
     * - CONFIRMADA → CANCELADA
     * 
     * NO se pueden cancelar citas COMPLETADAS.
     * 
     * @param id ID de la cita a cancelar
     * @return DTO con los datos actualizados (estado CANCELADA)
     * @throws AppointmentNotFoundException si no existe la cita
     * @throws InvalidStateTransitionException si está COMPLETADA o ya CANCELADA
     */
    AppointmentResponseDTO cancelarCita(Long id);
    
    /**
     * Marca una cita como completada (CONFIRMADA → COMPLETADA).
     * 
     * Solo se pueden completar citas en estado CONFIRMADA.
     * Este es un estado final, no permite más cambios.
     * 
     * @param id ID de la cita a completar
     * @return DTO con los datos actualizados (estado COMPLETADA)
     * @throws AppointmentNotFoundException si no existe la cita
     * @throws InvalidStateTransitionException si no está CONFIRMADA
     */
    AppointmentResponseDTO completarCita(Long id);
}
