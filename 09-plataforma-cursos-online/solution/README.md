# 📚 Proyecto 9 - Solution

## 📖 Documentación Técnica de la Implementación

Esta solución implementa una API REST para gestión de cursos online con **DTOs especializados**, **lógica de negocio** para cálculos de precios y **estadísticas agregadas**.

## 🏗️ Arquitectura Implementada

### Diagrama de Capas
```
┌─────────────────────────────────────────────┐
│         CourseController (REST API)         │
│  - 14 endpoints REST                        │
│  - DTOs entrada/salida                      │
│  - ResponseEntity con status codes          │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│         CourseService (Business Logic)      │
│  - CRUD operations                          │
│  - Cálculo de finalPrice                    │
│  - Conversiones Entity ↔ DTO                │
│  - Generación de estadísticas               │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│      CourseRepository (Data Access)         │
│  - JpaRepository<Course, Long>              │
│  - Query methods personalizados             │
│  - Queries agregadas (@Query)               │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│           Course Entity (Model)             │
│  - 14 campos persistidos                    │
│  - Validaciones @Column                     │
│  - Enum Level                               │
└─────────────────────────────────────────────┘
```

## 📦 Componentes Principales

### 1️⃣ Entity Layer

#### Course.java
```java
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 200)
    private String title;
    
    @Column(nullable = false, length = 2000)
    private String description;
    
    @Column(nullable = false, length = 150)
    private String instructor;
    
    @Column(nullable = false)
    private Integer durationHours;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer discount = 0;
    
    // No persisted, calculated in runtime
    @Transient
    private BigDecimal finalPrice;
    
    @Column(nullable = false, length = 50)
    private String category;
    
    private String videoUrl;
    private String thumbnail;
    
    private LocalDate publishedDate;
    
    @Column(nullable = false)
    private Integer enrolledStudents = 0;
    
    @Column(nullable = false)
    private Double averageRating = 0.0;
    
    // Constructor, getters, setters
}

public enum Level {
    BEGINNER, INTERMEDIATE, ADVANCED
}
```

**Decisiones Técnicas:**
- ✅ `@Enumerated(EnumType.STRING)` → Legibilidad en DB
- ✅ `unique = true` en title → No duplicados
- ✅ `@Transient` en finalPrice → No persistir campo calculado
- ✅ `precision = 10, scale = 2` → Precisión monetaria
- ✅ `length` apropiado → Optimización de espacio

### 2️⃣ DTO Layer

#### CourseCardDTO.java (Vista Compacta)
```java
public record CourseCardDTO(
    Long id,
    String title,
    String instructor,
    Integer durationHours,
    String level,
    BigDecimal finalPrice,
    String thumbnail,
    Integer enrolledStudents,
    Double averageRating
) {}
```
**Uso:** Listados, catálogos, búsquedas

#### CourseDetailDTO.java (Vista Completa)
```java
public record CourseDetailDTO(
    Long id,
    String title,
    String description,
    String instructor,
    Integer durationHours,
    String level,
    BigDecimal price,
    Integer discount,
    BigDecimal finalPrice,
    String category,
    String videoUrl,
    String thumbnail,
    LocalDate publishedDate,
    Integer enrolledStudents,
    Double averageRating
) {}
```
**Uso:** Detalle de curso individual

#### CourseCreateDTO.java (Creación)
```java
public record CourseCreateDTO(
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    String title,
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres")
    String description,
    
    @NotBlank(message = "El instructor es obligatorio")
    String instructor,
    
    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración mínima es 1 hora")
    @Max(value = 500, message = "La duración máxima es 500 horas")
    Integer durationHours,
    
    @NotNull(message = "El nivel es obligatorio")
    Level level,
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio mínimo es 0")
    @DecimalMax(value = "9999.99", message = "El precio máximo es 9999.99")
    BigDecimal price,
    
    @Min(value = 0, message = "El descuento mínimo es 0%")
    @Max(value = 100, message = "El descuento máximo es 100%")
    Integer discount,
    
    @NotBlank(message = "La categoría es obligatoria")
    String category,
    
    String videoUrl,
    String thumbnail,
    LocalDate publishedDate
) {}
```
**Validaciones aplicadas en Controller con @Valid**

