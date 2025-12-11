package com.payoyo.working.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payoyo.working.dtos.BookCreateDTO;
import com.payoyo.working.dtos.BookDetailDTO;
import com.payoyo.working.dtos.BookListDTO;
import com.payoyo.working.dtos.BookStockUpdateDTO;
import com.payoyo.working.exceptions.BookNotFoundException;
import com.payoyo.working.exceptions.DuplicateIsbnException;
import com.payoyo.working.model.Book;
import com.payoyo.working.repository.BookRepository;
import com.payoyo.working.service.BookService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de libros.
 * 
 * Responsabilidades:
 * - Lógica de negocio (validaciones, reglas de dominio)
 * - Conversiones entre DTOs y Entities
 * - Coordinación con el Repository para persistencia
 * - Lanzamiento de excepciones de negocio apropiadas
 * 
 * Principios aplicados:
 * - Todos los métodos públicos trabajan con DTOs (nunca exponen Entity)
 * - Inyección de dependencias por constructor (inmutabilidad)
 * - @Transactional en operaciones de escritura (atomicidad)
 * - Uso de Optional para manejo de valores nulos
 * - Conversiones centralizadas usando métodos de DTOs
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    /**
     * Repositorio para acceso a datos de libros.
     * 
     * Marcado como final para:
     * - Inmutabilidad (no puede ser reasignado)
     * - Inyección obligatoria por constructor
     * - Thread-safety
     */
    private final BookRepository bookRepository;

    /**
     * {@inheritDoc}
     * 
     * Implementación:
     * 1. Obtiene todos los libros del repository (List<Book>)
     * 2. Convierte cada Book a BookListDTO usando Stream API
     * 3. Retorna lista de DTOs (vista resumida)
     * 
     * Patrón: Entity → DTO (salida)
     * Conversión: BookListDTO.fromEntity(book)
     * 
     * Nota: Esta operación NO necesita @Transactional porque es solo lectura
     */
    @Override
    public List<BookListDTO> findAll() {
        return bookRepository.findAll().stream()
                .map(BookListDTO::fromEntity)  // Method reference: book -> BookListDTO.fromEntity(book)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * 
     * Implementación:
     * 1. Busca libro en repository por ISBN → Optional<Book>
     * 2. Si existe: convierte Book a BookDetailDTO
     * 3. Si no existe: lanza BookNotFoundException
     * 
     * Patrón: Entity → DTO (salida)
     * Conversión: BookDetailDTO.fromEntity(book)
     * 
     * Uso de Optional.orElseThrow():
     * - Evita if-else verboso
     * - Código funcional más limpio
     * - Lanza excepción si Optional está vacío
     */
    @Override
    public BookDetailDTO findByIsbn(String isbn) {
        return bookRepository.findById(isbn)
                .map(BookDetailDTO::fromEntity)
                .orElseThrow(() -> new BookNotFoundException(
                        "Libro no encontrado con ISBN: " + isbn));
    }

    /**
     * {@inheritDoc}
     * 
     * Implementación:
     * 1. Valida que el ISBN no exista (negocio)
     * 2. Convierte DTO a Entity
     * 3. Persiste en base de datos
     * 4. Convierte Entity guardada a DTO de respuesta
     * 
     * @Transactional:
     * - Garantiza atomicidad (todo o nada)
     * - Rollback automático si hay excepción
     * - Gestión automática de EntityManager
     * 
     * Validación de negocio:
     * - ISBN debe ser único (es PK)
     * - Se verifica ANTES de intentar guardar
     * - Lanza excepción clara en lugar de constraint violation de BD
     * 
     * Patrón: DTO → Entity → guardar → Entity → DTO (ciclo completo)
     */
    @Override
    @Transactional  // Operación de escritura requiere transacción
    public BookDetailDTO create(BookCreateDTO dto) {
        // Validación de negocio: ISBN único
        if (bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new DuplicateIsbnException(
                    "Ya existe un libro con ISBN: " + dto.getIsbn());
        }

        // Conversión DTO → Entity
        Book book = dto.toEntity();
        
        // Persistencia
        Book savedBook = bookRepository.save(book);
        
        // Conversión Entity → DTO de respuesta
        return BookDetailDTO.fromEntity(savedBook);
    }

    /**
     * {@inheritDoc}
     * 
     * Implementación:
     * 1. Busca libro existente por ISBN
     * 2. Si no existe: lanza BookNotFoundException
     * 3. Si existe: actualiza todos los campos (excepto ISBN)
     * 4. Guarda cambios en BD
     * 5. Retorna libro actualizado como DTO
     * 
     * @Transactional:
     * - Garantiza que la actualización sea atómica
     * - Si falla algún campo, se hace rollback de todos
     * 
     * Nota importante:
     * - El ISBN NO se actualiza (es la PK, inmutable)
     * - El ISBN del DTO se ignora, se usa el de la URL
     * - Todos los demás campos SÍ se actualizan
     * 
     * Patrón: Buscar Entity → Modificar → Guardar → Entity → DTO
     */
    @Override
    @Transactional
    public BookDetailDTO update(String isbn, BookCreateDTO dto) {
        // Buscar libro existente
        Book existingBook = bookRepository.findById(isbn)
                .orElseThrow(() -> new BookNotFoundException(
                        "Libro no encontrado con ISBN: " + isbn));

        // Actualizar todos los campos EXCEPTO el ISBN (PK inmutable)
        existingBook.setTitle(dto.getTitle());
        existingBook.setAuthor(dto.getAuthor());
        existingBook.setPublisher(dto.getPublisher());
        existingBook.setPublicationYear(dto.getPublicationYear());
        existingBook.setPages(dto.getPages());
        existingBook.setLanguage(dto.getLanguage());
        existingBook.setPrice(dto.getPrice());
        existingBook.setStock(dto.getStock());
        existingBook.setSynopsis(dto.getSynopsis());
        existingBook.setImageUrl(dto.getImageUrl());

        // JPA detecta cambios automáticamente y hace UPDATE en la BD
        // gracias a @Transactional (no es necesario llamar a save() explícitamente,
        // pero lo hacemos por claridad)
        Book updatedBook = bookRepository.save(existingBook);

        // Retornar libro actualizado como DTO
        return BookDetailDTO.fromEntity(updatedBook);
    }

    /**
     * {@inheritDoc}
     * 
     * Implementación:
     * 1. Busca libro existente por ISBN
     * 2. Si no existe: lanza BookNotFoundException
     * 3. Si existe: actualiza SOLO el campo stock
     * 4. Guarda cambios en BD
     * 5. Retorna libro actualizado como DTO
     * 
     * @Transactional:
     * - Operación atómica de actualización
     * - Rollback automático si falla
     * 
     * Ventaja de operación específica:
     * - No hay riesgo de sobrescribir otros campos por error
     * - API explícita (solo acepta stock en el DTO)
     * - Ideal para integraciones de inventario
     * 
     * Caso de uso típico:
     * - Sistema de ventas actualiza stock después de cada compra
     * - Sistema de almacén actualiza stock tras recepción
     * - Ajustes de inventario
     */
    @Override
    @Transactional
    public BookDetailDTO updateStock(String isbn, BookStockUpdateDTO dto) {
        // Buscar libro existente
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new BookNotFoundException(
                        "Libro no encontrado con ISBN: " + isbn));

        // Actualizar SOLO el stock (operación atómica específica)
        book.setStock(dto.getStock());

        // Persistir cambio
        Book updatedBook = bookRepository.save(book);

        // Retornar libro completo actualizado
        return BookDetailDTO.fromEntity(updatedBook);
    }

    /**
     * {@inheritDoc}
     * 
     * Implementación:
     * 1. Verifica que el libro exista
     * 2. Si no existe: lanza BookNotFoundException
     * 3. Si existe: elimina de la base de datos
     * 
     * @Transactional:
     * - Garantiza que la eliminación sea atómica
     * - Rollback si hay error durante eliminación
     * 
     * Alternativa de implementación:
     * - Podríamos usar directamente deleteById() sin verificar existencia
     * - Pero así damos un mensaje claro si el libro no existe
     * - Mejor UX: 404 "Libro no encontrado" vs 204 exitoso sin efecto
     * 
     * Nota: Este método no retorna nada (void)
     * El Controller devolverá 204 No Content si tiene éxito
     */
    @Override
    @Transactional
    public void delete(String isbn) {
        // Verificar que el libro existe antes de eliminar
        if (!bookRepository.existsById(isbn)) {
            throw new BookNotFoundException(
                    "Libro no encontrado con ISBN: " + isbn);
        }

        // Eliminar libro
        bookRepository.deleteById(isbn);
    }
}