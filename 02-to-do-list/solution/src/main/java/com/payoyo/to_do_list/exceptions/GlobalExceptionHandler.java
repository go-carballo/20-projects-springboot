package com.payoyo.to_do_list.exceptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
 * Manejador Global de excepciones para toda la aplicacion
 * 
 * @RestControllerAdvice: Componente de Spring que intercepta todas las excepciones
 * lanzadas por los controladores y las maneha de forma centralizada
 * 
 * Ventajas de este enfoque:
 * 1. Codigo DRY: No repetimos try-catch en cada controller
 * 2. Consistencia: Todos los errores tienen el mismo formato
 * 3. Separacion de responsabilidades: Los controlleres no manejan errores
 * 4. Mantenibilidad: Un solo lugar para modificar el manejo de errores
 * 
 * Alternativa sin @RestControllerAdvice (MAL):
 * Cada método del controller tendría:
 * try {
 *     // lógica
 * } catch (Exception e) {
 *     // manejo de error
 * }
 * Esto es repetitivo y difícil de mantener.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /*
     * Maneja la excepcion TaskNotFoundException
     * 
     * @ExceptionHandler: Indica que tipo de excepcion maneja este metodo
     * Se ejecuta automaticamente cuando se lanza TaskNotFoundException
     * en cualquier parte de la aplicacion
     * 
     * Flujo:
     * 1. Service lanza: throw new TaskNotFoundException(id)
     * 2. Spring intercepta la excepcion
     * 3. Este metodo la captura y la procesa
     * 4. Retorna un ResponseEntity con el error formateado
     * 
     * @param ex -> La excepcion lanzada
     * @return ResponseEntity con codigo 404 y detalle del error
     */
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFound(TaskNotFoundException ex) {
        // construimos una respuesta de error estructurada
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .build();
        
        // retornamos con cofigo HTTP 404
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /*
     * Maneja errores de validacion de datos (Bean Validation)
     * 
     * MethodArgumentNotValidException se lanza cuando:
     * - Un @RequestBody falla las validaciones (@NotBlank, @Size, etc)
     * 
     * Ejemplo:
     * {
     *   "title": "", // falla @NotBlank
     *   "descripction": "texto muy largo..." // falla @Size(max=500)
     * }
     * 
     * Esta excepcion contiene TODOS los errores de validacion,
     * por lo que iteramos sobre ellos y los devolvemos al cliente
     * 
     * Respuesta generada: 
     * {
     *   "timestamp": "2025-11-15 14:30:00",
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "errors": {
     *     "title": "El titulo de la tarea es obligatorio",
     *     "description": "La descripcion no puede contener mas de 500 caracteres"
     *   }
     * }
     * 
     * @param ex -> Excepcion de validacion con todos los errores
     * @return ResponseEntity con codigo 400 y mapa de errores por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions (MethodArgumentNotValidException ex){
        
        // mapa para almacenar errores: campo -> mensaje
        Map<String, String> errors = new HashMap<>();

        // iteramos sobre todos los errores de validacion
        ex.getBindingResult().getAllErrors().forEach(error -> {
            // obtenemos el nombre del campo que fallo
            String fieldName = ((FieldError) error).getField();
            // obtenemos el mensaje de error
            String errorMessage = error.getDefaultMessage();
            // los guardamos en el map
            errors.put(fieldName, errorMessage);
        });

        // construimos la respuesta completa
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", errors); // map con todos los errores

        return ResponseEntity.badRequest().body(response);
    }

    /*
     * Maneja cualquier otra excepcion no contemplada (catch-all)
     * 
     * Es el ultimo recurso cuando ningun otro @ExceptionHandler aplica
     * Previene que el servidor devuelva stack traces al cliente
     * 
     * Importante:
     * - NO expongas detalles internos al cliente (por seguridad)
     * - Registra el error completo en logs para debugging
     * - Devuelve un mensaje generico al usuario
     * 
     * Casos que captura:
     * - NullPointException
     * - SQLException
     * - Cualquier RunTimeException no manejada especificamente
     * 
     * @param ex -> Cualuier excepcion no manejada
     * @return ResponseEntity con codigo 500 y mensaje generico
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("Ocurrio un error inesperado. Por favo, intentelo de nuevo mas tarde")
            .build();
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }

    /* 
     * BONUS: Maneja tambien IllegalArgumentException
     * 
     * Util para validaciones de negocio que no son de formato
     * 
     * Ejemplo:
     * if (dueDate.isBefore(LocalDate.now())) {
     *     throw new IllegalArgumentException("La fecha límite no puede ser en el pasado");
     * }
     * 
     * @param ex -> Excepción de argumento ilegal
     * @return ResponseEntity con código 400
    */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex){
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .build();

        return ResponseEntity.badRequest().body(error);
    }
}
