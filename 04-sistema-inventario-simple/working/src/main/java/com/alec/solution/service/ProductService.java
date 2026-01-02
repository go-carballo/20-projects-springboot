package com.alec.solution.service;

import com.alec.solution.entity.Categoria;
import com.alec.solution.entity.MovimientoStock;
import com.alec.solution.entity.Product;
import com.alec.solution.entity.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de productos del inventario.
 */
public interface ProductService {

    // ==================== CRUD ====================

    /**
     * Crea un nuevo producto.
     */
    Product crear(Product product);

    /**
     * Lista todos los productos activos con paginación.
     */
    Page<Product> listarTodos(Pageable pageable);

    /**
     * Obtiene un producto activo por su ID.
     */
    Optional<Product> obtenerPorId(Long id);

    /**
     * Obtiene un producto activo por su SKU.
     */
    Optional<Product> obtenerPorSku(String sku);

    /**
     * Actualiza un producto existente.
     */
    Product actualizar(Long id, Product productDetails);

    /**
     * Elimina un producto (soft delete).
     */
    void eliminar(Long id);

    /**
     * Reactiva un producto eliminado.
     */
    Product reactivar(Long id);

    // ==================== STOCK ====================

    /**
     * Registra una entrada de stock.
     */
    Product entradaStock(Long id, Integer cantidad, String motivo);

    /**
     * Registra una salida de stock.
     */
    Product salidaStock(Long id, Integer cantidad, String motivo);

    /**
     * Registra un ajuste de inventario.
     */
    Product ajusteStock(Long id, Integer nuevaCantidad, String motivo);

    /**
     * Obtiene productos con stock bajo.
     */
    List<Product> obtenerProductosConStockBajo();

    // ==================== BÚSQUEDAS ====================

    /**
     * Busca productos por nombre con paginación.
     */
    Page<Product> buscarPorNombre(String nombre, Pageable pageable);

    /**
     * Filtra productos por rango de precio con paginación.
     */
    Page<Product> filtrarPorPrecio(BigDecimal min, BigDecimal max, Pageable pageable);

    /**
     * Filtra productos por categoría con paginación.
     */
    Page<Product> filtrarPorCategoria(Categoria categoria, Pageable pageable);

    /**
     * Búsqueda avanzada con filtros opcionales.
     */
    Page<Product> buscarConFiltros(String nombre, Categoria categoria,
                                   BigDecimal precioMin, BigDecimal precioMax,
                                   Pageable pageable);

    // ==================== HISTORIAL ====================

    /**
     * Obtiene el historial de movimientos de un producto.
     */
    Page<MovimientoStock> obtenerHistorialMovimientos(Long productoId, Pageable pageable);

    /**
     * Obtiene movimientos en un rango de fechas.
     */
    Page<MovimientoStock> obtenerMovimientosPorFechas(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);

    /**
     * Obtiene movimientos por tipo.
     */
    Page<MovimientoStock> obtenerMovimientosPorTipo(TipoMovimiento tipo, Pageable pageable);

    // ==================== EXCEPCIONES ====================

    class ProductoNoEncontradoException extends RuntimeException {
        public ProductoNoEncontradoException(String message) {
            super(message);
        }
    }

    class SkuDuplicadoException extends RuntimeException {
        public SkuDuplicadoException(String message) {
            super(message);
        }
    }

    class CantidadInvalidaException extends RuntimeException {
        public CantidadInvalidaException(String message) {
            super(message);
        }
    }

    class StockInsuficienteException extends RuntimeException {
        public StockInsuficienteException(String message) {
            super(message);
        }
    }

    class OperacionInvalidaException extends RuntimeException {
        public OperacionInvalidaException(String message) {
            super(message);
        }
    }
}
