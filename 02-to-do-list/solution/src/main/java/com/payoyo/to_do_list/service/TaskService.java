package com.payoyo.to_do_list.service;

import java.time.LocalDate;
import java.util.List;

import com.payoyo.to_do_list.entity.Task;
import com.payoyo.to_do_list.entity.enums.Priority;
import com.payoyo.to_do_list.entity.enums.Status;
import com.payoyo.to_do_list.exceptions.TaskNotFoundException;

/*
 * Interfaz del servicio de tareas
 * 
 * Define el contrato (metidos) que debe implementar el servicio de negocio
 * 
 * POR QUÉ USAR UNA INTERFAZ?
 * 
 * VENTAJAS
 * 1. Abstraccion: El controlador depende de la intefaz, no de la implementacion
 * 2. Testabilidad: Facil crear mocks para testing
 * 3. Flexibilidad: Podemos cambiar la implementacion sin tocar el controlador
 * 4. Buena práctica empresarial: Muchas empresas siguen este patrin
 * 
 * DESVENTAJA:
 * - Añade una capa extra qu puede no ser necesaria en proyectos pequeños
 * 
 * NOTA: en SpringBoot moderno, muchos proyectos omiten la intefaz
 * y usan directamente la clase de implementacion con @Service
 * Ambos enfoques son validos
 * 
 * Patron aplicado -> TaskService(interfaz) -> TaskServiceImpl(implementacion) -> Repository
 */
public interface TaskService {

    // ========== OPERACIONES CRUD ==========
    /*
     * Crea una nueva tarea en el sistema
     * 
     * Proceso:
     * 1. Valida los datos (Spring lo hace automaticamente con @Valid)
     * 2. Asigna valores por defecto si es necesario
     * 3. Persiste en la base de datos
     * 4. Retorna la tarea guardada con su ID generado
     * 
     * @param task -> Tarea a crear (sin ID)
     * @return Tarea creada con ID asignado
     */
    Task createTask(Task task);

    /*
     * Obtiene una tarea por su ID
     * 
     * @param id -> ID de la tarea a buscar
     * @return Tarea encontrada
     * @throws TaskNotFoundException si no existe la tarea con ese id
     */
    Task getTaskById(Long id);

    /*
     * Obtiene todas las tareas del sistema
     * 
     * NOTA: En sistemas grandes, hay que considerar
     * Paginacion en lugar de devolverlas todas
     * 
     * @return Lista de todas las tareas
     */
    List<Task> getAllTasks();

    /*
     * Actualiza una tarea existente
     * 
     * Proceso:
     * 1. Verifica que la tarea existe
     * 2. Actualiza los campos modificables
     * 3. Persiste los cambios
     * 4. Retorna la tarea actualizada
     * 
     * @param id -> ID de la tarea a actualizar
     * @param task Datos actualizados de la tarea
     * @return Tarea actualizada
     * @throws TaskNotFoundException si no existe la tarea con es ID
     */
    Task updateTask(Long id, Task task);

    /*
     * Elimina una tarea por su ID
     * 
     * @param id -> ID de la tarea a eliminar
     * @throws TaskNotFoundException si no existe la tarea con ese ID
     */
    void deleteTask(Long id);

    // ========== FILTROS POR ESTADO Y PRIORIDAD ==========
    /*
     * Obtiene todas las tareas con un estado especifico
     * 
     * Casos de uso:
     * - Ver todas las tareas pendientes
     * - Ver todas las tareas en progreso
     * - Ver todas las tareas completadas
     * 
     * @param status -> Estado a filtrar (PENDING, IN_PROGRESS, COMPLETED)
     * @return Lista de tareas con ese estado
     */
    List<Task> getTasksByStatus(Status status);

