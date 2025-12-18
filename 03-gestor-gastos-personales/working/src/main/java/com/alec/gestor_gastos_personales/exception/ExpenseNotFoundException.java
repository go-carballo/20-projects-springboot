package com.alec.gestor_gastos_personales.exception;

/**
 * Excepción lanzada cuando no se encuentra un gasto.
 */
public class ExpenseNotFoundException extends RuntimeException {
    
    public ExpenseNotFoundException(Long id) {
        super("No se encontró el gasto con ID: " + id);
    }
    
    public ExpenseNotFoundException(String message) {
        super(message);
    }
}
