package com.payoyo.to_do_list.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payoyo.to_do_list.entity.Task;
import com.payoyo.to_do_list.entity.enums.Priority;
import com.payoyo.to_do_list.entity.enums.Status;
import com.payoyo.to_do_list.exceptions.TaskNotFoundException;
import com.payoyo.to_do_list.repository.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * Implementacion del servicio de tareas
 * 
 * Esta clase contiene toda la lógica de negocio de la aplicacion
 * 
 * RESPONSABILIDADES:
 * - Validaciones de negocio (más allá de las validaciones de formato)
 * - Orquestación de operaciones
 * - Transformacion de datos si es necesario
 * - Manejo de excepciones de negocio
 * - Logging de operaciones importantes
 * 
 * PRINCIPIOS APLICADOS:
 * - Single Responsibility: Solo maneja logica de negocio de las tareas
 * - Dependency Inversion: Depende de abstracciones (interfaces)
 * - Open/Closed: Abierto a extension, cerrado a modificacion
 * 
 * @Service: Marca esta clase como un componente del servicio de Spring, que lo registra en el contexto y permite la inyeccion de dependencias
 * 
 * @Transactional(readOnly = true): Configuracion de transacciones por defecto ->
 * - Toda las operaciones son read-only por defecto
 * - Optimiza el rendimiento (no bloquea la BD innecesariamente)
 * - Los metodos que modifican datos sobreescriben @Transactional
 * 
 * @RequiredArgsConstructor: Lombok genera constructor con campos al final. Esto es inyeccion de dependencias por constructor (mejor práctica)
 * 
 * @Slf4j: Lombok genera automaticamente un logger (log). Permute hacer log.info(), log.error(), etc 
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService{

    /*Repositorio de tareas
     * 
     * final: No puede cambiar después de la construcción (inmutable)
     * Inyeccion por constructor
     * 
     * Por qué inyeccion por constructor?
     * 1. Inmutabilidad (final)
     * 2. Obligatorio (no puede ser null)
     * 3. Testeable (facil pasar mocks en tests)
     * 4. Recomendado por Spring
     */
    private final TaskRepository taskRepository;

    // ========== OPERACIONES CRUD ==========
    /*
     * Crea una nueva tarea en el sistama.
     * 
     * @Transactional: Sobreescribe el readOnly=true de la clase. Esta operacion MODIFICA datos, necesita transaccion de escritura
     * 
     * Flujo:
     * 1. Se recibe la tarea del controller (ya validada con @Valid)
     * 2. Se registra en el log (trazabilidad)
     * 3. Se persiste en la base de datos
     * 4. Se registra el exito
     * 5. Se retorna la tarea guardada (con ID generado)
     * 
     * IMPORTANTE:
     * - El ID se genera automaticamente por la BD
     * - Los timestamps (createdAt, updatedAt) se asignan automaticamente
     * - Los valores por defecto (@Builder.Default) ya están aplicados
     * 
     * @param task -> Tarea a crear (sin ID)
     * @return Tarea guardada con ID asignado
     */
    @Override
    @Transactional
    public Task createTask(Task task) {
        log.info("Creando nueva tarea: {}", task.getId());

        Task taskSaved = taskRepository.save(task);

        log.info("Tarea creada exitosamente con ID: {}", taskSaved.getId());
        return taskSaved;
    }

    /*
     * Obtiene una tarea por su ID
     * 
     * FLUJO:
     * 1. Se busca la tarea en el repositorio
     * 2. Si exite se retorna
     * 3. Si NO existe se lanza la excepcion TaskNotFound
     * 
     * Optional.orElseThrow():
     * - Si findById encuentra la tarea -> la devuelve
     * - Si NO la encuentra -> ejecuta lambda y lanza excepcion
     * 
     * El @RestControllerAdvice capturará la excepcion y generará una respuesta HTTP 404 con el mensaje apropiado 
     * 
     * @param id -> ID de la tarea
     * @return Tarea encontrada
     * @Throws TaskNotFoundException si no existe
     */
    @Override
    public Task getTaskById(Long id) {
        log.debug("Buscando tarea con ID: {}", id);
        return taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
    }

    /*
     * Obtiene todas las tareas del sistema
     * 
     * ADVERTENCIA: En sistemas con muchos datos, esto puede ser infeciente
     * 
     * Alternativas recomendadas para produccion:
     * 1. Paginacion: Page<Task> findAll(Pageable pageable)
     * 2. Filtros: Solo devolver tareas relevantes
     * 3. Limite: Solo las ultimas N tareas
     * 
     * Para este proyecto, esta bien devovler todas ya que no habrá muchas
     * 
     * @return Lista de todas las tareas
     */
    @Override
    public List<Task> getAllTasks() {
        log.debug("Obteniendo todas las tareas");
        return taskRepository.findAll();
    }

    /*
     * Actualiza una tarea existente
     * 
     * @Transactional: Necesaria porque modifica los datos
     * 
     * Flujo:
     * 1. Busca la tarea existente (lanza la excepcion si no existe)
     * 2. Actualiza SOLO los campos modificables
     * 3. Persiste los cambios (save)
     * 4. Retorna la tarea actualizada
     * 
     * IMPORTANTE
     * - El ID NO se actualiza (es inmutable)
     * - createdAt NO se actualiza (es inmutable)
     * - updatedAt se actualiza automaticamente (@UptadeTimestamp)
     * 
     * Campos actualizables:
     * - title, description, status, priority, dueDate
     * 
     * @param id -> ID de la tarea a actualizar
     * @param task -> Nuevos datos de la tarea
     * @return Tarea actualizada
     * @throws TaskNotFoundException si no existe
     */
    @Override
    @Transactional
    public Task updateTask(Long id, Task task) {
        log.info("Actualizando la tarea con ID: {}", id);

        //1. Buscar la tarea existente
        Task existingTask = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        //2. Actualizar campos modificables
        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setPriority(task.getPriority());
        existingTask.setDueDate(task.getDueDate());

        /*
         * 3. Persistir cambios
         * JPA detecta los cambios automaticamente (dirty checking)
         * pero es buena practica hacer save() explicito
         */
        Task updatedTask = taskRepository.save(existingTask);

        log.info("Tarea actualizada exitosamente");
        return updatedTask;
    }

    /*
     * Elimin una tarea por su ID
     * 
     * @Transactional: Necesaria poeque modifica datos
     * 
     * FLujo:
     * 1. Verifica que la tarea existe
     * 2. Si no existe, lanza una excepcion
     * 3. Si existe la elimina
     * 
     * Alternativa (más eficiente):
     * taskRepository.deleteById(id);
     * Pero NO verifica existencia, falla silenciosamente si no existe
     * 
     * Nuestra implementacion es más robusta:
     * - Valida existencia primero
     * - Lanza excepcion clara si no existe
     * - Mejor experiencia para el usuario
     * 
     * @param is -> ID de la tarea a eliminar
     * @throws TaskNotFoundExcepction si no existe
     */
    @Override
    @Transactional
    public void deleteTask(Long id) {
        log.info("Eliminando tarea con ID: {}", id);

        // verificar que existe
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        
        //eliminar
        taskRepository.deleteById(id);

        log.info("Tarea eliminada exitosamente");
    }


    // ========== FILTROS POR ESTADO Y PRIORIDAD =========

    /*
     * Obtiene tareas filtradas por estado
     * 
     * Casos de uso típicos:
     * - Dashbboard con columnas por estado (Kanban)
     * - Vista de "tareas pendientes"
     * - Vista de "tareas completadas"
     * 
     * @param status -> Estado a filtrar
     * @return Lista de tareas con ese estado
     */
    @Override
    public List<Task> getTasksByStatus(Status status) {
        log.debug("Buscando tareas por estado: {}", status);
        return taskRepository.findByStatus(status);
    }

    /*
     * Obtiene tareas filtradas por prioridad
     * 
     * Casos de uso tipicos:
     * - Ver solo tareas de alta prioridad (urgentes)
     * - Organizar trabajo por importancia
     * 
     * @param priority -> Prioridad a filtrar
     * @return Lista de tareas con esa prioridad
     */
    @Override
    public List<Task> getTasksByPriority(Priority priority) {
        log.debug("Buscando tareas por prioridad: {}", priority);
        return taskRepository.findByPriority(priority);
    }

    /*
     * Obtiene las tareas con filtro combinado: estado y prioridad
     * 
     * Ejemplo de uso:
     * getTasksByStatusAndPriority(Status.PENDING, Priority.HIGH) -> Devuelve tareas pendientes de alta prioridad
     * 
     * Util para:
     * - Identificar lo más urgente e importante
     * - Priorizacion de trabajo
     * 
     * @param status -> Estado a filtrar
     * @param priority -> Prioridad a filtrar
     * @return Lista de tareas que cumplen ambos criterios 
     */
    @Override
    public List<Task> getTasksByStatusAndPriority(Status status, Priority priority) {
        log.debug("Buscando tareas por estado: {} y prioridad: {}", status, priority);
        return taskRepository.findByStatusAndPriority(status, priority);
    }


    // ========== BÚSQUEDA POR FECHAS ==========

    /*
     * Busca tareas cuya fecha limite esta en un rango
     * 
     * Validacion de negocio:
     * - startDate no puede ser posterior a endDate
     * - Si lo es, lanza IllegalArgumentException
     * 
     * Ejemplo de uso:
     * LocalDate hoy = LocalDate.now();
     * LocalDate finSemana = hoy.plusDays(7);
     * getTasksByDueDateRange(hoy, finSemana);
     * -> Devuelve tareas que vencen esta semana
     * 
     * @param startDate -> Fecha inicial (inclusiva)
     * @param endDate -> Fecha final (inclusiva)
     * @return Lista de tareas en ese rango
     * @throws IllegalArgumentException si el rango es inválido
     */
    @Override
    public List<Task> getTasksByDueDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Buscando tareas con fecha limite entre {} y {}", startDate, endDate);

        // validacion de negocio
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final");
        }

        return taskRepository.findByDueDateBetween(startDate, endDate);
    }

    /*
     * Obtiene todas las tareas vencidas (overDue)
     * 
     * Definicion de "vencida":
     * - Su fecha limite ya paso (dueDate < hoy)
     * - NO está completada (status != COMPLETED)
     * 
     * Esta logica esta implementada en el repositorio con @Query
     * 
     * Casos de uso:
     * - Dashboard de alertas
     * - Notificaciones de tareas atrasadas
     * - Reportes de productividad
     * 
     * @return Lista de tareas vencidas
     */
    @Override
    public List<Task> getOverdueTasks() {
        log.debug("Buscando tareas vencidas");
        return taskRepository.findOverdueTasks(LocalDate.now());
    }

    /*
     * Obtiene tareas que vencen HOY
     * 
     * Solo incluye tareas no completadas
     * 
     * Casos de uso:
     * - DashBoard de "Tareas del dia"
     * -Notificaciones matutianas
     * - Vista de trabajo diario
     * 
     * @return Lista de tareas que vencen hoy
     */
    @Override
    public List<Task> getTasksDueToday() {
        log.debug("Buscando tareas que vencen hoy");
        return taskRepository.findTasksDueToday(LocalDate.now());
    }


    // ========== BÚSQUEDA POR TEXTO ==========

    /*
     * Busca tareas por termino de busqueda
     * 
     * Busca en: 
     * - Titulo de la tarea
     * - Descripcion de la tarea
     * 
     * Características:
     * - Case-insensitive (no distingue mayus/minus)
     * - Busqueda parcial (encuentra "Spring" en "Spring Boot")
     * 
     * Validacion:
     * - Si el termino esta vacio o es null, lanzamos excepcion
     * 
     * @param searchTerm -> Termino a buscar
     * @return Lista de tareas que contiene ese termino
     * @throws IllegalArgumentException si el termino esta vaico
     */
    @Override
    public List<Task> searchTasks(String searchTerm) {
        log.debug("Buscando tareas con termino: {}", searchTerm);

        // validacion
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("El termino no puede estar vacio");
        }

        return taskRepository.searchByTitleOrDescription(searchTerm);
    }


    // ========== CAMBIOS DE ESTADO ==========

    /*
     * Marca una tarea como "En Progreso"
     * 
     * @Transactional: porque modifica datos
     * 
     * Logica de negocio:
     * - Busca la tarea
     * Cambia su estado a IN_PROGRESS usando el metodo de negocio de la entidad
     * - Persiste el cambio
     * 
     * NOTA: La lógica de cambio de estado esta en la entidad (markAsInPregress) siguiendo Domain-Driven Design (DDD)
     * 
     * Podriamos agregar validaciones adicionales aqui, por ejemplo:
     * - Solo permitir si el estado actual es PENDING
     * - Registrar quien cambio el estado
     * - Enviar notificacion
     * 
     * @param id -> ID de la tarea
     * @return Tarea con estado actualizado
     * @throws TaskNotFoundException si no existe
     */
    @Override
    @Transactional
    public Task markTaskAsInProgress(Long id) {
        log.info("Marcando tarea {} como en progreso", id);

        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        // metodo de negocio de la entidad
        task.markAsInProgress();

        Task updatedTask = taskRepository.save(task);

        log.info("Tarea marca como en progreso exitosamente");
        return updatedTask;
    }

    /*
     * Marcar la tarea como completada
     * 
     * @Transactional ya que modifica los datos
     * 
     * Similar a la anterior pero cambia a COMPLETED
     * 
     * Extentiones posibles:
     *  - Registrar fecha/hora de completado
     *  - Calcular tiempo que tomo completar la tarea
     * - Enviar notificacion de tarea completada
     * - Actualizar estadisticas del usuario
     * 
     * @param id -> ID de la tarea
     * @return Tares con estado COMPLETED
     * @throws TaskNotFoundException si no existe
     */
    @Override
    @Transactional
    public Task markTaskAsCompleted(Long id) {
        log.info("Marcando tarea {} como completada", id);

        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        // metodo de negocio de la entidad
        task.markAsCompleted();

        Task updatedTask = taskRepository.save(task);

        log.info("Tarea marcada como completada exitosamente");
        return updatedTask;

    }


    // ========== ESTADÍSTICAS ==========

    /*
     * Cuenta tareas por estado
     * 
     * Util para:
     * - Dashboard con contadores
     * - Gráficos de progreso
     * - Métricas de productividad
     * 
     * Ejemplo:
     * Long pendientes = countByStatus(Status.PENDING);
     * Long completadas = countByStatus(Status.COMPLETED);
     * double porcentaje = (completadas * 100.0) / (pendientes + completadas);
     * 
     * @param status -> Estado a contar
     * @return Numero de tareas con ese estado
     */
    @Override
    public Long countByStatus(Status status) {
        log.debug("Contando tareas por estado: {}", status);
        return taskRepository.countByStatus(status);
    }
    
}
