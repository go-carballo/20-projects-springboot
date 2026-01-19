package com.payoyo.working.exceptions;

/**
 * Excepción lanzada cuando se intenta establecer un descuento fuera del rango válido.
 * 
 * Rango válido: 0 a 100 (porcentaje)
 * 
 * Caso de uso:
 * - POST /api/courses con discount > 100 o < 0
 * - PUT /api/courses/{id} con discount inválido
 */
public class InvalidDiscountException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje descriptivo del error
     */
    public InvalidDiscountException(String message) {
        super(message);
    }

    /**
     * Constructor conveniente que genera mensaje con el descuento inválido.
     * 
     * @param discount Valor de descuento que causó el error
     */
    public InvalidDiscountException(Integer discount) {
        super("Descuento inválido: " + discount + "%. Debe estar entre 0 y 100");
    }
}