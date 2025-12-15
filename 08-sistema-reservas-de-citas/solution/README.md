# Sistema de Reservas de Citas - Solución Completa 📘

## 📖 Documentación Técnica

Este documento detalla la implementación completa del sistema de reservas de citas con todas las decisiones técnicas, patrones aplicados y mejores prácticas utilizadas.

---

## 🏗️ Arquitectura General

### Patrón: Arquitectura en Capas (Layered Architecture)

```
┌─────────────────────────────────────┐
│   Controller (REST API)             │  ← Capa de Presentación
├─────────────────────────────────────┤
│   Service (Business Logic)          │  ← Capa de Negocio
├─────────────────────────────────────┤
│   Repository (Data Access)          │  ← Capa de Datos
├─────────────────────────────────────┤
│   Entity (Domain Model)             │  ← Capa de Dominio
└─────────────────────────────────────┘
         ↕
    ┌─────────┐
    │   DTOs  │  ← Objetos de Transferencia
    └─────────┘
```

### Flujo de Datos

1. **Request** → Controller recibe `AppointmentRequestDTO`
2. **Validación** → Bean Validation verifica constraints
3. **Transformación** → Service mapea DTO → Entity
4. **Procesamiento** → Service aplica lógica de negocio
5. **Persistencia** → Repository guarda en BD
6. **Respuesta** → Service mapea Entity → DTO Response
7. **Response** → Controller devuelve DTO al cliente

---

## 📦 Componentes Implementados

### 1️⃣ Capa de Dominio (Entity)

#### EstadoCita.java
```java
public enum EstadoCita {
    PENDIENTE,      // Estado inicial al crear cita
    CONFIRMADA,     // Cliente confirmó asistencia
    CANCELADA,      // Cita cancelada
    COMPLETADA      // Servicio prestado
}
```

**Decisiones de Diseño**:
- Enum para garantizar valores válidos en compile-time
- Estados claramente diferenciados para máquina de estados
- Sin lógica en el enum (separación de responsabilidades)

#### Appointment.java

**Campos Principales**:
| Campo | Tipo | Propósito | Estrategia |
|-------|------|-----------|------------|
| id | Long | PK | @GeneratedValue |
| nombreCliente | String | Identificación cliente | @Column(nullable=false) |
| email | String | Contacto | @Column(nullable=false) |
| telefono | String | Contacto alternativo | Formato validado |
| fecha | LocalDate | Día de la cita | @Column(nullable=false) |
| horaInicio | LocalTime | Inicio del servicio | @Column(nullable=false) |
| horaFin | LocalTime | Fin del servicio | @Column(nullable=false) |
| servicio | String | Tipo de servicio | @Column(nullable=false) |
| estado | EstadoCita | Estado actual | @Enumerated(STRING) |
| precio | BigDecimal | Coste del servicio | @Column(precision=10, scale=2) |
| notas | String | Información adicional | @Column(length=500, nullable=true) |
| codigoConfirmacion | String | Identificador único | @Column(unique=true) |
| createdAt | LocalDateTime | Auditoría creación | @CreationTimestamp |
| updatedAt | LocalDateTime | Auditoría modificación | @UpdateTimestamp |

**Anotaciones Clave**:
```java
@Entity
@Table(name = "appointments")
@Getter @Setter
@NoArgsConstructor
```

**Decisiones Técnicas**:
- **BigDecimal** para precio → precisión en cálculos monetarios
- **LocalDate/LocalTime** → API moderna Java 8+ para fechas
- **@Enumerated(STRING)** → legibilidad en BD vs ORDINAL
- **unique=true** en código → garantía de unicidad a nivel BD
- **Timestamps automáticos** → auditoría sin lógica manual

---

### 2️⃣ Capa de DTOs

#### AppointmentRequestDTO.java

**Propósito**: Recibir y validar datos de entrada del cliente

**Validaciones Implementadas**:
```java
@NotBlank(message = "El nombre del cliente es obligatorio")
@Size(min = 2, max = 100)
private String nombreCliente;

@NotBlank(message = "El email es obligatorio")
@Email(message = "Formato de email inválido")
private String email;

@NotBlank(message = "El teléfono es obligatorio")
@Pattern(regexp = "^\\+?[0-9\\s-]{9,15}$")
private String telefono;

@NotNull(message = "La fecha es obligatoria")
@FutureOrPresent(message = "La fecha no puede ser anterior a hoy")
private LocalDate fecha;

@NotNull(message = "La hora de inicio es obligatoria")
private LocalTime horaInicio;

@NotNull(message = "La hora de fin es obligatoria")
private LocalTime horaFin;

@NotNull(message = "El precio es obligatorio")
@DecimalMin(value = "0.0", message = "El precio debe ser mayor o igual a 0")
private BigDecimal precio;

@Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
private String notas;
```

