# 📖 Proyecto 7: Catálogo de Libros - Working Directory

¡Bienvenido al directorio de desarrollo del Proyecto 7! Aquí construirás paso a paso un sistema de gestión de catálogo de libros con **DTOs diferenciados** según el caso de uso.

## 🎯 Objetivo del Proyecto

Implementar un sistema que expone **diferentes vistas de la misma entidad** mediante DTOs:
- Vista **resumida** para listados (BookListDTO)
- Vista **completa** para detalles (BookDetailDTO)
- DTO de **entrada** para creación/actualización (BookCreateDTO)
- DTO **específico** para operaciones parciales (BookStockUpdateDTO)

## 🏗️ Estructura a Implementar

```
src/main/java/com/library/catalog/
├── entity/
│   └── Book.java                 ← EMPEZAR AQUÍ
├── dto/
│   ├── BookListDTO.java          ← 2º
│   ├── BookDetailDTO.java        ← 3º
│   ├── BookCreateDTO.java        ← 4º
│   └── BookStockUpdateDTO.java   ← 5º
├── repository/
│   └── BookRepository.java       ← 6º
├── service/
│   └── BookService.java          ← 7º
└── controller/
    └── BookController.java       ← 8º
```

## 📋 Checklist de Desarrollo

### Fase 1: Modelo de Datos
- [ ] **Book.java** - Entidad con validaciones JPA
  - ISBN como PK (@Id, no @GeneratedValue)
  - Validaciones: @NotNull, @NotBlank, @Min, @Max, @DecimalMin
  - Campos opcionales: publisher, synopsis, imageUrl

### Fase 2: DTOs (¡Lo nuevo de este proyecto!)
- [ ] **BookListDTO.java** - Vista resumida
  - Solo: isbn, title, author, price, stock
  - Método static: `fromEntity(Book book)`
  
- [ ] **BookDetailDTO.java** - Vista completa
  - Todos los campos de Book
  - Método static: `fromEntity(Book book)`
  
- [ ] **BookCreateDTO.java** - Entrada de datos
  - Validaciones Jakarta: @NotBlank, @Size, @Min, @Max, @Pattern
  - Método: `toEntity()` para convertir a Book
  
- [ ] **BookStockUpdateDTO.java** - Actualización parcial
  - Solo campo: stock con @NotNull y @Min(0)

### Fase 3: Persistencia
- [ ] **BookRepository.java**
  - Interface que extiende JpaRepository<Book, String>
  - Método custom: `boolean existsByIsbn(String isbn)`

### Fase 4: Lógica de Negocio
- [ ] **BookService.java**
  - Métodos que devuelven DTOs (no Entities)
  - `findAll()` → List<BookListDTO>
  - `findByIsbn(String)` → BookDetailDTO
  - `create(BookCreateDTO)` → BookDetailDTO
  - `update(String, BookCreateDTO)` → BookDetailDTO
  - `updateStock(String, BookStockUpdateDTO)` → BookDetailDTO
  - `delete(String)` → void

### Fase 5: API REST
- [ ] **BookController.java**
  - 6 endpoints REST
  - Códigos HTTP correctos (200, 201, 204, 404)
  - Validación con @Valid
  - Header Location en POST

## 📡 Endpoints a Implementar

### 1️⃣ Listar Todos los Libros (Vista Resumida)
```http
GET http://localhost:8080/api/books
```
**Response**: 200 OK + `List<BookListDTO>`

### 2️⃣ Obtener Detalle de un Libro
```http
GET http://localhost:8080/api/books/{isbn}
```
**Response**: 200 OK + `BookDetailDTO` o 404 Not Found

### 3️⃣ Crear Nuevo Libro
```http
POST http://localhost:8080/api/books
Content-Type: application/json
```
**Request Body**: `BookCreateDTO`  
**Response**: 201 Created + Header Location + `BookDetailDTO`

### 4️⃣ Actualizar Libro Completo
```http
PUT http://localhost:8080/api/books/{isbn}
Content-Type: application/json
```
**Request Body**: `BookCreateDTO`  
**Response**: 200 OK + `BookDetailDTO` o 404 Not Found

