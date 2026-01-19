# 📚 Proyecto 9 - Plataforma de Cursos Online

## 🎯 Objetivo del Proyecto
Desarrollar una API REST para gestionar una plataforma de cursos online, implementando **múltiples DTOs** especializados según el contexto de uso y aplicando **lógica de negocio** para cálculos de precios con descuentos y generación de estadísticas.

## 🎓 Nivel: Intermedio - DTOs Avanzados
**Fase del Curso:** Proyectos 6-10 (Introducción de DTOs)  
**Complejidad DTOs:** Alta - Múltiples DTOs con diferentes propósitos, cálculos y agregaciones

## 📋 Requisitos Funcionales

### Entidad Principal: Course
- **id**: Long (auto-generado)
- **title**: String (obligatorio, único, max 200 caracteres)
- **description**: String (obligatorio, max 2000 caracteres)
- **instructor**: String (obligatorio, max 150 caracteres)
- **durationHours**: Integer (obligatorio, mínimo 1, máximo 500)
- **level**: Enum [BEGINNER, INTERMEDIATE, ADVANCED] (obligatorio)
- **price**: BigDecimal (obligatorio, mínimo 0, máximo 9999.99)
- **discount**: Integer (0-100, representa porcentaje de descuento)
- **finalPrice**: BigDecimal (calculado automáticamente: price - (price * discount / 100))
- **category**: String (obligatorio, max 50 caracteres)
- **videoUrl**: String (URL del video promocional)
- **thumbnail**: String (URL de la imagen del curso)
- **publishedDate**: LocalDate (fecha de publicación)
- **enrolledStudents**: Integer (default 0, número de estudiantes inscritos)
- **averageRating**: Double (default 0.0, calificación promedio 0-5)

### DTOs Requeridos

#### 1️⃣ CourseCardDTO (Para Listados)
**Propósito:** Vista compacta para tarjetas de cursos en catálogo  
**Campos:**
- id
- title
- instructor
- durationHours
- level
- finalPrice (calculado)
- thumbnail
- enrolledStudents
- averageRating

#### 2️⃣ CourseDetailDTO (Vista Completa)
**Propósito:** Información completa de un curso específico  
**Campos:**
- id
- title
- description
- instructor
- durationHours
- level
- price (original)
- discount
- finalPrice (calculado)
- category
- videoUrl
- thumbnail
- publishedDate
- enrolledStudents
- averageRating

#### 3️⃣ CourseCreateDTO (Creación)
**Propósito:** Datos necesarios para crear un curso  
**Campos:**
- title (validación @NotBlank, @Size)
- description (validación @NotBlank, @Size)
- instructor (validación @NotBlank)
- durationHours (validación @Min, @Max)
- level (validación @NotNull)
- price (validación @NotNull, @DecimalMin, @DecimalMax)
- discount (validación @Min, @Max)
- category (validación @NotBlank)
- videoUrl (opcional)
- thumbnail (opcional)
- publishedDate (opcional, default hoy)

#### 4️⃣ CourseUpdateDTO (Actualización)
**Propósito:** Campos actualizables (similar a CreateDTO pero todos opcionales)  
**Campos:** Todos los de CourseCreateDTO pero opcionales

#### 5️⃣ CourseEnrollmentDTO (Inscripción)
**Propósito:** Actualizar estadísticas al inscribir estudiante  
**Campos:**
- courseId
- enrolledStudents (incrementado)
- finalPrice (para confirmar pago)

#### 6️⃣ CourseStatsDTO (Estadísticas)
**Propósito:** Agregación de datos de múltiples cursos  
**Campos:**
- totalCourses
- totalEnrolledStudents
- averageRating (global)
- averagePrice (global)
- coursesByLevel (Map<String, Long>)
- topRatedCourse (CourseCardDTO del mejor valorado)

## 🔧 Requisitos Técnicos

### Arquitectura
- **Capa Controller:** Endpoints REST con DTOs de entrada/salida
- **Capa Service:** Lógica de negocio, cálculos, conversiones DTO ↔ Entity
- **Capa Repository:** JpaRepository con queries personalizadas
- **Capa Entity:** Modelo de datos con validaciones

### Endpoints REST