**Validación Personalizada** (Cross-field):
```java
@AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
private boolean isHoraFinValida() {
    if (horaInicio == null || horaFin == null) {
        return true; // Dejar que @NotNull maneje esto
    }
    return horaFin.isAfter(horaInicio);
}

@AssertTrue(message = "La duración debe ser entre 15 minutos y 8 horas")
private boolean isDuracionValida() {
    if (horaInicio == null || horaFin == null) {
        return true;
    }
    long minutos = ChronoUnit.MINUTES.between(horaInicio, horaFin);
    return minutos >= 15 && minutos <= 480; // 8 horas
}
```

**Patrones Aplicados**:
- **Bean Validation** → validación declarativa
- **Fail Fast** → errores detectados antes de llegar a Service
- **Mensajes descriptivos** → UX mejorada

#### AppointmentResponseDTO.java

**Propósito**: Devolver información completa al cliente

**Campos Adicionales vs Request**:
```java
private Long id;
private EstadoCita estado;
private String codigoConfirmacion;
private Long duracionMinutos;  // Calculado
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
```

**Cálculo de Duración**:
```java
// Realizado en mapeo Entity → DTO
long duracionMinutos = ChronoUnit.MINUTES.between(
    appointment.getHoraInicio(),
    appointment.getHoraFin()
);
```

#### AppointmentConfirmationDTO.java

**Propósito**: Respuesta simplificada tras crear cita

**Campos Esenciales**:
```java
private Long id;
private String codigoConfirmacion;
private String nombreCliente;
private LocalDate fecha;
private LocalTime horaInicio;
private LocalTime horaFin;
private String servicio;
private EstadoCita estado;
private String mensaje;  // Mensaje descriptivo al cliente
```

**Generación del Mensaje**:
```java
// En Service al crear
String mensaje = String.format(
    "Cita reservada con éxito. Código de confirmación: %s. " +
    "Por favor, confirme su asistencia.",
    appointment.getCodigoConfirmacion()
);
```

#### AvailabilityDTO.java

**Propósito**: Mostrar disponibilidad de horarios

**Estructura**:
```java
private LocalDate fecha;
private List<String> horariosDisponibles;  // ["08:00 - 08:30", ...]
private List<String> horariosOcupados;     // ["10:00 - 11:00", ...]
private Integer totalDisponibles;
```

**Algoritmo de Cálculo** (ver Service):
1. Generar intervalos de 30 min de 08:00 a 20:00
2. Obtener citas CONFIRMADAS/PENDIENTES de la fecha
3. Marcar intervalos ocupados
4. Devolver disponibles y ocupados

---

### 3️⃣ Capa de Acceso a Datos (Repository)

#### AppointmentRepository.java

```java
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Búsqueda por código único
    Optional<Appointment> findByCodigoConfirmacion(String codigo);
    
    // Verificar existencia de código (para generación)
    boolean existsByCodigoConfirmacion(String codigo);
    
    // Filtrar por email del cliente
    List<Appointment> findByEmailOrderByFechaDesc(String email);
    
    // Filtrar por estado
    List<Appointment> findByEstadoOrderByFechaAsc(EstadoCita estado);
    
    // Filtrar por fecha
    List<Appointment> findByFechaOrderByHoraInicioAsc(LocalDate fecha);
    
    // Query personalizada: Detectar solapamiento de horarios
    @Query("SELECT a FROM Appointment a WHERE a.fecha = :fecha " +
           "AND a.estado IN ('PENDIENTE', 'CONFIRMADA') " +
           "AND ((a.horaInicio < :horaFin AND a.horaFin > :horaInicio))")
    List<Appointment> findOverlappingAppointments(
        @Param("fecha") LocalDate fecha,
        @Param("horaInicio") LocalTime horaInicio,
        @Param("horaFin") LocalTime horaFin
    );
    
    // Obtener citas activas para calcular disponibilidad
    @Query("SELECT a FROM Appointment a WHERE a.fecha = :fecha " +
           "AND a.estado IN ('PENDIENTE', 'CONFIRMADA') " +
           "ORDER BY a.horaInicio ASC")
    List<Appointment> findActiveAppointmentsByDate(
        @Param("fecha") LocalDate fecha
    );
}
```