### 5️⃣ Actualizar Solo Stock (Operación Parcial)
```http
PATCH http://localhost:8080/api/books/{isbn}/stock
Content-Type: application/json
```
**Request Body**: `BookStockUpdateDTO`  
**Response**: 200 OK + `BookDetailDTO` o 404 Not Found

### 6️⃣ Eliminar Libro
```http
DELETE http://localhost:8080/api/books/{isbn}
```
**Response**: 204 No Content o 404 Not Found

## 🧪 Colección Postman

Importa el archivo **`Proyecto7-BookCatalog.postman_collection.json`** incluido en este directorio.

### Ejemplos de Request Bodies

#### ✅ Crear Libro (POST/PUT)
```json
{
  "isbn": "978-0-134-68599-1",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "publisher": "Addison-Wesley Professional",
  "publicationYear": 2018,
  "pages": 416,
  "language": "en",
  "price": 45.99,
  "stock": 12,
  "synopsis": "The definitive guide to Java programming best practices. This third edition covers Java 7, 8, and 9, including new language features and library enhancements.",
  "imageUrl": "https://example.com/covers/effective-java-3rd.jpg"
}
```

#### ✅ Actualizar Stock (PATCH)
```json
{
  "stock": 25
}
```

#### ❌ Validación Fallida - Precio Negativo
```json
{
  "isbn": "978-0-134-68599-1",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "publisher": "Addison-Wesley Professional",
  "publicationYear": 2018,
  "pages": 416,
  "language": "en",
  "price": -10.50,  // ❌ Error: debe ser >= 0.01
  "stock": 12
}
```

## 🔑 Conceptos Clave de Este Proyecto

### 1. DTOs para Diferentes Contextos

**¿Por qué múltiples DTOs?**
- **Performance**: BookListDTO reduce ~60% el tamaño de respuesta en listados
- **Seguridad**: No expones estructura interna de la entidad
- **Flexibilidad**: Cambias DTOs sin tocar la entidad
- **Claridad**: Cada DTO tiene un propósito específico

**Ejemplo del Flujo**:
```
Cliente solicita listado
    ↓
GET /api/books
    ↓
Controller llama Service.findAll()
    ↓
Service obtiene List<Book> del Repository
    ↓
Service convierte cada Book a BookListDTO
    ↓
Controller devuelve List<BookListDTO>
    ↓
Cliente recibe solo información esencial
```

### 2. Separación de Responsabilidades

| Capa | Responsabilidad | Trabaja con |
|------|----------------|-------------|
| **Controller** | Recibir requests, devolver responses | DTOs |
| **Service** | Lógica de negocio, conversiones | DTOs + Entities |
| **Repository** | Acceso a datos | Entities |
| **Entity** | Modelo de base de datos | - |
| **DTO** | Contratos de API | - |

### 3. Validaciones en Capas

**Validaciones JPA** (Entity):
- Constraints de base de datos
- Ejemplo: @NotNull, @Column(nullable = false)

**Validaciones Jakarta** (DTO):
- Constraints de entrada de API
- Ejemplo: @NotBlank, @Size(max=200), @Min(0)

**¿Por qué en ambos lugares?**
- Entity: Protege integridad de datos
- DTO: Feedback rápido al cliente, antes de llegar a Service

### 4. Conversión DTO ↔ Entity

**Patrón recomendado**:
```java
// DTO → Entity (entrada)
public class BookCreateDTO {
    public Book toEntity() {
        Book book = new Book();
        book.setIsbn(this.isbn);
        book.setTitle(this.title);
        // ...
        return book;
    }
}

// Entity → DTO (salida)
public class BookListDTO {
    public static BookListDTO fromEntity(Book book) {
        BookListDTO dto = new BookListDTO();
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        // ...
        return dto;
    }
}
```

## ⚠️ Errores Comunes a Evitar

### ❌ Error 1: Exponer Entities en Controller
```java
// MAL
@GetMapping
public List<Book> findAll() {
    return bookService.findAll();
}
```
**Problema**: Expones estructura interna, acoplas frontend a tu BD

### ✅ Correcto: Usar DTOs
```java
// BIEN
@GetMapping
public List<BookListDTO> findAll() {
    return bookService.findAll();
}
```

