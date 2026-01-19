# 📚 Proyecto 9 - Working Directory

## 🎯 Guía de Desarrollo

Este directorio contiene la estructura base para desarrollar el Proyecto 9. Sigue el orden indicado y construye clase por clase.

## 📋 Orden de Implementación

### 1️⃣ Entidad (entity/)
```
Course.java - Entidad principal con todos los campos
```

**Puntos Clave:**
- Usar `@Enumerated(EnumType.STRING)` para level
- BigDecimal para campos monetarios
- Validaciones con `@Column` (unique, nullable, length)
- No incluir finalPrice como campo persistido (se calcula)

### 2️⃣ DTOs (dto/)
```
CourseCardDTO.java       - Vista compacta (listados)
CourseDetailDTO.java     - Vista completa (detalle)
CourseCreateDTO.java     - Creación (con validaciones)
CourseUpdateDTO.java     - Actualización (opcionales)
CourseEnrollmentDTO.java - Inscripción de estudiante
CourseStatsDTO.java      - Estadísticas agregadas
```

**Puntos Clave:**
- Usar Java Records para inmutabilidad
- Validaciones con Bean Validation en CreateDTO
- CourseStatsDTO incluye Map<String, Long> para coursesByLevel
- finalPrice calculado en el mapeo, no en el DTO

### 3️⃣ Repository (repository/)
```
CourseRepository.java - Queries personalizadas
```

**Queries Necesarias:**
```java
List<Course> findByCategory(String category);
List<Course> findByLevel(Level level);
List<Course> findByInstructor(String instructor);
List<Course> findByFinalPriceCalculatedBetween(BigDecimal min, BigDecimal max);
List<Course> findTop5ByOrderByAverageRatingDesc();
List<Course> findTop5ByOrderByEnrolledStudentsDesc();
Long countByLevel(Level level);
@Query("SELECT AVG(c.averageRating) FROM Course c")
Double findAverageRatingGlobal();
@Query("SELECT AVG(c.price) FROM Course c")
BigDecimal findAveragePriceGlobal();
```

### 4️⃣ Service (service/)
```
CourseService.java - Lógica de negocio
```

**Métodos Requeridos:**
```java
// CRUD
CourseDetailDTO createCourse(CourseCreateDTO dto)
List<CourseCardDTO> getAllCourses()
CourseDetailDTO getCourseById(Long id)
CourseDetailDTO updateCourse(Long id, CourseUpdateDTO dto)
void deleteCourse(Long id)

// Búsquedas
List<CourseCardDTO> getCoursesByCategory(String category)
List<CourseCardDTO> getCoursesByLevel(String level)
List<CourseCardDTO> getCoursesByInstructor(String instructor)
List<CourseCardDTO> getCoursesByPriceRange(BigDecimal min, BigDecimal max)

// Especiales
CourseStatsDTO getStatistics()
List<CourseCardDTO> getTopRatedCourses()
List<CourseCardDTO> getPopularCourses()
CourseEnrollmentDTO enrollStudent(Long id)
CourseDetailDTO updateRating(Long id, Double rating)

// Conversiones privadas
private CourseCardDTO toCardDTO(Course course)
private CourseDetailDTO toDetailDTO(Course course)
private Course toEntity(CourseCreateDTO dto)
private BigDecimal calculateFinalPrice(BigDecimal price, Integer discount)
```

**Lógica Importante:**
```java
// Cálculo de precio final
BigDecimal finalPrice = price.subtract(
    price.multiply(BigDecimal.valueOf(discount))
         .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
);

// Actualización de rating (ejemplo simplificado)
// En producción usarías un sistema más robusto
course.setAverageRating((course.getAverageRating() + newRating) / 2);
```

### 5️⃣ Controller (controller/)
```
CourseController.java - Endpoints REST
```

**Endpoints Implementados:**
```java
POST   /api/courses                    - Crear curso
GET    /api/courses                    - Listar todos
GET    /api/courses/{id}               - Obtener por ID
PUT    /api/courses/{id}               - Actualizar curso
DELETE /api/courses/{id}               - Eliminar curso
GET    /api/courses/category/{category} - Por categoría
GET    /api/courses/level/{level}      - Por nivel
GET    /api/courses/instructor/{name}  - Por instructor
GET    /api/courses/price-range        - Por rango precio
GET    /api/courses/stats              - Estadísticas
GET    /api/courses/top-rated          - Top 5 rating
GET    /api/courses/popular            - Top 5 inscritos
POST   /api/courses/{id}/enroll        - Inscribir estudiante
PUT    /api/courses/{id}/rating        - Actualizar rating
```

## 📬 Colección Postman

### 📁 Ubicación
```
working/postman/Project09_OnlineCourses.postman_collection.json
working/postman/examples/
├── create-course-springboot.json
├── create-course-react.json
├── create-course-python.json
├── update-course.json
└── enrollment-response.json
```

