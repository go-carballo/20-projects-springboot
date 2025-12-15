# Sistema de Reservas de Citas - Proyecto Base 🚀

## 📌 Información del Proyecto

**Proyecto**: 8 - Sistema de Reservas de Citas  
**Fase**: DTOs Intermedios (6-10)  
**Complejidad**: ⭐⭐⭐ Media-Alta

---

## 🎯 Objetivo

Desarrollar un sistema completo de gestión de reservas de citas con:
- ✅ 4 DTOs especializados
- ✅ Validaciones complejas de horarios
- ✅ Generación automática de códigos de confirmación
- ✅ Cálculo de duraciones
- ✅ Control de disponibilidad

---

## 📁 Estructura de Carpetas

```
working/
├── src/main/java/com/proyecto8/
│   ├── model/
│   │   ├── EstadoCita.java              # Enum de estados
│   │   └── Appointment.java             # Entidad principal
│   ├── dto/
│   │   ├── AppointmentRequestDTO.java   # DTO de entrada
│   │   ├── AppointmentResponseDTO.java  # DTO de salida completo
│   │   ├── AppointmentConfirmationDTO.java # DTO de confirmación
│   │   └── AvailabilityDTO.java         # DTO de disponibilidad
│   ├── repository/
│   │   └── AppointmentRepository.java   # Repositorio JPA
│   ├── service/
│   │   └── AppointmentService.java      # Lógica de negocio
│   ├── controller/
│   │   └── AppointmentController.java   # Controlador REST
│   └── exception/
│       ├── AppointmentNotFoundException.java
│       ├── InvalidTimeRangeException.java
│       ├── TimeSlotNotAvailableException.java
│       └── InvalidStateTransitionException.java
```

---

# 📄 Ejemplos JSON


## Base URL
```
http://localhost:8080/api/appointments
```

---

## 1. Crear Cita
**POST** `/api/appointments`
```json
{
  "nombreCliente": "Juan Pérez",
  "email": "juan@email.com",
  "telefono": "+34 612 345 678",
  "fecha": "2025-01-20",
  "horaInicio": "10:00",
  "horaFin": "11:00",
  "servicio": "Consulta Médica",
  "precio": 45.50,
  "notas": "Primera visita"
}
```

**Respuesta**: 201 Created + código de confirmación

---

## 2. Crear Segunda Cita
**POST** `/api/appointments`
```json
{
  "nombreCliente": "María López",
  "email": "maria@email.com",
  "telefono": "+34 623 456 789",
  "fecha": "2025-01-20",
  "horaInicio": "12:00",
  "horaFin": "13:00",
  "servicio": "Fisioterapia",
  "precio": 35.00,
  "notas": "Sesión de seguimiento"
}
```

---

## 3. Crear Tercera Cita
**POST** `/api/appointments`
```json
{
  "nombreCliente": "Carlos Martínez",
  "email": "carlos@email.com",
  "telefono": "+34 634 567 890",
  "fecha": "2025-01-20",
  "horaInicio": "15:30",
  "horaFin": "16:30",
  "servicio": "Revisión Dental",
  "precio": 60.00,
  "notas": "Incluye limpieza"
}
```

---

## 4. Listar Todas las Citas
**GET** `/api/appointments`

---

## 5. Obtener por ID
**GET** `/api/appointments/1`

---

## 6. Buscar por Código
**GET** `/api/appointments/codigo/APT-A3F9`

*(Usar el código devuelto al crear)*

---

## 7. Buscar por Email
**GET** `/api/appointments/cliente/email/juan@email.com`

---

## 8. Filtrar por Estado PENDIENTE
**GET** `/api/appointments/estado/PENDIENTE`

---

## 9. Filtrar por Fecha
**GET** `/api/appointments/fecha/2025-01-20`

---

## 10. Ver Disponibilidad
**GET** `/api/appointments/availability/2025-01-20`

---

## 11. Confirmar Cita
**PATCH** `/api/appointments/1/confirmar`

---

## 12. Filtrar por Estado CONFIRMADA
**GET** `/api/appointments/estado/CONFIRMADA`

