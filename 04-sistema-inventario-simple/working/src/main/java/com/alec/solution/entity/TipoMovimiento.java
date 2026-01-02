package com.alec.solution.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipos de movimiento de stock")
public enum TipoMovimiento {
    
    @Schema(description = "Entrada de stock al inventario")
    ENTRADA,
    
    @Schema(description = "Salida de stock del inventario")
    SALIDA,
    
    @Schema(description = "Ajuste de inventario (corrección)")
    AJUSTE
}
