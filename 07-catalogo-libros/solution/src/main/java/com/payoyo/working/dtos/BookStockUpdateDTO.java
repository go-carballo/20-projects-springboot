package com.payoyo.working.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para actualización específica del stock de un libro.
 * 
 * Propósito: Permitir actualización atómica del inventario sin riesgo
 *            de sobrescribir otros campos del libro por error.
 * 
 * Contexto de uso:
 * - PATCH /api/books/{isbn}/stock
 * - Operaciones de inventario (recepciones, ventas, ajustes)
 * - Integraciones con sistemas de gestión de almacén
 * 
 * Ventajas de usar un DTO específico:
 * 1. Seguridad: No se pueden modificar accidentalmente otros campos (precio, título, etc.)
 * 2. Claridad: El contrato de API es explícito - solo acepta stock
 * 3. Validación: Solo valida el campo relevante
 * 4. Simplicidad: Las integraciones de inventario solo envían lo necesario
 * 
 * Ejemplo de uso:
 * Sistema de ventas reduce stock después de compra
 * PATCH /api/books/978-0-134-68599-1/stock
 * { "stock": 8 }  // Antes: 10, Vendidas: 2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BookStockUpdateDTO {

    /**
     * Nueva cantidad de stock para el libro.
     * 
     * Validaciones:
     * - Campo obligatorio (no puede ser null)
     * - Mínimo: 0 (no puede ser negativo)
     * 
     * Nota: Este valor REEMPLAZA el stock actual, no lo incrementa/decrementa.
     * Si necesitas incrementar/decrementar, debes calcularlo antes de enviar.
     * 
     * Ejemplo:
     * - Stock actual: 10
     * - Vendiste 2 unidades
     * - Envías: { "stock": 8 }  ← Valor final, no -2
     */
    @NotNull(message = "El valor de stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
}