---

## 13. Actualizar Cita
**PUT** `/api/appointments/1`
```json
{
  "nombreCliente": "Juan Pérez García",
  "email": "juan@email.com",
  "telefono": "+34 612 345 678",
  "fecha": "2025-01-20",
  "horaInicio": "10:30",
  "horaFin": "11:30",
  "servicio": "Consulta Médica - Revisión",
  "precio": 50.00,
  "notas": "Revisión tras primera visita"
}
```

---

## 14. Completar Cita
**PATCH** `/api/appointments/2/completar`

*(Primero confirmarla si está PENDIENTE)*

---

## 15. Cancelar Cita
**PATCH** `/api/appointments/3/cancelar`

---

## 16. Eliminar Cita
**DELETE** `/api/appointments/3`

**Respuesta**: 204 No Content

---

## Casos de Prueba de Validación

### Error: Email inválido
**POST** `/api/appointments`
```json
{
  "nombreCliente": "Test",
  "email": "email-invalido",
  "telefono": "+34 612345678",
  "fecha": "2025-01-20",
  "horaInicio": "10:00",
  "horaFin": "11:00",
  "servicio": "Test",
  "precio": 50.00
}
```
**Respuesta**: 400 Bad Request

---

### Error: Hora fin antes que inicio
**POST** `/api/appointments`
```json
{
  "nombreCliente": "Test",
  "email": "test@email.com",
  "telefono": "+34 612345678",
  "fecha": "2025-01-20",
  "horaInicio": "11:00",
  "horaFin": "10:00",
  "servicio": "Test",
  "precio": 50.00
}
```
**Respuesta**: 400 Bad Request

---

### Error: Horario ocupado
**POST** `/api/appointments` (con horario 10:00-11:00 ya ocupado)
```json
{
  "nombreCliente": "Test",
  "email": "test@email.com",
  "telefono": "+34 612345678",
  "fecha": "2025-01-20",
  "horaInicio": "10:30",
  "horaFin": "11:30",
  "servicio": "Test",
  "precio": 50.00
}
```
**Respuesta**: 409 Conflict

---

### Error: Transición de estado inválida
**PATCH** `/api/appointments/1/completar` (con cita en estado PENDIENTE)

**Respuesta**: 400 Bad Request

---

## Estados de Cita

- **PENDIENTE**: Estado inicial al crear
- **CONFIRMADA**: Cliente confirmó asistencia
- **CANCELADA**: Cita cancelada
- **COMPLETADA**: Servicio prestado (estado final)

---

## Transiciones Válidas
```
PENDIENTE → CONFIRMADA ✓
PENDIENTE → CANCELADA ✓
CONFIRMADA → CANCELADA ✓
CONFIRMADA → COMPLETADA ✓
```

---

## Orden de Prueba Recomendado

1. ✅ POST crear 3 citas diferentes (pasos 1-3)
2. ✅ GET listar todas (paso 4)
3. ✅ GET obtener por ID (paso 5)
4. ✅ GET buscar por código (paso 6)
5. ✅ GET buscar por email (paso 7)
6. ✅ GET filtrar por estado PENDIENTE (paso 8)
7. ✅ GET filtrar por fecha (paso 9)
8. ✅ GET ver disponibilidad (paso 10)
9. ✅ PATCH confirmar cita (paso 11)
10. ✅ GET filtrar por estado CONFIRMADA (paso 12)
11. ✅ PUT actualizar cita (paso 13)
12. ✅ PATCH completar cita (paso 14)
13. ✅ PATCH cancelar cita (paso 15)
14. ✅ DELETE eliminar cita (paso 16)
15. ✅ Probar casos de validación

---

## 📝 Notas

- Todas las fechas deben ser futuras (mínimo 2 horas de anticipación)
- El horario laboral es de 08:00 a 20:00
- Duración mínima de cita: 15 minutos
- Duración máxima de cita: 8 horas
- No puede haber dos citas en el mismo horario

---

## 🧪 Pruebas con Postman

### Orden Recomendado de Pruebas

