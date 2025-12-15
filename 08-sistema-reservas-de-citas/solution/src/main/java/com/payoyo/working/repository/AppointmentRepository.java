package com.payoyo.working.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.payoyo.working.model.Appointment;
import com.payoyo.working.model.EstadoCita;

/**
 * Repositorio para gestión de citas.
 * 
 * Proporciona métodos de acceso a datos tanto mediante query methods
 * (convención de nombres de Spring Data JPA) como consultas personalizadas
 * con @Query para lógica más compleja.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>{

    // ==================== BÚSQUEDAS POR CAMPO ÚNICO ====================
    /**
     * Busca una cita por su código de confirmación único.
     * 
     * Query method: Spring Data JPA genera automáticamente:
     * SELECT * FROM appointments WHERE codigo_confirmacion = ?
     * 
     * @param codigo Código de confirmación (formato: APT-XXXX)
     * @return Optional con la cita si existe, vacío si no
     */
    Optional<Appointment> findByCodigoConfirmacion(String codigo);

    /**
     * Verifica si existe una cita con el código de confirmación dado.
     * 
     * Usado durante la generación de códigos únicos para evitar duplicados.
     * Es más eficiente que findByCodigoConfirmacion() porque solo hace COUNT,
     * no recupera toda la entidad.
     * 
     * Query generada: SELECT COUNT(*) > 0 FROM appointments WHERE codigo_confirmacion = ?
     * 
     * @param codigo Código a verificar
     * @return true si existe, false si no
     */
    boolean existsByCodigoConfirmacion(String codigo);

    // ==================== BÚSQUEDAS POR EMAIL ====================
    
    /**
     * Obtiene todas las citas de un cliente por su email.
     * 
     * Ordenadas por fecha descendente (más recientes primero).
     * Útil para que el cliente vea su historial de citas.
     * 
     * Query generada: 
     * SELECT * FROM appointments WHERE email = ? ORDER BY fecha DESC
     * 
     * @param email Email del cliente
     * @return Lista de citas (vacía si no tiene ninguna)
     */
    List<Appointment> findByEmailOrderByFechaDesc(String email);

    // ==================== BÚSQUEDAS POR ESTADO ====================
    
    /**
     * Filtra citas por estado.
     * 
     * Ordenadas por fecha ascendente (próximas primero).
     * Casos de uso:
     * - Ver citas PENDIENTES que necesitan confirmación
     * - Listar citas CONFIRMADAS del día
     * - Revisar citas CANCELADAS
     * - Historial de citas COMPLETADAS
     * 
     * @param estado Estado de la cita (PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA)
     * @return Lista de citas con ese estado
     */
    List<Appointment> findByEstadoOrderByFechaAsc(EstadoCita estado);

    // ==================== BÚSQUEDAS POR FECHA ====================
    
    /**
     * Obtiene todas las citas de una fecha específica.
     * 
     * Ordenadas por hora de inicio ascendente (cronológico).
     * Útil para ver la agenda del día.
     * 
     * @param fecha Fecha a consultar
     * @return Lista de citas de ese día
     */
    List<Appointment> findByFechaOrderByHoraInicioAsc(LocalDate fecha);

    // ==================== CONSULTAS PERSONALIZADAS ====================
    
    /**
     * Detecta si existe solapamiento de horarios para una nueva cita.
     * 
     * Algoritmo de detección de solapamiento:
     * Dos intervalos [A, B] y [C, D] se solapan si y solo si:
     *   A < D  AND  B > C
     * 
     * Aplicado a citas:
     *   horaInicio_nueva < horaFin_existente  AND  horaFin_nueva > horaInicio_existente
     * 
     * Ejemplo visual:
     * Cita existente:    |----10:00----11:00----|
     * Nueva cita:           |--10:30----11:30--|   ← SOLAPA ✗
     * Nueva cita:  |--09:00----10:00--|            ← NO SOLAPA ✓ (termina cuando empieza la otra)
     * Nueva cita:                        |--11:00----12:00--| ← NO SOLAPA ✓ (empieza cuando termina la otra)
     * 
     * IMPORTANTE: Solo considera citas PENDIENTES o CONFIRMADAS.
     * Las citas CANCELADAS o COMPLETADAS no bloquean horarios.
     * 
     * @param fecha Fecha de la nueva cita
     * @param horaInicio Hora de inicio de la nueva cita
     * @param horaFin Hora de fin de la nueva cita
     * @return Lista de citas que solapan (vacía si no hay conflictos)
     */
    @Query("SELECT a FROM Appointment a WHERE a.fecha = :fecha " +
           "AND a.estado IN ('PENDIENTE', 'CONFIRMADA') " +
           "AND ((a.horaInicio < :horaFin AND a.horaFin > :horaInicio))")
    List<Appointment> findOverlappingAppointments(
        @Param("fecha") LocalDate fecha,
        @Param("horaInicio") LocalTime horaInicio,
        @Param("horaFin") LocalTime horaFin
    );

    /**
     * Obtiene todas las citas activas (PENDIENTE o CONFIRMADA) de una fecha.
     * 
     * Usado para calcular la disponibilidad de horarios.
     * Solo las citas activas bloquean slots de tiempo.
     * 
     * Ordenadas por hora de inicio para procesamiento secuencial.
     * 
     * Ejemplo de uso en Service:
     * 1. Generar todos los slots de 08:00 a 20:00
     * 2. Obtener citas activas con este método
     * 3. Marcar slots ocupados por cada cita
     * 4. Devolver slots disponibles = todos - ocupados
     * 
     * @param fecha Fecha a consultar
     * @return Lista de citas activas de ese día, ordenadas cronológicamente
     */
    @Query("SELECT a FROM Appointment a WHERE a.fecha = :fecha " +
           "AND a.estado IN ('PENDIENTE', 'CONFIRMADA') " +
           "ORDER BY a.horaInicio ASC")
    List<Appointment> findActiveAppointmentsByDate(@Param("fecha") LocalDate fecha);
} 