#### CourseStatsDTO.java (Estadísticas)
```java
public record CourseStatsDTO(
    Long totalCourses,
    Integer totalEnrolledStudents,
    Double averageRating,
    BigDecimal averagePrice,
    Map<String, Long> coursesByLevel,
    CourseCardDTO topRatedCourse
) {}
```
**Agregación compleja con nested DTO**

### 3️⃣ Repository Layer

#### CourseRepository.java
```java
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    // Búsquedas simples por campo
    List<Course> findByCategory(String category);
    List<Course> findByLevel(Level level);
    List<Course> findByInstructor(String instructor);
    
    // Top rankings
    List<Course> findTop5ByOrderByAverageRatingDesc();
    List<Course> findTop5ByOrderByEnrolledStudentsDesc();
    
    // Queries agregadas
    @Query("SELECT COUNT(c) FROM Course c WHERE c.level = :level")
    Long countByLevel(@Param("level") Level level);
    
    @Query("SELECT AVG(c.averageRating) FROM Course c")
    Double findAverageRatingGlobal();
    
    @Query("SELECT AVG(c.price) FROM Course c")
    BigDecimal findAveragePriceGlobal();
    
    @Query("SELECT SUM(c.enrolledStudents) FROM Course c")
    Integer findTotalEnrolledStudents();
}
```

**Notas:**
- Queries por precio requieren cálculo en Service (finalPrice no persistido)
- Queries agregadas usan JPQL con funciones SQL (COUNT, AVG, SUM)
- `@Param` para claridad en queries con parámetros

### 4️⃣ Service Layer

