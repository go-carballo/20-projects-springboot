# Proyecto 8: Sistema de Reservas de Citas 📅

## 📖 Descripción General

Sistema completo de gestión de reservas de citas con generación automática de códigos de confirmación, validaciones avanzadas de horarios, cálculo de duraciones y control de disponibilidad. Este proyecto introduce DTOs complejos con lógica de negocio avanzada y validaciones cruzadas.

**Fase del Curso**: DTOs Intermedios (Proyecto 6-10)  
**Complejidad**: ⭐⭐⭐ Media-Alta  
**Testing**: No incluido (se introduce en Proyecto 10)

---

## 🎯 Objetivos de Aprendizaje

### Técnicos
- **DTOs con validaciones complejas**: Validación de rangos de fechas/horas, coherencia entre campos
- **Generación automática de códigos**: UUIDs cortos para confirmación de citas
- **Cálculos de duración**: LocalTime para calcular duración entre hora inicio y fin
- **Múltiples DTOs especializados**: Diferentes representaciones según caso de uso
- **Manejo de estados**: Enum para estados de cita (PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA)

### Mejores Prácticas
- **Separación de responsabilidades**: Cada DTO tiene un propósito específico
- **Validaciones declarativas**: Uso de Bean Validation con mensajes personalizados
- **Mapeo DTO-Entity bidireccional**: Conversión limpia en capa Service
- **Manejo de errores específico**: Excepciones personalizadas para reglas de negocio

---

## 📊 Modelo de Datos

### Entidad: Appointment

```java
Appointment {
    Long id                      // PK autoincremental
    String nombreCliente         // Nombre completo del cliente
    String email                 // Email de contacto
    String telefono              // Teléfono de contacto
    LocalDate fecha              // Fecha de la cita
    LocalTime horaInicio         // Hora de inicio
    LocalTime horaFin            // Hora de finalización
    String servicio              // Tipo de servicio (ej: "Consulta", "Revisión")
    EstadoCita estado            // PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA
    BigDecimal precio            // Precio del servicio
    String notas                 // Notas adicionales (opcional)
    String codigoConfirmacion    // Código único para confirmar/gestionar (ej: "APT-A3F9")
    LocalDateTime createdAt      // Timestamp de creación
    LocalDateTime updatedAt      // Timestamp de última actualización
}
```

### Enum: EstadoCita
```
PENDIENTE     - Cita creada, pendiente de confirmación
CONFIRMADA    - Cliente confirmó la cita
CANCELADA     - Cita cancelada
COMPLETADA    - Servicio prestado
```

---

## 🔄 DTOs del Sistema

### 1️⃣ AppointmentRequestDTO
**Propósito**: Recibir datos para crear/actualizar citas  
**Campos**:
- `nombreCliente` (String, @NotBlank, @Size 2-100)
- `email` (String, @NotBlank, @Email)
- `telefono` (String, @NotBlank, @Pattern para formato)
- `fecha` (LocalDate, @NotNull, @FutureOrPresent)
- `horaInicio` (LocalTime, @NotNull)
- `horaFin` (LocalTime, @NotNull)
- `servicio` (String, @NotBlank, @Size 2-100)
- `precio` (BigDecimal, @NotNull, @DecimalMin "0.0")
- `notas` (String, @Size max 500, opcional)

**Validaciones Especiales**:
- Hora fin debe ser posterior a hora inicio
- Fecha no puede ser anterior a hoy
- Duración mínima: 15 minutos
- Duración máxima: 8 horas

### 2️⃣ AppointmentResponseDTO
**Propósito**: Devolver información completa de citas  
**Campos**: Todos los de Request + id, estado, codigoConfirmacion, duracionMinutos, timestamps

### 3️⃣ AppointmentConfirmationDTO
**Propósito**: Respuesta simplificada al crear cita (confirmación al cliente)  
**Campos**:
- `id`
- `codigoConfirmacion`
- `nombreCliente`
- `fecha`
- `horaInicio`
- `horaFin`
- `servicio`
- `estado`
- `mensaje` (String descriptivo: "Cita reservada con éxito. Código de confirmación: ...")

