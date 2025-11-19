package com.payoyo.to_do_list.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.payoyo.to_do_list.entity.Task;

import java.time.LocalDate;
import java.util.List;
import com.payoyo.to_do_list.entity.enums.Status;
import com.payoyo.to_do_list.entity.enums.Priority;

/*
 * Repositorio para operaciones de base de datos sobre la entidad Task
 * 
 * Spring Data JPA:
 * - Extiende JpaRepository, que proporciona métodos CRUD básicos
 * - Los métodos personalizados se pueden definir por nomenclatura o con @Query
 * - No necesita implementación, Spring lo genera automáticamente en runtime
 * 
 * JpaRepository<Task, Long>:
 *  - Task: Tipo de entidad que maneja este repositorio
 *  - Long: Tipo de dato del ID de la entidad
 */
@Repository // Indica que es un componente de persitencia de Spring
public interface TaskRepository extends JpaRepository<Task, Long>{
    
    // ========== MÉTODOS HEREDADOS DE JpaRepository ==========
    // Ya disponibles sin necesidad de declararlos:
    // - save(Task task) -> Crear o actualizar
    // - findById(Long id) -> Buscar por ID
    // - findAll() -> Obtener todas las tareas
    // - deleteById(Long id) -> Eliminar por ID
    // - count() -> Contar registros
    // - existsById(Long id) -> Verificar si existe


    // ========== QUERY METHODS (Métodos por Nomenclatura) ==========
    /*
     * Busca todas las tareas con un estado especifico
     * 
     * Nomenclatrura Spring Data JPA:
     *  - "findBy" + "Status" -> WHERE status = ?
     * 
     * SQL generado:
     * SELECT * FROM tasks WHERE status = ?
     * 
     * @param status -> Estado a buscar (del enum)
     * @return Lista de tareas con ese estado
     */
    List<Task> findByStatus(Status status);

    /*
     * Busca todas las tareas con una proridad especifica
     * 
     * SQL generado:
     * SELECT * FROM tasks WHERE priority = ?
     * 
     * @param priority -> Prioridad a buscar
     * @return Lista de tareas con esa prioridad
     */
    List<Task> findByPriority(Priority priority);

    /*
     * Busca tareas filtrando por estado Y prioridad simultáneamente
     * 
     * Nomenclatura:
     *  - "And" une múltiples condiciones con el operador AND
     * 
     * SQL generado:
     * SELECT * FROM tasks WHERE status = ? AND priority = ?
     * 
     * Ejemplo de uso:
     * findByStatusAndPriority(Status.PENDING, Priority.HIGH)
     * -> Devuelve tareas pendientes y de alta prioridad
     * 
     * @param status -> Estado a buscar
     * @param priority -> Prioridad a buscar
     * @return Lista de tareas que cumplen ambas condiciones
     */
    List<Task> findByStatusAndPriority(Status status, Priority priority);


    // ========== CONSULTAS POR FECHAS ==========
    /*
     * Busca tareas cuya fecha limite este dentro de un rango
     * 
     * Nomenclatura:
     * - "Between" genera una condicion SQL BETWEEN
     * 
     * SQL generado:
     * SELECT * FROM tasks WHERE due_date BETWEEN ? AND ?
     * 
     * Caso de uso:
     * Ver todas las tareas que vencen esta semana
     * findByDueDateBetween(hoy, hoy.plusDays(7))
     * 
     * @param startDate -> Fecha inicial (inclusiva)
     * @param endDate -> Fecha inicial (inclusiva)
     * @return Lista de tareas en ese rango de fechas
     */
    List<Task> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    /*
     * Busca tareas con una fecha limite exacta
     * 
     * SQL generado:
     * SELECT * FROM tasks WHERE due_date = ?
     * 
     * Util para ver tareas que vencen en un dia en especifico
     * 
     * @param dueDate -> Fecha Limite especifica
     * @return Lista de tareas con esa fecha limite
     */
    List<Task> findByDueDate(LocalDate dueDate);



    // ========== CONSULTAS PERSONALIZADAS CON @Query ==========
    // Cuando la nomenclatura de métodos no es suficiente,
    // usamos @Query para escribir JPQL (similar a SQL pero orientado a objetos)
    /*
     * Encuentra todas las tareas vencidas (overdue)
     * 
     * Una tarea esta vencida si: 
     * 1. Su fecha limite ya paso (dueDate < hoy)
     * 2. No esta completada (status != COMPLETED)
     * 
     * @Query: Permite escribir consultas JPQL personalizadas
     * JPQL usa nombres de clase/atributos, no nombres de tabla/columna
     * 
     * Comparacion:
     * JQPL: SELECT t FROM Task t WHERE t.dueDate < :today
     * SQL: SELECT * FROM tasks WHERE due_date < ?
     * 
     * @Param("today"): Vincula el parámetro del método con :today en la query
     * 
     * @param today -> Fecha actual para comparar
     * @return Lista de tareas vencidas
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate < :today AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("today") LocalDate today);

    /*
     * Encuentra tareas que vencen hoy y aun no estan completas
     * 
     * Util para notificaciones diarias o dashboard de "tareas de hoy"
     * 
     * @param today -> Fecha actual
     * @return Lista de tareas que vencen hoy
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate = :today AND t.status != 'COMPLETED'")
    List<Task> findTasksDueToday(@Param("today") LocalDate today);

    /*
     * Busca tareas por termino de busqueda en titulo o descripcion
     * 
     * Caracteristicas: 
     * - LOWER(): Convierte a minusculas para busqueda case-insensitive
     * - CONCAT('%', :searchTerm, '%'): Agrega % antes y despues para LIKE
     * - OR: Busca un titulo o descripcion
     * 
     * Ejemplo:
     * searchByTitleOrDescription("spring")
     * -> encuentra tareas con "spring", "Spring", "SPRING" en titulo o descripcion
     * 
     * SQL equivalente:
     * SELECT * FROM tasks
     * WHERE LOWER(title) LIKE LOWER('%spring%')
     *      OR LOWER(description) LIKE LOWER('%spring%')
     * 
     * @param searchTerm -> Término a buscar
     * @return Lista de tareas que contienen el termino
     */
    @Query("SELECT t FROM Task t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Task> searchByTitleOrDescription(@Param("searchTerm") String searchTerm);

    /*
     * Cuenta cuántas tareas hay con un estado especifico
     * 
     * Nomenclatura:
     * - "countBy" + campo -> genera COUNT(*)
     * 
     * SQL generado:
     * SELECT COUNT(*) FROM Tasks WHERE status = ?
     * 
     * Util para estadisticas y dashboards:
     * - countByStatus(PENDING) -> Cuantas tareas pendientes hay?
     * - countByStatus(COMPLETED) -> Cuantas tareas completadas hay?
     * 
     * @param status -> Estado a contar
     * @return Numero de tareas con ese estado
     */
    Long countByStatus(Status status);


}
