package com.payoyo.gestor_notas_personales.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

/*
 * Manejador global de excepciones para la aplicacion
 * Captura y procesa las excepciones lanzadas por los controladores REST
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /*
     * Maneja excepciones cuando no se encuentra una nota
     * 
     * @param ex -> la excepcion lanzada
     * @param request -> info del request HTTP
     * @return respuesta con estado 404 NOT FOUND
     */
    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoteNotFoundException(
        NoteNotFoundException ex,
        WebRequest request
    ) {
        log.info("Nota no encontrada: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .message(ex.getMessage())
            .path(getRequestPath(request))
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /*
     * Maneja excepciones de validacoin de Bean Validation
     * 
     * @param ex -> la excepcion de validacion
     * @param request -> informacion del request HTTP
     * @return repuesta con estado 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        WebRequest request
    ){
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String filedName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(filedName, errorMessage);
        });

        log.warn("Error de validacion en {}: {}", getRequestPath(request), validationErrors);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Error de validación en los datos enviados")
                .path(getRequestPath(request))
                .validationErrors(validationErrors)
                .build();
        
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Maneja cualquier excepción no capturada específicamente.
     * 
     * @param ex la excepción genérica
     * @param request información del request HTTP
     * @return respuesta con estado 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            WebRequest request) {
        
        log.error("Error inesperado en {}: {}", getRequestPath(request), ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Ha ocurrido un error interno en el servidor")
                .path(getRequestPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Extrae la ruta del request HTTP.
     * 
     * @param request el request HTTP
     * @return la ruta del request
     */
    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
