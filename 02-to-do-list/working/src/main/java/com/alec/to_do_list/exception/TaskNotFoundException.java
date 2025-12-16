package com.alec.to_do_list.exception;


public class TaskNotFoundException extends RuntimeException {

    /**
     * Constructor que recibe el ID de la tarea no encontrada
     * Genera un mensaje descriptivo automaticamente
     * 
     * @param id -> ID de la tarea que no se encontró
     */
    public TaskNotFoundException(Long id) {
        super(String.format("Task not found with ID: %d", id));
    }

    /**
     * Constructor con mensaje personalizado
     * 
     * @param message mensaje de error personalizado
     */
    public TaskNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje personalizado y causa
     * 
     * @param message mensaje de error personalizado
     * @param cause causa de la excepción
     */
    public TaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}