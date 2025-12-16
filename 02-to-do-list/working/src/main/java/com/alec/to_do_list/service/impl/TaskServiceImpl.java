package com.alec.to_do_list.service.impl;

import com.alec.to_do_list.entity.Task;
import com.alec.to_do_list.enums.Priority;
import com.alec.to_do_list.enums.Status;
import com.alec.to_do_list.exception.TaskNotFoundException;
import com.alec.to_do_list.repository.TaskRepository;
import com.alec.to_do_list.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    public Task createTask(Task task) {
        log.info("Creating new task with title: {}", task.getTitle());
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());
        return savedTask;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        log.info("Fetching all tasks");
        List<Task> tasks = taskRepository.findAllByOrderByCreatedAtDesc();
        log.info("Found {} tasks", tasks.size());
        return tasks;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id) {
        log.info("Fetching task by ID: {}", id);
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            log.info("Task found with ID: {}", id);
        } else {
            log.warn("Task not found with ID: {}", id);
        }
        return task;
    }

    @Override
    public Task updateTask(Long id, Task task) {
        log.info("Updating task with ID: {}", id);


        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setPriority(task.getPriority());
        existingTask.setDueDate(task.getDueDate());
        
        Task updatedTask = taskRepository.save(existingTask);
        log.info("Task updated successfully with ID: {}", updatedTask.getId());
        return updatedTask;
    }

    @Override
    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
        log.info("Task deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(Status status) {
        log.info("Fetching tasks by status: {}", status);
        List<Task> tasks = taskRepository.findByStatus(status);
        log.info("Found {} tasks with status: {}", tasks.size(), status);
        return tasks;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByPriority(Priority priority) {
        log.info("Fetching tasks by priority: {}", priority);
        List<Task> tasks = taskRepository.findByPriority(priority);
        log.info("Found {} tasks with priority: {}", tasks.size(), priority);
        return tasks;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByStatusAndPriority(Status status, Priority priority) {
        log.info("Fetching tasks by status: {} and priority: {}", status, priority);
        List<Task> tasks = taskRepository.findByStatusAndPriority(status, priority);
        log.info("Found {} tasks with status: {} and priority: {}", tasks.size(), status, priority);
        return tasks;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching tasks by date range: {} to {}", startDate, endDate);


        List<Task> tasks = taskRepository.findByDueDateBetween(startDate, endDate);

        log.info("Found {} tasks in date range: {} to {}", tasks.size(), startDate, endDate);
        return tasks;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getOverdueTasks() {
        log.info("Fetching overdue tasks");
        LocalDate currentDate = LocalDate.now();
        List<Task> tasks = taskRepository.findOverdueTasks(currentDate);
        log.info("Found {} overdue tasks", tasks.size());
        return tasks;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksDueToday() {
        log.info("Fetching tasks due today");
        LocalDate today = LocalDate.now();
        List<Task> tasks = taskRepository.findTasksDueToday(today);
        log.info("Found {} tasks due today", tasks.size());
        return tasks;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> searchTasksByTerm(String term) {
        log.info("Searching tasks by term: {}", term);
        List<Task> tasks = taskRepository.findByTitleOrDescriptionContainingIgnoreCase(term);
        log.info("Found {} tasks matching term: {}", tasks.size(), term);
        return tasks;
    }

    @Override
    @Transactional(readOnly = true)
    public long countTasksByStatus(Status status) {
        log.info("Counting tasks by status: {}", status);
        long count = taskRepository.countByStatus(status);
        log.info("Found {} tasks with status: {}", count, status);
        return count;
    }

    @Override
    public Task markTaskAsInProgress(Long id) {
        log.info("Marking task as IN_PROGRESS with ID: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        task.markAsInProgress();
        Task updatedTask = taskRepository.save(task);
        log.info("Task marked as IN_PROGRESS with ID: {}", updatedTask.getId());
        return updatedTask;
    }

    @Override
    public Task markTaskAsCompleted(Long id) {
        log.info("Marking task as COMPLETED with ID: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        
        task.markAsCompleted();
        Task updatedTask = taskRepository.save(task);
        log.info("Task marked as COMPLETED with ID: {}", updatedTask.getId());
        return updatedTask;
    }
}