### 4️⃣ AvailabilityDTO
**Propósito**: Mostrar disponibilidad de horarios para una fecha  
**Campos**:
- `fecha` (LocalDate)
- `horariosDisponibles` (List<String> con formato "HH:mm - HH:mm")
- `horariosOcupados` (List<String>)
- `totalDisponibles` (Integer)

---

## 🛣️ Endpoints REST

### Base URL: `/api/appointments`

| Método | Endpoint | Body | Descripción |
|--------|----------|------|-------------|
| **POST** | `/` | AppointmentRequestDTO | Crear nueva cita → AppointmentConfirmationDTO |
| **GET** | `/` | - | Listar todas las citas → List<AppointmentResponseDTO> |
| **GET** | `/{id}` | - | Obtener cita por ID → AppointmentResponseDTO |
| **GET** | `/codigo/{codigo}` | - | Buscar cita por código confirmación → AppointmentResponseDTO |
| **GET** | `/cliente/email/{email}` | - | Listar citas de un cliente → List<AppointmentResponseDTO> |
| **GET** | `/estado/{estado}` | - | Filtrar por estado → List<AppointmentResponseDTO> |
| **GET** | `/fecha/{fecha}` | - | Filtrar por fecha → List<AppointmentResponseDTO> |
| **GET** | `/availability/{fecha}` | - | Ver disponibilidad para una fecha → AvailabilityDTO |
| **PUT** | `/{id}` | AppointmentRequestDTO | Actualizar cita → AppointmentResponseDTO |
| **PATCH** | `/{id}/confirmar` | - | Confirmar cita → AppointmentResponseDTO |
| **PATCH** | `/{id}/cancelar` | - | Cancelar cita → AppointmentResponseDTO |
| **PATCH** | `/{id}/completar` | - | Marcar como completada → AppointmentResponseDTO |
| **DELETE** | `/{id}` | - | Eliminar cita → 204 No Content |

---

## 🔐 Reglas de Negocio

### Validaciones de Horarios
1. **Horario laboral**: 08:00 - 20:00
2. **Duración mínima**: 15 minutos
3. **Duración máxima**: 8 horas
4. **No solapamiento**: No puede haber dos citas en el mismo horario
5. **Anticipación mínima**: Citas deben crearse con al menos 2 horas de anticipación

### Generación de Código de Confirmación
- **Formato**: `APT-XXXX` (APT + 4 caracteres alfanuméricos en mayúsculas)
- **Ejemplo**: `APT-A3F9`, `APT-K7M2`
- **Único**: No puede haber dos códigos iguales
- **Generación**: Automática al crear la cita

### Transiciones de Estado
- **PENDIENTE** → CONFIRMADA (mediante endpoint `/confirmar`)
- **PENDIENTE** → CANCELADA (mediante endpoint `/cancelar`)
- **CONFIRMADA** → CANCELADA (mediante endpoint `/cancelar`)
- **CONFIRMADA** → COMPLETADA (mediante endpoint `/completar`)
- ❌ No se puede confirmar/completar una cita CANCELADA
- ❌ No se puede cancelar una cita COMPLETADA

### Cálculo de Disponibilidad
- **Horario base**: 08:00 - 20:00 en intervalos de 30 minutos
- **Excluir**: Horarios ocupados por citas CONFIRMADAS o PENDIENTES
- **Mostrar**: Lista de bloques disponibles y ocupados

---

## 🛠️ Stack Tecnológico

- **Spring Boot**: 3.x
- **Spring Data JPA**: Acceso a datos
- **H2 Database**: Base de datos en memoria
- **Bean Validation**: Validaciones declarativas
- **Lombok**: Reducción de boilerplate
- **Java 17+**: Records, LocalDate/LocalTime

---

## 📦 Estructura del Proyecto

