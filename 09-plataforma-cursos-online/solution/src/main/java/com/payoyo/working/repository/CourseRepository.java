package com.payoyo.working.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.payoyo.working.entity.Course;
import com.payoyo.working.entity.Level;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio para operaciones CRUD y consultas personalizadas sobre cursos.
 * Extiende JpaRepository para obtener métodos básicos (save, findById, findAll, delete, etc.).
 * 
 * Incluye:
 * - Query methods derivados (Spring genera SQL automáticamente)
 * - Top N queries para rankings
 * - Queries agregadas con JPQL para estadísticas
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // ========== BÚSQUEDAS SIMPLES (Query Methods Derivados) ==========

    /**
     * Busca todos los cursos de una categoría específica.
     * Spring genera automáticamente: SELECT * FROM courses WHERE category = ?
     * 
     * @param category Nombre de la categoría (ej: "Backend", "Frontend")
     * @return Lista de cursos en esa categoría
     */
    List<Course> findByCategory(String category);

    /**
     * Busca todos los cursos de un nivel específico.
     * Spring genera: SELECT * FROM courses WHERE level = ?
     * 
     * @param level Nivel del curso (BEGINNER, INTERMEDIATE, ADVANCED)
     * @return Lista de cursos de ese nivel
     */
    List<Course> findByLevel(Level level);

    /**
     * Busca todos los cursos impartidos por un instructor específico.
     * Spring genera: SELECT * FROM courses WHERE instructor = ?
     * 
     * @param instructor Nombre del instructor
     * @return Lista de cursos del instructor
     */
    List<Course> findByInstructor(String instructor);

    /**
     * Verifica si existe un curso con el título especificado.
     * Útil para validar unicidad antes de crear/actualizar.
     * 
     * @param title Título del curso a verificar
     * @return true si existe, false si no
     */
    boolean existsByTitle(String title);

    // ========== TOP RANKINGS (Limitados a Top 5) ==========

    /**
     * Obtiene los 5 cursos con mejor calificación promedio.
     * Spring genera: SELECT * FROM courses ORDER BY average_rating DESC LIMIT 5
     * 
     * Uso típico: Sección "Cursos Mejor Valorados" en homepage
     * 
     * @return Lista de máximo 5 cursos ordenados por rating descendente
     */
    List<Course> findTop5ByOrderByAverageRatingDesc();

    /**
     * Obtiene los 5 cursos más populares por número de estudiantes inscritos.
     * Spring genera: SELECT * FROM courses ORDER BY enrolled_students DESC LIMIT 5
     * 
     * Uso típico: Sección "Cursos Más Populares"
     * 
     * @return Lista de máximo 5 cursos ordenados por inscritos descendente
     */
    List<Course> findTop5ByOrderByEnrolledStudentsDesc();

    /**
     * Obtiene los cursos más recientes ordenados por fecha de publicación.
     * Spring genera: SELECT * FROM courses ORDER BY published_date DESC LIMIT 10
     * 
     * @return Lista de máximo 10 cursos ordenados por fecha descendente
     */
    List<Course> findTop10ByOrderByPublishedDateDesc();

    // ========== QUERIES AGREGADAS PARA ESTADÍSTICAS ==========

    /**
     * Cuenta cuántos cursos existen de un nivel específico.
     * Se usa JPQL (Java Persistence Query Language) en lugar de SQL nativo.
     * 
     * @param level Nivel a contar (BEGINNER, INTERMEDIATE, ADVANCED)
     * @return Número de cursos de ese nivel
     * 
     * Uso: Generar Map<String, Long> en CourseStatsDTO
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.level = :level")
    Long countByLevel(@Param("level") Level level);

    /**
     * Calcula la calificación promedio de TODOS los cursos de la plataforma.
     * Función agregada AVG() sobre el campo averageRating.
     * 
     * @return Rating promedio global (puede ser null si no hay cursos)
     * 
     * Nota: Retorna Double, no double, para manejar caso sin datos (null)
     */
    @Query("SELECT AVG(c.averageRating) FROM Course c")
    Double findAverageRatingGlobal();

    /**
     * Calcula el precio promedio de todos los cursos.
     * 
     * @return Precio promedio global (puede ser null si no hay cursos)
     * 
     * Nota: Se calcula sobre precio base, no sobre precio final con descuento
     */
    @Query("SELECT AVG(c.price) FROM Course c")
    BigDecimal findAveragePriceGlobal();

    /**
     * Suma el total de estudiantes inscritos en TODOS los cursos.
     * Útil para estadísticas de alcance de la plataforma.
     * 
     * @return Número total de inscripciones (puede ser null si no hay cursos)
     * 
     * Ejemplo: Si hay 3 cursos con 50, 100 y 75 estudiantes → retorna 225
     */
    @Query("SELECT SUM(c.enrolledStudents) FROM Course c")
    Integer findTotalEnrolledStudents();

    /**
     * Calcula el total recaudado si todos los estudiantes pagaran precio completo.
     * Multiplica precio base por estudiantes inscritos y suma todo.
     * 
     * @return Ingresos totales estimados (sin considerar descuentos)
     * 
     * Nota: Esta es una estimación. En producción se haría seguimiento
     * de pagos reales en tabla separada.
     */
    @Query("SELECT SUM(c.price * c.enrolledStudents) FROM Course c")
    BigDecimal calculateTotalRevenue();

    // ========== BÚSQUEDAS COMBINADAS ==========

    /**
     * Busca cursos por categoría Y nivel específico.
     * Spring genera: SELECT * FROM courses WHERE category = ? AND level = ?
     * 
     * @param category Categoría del curso
     * @param level Nivel del curso
     * @return Lista de cursos que cumplen ambos criterios
     * 
     * Uso típico: Filtros combinados en frontend
     */
    List<Course> findByCategoryAndLevel(String category, Level level);

    /**
     * Busca cursos que tengan calificación mayor o igual al umbral.
     * Spring genera: SELECT * FROM courses WHERE average_rating >= ?
     * 
     * @param minRating Calificación mínima requerida
     * @return Lista de cursos con rating >= minRating
     * 
     * Uso típico: Filtro "Solo cursos 4+ estrellas"
     */
    List<Course> findByAverageRatingGreaterThanEqual(Double minRating);

    /**
     * Busca cursos con duración en un rango específico de horas.
     * Spring genera: SELECT * FROM courses WHERE duration_hours BETWEEN ? AND ?
     * 
     * @param minHours Duración mínima en horas
     * @param maxHours Duración máxima en horas
     * @return Lista de cursos en ese rango de duración
     * 
     * Uso típico: Filtro "Cursos cortos (< 10h)" o "Cursos largos (> 50h)"
     */
    List<Course> findByDurationHoursBetween(Integer minHours, Integer maxHours);

    // ========== NOTA SOBRE PRECIO FINAL ==========
    
    /*
     * NO hay query para buscar por precio final (finalPrice) porque:
     * 
     * 1. finalPrice NO está persistido en la base de datos
     * 2. Se calcula en runtime: price - (price * discount / 100)
     * 3. La búsqueda por rango de precio final se hará en Service:
     *    - Obtener todos los cursos
     *    - Calcular finalPrice para cada uno
     *    - Filtrar en memoria con Stream
     * 
     * En producción con millones de cursos, se consideraría:
     * - Persistir finalPrice como campo calculado (denormalización)
     * - Usar índices en DB
     * - O calcular en query con SQL nativo
     */
}