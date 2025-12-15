package com.payoyo.working.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para toda la aplicación.
 * 
 * @RestControllerAdvice combina @ControllerAdvice + @ResponseBody:
 * - Captura excepciones lanzadas por cualquier @RestController
 * - Devuelve las respuestas automáticamente como JSON
 * - Centraliza el manejo de errores en un único punto
 * 
 * Beneficios:
 * - Código DRY: no repetir try-catch en cada controller
 * - Respuestas consistentes: mismo formato JSON para todos los errores
 * - Separación de responsabilidades: controllers solo manejan happy path
 * - Facilita testing: se puede probar el manejo de errores aisladamente
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // ==================== EXCEPCIONES DE NEGOCIO ====================
    
    /**
     * Maneja AppointmentNotFoundException.
     * 
     * Se lanza cuando:
     * - GET /api/appointments/{id} con ID inexistente
     * - GET /api/appointments/codigo/{codigo} con código inválido
     * - PUT/PATCH/DELETE sobre cita inexistente
     * 
     * HTTP Status: 404 Not Found
     * Semántica: El recurso solicitado no existe en el servidor
     * 
     * @param ex Excepción capturada
     * @return ResponseEntity con ErrorResponse y status 404
     */
    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAppointmentNotFound(
            AppointmentNotFoundException ex) {
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase()) // "Not Found"
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }
    
    /**
     * Maneja InvalidTimeRangeException.
     * 
     * Se lanza cuando:
     * - Horario fuera del rango laboral (no entre 08:00 y 20:00)
     * - Anticipación mínima no cumplida (< 2 horas)
     * - Otras validaciones de horario en Service
     * 
     * HTTP Status: 400 Bad Request
     * Semántica: Los datos enviados son inválidos según reglas de negocio
     * 
     * @param ex Excepción capturada
     * @return ResponseEntity con ErrorResponse y status 400
     */
    @ExceptionHandler(InvalidTimeRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTimeRange(
            InvalidTimeRangeException ex) {
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase()) // "Bad Request"
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
    
    /**
     * Maneja TimeSlotNotAvailableException.
     * 
     * Se lanza cuando:
     * - POST /api/appointments con horario que solapa con otra cita activa
     * - PUT /api/appointments/{id} actualizando a horario ocupado
     * 
     * HTTP Status: 409 Conflict
     * Semántica: El request es válido pero conflictúa con el estado actual
     * 
     * Diferencia con 400:
     * - 400: Los datos son intrínsecamente inválidos
     * - 409: Los datos son válidos pero hay conflicto de estado
     * 
     * Ejemplo: Un horario 10:00-11:00 es válido en sí mismo (formato correcto,
     * duración correcta), pero está en conflicto porque ya está ocupado.
     * 
     * @param ex Excepción capturada
     * @return ResponseEntity con ErrorResponse y status 409
     */
    @ExceptionHandler(TimeSlotNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleTimeSlotNotAvailable(
            TimeSlotNotAvailableException ex) {
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase()) // "Conflict"
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }
    
    /**
     * Maneja InvalidStateTransitionException.
     * 
     * Se lanza cuando:
     * - PATCH /api/appointments/{id}/confirmar con estado ≠ PENDIENTE
     * - PATCH /api/appointments/{id}/cancelar con estado = COMPLETADA
     * - PATCH /api/appointments/{id}/completar con estado ≠ CONFIRMADA
     * 
     * HTTP Status: 400 Bad Request
     * Semántica: La operación solicitada no es válida para el estado actual
     * 
     * @param ex Excepción capturada
     * @return ResponseEntity con ErrorResponse y status 400
     */
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransition(
            InvalidStateTransitionException ex) {
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase()) // "Bad Request"
                .message(ex.getMessage())
                .build();
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
    
    // ==================== VALIDACIONES BEAN VALIDATION ====================
    
    /**
     * Maneja errores de validación Bean Validation (@Valid en Controller).
     * 
     * Se lanza automáticamente cuando:
     * - POST/PUT con AppointmentRequestDTO que falla validaciones
     * - @NotBlank, @Email, @Size, @DecimalMin, @AssertTrue, etc. fallan
     * 
     * HTTP Status: 400 Bad Request
     * 
     * Esta excepción puede contener MÚLTIPLES errores de validación
     * (ej: email inválido + teléfono vacío + precio negativo).
     * 
     * Procesamiento:
     * 1. Extraer todos los FieldError del BindingResult
     * 2. Mapear cada error a su mensaje por defecto
     * 3. Devolver lista en el campo "details"
     * 
     * Ejemplo de respuesta JSON:
     * {
     *   "timestamp": "2024-12-15T10:30:00",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "Error de validación en los datos enviados",
     *   "details": [
     *     "El email es obligatorio",
     *     "La hora de fin debe ser posterior a la hora de inicio",
     *     "El precio debe ser mayor o igual a 0"
     *   ]
     * }
     * 
     * @param ex Excepción capturada con todos los errores de validación
     * @return ResponseEntity con ErrorResponse y lista de errores
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        // Extraer mensajes de todos los errores de campo
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Error de validación en los datos enviados")
                .details(errors) // Lista de errores específicos
                .build();
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
    
    // ==================== EXCEPCIÓN GENÉRICA (CATCH-ALL) ====================
    
    /**
     * Maneja cualquier excepción no capturada por los handlers específicos.
     * 
     * Actúa como red de seguridad (catch-all) para:
     * - Errores inesperados de programación (NullPointerException, etc.)
     * - Errores de infraestructura (BD caída, timeouts, etc.)
     * - Cualquier RuntimeException no prevista
     * 
     * HTTP Status: 500 Internal Server Error
     * Semántica: Error del servidor, no del cliente
     * 
     * IMPORTANTE: En producción, NO exponer detalles técnicos (stack trace)
     * al cliente por seguridad. Solo mensaje genérico + logging interno.
     * 
     * Best practice:
     * - Loguear el error completo con stack trace en servidor
     * - Devolver mensaje genérico al cliente
     * - Incluir timestamp para correlación con logs
     * 
     * @param ex Cualquier excepción no manejada específicamente
     * @return ResponseEntity con ErrorResponse genérico y status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        
        // En producción, loguear el error completo:
        // log.error("Error inesperado: ", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()) // "Internal Server Error"
                .message("Ha ocurrido un error inesperado. Por favor, contacte al administrador.")
                .build();
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}