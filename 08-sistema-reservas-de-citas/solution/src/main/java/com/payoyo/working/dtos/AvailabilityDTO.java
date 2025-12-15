package com.payoyo.working.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para mostrar la disponibilidad de horarios en una fecha específica.
 * 
 * Calcula y devuelve los slots de tiempo disponibles y ocupados,
 * facilitando al cliente la visualización de horarios libres para reservar.
 * 
 * Este DTO se usa en:
 * - GET /api/appointments/availability/{fecha}
 * 
 * Algoritmo de cálculo (realizado en Service):
 * 1. Generar todos los slots de 30 min entre 08:00 y 20:00 (24 slots)
 * 2. Obtener citas activas (PENDIENTE o CONFIRMADA) de la fecha
 * 3. Marcar slots ocupados por las citas
 * 4. Devolver disponibles = todos - ocupados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDTO {
    
    /**
     * Fecha consultada.
     */
    private LocalDate fecha;
    
    /**
     * Lista de horarios disponibles para reservar.
     * 
     * Formato de cada elemento: "HH:mm - HH:mm"
     * Ejemplos: ["08:00 - 08:30", "08:30 - 09:00", "11:00 - 11:30"]
     * 
     * Representa bloques de 30 minutos que NO tienen citas asignadas.
     * El cliente puede elegir cualquiera de estos horarios.
     */
    private List<String> horariosDisponibles;
    
    /**
     * Lista de horarios ya ocupados por citas.
     * 
     * Formato: Puede variar según la duración de cada cita.
     * Ejemplos: 
     * - Cita de 1 hora: "10:00 - 11:00"
     * - Cita de 30 min: "14:00 - 14:30"
     * - Cita de 2 horas: "15:00 - 17:00"
     * 
     * Muestra al cliente qué franjas están reservadas.
     */
    private List<String> horariosOcupados;
    
    /**
     * Contador de horarios disponibles.
     * 
     * Facilita al frontend mostrar métricas:
     * - "23 de 24 horarios disponibles"
     * - "Alta disponibilidad" vs "Baja disponibilidad"
     * 
     * Valor máximo teórico: 24 (si no hay ninguna cita)
     * Valor mínimo: 0 (si todas las citas ocupan los 24 slots)
     */
    private Integer totalDisponibles;
}
