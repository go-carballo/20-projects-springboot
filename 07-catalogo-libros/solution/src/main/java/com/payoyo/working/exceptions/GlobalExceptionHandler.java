package com.payoyo.working.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la aplicación.
 * 
 * @RestControllerAdvice intercepta excepciones lanzadas por los Controllers
 * y las transforma en respuestas HTTP apropiadas con estructura JSON consistente.
 * 
 * Ventajas del manejo centralizado:
 * 1. Evita bloques try-catch repetitivos en cada Controller
 * 2. Respuestas de error consistentes en toda la API
 * 3. Separación de responsabilidades (Controller se enfoca en lógica)
 * 4. Facilita mantenimiento y testing
 * 
 * Estructura de respuesta de error:
 * {
 *   "timestamp": "2024-12-08T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Libro no encontrado con ISBN: 978-0-134-68599-1",
 *   "path": "/api/books/978-0-134-68599-1"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja la excepción BookNotFoundException.
     * 
     * Se lanza cuando se intenta acceder a un libro que no existe.
     * 
     * Escenarios:
     * - GET /api/books/{isbn} con ISBN inexistente
     * - PUT /api/books/{isbn} intentando actualizar libro inexistente
     * - PATCH /api/books/{isbn}/stock intentando actualizar stock de libro inexistente
     * - DELETE /api/books/{isbn} intentando eliminar libro inexistente
     * 
     * @param ex Excepción BookNotFoundException lanzada
     * @return ResponseEntity con código 404 y mensaje de error
     */
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFoundException(BookNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja la excepción DuplicateIsbnException.
     * 
     * Se lanza cuando se intenta crear un libro con un ISBN que ya existe.
     * El ISBN es la clave primaria, por lo tanto debe ser único.
     * 
     * Escenarios:
     * - POST /api/books con ISBN duplicado
     * 
     * Nota: Devuelve 409 Conflict (estándar REST para conflictos de recursos)
     * 
     * @param ex Excepción DuplicateIsbnException lanzada
     * @return ResponseEntity con código 409 y mensaje de error
     */
    @ExceptionHandler(DuplicateIsbnException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateIsbnException(DuplicateIsbnException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Maneja errores de validación de Bean Validation (@Valid en DTOs).
     * 
     * Se lanza cuando un DTO con @Valid no cumple las validaciones definidas
     * con anotaciones como @NotBlank, @Size, @Min, @Max, @Pattern, etc.
     * 
     * Escenarios:
     * - POST /api/books con BookCreateDTO inválido
     * - PUT /api/books/{isbn} con BookCreateDTO inválido
     * - PATCH /api/books/{isbn}/stock con BookStockUpdateDTO inválido
     * 
     * Respuesta estructurada con todos los campos que fallaron:
     * {
     *   "timestamp": "2024-12-08T10:30:00",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "Error de validación en los datos enviados",
     *   "errors": {
     *     "price": "El precio debe ser al menos 0.01",
     *     "title": "El título es obligatorio"
     *   }
     * }
     * 
     * @param ex Excepción MethodArgumentNotValidException lanzada por Spring
     * @return ResponseEntity con código 400 y detalles de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        // Extraer errores de validación campo por campo
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Error de validación en los datos enviados")
                .errors(errors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja cualquier otra excepción no contemplada específicamente.
     * 
     * Este es el handler "catch-all" que atrapa cualquier Exception
     * que no haya sido manejada por los handlers específicos anteriores.
     * 
     * Escenarios:
     * - Errores inesperados de base de datos
     * - NullPointerException no controladas
     * - Errores de lógica de negocio no previstos
     * - Cualquier RuntimeException no específica
     * 
     * Nota: En producción, NO se debe exponer el stack trace completo
     * al cliente por seguridad. Aquí usamos ex.getMessage() solamente.
     * 
     * @param ex Excepción genérica capturada
     * @return ResponseEntity con código 500 y mensaje genérico
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Ha ocurrido un error interno en el servidor")
                .build();
        
        // En desarrollo, puedes hacer log del stack trace completo
        ex.printStackTrace(); 
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Clase interna para estructura de respuestas de error genéricas.
     * 
     * Proporciona formato consistente para todos los errores de la API.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ErrorResponse {
        /**
         * Timestamp del momento en que ocurrió el error
         */
        private LocalDateTime timestamp;
        
        /**
         * Código de estado HTTP (404, 409, 500, etc.)
         */
        private int status;
        
        /**
         * Descripción textual del código HTTP (Not Found, Conflict, etc.)
         */
        private String error;
        
        /**
         * Mensaje descriptivo del error específico
         */
        private String message;
    }

    /**
     * Clase interna para estructura de respuestas de errores de validación.
     * 
     * Extiende ErrorResponse añadiendo un mapa de errores por campo.
     * Usado específicamente para errores de @Valid en DTOs.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ValidationErrorResponse {
        /**
         * Timestamp del momento en que ocurrió el error
         */
        private LocalDateTime timestamp;
        
        /**
         * Código de estado HTTP (siempre 400 para errores de validación)
         */
        private int status;
        
        /**
         * Descripción textual del código HTTP (Bad Request)
         */
        private String error;
        
        /**
         * Mensaje general sobre el tipo de error
         */
        private String message;
        
        /**
         * Mapa de errores por campo.
         * 
         * Estructura:
         * {
         *   "nombreCampo": "mensaje de error",
         *   "otroCampo": "otro mensaje"
         * }
         * 
         * Ejemplo:
         * {
         *   "price": "El precio debe ser al menos 0.01",
         *   "isbn": "El ISBN es obligatorio",
         *   "publicationYear": "El año de publicación debe ser posterior a 1450"
         * }
         */
        private Map<String, String> errors;
    }
}