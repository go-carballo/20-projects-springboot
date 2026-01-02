package com.alec.solution.mapper;

import com.alec.solution.dto.*;
import com.alec.solution.entity.MovimientoStock;
import com.alec.solution.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades y DTOs.
 * Proporciona conversiones bidireccionales para Product y MovimientoStock.
 */
@Component
public class ProductMapper {

    /**
     * Convierte un ProductCreateRequest a entidad Product.
     */
    public Product toEntity(ProductCreateRequest request) {
        return Product.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .cantidad(request.cantidad())
                .stockMinimo(request.stockMinimo())
                .precio(request.precio())
                .sku(request.sku())
                .categoria(request.categoria())
                .build();
    }

    /**
     * Actualiza una entidad Product con datos de ProductUpdateRequest.
     */
    public void updateEntity(Product product, ProductUpdateRequest request) {
        product.setNombre(request.nombre());
        product.setDescripcion(request.descripcion());
        product.setStockMinimo(request.stockMinimo());
        product.setPrecio(request.precio());
        product.setCategoria(request.categoria());
    }

    /**
     * Convierte una entidad Product a ProductResponse.
     */
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getNombre(),
                product.getDescripcion(),
                product.getCantidad(),
                product.getStockMinimo(),
                product.getPrecio(),
                product.getSku(),
                product.getCategoria(),
                product.getActivo(),
                product.isRequiereReabastecimiento(),
                product.getUltimaActualizacion()
        );
    }

    /**
     * Convierte una entidad MovimientoStock a MovimientoStockResponse.
     */
    public MovimientoStockResponse toResponse(MovimientoStock movimiento) {
        return new MovimientoStockResponse(
                movimiento.getId(),
                movimiento.getProducto().getId(),
                movimiento.getProducto().getNombre(),
                movimiento.getProducto().getSku(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockNuevo(),
                movimiento.getMotivo(),
                movimiento.getFechaMovimiento()
        );
    }
}