#### 📦 CRUD Básico
- `POST /api/courses` - Crear curso (CourseCreateDTO → CourseDetailDTO)
- `GET /api/courses` - Listar cursos (→ List<CourseCardDTO>)
- `GET /api/courses/{id}` - Obtener curso (→ CourseDetailDTO)
- `PUT /api/courses/{id}` - Actualizar curso (CourseUpdateDTO → CourseDetailDTO)
- `DELETE /api/courses/{id}` - Eliminar curso

#### 🔍 Búsquedas y Filtros
- `GET /api/courses/category/{category}` - Cursos por categoría (→ List<CourseCardDTO>)
- `GET /api/courses/level/{level}` - Cursos por nivel (→ List<CourseCardDTO>)
- `GET /api/courses/instructor/{instructor}` - Cursos por instructor (→ List<CourseCardDTO>)
- `GET /api/courses/price-range?min={min}&max={max}` - Cursos por rango de precio final

#### 📊 Estadísticas y Especiales
- `GET /api/courses/stats` - Estadísticas globales (→ CourseStatsDTO)
- `GET /api/courses/top-rated` - Top 5 cursos mejor valorados (→ List<CourseCardDTO>)
- `GET /api/courses/popular` - Top 5 cursos más populares por inscritos (→ List<CourseCardDTO>)
- `POST /api/courses/{id}/enroll` - Inscribir estudiante (incrementa enrolledStudents)
- `PUT /api/courses/{id}/rating?rating={rating}` - Actualizar calificación promedio

### Validaciones
- Precios: 2 decimales, entre 0 y 9999.99
- Descuentos: 0-100 (porcentaje)
- Duración: 1-500 horas
- Calificaciones: 0.0-5.0
- Títulos únicos (constraint en DB)

### Lógica de Negocio Destacada
1. **Cálculo de Precio Final:** `finalPrice = price - (price * discount / 100)`
2. **Actualización de Rating:** Recalcular promedio al recibir nueva calificación
3. **Estadísticas Agregadas:** Contar cursos por nivel, promedios globales
4. **Incremento de Inscritos:** Operación atómica al inscribir

## 📦 Estructura del Proyecto
```
project-09-online-courses/
├── README.md (este archivo)
├── working/
│   ├── README.md (guía de desarrollo)
│ 
│   └── src/main/java/com/springcourse/onlinecourses/
│       ├── entity/
│       │   └── Course.java
│       ├── dto/
│       │   ├── CourseCardDTO.java
│       │   ├── CourseDetailDTO.java
│       │   ├── CourseCreateDTO.java
│       │   ├── CourseUpdateDTO.java
│       │   ├── CourseEnrollmentDTO.java
│       │   └── CourseStatsDTO.java
│       ├── repository/
│       │   └── CourseRepository.java
│       ├── service/
│       │   └── CourseService.java
│       └── controller/
│           └── CourseController.java
└── solution/
    ├── README.md (documentación técnica)
    └── src/ (código completo comentado)
```

## 🎯 Objetivos de Aprendizaje
1. ✅ Diseñar DTOs especializados según contexto de uso
2. ✅ Implementar lógica de negocio en capa Service
3. ✅ Realizar cálculos automáticos (precios con descuento)
4. ✅ Generar estadísticas agregadas desde repositorio
5. ✅ Aplicar validaciones con Bean Validation
6. ✅ Mapear Entity ↔ DTO en múltiples direcciones
7. ✅ Usar enums para campos con valores fijos
8. ✅ Gestionar tipos de datos específicos (BigDecimal, LocalDate)

## 📚 Conceptos Clave
- **DTOs por Contexto:** Card (listado), Detail (vista completa), Create/Update (modificación)
- **Separation of Concerns:** Lógica de negocio en Service, no en Controller
- **Immutability:** DTOs como records inmutables
- **Calculated Fields:** finalPrice calculado, no almacenado duplicado
- **Aggregation Queries:** Estadísticas con métodos de repositorio
- **BigDecimal:** Precisión en cálculos monetarios

## 🚀 Siguientes Pasos
1. Leer `working/README.md` para guía de inicio
2. Implementar clase por clase siguiendo metodología
3. Probar con colección Postman incluida
4. Comparar con `solution/` al finalizar

---
**Proyecto 9/20** | Fase DTOs | Complejidad: ⭐⭐⭐⭐ | Testing: ❌ (desde P10)