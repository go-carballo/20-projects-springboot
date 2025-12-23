package com.payoyo.working.exceptions;

import java.time.LocalDateTime;

/**
 * DTO estándar para respuestas de error de la API.
 * Proporciona información consistente sobre errores a los clientes.
 * 
 * Estructura JSON de respuesta:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "No se encontró el curso con ID: 123",
 *   "path": "/api/courses/123"
 * }
 */
public record ErrorResponse(
        /**
         * Momento exacto en que ocurrió el error.
         */
        LocalDateTime timestamp,
        
        /**
         * Código de estado HTTP (404, 400, 500, etc.).
         */
        int status,
        
        /**
         * Nombre del error HTTP (Not Found, Bad Request, etc.).
         */
        String error,
        
        /**
         * Mensaje descriptivo del error específico.
         */
        String message,
        
        /**
         * Ruta del endpoint que causó el error.
         */
        String path
) {
    /**
     * Constructor conveniente que establece timestamp automáticamente.
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path);
    }
}