package com.payoyo.to_do_list.controller;

import com.payoyo.to_do_list.service.TaskService;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.payoyo.to_do_list.entity.Task;
import com.payoyo.to_do_list.entity.enums.Priority;
import com.payoyo.to_do_list.entity.enums.Status;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
 * Controlador REST para la gestion de tareas
 * 
 * RESPONSABILIDADES:
 * - Exponer endpoints HTTP para operaciones CRUD
 * - Recibir y validar datos de entrada (JSON)
 * - Invocar la capa de servicio (lógica de negocio)
 * - Retornar respuestas HTTP apropiadas
 * 
 * PRINCIPIOS APLICADOS:
 * - REDTful API: Uso correcto de verbos HTTP y cpdigos de estado
 * - Single Responsibility: Solo maneja HTTP, no logica de negocio
 * - Thin Controller: Delega toda la logica al servicio
 * 
 * BUENAS PRÁCTICAS:
 * - Validacion con @Valid en @RequesBody
 * - Codigos HTTP semanticos (200, 201, 204, 404, etc)
 * - URIs descriptivas y consistentes
 * - Uso de ResponseEntity para el control total de la respuesta
 */

 /*
  * RESTCONTROLLER -> Combinacion de @Controller + @ResponseBody
        Todas las respuestas se serializa automaticamente a JSON
  */
  /*
 * REQUESTMAPPING("/api/tasks"): Prefiijo base para todos los endpoints. Todos los metodos heredan esta ruta base
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor // para inyeccion de dependencias
public class TaskController {

    /*
     * Servicio de tareas (logica de negocio)
     * 
     * Inyeccion por constructor
     * El controller NO debe tener logica de negocio, solo delega al SERVICE
     */
    private final TaskService taskService;

    // ========== OPERACIONES CRUD ==========

    /*
     * Crea una nueva tarea
     * 
     * Endpoint: POST /api/tasks
     * 
     * @PostMapping: Maneja peticiones HTTP POST. Se usa para crear recursos
     * 
     * @RequestBody: Indica que el parametro viene en el cuerpo de la peticion (JSON)
     * @Valid: Activa las validaciones Bean Validation en la entidad Task. Si las excepciones fallan , spring lanza 
     * MethodArgumentNotValidException, que es capturada por nuestro GlobalExceptionHandler
     * 
     * ResponseEntity<Task>: Permite controlar:
     * - Codigo de estado HTTP
     * - Headers
     * - Body (la tarea creada)
     * 
     * Flujo:
     * 1. Cliente envía JSON con datos de tarea
     * 2. Spring deserializa JSON -> objeto Task
     * 3. @Valid ejecuta validaciones (@NotBlank, @Size, etc.)
     * 4. Si pasa validación -> llama al service
     * 5. Service persiste y retorna tarea guardada
     * 6. Controller retorna HTTP 201 con la tarea (incluyendo ID generado)
     * 
     * @param task -> Tarea a crear (validada)
     * @return ResponseEntity con código 201 y la tarea creada
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    /*
     * Obtiene una tarea por su ID.
     * 
     * Endpoint: GET /api/tasks/{id}
     * 
     * @GetMapping("/{id}"): Maneja GET con variable de path  {id} es un placeholder para el ID de la tarea
     * 
     * @PathVariable Long id: Extrae el valor de {id} de la URL Spring convierte automáticamente String -> Long
     * 
     * ResponseEntity.ok(): Crea respuesta con código HTTP 200 (OK)
     * 
     * Si la tarea no existe:
     * - El service lanza TaskNotFoundException
     * - GlobalExceptionHandler la captura
     * - Retorna HTTP 404 con mensaje de error
     * 
     * @param id ID de la tarea a buscar
     * @return ResponseEntity con código 200 y la tarea encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id){
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    /*
     *  Obtiene todas las tareas
     * 
     * Endpoint: GET /api/tasks
     * 
     * @GetMapping (sin parámetros): Ruta base /api/tasks
     * 
     * Retorna una lista de todas las tareas existentes.
     * Si no hay tareas, retorna lista vacía [] (no null).
     * 
     * NOTA: Como ya he dicho, en sistemas con muchos datos, considerar paginación
     * 
     * @return ResponseEntity con código 200 y lista de tareas
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(){
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /*
     * Actualiza una tarea existente
     * 
     * Endpoint: PUT /api/tasks/{id}
     * 
     * @PutMapping: Maneja peticiones HTTP PUT. PUT se usa para ACTUALIZAR recursos completos
     * 
     * Diferencia PUT vs PATCH:
     * - PUT: Actualizacion completa (reemplaza el recurso)
     * - PATCH: Actualizacion parcial (modifica solo campos especificos)
     * 
     * Este endpoint usa PUT porque esperamos recibir todos los campos
     * 
     * param id ID de la tarea a actualizar
     * @param task Nuevos datos de la tarea
     * @return ResponseEntity con código 200 y la tarea actualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
        @PathVariable Long id, 
        @Valid @RequestBody Task task
    ){
        Task updatedTask = taskService.updateTask(id, task);
        return ResponseEntity.ok(updatedTask);
    }

    /*
     * Elimina una tarea.
     * 
     * Endpoint: DELETE /api/tasks/{id}
     * 
     * @DeleteMapping: Maneja peticiones HTTP DELETE. DELETE se usa para ELIMINAR recursos
     * 
     * ResponseEntity<Void>: No retorna body (solo código de estado)
     * HttpStatus.NO_CONTENT (204): Código apropiado para eliminaciones exitosas 204 significa "exitoso pero sin contenido para retornar"
     * 
     * @param id ID de la tarea a eliminar
     * @return ResponseEntity con código 204 sin body
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }


    // ========== FILTROS POR ESTADO Y PRIORIDAD ==========
    /*
     * Obtiene tareas filtradas por estado.
     * 
     * Endpoint: GET /api/tasks/status/{status}
     * 
     * @PathVariable Status status: Spring convierte 
     * automáticamente el String de la URL -> enum Status
     * 
     * Si se envía un valor inválido (ej: "INVALID"):
     * - Spring lanza MethodArgumentTypeMismatchException
     * - Retorna HTTP 400 Bad Request
     * 
     * @param status Estado a filtrar (PENDING, 
     * IN_PROGRESS, COMPLETED)
     * @return ResponseEntity con lista de tareas con ese 
     * estado
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTaskByStatus(@PathVariable Status status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    /*
     * Obtiene tareas filtradas por prioridad
     * 
     * Endpoint: GET /api/tasks/priority/{priority}
     * 
     * Similar a getTasksByStatus pero filtra por 
     * prioridad
     * 
     * @param priority Prioridad a filtrar (LOW, MEDIUM, 
     * HIGH)
     * @return ResponseEntity con lista de tareas con esa 
     * prioridad
     */
    @GetMapping("priority/{priority}")
    public ResponseEntity<List<Task>> getTasksByPriority(@PathVariable Priority priority){
        List<Task> tasks = taskService.getTasksByPriority(priority);
        return ResponseEntity.ok(tasks);
    }

    /*
     * Endpoint: GET /api/tasks/filter?
     * status=PENDING&priority=HIGH
     * 
     * @RequestParam: Extrae parámetros de query string (?
     * status=...&priority=...)
     * 
     * Diferencia @PathVariable vs @RequestParam:
     * 
     * @PathVariable -> Parte de la ruta:
     * /api/tasks/{id} -> id es variable de path
     * 
     * @RequestParam -> Query string (después del ?):
     * /api/tasks?status=PENDING -> status es parámetro 
     * de query
     * 
     * Ventaja de @RequestParam:
     * - Permite parámetros opcionales
     * - Mejor para filtros múltiples
     * - URL más limpia cuando hay muchos filtros
     * 
     * Ejemplo de request:
     * GET /api/tasks/filter?status=PENDING&priority=HIGH
     * 
     * @param status Estado a filtrar
     * @param priority Prioridad a filtrar
     * @return ResponseEntity con lista de tareas que 
     * cumplen ambos criterios
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Task>> getTasksByStatusAndPriority(
        @RequestParam Status status,
        @RequestParam Priority priority
    ){
        List<Task> tasks = taskService.getTasksByStatusAndPriority(status, priority);
        return ResponseEntity.ok(tasks);
    }

    // ========== BÚSQUEDA POR FECHAS ==========

    /*
     * Obtiene tarea cuya fecha esta en el rango
     * 
     * Endpoint: GET /api/tasks/due-date-range?
     * startDate=2025-11-15&endDate=2025-11-22
     * 
     * @DateTimeFormat(iso = DateTimeFormat.ISO.DATE): 
     * Indica el formato de fecha. Formato ISO: yyyy-MM-dd
     * 
     * Sin @DateTimeFormat, Spring no sabría como 
     * convertir String -> LocalDate
     * 
     * @param startDate Fecha inicial (formato: yyyy-MM-
     * dd)
     * @param endDate Fecha final (formato: yyyy-MM-dd)
     * @return ResponseEntity con lista de tareas en ese 
     * rango
     */
    @GetMapping("/due-date-range")
    public ResponseEntity<List<Task>> getTasksByDueDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ){
        List<Task> tasks = taskService.getTasksByDueDateRange(startDate, endDate);
        return ResponseEntity.ok(tasks);
    }

    /*
     * Obtiene tareas vencidas
     * 
     * Endpoint: GET /api/tasks/overdue
     * 
     * @return ResponseEntity con lista de tareas vencidas
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks() {
        List<Task> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }

    /*
     * Obtiene tareas que vencen hoy.
     * 
     * Endpoint: GET /api/tasks/due-today
     * 
     * @return ResponseEntity con lista de tareas que 
     * vencen hoy
     */
    @GetMapping("/due-today")
    public ResponseEntity<List<Task>> getTasksDueToday() {
        List<Task> tasks = taskService.getTasksDueToday();
        return ResponseEntity.ok(tasks);
    }


    // ========== BÚSQUEDA POR TEXTO ==========

    /*
     * Busca tareas por término de búsqueda.
     * 
     * Endpoint: GET /api/tasks/search?term=spring
     * 
     * @param term Término a buscar
     * @return ResponseEntity con lista de tareas que 
     * contienen el término
     */
    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchTasks(@RequestParam String term){
        List<Task> tasks = taskService.searchTasks(term);
        return ResponseEntity.ok(tasks);
    }


    // ========== CAMBIOS DE ESTADO ==========

    /*
     * Marca una tarea como "En Progreso".
     * 
     * Endpoint: PATCH /api/tasks/{id}/in-progress
     * 
     * @PatchMapping: Maneja peticiones HTTP PATCH. PATCH 
     * se usa para actualizaciones PARCIALES
     * 
     * ¿Por qué PATCH y no PUT?
     * - PUT: Actualización completa del recurso
     * - PATCH: Actualización parcial (solo cambiamos el 
     * estado)
     * 
     * Este endpoint solo modifica el campo 'status',
     * por eso PATCH es más semántico.
     * 
     * @param id ID de la tarea
     * @return ResponseEntity con la tarea actualizada
     */
    @PatchMapping("/{id}/in-progress")
    public ResponseEntity<Task> markTaskAsInProgress(@PathVariable Long id){
        Task updatedTask = taskService.markTaskAsInProgress(id);
        return ResponseEntity.ok(updatedTask);
    }

    /*
     * Marca una tarea como completada
     * 
     * Endpoint: PATCH /api/tasks/{id}/complete
     * 
     * Similar a markTaskAsInProgress pero cambia estado a 
     * COMPLETED.
     * 
     * @param id ID de la tarea
     * @return ResponseEntity con la tarea completada
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<Task> markTaskAsCompleted(@PathVariable Long id){
        Task completedTask = taskService.markTaskAsCompleted(id);
        return ResponseEntity.ok(completedTask);
    }


    // ========== ESTADÍSTICAS (BONUS) ==========

    /*
     * Obtiene la cuenta de tareas por estado
     * 
     * Endpoint: GET /api/tasks/count/status/{status}
     * 
     * @param status Estado a contar
     * @return ResponseEntity con el número de tareas
     */
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countTasksByStatus(@PathVariable Status status) {
        Long count = taskService.countByStatus(status);
        return ResponseEntity.ok(count);
    }
    
}
