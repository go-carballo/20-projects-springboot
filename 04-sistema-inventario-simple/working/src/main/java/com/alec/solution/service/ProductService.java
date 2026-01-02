package com.alec.solution.service;

import com.alec.solution.entity.Categoria;
import com.alec.solution.entity.MovimientoStock;
import com.alec.solution.entity.Product;
import com.alec.solution.entity.TipoMovimiento;
import com.alec.solution.repository.MovimientoStockRepository;
import com.alec.solution.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    /**
     * Crea un nuevo producto.
     */
    @CacheEvict(value = {"productos", "productosLowStock"}, allEntries = true)
    public Product crear(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new SkuDuplicadoException("Ya existe un producto con el SKU '" + product.getSku() + "'");
        }
        return productRepository.save(product);
    }

    /**
     * Lista todos los productos activos con paginación.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "productos", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<Product> listarTodos(Pageable pageable) {
        return productRepository.findByActivoTrue(pageable);
    }

    /**
     * Obtiene un producto activo por su ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "productos", key = "#id")
    public Optional<Product> obtenerPorId(Long id) {
        return productRepository.findById(id)
                .filter(Product::getActivo);
    }

    /**
     * Obtiene un producto activo por su SKU.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "productos", key = "'sku-' + #sku")
    public Optional<Product> obtenerPorSku(String sku) {
        return productRepository.findBySkuAndActivoTrue(sku);
    }

    /**
     * Actualiza un producto existente.
     */
    @CacheEvict(value = {"productos", "productosLowStock"}, allEntries = true)
    public Product actualizar(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .filter(Product::getActivo)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto con ID " + id + " no encontrado"));

        product.setNombre(productDetails.getNombre());
        product.setDescripcion(productDetails.getDescripcion());
        product.setStockMinimo(productDetails.getStockMinimo());
        product.setPrecio(productDetails.getPrecio());
        product.setCategoria(productDetails.getCategoria());

        return productRepository.save(product);
    }

    /**
     * Elimina un producto (soft delete).
     */
    @CacheEvict(value = {"productos", "productosLowStock"}, allEntries = true)
    public void eliminar(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::getActivo)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto con ID " + id + " no encontrado"));
        
        product.softDelete();
        productRepository.save(product);
    }

    /**
     * Reactiva un producto eliminado.
     */
    @CacheEvict(value = {"productos", "productosLowStock"}, allEntries = true)
    public Product reactivar(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto con ID " + id + " no encontrado"));
        
        if (product.getActivo()) {
            throw new OperacionInvalidaException("El producto ya está activo");
        }
        
        product.reactivar();
        return productRepository.save(product);
    }

    /**
     * Registra una entrada de stock.
     */
    @CacheEvict(value = {"productos", "productosLowStock"}, allEntries = true)
    public Product entradaStock(Long id, Integer cantidad, String motivo) {
        if (cantidad <= 0) {
            throw new CantidadInvalidaException("La cantidad debe ser mayor a 0");
        }

        Product product = productRepository.findById(id)
                .filter(Product::getActivo)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto con ID " + id + " no encontrado"));

        Integer stockAnterior = product.getCantidad();
        product.setCantidad(stockAnterior + cantidad);
        Product savedProduct = productRepository.save(product);

        // Registrar movimiento
        MovimientoStock movimiento = MovimientoStock.builder()
                .producto(savedProduct)
                .tipo(TipoMovimiento.ENTRADA)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(savedProduct.getCantidad())
                .motivo(motivo)
                .build();
        movimientoStockRepository.save(movimiento);

        return savedProduct;
    }

    /**
     * Registra una salida de stock.
     */
    @CacheEvict(value = {"productos", "productosLowStock"}, allEntries = true)
    public Product salidaStock(Long id, Integer cantidad, String motivo) {
        if (cantidad <= 0) {
            throw new CantidadInvalidaException("La cantidad debe ser mayor a 0");
        }

        Product product = productRepository.findById(id)
                .filter(Product::getActivo)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto con ID " + id + " no encontrado"));

        if (product.getCantidad() < cantidad) {
            throw new StockInsuficienteException(
                    "Stock insuficiente. Stock actual: " + product.getCantidad() + ", Cantidad solicitada: " + cantidad);
        }

        Integer stockAnterior = product.getCantidad();
        product.setCantidad(stockAnterior - cantidad);
        Product savedProduct = productRepository.save(product);

        // Registrar movimiento
        MovimientoStock movimiento = MovimientoStock.builder()
                .producto(savedProduct)
                .tipo(TipoMovimiento.SALIDA)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(savedProduct.getCantidad())
                .motivo(motivo)
                .build();
        movimientoStockRepository.save(movimiento);

        return savedProduct;
    }

    /**
     * Registra un ajuste de inventario.
     */
    @CacheEvict(value = {"productos", "productosLowStock"}, allEntries = true)
    public Product ajusteStock(Long id, Integer nuevaCantidad, String motivo) {
        if (nuevaCantidad < 0) {
            throw new CantidadInvalidaException("La cantidad no puede ser negativa");
        }

        Product product = productRepository.findById(id)
                .filter(Product::getActivo)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto con ID " + id + " no encontrado"));

        Integer stockAnterior = product.getCantidad();
        product.setCantidad(nuevaCantidad);
        Product savedProduct = productRepository.save(product);

        // Registrar movimiento de ajuste
        MovimientoStock movimiento = MovimientoStock.builder()
                .producto(savedProduct)
                .tipo(TipoMovimiento.AJUSTE)
                .cantidad(Math.abs(nuevaCantidad - stockAnterior))
                .stockAnterior(stockAnterior)
                .stockNuevo(nuevaCantidad)
                .motivo(motivo != null ? motivo : "Ajuste de inventario")
                .build();
        movimientoStockRepository.save(movimiento);

        return savedProduct;
    }

    /**
     * Obtiene productos con stock bajo.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "productosLowStock")
    public List<Product> obtenerProductosConStockBajo() {
        return productRepository.findProductsWithLowStock();
    }

    /**
     * Busca productos por nombre con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Product> buscarPorNombre(String nombre, Pageable pageable) {
        return productRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre, pageable);
    }

    /**
     * Filtra productos por rango de precio con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Product> filtrarPorPrecio(BigDecimal min, BigDecimal max, Pageable pageable) {
        return productRepository.findByPrecioBetweenAndActivoTrue(min, max, pageable);
    }

    /**
     * Filtra productos por categoría con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Product> filtrarPorCategoria(Categoria categoria, Pageable pageable) {
        return productRepository.findByCategoriaAndActivoTrue(categoria, pageable);
    }

    /**
     * Búsqueda avanzada con filtros opcionales.
     */
    @Transactional(readOnly = true)
    public Page<Product> buscarConFiltros(String nombre, Categoria categoria, 
                                          BigDecimal precioMin, BigDecimal precioMax, 
                                          Pageable pageable) {
        return productRepository.buscarConFiltros(nombre, categoria, precioMin, precioMax, pageable);
    }

    /**
     * Obtiene el historial de movimientos de un producto.
     */
    @Transactional(readOnly = true)
    public Page<MovimientoStock> obtenerHistorialMovimientos(Long productoId, Pageable pageable) {
        // Verificar que el producto existe
        productRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto con ID " + productoId + " no encontrado"));
        
        return movimientoStockRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId, pageable);
    }

    /**
     * Obtiene movimientos en un rango de fechas.
     */
    @Transactional(readOnly = true)
    public Page<MovimientoStock> obtenerMovimientosPorFechas(LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        return movimientoStockRepository.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(desde, hasta, pageable);
    }

    /**
     * Obtiene movimientos por tipo.
     */
    @Transactional(readOnly = true)
    public Page<MovimientoStock> obtenerMovimientosPorTipo(TipoMovimiento tipo, Pageable pageable) {
        return movimientoStockRepository.findByTipoOrderByFechaMovimientoDesc(tipo, pageable);
    }

    // Excepciones personalizadas
    public static class ProductoNoEncontradoException extends RuntimeException {
        public ProductoNoEncontradoException(String message) {
            super(message);
        }
    }

    public static class SkuDuplicadoException extends RuntimeException {
        public SkuDuplicadoException(String message) {
            super(message);
        }
    }

    public static class CantidadInvalidaException extends RuntimeException {
        public CantidadInvalidaException(String message) {
            super(message);
        }
    }

    public static class StockInsuficienteException extends RuntimeException {
        public StockInsuficienteException(String message) {
            super(message);
        }
    }

    public static class OperacionInvalidaException extends RuntimeException {
        public OperacionInvalidaException(String message) {
            super(message);
        }
    }
}
