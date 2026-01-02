package com.alec.solution.controller;

import com.alec.solution.entity.Categoria;
import com.alec.solution.entity.MovimientoStock;
import com.alec.solution.entity.Product;
import com.alec.solution.entity.TipoMovimiento;
import com.alec.solution.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "API para gestión de productos del inventario")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Crear producto", description = "Crea un nuevo producto en el inventario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "SKU duplicado")
    })
    @PostMapping
    public ResponseEntity<Product> crear(@Valid @RequestBody Product product) {
        Product created = productService.crear(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Listar productos", 
               description = "Obtiene todos los productos activos con paginación y ordenamiento. " +
                            "Ordenar por: nombre, precio, cantidad, stockMinimo, ultimaActualizacion")
    @ApiResponse(responseCode = "200", description = "Página de productos obtenida exitosamente")
    @GetMapping
    public ResponseEntity<Page<Product>> listarTodos(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(productService.listarTodos(pageable));
    }

    @Operation(summary = "Obtener producto por ID", description = "Busca un producto activo por su identificador único")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> obtenerPorId(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        return productService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProductService.ProductoNoEncontradoException(
                        "Producto con ID " + id + " no encontrado"));
    }

    @Operation(summary = "Obtener producto por SKU", description = "Busca un producto activo por su código SKU")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/sku/{sku}")
    public ResponseEntity<Product> obtenerPorSku(
            @Parameter(description = "SKU del producto (formato: XXX-0000)") @PathVariable String sku) {
        return productService.obtenerPorSku(sku)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProductService.ProductoNoEncontradoException(
                        "Producto con SKU '" + sku + "' no encontrado"));
    }

    @Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Product> actualizar(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody Product product) {
        Product updated = productService.actualizar(id, product);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar producto (soft delete)", 
               description = "Marca un producto como inactivo sin eliminarlo físicamente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        productService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reactivar producto", description = "Reactiva un producto previamente eliminado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto reactivado exitosamente",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "El producto ya está activo"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PostMapping("/{id}/reactivar")
    public ResponseEntity<Product> reactivar(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        Product reactivated = productService.reactivar(id);
        return ResponseEntity.ok(reactivated);
    }

    @Operation(summary = "Entrada de stock", description = "Registra una entrada de stock para un producto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Cantidad inválida"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PostMapping("/{id}/stock/entrada")
    public ResponseEntity<Product> entradaStock(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @RequestBody StockRequest request) {
        Product updated = productService.entradaStock(id, request.cantidad(), request.motivo());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Salida de stock", description = "Registra una salida de stock para un producto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Cantidad inválida o stock insuficiente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PostMapping("/{id}/stock/salida")
    public ResponseEntity<Product> salidaStock(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @RequestBody StockRequest request) {
        Product updated = productService.salidaStock(id, request.cantidad(), request.motivo());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Ajuste de stock", description = "Realiza un ajuste de inventario estableciendo una cantidad específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock ajustado exitosamente",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Cantidad inválida"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PostMapping("/{id}/stock/ajuste")
    public ResponseEntity<Product> ajusteStock(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @RequestBody AjusteRequest request) {
        Product updated = productService.ajusteStock(id, request.nuevaCantidad(), request.motivo());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Alertas de stock bajo", description = "Obtiene productos con stock por debajo del mínimo")
    @ApiResponse(responseCode = "200", description = "Lista de productos con stock bajo")
    @GetMapping("/alertas")
    public ResponseEntity<?> obtenerAlertas() {
        return ResponseEntity.ok(productService.obtenerProductosConStockBajo());
    }

    @Operation(summary = "Historial de movimientos", description = "Obtiene el historial de movimientos de stock de un producto")
    @ApiResponse(responseCode = "200", description = "Página de movimientos obtenida exitosamente")
    @GetMapping("/{id}/movimientos")
    public ResponseEntity<Page<MovimientoStock>> obtenerHistorialMovimientos(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @PageableDefault(size = 20, sort = "fechaMovimiento", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productService.obtenerHistorialMovimientos(id, pageable));
    }

    @Operation(summary = "Buscar por nombre", description = "Busca productos que contengan el texto en su nombre")
    @ApiResponse(responseCode = "200", description = "Página de productos encontrados")
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre") @RequestParam String nombre,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(productService.buscarPorNombre(nombre, pageable));
    }

    @Operation(summary = "Filtrar por precio", description = "Obtiene productos dentro de un rango de precios")
    @ApiResponse(responseCode = "200", description = "Página de productos en el rango de precio")
    @GetMapping("/precio")
    public ResponseEntity<Page<Product>> filtrarPorPrecio(
            @Parameter(description = "Precio mínimo") @RequestParam BigDecimal min,
            @Parameter(description = "Precio máximo") @RequestParam BigDecimal max,
            @PageableDefault(size = 10, sort = "precio") Pageable pageable) {
        return ResponseEntity.ok(productService.filtrarPorPrecio(min, max, pageable));
    }

    @Operation(summary = "Filtrar por categoría", description = "Obtiene productos de una categoría específica")
    @ApiResponse(responseCode = "200", description = "Página de productos de la categoría")
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<Page<Product>> filtrarPorCategoria(
            @Parameter(description = "Categoría del producto") @PathVariable Categoria categoria,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(productService.filtrarPorCategoria(categoria, pageable));
    }

    @Operation(summary = "Búsqueda avanzada", description = "Busca productos con múltiples filtros opcionales")
    @ApiResponse(responseCode = "200", description = "Página de productos que coinciden con los filtros")
    @GetMapping("/buscar")
    public ResponseEntity<Page<Product>> busquedaAvanzada(
            @Parameter(description = "Nombre (parcial)") @RequestParam(required = false) String nombre,
            @Parameter(description = "Categoría") @RequestParam(required = false) Categoria categoria,
            @Parameter(description = "Precio mínimo") @RequestParam(required = false) BigDecimal precioMin,
            @Parameter(description = "Precio máximo") @RequestParam(required = false) BigDecimal precioMax,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(productService.buscarConFiltros(nombre, categoria, precioMin, precioMax, pageable));
    }

    @Operation(summary = "Movimientos por tipo", description = "Obtiene movimientos de stock filtrados por tipo")
    @ApiResponse(responseCode = "200", description = "Página de movimientos del tipo especificado")
    @GetMapping("/movimientos/tipo/{tipo}")
    public ResponseEntity<Page<MovimientoStock>> obtenerMovimientosPorTipo(
            @Parameter(description = "Tipo de movimiento") @PathVariable TipoMovimiento tipo,
            @PageableDefault(size = 20, sort = "fechaMovimiento", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productService.obtenerMovimientosPorTipo(tipo, pageable));
    }

    @Operation(summary = "Movimientos por fechas", description = "Obtiene movimientos de stock en un rango de fechas")
    @ApiResponse(responseCode = "200", description = "Página de movimientos en el rango de fechas")
    @GetMapping("/movimientos")
    public ResponseEntity<Page<MovimientoStock>> obtenerMovimientosPorFechas(
            @Parameter(description = "Fecha desde (formato: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @Parameter(description = "Fecha hasta (formato: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 20, sort = "fechaMovimiento", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productService.obtenerMovimientosPorFechas(desde, hasta, pageable));
    }

    @Schema(description = "Request para movimientos de stock (entrada/salida)")
    public record StockRequest(
            @Schema(description = "Cantidad a mover", example = "10")
            Integer cantidad,
            @Schema(description = "Motivo del movimiento", example = "Compra a proveedor")
            String motivo) {}

    @Schema(description = "Request para ajuste de inventario")
    public record AjusteRequest(
            @Schema(description = "Nueva cantidad en stock", example = "100")
            Integer nuevaCantidad,
            @Schema(description = "Motivo del ajuste", example = "Corrección por inventario físico")
            String motivo) {}
}
