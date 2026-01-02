package com.alec.solution.service;

import com.alec.solution.dto.ProductUpdateRequest;
import com.alec.solution.entity.Categoria;
import com.alec.solution.entity.MovimientoStock;
import com.alec.solution.entity.Product;
import com.alec.solution.entity.TipoMovimiento;
import com.alec.solution.repository.MovimientoStockRepository;
import com.alec.solution.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MovimientoStockRepository movimientoStockRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .nombre("Laptop HP")
                .descripcion("Laptop con procesador Intel i7")
                .cantidad(50)
                .stockMinimo(10)
                .precio(new BigDecimal("1299.99"))
                .sku("LAP-0001")
                .categoria(Categoria.ELECTRONICA)
                .activo(true)
                .ultimaActualizacion(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Crear Producto")
    class CrearProducto {

        @Test
        @DisplayName("Debe crear producto correctamente cuando SKU es único")
        void debeCrearProductoCuandoSkuEsUnico() {
            when(productRepository.existsBySku("LAP-0001")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            Product result = productService.crear(testProduct);

            assertThat(result).isNotNull();
            assertThat(result.getSku()).isEqualTo("LAP-0001");
            verify(productRepository).existsBySku("LAP-0001");
            verify(productRepository).save(testProduct);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando SKU ya existe")
        void debeLanzarExcepcionCuandoSkuExiste() {
            when(productRepository.existsBySku("LAP-0001")).thenReturn(true);

            assertThatThrownBy(() -> productService.crear(testProduct))
                    .isInstanceOf(ProductService.SkuDuplicadoException.class)
                    .hasMessageContaining("LAP-0001");

            verify(productRepository).existsBySku("LAP-0001");
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Listar Productos")
    class ListarProductos {

        @Test
        @DisplayName("Debe retornar página de productos activos")
        void debeRetornarPaginaDeProductosActivos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> expectedPage = new PageImpl<>(List.of(testProduct), pageable, 1);
            when(productRepository.findByActivoTrue(pageable)).thenReturn(expectedPage);

            Page<Product> result = productService.listarTodos(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getNombre()).isEqualTo("Laptop HP");
            verify(productRepository).findByActivoTrue(pageable);
        }

        @Test
        @DisplayName("Debe retornar página vacía cuando no hay productos")
        void debeRetornarPaginaVaciaCuandoNoHayProductos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> emptyPage = Page.empty(pageable);
            when(productRepository.findByActivoTrue(pageable)).thenReturn(emptyPage);

            Page<Product> result = productService.listarTodos(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("Obtener Producto")
    class ObtenerProducto {

        @Test
        @DisplayName("Debe retornar producto cuando existe y está activo")
        void debeRetornarProductoCuandoExisteYEstaActivo() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            Optional<Product> result = productService.obtenerPorId(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getNombre()).isEqualTo("Laptop HP");
        }

        @Test
        @DisplayName("Debe retornar vacío cuando producto no existe")
        void debeRetornarVacioCuandoProductoNoExiste() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Product> result = productService.obtenerPorId(999L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar vacío cuando producto está inactivo")
        void debeRetornarVacioCuandoProductoEstaInactivo() {
            testProduct.setActivo(false);
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            Optional<Product> result = productService.obtenerPorId(1L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar producto por SKU cuando existe y está activo")
        void debeRetornarProductoPorSkuCuandoExisteYEstaActivo() {
            when(productRepository.findBySkuAndActivoTrue("LAP-0001")).thenReturn(Optional.of(testProduct));

            Optional<Product> result = productService.obtenerPorSku("LAP-0001");

            assertThat(result).isPresent();
            assertThat(result.get().getSku()).isEqualTo("LAP-0001");
        }
    }

    @Nested
    @DisplayName("Actualizar Producto")
    class ActualizarProducto {

        @Test
        @DisplayName("Debe actualizar producto correctamente")
        void debeActualizarProductoCorrectamente() {
            ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                    "Laptop HP Actualizada",
                    "Nueva descripcion",
                    15,
                    new BigDecimal("1499.99"),
                    Categoria.ELECTRONICA
            );

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

            Product result = productService.actualizar(1L, updateRequest);

            assertThat(result.getNombre()).isEqualTo("Laptop HP Actualizada");
            assertThat(result.getDescripcion()).isEqualTo("Nueva descripcion");
            assertThat(result.getPrecio()).isEqualByComparingTo("1499.99");
        }

        @Test
        @DisplayName("Debe lanzar excepcion cuando producto no existe")
        void debeLanzarExcepcionCuandoProductoNoExiste() {
            ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                    "Test", "Test", 10, new BigDecimal("100"), Categoria.OTROS
            );
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.actualizar(999L, updateRequest))
                    .isInstanceOf(ProductService.ProductoNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("Eliminar Producto (Soft Delete)")
    class EliminarProducto {

        @Test
        @DisplayName("Debe realizar soft delete correctamente")
        void debeRealizarSoftDeleteCorrectamente() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

            productService.eliminar(1L);

            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(productCaptor.capture());

            Product savedProduct = productCaptor.getValue();
            assertThat(savedProduct.getActivo()).isFalse();
            assertThat(savedProduct.getFechaEliminacion()).isNotNull();
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando producto no existe")
        void debeLanzarExcepcionCuandoProductoNoExiste() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.eliminar(999L))
                    .isInstanceOf(ProductService.ProductoNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("Reactivar Producto")
    class ReactivarProducto {

        @Test
        @DisplayName("Debe reactivar producto correctamente")
        void debeReactivarProductoCorrectamente() {
            testProduct.setActivo(false);
            testProduct.setFechaEliminacion(LocalDateTime.now());
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

            Product result = productService.reactivar(1L);

            assertThat(result.getActivo()).isTrue();
            assertThat(result.getFechaEliminacion()).isNull();
        }

        @Test
        @DisplayName("Debe lanzar excepción si producto ya está activo")
        void debeLanzarExcepcionSiProductoYaEstaActivo() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            assertThatThrownBy(() -> productService.reactivar(1L))
                    .isInstanceOf(ProductService.OperacionInvalidaException.class)
                    .hasMessageContaining("ya está activo");
        }
    }

    @Nested
    @DisplayName("Entrada de Stock")
    class EntradaStock {

        @Test
        @DisplayName("Debe registrar entrada de stock correctamente")
        void debeRegistrarEntradaDeStockCorrectamente() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
            when(movimientoStockRepository.save(any(MovimientoStock.class))).thenAnswer(i -> i.getArgument(0));

            Product result = productService.entradaStock(1L, 20, "Compra a proveedor");

            assertThat(result.getCantidad()).isEqualTo(70); // 50 + 20

            ArgumentCaptor<MovimientoStock> movimientoCaptor = ArgumentCaptor.forClass(MovimientoStock.class);
            verify(movimientoStockRepository).save(movimientoCaptor.capture());

            MovimientoStock movimiento = movimientoCaptor.getValue();
            assertThat(movimiento.getTipo()).isEqualTo(TipoMovimiento.ENTRADA);
            assertThat(movimiento.getCantidad()).isEqualTo(20);
            assertThat(movimiento.getStockAnterior()).isEqualTo(50);
            assertThat(movimiento.getStockNuevo()).isEqualTo(70);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando cantidad es cero o negativa")
        void debeLanzarExcepcionCuandoCantidadEsCeroONegativa() {
            assertThatThrownBy(() -> productService.entradaStock(1L, 0, "Test"))
                    .isInstanceOf(ProductService.CantidadInvalidaException.class);

            assertThatThrownBy(() -> productService.entradaStock(1L, -5, "Test"))
                    .isInstanceOf(ProductService.CantidadInvalidaException.class);
        }
    }

    @Nested
    @DisplayName("Salida de Stock")
    class SalidaStock {

        @Test
        @DisplayName("Debe registrar salida de stock correctamente")
        void debeRegistrarSalidaDeStockCorrectamente() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
            when(movimientoStockRepository.save(any(MovimientoStock.class))).thenAnswer(i -> i.getArgument(0));

            Product result = productService.salidaStock(1L, 10, "Venta");

            assertThat(result.getCantidad()).isEqualTo(40); // 50 - 10

            ArgumentCaptor<MovimientoStock> movimientoCaptor = ArgumentCaptor.forClass(MovimientoStock.class);
            verify(movimientoStockRepository).save(movimientoCaptor.capture());

            MovimientoStock movimiento = movimientoCaptor.getValue();
            assertThat(movimiento.getTipo()).isEqualTo(TipoMovimiento.SALIDA);
            assertThat(movimiento.getCantidad()).isEqualTo(10);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando stock es insuficiente")
        void debeLanzarExcepcionCuandoStockEsInsuficiente() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            assertThatThrownBy(() -> productService.salidaStock(1L, 100, "Venta"))
                    .isInstanceOf(ProductService.StockInsuficienteException.class)
                    .hasMessageContaining("Stock insuficiente");
        }
    }

    @Nested
    @DisplayName("Ajuste de Stock")
    class AjusteStock {

        @Test
        @DisplayName("Debe registrar ajuste de stock correctamente")
        void debeRegistrarAjusteDeStockCorrectamente() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
            when(movimientoStockRepository.save(any(MovimientoStock.class))).thenAnswer(i -> i.getArgument(0));

            Product result = productService.ajusteStock(1L, 100, "Inventario físico");

            assertThat(result.getCantidad()).isEqualTo(100);

            ArgumentCaptor<MovimientoStock> movimientoCaptor = ArgumentCaptor.forClass(MovimientoStock.class);
            verify(movimientoStockRepository).save(movimientoCaptor.capture());

            MovimientoStock movimiento = movimientoCaptor.getValue();
            assertThat(movimiento.getTipo()).isEqualTo(TipoMovimiento.AJUSTE);
            assertThat(movimiento.getCantidad()).isEqualTo(50); // |100 - 50|
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando cantidad es negativa")
        void debeLanzarExcepcionCuandoCantidadEsNegativa() {
            assertThatThrownBy(() -> productService.ajusteStock(1L, -5, "Test"))
                    .isInstanceOf(ProductService.CantidadInvalidaException.class);
        }
    }

    @Nested
    @DisplayName("Búsquedas")
    class Busquedas {

        @Test
        @DisplayName("Debe buscar productos por nombre con paginación")
        void debeBuscarProductosPorNombreConPaginacion() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> expectedPage = new PageImpl<>(List.of(testProduct), pageable, 1);
            when(productRepository.findByNombreContainingIgnoreCaseAndActivoTrue("Laptop", pageable))
                    .thenReturn(expectedPage);

            Page<Product> result = productService.buscarPorNombre("Laptop", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findByNombreContainingIgnoreCaseAndActivoTrue("Laptop", pageable);
        }

        @Test
        @DisplayName("Debe filtrar productos por categoría")
        void debeFiltrarProductosPorCategoria() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> expectedPage = new PageImpl<>(List.of(testProduct), pageable, 1);
            when(productRepository.findByCategoriaAndActivoTrue(Categoria.ELECTRONICA, pageable))
                    .thenReturn(expectedPage);

            Page<Product> result = productService.filtrarPorCategoria(Categoria.ELECTRONICA, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCategoria()).isEqualTo(Categoria.ELECTRONICA);
        }

        @Test
        @DisplayName("Debe filtrar productos por rango de precio")
        void debeFiltrarProductosPorRangoDePrecio() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> expectedPage = new PageImpl<>(List.of(testProduct), pageable, 1);
            BigDecimal min = new BigDecimal("1000");
            BigDecimal max = new BigDecimal("1500");
            when(productRepository.findByPrecioBetweenAndActivoTrue(min, max, pageable))
                    .thenReturn(expectedPage);

            Page<Product> result = productService.filtrarPorPrecio(min, max, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Historial de Movimientos")
    class HistorialMovimientos {

        @Test
        @DisplayName("Debe obtener historial de movimientos de un producto")
        void debeObtenerHistorialDeMovimientos() {
            Pageable pageable = PageRequest.of(0, 20);
            MovimientoStock movimiento = MovimientoStock.builder()
                    .id(1L)
                    .producto(testProduct)
                    .tipo(TipoMovimiento.ENTRADA)
                    .cantidad(10)
                    .stockAnterior(40)
                    .stockNuevo(50)
                    .motivo("Test")
                    .fechaMovimiento(LocalDateTime.now())
                    .build();
            Page<MovimientoStock> expectedPage = new PageImpl<>(List.of(movimiento), pageable, 1);

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(movimientoStockRepository.findByProductoIdOrderByFechaMovimientoDesc(1L, pageable))
                    .thenReturn(expectedPage);

            Page<MovimientoStock> result = productService.obtenerHistorialMovimientos(1L, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTipo()).isEqualTo(TipoMovimiento.ENTRADA);
        }

        @Test
        @DisplayName("Debe lanzar excepción si producto no existe al obtener historial")
        void debeLanzarExcepcionSiProductoNoExisteAlObtenerHistorial() {
            Pageable pageable = PageRequest.of(0, 20);
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.obtenerHistorialMovimientos(999L, pageable))
                    .isInstanceOf(ProductService.ProductoNoEncontradoException.class);
        }
    }
}