#### CourseService.java (Extracto clave)
```java
@Service
public class CourseService {
    
    private final CourseRepository repository;
    
    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }
    
    // ========== CRUD Operations ==========
    
    public CourseDetailDTO createCourse(CourseCreateDTO dto) {
        Course course = toEntity(dto);
        course = repository.save(course);
        return toDetailDTO(course);
    }
    
    public List<CourseCardDTO> getAllCourses() {
        return repository.findAll().stream()
            .map(this::toCardDTO)
            .toList();
    }
    
    // ========== Business Logic ==========
    
    /**
     * Inscribe un estudiante incrementando el contador.
     * Operación atómica: lee, incrementa y guarda.
     */
    public CourseEnrollmentDTO enrollStudent(Long id) {
        Course course = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        
        course.setEnrolledStudents(course.getEnrolledStudents() + 1);
        course = repository.save(course);
        
        BigDecimal finalPrice = calculateFinalPrice(course.getPrice(), course.getDiscount());
        
        return new CourseEnrollmentDTO(
            course.getId(),
            course.getEnrolledStudents(),
            finalPrice
        );
    }
    
    /**
     * Actualiza la calificación promedio.
     * Nota: En producción se usaría una tabla de ratings separada.
     */
    public CourseDetailDTO updateRating(Long id, Double newRating) {
        if (newRating < 0.0 || newRating > 5.0) {
            throw new IllegalArgumentException("Rating debe estar entre 0 y 5");
        }
        
        Course course = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        
        // Cálculo simplificado (promedio simple)
        // En producción: (sum_all_ratings + newRating) / (total_ratings + 1)
        double currentRating = course.getAverageRating();
        double updatedRating = (currentRating + newRating) / 2;
        course.setAverageRating(Math.round(updatedRating * 10) / 10.0);
        
        course = repository.save(course);
        return toDetailDTO(course);
    }
    
    /**
     * Genera estadísticas agregadas de todos los cursos.
     * Combina queries de repositorio y cálculos en memoria.
     */
    public CourseStatsDTO getStatistics() {
        Long total = repository.count();
        Integer totalStudents = repository.findTotalEnrolledStudents();
        Double avgRating = repository.findAverageRatingGlobal();
        BigDecimal avgPrice = repository.findAveragePriceGlobal();
        
        // Contar cursos por nivel
        Map<String, Long> byLevel = new HashMap<>();
        for (Level level : Level.values()) {
            byLevel.put(level.name(), repository.countByLevel(level));
        }
        
        // Obtener curso mejor valorado
        List<Course> topRated = repository.findTop5ByOrderByAverageRatingDesc();
        CourseCardDTO topCourse = topRated.isEmpty() ? null : toCardDTO(topRated.get(0));
        
        return new CourseStatsDTO(
            total,
            totalStudents != null ? totalStudents : 0,
            avgRating != null ? avgRating : 0.0,
            avgPrice != null ? avgPrice : BigDecimal.ZERO,
            byLevel,
            topCourse
        );
    }
    
    /**
     * Filtra cursos por rango de precio final (calculado).
     * Nota: finalPrice no está en DB, filtrado en memoria.
     */
    public List<CourseCardDTO> getCoursesByPriceRange(BigDecimal min, BigDecimal max) {
        return repository.findAll().stream()
            .filter(course -> {
                BigDecimal fp = calculateFinalPrice(course.getPrice(), course.getDiscount());
                return fp.compareTo(min) >= 0 && fp.compareTo(max) <= 0;
            })
            .map(this::toCardDTO)
            .toList();
    }
    
    // ========== DTO Conversions ==========
    
    private CourseCardDTO toCardDTO(Course course) {
        return new CourseCardDTO(
            course.getId(),
            course.getTitle(),
            course.getInstructor(),
            course.getDurationHours(),
            course.getLevel().name(),
            calculateFinalPrice(course.getPrice(), course.getDiscount()),
            course.getThumbnail(),
            course.getEnrolledStudents(),
            course.getAverageRating()
        );
    }
    
    private CourseDetailDTO toDetailDTO(Course course) {
        return new CourseDetailDTO(
            course.getId(),
            course.getTitle(),
            course.getDescription(),
            course.getInstructor(),
            course.getDurationHours(),
            course.getLevel().name(),
            course.getPrice(),
            course.getDiscount(),
            calculateFinalPrice(course.getPrice(), course.getDiscount()),
            course.getCategory(),
            course.getVideoUrl(),
            course.getThumbnail(),
            course.getPublishedDate(),
            course.getEnrolledStudents(),
            course.getAverageRating()
        );
    }
    
    /**
     * Calcula el precio final aplicando el descuento.
     * Formula: finalPrice = price - (price * discount / 100)
     * 
     * @param price Precio original
     * @param discount Porcentaje de descuento (0-100)
     * @return Precio con descuento aplicado
     */
    private BigDecimal calculateFinalPrice(BigDecimal price, Integer discount) {
        if (discount == null || discount == 0) {
            return price;
        }
        
        BigDecimal discountAmount = price
            .multiply(BigDecimal.valueOf(discount))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        return price.subtract(discountAmount);
    }
}
```

### 5️⃣ Controller Layer

#### CourseController.java (Extracto)
```java
@RestController
@RequestMapping("/api/courses")
public class CourseController {
    
    private final CourseService service;
    
    public CourseController(CourseService service) {
        this.service = service;
    }
    
    /**
     * Crear nuevo curso.
     * Validaciones automáticas con @Valid.
     */
    @PostMapping
    public ResponseEntity<CourseDetailDTO> createCourse(
            @Valid @RequestBody CourseCreateDTO dto) {
        CourseDetailDTO created = service.createCourse(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * Obtener estadísticas globales.
     * Incluye agregaciones y curso mejor valorado.
     */
    @GetMapping("/stats")
    public ResponseEntity<CourseStatsDTO> getStatistics() {
        return ResponseEntity.ok(service.getStatistics());
    }
    
    /**
     * Inscribir estudiante en un curso.
     * Incrementa contador de inscritos.
     */
    @PostMapping("/{id}/enroll")
    public ResponseEntity<CourseEnrollmentDTO> enrollStudent(@PathVariable Long id) {
        return ResponseEntity.ok(service.enrollStudent(id));
    }
    
    /**
     * Actualizar calificación de un curso.
     * Rating debe estar entre 0 y 5.
     */
    @PutMapping("/{id}/rating")
    public ResponseEntity<CourseDetailDTO> updateRating(
            @PathVariable Long id,
            @RequestParam Double rating) {
        return ResponseEntity.ok(service.updateRating(id, rating));
    }
    
    /**
     * Buscar cursos por rango de precio final.
     * min y max como query params.
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<CourseCardDTO>> getCoursesByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return ResponseEntity.ok(service.getCoursesByPriceRange(min, max));
    }
}
```

