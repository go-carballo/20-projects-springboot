package com.payoyo.working.controller;

import com.payoyo.working.dtos.AppointmentConfirmationDTO;
import com.payoyo.working.dtos.AppointmentRequestDTO;
import com.payoyo.working.dtos.AppointmentResponseDTO;
import com.payoyo.working.dtos.AvailabilityDTO;
import com.payoyo.working.model.EstadoCita;
import com.payoyo.working.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para la gestión de citas.
 * 
 * Expone endpoints HTTP para operaciones CRUD, búsquedas, cambios de estado
 * y consulta de disponibilidad del sistema de reservas.
 * 
 * Base URL: /api/appointments
 * 
 * Responsabilidades:
 * - Recibir requests HTTP y validar formato
 * - Delegar lógica de negocio al Service
 * - Devolver responses HTTP con status codes apropiados
 * - NO contiene lógica de negocio (capa thin)
 * 
 * @RestController combina @Controller + @ResponseBody
 * @RequestMapping define la ruta base para todos los endpoints
 * @RequiredArgsConstructor (Lombok) genera constructor con dependencias final
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    
    /**
     * Servicio de citas inyectado por constructor.
     * final garantiza inmutabilidad y @RequiredArgsConstructor genera el constructor.
     */
    private final AppointmentService service;
    
    // ==================== OPERACIONES CRUD ====================
    
    /**
     * Crea una nueva cita.
     * 
     * POST /api/appointments
     * 
     * @Valid activa las validaciones Bean Validation del DTO
     * (@NotBlank, @Email, @AssertTrue, etc.)
     * 
     * Si las validaciones fallan, Spring lanza MethodArgumentNotValidException
     * que es capturada por GlobalExceptionHandler → 400 Bad Request
     * 
     * @param dto Datos de la cita a crear
     * @return 201 Created con DTO de confirmación (incluye código único)
     */
    @PostMapping
    public ResponseEntity<AppointmentConfirmationDTO> createAppointment(
            @Valid @RequestBody AppointmentRequestDTO dto) {
        
        AppointmentConfirmationDTO created = service.createAppointment(dto);
        
        // 201 Created: Recurso creado exitosamente
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }
    
    /**
     * Obtiene todas las citas del sistema.
     * 
     * GET /api/appointments
     * 
     * @return 200 OK con lista de citas (vacía si no hay ninguna)
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments() {
        List<AppointmentResponseDTO> appointments = service.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }
    
    /**
     * Obtiene una cita por su ID.
     * 
     * GET /api/appointments/{id}
     * 
     * @PathVariable extrae {id} de la URL
     * Ejemplo: GET /api/appointments/5 → id=5
     * 
     * @param id Identificador de la cita
     * @return 200 OK con datos de la cita
     * @throws AppointmentNotFoundException si no existe (manejada por GlobalExceptionHandler → 404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(
            @PathVariable Long id) {
        
        AppointmentResponseDTO appointment = service.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }
    
    /**
     * Actualiza una cita existente.
     * 
     * PUT /api/appointments/{id}
     * 
     * PUT se usa para actualización completa (todos los campos).
     * El código de confirmación y estado NO se modifican aquí
     * (tienen endpoints específicos).
     * 
     * @param id ID de la cita a actualizar
     * @param dto Nuevos datos de la cita
     * @return 200 OK con datos actualizados
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequestDTO dto) {
        
        AppointmentResponseDTO updated = service.updateAppointment(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Elimina una cita.
     * 
     * DELETE /api/appointments/{id}
     * 
     * @param id ID de la cita a eliminar
     * @return 204 No Content (eliminación exitosa sin cuerpo de respuesta)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        service.deleteAppointment(id);
        
        // 204 No Content: Operación exitosa, sin contenido que devolver
        return ResponseEntity.noContent().build();
    }
    
    // ==================== BÚSQUEDAS Y FILTROS ====================
    
    /**
     * Busca una cita por su código de confirmación.
     * 
     * GET /api/appointments/codigo/{codigo}
     * Ejemplo: GET /api/appointments/codigo/APT-A3F9
     * 
     * Útil para que clientes consulten su cita sin necesidad de ID.
     * 
     * @param codigo Código de confirmación único
     * @return 200 OK con datos de la cita
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentByCodigo(
            @PathVariable String codigo) {
        
        AppointmentResponseDTO appointment = service.getAppointmentByCodigo(codigo);
        return ResponseEntity.ok(appointment);
    }
    
    /**
     * Obtiene todas las citas de un cliente por su email.
     * 
     * GET /api/appointments/cliente/email/{email}
     * Ejemplo: GET /api/appointments/cliente/email/juan@email.com
     * 
     * Nota: En URLs, @ se codifica como %40 automáticamente por el navegador.
     * Spring lo decodifica automáticamente.
     * 
     * @param email Email del cliente
     * @return 200 OK con lista de citas del cliente
     */
    @GetMapping("/cliente/email/{email}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByEmail(
            @PathVariable String email) {
        
        List<AppointmentResponseDTO> appointments = service.getAppointmentsByEmail(email);
        return ResponseEntity.ok(appointments);
    }
    
    /**
     * Filtra citas por estado.
     * 
     * GET /api/appointments/estado/{estado}
     * Ejemplos:
     * - GET /api/appointments/estado/PENDIENTE
     * - GET /api/appointments/estado/CONFIRMADA
     * - GET /api/appointments/estado/CANCELADA
     * - GET /api/appointments/estado/COMPLETADA
     * 
     * Spring convierte automáticamente el String a enum EstadoCita.
     * Si el valor no es válido, lanza 400 Bad Request.
     * 
     * @param estado Estado a filtrar
     * @return 200 OK con lista de citas con ese estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByEstado(
            @PathVariable EstadoCita estado) {
        
        List<AppointmentResponseDTO> appointments = service.getAppointmentsByEstado(estado);
        return ResponseEntity.ok(appointments);
    }
    
    /**
     * Obtiene todas las citas de una fecha específica.
     * 
     * GET /api/appointments/fecha/{fecha}
     * Ejemplo: GET /api/appointments/fecha/2024-12-20
     * 
     * @DateTimeFormat indica el formato ISO de fecha (yyyy-MM-dd)
     * Spring parsea automáticamente el String a LocalDate.
     * 
     * @param fecha Fecha a consultar
     * @return 200 OK con lista de citas de ese día
     */
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByFecha(
            @PathVariable 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        List<AppointmentResponseDTO> appointments = service.getAppointmentsByFecha(fecha);
        return ResponseEntity.ok(appointments);
    }
    
    /**
     * Obtiene la disponibilidad de horarios para una fecha.
     * 
     * GET /api/appointments/availability/{fecha}
     * Ejemplo: GET /api/appointments/availability/2024-12-20
     * 
     * Devuelve slots de 30 minutos entre 08:00 y 20:00,
     * marcando cuáles están disponibles y cuáles ocupados.
     * 
     * @param fecha Fecha a consultar
     * @return 200 OK con DTO de disponibilidad
     */
    @GetMapping("/availability/{fecha}")
    public ResponseEntity<AvailabilityDTO> getAvailability(
            @PathVariable 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        AvailabilityDTO availability = service.getAvailability(fecha);
        return ResponseEntity.ok(availability);
    }
    
    // ==================== TRANSICIONES DE ESTADO ====================
    
    /**
     * Confirma una cita (PENDIENTE → CONFIRMADA).
     * 
     * PATCH /api/appointments/{id}/confirmar
     * 
     * PATCH se usa para actualizaciones parciales (solo cambia el estado).
     * Diferencia con PUT: PUT reemplaza el recurso completo.
     * 
     * Solo funciona si la cita está en estado PENDIENTE.
     * Si no, el Service lanza InvalidStateTransitionException → 400 Bad Request.
     * 
     * @param id ID de la cita a confirmar
     * @return 200 OK con datos actualizados (estado CONFIRMADA)
     */
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<AppointmentResponseDTO> confirmarCita(
            @PathVariable Long id) {
        
        AppointmentResponseDTO confirmed = service.confirmarCita(id);
        return ResponseEntity.ok(confirmed);
    }
    
    /**
     * Cancela una cita.
     * 
     * PATCH /api/appointments/{id}/cancelar
     * 
     * Transiciones válidas:
     * - PENDIENTE → CANCELADA
     * - CONFIRMADA → CANCELADA
     * 
     * NO permite cancelar citas COMPLETADAS.
     * 
     * @param id ID de la cita a cancelar
     * @return 200 OK con datos actualizados (estado CANCELADA)
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AppointmentResponseDTO> cancelarCita(
            @PathVariable Long id) {
        
        AppointmentResponseDTO cancelled = service.cancelarCita(id);
        return ResponseEntity.ok(cancelled);
    }
    
    /**
     * Marca una cita como completada (CONFIRMADA → COMPLETADA).
     * 
     * PATCH /api/appointments/{id}/completar
     * 
     * Solo funciona si la cita está CONFIRMADA.
     * Este es un estado final, no permite más cambios.
     * 
     * @param id ID de la cita a completar
     * @return 200 OK con datos actualizados (estado COMPLETADA)
     */
    @PatchMapping("/{id}/completar")
    public ResponseEntity<AppointmentResponseDTO> completarCita(
            @PathVariable Long id) {
        
        AppointmentResponseDTO completed = service.completarCita(id);
        return ResponseEntity.ok(completed);
    }
}