**Decisiones Técnicas**:
- **Query Methods** para consultas simples → menos código
- **@Query JPQL** para lógica compleja → control total
- **Ordenamiento** en método → resultados predecibles
- **IN clause** para múltiples estados → query eficiente

---

### 4️⃣ Capa de Negocio (Service)

#### AppointmentService.java

**Responsabilidades Principales**:
1. ✅ Generación de códigos únicos
2. ✅ Validación de reglas de negocio
3. ✅ Detección de solapamiento
4. ✅ Cálculo de disponibilidad
5. ✅ Transiciones de estado
6. ✅ Mapeo DTO ↔ Entity

**Métodos Clave**:

##### 1. Crear Cita
```java
@Transactional
public AppointmentConfirmationDTO createAppointment(AppointmentRequestDTO dto) {
    // 1. Validar horario laboral (08:00 - 20:00)
    validateBusinessHours(dto.getHoraInicio(), dto.getHoraFin());
    
    // 2. Validar no solapamiento
    validateNoOverlap(dto.getFecha(), dto.getHoraInicio(), dto.getHoraFin());
    
    // 3. Validar anticipación mínima (2 horas)
    validateMinimumAdvance(dto.getFecha(), dto.getHoraInicio());
    
    // 4. Mapear DTO → Entity
    Appointment appointment = mapToEntity(dto);
    
    // 5. Generar código único
    appointment.setCodigoConfirmacion(generateUniqueCode());
    
    // 6. Establecer estado inicial
    appointment.setEstado(EstadoCita.PENDIENTE);
    
    // 7. Guardar
    Appointment saved = repository.save(appointment);
    
    // 8. Mapear Entity → ConfirmationDTO
    return mapToConfirmationDTO(saved);
}
```

##### 2. Generación de Código Único
```java
private String generateUniqueCode() {
    String code;
    do {
        // Generar código aleatorio: APT-XXXX
        code = "APT-" + UUID.randomUUID()
            .toString()
            .replace("-", "")  // Eliminar guiones
            .substring(0, 4)   // Tomar 4 caracteres
            .toUpperCase();    // Mayúsculas
    } while (repository.existsByCodigoConfirmacion(code));
    return code;
}
```

**Patrón aplicado**: Do-While Loop hasta encontrar código único

##### 3. Validación de Solapamiento
```java
private void validateNoOverlap(LocalDate fecha, LocalTime inicio, LocalTime fin) {
    List<Appointment> overlapping = repository.findOverlappingAppointments(
        fecha, inicio, fin
    );
    
    if (!overlapping.isEmpty()) {
        throw new TimeSlotNotAvailableException(
            "El horario solicitado ya está ocupado"
        );
    }
}
```

**Algoritmo de Solapamiento**:
```
Dos citas solapan si:
  (inicio1 < fin2) AND (fin1 > inicio2)

Ejemplo:
  Cita existente: 10:00 - 11:00
  Nueva cita:     10:30 - 11:30
  
  ¿Solapan? (10:30 < 11:00) AND (11:30 > 10:00) = TRUE ✓
```

##### 4. Cálculo de Disponibilidad
```java
public AvailabilityDTO getAvailability(LocalDate fecha) {
    // 1. Definir horario laboral (08:00 - 20:00 en intervalos de 30 min)
    List<String> allSlots = generateTimeSlots();
    
    // 2. Obtener citas activas del día
    List<Appointment> appointments = repository.findActiveAppointmentsByDate(fecha);
    
    // 3. Determinar slots ocupados
    List<String> ocupados = new ArrayList<>();
    for (Appointment apt : appointments) {
        ocupados.addAll(getSlotsForAppointment(apt));
    }
    
    // 4. Calcular disponibles = todos - ocupados
    List<String> disponibles = allSlots.stream()
        .filter(slot -> !ocupados.contains(slot))
        .collect(Collectors.toList());
    
    // 5. Construir DTO
    return AvailabilityDTO.builder()
        .fecha(fecha)
        .horariosDisponibles(disponibles)
        .horariosOcupados(ocupados)
        .totalDisponibles(disponibles.size())
        .build();
}

private List<String> generateTimeSlots() {
    List<String> slots = new ArrayList<>();
    LocalTime start = LocalTime.of(8, 0);
    LocalTime end = LocalTime.of(20, 0);
    
    while (start.isBefore(end)) {
        LocalTime slotEnd = start.plusMinutes(30);
        slots.add(start + " - " + slotEnd);
        start = slotEnd;
    }
    
    return slots; // 24 slots de 30 min cada uno
}
```

