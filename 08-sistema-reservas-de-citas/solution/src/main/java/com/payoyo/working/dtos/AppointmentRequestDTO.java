package com.payoyo.working.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir datos de creación/actualización de citas.
 * 
 * Incluye validaciones Bean Validation y validaciones personalizadas
 * para garantizar coherencia de datos antes de llegar a la capa de servicio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequestDTO {
    
    /**
     * Nombre completo del cliente.
     * Debe tener entre 2 y 100 caracteres.
     */
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombreCliente;

    /**
     * Email del cliente.
     * Debe cumplir el formato estandar de email
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato de email es inválido")
    private String email;

    /**
     * Teléfono del cliente.
     * 
     * Pattern acepta formatos internacionales:
     * - Con o sin prefijo internacional (+34, +1, etc.)
     * - Espacios y guiones opcionales
     * - Entre 9 y 15 dígitos totales
     * 
     * Ejemplos válidos: "+34 612 345 678", "612345678", "+1-555-123-4567"
     */
    @NotBlank(message = "El telefono es obligatorio")
    @Pattern(
        regexp = "^\\+?[0-9\\s-]{9,15}$",
        message = "El formato del teléfono es inválido"
    )
    private String telefono;

    /**
     * Fecha de la cita.
     * 
     * @FutureOrPresent permite el día actual o fechas futuras,
     * pero no fechas pasadas (protege contra errores de entrada).
     */
    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser anterior a hoy")
    private LocalDate fecha;

    /**
     * Hora de inicio de la cita.
     * La validación de horario laboral (08:00-20:00) se realiza en Service.
     */
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;
    
    /**
     * Hora de finalización de la cita.
     * Debe ser posterior a horaInicio (validado en método personalizado).
     */
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    /**
     * Tipo de servicio solicitado.
     */
    @NotBlank(message = "El servicio es obligatorio")
    @Size(min = 2, max = 100, message = "El servicio debe tener entre 2 y 100 caracteres")
    private String servicio;

    /**
     * Precio del servicio.
     * 
     * @DecimalMin("0.0") permite servicios gratuitos pero no precios negativos.
     * inclusive=true significa que 0.0 es válido.
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precio;
    
    /**
     * Notas adicionales (opcional).
     * Máximo 500 caracteres para evitar textos excesivamente largos.
     */
    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notas;


    // ==================== VALIDACIONES PERSONALIZADAS ====================
    
    /**
     * Validación cruzada: La hora de fin debe ser posterior a la hora de inicio.
     * 
     * @AssertTrue se evalúa después de las validaciones individuales (@NotNull).
     * Si horaInicio o horaFin son null, devuelve true para que @NotNull maneje el error,
     * evitando NullPointerException.
     * 
     * @return true si la validación pasa, false si falla
     */
    @AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
    public boolean isHoraFinPosterior() {
        if (horaInicio == null || horaFin == null) {
            return true; // dejamos que @NotNull maneje los nulls
        }
        return horaFin.isAfter(horaInicio);
    }

    /**
     * Validación de duración de la cita.
     * 
     * Reglas de negocio:
     * - Duración mínima: 15 minutos (evita citas inútilmente cortas)
     * - Duración máxima: 8 horas = 480 minutos (jornada laboral completa)
     * 
     * ChronoUnit.MINUTES.between() calcula la diferencia exacta en minutos.
     * 
     * @return true si la duración está en el rango válido
     */
    @AssertTrue(message = "La duracion de la cita debe estar entre 15 minutos y 8 horas")
    public boolean isDuracionValida() {
        if (horaInicio == null || horaFin == null) {
            return true;
        }

        long duracionMinutos = ChronoUnit.MINUTES.between(horaInicio, horaFin);
        return duracionMinutos >= 15 && duracionMinutos <= 480; // 15 min a 8 horas
    }
}
