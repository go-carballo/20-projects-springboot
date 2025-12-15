package com.payoyo.working.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para estructurar las respuestas de error de forma consistente.
 * 
 * Proporciona información detallada sobre errores al cliente en formato JSON,
 * siguiendo el estándar Problem Details (RFC 7807) adaptado.
 * 
 * @JsonInclude(NON_NULL) excluye campos null del JSON de respuesta,
 * haciendo las respuestas más limpias cuando no hay detalles adicionales.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * Timestamp del momento en que ocurrió el error.
     * Útil para correlacionar errores con logs del servidor.
     */
    private LocalDateTime timestamp;
    
    /**
     * Código de estado HTTP.
     * Ejemplos: 400, 404, 409, 500
     */
    private int status;
    
    /**
     * Nombre descriptivo del error HTTP.
     * Ejemplos: "Bad Request", "Not Found", "Conflict"
     */
    private String error;
    
    /**
     * Mensaje descriptivo del error específico.
     * 
     * Este mensaje proviene de:
     * - La excepción lanzada (ex.getMessage())
     * - Mensajes personalizados del handler
     * 
     * Ejemplos:
     * - "Cita con ID 123 no encontrada"
     * - "El horario solicitado ya está ocupado"
     * - "Solo se pueden confirmar citas en estado PENDIENTE"
     */
    private String message;
    
    /**
     * Path de la petición que generó el error.
     * Ejemplo: "/api/appointments/123"
     * 
     * Opcional: Solo se incluye si se captura del request.
     */
    private String path;
    
    /**
     * Lista de errores detallados (opcional).
     * 
     * Se usa principalmente para errores de validación Bean Validation,
     * donde múltiples campos pueden tener errores simultáneamente.
     * 
     * Ejemplo para POST con múltiples campos inválidos:
     * [
     *   "El nombre del cliente es obligatorio",
     *   "El formato del email es inválido",
     *   "La hora de fin debe ser posterior a la hora de inicio"
     * ]
     * 
     * @JsonInclude(NON_NULL) hace que este campo no aparezca en el JSON
     * si no hay errores de validación múltiples.
     */
    private List<String> details;
}
