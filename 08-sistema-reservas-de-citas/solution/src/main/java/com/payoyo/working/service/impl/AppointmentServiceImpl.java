package com.payoyo.working.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payoyo.working.dtos.AppointmentConfirmationDTO;
import com.payoyo.working.dtos.AppointmentResponseDTO;
import com.payoyo.working.model.Appointment;
import com.payoyo.working.dtos.AppointmentRequestDTO;
import com.payoyo.working.dtos.AvailabilityDTO;
import com.payoyo.working.model.EstadoCita;
import com.payoyo.working.exceptions.AppointmentNotFoundException;
import com.payoyo.working.exceptions.InvalidStateTransitionException;
import com.payoyo.working.exceptions.InvalidTimeRangeException;
import com.payoyo.working.exceptions.TimeSlotNotAvailableException;
import com.payoyo.working.repository.AppointmentRepository;
import com.payoyo.working.service.AppointmentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de citas.
 * 
 * Contiene toda la lógica de negocio del sistema:
 * - Validaciones de horarios y reglas de negocio
 * - Generación de códigos únicos de confirmación
 * - Cálculo de disponibilidad de horarios
 * - Control de transiciones de estado
 * - Mapeo entre DTOs y entidades
 * 
 * @Service marca esta clase como un componente de servicio de Spring
 * @RequiredArgsConstructor (Lombok) genera constructor con dependencias final
 * @Transactional en métodos de escritura garantiza atomicidad y rollback
 */
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    
    /**
     * Repositorio para acceso a datos de citas.
     * Inyección por constructor (inmutable, testeable, sin @Autowired).
     */
    private final AppointmentRepository repository;
    
    // Constantes de configuración del negocio
    private static final LocalTime HORARIO_APERTURA = LocalTime.of(8, 0);   // 08:00
    private static final LocalTime HORARIO_CIERRE = LocalTime.of(20, 0);    // 20:00
    private static final int SLOT_DURACION_MINUTOS = 30;                     // Intervalos de 30 min
    private static final long ANTICIPACION_MINIMA_HORAS = 2;                 // 2 horas de antelación
    
    // ==================== OPERACIONES CRUD ====================
    
    /**
     * Crea una nueva cita con validaciones completas.
     * 
     * @Transactional garantiza que si algo falla (validación o guardado),
     * se hace rollback automático de cualquier cambio en BD.
     */
    @Override
    @Transactional
    public AppointmentConfirmationDTO createAppointment(AppointmentRequestDTO dto) {
        // 1. Validar horario laboral (08:00 - 20:00)
        validateBusinessHours(dto.getHoraInicio(), dto.getHoraFin());
        
        // 2. Validar que no haya solapamiento con otras citas
        validateNoOverlap(dto.getFecha(), dto.getHoraInicio(), dto.getHoraFin(), null);
        
        // 3. Validar anticipación mínima (2 horas)
        validateMinimumAdvance(dto.getFecha(), dto.getHoraInicio());
        
        // 4. Mapear DTO → Entity
        Appointment appointment = mapToEntity(dto);
        
        // 5. Generar código único de confirmación
        appointment.setCodigoConfirmacion(generateUniqueConfirmationCode());
        
        // 6. Establecer estado inicial
        appointment.setEstado(EstadoCita.PENDIENTE);
        
        // 7. Guardar en BD
        Appointment saved = repository.save(appointment);
        
        // 8. Mapear Entity → ConfirmationDTO y devolver
        return mapToConfirmationDTO(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long id) {
        Appointment appointment = findByIdOrThrow(id);
        return mapToResponseDTO(appointment);
    }
    
    /**
     * Actualiza una cita existente.
     * 
     * IMPORTANTE: Al validar solapamiento, excluye la propia cita
     * para permitir modificar horarios sin conflicto consigo misma.
     */
    @Override
    @Transactional
    public AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO dto) {
        // Verificar que la cita existe
        Appointment existing = findByIdOrThrow(id);
        
        // Validar nuevo horario
        validateBusinessHours(dto.getHoraInicio(), dto.getHoraFin());
        
        // Validar solapamiento (excluyendo la propia cita con ID)
        validateNoOverlap(dto.getFecha(), dto.getHoraInicio(), dto.getHoraFin(), id);
        
        // Validar anticipación mínima
        validateMinimumAdvance(dto.getFecha(), dto.getHoraInicio());
        
        // Actualizar campos (manteniendo ID, código, estado, timestamps)
        updateEntityFromDTO(existing, dto);
        
        // Guardar cambios
        Appointment updated = repository.save(existing);
        
        return mapToResponseDTO(updated);
    }
    
    @Override
    @Transactional
    public void deleteAppointment(Long id) {
        // Verificar que existe antes de eliminar
        Appointment appointment = findByIdOrThrow(id);
        repository.delete(appointment);
    }
    
    // ==================== BÚSQUEDAS Y FILTROS ====================
    
    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentByCodigo(String codigo) {
        Appointment appointment = repository.findByCodigoConfirmacion(codigo)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Cita con código " + codigo + " no encontrada"));
        
        return mapToResponseDTO(appointment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByEmail(String email) {
        return repository.findByEmailOrderByFechaDesc(email)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByEstado(EstadoCita estado) {
        return repository.findByEstadoOrderByFechaAsc(estado)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByFecha(LocalDate fecha) {
        return repository.findByFechaOrderByHoraInicioAsc(fecha)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    // ==================== DISPONIBILIDAD ====================
    
    /**
     * Calcula la disponibilidad de horarios para una fecha.
     * 
     * Algoritmo:
     * 1. Genera todos los slots de 30 min entre 08:00 y 20:00
     * 2. Obtiene citas activas del día
     * 3. Por cada cita, marca los slots que ocupa como ocupados
     * 4. Calcula disponibles = todos - ocupados
     */
    @Override
    @Transactional(readOnly = true)
    public AvailabilityDTO getAvailability(LocalDate fecha) {
        // 1. Generar todos los slots posibles (08:00 - 20:00 en intervalos de 30 min)
        List<String> allSlots = generateAllTimeSlots();
        
        // 2. Obtener citas activas (PENDIENTE o CONFIRMADA) de la fecha
        List<Appointment> activeAppointments = repository.findActiveAppointmentsByDate(fecha);
        
        // 3. Determinar slots ocupados por las citas
        List<String> occupiedSlots = new ArrayList<>();
        for (Appointment apt : activeAppointments) {
            occupiedSlots.add(formatTimeRange(apt.getHoraInicio(), apt.getHoraFin()));
        }
        
        // 4. Calcular slots disponibles = todos - ocupados
        // Para simplificación, consideramos que un slot ocupado bloquea ese rango
        // En un sistema real, habría que hacer cálculo más sofisticado de solapamiento
        List<String> availableSlots = allSlots.stream()
                .filter(slot -> !isSlotOccupied(slot, activeAppointments))
                .collect(Collectors.toList());
        
        // 5. Construir y devolver DTO
        return AvailabilityDTO.builder()
                .fecha(fecha)
                .horariosDisponibles(availableSlots)
                .horariosOcupados(occupiedSlots)
                .totalDisponibles(availableSlots.size())
                .build();
    }
    
    // ==================== TRANSICIONES DE ESTADO ====================
    
    @Override
    @Transactional
    public AppointmentResponseDTO confirmarCita(Long id) {
        Appointment appointment = findByIdOrThrow(id);
        
        // Validar transición: solo PENDIENTE → CONFIRMADA
        if (appointment.getEstado() != EstadoCita.PENDIENTE) {
            throw new InvalidStateTransitionException(
                    "Solo se pueden confirmar citas en estado PENDIENTE. Estado actual: " 
                    + appointment.getEstado());
        }
        
        // Cambiar estado
        appointment.setEstado(EstadoCita.CONFIRMADA);
        
        // Guardar y devolver
        Appointment updated = repository.save(appointment);
        return mapToResponseDTO(updated);
    }
    
    @Override
    @Transactional
    public AppointmentResponseDTO cancelarCita(Long id) {
        Appointment appointment = findByIdOrThrow(id);
        
        // Validar transiciones: PENDIENTE → CANCELADA o CONFIRMADA → CANCELADA
        if (appointment.getEstado() == EstadoCita.COMPLETADA) {
            throw new InvalidStateTransitionException(
                    "No se puede cancelar una cita completada");
        }
        
        if (appointment.getEstado() == EstadoCita.CANCELADA) {
            throw new InvalidStateTransitionException(
                    "La cita ya está cancelada");
        }
        
        // Cambiar estado
        appointment.setEstado(EstadoCita.CANCELADA);
        
        // Guardar y devolver
        Appointment updated = repository.save(appointment);
        return mapToResponseDTO(updated);
    }
    
    @Override
    @Transactional
    public AppointmentResponseDTO completarCita(Long id) {
        Appointment appointment = findByIdOrThrow(id);
        
        // Validar transición: solo CONFIRMADA → COMPLETADA
        if (appointment.getEstado() != EstadoCita.CONFIRMADA) {
            throw new InvalidStateTransitionException(
                    "Solo se pueden completar citas confirmadas. Estado actual: " 
                    + appointment.getEstado());
        }
        
        // Cambiar estado
        appointment.setEstado(EstadoCita.COMPLETADA);
        
        // Guardar y devolver
        Appointment updated = repository.save(appointment);
        return mapToResponseDTO(updated);
    }
    
    // ==================== VALIDACIONES DE NEGOCIO ====================
    
    /**
     * Valida que el horario esté dentro del rango laboral (08:00 - 20:00).
     * 
     * Regla: Tanto hora de inicio como hora de fin deben estar en el rango.
     * 
     * @throws InvalidTimeRangeException si está fuera del horario laboral
     */
    private void validateBusinessHours(LocalTime horaInicio, LocalTime horaFin) {
        if (horaInicio.isBefore(HORARIO_APERTURA) || horaFin.isAfter(HORARIO_CIERRE)) {
            throw new InvalidTimeRangeException(
                    String.format("Las citas deben estar entre %s y %s. Horario solicitado: %s - %s",
                            HORARIO_APERTURA, HORARIO_CIERRE, horaInicio, horaFin));
        }
    }
    
    /**
     * Valida que no haya solapamiento con otras citas activas.
     * 
     * Usa el query findOverlappingAppointments del repository.
     * Si excludeId no es null, excluye esa cita de la búsqueda (útil para updates).
     * 
     * @param excludeId ID de cita a excluir (null para crear, ID para update)
     * @throws TimeSlotNotAvailableException si hay solapamiento
     */
    private void validateNoOverlap(LocalDate fecha, LocalTime horaInicio, 
                                   LocalTime horaFin, Long excludeId) {
        List<Appointment> overlapping = repository.findOverlappingAppointments(
                fecha, horaInicio, horaFin);
        
        // Si estamos actualizando, excluir la propia cita
        if (excludeId != null) {
            overlapping = overlapping.stream()
                    .filter(apt -> !apt.getId().equals(excludeId))
                    .collect(Collectors.toList());
        }
        
        if (!overlapping.isEmpty()) {
            throw new TimeSlotNotAvailableException(
                    String.format("El horario solicitado (%s - %s) ya está ocupado",
                            horaInicio, horaFin));
        }
    }
    
    /**
     * Valida que la cita se cree con anticipación mínima.
     * 
     * Regla: Las citas deben crearse al menos 2 horas antes de su inicio.
     * 
     * Ejemplo:
     * - Ahora: 2024-12-15 10:00
     * - Cita válida: 2024-12-15 12:01 o posterior
     * - Cita inválida: 2024-12-15 11:59 o anterior
     * 
     * @throws InvalidTimeRangeException si no cumple anticipación mínima
     */
    private void validateMinimumAdvance(LocalDate fecha, LocalTime horaInicio) {
        LocalDateTime citaDateTime = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime minimoPermitido = ahora.plusHours(ANTICIPACION_MINIMA_HORAS);
        
        if (citaDateTime.isBefore(minimoPermitido)) {
            throw new InvalidTimeRangeException(
                    String.format("Las citas deben crearse con al menos %d horas de anticipación",
                            ANTICIPACION_MINIMA_HORAS));
        }
    }
    
    // ==================== GENERACIÓN DE CÓDIGOS ====================
    
    /**
     * Genera un código único de confirmación.
     * 
     * Formato: APT-XXXX (APT + 4 caracteres alfanuméricos en mayúsculas)
     * Ejemplo: APT-A3F9, APT-K7M2
     * 
     * Algoritmo:
     * 1. Generar UUID aleatorio
     * 2. Tomar primeros 4 caracteres (sin guiones)
     * 3. Convertir a mayúsculas
     * 4. Agregar prefijo "APT-"
     * 5. Verificar que no exista en BD (do-while)
     * 
     * @return Código único de confirmación
     */
    private String generateUniqueConfirmationCode() {
        String code;
        do {
            // UUID.randomUUID() genera algo como: "a8f3e1c0-9b2d-4e5f-8c3a-1b4d9e7f2c8a"
            String uuid = UUID.randomUUID().toString().replace("-", "");
            // Tomar 4 caracteres: "a8f3"
            String suffix = uuid.substring(0, 4).toUpperCase(); // "A8F3"
            code = "APT-" + suffix; // "APT-A8F3"
        } while (repository.existsByCodigoConfirmacion(code)); // Repetir si ya existe
        
        return code;
    }
    
    // ==================== HELPERS DE DISPONIBILIDAD ====================
    
    /**
     * Genera todos los slots de tiempo posibles.
     * 
     * De 08:00 a 20:00 en intervalos de 30 minutos = 24 slots.
     * Formato: "HH:mm - HH:mm"
     * 
     * @return Lista con todos los slots ["08:00 - 08:30", "08:30 - 09:00", ...]
     */
    private List<String> generateAllTimeSlots() {
        List<String> slots = new ArrayList<>();
        LocalTime current = HORARIO_APERTURA;
        
        while (current.isBefore(HORARIO_CIERRE)) {
            LocalTime next = current.plusMinutes(SLOT_DURACION_MINUTOS);
            slots.add(formatTimeRange(current, next));
            current = next;
        }
        
        return slots;
    }
    
    /**
     * Verifica si un slot está ocupado por alguna cita.
     * 
     * Un slot "08:00 - 08:30" está ocupado si alguna cita solapa con él.
     * 
     * Simplificación: Este método compara strings. En sistema real,
     * habría que parsear y calcular solapamiento matemático.
     */
    private boolean isSlotOccupied(String slot, List<Appointment> appointments) {
        // Extraer inicio y fin del slot
        String[] parts = slot.split(" - ");
        LocalTime slotStart = LocalTime.parse(parts[0]);
        LocalTime slotEnd = LocalTime.parse(parts[1]);
        
        // Verificar si alguna cita solapa con este slot
        for (Appointment apt : appointments) {
            // Algoritmo de solapamiento: (A < D) AND (B > C)
            if (apt.getHoraInicio().isBefore(slotEnd) && 
                apt.getHoraFin().isAfter(slotStart)) {
                return true; // Hay solapamiento
            }
        }
        
        return false; // No hay solapamiento
    }
    
    /**
     * Formatea un rango de tiempo como string.
     * 
     * @return String en formato "HH:mm - HH:mm"
     */
    private String formatTimeRange(LocalTime inicio, LocalTime fin) {
        return String.format("%s - %s", inicio, fin);
    }
    
    // ==================== MAPEO DTO ↔ ENTITY ====================
    
    /**
     * Mapea RequestDTO → Entity (para crear).
     * 
     * NO incluye: id, estado, código, timestamps (generados automáticamente).
     */
    private Appointment mapToEntity(AppointmentRequestDTO dto) {
        Appointment appointment = new Appointment();
        appointment.setNombreCliente(dto.getNombreCliente());
        appointment.setEmail(dto.getEmail());
        appointment.setTelefono(dto.getTelefono());
        appointment.setFecha(dto.getFecha());
        appointment.setHoraInicio(dto.getHoraInicio());
        appointment.setHoraFin(dto.getHoraFin());
        appointment.setServicio(dto.getServicio());
        appointment.setPrecio(dto.getPrecio());
        appointment.setNotas(dto.getNotas());
        return appointment;
    }
    
    /**
     * Actualiza entity existente con datos del DTO.
     * 
     * Mantiene: id, estado, código, timestamps (manejados por JPA).
     */
    private void updateEntityFromDTO(Appointment entity, AppointmentRequestDTO dto) {
        entity.setNombreCliente(dto.getNombreCliente());
        entity.setEmail(dto.getEmail());
        entity.setTelefono(dto.getTelefono());
        entity.setFecha(dto.getFecha());
        entity.setHoraInicio(dto.getHoraInicio());
        entity.setHoraFin(dto.getHoraFin());
        entity.setServicio(dto.getServicio());
        entity.setPrecio(dto.getPrecio());
        entity.setNotas(dto.getNotas());
        // NO actualizamos: estado, codigoConfirmacion (se manejan por endpoints específicos)
    }
    
    /**
     * Mapea Entity → ResponseDTO (completo).
     * 
     * Incluye cálculo de duracionMinutos.
     */
    private AppointmentResponseDTO mapToResponseDTO(Appointment entity) {
        // Calcular duración en minutos
        long duracion = ChronoUnit.MINUTES.between(
                entity.getHoraInicio(), 
                entity.getHoraFin()
        );
        
        return AppointmentResponseDTO.builder()
                .id(entity.getId())
                .nombreCliente(entity.getNombreCliente())
                .email(entity.getEmail())
                .telefono(entity.getTelefono())
                .fecha(entity.getFecha())
                .horaInicio(entity.getHoraInicio())
                .horaFin(entity.getHoraFin())
                .servicio(entity.getServicio())
                .estado(entity.getEstado())
                .precio(entity.getPrecio())
                .notas(entity.getNotas())
                .codigoConfirmacion(entity.getCodigoConfirmacion())
                .duracionMinutos(duracion)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * Mapea Entity → ConfirmationDTO (simplificado con mensaje).
     */
    private AppointmentConfirmationDTO mapToConfirmationDTO(Appointment entity) {
        // Generar mensaje personalizado
        String mensaje = String.format(
                "Cita reservada con éxito. Código de confirmación: %s. " +
                "Por favor, confirme su asistencia.",
                entity.getCodigoConfirmacion()
        );
        
        return AppointmentConfirmationDTO.builder()
                .id(entity.getId())
                .codigoConfirmacion(entity.getCodigoConfirmacion())
                .nombreCliente(entity.getNombreCliente())
                .fecha(entity.getFecha())
                .horaInicio(entity.getHoraInicio())
                .horaFin(entity.getHoraFin())
                .servicio(entity.getServicio())
                .estado(entity.getEstado())
                .mensaje(mensaje)
                .build();
    }
    
    // ==================== HELPERS GENÉRICOS ====================
    
    /**
     * Busca una cita por ID o lanza excepción si no existe.
     * 
     * Método helper para evitar repetir el patrón findById + orElseThrow.
     * 
     * @param id ID de la cita
     * @return Appointment encontrada
     * @throws AppointmentNotFoundException si no existe
     */
    private Appointment findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Cita con ID " + id + " no encontrada"));
    }
}
