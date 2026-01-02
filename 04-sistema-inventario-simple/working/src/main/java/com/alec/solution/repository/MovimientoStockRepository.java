package com.alec.solution.repository;

import com.alec.solution.entity.MovimientoStock;
import com.alec.solution.entity.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    /**
     * Obtiene todos los movimientos de un producto con paginación.
     */
    Page<MovimientoStock> findByProductoIdOrderByFechaMovimientoDesc(Long productoId, Pageable pageable);

    /**
     * Obtiene todos los movimientos de un producto.
     */
    List<MovimientoStock> findByProductoIdOrderByFechaMovimientoDesc(Long productoId);

    /**
     * Obtiene movimientos por tipo con paginación.
     */
    Page<MovimientoStock> findByTipoOrderByFechaMovimientoDesc(TipoMovimiento tipo, Pageable pageable);

    /**
     * Obtiene movimientos en un rango de fechas con paginación.
     */
    Page<MovimientoStock> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(
            LocalDateTime desde, LocalDateTime hasta, Pageable pageable);

    /**
     * Obtiene movimientos de un producto en un rango de fechas.
     */
    @Query("SELECT m FROM MovimientoStock m WHERE m.producto.id = :productoId " +
           "AND m.fechaMovimiento BETWEEN :desde AND :hasta " +
           "ORDER BY m.fechaMovimiento DESC")
    Page<MovimientoStock> findByProductoAndFechasBetween(
            @Param("productoId") Long productoId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable);

    /**
     * Cuenta movimientos por tipo para un producto.
     */
    long countByProductoIdAndTipo(Long productoId, TipoMovimiento tipo);
}
