package com.payoyo.working.controller;


import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.payoyo.working.dtos.CourseCardDTO;
import com.payoyo.working.dtos.CourseCreateDTO;
import com.payoyo.working.dtos.CourseDetailDTO;
import com.payoyo.working.dtos.CourseEnrollmentDTO;
import com.payoyo.working.dtos.CourseStatsDTO;
import com.payoyo.working.dtos.CourseUpdateDTO;
import com.payoyo.working.service.CourseService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST para la gestión de cursos online.
 * 
 * Base path: /api/courses
 * 
 * Responsabilidades:
 * - Recibir requests HTTP
 * - Validar datos de entrada con @Valid
 * - Delegar lógica de negocio al Service
 * - Retornar respuestas HTTP apropiadas
 * 
 * @RestController = @Controller + @ResponseBody
 * Todas las respuestas se serializan automáticamente a JSON.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;

    /**
     * Inyección por constructor (recomendado sobre @Autowired).
     * Nota: Se inyecta la INTERFAZ, no la implementación.
     * Esto sigue el principio de Inversión de Dependencias (SOLID).
     */
    public CourseController(CourseService service) {
        this.service = service;
    }

    // ========== CRUD Endpoints ==========

    /**
     * Crea un nuevo curso.
     * 
     * Endpoint: POST /api/courses
     * Request Body: CourseCreateDTO (JSON)
     * Response: 201 CREATED con CourseDetailDTO
     * 
     * @Valid activa validaciones de Bean Validation del DTO.
     * Si falla validación, lanza MethodArgumentNotValidException
     * (manejada por GlobalExceptionHandler).
     * 
     * Ejemplo Request:
     * POST /api/courses
     * {
     *   "title": "Spring Boot Completo",
     *   "description": "Curso completo de Spring Boot...",
     *   "instructor": "Jose Luis",
     *   "durationHours": 45,
     *   "level": "INTERMEDIATE",
     *   "price": 89.99,
     *   "discount": 20,
     *   "category": "Backend"
     * }
     * 
     * Ejemplo Response (201):
     * {
     *   "id": 1,
     *   "title": "Spring Boot Completo",
     *   ...
     *   "finalPrice": 71.99
     * }
     */
    @PostMapping
    public ResponseEntity<CourseDetailDTO> createCourse(
            @Valid @RequestBody CourseCreateDTO dto) {
        
        CourseDetailDTO created = service.createCourse(dto);
        
        // 201 CREATED - Recurso creado exitosamente
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Obtiene todos los cursos en formato compacto.
     * 
     * Endpoint: GET /api/courses
     * Response: 200 OK con List<CourseCardDTO>
     * 
     * Ejemplo Response (200):
     * [
     *   {
     *     "id": 1,
     *     "title": "Spring Boot Completo",
     *     "instructor": "Jose Luis",
     *     "finalPrice": 71.99,
     *     ...
     *   },
     *   ...
     * ]
     */
    @GetMapping
    public ResponseEntity<List<CourseCardDTO>> getAllCourses() {
        List<CourseCardDTO> courses = service.getAllCourses();
        
        // 200 OK - Respuesta exitosa
        return ResponseEntity.ok(courses);
    }

    /**
     * Obtiene un curso específico por ID.
     * 
     * Endpoint: GET /api/courses/{id}
     * Path Variable: id (Long)
     * Response: 200 OK con CourseDetailDTO
     * 
     * @PathVariable extrae el ID de la URL.
     * Si el curso no existe, el Service lanza CourseNotFoundException
     * (manejada por GlobalExceptionHandler → 404).
     * 
     * Ejemplo: GET /api/courses/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDetailDTO> getCourseById(@PathVariable Long id) {
        CourseDetailDTO course = service.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    /**
     * Actualiza un curso existente (actualización parcial).
     * 
     * Endpoint: PUT /api/courses/{id}
     * Path Variable: id (Long)
     * Request Body: CourseUpdateDTO (JSON, campos opcionales)
     * Response: 200 OK con CourseDetailDTO actualizado
     * 
     * Solo actualiza los campos proporcionados (no-null).
     * 
     * Ejemplo Request:
     * PUT /api/courses/1
     * {
     *   "price": 79.99,
     *   "discount": 30
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseDetailDTO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateDTO dto) {
        
        CourseDetailDTO updated = service.updateCourse(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Elimina un curso por ID.
     * 
     * Endpoint: DELETE /api/courses/{id}
     * Path Variable: id (Long)
     * Response: 204 NO CONTENT (sin body)
     * 
     * 204 NO CONTENT indica que la operación fue exitosa
     * pero no hay contenido que retornar.
     * 
     * Ejemplo: DELETE /api/courses/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        service.deleteCourse(id);
        
        // 204 NO CONTENT - Eliminado exitosamente, sin contenido
        return ResponseEntity.noContent().build();
    }

    // ========== Search & Filter Endpoints ==========

    /**
     * Busca cursos por categoría.
     * 
     * Endpoint: GET /api/courses/category/{category}
     * Path Variable: category (String)
     * Response: 200 OK con List<CourseCardDTO>
     * 
     * Ejemplo: GET /api/courses/category/Backend
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseCardDTO>> getCoursesByCategory(
            @PathVariable String category) {
        
        List<CourseCardDTO> courses = service.getCoursesByCategory(category);
        return ResponseEntity.ok(courses);
    }

    /**
     * Busca cursos por nivel de dificultad.
     * 
     * Endpoint: GET /api/courses/level/{level}
     * Path Variable: level (String: BEGINNER, INTERMEDIATE, ADVANCED)
     * Response: 200 OK con List<CourseCardDTO>
     * 
     * Si el nivel es inválido, el Service lanza IllegalArgumentException
     * (manejada por GlobalExceptionHandler → 400).
     * 
     * Ejemplo: GET /api/courses/level/INTERMEDIATE
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<List<CourseCardDTO>> getCoursesByLevel(
            @PathVariable String level) {
        
        List<CourseCardDTO> courses = service.getCoursesByLevel(level);
        return ResponseEntity.ok(courses);
    }

    /**
     * Busca cursos por instructor.
     * 
     * Endpoint: GET /api/courses/instructor/{instructor}
     * Path Variable: instructor (String)
     * Response: 200 OK con List<CourseCardDTO>
     * 
     * Ejemplo: GET /api/courses/instructor/Jose Luis
     * 
     * Nota: Los espacios en URL se codifican como %20:
     * GET /api/courses/instructor/Jose%20Luis
     */
    @GetMapping("/instructor/{instructor}")
    public ResponseEntity<List<CourseCardDTO>> getCoursesByInstructor(
            @PathVariable String instructor) {
        
        List<CourseCardDTO> courses = service.getCoursesByInstructor(instructor);
        return ResponseEntity.ok(courses);
    }

    /**
     * Busca cursos por rango de precio final (con descuento aplicado).
     * 
     * Endpoint: GET /api/courses/price-range?min={min}&max={max}
     * Query Params: min (BigDecimal), max (BigDecimal)
     * Response: 200 OK con List<CourseCardDTO>
     * 
     * @RequestParam extrae parámetros de query string.
     * 
     * Ejemplo: GET /api/courses/price-range?min=50.00&max=100.00
     * 
     * Retorna cursos con finalPrice entre 50 y 100 euros (inclusive).
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<CourseCardDTO>> getCoursesByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        
        List<CourseCardDTO> courses = service.getCoursesByPriceRange(min, max);
        return ResponseEntity.ok(courses);
    }

    // ========== Statistics & Special Endpoints ==========

    /**
     * Obtiene estadísticas agregadas de todos los cursos.
     * 
     * Endpoint: GET /api/courses/stats
     * Response: 200 OK con CourseStatsDTO
     * 
     * Incluye:
     * - Totales (cursos, estudiantes)
     * - Promedios (rating, precio)
     * - Cursos por nivel (Map)
     * - Curso mejor valorado (nested DTO)
     * 
     * Ejemplo Response (200):
     * {
     *   "totalCourses": 15,
     *   "totalEnrolledStudents": 1847,
     *   "averageRating": 4.3,
     *   "averagePrice": 67.45,
     *   "coursesByLevel": {
     *     "BEGINNER": 5,
     *     "INTERMEDIATE": 7,
     *     "ADVANCED": 3
     *   },
     *   "topRatedCourse": {
     *     "id": 3,
     *     "title": "Python para Data Science",
     *     ...
     *   }
     * }
     */
    @GetMapping("/stats")
    public ResponseEntity<CourseStatsDTO> getStatistics() {
        CourseStatsDTO stats = service.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtiene los 5 cursos mejor valorados.
     * 
     * Endpoint: GET /api/courses/top-rated
     * Response: 200 OK con List<CourseCardDTO> (máximo 5)
     * 
     * Ordenados por averageRating descendente.
     * 
     * Ejemplo: GET /api/courses/top-rated
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<CourseCardDTO>> getTopRatedCourses() {
        List<CourseCardDTO> courses = service.getTopRatedCourses();
        return ResponseEntity.ok(courses);
    }

    /**
     * Obtiene los 5 cursos más populares por estudiantes inscritos.
     * 
     * Endpoint: GET /api/courses/popular
     * Response: 200 OK con List<CourseCardDTO> (máximo 5)
     * 
     * Ordenados por enrolledStudents descendente.
     * 
     * Ejemplo: GET /api/courses/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<List<CourseCardDTO>> getPopularCourses() {
        List<CourseCardDTO> courses = service.getPopularCourses();
        return ResponseEntity.ok(courses);
    }

    /**
     * Inscribe un estudiante en un curso.
     * Incrementa el contador de enrolledStudents.
     * 
     * Endpoint: POST /api/courses/{id}/enroll
     * Path Variable: id (Long)
     * Request Body: Ninguno (body vacío)
     * Response: 200 OK con CourseEnrollmentDTO
     * 
     * Ejemplo Request: POST /api/courses/1/enroll
     * 
     * Ejemplo Response (200):
     * {
     *   "courseId": 1,
     *   "enrolledStudents": 128,
     *   "finalPrice": 71.99
     * }
     * 
     * Retorna el precio final para confirmar el pago al estudiante.
     */
    @PostMapping("/{id}/enroll")
    public ResponseEntity<CourseEnrollmentDTO> enrollStudent(@PathVariable Long id) {
        CourseEnrollmentDTO enrollment = service.enrollStudent(id);
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Actualiza la calificación promedio de un curso.
     * 
     * Endpoint: PUT /api/courses/{id}/rating?rating={rating}
     * Path Variable: id (Long)
     * Query Param: rating (Double, 0.0 a 5.0)
     * Response: 200 OK con CourseDetailDTO actualizado
     * 
     * @RequestParam extrae el rating del query string.
     * 
     * Ejemplo: PUT /api/courses/1/rating?rating=4.8
     * 
     * Si rating está fuera de rango, el Service lanza InvalidRatingException
     * (manejada por GlobalExceptionHandler → 400).
     */
    @PutMapping("/{id}/rating")
    public ResponseEntity<CourseDetailDTO> updateRating(
            @PathVariable Long id,
            @RequestParam Double rating) {
        
        CourseDetailDTO updated = service.updateRating(id, rating);
        return ResponseEntity.ok(updated);
    }
}