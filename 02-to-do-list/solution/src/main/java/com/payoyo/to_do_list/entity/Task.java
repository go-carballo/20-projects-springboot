package com.payoyo.to_do_list.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.payoyo.to_do_list.entity.enums.Priority;
import com.payoyo.to_do_list.entity.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Entidad que representa una tarea
 * 
 * Buenas prácticas aplicadas:
 * - Uso de lombok para reducir codigo boilerplate
 * - Validaciones a nivel de entidad
 * - Indices en campos frecuentemente consultados
 * - Metodos de negocio encapsulados en la entidad (DDD)
 * 
 */
@Entity // Indica que esta clase es una entidad JPA que se mapea a una tabla
@Table(name = "tasks", indexes = {
    // Los índices mejoran el rendimiento en consultas por estos campos
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_priority", columnList = "priority"),
    @Index(name = "idx_due_date", columnList = "due_date"),
})
@Getter // Lombok: genera getters automáticamente
@Setter // Lombok: genera setters automáticamente
@NoArgsConstructor // Lombok: genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor // Lombok: genera constructor con todos los argumentos
@Builder // Lombok: implementa el patrón Builder para construcción fluida de objetos
public class Task {
    
    @Id // Marca este campo como clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremento de ID en BD
    private Long id;

    /**
     * Título de la tarea.
     * Validaciones:
     * - No puede estar vacío (@NotBlank)
     * - Entre 3 y 100 caracteres (@Size)
     */
    @NotBlank(message = "El titulo de la tarea es obligatorio")
    @Size(min = 3, max = 100, message = "El titulo debe tener entre 3 y 100 caracteres")
    @Column(nullable = false, length = 100) // Define restricciones a nivel de BD
    private String title;

    /**
     * Descripción detallada de la tarea.
     * Este campo es OPCIONAL (no tiene @NotBlank)
     * Máximo 500 caracteres para mantener concisión
     */
    @Size(max = 500, message = "La descripcion no puede contener mas de 500 caracteres")
    @Column(length = 500)
    private String description;

    /**
     * Estado actual de la tarea (PENDING, IN_PROGRESS, COMPLETED)
     * 
     * @Enumerated(EnumType.STRING): Guarda el nombre del enum en BD (no el ordinal)
     * Esto es mejor porque:
     * - Más legible en la BD
     * - No se rompe si reordenamos el enum
     * 
     * @Builder.Default: Valor por defecto cuando usamos el Builder
     */
    @NotNull(message = "El estado de la tarea es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    /**
     * Prioridad de la tarea (LOW, MEDIUM, HIGH)
     * Por defecto se asigna prioridad MEDIUM
     */
    @NotNull(message = "La prioridad de la tarea es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    /**
     * Fecha límite para completar la tarea.
     * 
     * Usamos LocalDate (no LocalDateTime) porque:
     * - Las fechas límite suelen ser "para ese día", no una hora específica
     * - Más simple de manejar
     * - Coherente con el nombre del campo (dueDate, no dueDateTime)
     * 
     * Este campo es OPCIONAL (puede ser null)
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * Fecha y hora de creación del registro.
     * 
     * @CreationTimestamp: Hibernate asigna automáticamente la fecha al crear
     * updatable = false: Una vez creado, este campo NO se puede modificar
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de última actualización.
     * 
     * @UpdateTimestamp: Hibernate actualiza automáticamente este campo
     * cada vez que se modifica el registro
     * 
     * IMPORTANTE: NO debe tener updatable = false
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // ========== MÉTODOS DE NEGOCIO ==========
    /*
    * Siguiendo Domain-Driven Design (DDD), la lógica relacionada
    * con la entidad vive dentro de la propia entidad
    */

    /**
     * Verifica si la tarea está vencida.
     * Una tarea está vencida si:
     * - Tiene fecha límite definida
     * - La fecha límite ya pasó
     * - NO está completada
     * 
     * @return true si la tarea está vencida
     */
    public boolean isOverdue() {
        return dueDate != null  && LocalDate.now().isAfter(dueDate) && status != Status.COMPLETED;
    }

    /**
     * Verifica si la tarea vence hoy.
     * 
     * @return true si la fecha límite es hoy
     */
    public boolean isDueToday() {
        return dueDate != null && dueDate.equals(LocalDate.now());
    }

    /**
     * Cambia el estado de la tarea a "En Progreso".
     * Encapsula la lógica de cambio de estado.
     */
    public void markAsInProgress() {
        this.status = Status.IN_PROGRESS;
    }

    /**
     * Marca la tarea como completada.
     * Encapsula la lógica de cambio de estado.
     */
    public void markAsCompleted() {
        this.status = Status.COMPLETED;
    }
}