### ❌ Error 2: No Reutilizar DTOs
```java
// MAL - Crear DTOs separados para POST y PUT
public class BookCreateDTO { ... }
public class BookUpdateDTO { ... } // Duplicado innecesario
```

### ✅ Correcto: Reutilizar cuando aplica
```java
// BIEN - Mismo DTO para create y update
@PostMapping
public ResponseEntity<BookDetailDTO> create(@Valid @RequestBody BookCreateDTO dto) { ... }

@PutMapping("/{isbn}")
public ResponseEntity<BookDetailDTO> update(@PathVariable String isbn, 
                                             @Valid @RequestBody BookCreateDTO dto) { ... }
```

### ❌ Error 3: Hacer conversiones en Controller
```java
// MAL
@GetMapping
public List<BookListDTO> findAll() {
    List<Book> books = bookService.findAll();
    return books.stream()
                .map(BookListDTO::fromEntity)
                .collect(Collectors.toList());
}
```
**Problema**: Controller no debe conocer la entidad

### ✅ Correcto: Conversiones en Service
```java
// Controller - BIEN
@GetMapping
public List<BookListDTO> findAll() {
    return bookService.findAll(); // Ya devuelve DTOs
}

// Service - BIEN
public List<BookListDTO> findAll() {
    return bookRepository.findAll().stream()
                         .map(BookListDTO::fromEntity)
                         .collect(Collectors.toList());
}
```

## 🚀 Cómo Empezar

1. **Configura el proyecto**:
   ```bash
   # Crea estructura de paquetes
   mkdir -p src/main/java/com/library/catalog/{entity,dto,repository,service,controller}
   ```

2. **Configura application.properties** (raíz del proyecto):
   ```properties
   spring.h2.console.enabled=true
   spring.datasource.url=jdbc:h2:mem:bookdb
   spring.jpa.show-sql=true
   ```

3. **Empieza por la Entity**: `Book.java`
   - Define campos con tipos correctos
   - Añade validaciones JPA
   - Getters/Setters o Lombok

4. **Crea los DTOs** uno por uno
   - Empieza por BookListDTO (el más simple)
   - Sigue con BookDetailDTO
   - Luego BookCreateDTO con validaciones
   - Termina con BookStockUpdateDTO

5. **Implementa Repository** (interface simple)

6. **Desarrolla Service**:
   - Aquí va la lógica de conversiones
   - Manejo de Optional para findById
   - Verificaciones de existencia

7. **Finaliza con Controller**:
   - Usa @Valid para validar DTOs
   - Códigos HTTP apropiados
   - Header Location en POST

## 📚 Datos de Prueba

Una vez funcione tu API, prueba crear estos libros:

```json
// Libro 1: Programación
{
  "isbn": "978-0-134-68599-1",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "publisher": "Addison-Wesley",
  "publicationYear": 2018,
  "pages": 416,
  "language": "en",
  "price": 45.99,
  "stock": 12,
  "synopsis": "The definitive guide to Java programming best practices.",
  "imageUrl": "https://example.com/effective-java.jpg"
}

// Libro 2: Español
{
  "isbn": "978-84-376-0494-7",
  "title": "Cien Años de Soledad",
  "author": "Gabriel García Márquez",
  "publisher": "Editorial Sudamericana",
  "publicationYear": 1967,
  "pages": 471,
  "language": "es",
  "price": 22.50,
  "stock": 8,
  "synopsis": "La obra cumbre del realismo mágico.",
  "imageUrl": "https://example.com/cien-anos.jpg"
}

// Libro 3: Sin imagen ni sinopsis (campos opcionales)
{
  "isbn": "978-0-13-468599-1",
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "publisher": "Prentice Hall",
  "publicationYear": 2008,
  "pages": 464,
  "language": "en",
  "price": 42.00,
  "stock": 15
}
```

## 🎓 Recursos Adicionales

- **Validaciones Jakarta**: [Bean Validation Spec](https://beanvalidation.org/2.0/spec/)
- **DTOs en Spring**: [Baeldung - Entity to DTO](https://www.baeldung.com/entity-to-and-from-dto-for-a-java-spring-application)
- **HTTP Status Codes**: [MDN HTTP Status](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)

---

¡Éxito con el desarrollo! 🚀