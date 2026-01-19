package com.payoyo.working.exceptions;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Intercepta excepciones lanzadas en cualquier Controller y las convierte
 * en respuestas HTTP apropiadas con formato consistente.
 * 
 * @RestControllerAdvice aplica a todos los @RestController de la aplicación.
 * 
 * Beneficios:
 * - Manejo centralizado de errores (no repetir try-catch en Controllers)
 * - Respuestas JSON consistentes
 * - Códigos HTTP apropiados según el tipo de error
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja CourseNotFoundException.
     * Retorna 404 NOT FOUND cuando no se encuentra un curso.
     * 
     * @param ex Excepción capturada
     * @param request Request HTTP para obtener la ruta
     * @return ResponseEntity con ErrorResponse y status 404
     */
    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCourseNotFound(
            CourseNotFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja DuplicateCourseException.
     * Retorna 409 CONFLICT cuando se intenta crear un curso con título duplicado.
     * 
     * @param ex Excepción capturada
     * @param request Request HTTP para obtener la ruta
     * @return ResponseEntity con ErrorResponse y status 409
     */
    @ExceptionHandler(DuplicateCourseException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCourse(
            DuplicateCourseException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja InvalidRatingException.
     * Retorna 400 BAD REQUEST cuando el rating está fuera de rango (0.0-5.0).
     * 
     * @param ex Excepción capturada
     * @param request Request HTTP para obtener la ruta
     * @return ResponseEntity con ErrorResponse y status 400
     */
    @ExceptionHandler(InvalidRatingException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRating(
            InvalidRatingException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja InvalidDiscountException.
     * Retorna 400 BAD REQUEST cuando el descuento está fuera de rango (0-100).
     * 
     * @param ex Excepción capturada
     * @param request Request HTTP para obtener la ruta
     * @return ResponseEntity con ErrorResponse y status 400
     */
    @ExceptionHandler(InvalidDiscountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDiscount(
            InvalidDiscountException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja errores de validación de Bean Validation (@Valid en Controller).
     * Retorna 400 BAD REQUEST con detalles de todos los campos que fallaron validación.
     * 
     * Ejemplo de respuesta:
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "message": "title: El título es obligatorio; price: El precio mínimo es 0",
     *   "path": "/api/courses"
     * }
     * 
     * @param ex Excepción de validación con todos los errores
     * @param request Request HTTP para obtener la ruta
     * @return ResponseEntity con ErrorResponse detallado y status 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        // Construir mensaje con todos los errores de validación
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        // Convertir Map a String legible: "campo1: error1; campo2: error2"
        String errorMessage = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Error de validación");
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                errorMessage,
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja IllegalArgumentException.
     * Captura errores de argumentos inválidos no cubiertos por otras excepciones.
     * Retorna 400 BAD REQUEST.
     * 
     * @param ex Excepción capturada
     * @param request Request HTTP para obtener la ruta
     * @return ResponseEntity con ErrorResponse y status 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja cualquier excepción no capturada específicamente.
     * Actúa como red de seguridad para errores inesperados.
     * Retorna 500 INTERNAL SERVER ERROR.
     * 
     * IMPORTANTE: En producción, NO exponer detalles internos del error.
     * Usar mensaje genérico y loggear el error completo.
     * 
     * @param ex Excepción capturada
     * @param request Request HTTP para obtener la ruta
     * @return ResponseEntity con ErrorResponse y status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        // En producción: loggear ex.getMessage() y stacktrace
        // System.err.println("Error no controlado: " + ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocurrió un error inesperado. Por favor contacte al administrador.",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}