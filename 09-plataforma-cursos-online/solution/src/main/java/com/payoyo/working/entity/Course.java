package com.payoyo.working.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa un curso online en la plataforma.
 * Incluye información del curso, precio con descuentos, estadísticas de inscripciones
 * y calificaciones de usuarios.
 */
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título del curso - debe ser único en la plataforma.
     * Se valida con constraint UNIQUE a nivel de base de datos.
     */
    @Column(nullable = false, unique = true, length = 200)
    private String title;

    /**
     * Descripción detallada del contenido del curso.
     * Máximo 2000 caracteres para descripción completa.
     */
    @Column(nullable = false, length = 2000)
    private String description;

    /**
     * Nombre del instructor que imparte el curso.
     */
    @Column(nullable = false, length = 150)
    private String instructor;

    /**
     * Duración total del curso expresada en horas.
     * Debe ser un valor positivo entre 1 y 500 horas.
     */
    @Column(nullable = false)
    private Integer durationHours;

    /**
     * Nivel de dificultad del curso.
     * Se almacena como STRING en lugar de ordinal para legibilidad en la base de datos
     * y para evitar problemas si se reordenan los valores del enum.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Level level;

    /**
     * Precio base del curso en la moneda de la plataforma.
     * Se usa BigDecimal para precisión en cálculos monetarios.
     * precision=10 permite números hasta 99,999,999.99
     * scale=2 mantiene exactamente 2 decimales
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Porcentaje de descuento aplicado al precio base (0-100).
     * El precio final se calcula en tiempo de ejecución.
     */
    @Column(nullable = false)
    private Integer discount = 0;

    /**
     * Categoría temática del curso (ej: Backend, Frontend, Data Science).
     * Permite agrupar cursos por áreas de conocimiento.
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * URL del video promocional del curso.
     * Campo opcional para marketing del curso.
     */
    @Column(length = 500)
    private String videoUrl;

    /**
     * URL de la imagen miniatura del curso.
     * Se muestra en listados y tarjetas de cursos.
     */
    @Column(length = 500)
    private String thumbnail;

    /**
     * Fecha en que el curso fue publicado en la plataforma.
     * Se puede establecer en el futuro para cursos programados.
     */
    private LocalDate publishedDate;

    /**
     * Contador de estudiantes inscritos en el curso.
     * Se incrementa con cada inscripción nueva.
     */
    @Column(nullable = false)
    private Integer enrolledStudents = 0;

    /**
     * Calificación promedio del curso (0.0 a 5.0).
     * Se recalcula cuando los usuarios envían nuevas valoraciones.
     * En producción, esto debería calcularse desde una tabla de ratings.
     */
    @Column(nullable = false)
    private Double averageRating = 0.0;

    // ========== Constructores ==========

    /**
     * Constructor vacío requerido por JPA.
     */
    public Course() {
    }

    /**
     * Constructor con campos obligatorios.
     * Los campos con valores por defecto se inicializan automáticamente.
     */
    public Course(String title, String description, String instructor, 
                  Integer durationHours, Level level, BigDecimal price, String category) {
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.durationHours = durationHours;
        this.level = level;
        this.price = price;
        this.category = category;
    }

    // ========== Getters y Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public Integer getEnrolledStudents() {
        return enrolledStudents;
    }

    public void setEnrolledStudents(Integer enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    // ========== toString para debugging ==========

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", instructor='" + instructor + '\'' +
                ", level=" + level +
                ", price=" + price +
                ", discount=" + discount +
                ", category='" + category + '\'' +
                ", enrolledStudents=" + enrolledStudents +
                ", averageRating=" + averageRating +
                '}';
    }
}