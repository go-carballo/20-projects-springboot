package com.payoyo.working.dtos;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

import com.payoyo.working.model.Book;

/**
 * DTO para creación y actualización de libros.
 * 
 * Propósito: Validar los datos de entrada antes de persistirlos.
 * Contexto de uso:
 * - POST /api/books (crear nuevo libro)
 * - PUT /api/books/{isbn} (actualizar libro completo)
 * 
 * Características:
 * - Validaciones Jakarta (Bean Validation)
 * - Mensajes de error personalizados en español
 * - Reutilizable para POST y PUT
 * - Se convierte a Entity mediante toEntity()
 * 
 * Ventajas de usar DTO de entrada:
 * - Validación antes de llegar a Service/Repository
 * - Mensajes claros al cliente sobre errores
 * - Desacopla API de estructura de base de datos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BookCreateDTO {

    /**
     * ISBN del libro - Identificador único.
     * 
     * Formato típico: 978-0-134-68599-1 (13 dígitos con guiones)
     */
    @NotBlank(message = "El ISBN es obligatorio")
    private String isbn;

    /**
     * Título del libro.
     * 
     * Validaciones:
     * - No puede estar vacío
     * - Máximo 200 caracteres
     */
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede exceder los 200 caracteres")
    private String title;

    /**
     * Nombre del autor del libro.
     * 
     * Validaciones:
     * - No puede estar vacío
     * - Máximo 100 caracteres
     */
    @NotBlank(message = "El autor es obligatorio")
    @Size(max = 100, message = "El nombre del autor no puede exceder los 100 caracteres")
    private String author;

    /**
     * Editorial que publicó el libro.
     * 
     * Campo OPCIONAL - Puede ser null.
     * Solo se valida el tamaño máximo si se proporciona.
     */
    @Size(max = 100, message = "El nombre de la editorial no puede exceder los 100 caracteres")
    private String publisher;

    /**
     * Año de publicación del libro.
     * 
     * Validaciones:
     * - Campo obligatorio
     * - Mínimo: 1450 (invención de la imprenta de Gutenberg)
     * - Máximo: 2100 (límite razonable para futuros lanzamientos)
     */
    @NotNull(message = "El año de publicación es obligatorio")
    @Min(value = 1450, message = "El año de publicación debe ser posterior a 1450")
    @Max(value = 2100, message = "El año de publicación no puede ser posterior a 2100")
    private Integer publicationYear;

    /**
     * Número de páginas del libro.
     * 
     * Validaciones:
     * - Campo obligatorio
     * - Debe ser un número positivo (mínimo 1)
     */
    @NotNull(message = "El número de páginas es obligatorio")
    @Positive(message = "El número de páginas debe ser mayor que cero")
    private Integer pages;

    /**
     * Idioma del libro en formato ISO 639-1.
     * 
     * Código de 2 letras: es (español), en (inglés), fr (francés), etc.
     * 
     * Validaciones:
     * - Campo obligatorio
     * - Exactamente 2 caracteres
     */
    @NotBlank(message = "El idioma es obligatorio")
    @Size(min = 2, max = 2, message = "El idioma debe ser un código ISO 639-1 de 2 caracteres (ej: es, en, fr)")
    private String language;

    /**
     * Precio de venta del libro.
     * 
     * Validaciones:
     * - Campo obligatorio
     * - Mínimo: 0.01 (al menos 1 céntimo)
     * 
     * Nota: Se usa BigDecimal para evitar problemas de redondeo en operaciones monetarias
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser al menos 0.01")
    private BigDecimal price;

    /**
     * Cantidad disponible en inventario.
     * 
     * Validaciones:
     * - Campo obligatorio
     * - Mínimo: 0 (no puede ser negativo)
     */
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    /**
     * Sinopsis o descripción del libro.
     * 
     * Campo OPCIONAL - Puede ser null.
     * Máximo 1000 caracteres para descripciones razonables.
     */
    @Size(max = 1000, message = "La sinopsis no puede exceder los 1000 caracteres")
    private String synopsis;

    /**
     * URL de la imagen de portada del libro.
     * 
     * Campo OPCIONAL - Puede ser null.
     * 
     * Validaciones:
     * - Si se proporciona, debe ser una URL válida (http:// o https://)
     */
    @Pattern(
        regexp = "^https?://.*", 
        message = "La URL de la imagen debe ser válida y comenzar con http:// o https://"
    )
    private String imageUrl;

    /**
     * Convierte este DTO a una entidad Book para persistir en base de datos.
     * 
     * Patrón de conversión DTO → Entity.
     * 
     * Este método es usado en Service al crear o actualizar libros:
     * <pre>
     * Book book = bookCreateDTO.toEntity();
     * bookRepository.save(book);
     * </pre>
     * 
     * Nota importante:
     * - Los campos opcionales (publisher, synopsis, imageUrl) pueden ser null
     * - El stock tiene valor por defecto 0 en la entidad si no se proporciona aquí
     * 
     * @return Instancia de Book lista para persistir
     */
    public Book toEntity() {
        Book book = new Book();
        book.setIsbn(this.isbn);
        book.setTitle(this.title);
        book.setAuthor(this.author);
        book.setPublisher(this.publisher);
        book.setPublicationYear(this.publicationYear);
        book.setPages(this.pages);
        book.setLanguage(this.language);
        book.setPrice(this.price);
        book.setStock(this.stock);
        book.setSynopsis(this.synopsis);
        book.setImageUrl(this.imageUrl);
        return book;
    }
}
