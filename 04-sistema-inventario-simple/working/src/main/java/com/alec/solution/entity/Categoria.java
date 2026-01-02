package com.alec.solution.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categorías de productos disponibles")
public enum Categoria {
    
    @Schema(description = "Productos electrónicos")
    ELECTRONICA,
    
    @Schema(description = "Ropa y accesorios")
    ROPA,
    
    @Schema(description = "Artículos para el hogar")
    HOGAR,
    
    @Schema(description = "Alimentos y bebidas")
    ALIMENTOS,
    
    @Schema(description = "Artículos deportivos")
    DEPORTES,
    
    @Schema(description = "Libros y material educativo")
    LIBROS,
    
    @Schema(description = "Juguetes y entretenimiento")
    JUGUETES,
    
    @Schema(description = "Salud y cuidado personal")
    SALUD,
    
    @Schema(description = "Herramientas y ferretería")
    HERRAMIENTAS,
    
    @Schema(description = "Otros productos no categorizados")
    OTROS
}