##### 5. Transiciones de Estado
```java
@Transactional
public AppointmentResponseDTO confirmarCita(Long id) {
    Appointment appointment = findByIdOrThrow(id);
    
    // Validar transición permitida
    if (appointment.getEstado() != EstadoCita.PENDIENTE) {
        throw new InvalidStateTransitionException(
            "Solo se pueden confirmar citas en estado PENDIENTE"
        );
    }
    
    appointment.setEstado(EstadoCita.CONFIRMADA);
    Appointment updated = repository.save(appointment);
    
    return mapToResponseDTO(updated);
}

@Transactional
public AppointmentResponseDTO cancelarCita(Long id) {
    Appointment appointment = findByIdOrThrow(id);
    
    // Validar transición permitida
    if (appointment.getEstado() == EstadoCita.COMPLETADA) {
        throw new InvalidStateTransitionException(
            "No se puede cancelar una cita completada"
        );
    }
    
    if (appointment.getEstado() == EstadoCita.CANCELADA) {
        throw new InvalidStateTransitionException(
            "La cita ya está cancelada"
        );
    }
    
    appointment.setEstado(EstadoCita.CANCELADA);
    Appointment updated = repository.save(appointment);
    
    return mapToResponseDTO(updated);
}

@Transactional
public AppointmentResponseDTO completarCita(Long id) {
    Appointment appointment = findByIdOrThrow(id);
    
    // Validar transición permitida
    if (appointment.getEstado() != EstadoCita.CONFIRMADA) {
        throw new InvalidStateTransitionException(
            "Solo se pueden completar citas confirmadas"
        );
    }
    
    appointment.setEstado(EstadoCita.COMPLETADA);
    Appointment updated = repository.save(appointment);
    
    return mapToResponseDTO(updated);
}
```

**Máquina de Estados Implementada**:
```
    PENDIENTE
      ↓    ↓
   CONF   CANC
    ↓      ✗
  COMP    
```

##### 6. Mapeo DTO ↔ Entity
```java
// Request DTO → Entity
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

// Entity → Response DTO
private AppointmentResponseDTO mapToResponseDTO(Appointment entity) {
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

// Entity → Confirmation DTO
private AppointmentConfirmationDTO mapToConfirmationDTO(Appointment entity) {
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
```

**Decisión de No Usar MapStruct**:
- Proyecto educativo → mapeo manual más didáctico
- Mapeos simples 1:1 sin transformaciones complejas
- Control total sobre la lógica de transformación

---

### 5️⃣ Capa de Presentación (Controller)

#### AppointmentController.java

