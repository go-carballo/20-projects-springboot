package com.payoyo.working.dtos;

import lombok.*;
import java.math.BigDecimal;

import com.payoyo.working.model.Book;

/**
 * DTO de vista completa para detalles de un libro.
 * 
 * Propósito: Proporcionar toda la información de un libro específico.
 * Contexto de uso:
 * - GET /api/books/{isbn} (consulta individual)
 * - Respuestas de POST (confirmación de creación)
 * - Respuestas de PUT/PATCH (confirmación de actualización)
 * 
 * Contiene todos los campos de la entidad Book.
 * Es el "espejo" de la entidad pero sin anotaciones JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BookDetailDTO {

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
     * Editorial (campo opcional)
     */
    private String publisher;

    /**
     * Año de publicación
     */
    private Integer publicationYear;

    /**
     * Número de páginas
     */
    private Integer pages;

    /**
     * Idioma en formato ISO 639-1 (ej: es, en, fr)
     */
    private String language;

    /**
     * Precio de venta
     */
    private BigDecimal price;

    /**
     * Cantidad disponible en inventario
     */
    private Integer stock;

    /**
     * Sinopsis o descripción del libro (campo opcional)
     */
    private String synopsis;

    /**
     * URL de la imagen de portada (campo opcional)
     */
    private String imageUrl;

    /**
     * Convierte una entidad Book a su representación DTO completa.
     * 
     * Patrón Factory Method estático para conversión Entity → DTO.
     * Incluye todos los campos de la entidad.
     * 
     * Uso típico en Service:
     * <pre>
     * Book book = bookRepository.findById(isbn).orElseThrow(...);
     * return BookDetailDTO.fromEntity(book);
     * </pre>
     * 
     * @param book Entidad Book de la base de datos
     * @return BookDetailDTO con todos los campos
     */
    public static BookDetailDTO fromEntity(Book book) {
        BookDetailDTO dto = new BookDetailDTO();
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPublisher(book.getPublisher());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setPages(book.getPages());
        dto.setLanguage(book.getLanguage());
        dto.setPrice(book.getPrice());
        dto.setStock(book.getStock());
        dto.setSynopsis(book.getSynopsis());
        dto.setImageUrl(book.getImageUrl());
        return dto;
    }
}