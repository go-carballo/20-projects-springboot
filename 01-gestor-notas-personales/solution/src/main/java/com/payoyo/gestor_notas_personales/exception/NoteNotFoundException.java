package com.payoyo.gestor_notas_personales.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/*
 * Excepcion lanzada cuando no se encuentra una nota pos su ID
 * Retorna un HTTP 404 (NOT_FOUND) automaticamente
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoteNotFoundException extends RuntimeException{
    
    private static final long serialVersionUID = 1L;

    /*
     * Constructor con ID de la nota no encontrada
     * @param id -> el ID de la nota que no se encontro
     */
    public NoteNotFoundException(Long id){
        super(String.format("Nota no encontrada con id: %d", id));
    }

    /*
     * Constructor con mensaje personalizado
     * @param message -> el mensaje de error
     */
    public NoteNotFoundException(String message) {
        super(message);
    }

    /*
     * Constructor con mensaje y causa
     * @param message -> el mensaje de error
     * @param cause -> la causa de la excepcion
     */
    public NoteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