```java
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor  // Inyección por constructor con Lombok
public class AppointmentController {
    
    private final AppointmentService service;
    
    // POST /api/appointments
    @PostMapping
    public ResponseEntity<AppointmentConfirmationDTO> create(
        @Valid @RequestBody AppointmentRequestDTO dto
    ) {
        AppointmentConfirmationDTO created = service.createAppointment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    // GET /api/appointments
    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllAppointments());
    }
    
    // GET /api/appointments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getById(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getAppointmentById(id));
    }
    
    // GET /api/appointments/codigo/{codigo}
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<AppointmentResponseDTO> getByCodigo(
        @PathVariable String codigo
    ) {
        return ResponseEntity.ok(service.getAppointmentByCodigo(codigo));
    }
    
    // GET /api/appointments/cliente/email/{email}
    @GetMapping("/cliente/email/{email}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByEmail(
        @PathVariable String email
    ) {
        return ResponseEntity.ok(service.getAppointmentsByEmail(email));
    }
    
    // GET /api/appointments/estado/{estado}
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByEstado(
        @PathVariable EstadoCita estado
    ) {
        return ResponseEntity.ok(service.getAppointmentsByEstado(estado));
    }
    
    // GET /api/appointments/fecha/{fecha}
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByFecha(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return ResponseEntity.ok(service.getAppointmentsByFecha(fecha));
    }
    
    // GET /api/appointments/availability/{fecha}
    @GetMapping("/availability/{fecha}")
    public ResponseEntity<AvailabilityDTO> getAvailability(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return ResponseEntity.ok(service.getAvailability(fecha));
    }
    
    // PUT /api/appointments/{id}
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> update(
        @PathVariable Long id,
        @Valid @RequestBody AppointmentRequestDTO dto
    ) {
        return ResponseEntity.ok(service.updateAppointment(id, dto));
    }
    
    // PATCH /api/appointments/{id}/confirmar
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<AppointmentResponseDTO> confirmar(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.confirmarCita(id));
    }
    
    // PATCH /api/appointments/{id}/cancelar
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AppointmentResponseDTO> cancelar(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.cancelarCita(id));
    }
    
    // PATCH /api/appointments/{id}/completar
    @PatchMapping("/{id}/completar")
    public ResponseEntity<AppointmentResponseDTO> completar(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.completarCita(id));
    }
    
    // DELETE /api/appointments/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Decisiones REST**:
- **POST** → 201 Created (recurso creado)
- **GET** → 200 OK
- **PUT** → 200 OK (actualización completa)
- **PATCH** → 200 OK (actualización parcial)
- **DELETE** → 204 No Content

**Validación**:
- `@Valid` activa Bean Validation en DTOs
- Spring maneja errores automáticamente → 400 Bad Request

---

### 6️⃣ Manejo de Excepciones

#### Excepciones Personalizadas

```java
// 404 Not Found
public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(String message) {
        super(message);
    }
}

// 400 Bad Request
public class InvalidTimeRangeException extends RuntimeException {
    public InvalidTimeRangeException(String message) {
        super(message);
    }
}

// 409 Conflict
public class TimeSlotNotAvailableException extends RuntimeException {
    public TimeSlotNotAvailableException(String message) {
        super(message);
    }
}

// 400 Bad Request
public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
```

#### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // 404 Not Found
    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        AppointmentNotFoundException ex
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    // 400 Bad Request (validaciones de negocio)
    @ExceptionHandler({
        InvalidTimeRangeException.class,
        InvalidStateTransitionException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
        RuntimeException ex
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    // 409 Conflict (solapamiento de horarios)
    @ExceptionHandler(TimeSlotNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
        TimeSlotNotAvailableException ex
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    // 400 Bad Request (validaciones Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex
    ) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Error en los datos enviados")
            .details(errors)
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
}
```

**Estructura de ErrorResponse**:
```java
@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private List<String> details;  // Opcional para múltiples errores
}
```

---

## 🎯 Patrones y Mejores Prácticas Aplicadas

### 1. Inyección de Dependencias
```java
@RequiredArgsConstructor  // Constructor con final fields
private final AppointmentService service;
```
**Ventajas**: Inmutabilidad, testeable, sin @Autowired

### 2. Transaccionalidad
```java
@Transactional  // En Service para operaciones de escritura
public AppointmentResponseDTO confirmarCita(Long id) {
    // ...
}
```
**Garantiza**: ACID, rollback automático en excepciones

### 3. Validaciones en Capas
- **Controller**: Bean Validation con `@Valid`
- **Service**: Lógica de negocio compleja
- **Repository**: Constraints de BD

### 4. Separación de Responsabilidades (SRP)
- **Controller**: Solo maneja HTTP
- **Service**: Solo lógica de negocio
- **Repository**: Solo acceso a datos
- **DTO**: Solo transferencia de datos

### 5. Manejo de Errores Centralizado
```java
@RestControllerAdvice  // Único punto de manejo de errores
```

### 6. Inmutabilidad en DTOs
```java
@Builder  // Patrón Builder para construcción limpia
@Getter   // Solo getters, sin setters
```

### 7. Consultas Optimizadas
```java
@Query("SELECT a FROM Appointment a WHERE ...")
// JPQL específico vs fetch innecesario
```

---

## 📊 Diagramas de Flujo

### Flujo: Crear Cita

```
Cliente → POST /api/appointments
    ↓
Controller: @Valid valida DTO
    ↓
Service: validateBusinessHours()
    ↓
Service: validateNoOverlap()
    ↓
Service: validateMinimumAdvance()
    ↓
Service: mapToEntity()
    ↓
Service: generateUniqueCode()
    ↓
Repository: save()
    ↓
Service: mapToConfirmationDTO()
    ↓
Controller: ResponseEntity 201
    ↓
Cliente ← AppointmentConfirmationDTO
```

