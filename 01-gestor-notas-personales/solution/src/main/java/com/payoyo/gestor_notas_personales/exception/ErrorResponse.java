package com.payoyo.gestor_notas_personales.exception;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

/*
 * Respuesta estandar de error para la API REST
 * Se serializa como JSON en las respuestas HTTP
 */

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /*
     * Timestamp de cuando ocurrio el error
     */
    private LocalDateTime timestamp;

    /*
     * Codigo de estado HTTP
     */
    private int status;

    /*
     * Nombre del error HTTP
     */
    private String error;

    /*
     * Mensaje descriptivo del error
     */
    private String message;

    /*
     * Ruta del request que causó el error
     */
    private String path;

    /*
     * Errores de validacion detallados por campo
     * (solo para errores de validacion)
     */
    private Map<String, String> validationErrors;
}
