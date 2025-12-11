package com.payoyo.working.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payoyo.working.model.Book;

/**
 * Repositorio para operaciones de persistencia de libros.
 * 
 * Características:
 * - Extiende JpaRepository<Book, String> porque ISBN (String) es la PK
 * - Spring Data JPA genera automáticamente la implementación
 * - Métodos CRUD heredados sin necesidad de implementarlos:
 *   * save(Book) - Crear o actualizar
 *   * findById(String) - Buscar por ISBN
 *   * findAll() - Listar todos
 *   * deleteById(String) - Eliminar por ISBN
 *   * existsById(String) - Verificar existencia
 *   * count() - Contar registros
 *   * etc.
 * 
 * Métodos custom:
 * - existsByIsbn(String) - Validación de ISBN duplicado
 */
@Repository // Opcional: JpaRepository ya marca la clase como componente Spring
public interface BookRepository extends JpaRepository<Book, String> {

    /**
     * Verifica si existe un libro con el ISBN especificado.
     * 
     * Uso principal: Validación de negocio antes de crear un libro
     * para evitar duplicados de ISBN (que es la clave primaria).
     * 
     * Spring Data JPA genera automáticamente la implementación basándose
     * en el nombre del método:
     * - "existsBy" → SELECT COUNT(*)... WHERE ...
     * - "Isbn" → campo 'isbn' de la entidad Book
     * 
     * Query SQL generada automáticamente:
     * SELECT COUNT(*) > 0 FROM books WHERE isbn = ?
     * 
     * Ejemplo de uso en Service:
     * <pre>
     * if (bookRepository.existsByIsbn(dto.getIsbn())) {
     *     throw new DuplicateIsbnException("Ya existe un libro con ese ISBN");
     * }
     * </pre>
     * 
     * @param isbn ISBN del libro a verificar
     * @return true si existe un libro con ese ISBN, false en caso contrario
     */
    boolean existsByIsbn(String isbn);
}