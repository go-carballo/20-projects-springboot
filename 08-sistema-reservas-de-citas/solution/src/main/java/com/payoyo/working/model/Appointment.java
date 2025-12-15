package com.payoyo.working.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una cita/reserva en el sistema.
 * 
 * Gestiona las reservas de servicios con validación de horarios,
 * generación automática de códigos de confirmación y control de estados.
 */
@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre completo del cliente que reserva la cita.
     * Campo obligatorio para identificación.
     */
    @Column(nullable = false, length = 100)
    private String nombreCliente;

    /**
     * Email de contacto del cliente.
     * Se usa para notificaciones y búsqueda de citas.
     */
    @Column(nullable = false, length = 100)
    private String email;  

    /**
     * Teléfono de contacto del cliente.
     * Formato flexible para soportar diferentes países.
     */
    @Column(nullable = false, length = 20)
    private String telefono;

    /**
     * Fecha de la cita (día).
     * No puede ser anterior a la fecha actual.
     */
    @Column(nullable = false)
    private LocalDate fecha;

    /**
     * Hora de inicio del servicio.
     * Debe estar dentro del horario laboral (08:00 - 20:00).
     */
    @Column(nullable = false)
    private LocalTime horaInicio;

    /**
     * Hora de finalización del servicio.
     * Debe ser posterior a horaInicio con duración entre 15 min y 8 horas.
     */
    @Column(nullable = false)
    private LocalTime horaFin;

    /**
     * Tipo de servicio solicitado.
     * Ejemplos: "Consulta Médica", "Fisioterapia", "Revisión Dental".
     */
    @Column(nullable = false, length = 100)
    private String servicio;

    /**
     * Estado actual de la cita.
     * 
     * @Enumerated(STRING) almacena el nombre del enum en BD (más legible)
     * vs ORDINAL que almacenaría el índice numérico (frágil ante reordenamientos).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    /**
     * Precio del servicio en euros.
     * 
     * BigDecimal se usa para evitar errores de precisión en cálculos monetarios
     * que ocurrirían con float/double.
     * precision=10: hasta 10 dígitos totales
     * scale=2: 2 dígitos decimales (centavos)
     * Ejemplo: 9999999.99
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /**
     * Notas adicionales sobre la cita (opcional).
     * Puede contener instrucciones especiales, historial previo, etc.
     */
    @Column(length = 500)
    private String notas;

    /**
     * Código único de confirmación generado automáticamente.
     * Formato: "APT-XXXX" donde XXXX son 4 caracteres alfanuméricos.
     * 
     * unique=true garantiza unicidad a nivel de base de datos,
     * previniendo duplicados incluso en entornos concurrentes.
     */
    @Column(unique = true, nullable = false, length = 10)
    private String codigoConfirmacion;

    /**
     * Timestamp de creación del registro.
     * 
     * @CreationTimestamp genera automáticamente la fecha/hora al insertar.
     * updatable=false previene modificaciones accidentales en actualizaciones.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp de última actualización del registro.
     * 
     * @UpdateTimestamp actualiza automáticamente en cada modificación.
     * Útil para auditoría y tracking de cambios.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
