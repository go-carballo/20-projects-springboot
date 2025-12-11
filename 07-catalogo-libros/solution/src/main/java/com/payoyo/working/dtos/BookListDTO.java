package com.payoyo.working.dtos;

import lombok.*;
import java.math.BigDecimal;

import com.payoyo.working.model.Book;

/**
 * DTO de vista resumida para listados de libros.
 * 
 * Propósito: Reducir el payload en endpoints que devuelven múltiples libros.
 * Contexto de uso: GET /api/books (listado completo), búsquedas, catálogos
 * 
 * Contiene solo información esencial:
 * - Identificación (ISBN, título, autor)
 * - Datos comerciales (precio, stock)
 * 
 * Reducción de payload: ~60% respecto a enviar la entidad completa
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BookListDTO {

    /**
     * ISBN del libro - Identificador único
     */
    private String isbn;

    /**
     * Título del libro
     */
    private String title;

    /**
     * Nombre del autor
     */
    private String author;

    /**
     * Precio de venta
     */
    private BigDecimal price;

    /**
     * Cantidad disponible en inventario
     */
    private Integer stock;

    /**
     * Convierte una entidad Book a su representación DTO resumida.
     * 
     * Patrón Factory Method estático para conversión Entity → DTO.
     * 
     * Ventajas:
     * - Centraliza la lógica de conversión
     * - Más legible que hacer new + setters en Service
     * - Facilita mantenimiento si cambia estructura
     * 
     * @param book Entidad Book de la base de datos
     * @return BookListDTO con los campos esenciales
     */
    public static BookListDTO fromEntity(Book book) {
        BookListDTO dto = new BookListDTO();
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPrice(book.getPrice());
        dto.setStock(book.getStock());
        return dto;
    }
}
