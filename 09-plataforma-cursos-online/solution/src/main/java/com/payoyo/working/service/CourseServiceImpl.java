package com.payoyo.working.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payoyo.working.dtos.*;
import com.payoyo.working.entity.Course;
import com.payoyo.working.entity.Level;
import com.payoyo.working.exceptions.*;
import com.payoyo.working.repository.CourseRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación de la interfaz CourseService.
 * Contiene la lógica de negocio para gestión de cursos.
 * 
 * Anotaciones:
 * @Service - Marca esta clase como componente de servicio de Spring
 * @Transactional - Las operaciones de escritura se ejecutan en transacciones
 */
@Service
@Transactional(readOnly = true) // Por defecto, operaciones de solo lectura
public class CourseServiceImpl implements CourseService {

    private final CourseRepository repository;

    /**
     * Inyección de dependencias por constructor (recomendado sobre @Autowired).
     * Facilita testing y hace explícitas las dependencias.
     */
    public CourseServiceImpl(CourseRepository repository) {
        this.repository = repository;
    }

    // ========== CRUD Operations ==========

    /**
     * Crea un nuevo curso validando que el título sea único.
     * 
     * @Transactional sin readOnly = transacción de escritura
     */
    @Override
    @Transactional
    public CourseDetailDTO createCourse(CourseCreateDTO dto) {
        // Validar que el título no esté duplicado
        if (repository.existsByTitle(dto.title())) {
            throw new DuplicateCourseException(dto.title());
        }

        // Convertir DTO a Entity
        Course course = toEntity(dto);

        // Establecer valores por defecto si no fueron proporcionados
        if (course.getDiscount() == null) {
            course.setDiscount(0);
        }
        if (course.getPublishedDate() == null) {
            course.setPublishedDate(LocalDate.now());
        }

        // Guardar en base de datos
        course = repository.save(course);

        // Convertir Entity a DTO de respuesta
        return toDetailDTO(course);
    }

    @Override
    public List<CourseCardDTO> getAllCourses() {
        return repository.findAll().stream()
                .map(this::toCardDTO)
                .toList();
    }

    @Override
    public CourseDetailDTO getCourseById(Long id) {
        Course course = findCourseByIdOrThrow(id);
        return toDetailDTO(course);
    }

    /**
     * Actualiza solo los campos proporcionados (actualización parcial).
     * Los campos null en el DTO no se actualizan.
     * 
     * @Transactional sin readOnly = transacción de escritura
     */
    @Override
    @Transactional
    public CourseDetailDTO updateCourse(Long id, CourseUpdateDTO dto) {
        Course course = findCourseByIdOrThrow(id);

        // Actualizar solo campos no-null (actualización parcial)
        if (dto.title() != null) {
            // Validar que el nuevo título no esté duplicado
            if (!dto.title().equals(course.getTitle()) && 
                repository.existsByTitle(dto.title())) {
                throw new DuplicateCourseException(dto.title());
            }
            course.setTitle(dto.title());
        }
        if (dto.description() != null) {
            course.setDescription(dto.description());
        }
        if (dto.instructor() != null) {
            course.setInstructor(dto.instructor());
        }
        if (dto.durationHours() != null) {
            course.setDurationHours(dto.durationHours());
        }
        if (dto.level() != null) {
            course.setLevel(dto.level());
        }
        if (dto.price() != null) {
            course.setPrice(dto.price());
        }
        if (dto.discount() != null) {
            validateDiscount(dto.discount());
            course.setDiscount(dto.discount());
        }
        if (dto.category() != null) {
            course.setCategory(dto.category());
        }
        if (dto.videoUrl() != null) {
            course.setVideoUrl(dto.videoUrl());
        }
        if (dto.thumbnail() != null) {
            course.setThumbnail(dto.thumbnail());
        }
        if (dto.publishedDate() != null) {
            course.setPublishedDate(dto.publishedDate());
        }

        // JPA detecta cambios y hace UPDATE automáticamente
        course = repository.save(course);

        return toDetailDTO(course);
    }

