package com.alec.to_do_list.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase para estructurar las respuestas de error de la API
 * 
 * Proporciona información detallada sobre errores:
 * - Timestamp del error
 * - Código de estado HTTP
 * - Tipo de error
 * - Mensaje principal
 * - Lista de errores de validación (si aplica)
 * - Path del endpoint donde ocurrió el error
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private int status;
    private String error;
    private String message;
    private List<String> validationErrors;
    private String path;

    /**
     * Constructor para errores simples sin validaciones
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    /**
     * Constructor para errores con validaciones
     */
    public ErrorResponse(int status, String error, String message, List<String> validationErrors, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.validationErrors = validationErrors;
        this.path = path;
    }
}