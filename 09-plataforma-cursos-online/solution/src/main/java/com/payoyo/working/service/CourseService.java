package com.payoyo.working.service;

import java.math.BigDecimal;
import java.util.List;

import com.payoyo.working.dtos.*;

/**
 * Interfaz que define el contrato de servicios para la gestión de cursos.
 * 
 * Siguiendo el principio de Inversión de Dependencias (SOLID):
 * - El Controller depende de esta interfaz, no de la implementación
 * - Facilita testing con mocks
 * - Permite cambiar implementación sin afectar Controller
 * 
 * Responsabilidades:
 * - Lógica de negocio
 * - Conversiones Entity ↔ DTO
 * - Validaciones de negocio
 * - Coordinación entre Repository y Controller
 */
public interface CourseService {

    // ========== CRUD Operations ==========

    /**
     * Crea un nuevo curso en la plataforma.
     * Valida que el título no esté duplicado antes de crear.
     * 
     * @param dto Datos del curso a crear (validados con @Valid en Controller)
     * @return DTO con todos los detalles del curso creado
     * @throws DuplicateCourseException si el título ya existe
     */
    CourseDetailDTO createCourse(CourseCreateDTO dto);

    /**
     * Obtiene todos los cursos en formato compacto para listados.
     * 
     * @return Lista de cursos en formato CourseCardDTO
     */
    List<CourseCardDTO> getAllCourses();

    /**
     * Busca un curso específico por su ID.
     * 
     * @param id Identificador único del curso
     * @return DTO con detalles completos del curso
     * @throws CourseNotFoundException si el ID no existe
     */
    CourseDetailDTO getCourseById(Long id);

    /**
     * Actualiza un curso existente.
     * Solo actualiza los campos proporcionados (no-null).
     * 
     * @param id Identificador del curso a actualizar
     * @param dto Datos a actualizar (campos opcionales)
     * @return DTO con detalles actualizados del curso
     * @throws CourseNotFoundException si el ID no existe
     */
    CourseDetailDTO updateCourse(Long id, CourseUpdateDTO dto);

    /**
     * Elimina un curso de la plataforma.
     * 
     * @param id Identificador del curso a eliminar
     * @throws CourseNotFoundException si el ID no existe
     */
    void deleteCourse(Long id);

    // ========== Search & Filters ==========

    /**
     * Busca cursos por categoría.
     * 
     * @param category Nombre de la categoría (ej: "Backend", "Frontend")
     * @return Lista de cursos en esa categoría
     */
    List<CourseCardDTO> getCoursesByCategory(String category);

    /**
     * Busca cursos por nivel de dificultad.
     * 
     * @param level Nivel como String ("BEGINNER", "INTERMEDIATE", "ADVANCED")
     * @return Lista de cursos de ese nivel
     */
    List<CourseCardDTO> getCoursesByLevel(String level);

    /**
     * Busca cursos por instructor.
     * 
     * @param instructor Nombre del instructor
     * @return Lista de cursos impartidos por ese instructor
     */
    List<CourseCardDTO> getCoursesByInstructor(String instructor);

    /**
     * Busca cursos por rango de precio final (con descuento aplicado).
     * Nota: Requiere calcular finalPrice para cada curso (no persistido en DB).
     * 
     * @param min Precio mínimo (inclusive)
     * @param max Precio máximo (inclusive)
     * @return Lista de cursos en ese rango de precio
     */
    List<CourseCardDTO> getCoursesByPriceRange(BigDecimal min, BigDecimal max);

    // ========== Statistics & Special Endpoints ==========

    /**
     * Genera estadísticas agregadas de todos los cursos.
     * Incluye totales, promedios, agrupación por nivel y curso mejor valorado.
     * 
     * @return DTO con estadísticas completas de la plataforma
     */
    CourseStatsDTO getStatistics();

    /**
     * Obtiene los 5 cursos mejor valorados.
     * 
     * @return Lista de top 5 cursos ordenados por rating descendente
     */
    List<CourseCardDTO> getTopRatedCourses();

    /**
     * Obtiene los 5 cursos más populares por número de estudiantes inscritos.
     * 
     * @return Lista de top 5 cursos ordenados por inscritos descendente
     */
    List<CourseCardDTO> getPopularCourses();

    /**
     * Inscribe un estudiante en un curso.
     * Incrementa el contador de enrolledStudents.
     * 
     * @param id Identificador del curso
     * @return DTO con datos de inscripción (ID, nuevos inscritos, precio final)
     * @throws CourseNotFoundException si el ID no existe
     */
    CourseEnrollmentDTO enrollStudent(Long id);

    /**
     * Actualiza la calificación promedio de un curso.
     * 
     * @param id Identificador del curso
     * @param rating Nueva calificación (0.0 a 5.0)
     * @return DTO con detalles actualizados del curso
     * @throws CourseNotFoundException si el ID no existe
     * @throws InvalidRatingException si el rating está fuera de rango
     */
    CourseDetailDTO updateRating(Long id, Double rating);
}