```
proyecto-8-sistema-reservas/
├── working/                          # Proyecto base para desarrollo
│   ├── src/
│   │   └── main/
│   │       ├── java/com/proyecto8/
│   │       │   ├── entity/          # Entidades JPA
│   │       │   ├── dto/             # DTOs (Request, Response, Confirmation, Availability)
│   │       │   ├── repository/      # Repositorios Spring Data JPA
│   │       │   ├── service/         # Lógica de negocio
│   │       │   ├── controller/      # Controladores REST
│   │       │   └── exception/       # Excepciones personalizadas
│   │       └── resources/
│   │           └── application.properties
│   ├── postman/
│   │   ├── Proyecto8-Appointments.postman_collection.json
│   │   └── ejemplos/                # JSONs de ejemplo
│   └── README.md                     # Guía de desarrollo
├── solution/                         # Solución completa comentada
│   └── (misma estructura que working)
└── README.md                         # Este archivo
```

---

## 🚀 Progresión del Desarrollo

### Fase 1: Modelo Base
1. Crear enum `EstadoCita`
2. Crear entidad `Appointment`
3. Crear repositorio `AppointmentRepository`

### Fase 2: DTOs
4. `AppointmentRequestDTO` (con validaciones)
5. `AppointmentResponseDTO`
6. `AppointmentConfirmationDTO`
7. `AvailabilityDTO`

### Fase 3: Capa de Servicio
8. Métodos de generación de código
9. Validaciones de horarios
10. Lógica de disponibilidad
11. Transiciones de estado
12. Mapeo DTO ↔ Entity

### Fase 4: Controlador REST
13. Endpoints CRUD básicos
14. Endpoints de búsqueda
15. Endpoints de cambio de estado
16. Endpoint de disponibilidad

---

## 📚 Conceptos Clave del Proyecto

### 1. Validaciones Complejas en DTOs
```java
// Ejemplo de validación cruzada
@AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
private boolean isHoraFinValid() {
    return horaFin != null && horaInicio != null && horaFin.isAfter(horaInicio);
}
```

### 2. Generación de Códigos Únicos
```java
// Patrón para generar códigos con UUID
String codigo = "APT-" + UUID.randomUUID()
    .toString()
    .substring(0, 4)
    .toUpperCase();
```

### 3. Cálculo de Duración
```java
// LocalTime para calcular diferencia
long duracionMinutos = ChronoUnit.MINUTES.between(horaInicio, horaFin);
```

### 4. Consultas Personalizadas
```java
// Buscar citas que solapen con un horario
@Query("SELECT a FROM Appointment a WHERE a.fecha = :fecha " +
       "AND a.estado IN ('PENDIENTE', 'CONFIRMADA') " +
       "AND ((a.horaInicio < :horaFin AND a.horaFin > :horaInicio))")
List<Appointment> findOverlappingAppointments(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin);
```

---

## ✅ Criterios de Éxito

- [ ] 4 DTOs implementados con validaciones completas
- [ ] Generación automática de códigos de confirmación
- [ ] Validación de solapamiento de horarios
- [ ] Cálculo correcto de duración
- [ ] Endpoints de cambio de estado funcionando
- [ ] Endpoint de disponibilidad con lógica correcta
- [ ] Manejo de excepciones personalizado
- [ ] Código limpio y bien comentado
- [ ] Colección Postman completa y funcional
- [ ] READMEs detallados en working/ y solution/

---

## 🎓 Aprendizajes del Proyecto

Al completar este proyecto habrás aprendido:

✅ Implementar DTOs especializados según caso de uso  
✅ Validaciones complejas con Bean Validation  
✅ Generación automática de identificadores únicos  
✅ Trabajo con LocalDate y LocalTime para cálculos  
✅ Consultas personalizadas con @Query  
✅ Máquina de estados para transiciones controladas  
✅ Lógica de disponibilidad con algoritmos de detección de solapamientos  
✅ Separación clara de responsabilidades en capas  

---

**Proyecto desarrollado como parte del curso progresivo de Spring Boot**  
**Fase**: DTOs Intermedios (6-10) | **Proyecto**: 8/20