    /**
     * Elimina un curso por ID.
     * 
     * @Transactional sin readOnly = transacción de escritura
     */
    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = findCourseByIdOrThrow(id);
        repository.delete(course);
    }

    // ========== Search & Filters ==========

    @Override
    public List<CourseCardDTO> getCoursesByCategory(String category) {
        return repository.findByCategory(category).stream()
                .map(this::toCardDTO)
                .toList();
    }

    @Override
    public List<CourseCardDTO> getCoursesByLevel(String level) {
        // Convertir String a Enum Level
        Level levelEnum;
        try {
            levelEnum = Level.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Nivel inválido: " + level + ". Valores válidos: BEGINNER, INTERMEDIATE, ADVANCED"
            );
        }

        return repository.findByLevel(levelEnum).stream()
                .map(this::toCardDTO)
                .toList();
    }

    @Override
    public List<CourseCardDTO> getCoursesByInstructor(String instructor) {
        return repository.findByInstructor(instructor).stream()
                .map(this::toCardDTO)
                .toList();
    }

    /**
     * Filtra cursos por rango de precio FINAL (con descuento).
     * 
     * Nota: Como finalPrice no está en DB, se filtra en memoria:
     * 1. Obtener todos los cursos
     * 2. Calcular finalPrice para cada uno
     * 3. Filtrar con Stream API
     * 
     * En producción con millones de cursos, se consideraría:
     * - Persistir finalPrice como campo calculado
     * - Usar query SQL nativa con cálculo
     */
    @Override
    public List<CourseCardDTO> getCoursesByPriceRange(BigDecimal min, BigDecimal max) {
        return repository.findAll().stream()
                .filter(course -> {
                    BigDecimal finalPrice = calculateFinalPrice(
                        course.getPrice(), 
                        course.getDiscount()
                    );
                    // compareTo: -1 (menor), 0 (igual), 1 (mayor)
                    return finalPrice.compareTo(min) >= 0 && 
                           finalPrice.compareTo(max) <= 0;
                })
                .map(this::toCardDTO)
                .toList();
    }

    // ========== Statistics & Special Endpoints ==========

    /**
     * Genera estadísticas agregadas combinando múltiples queries.
     * 
     * Pasos:
     * 1. Obtener totales y promedios del repositorio
     * 2. Contar cursos por cada nivel (Map)
     * 3. Obtener curso mejor valorado
     * 4. Construir CourseStatsDTO
     */
    @Override
    public CourseStatsDTO getStatistics() {
        // Obtener estadísticas básicas con queries agregadas
        Long totalCourses = repository.count();
        Integer totalStudents = repository.findTotalEnrolledStudents();
        Double avgRating = repository.findAverageRatingGlobal();
        BigDecimal avgPrice = repository.findAveragePriceGlobal();

        // Contar cursos por nivel
        Map<String, Long> coursesByLevel = new HashMap<>();
        for (Level level : Level.values()) {
            Long count = repository.countByLevel(level);
            coursesByLevel.put(level.name(), count);
        }

        // Obtener curso mejor valorado
        List<Course> topRated = repository.findTop5ByOrderByAverageRatingDesc();
        CourseCardDTO topCourse = topRated.isEmpty() ? null : toCardDTO(topRated.get(0));

        // Construir DTO de estadísticas
        // Manejar nulls de queries (cuando no hay cursos)
        return new CourseStatsDTO(
            totalCourses,
            totalStudents != null ? totalStudents : 0,
            avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0, // Redondear a 1 decimal
            avgPrice != null ? avgPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
            coursesByLevel,
            topCourse
        );
    }

    @Override
    public List<CourseCardDTO> getTopRatedCourses() {
        return repository.findTop5ByOrderByAverageRatingDesc().stream()
                .map(this::toCardDTO)
                .toList();
    }

    @Override
    public List<CourseCardDTO> getPopularCourses() {
        return repository.findTop5ByOrderByEnrolledStudentsDesc().stream()
                .map(this::toCardDTO)
                .toList();
    }

    /**
     * Inscribe un estudiante incrementando el contador.
     * 
     * Operación atómica:
     * 1. Buscar curso
     * 2. Incrementar enrolledStudents
     * 3. Guardar (UPDATE en DB)
     * 
     * @Transactional asegura atomicidad
     */
    @Override
    @Transactional
    public CourseEnrollmentDTO enrollStudent(Long id) {
        Course course = findCourseByIdOrThrow(id);

        // Incrementar contador de estudiantes inscritos
        course.setEnrolledStudents(course.getEnrolledStudents() + 1);
        course = repository.save(course);

        // Calcular precio final para confirmar al estudiante
        BigDecimal finalPrice = calculateFinalPrice(
            course.getPrice(), 
            course.getDiscount()
        );

        return new CourseEnrollmentDTO(
            course.getId(),
            course.getEnrolledStudents(),
            finalPrice
        );
    }

    /**
     * Actualiza la calificación promedio del curso.
     * 
     * NOTA: Esta es una implementación SIMPLIFICADA para el proyecto.
     * En producción real:
     * - Tener tabla "ratings" separada con rating de cada usuario
     * - Calcular promedio real: SUM(ratings) / COUNT(ratings)
     * - Evitar que un usuario califique múltiples veces
     * 
     * Implementación actual (simplificada):
     * - Promedio simple entre rating actual y nuevo rating
     * - Suficiente para aprendizaje de conceptos
     */
    @Override
    @Transactional
    public CourseDetailDTO updateRating(Long id, Double newRating) {
        // Validar rango de rating (0.0 a 5.0)
        if (newRating < 0.0 || newRating > 5.0) {
            throw new InvalidRatingException(newRating);
        }

        Course course = findCourseByIdOrThrow(id);

        // Cálculo simplificado del nuevo promedio
        double currentRating = course.getAverageRating();
        double updatedRating = (currentRating + newRating) / 2;
        
        // Redondear a 1 decimal
        updatedRating = Math.round(updatedRating * 10.0) / 10.0;
        
        course.setAverageRating(updatedRating);
        course = repository.save(course);

        return toDetailDTO(course);
    }

    // ========== DTO Conversions (Private Helper Methods) ==========

    /**
     * Convierte Entity a CourseCardDTO (vista compacta).
     * Calcula finalPrice en runtime.
     */
    private CourseCardDTO toCardDTO(Course course) {
        return new CourseCardDTO(
            course.getId(),
            course.getTitle(),
            course.getInstructor(),
            course.getDurationHours(),
            course.getLevel().name(), // Enum a String
            calculateFinalPrice(course.getPrice(), course.getDiscount()),
            course.getThumbnail(),
            course.getEnrolledStudents(),
            course.getAverageRating()
        );
    }

    /**
     * Convierte Entity a CourseDetailDTO (vista completa).
     * Incluye precio original, descuento y precio final calculado.
     */
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
     * Convierte CourseCreateDTO a Entity.
     * No incluye ID (se genera automáticamente).
     * Valores por defecto se establecen en createCourse().
     */
    private Course toEntity(CourseCreateDTO dto) {
        Course course = new Course();
        course.setTitle(dto.title());
        course.setDescription(dto.description());
        course.setInstructor(dto.instructor());
        course.setDurationHours(dto.durationHours());
        course.setLevel(dto.level());
        course.setPrice(dto.price());
        course.setDiscount(dto.discount() != null ? dto.discount() : 0);
        course.setCategory(dto.category());
        course.setVideoUrl(dto.videoUrl());
        course.setThumbnail(dto.thumbnail());
        course.setPublishedDate(dto.publishedDate());
        
        // Valores por defecto
        course.setEnrolledStudents(0);
        course.setAverageRating(0.0);
        
        return course;
    }

    // ========== Business Logic (Private Helper Methods) ==========

    /**
     * Calcula el precio final aplicando el descuento.
     * 
     * Fórmula: finalPrice = price - (price * discount / 100)
     * 
     * Ejemplo:
     * - Precio: 100.00€
     * - Descuento: 20%
     * - Final: 100 - (100 * 20 / 100) = 100 - 20 = 80.00€
     * 
     * Uso de BigDecimal:
     * - Precisión exacta en cálculos monetarios
     * - RoundingMode.HALF_UP para redondeo bancario
     * - scale(2) = 2 decimales
     */
    private BigDecimal calculateFinalPrice(BigDecimal price, Integer discount) {
        if (discount == null || discount == 0) {
            return price.setScale(2, RoundingMode.HALF_UP);
        }

        // Calcular monto del descuento
        BigDecimal discountAmount = price
            .multiply(BigDecimal.valueOf(discount))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Restar descuento del precio original
        return price.subtract(discountAmount)
                   .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Busca un curso por ID o lanza excepción si no existe.
     * Método helper para evitar duplicación de código.
     * 
     * @throws CourseNotFoundException si el ID no existe
     */
    private Course findCourseByIdOrThrow(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new CourseNotFoundException(id));
    }

    /**
     * Valida que el descuento esté en rango válido (0-100).
     * 
     * @throws InvalidDiscountException si está fuera de rango
     */
    private void validateDiscount(Integer discount) {
        if (discount < 0 || discount > 100) {
            throw new InvalidDiscountException(discount);
        }
    }
}