1. **Crear varias citas** (POST `/api/appointments`)
2. **Listar todas** (GET `/api/appointments`)
3. **Buscar por código** (GET `/api/appointments/codigo/{codigo}`)
4. **Ver disponibilidad** (GET `/api/appointments/availability/2024-12-20`)
5. **Confirmar cita** (PATCH `/api/appointments/{id}/confirmar`)
6. **Buscar por estado** (GET `/api/appointments/estado/CONFIRMADA`)
7. **Buscar por email** (GET `/api/appointments/cliente/email/juan@email.com`)
8. **Actualizar cita** (PUT `/api/appointments/{id}`)
9. **Cancelar cita** (PATCH `/api/appointments/{id}/cancelar`)
10. **Completar cita** (PATCH `/api/appointments/{id}/completar`)
11. **Eliminar cita** (DELETE `/api/appointments/{id}`)

### Variables de Entorno Sugeridas

```
base_url = http://localhost:8080/api/appointments
appointment_id = 1
codigo_confirmacion = APT-A3F9
email_cliente = juan@email.com
fecha_test = 2024-12-20
```

---

## 🔐 Validaciones Implementadas

### AppointmentRequestDTO

| Campo | Validación | Mensaje de Error |
|-------|-----------|------------------|
| nombreCliente | @NotBlank, @Size(min=2, max=100) | "El nombre del cliente es obligatorio" |
| email | @NotBlank, @Email | "Email inválido" |
| telefono | @NotBlank, @Pattern | "Formato de teléfono inválido" |
| fecha | @NotNull, @FutureOrPresent | "La fecha no puede ser anterior a hoy" |
| horaInicio | @NotNull | "La hora de inicio es obligatoria" |
| horaFin | @NotNull | "La hora de fin es obligatoria" |
| servicio | @NotBlank, @Size(min=2, max=100) | "El servicio es obligatorio" |
| precio | @NotNull, @DecimalMin("0.0") | "El precio debe ser mayor o igual a 0" |
| notas | @Size(max=500) | "Las notas no pueden exceder 500 caracteres" |

### Validaciones Personalizadas
- ✅ Hora fin posterior a hora inicio
- ✅ Duración mínima: 15 minutos
- ✅ Duración máxima: 8 horas
- ✅ Horario laboral: 08:00 - 20:00
- ✅ No solapamiento con otras citas

---

## 🚨 Manejo de Excepciones

### Excepciones Personalizadas

| Excepción | Status HTTP | Caso de Uso |
|-----------|-------------|-------------|
| AppointmentNotFoundException | 404 Not Found | Cita no encontrada por ID/código |
| InvalidTimeRangeException | 400 Bad Request | Horario inválido (duración, rango) |
| TimeSlotNotAvailableException | 409 Conflict | Horario ocupado/solapamiento |
| InvalidStateTransitionException | 400 Bad Request | Transición de estado inválida |

---

## 📊 Datos de Prueba Sugeridos

### Cita 1: Consulta Médica
```json
{
  "nombreCliente": "Juan Pérez García",
  "email": "juan.perez@email.com",
  "telefono": "+34 612 345 678",
  "fecha": "2024-12-20",
  "horaInicio": "10:00",
  "horaFin": "11:00",
  "servicio": "Consulta Médica General",
  "precio": 45.50,
  "notas": "Primera visita"
}
```

### Cita 2: Sesión de Terapia
```json
{
  "nombreCliente": "María López Sánchez",
  "email": "maria.lopez@email.com",
  "telefono": "+34 623 456 789",
  "fecha": "2024-12-20",
  "horaInicio": "12:00",
  "horaFin": "13:00",
  "servicio": "Sesión de Fisioterapia",
  "precio": 35.00,
  "notas": "Sesión de seguimiento"
}
```

### Cita 3: Revisión Dental
```json
{
  "nombreCliente": "Carlos Martínez Ruiz",
  "email": "carlos.martinez@email.com",
  "telefono": "+34 634 567 890",
  "fecha": "2024-12-20",
  "horaInicio": "15:30",
  "horaFin": "16:30",
  "servicio": "Revisión Dental Completa",
  "precio": 60.00,
  "notas": "Incluye limpieza"
}
```