### 🔗 Ejemplos de Request Bodies

#### ➕ Crear Curso (POST /api/courses)
```json
{
  "title": "Spring Boot Completo - De Cero a Experto",
  "description": "Curso completo de Spring Boot donde aprenderás desde los fundamentos hasta arquitecturas avanzadas con microservicios.",
  "instructor": "Jose Luis Martinez",
  "durationHours": 45,
  "level": "INTERMEDIATE",
  "price": 89.99,
  "discount": 20,
  "category": "Backend",
  "videoUrl": "https://example.com/videos/springboot-intro.mp4",
  "thumbnail": "https://example.com/images/springboot-thumb.jpg",
  "publishedDate": "2024-01-15"
}
```

#### ✏️ Actualizar Curso (PUT /api/courses/{id})
```json
{
  "price": 79.99,
  "discount": 30,
  "description": "Curso ACTUALIZADO con nuevas secciones de Docker y Kubernetes"
}
```

#### 📝 Inscribir Estudiante (POST /api/courses/{id}/enroll)
```
(Body vacío, el ID va en la URL)
```

#### ⭐ Actualizar Rating (PUT /api/courses/{id}/rating?rating=4.8)
```
(Rating como query param, body vacío)
```

### ✅ Respuestas Esperadas

#### CourseDetailDTO (GET /api/courses/{id})
```json
{
  "id": 1,
  "title": "Spring Boot Completo - De Cero a Experto",
  "description": "Curso completo de Spring Boot...",
  "instructor": "Jose Luis Martinez",
  "durationHours": 45,
  "level": "INTERMEDIATE",
  "price": 89.99,
  "discount": 20,
  "finalPrice": 71.99,
  "category": "Backend",
  "videoUrl": "https://example.com/videos/springboot-intro.mp4",
  "thumbnail": "https://example.com/images/springboot-thumb.jpg",
  "publishedDate": "2024-01-15",
  "enrolledStudents": 127,
  "averageRating": 4.7
}
```

#### CourseStatsDTO (GET /api/courses/stats)
```json
{
  "totalCourses": 15,
  "totalEnrolledStudents": 1847,
  "averageRating": 4.3,
  "averagePrice": 67.45,
  "coursesByLevel": {
    "BEGINNER": 5,
    "INTERMEDIATE": 7,
    "ADVANCED": 3
  },
  "topRatedCourse": {
    "id": 3,
    "title": "Python para Data Science",
    "instructor": "Ana Garcia",
    "durationHours": 60,
    "level": "ADVANCED",
    "finalPrice": 99.99,
    "thumbnail": "https://example.com/images/python-ds.jpg",
    "enrolledStudents": 234,
    "averageRating": 4.9
  }
}
```

## 🎯 Checklist de Desarrollo

### Entity ✓
- [ ] Enum Level (BEGINNER, INTERMEDIATE, ADVANCED)
- [ ] Course entity con todos los campos
- [ ] Validaciones @Column
- [ ] Sin campo finalPrice persistido

### DTOs ✓
- [ ] CourseCardDTO (8 campos)
- [ ] CourseDetailDTO (todos los campos)
- [ ] CourseCreateDTO (validaciones)
- [ ] CourseUpdateDTO (opcionales)
- [ ] CourseEnrollmentDTO
- [ ] CourseStatsDTO (con Map y nested DTO)

### Repository ✓
- [ ] Queries by category, level, instructor
- [ ] Query por rango de precio final
- [ ] Top 5 by rating y by enrolled
- [ ] Queries agregadas (COUNT, AVG)

### Service ✓
- [ ] CRUD completo con conversiones DTO
- [ ] Búsquedas con filtros
- [ ] Cálculo de finalPrice
- [ ] Método enrollStudent (incrementar)
- [ ] Método updateRating
- [ ] Generación de CourseStatsDTO

### Controller ✓
- [ ] 14 endpoints REST
- [ ] Uso correcto de @PathVariable y @RequestParam
- [ ] DTOs de entrada y salida
- [ ] ResponseEntity con status codes

## 🧪 Pruebas Manuales

1. **Crear 5-10 cursos** con diferentes niveles y categorías
2. **Probar filtros** (por categoría, nivel, instructor, precio)
3. **Inscribir estudiantes** en varios cursos
4. **Actualizar ratings** para tener estadísticas realistas
5. **Verificar estadísticas** (/stats debe calcular correctamente)
6. **Comprobar cálculos** (finalPrice con diferentes descuentos)

## 📖 Recursos

- [Spring Data JPA Query Methods](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods)
- [Bean Validation Constraints](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-builtin-constraints)
- [BigDecimal Best Practices](https://www.baeldung.com/java-bigdecimal-biginteger)
- [Java Records](https://www.baeldung.com/java-record-keyword)

---
**Siguiente:** Implementa entity/Course.java y compártela para feedback