### Flujo: Calcular Disponibilidad

```
Cliente → GET /api/appointments/availability/2024-12-20
    ↓
Controller: parsea fecha
    ↓
Service: generateTimeSlots() → 24 slots de 30 min
    ↓
Repository: findActiveAppointmentsByDate()
    ↓
Service: determinar slots ocupados
    ↓
Service: calcular disponibles = todos - ocupados
    ↓
Service: construir AvailabilityDTO
    ↓
Controller: ResponseEntity 200
    ↓
Cliente ← AvailabilityDTO con horarios
```

---

## 🧪 Casos de Prueba Recomendados

### Happy Path
1. ✅ Crear cita válida → 201 + ConfirmationDTO
2. ✅ Listar todas las citas → 200 + List
3. ✅ Buscar por código → 200 + ResponseDTO
4. ✅ Confirmar cita PENDIENTE → 200 + estado CONFIRMADA
5. ✅ Ver disponibilidad → 200 + AvailabilityDTO
6. ✅ Cancelar cita → 200 + estado CANCELADA

### Edge Cases
1. ⚠️ Crear cita con fecha pasada → 400 (Bean Validation)
2. ⚠️ Crear cita con hora fin < hora inicio → 400
3. ⚠️ Crear cita con duración < 15 min → 400
4. ⚠️ Crear cita con horario solapado → 409
5. ⚠️ Confirmar cita CANCELADA → 400 (InvalidStateTransition)
6. ⚠️ Cancelar cita COMPLETADA → 400
7. ⚠️ Buscar cita inexistente → 404

---

## 📚 Conceptos Avanzados del Proyecto

### 1. Algoritmo de Detección de Solapamiento
```
Dos intervalos [A, B] y [C, D] solapan si:
  A < D AND B > C

Aplicado a citas:
  horaInicio1 < horaFin2 AND horaFin1 > horaInicio2
```

### 2. Generación de Códigos Únicos con UUID
```java
UUID → "a8f3e1c0-9b2d-4e5f-8c3a-1b4d9e7f2c8a"
substring(0, 4) → "a8f3"
toUpperCase() → "A8F3"
prefix → "APT-A8F3"
```

### 3. Cálculo de Duración con ChronoUnit
```java
LocalTime inicio = LocalTime.of(10, 0);
LocalTime fin = LocalTime.of(11, 30);
long minutos = ChronoUnit.MINUTES.between(inicio, fin);
// minutos = 90
```

### 4. Máquina de Estados Finitos
```
Estados: 4 (PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA)
Transiciones: 5 permitidas
Validación: Evitar transiciones ilegales
```

---

## ✅ Checklist de Calidad del Código

- [x] **Nomenclatura clara**: métodos, variables, clases
- [x] **SOLID**: Single Responsibility, Open/Closed, etc.
- [x] **DRY**: No hay código duplicado
- [x] **Comentarios explicativos** en lógica compleja
- [x] **Manejo de excepciones** robusto
- [x] **Validaciones en múltiples capas**
- [x] **DTOs especializados** por caso de uso
- [x] **Consultas optimizadas** con @Query
- [x] **Transacciones** en operaciones de escritura
- [x] **Códigos de estado HTTP** correctos
- [x] **Documentación README** completa

---

## 🎓 Lecciones Aprendidas

### Técnicas
1. **DTOs especializados** mejoran la API y UX
2. **Validaciones cruzadas** con `@AssertTrue` son potentes
3. **JPQL** permite consultas complejas de manera limpia
4. **Enums** para estados garantizan type-safety
5. **LocalDate/LocalTime** simplifican cálculos de fechas

### Arquitectura
1. **Separación estricta** de capas facilita mantenimiento
2. **Service** debe contener toda la lógica de negocio
3. **Controller** debe ser thin (solo HTTP handling)
4. **Excepciones personalizadas** mejoran la claridad

### Mejores Prácticas
1. **Inyección por constructor** > @Autowired
2. **Lombok** reduce boilerplate significativamente
3. **ResponseEntity** da control total sobre respuestas HTTP
4. **@ControllerAdvice** centraliza el manejo de errores

---

**Implementación completa y documentada del Proyecto 8**  
**Sistema de Reservas de Citas con DTOs Avanzados**