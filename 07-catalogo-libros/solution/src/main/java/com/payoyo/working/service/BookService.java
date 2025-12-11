package com.payoyo.working.service;

import java.util.List;

import com.payoyo.working.dtos.BookCreateDTO;
import com.payoyo.working.dtos.BookDetailDTO;
import com.payoyo.working.dtos.BookListDTO;
import com.payoyo.working.dtos.BookStockUpdateDTO;

/**
 * Interfaz que define el contrato de operaciones de negocio para libros.
 * 
 * Propósito:
 * - Definir la API del servicio de forma clara y explícita
 * - Facilitar testing con mocks/stubs
 * - Permitir múltiples implementaciones si fuera necesario
 * - Documentar las operaciones disponibles
 * 
 * Principio de diseño: Programar contra interfaces, no implementaciones
 * 
 * Todos los métodos trabajan con DTOs (nunca con Entity Book directamente)
 * para mantener separación de capas y desacoplar la API de la persistencia.
 * 
 * @author Jose Luis
 * @version 1.0
 */
public interface BookService {

    /**
     * Obtiene listado resumido de todos los libros.
     * 
     * Retorna BookListDTO para reducir payload (~60% menos que entidad completa).
     * Usado en: GET /api/books
     * 
     * @return Lista de libros en formato resumido (isbn, title, author, price, stock)
     */
    List<BookListDTO> findAll();

    /**
     * Busca un libro por su ISBN y retorna vista completa.
     * 
     * Retorna BookDetailDTO con todos los campos del libro.
     * Usado en: GET /api/books/{isbn}
     * 
     * @param isbn ISBN del libro a buscar
     * @return Libro encontrado con toda su información
     * @throws com.library.catalog.exception.BookNotFoundException si no existe el ISBN
     */
    BookDetailDTO findByIsbn(String isbn);

    /**
     * Crea un nuevo libro en el catálogo.
     * 
     * Validaciones de negocio:
     * - Verifica que el ISBN no esté duplicado
     * 
     * Usado en: POST /api/books
     * 
     * @param dto Datos del libro a crear (validados previamente por @Valid)
     * @return Libro creado con toda su información
     * @throws com.library.catalog.exception.DuplicateIsbnException si el ISBN ya existe
     */
    BookDetailDTO create(BookCreateDTO dto);

    /**
     * Actualiza todos los campos de un libro existente (excepto ISBN).
     * 
     * El ISBN es la clave primaria y no se puede modificar.
     * Todos los demás campos se actualizan con los valores del DTO.
     * 
     * Usado en: PUT /api/books/{isbn}
     * 
     * @param isbn ISBN del libro a actualizar (de la URL)
     * @param dto Nuevos datos del libro (validados previamente por @Valid)
     * @return Libro actualizado con toda su información
     * @throws com.library.catalog.exception.BookNotFoundException si no existe el ISBN
     */
    BookDetailDTO update(String isbn, BookCreateDTO dto);

    /**
     * Actualiza únicamente el stock de un libro.
     * 
     * Operación atómica para gestión de inventario.
     * Evita riesgo de sobrescribir accidentalmente otros campos.
     * 
     * Usado en: PATCH /api/books/{isbn}/stock
     * 
     * @param isbn ISBN del libro a actualizar
     * @param dto Nuevo valor de stock (validado previamente por @Valid)
     * @return Libro actualizado con toda su información
     * @throws com.library.catalog.exception.BookNotFoundException si no existe el ISBN
     */
    BookDetailDTO updateStock(String isbn, BookStockUpdateDTO dto);

    /**
     * Elimina un libro del catálogo por su ISBN.
     * 
     * Usado en: DELETE /api/books/{isbn}
     * 
     * @param isbn ISBN del libro a eliminar
     * @throws com.library.catalog.exception.BookNotFoundException si no existe el ISBN
     */
    void delete(String isbn);
}