    /**
     * Obtiene todas las tareas con una prioridad específica.
     * 
     * Casos de uso:
     * - Ver tareas de alta prioridad para enfocarse en lo urgente
     * - Organizar trabajo por nivel de importancia
     * 
     * @param priority Prioridad a filtrar (LOW, MEDIUM, HIGH)
     * @return Lista de tareas con esa prioridad
     */
    List<Task> getTasksByPriority(Priority priority);

    /**
     * Obtiene tareas filtrando por estado Y prioridad simultáneamente.
     * 
     * Caso de uso:
     * - Buscar tareas pendientes de alta prioridad (lo más urgente)
     * - Buscar tareas en progreso de baja prioridad
     * 
     * @param status Estado a filtrar
     * @param priority Prioridad a filtrar
     * @return Lista de tareas que cumplen ambos criterios
     */
    List<Task> getTasksByStatusAndPriority(Status status, Priority priority);



    // ========== BÚSQUEDA POR FECHAS ==========
    
    /**
     * Obtiene tareas cuya fecha límite está dentro de un rango.
     * 
     * Casos de uso:
     * - Ver tareas que vencen esta semana
     * - Ver tareas que vencen este mes
     * - Planificar trabajo por periodo
     * 
     * @param startDate Fecha inicial del rango (inclusiva)
     * @param endDate Fecha final del rango (inclusiva)
     * @return Lista de tareas con fecha límite en ese rango
     */
    List<Task> getTasksByDueDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene todas las tareas vencidas.
     * 
     * Una tarea está vencida si:
     * - Su fecha límite ya pasó
     * - NO está completada
     * 
     * Caso de uso:
     * - Dashboard de tareas atrasadas
     * - Notificaciones de tareas vencidas
     * - Reportes de productividad
     * 
     * @return Lista de tareas vencidas
     */
    List<Task> getOverdueTasks();

    /**
     * Obtiene las tareas que vencen hoy.
     * 
     * Caso de uso:
     * - Dashboard de "tareas del día"
     * - Notificaciones diarias
     * - Vista de trabajo diario
     * 
     * @return Lista de tareas que vencen hoy (y no están completadas)
     */
    List<Task> getTasksDueToday();


    // ========== BÚSQUEDA POR TEXTO ==========
    
    /**
     * Busca tareas por término de búsqueda en título o descripción.
     * 
     * Búsqueda case-insensitive (no distingue mayúsculas/minúsculas).
     * 
     * Caso de uso:
     * - Barra de búsqueda en la interfaz
     * - Encontrar tareas relacionadas con un tema
     * 
     * Ejemplo:
     * searchTasks("spring") encuentra:
     * - "Estudiar Spring Boot"
     * - "Tarea sobre SPRING framework"
     * - "spring cleaning"
     * 
     * @param searchTerm Término a buscar
     * @return Lista de tareas que contienen el término
     */
    List<Task> searchTasks(String searchTerm);


    // ========== CAMBIOS DE ESTADO ==========
    
    /**
     * Cambia el estado de una tarea a "En Progreso".
     * 
     * Caso de uso:
     * - Usuario comienza a trabajar en una tarea pendiente
     * 
     * @param id ID de la tarea
     * @return Tarea con estado actualizado
     * @throws TaskNotFoundException si no existe la tarea
     */
    Task markTaskAsInProgress(Long id);

    /**
     * Marca una tarea como completada.
     * 
     * Caso de uso:
     * - Usuario termina una tarea
     * 
     * @param id ID de la tarea
     * @return Tarea con estado actualizado a COMPLETED
     * @throws TaskNotFoundException si no existe la tarea
     */
    Task markTaskAsCompleted(Long id);


    
    // ========== ESTADÍSTICAS (BONUS) ==========
    
    /**
     * Cuenta cuántas tareas hay con un estado específico.
     * 
     * Caso de uso:
     * - Dashboard con estadísticas
     * - Métricas de productividad
     * - Gráficos de progreso
     * 
     * @param status Estado a contar
     * @return Número de tareas con ese estado
     */
    Long countByStatus(Status status);
    
}