---

## ✅ Checklist de Desarrollo

### Fase 1: Modelo Base
- [ ] Crear enum `EstadoCita` con 4 estados
- [ ] Crear entidad `Appointment` con 11 campos + timestamps
- [ ] Crear `AppointmentRepository` con consultas personalizadas

### Fase 2: DTOs
- [ ] `AppointmentRequestDTO` con validaciones Bean Validation
- [ ] Validación personalizada de rangos de tiempo
- [ ] `AppointmentResponseDTO` con todos los campos
- [ ] `AppointmentConfirmationDTO` con mensaje descriptivo
- [ ] `AvailabilityDTO` con listas de horarios

### Fase 3: Service
- [ ] Método para generar códigos de confirmación únicos
- [ ] Validación de solapamiento de horarios
- [ ] Cálculo de duración en minutos
- [ ] Lógica de disponibilidad (08:00-20:00 en bloques de 30 min)
- [ ] Validación de transiciones de estado
- [ ] Mapeo bidireccional DTO ↔ Entity

### Fase 4: Controller
- [ ] POST `/api/appointments` → crear cita
- [ ] GET `/api/appointments` → listar todas
- [ ] GET `/api/appointments/{id}` → obtener por ID
- [ ] GET `/api/appointments/codigo/{codigo}` → buscar por código
- [ ] GET `/api/appointments/cliente/email/{email}` → filtrar por cliente
- [ ] GET `/api/appointments/estado/{estado}` → filtrar por estado
- [ ] GET `/api/appointments/fecha/{fecha}` → filtrar por fecha
- [ ] GET `/api/appointments/availability/{fecha}` → ver disponibilidad
- [ ] PUT `/api/appointments/{id}` → actualizar
- [ ] PATCH `/api/appointments/{id}/confirmar` → confirmar
- [ ] PATCH `/api/appointments/{id}/cancelar` → cancelar
- [ ] PATCH `/api/appointments/{id}/completar` → completar
- [ ] DELETE `/api/appointments/{id}` → eliminar

### Fase 5: Excepciones
- [ ] `AppointmentNotFoundException`
- [ ] `InvalidTimeRangeException`
- [ ] `TimeSlotNotAvailableException`
- [ ] `InvalidStateTransitionException`
- [ ] `@ControllerAdvice` para manejo global

---

## 🎯 Consideraciones Técnicas

### Generación de Código de Confirmación
```java
// Patrón sugerido en Service
private String generateConfirmationCode() {
    String code;
    do {
        code = "APT-" + UUID.randomUUID()
            .toString()
            .substring(0, 4)
            .toUpperCase();
    } while (repository.existsByCodigoConfirmacion(code));
    return code;
}
```

### Cálculo de Duración
```java
// En método de mapeo o DTO
long duracionMinutos = ChronoUnit.MINUTES.between(
    appointment.getHoraInicio(), 
    appointment.getHoraFin()
);
```

### Validación de Solapamiento
```java
// Query personalizada en Repository
@Query("SELECT a FROM Appointment a WHERE a.fecha = :fecha " +
       "AND a.estado IN ('PENDIENTE', 'CONFIRMADA') " +
       "AND ((a.horaInicio < :horaFin AND a.horaFin > :horaInicio))")
List<Appointment> findOverlappingAppointments(
    @Param("fecha") LocalDate fecha,
    @Param("horaInicio") LocalTime horaInicio,
    @Param("horaFin") LocalTime horaFin
);
```

---

## 📚 Recursos

- [Spring Boot Validation](https://spring.io/guides/gs/validating-form-input/)
- [LocalDate/LocalTime API](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)
- [UUID en Java](https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html)
- [Spring Data JPA @Query](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.at-query)

---

## 🚀 ¡Listo para Empezar!

1. Configura las dependencias y properties
2. Crea las clases siguiendo el orden del checklist
3. Prueba cada endpoint con Postman
4. Verifica las validaciones y manejo de errores

**¡Adelante con el desarrollo! 💪**