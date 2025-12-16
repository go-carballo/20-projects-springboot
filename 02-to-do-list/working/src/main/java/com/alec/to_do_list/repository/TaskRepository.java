package com.alec.to_do_list.repository;

import com.alec.to_do_list.entity.Task;
import com.alec.to_do_list.enums.Priority;
import com.alec.to_do_list.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Filtrar por estado
    List<Task> findByStatus(Status status);

    // Filtrar por prioridad
    List<Task> findByPriority(Priority priority);

    // Filtrar por estado Y prioridad (combinado)
    List<Task> findByStatusAndPriority(Status status, Priority priority);

    // Buscar tareas por rango de fechas (fecha límite entre fecha1 y fecha2)
    List<Task> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    // Obtener tareas vencidas (overdue: dueDate < hoy AND status != COMPLETED)
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDate currentDate);

    // Buscar tareas por término en título o descripción (case-insensitive)
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Task> findByTitleOrDescriptionContainingIgnoreCase(@Param("term") String term);

    // BONUS: Contar tareas por estado (+3 pts)
    long countByStatus(Status status);

    // BONUS: Obtener tareas que vencen hoy
    @Query("SELECT t FROM Task t WHERE t.dueDate = :today")
    List<Task> findTasksDueToday(@Param("today") LocalDate today);

    // Método adicional para obtener todas las tareas ordenadas por fecha de creación
    List<Task> findAllByOrderByCreatedAtDesc();

    // Método adicional para buscar por estado ordenado por prioridad
    List<Task> findByStatusOrderByPriorityDesc(Status status);
}