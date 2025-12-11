package com.payoyo.working.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.payoyo.working.dtos.BookCreateDTO;
import com.payoyo.working.dtos.BookDetailDTO;
import com.payoyo.working.dtos.BookListDTO;
import com.payoyo.working.dtos.BookStockUpdateDTO;
import com.payoyo.working.service.BookService;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para la gestión de libros.
 * 
 * Responsabilidades:
 * - Exponer endpoints REST según especificación de la API
 * - Recibir y validar requests HTTP
 * - Delegar lógica de negocio al Service
 * - Devolver respuestas HTTP apropiadas (códigos, headers, body)
 * 
 * Este Controller NO maneja excepciones directamente:
 * - GlobalExceptionHandler intercepta y maneja todas las excepciones
 * - Permite código limpio sin try-catch repetitivos
 * 
 * Principios aplicados:
 * - RESTful API design (verbos HTTP correctos, códigos apropiados)
 * - Inyección de dependencias por constructor
 * - Trabajo exclusivo con DTOs (nunca Entity Book)
 * - Validación automática con @Valid
 * - ResponseEntity para control fino de respuestas
 * 
 * Base path: /api/books
 * 
 * @author Jose Luis
 * @version 1.0
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    /**
     * Servicio de lógica de negocio para libros.
     * 
     * Inyección de la INTERFAZ (IBookService) no de la implementación (BookService).
     * Ventajas:
     * - Programación contra abstracciones (SOLID)
     * - Facilita testing con mocks
     * - Desacoplamiento
     */
    private final BookService bookService;

    /**
     * Constructor para inyección de dependencias.
     * 
     * Spring inyecta automáticamente la implementación de IBookService.
     * 
     * @param bookService Servicio de libros (inyectado por Spring)
     */
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Lista todos los libros en formato resumido.
     * 
     * Endpoint: GET /api/books
     * 
     * Retorna BookListDTO (vista resumida) para optimizar payload:
     * - Reduce ~60% el tamaño de respuesta vs entidad completa
     * - Ideal para listados, catálogos, búsquedas
     * 
     * Respuesta exitosa:
     * - Código: 200 OK
     * - Body: Array de BookListDTO
     * 
     * Ejemplo de respuesta:
     * [
     *   {
     *     "isbn": "978-0-134-68599-1",
     *     "title": "Effective Java",
     *     "author": "Joshua Bloch",
     *     "price": 45.99,
     *     "stock": 12
     *   }
     * ]
     * 
     * @return ResponseEntity con lista de libros en formato resumido
     */
    @GetMapping
    public ResponseEntity<List<BookListDTO>> findAll() {
        List<BookListDTO> books = bookService.findAll();
        return ResponseEntity.ok(books);
    }

    /**
     * Obtiene el detalle completo de un libro por su ISBN.
     * 
     * Endpoint: GET /api/books/{isbn}
     * 
     * Retorna BookDetailDTO (vista completa) con todos los campos del libro.
     * 
     * Respuestas posibles:
     * - 200 OK: Libro encontrado (body con BookDetailDTO)
     * - 404 Not Found: ISBN no existe (manejado por GlobalExceptionHandler)
     * 
     * Ejemplo de respuesta exitosa:
     * {
     *   "isbn": "978-0-134-68599-1",
     *   "title": "Effective Java",
     *   "author": "Joshua Bloch",
     *   "publisher": "Addison-Wesley",
     *   "publicationYear": 2018,
     *   "pages": 416,
     *   "language": "en",
     *   "price": 45.99,
     *   "stock": 12,
     *   "synopsis": "Guide to best practices...",
     *   "imageUrl": "https://..."
     * }
     * 
     * @param isbn ISBN del libro a buscar (obtenido de la URL)
     * @return ResponseEntity con el libro encontrado
     */
    @GetMapping("/{isbn}")
    public ResponseEntity<BookDetailDTO> findByIsbn(@PathVariable String isbn) {
        BookDetailDTO book = bookService.findByIsbn(isbn);
        return ResponseEntity.ok(book);
    }

    /**
     * Crea un nuevo libro en el catálogo.
     * 
     * Endpoint: POST /api/books
     * 
     * @Valid activa las validaciones de BookCreateDTO:
     * - @NotBlank, @Size, @Min, @Max, @Pattern, etc.
     * - Si falla validación: 400 Bad Request (manejado por GlobalExceptionHandler)
     * 
     * Respuestas posibles:
     * - 201 Created: Libro creado exitosamente
     *   * Header Location: /api/books/{isbn} (URL del nuevo recurso)
     *   * Body: BookDetailDTO del libro creado
     * - 400 Bad Request: Validación fallida
     * - 409 Conflict: ISBN duplicado (manejado por GlobalExceptionHandler)
     * 
     * ServletUriComponentsBuilder:
     * - Construye URL del nuevo recurso automáticamente
     * - Toma base path actual + /{isbn}
     * - Cumple estándar REST para POST
     * 
     * Ejemplo de request:
     * POST /api/books
     * {
     *   "isbn": "978-0-134-68599-1",
     *   "title": "Effective Java",
     *   "author": "Joshua Bloch",
     *   "publicationYear": 2018,
     *   "pages": 416,
     *   "language": "en",
     *   "price": 45.99,
     *   "stock": 12
     * }
     * 
     * Ejemplo de respuesta:
     * HTTP/1.1 201 Created
     * Location: /api/books/978-0-134-68599-1
     * { ... BookDetailDTO ... }
     * 
     * @param dto Datos del libro a crear (validados automáticamente)
     * @return ResponseEntity con código 201, header Location y libro creado
     */
    @PostMapping
    public ResponseEntity<BookDetailDTO> create(@Valid @RequestBody BookCreateDTO dto) {
        BookDetailDTO createdBook = bookService.create(dto);
        
        // Construir URI del nuevo recurso: /api/books/{isbn}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()           // Base: /api/books
                .path("/{isbn}")                // Añade: /{isbn}
                .buildAndExpand(createdBook.getIsbn())  // Reemplaza {isbn} con valor real
                .toUri();
        
        // 201 Created + Location header + body
        return ResponseEntity.created(location).body(createdBook);
    }

    /**
     * Actualiza todos los campos de un libro existente (excepto ISBN).
     * 
     * Endpoint: PUT /api/books/{isbn}
     * 
     * El ISBN es la clave primaria y NO se puede modificar.
     * El ISBN de la URL prevalece sobre el del body (si viene en el DTO).
     * 
     * @Valid activa validaciones de BookCreateDTO.
     * 
     * Respuestas posibles:
     * - 200 OK: Libro actualizado exitosamente (body con BookDetailDTO)
     * - 400 Bad Request: Validación fallida
     * - 404 Not Found: ISBN no existe (manejado por GlobalExceptionHandler)
     * 
     * Semántica PUT:
     * - Actualización completa (todos los campos)
     * - Idempotente (misma operación N veces = mismo resultado)
     * 
     * Ejemplo de request:
     * PUT /api/books/978-0-134-68599-1
     * {
     *   "isbn": "978-0-134-68599-1",
     *   "title": "Effective Java (3rd Edition)",
     *   "author": "Joshua Bloch",
     *   ...todos los demás campos...
     * }
     * 
     * @param isbn ISBN del libro a actualizar (de la URL, tiene prioridad)
     * @param dto Nuevos datos del libro (validados automáticamente)
     * @return ResponseEntity con código 200 y libro actualizado
     */
    @PutMapping("/{isbn}")
    public ResponseEntity<BookDetailDTO> update(
            @PathVariable String isbn,
            @Valid @RequestBody BookCreateDTO dto) {
        
        BookDetailDTO updatedBook = bookService.update(isbn, dto);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * Actualiza únicamente el stock de un libro (operación parcial).
     * 
     * Endpoint: PATCH /api/books/{isbn}/stock
     * 
     * Operación atómica específica para gestión de inventario:
     * - Solo actualiza el campo stock
     * - No hay riesgo de sobrescribir otros campos por error
     * - Ideal para integraciones con sistemas de almacén/ventas
     * 
     * @Valid activa validaciones de BookStockUpdateDTO:
     * - stock no puede ser null
     * - stock no puede ser negativo
     * 
     * Respuestas posibles:
     * - 200 OK: Stock actualizado (body con BookDetailDTO completo)
     * - 400 Bad Request: Validación fallida (stock negativo, null, etc.)
     * - 404 Not Found: ISBN no existe (manejado por GlobalExceptionHandler)
     * 
     * Semántica PATCH:
     * - Actualización parcial (solo campos especificados)
     * - Más apropiado que PUT para actualizaciones de un solo campo
     * 
     * Ejemplo de request:
     * PATCH /api/books/978-0-134-68599-1/stock
     * {
     *   "stock": 25
     * }
     * 
     * Nota: Aunque solo actualizamos stock, retornamos BookDetailDTO completo
     * para que el cliente vea el estado final del libro.
     * 
     * @param isbn ISBN del libro a actualizar
     * @param dto Nuevo valor de stock (validado automáticamente)
     * @return ResponseEntity con código 200 y libro actualizado completo
     */
    @PatchMapping("/{isbn}/stock")
    public ResponseEntity<BookDetailDTO> updateStock(
            @PathVariable String isbn,
            @Valid @RequestBody BookStockUpdateDTO dto) {
        
        BookDetailDTO updatedBook = bookService.updateStock(isbn, dto);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * Elimina un libro del catálogo por su ISBN.
     * 
     * Endpoint: DELETE /api/books/{isbn}
     * 
     * Respuestas posibles:
     * - 204 No Content: Libro eliminado exitosamente (sin body)
     * - 404 Not Found: ISBN no existe (manejado por GlobalExceptionHandler)
     * 
     * Semántica DELETE:
     * - Eliminación del recurso
     * - 204 No Content es el código estándar (sin body en respuesta)
     * - Idempotente (eliminar N veces = mismo resultado)
     * 
     * ResponseEntity<Void>:
     * - Void indica que no hay body en la respuesta
     * - Solo devuelve código HTTP 204
     * 
     * Ejemplo de respuesta exitosa:
     * HTTP/1.1 204 No Content
     * (sin body)
     * 
     * @param isbn ISBN del libro a eliminar
     * @return ResponseEntity con código 204 sin body
     */
    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> delete(@PathVariable String isbn) {
        bookService.delete(isbn);
        return ResponseEntity.noContent().build();
    }
}
