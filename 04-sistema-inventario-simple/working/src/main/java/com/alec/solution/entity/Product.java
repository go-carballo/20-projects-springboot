package com.alec.solution.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidad que representa un producto del inventario")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del producto", example = "1")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    @Column(nullable = false)
    @Schema(description = "Nombre del producto", example = "Laptop HP Pavilion")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Schema(description = "Descripción detallada del producto", example = "Laptop con procesador Intel i7, 16GB RAM")
    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    @Column(nullable = false)
    @Schema(description = "Cantidad actual en stock", example = "50")
    private Integer cantidad;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(nullable = false)
    @Schema(description = "Cantidad mínima antes de alerta de reabastecimiento", example = "10")
    private Integer stockMinimo;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    @Schema(description = "Precio unitario del producto", example = "1299.99")
    private BigDecimal precio;

    @NotBlank(message = "El SKU es obligatorio")
    @Pattern(regexp = "^[A-Z]{3}-\\d{4}$", message = "El SKU debe tener el formato: 3 letras mayúsculas, guión, 4 números (ej: PRD-0001)")
    @Column(nullable = false, unique = true, length = 8)
    @Schema(description = "Código SKU único del producto", example = "PRD-0001")
    private String sku;

    @NotNull(message = "La categoría es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(description = "Categoría del producto", example = "ELECTRONICA")
    private Categoria categoria;

    @Column(nullable = false)
    @Schema(description = "Indica si el producto está activo (soft delete)", example = "true")
    @Builder.Default
    private Boolean activo = true;

    @Column(nullable = false)
    @Schema(description = "Fecha y hora de la última actualización")
    private LocalDateTime ultimaActualizacion;

    @Column
    @Schema(description = "Fecha y hora de eliminación lógica")
    private LocalDateTime fechaEliminacion;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Schema(hidden = true)
    @Builder.Default
    private List<MovimientoStock> movimientos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        ultimaActualizacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ultimaActualizacion = LocalDateTime.now();
    }

    /**
     * Campo calculado que indica si el producto requiere reabastecimiento.
     * Es true cuando la cantidad actual es menor que el stock mínimo.
     */
    @Schema(description = "Indica si el producto requiere reabastecimiento", example = "false")
    public boolean isRequiereReabastecimiento() {
        return cantidad != null && stockMinimo != null && cantidad < stockMinimo;
    }

    /**
     * Realiza el soft delete del producto.
     */
    public void softDelete() {
        this.activo = false;
        this.fechaEliminacion = LocalDateTime.now();
    }

    /**
     * Reactiva un producto eliminado.
     */
    public void reactivar() {
        this.activo = true;
        this.fechaEliminacion = null;
    }
}
