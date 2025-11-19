package com.payoyo.to_do_list.exceptions;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Clase que estructura las respuestas de error de la API
 * 
 * Proporciona un formato consistente para todos los errores:
 * {
 *  "timestamp": "2025-11-15 14:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "No se encontró la tarea con ID: 5"
 * }
 * 
 * Esto mejora la experiencia del consumidor de la API ya que 
 * todos los errores tienen la misma estructura predecible
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    /*
     * Momento exacto en el que ocurrio el error
     * 
     * @JsonFormat: Formatea la fecha en el JSON de respuesta
     * Formato: "2025-11-15 14:30:00"
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /*
     * Codigo de estado HTTP
     * Ejemplos: 400 (Bad Request), 404 (Not Found), 500 (Internal Server Error)
     */
    private int status;

    /*
     * Nombre del error HTTP
     * Ejemplos "Bad Request", "Not Found", "Internal Server Error"
     */
    private String error;

    /*
     * Mensaje descriptivo del error para el usuario
     * Debe ser claro y util para entender que salio mal
     */
    private String message;
    
}