## 🎓 Patrones y Mejores Prácticas Aplicadas

### 1. Separation of Concerns
- **Controller:** Solo gestiona HTTP (requests/responses)
- **Service:** Lógica de negocio, validaciones, cálculos
- **Repository:** Acceso a datos, queries
- **Entity:** Modelo de datos

### 2. DTOs por Contexto
- **Card:** Vista compacta (listados)
- **Detail:** Vista completa (detalle individual)
- **Create:** Validaciones de entrada
- **Update:** Campos opcionales
- **Stats:** Datos agregados

### 3. Immutability con Records
```java
// DTOs inmutables, thread-safe, menos boilerplate
public record CourseCardDTO(...) {}
```

### 4. BigDecimal para Dinero
```java
// Precisión en cálculos monetarios
@Column(precision = 10, scale = 2)
private BigDecimal price;

// RoundingMode explícito
.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
```

### 5. Enums Tipados
```java
@Enumerated(EnumType.STRING)  // Legible en DB
private Level level;
```

### 6. Campos Calculados No Persistidos
```java
@Transient  // No se guarda en DB
private BigDecimal finalPrice;

// Se calcula en runtime
private BigDecimal calculateFinalPrice(...)
```

### 7. Constructor Injection
```java
// Preferible a @Autowired
public CourseService(CourseRepository repository) {
    this.repository = repository;
}
```

### 8. Validaciones Declarativas
```java
@NotBlank
@Size(max = 200)
String title;

// Activadas con @Valid en Controller
```

## 📊 Complejidad del Proyecto

### Nivel de DTOs: ⭐⭐⭐⭐ (Alto)
- 6 DTOs diferentes
- Nested DTO (CourseCardDTO dentro de CourseStatsDTO)
- Map<String, Long> en DTO
- Cálculos en mappings

### Lógica de Negocio: ⭐⭐⭐ (Medio)
- Cálculo de precios con descuento
- Actualización de ratings
- Generación de estadísticas agregadas
- Filtrado por precio calculado

### Queries: ⭐⭐⭐ (Medio)
- Query methods derivados
- @Query con JPQL
- Funciones agregadas (COUNT, AVG, SUM)
- Top N queries

## 🚀 Posibles Mejoras (Fuera del Alcance)

1. **Sistema de Reviews:** Tabla separada para ratings individuales
2. **Paginación:** PageRequest en listados
3. **Cache:** @Cacheable en estadísticas
4. **Auditoría:** @CreatedDate, @LastModifiedDate
5. **Testing:** Tests unitarios e integración (Proyecto 10+)
6. **Relaciones:** Instructor como entidad (Proyecto 11+)
7. **Search:** Full-text search en título/descripción
8. **File Upload:** Subir thumbnails reales

## 📚 Conceptos Reforzados

- ✅ DTOs especializados por caso de uso
- ✅ Separation of concerns estricta
- ✅ Inmutabilidad con Java Records
- ✅ BigDecimal para precisión monetaria
- ✅ Enums para valores fijos
- ✅ @Transient para campos calculados
- ✅ Queries agregadas con JPQL
- ✅ Bean Validation en DTOs
- ✅ Constructor injection
- ✅ ResponseEntity con status codes

---
**Proyecto Completado** ✅ | Complejidad DTOs: Alta | Sin Testing (P10+)