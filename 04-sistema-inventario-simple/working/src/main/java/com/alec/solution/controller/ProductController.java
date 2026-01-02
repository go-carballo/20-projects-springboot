package com.alec.solution.controller;

import com.alec.solution.dto.*;
import com.alec.solution.entity.Categoria;
import com.alec.solution.entity.MovimientoStock;
import com.alec.solution.entity.Product;
import com.alec.solution.entity.TipoMovimiento;
import com.alec.solution.mapper.ProductMapper;
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
@Tag(name = "Productos", description = "API para gestion de productos del inventario")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Operation(summary = "Crear producto", description = "Crea un nuevo producto en el inventario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "409", description = "SKU duplicado")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> crear(@Valid @RequestBody ProductCreateRequest request) {
        Product product = productMapper.toEntity(request);
        Product created = productService.crear(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toResponse(created));
    }

    @Operation(summary = "Listar productos", 
               description = "Obtiene todos los productos activos con paginacion y ordenamiento. " +
                            "Ordenar por: nombre, precio, cantidad, stockMinimo, ultimaActualizacion")
    @ApiResponse(responseCode = "200", description = "Pagina de productos obtenida exitosamente")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> listarTodos(
            @Parameter(description = "Numero de pagina (0-indexed)", example = "0")
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ProductResponse> page = productService.listarTodos(pageable)
                .map(productMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Obtener producto por ID", description = "Busca un producto activo por su identificador unico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> obtenerPorId(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        return productService.obtenerPorId(id)
                .map(productMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProductService.ProductoNoEncontradoException(
                        "Producto con ID " + id + " no encontrado"));
    }

    @Operation(summary = "Obtener producto por SKU", description = "Busca un producto activo por su codigo SKU")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> obtenerPorSku(
            @Parameter(description = "SKU del producto (formato: XXX-0000)") @PathVariable String sku) {
        return productService.obtenerPorSku(sku)
                .map(productMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProductService.ProductoNoEncontradoException(
                        "Producto con SKU '" + sku + "' no encontrado"));
    }

    @Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> actualizar(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        Product updated = productService.actualizar(id, request);
        return ResponseEntity.ok(productMapper.toResponse(updated));
    }

    @Operation(summary = "Eliminar producto (soft delete)", 
               description = "Marca un producto como inactivo sin eliminarlo fisicamente")
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
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "El producto ya esta activo"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PostMapping("/{id}/reactivar")
    public ResponseEntity<ProductResponse> reactivar(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        Product reactivated = productService.reactivar(id);
        return ResponseEntity.ok(productMapper.toResponse(reactivated));
    }

    @Operation(summary = "Entrada de stock", description = "Registra una entrada de stock para un producto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cantidad invalida"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PostMapping("/{id}/stock/entrada")
    public ResponseEntity<ProductResponse> entradaStock(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody StockRequest request) {
        Product updated = productService.entradaStock(id, request.cantidad(), request.motivo());
        return ResponseEntity.ok(productMapper.toResponse(updated));
    }

    @Operation(summary = "Salida de stock", description = "Registra una salida de stock para un producto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cantidad invalida o stock insuficiente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PostMapping("/{id}/stock/salida")
    public ResponseEntity<ProductResponse> salidaStock(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody StockRequest request) {
        Product updated = productService.salidaStock(id, request.cantidad(), request.motivo());
        return ResponseEntity.ok(productMapper.toResponse(updated));
    }

    @Operation(summary = "Ajuste de stock", description = "Realiza un ajuste de inventario estableciendo una cantidad especifica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock ajustado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cantidad invalida"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PostMapping("/{id}/stock/ajuste")
    public ResponseEntity<ProductResponse> ajusteStock(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody AjusteStockRequest request) {
        Product updated = productService.ajusteStock(id, request.nuevaCantidad(), request.motivo());
        return ResponseEntity.ok(productMapper.toResponse(updated));
    }

    @Operation(summary = "Alertas de stock bajo", description = "Obtiene productos con stock por debajo del minimo")
    @ApiResponse(responseCode = "200", description = "Lista de productos con stock bajo")
    @GetMapping("/alertas")
    public ResponseEntity<Page<ProductResponse>> obtenerAlertas(
            @PageableDefault(size = 10, sort = "cantidad") Pageable pageable) {
        Page<ProductResponse> page = productService.obtenerProductosConStockBajo(pageable)
                .map(productMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Historial de movimientos", description = "Obtiene el historial de movimientos de stock de un producto")
    @ApiResponse(responseCode = "200", description = "Pagina de movimientos obtenida exitosamente")
    @GetMapping("/{id}/movimientos")
    public ResponseEntity<Page<MovimientoStockResponse>> obtenerHistorialMovimientos(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @PageableDefault(size = 20, sort = "fechaMovimiento", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimientoStockResponse> page = productService.obtenerHistorialMovimientos(id, pageable)
                .map(productMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Buscar por nombre", description = "Busca productos que contengan el texto en su nombre")
    @ApiResponse(responseCode = "200", description = "Pagina de productos encontrados")
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre") @RequestParam String nombre,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        Page<ProductResponse> page = productService.buscarPorNombre(nombre, pageable)
                .map(productMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Filtrar por precio", description = "Obtiene productos dentro de un rango de precios")
    @ApiResponse(responseCode = "200", description = "Pagina de productos en el rango de precio")
    @GetMapping("/precio")
    public ResponseEntity<Page<ProductResponse>> filtrarPorPrecio(
            @Parameter(description = "Precio minimo") @RequestParam BigDecimal min,
            @Parameter(description = "Precio maximo") @RequestParam BigDecimal max,
            @PageableDefault(size = 10, sort = "precio") Pageable pageable) {
        Page<ProductResponse> page = productService.filtrarPorPrecio(min, max, pageable)
                .map(productMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Filtrar por categoria", description = "Obtiene productos de una categoria especifica")
    @ApiResponse(responseCode = "200", description = "Pagina de productos de la categoria")
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<Page<ProductResponse>> filtrarPorCategoria(
            @Parameter(description = "Categoria del producto") @PathVariable Categoria categoria,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        Page<ProductResponse> page = productService.filtrarPorCategoria(categoria, pageable)
                .map(productMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Busqueda avanzada", description = "Busca productos con multiples filtros opcionales")
    @ApiResponse(responseCode = "200", description = "Pagina de productos que coinciden con los filtros")
    @GetMapping("/buscar")
    public ResponseEntity<Page<ProductResponse>> busquedaAvanzada(
            @Parameter(description = "Nombre (parcial)") @RequestParam(required = false) String nombre,
            @Parameter(description = "Categoria") @RequestParam(required = false) Categoria categoria,
            @Parameter(description = "Precio minimo") @RequestParam(required = false) BigDecimal precioMin,
            @Parameter(description = "Precio maximo") @RequestParam(required = false) BigDecimal precioMax,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable) {
        Page<ProductResponse> page = productService.buscarConFiltros(nombre, categoria, precioMin, precioMax, pageable)
                .map(productMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Movimientos por tipo", description = "Obtiene movimientos de stock filtrados por tipo")
    @ApiResponse(responseCode = "200", description = "Pagina de movimientos del tipo especificado")
    @GetMapping("/movimientos/tipo/{tipo}")
    public ResponseEntity<Page<MovimientoStockResponse>> obtenerMovimientosPorTipo(
            @Parameter(description = "Tipo de movimiento") @PathVariable TipoMovimiento tipo,
            @PageableDefault(size = 20, sort = "fechaMovimiento", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimientoStockResponse> page = productService.obtenerMovimientosPorTipo(tipo, pageable)
                .map(productMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Movimientos por fechas", description = "Obtiene movimientos de stock en un rango de fechas")
    @ApiResponse(responseCode = "200", description = "Pagina de movimientos en el rango de fechas")
    @GetMapping("/movimientos")
    public ResponseEntity<Page<MovimientoStockResponse>> obtenerMovimientosPorFechas(
            @Parameter(description = "Fecha desde (formato: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @Parameter(description = "Fecha hasta (formato: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 20, sort = "fechaMovimiento", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimientoStockResponse> page = productService.obtenerMovimientosPorFechas(desde, hasta, pageable)
                .map(productMapper::toResponse);
        return ResponseEntity.ok(page);
    }
}
