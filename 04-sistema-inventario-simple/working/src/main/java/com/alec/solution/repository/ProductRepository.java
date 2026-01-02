package com.alec.solution.repository;

import com.alec.solution.entity.Categoria;
import com.alec.solution.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Busca un producto por su SKU (solo activos).
     */
    Optional<Product> findBySkuAndActivoTrue(String sku);

    /**
     * Busca un producto por su SKU (incluye inactivos).
     */
    Optional<Product> findBySku(String sku);

    /**
     * Verifica si existe un producto con el SKU dado.
     */
    boolean existsBySku(String sku);

    /**
     * Lista todos los productos activos con paginación.
     */
    Page<Product> findByActivoTrue(Pageable pageable);

    /**
     * Busca productos activos cuyo nombre contenga el texto dado (case insensitive) con paginación.
     */
    Page<Product> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre, Pageable pageable);

    /**
     * Busca productos activos con stock bajo (cantidad < stockMinimo).
     */
    @Query("SELECT p FROM Product p WHERE p.cantidad < p.stockMinimo AND p.activo = true")
    List<Product> findProductsWithLowStock();

    /**
     * Busca productos activos dentro de un rango de precios con paginación.
     */
    Page<Product> findByPrecioBetweenAndActivoTrue(BigDecimal min, BigDecimal max, Pageable pageable);

    /**
     * Busca productos activos por categoría con paginación.
     */
    Page<Product> findByCategoriaAndActivoTrue(Categoria categoria, Pageable pageable);

    /**
     * Búsqueda avanzada con filtros opcionales y paginación.
     */
    @Query("SELECT p FROM Product p WHERE p.activo = true " +
           "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "AND (:categoria IS NULL OR p.categoria = :categoria) " +
           "AND (:precioMin IS NULL OR p.precio >= :precioMin) " +
           "AND (:precioMax IS NULL OR p.precio <= :precioMax)")
    Page<Product> buscarConFiltros(
            @Param("nombre") String nombre,
            @Param("categoria") Categoria categoria,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax,
            Pageable pageable);
}
