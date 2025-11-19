package com.payoyo.to_do_list.exceptions;

/*
 * Excepcion personalizada pra cuando no se encuentra una tarea
 * 
 * Extiende de RunTimeException(unchecked exception):
 * - No necesita declararse en throws
 * - Mas limpio para errores de negocio
 * - Spring maneja automaticamente estas excepciones
 * 
 * Buenas prácticas:
 * - Nombre descriptivo que indica el problema
 * Mensaje personalizado con informacion util
 * Hereda de RunTimeException (no de Exception)
 */
public class TaskNotFoundException extends RuntimeException{
    
    /*
     * Constructor que recibe el ID de la tarea no encontrada
     * Genera un mensaje descriptivo automaticamente
     * 
     * @param id -> ID de la tarea que no se encontró
     */
    public TaskNotFoundException(Long id) {
        super(String.format("No se encontro la tarea con ID: %d", id));
    }

    /*
     * Constructor que permite un mensaje personalizado
     * Util para casos especiales
     * 
     * @param message -> Mensaje de error personalizado
     */
    public TaskNotFoundException(String message) {
        super(message);
    }
}
