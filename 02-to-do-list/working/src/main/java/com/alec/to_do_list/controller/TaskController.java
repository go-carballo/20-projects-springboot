package com.alec.to_do_list.controller;

import com.alec.to_do_list.entity.Task;
import com.alec.to_do_list.enums.Priority;
import com.alec.to_do_list.enums.Status;
import com.alec.to_do_list.exception.TaskNotFoundException;
import com.alec.to_do_list.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ==================== CRUD BÁSICO ====================

    /**
     * Crear una nueva tarea
     * POST /api/tasks
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    /**
     * Obtener todas las tareas
     * GET /api/tasks
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Obtener una tarea por ID
     * GET /api/tasks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return ResponseEntity.ok(task);
    }

    /**
     * Actualizar una tarea existente
     * PUT /api/tasks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @Valid @RequestBody Task task) {
        Task updatedTask = taskService.updateTask(id, task);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Eliminar una tarea
     * DELETE /api/tasks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== FILTROS ====================

    /**
     * Filtrar tareas por estado
     * GET /api/tasks/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable Status status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Filtrar tareas por prioridad
     * GET /api/tasks/priority/{priority}
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Task>> getTasksByPriority(@PathVariable Priority priority) {
        List<Task> tasks = taskService.getTasksByPriority(priority);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Filtrar tareas por estado Y prioridad (combinado)
     * GET /api/tasks/filter?status=PENDING&priority=HIGH
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Task>> getTasksByStatusAndPriority(
            @RequestParam Status status,
            @RequestParam Priority priority) {
        List<Task> tasks = taskService.getTasksByStatusAndPriority(status, priority);
        return ResponseEntity.ok(tasks);
    }

    // ==================== BÚSQUEDAS POR FECHAS ====================

    /**
     * Buscar tareas por rango de fechas
     * GET /api/tasks/due-date-range?startDate=2025-11-15&endDate=2025-11-22
     */
    @GetMapping("/due-date-range")
    public ResponseEntity<List<Task>> getTasksByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Task> tasks = taskService.getTasksByDateRange(startDate, endDate);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Obtener tareas vencidas
     * GET /api/tasks/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks() {
        List<Task> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Obtener tareas que vencen hoy
     * GET /api/tasks/due-today
     */
    @GetMapping("/due-today")
    public ResponseEntity<List<Task>> getTasksDueToday() {
        List<Task> tasks = taskService.getTasksDueToday();
        return ResponseEntity.ok(tasks);
    }

    // ==================== BÚSQUEDA POR TEXTO ====================

    /**
     * Buscar tareas por término en título o descripción
     * GET /api/tasks/search?term=documentar
     */
    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchTasksByTerm(@RequestParam String term) {
        List<Task> tasks = taskService.searchTasksByTerm(term);
        return ResponseEntity.ok(tasks);
    }

    // ==================== BONUS ENDPOINTS ====================

    /**
     * Contar tareas por estado (BONUS +3 pts)
     * GET /api/tasks/count/status/{status}
     */
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countTasksByStatus(@PathVariable Status status) {
        long count = taskService.countTasksByStatus(status);
        return ResponseEntity.ok(count);
    }

    /**
     * Marcar tarea como en progreso (BONUS +4 pts)
     * PATCH /api/tasks/{id}/in-progress
     */
    @PatchMapping("/{id}/in-progress")
    public ResponseEntity<Task> markTaskAsInProgress(@PathVariable Long id) {
        Task updatedTask = taskService.markTaskAsInProgress(id);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Marcar tarea como completada (BONUS +4 pts)
     * PATCH /api/tasks/{id}/complete
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<Task> markTaskAsCompleted(@PathVariable Long id) {
        Task updatedTask = taskService.markTaskAsCompleted(id);
        return ResponseEntity.ok(updatedTask);
    }
}