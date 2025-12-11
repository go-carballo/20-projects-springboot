package com.payoyo.working.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad que representa un libro en el catálogo.
 * 
 * Características principales:
 * - ISBN como identificador primario (String, NO autoincremental)
 * - Validaciones JPA a nivel de base de datos
 * - BigDecimal para manejo preciso de precios
 * - Campos opcionales: publisher, synopsis, imageUrl
 */
@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"synopsis", "imageUrl"})
public class Book {
    
    /**
     * ISBN (International Standard Book Number) - Identificador único del libro.
     * 
     * Decisión técnica: 
     * - String porque ISBN contiene guiones (978-0-134-68599-1)
     * - @Id sin @GeneratedValue porque ISBN viene del exterior
     * - length=20 para soportar formato completo con guiones
     */
    @Id
    @Column(length = 20)
    private String isbn;

    /**
     * Título del libro.
     * 
     * Validaciones:
     * - @NotNull: No puede ser null a nivel de JPA
     * - @NotBlank: No puede ser vacío o solo espacios (Bean Validation)
     * - @Size(max=200): Límite de caracteres
     * - nullable=false: Constraint a nivel de base de datos
     */
    @NotNull
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Nombre del autor del libro.
     * 
     * Validaciones similares a title pero con límite de 100 caracteres.
     */
    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String author;

    /**
     * Editorial que publicó el libro.
     * 
     * Campo OPCIONAL - Puede ser null.
     * No lleva @NotNull ni @NotBlank, solo restricción de tamaño.
     */
    @Size(max = 100)
    @Column(length = 100)
    private String publisher;

    /**
     * Año de publicación del libro.
     * 
     * Validaciones:
     * - @NotNull: Campo obligatorio
     * - @Min(1450): Año de invención de la imprenta por Gutenberg
     * - @Max(2100): Límite razonable para libros futuros
     * 
     * Decisión técnica: Integer (no int) para permitir null en validaciones
     */
    @NotNull
    @Min(value = 1450, message = "El año de publicacion no puede ser menor al 1450")
    @Max(value = 2100, message = "El año de publicacion no puede ser superior a 2100")
    @Column(nullable = false)
    private Integer publicationYear;

    /**
     * Número de páginas del libro.
     * 
     * Validaciones:
     * - @NotNull: Campo obligatorio
     * - @Positive: Debe ser mayor que 0 (al menos 1 página)
     */
    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer pages;

    /**
     * Idioma del libro en formato ISO 639-1 (código de 2 letras).
     * 
     * Ejemplos: es (español), en (inglés), fr (francés), de (alemán)
     * 
     * Validaciones:
     * - @NotNull/@NotBlank: Campo obligatorio
     * - @Size(min=2, max=2): Exactamente 2 caracteres
     */
    @NotNull
    @NotBlank
    @Size(min = 2, max = 2)
    @Column(nullable = false, length = 2)
    private String language;

    /**
     * Precio del libro.
     * 
     * Decisión técnica: BigDecimal en lugar de double/float
     * Razón: Evita problemas de redondeo en operaciones monetarias
     * Ejemplo: 0.1 + 0.2 = 0.30000000000000004 (con double)
     *          0.1 + 0.2 = 0.3 (con BigDecimal)
     * 
     * Validaciones:
     * - @NotNull: Campo obligatorio
     * - @DecimalMin("0.01"): Precio mínimo de 1 céntimo
     * - precision=10, scale=2: Hasta 99.999.999,99
     */
    @NotNull
    @DecimalMin(value = "0.01", message = "El precio debe no puede ser menor a 0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Cantidad disponible en inventario.
     * 
     * Validaciones:
     * - @NotNull: Campo obligatorio
     * - @Min(0): No puede ser negativo
     * 
     * Valor por defecto: 0
     * Razón: Si se crea un libro sin especificar stock, asumimos que no hay existencias
     */
    @NotNull
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock = 0;

    /**
     * Sinopsis o descripción breve del libro.
     * 
     * Campo OPCIONAL - Puede ser null.
     * Límite: 1000 caracteres para descripción razonable
     * 
     * Decisión técnica: @Column(length=1000) en lugar de TEXT
     * Razón: H2 y MySQL manejan bien VARCHAR(1000), más eficiente para búsquedas
     */
    @Size(max = 1000)
    @Column(length = 1000)
    private String synopsis;

    /**
     * URL de la imagen de portada del libro.
     * 
     * Campo OPCIONAL - Puede ser null.
     * Límite: 500 caracteres para URLs completas
     * 
     * Nota: La validación de formato URL se hace en el DTO, no aquí
     */
    @Column(length = 500)
    private String imageUrl;
}
