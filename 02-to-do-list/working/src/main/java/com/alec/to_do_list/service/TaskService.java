package com.alec.to_do_list.service;

import com.alec.to_do_list.entity.Task;
import com.alec.to_do_list.enums.Priority;
import com.alec.to_do_list.enums.Status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskService {

    // CRUD Básico
    Task createTask(Task task);
    List<Task> getAllTasks();
    Optional<Task> getTaskById(Long id);
    Task updateTask(Long id, Task task);
    void deleteTask(Long id);

    // Filtros
    List<Task> getTasksByStatus(Status status);
    List<Task> getTasksByPriority(Priority priority);
    List<Task> getTasksByStatusAndPriority(Status status, Priority priority);

    // Búsquedas por fechas
    List<Task> getTasksByDateRange(LocalDate startDate, LocalDate endDate);
    List<Task> getOverdueTasks();
    List<Task> getTasksDueToday();

    // Búsqueda por texto
    List<Task> searchTasksByTerm(String term);

    // BONUS: Métodos adicionales
    long countTasksByStatus(Status status);
    Task markTaskAsInProgress(Long id);
    Task markTaskAsCompleted(Long id);
}