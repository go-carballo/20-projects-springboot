# 📘 Proyecto 7: Catálogo de Libros - Solution

Documentación técnica de la implementación completa del sistema de catálogo de libros con DTOs diferenciados.

## 📊 Arquitectura de la Solución

### Diagrama de Capas y Flujo de Datos

```
┌─────────────────────────────────────────────────────────┐
│                     CLIENTE (Postman/Frontend)           │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP Request (JSON)
                     │ DTOs: BookCreateDTO, BookStockUpdateDTO
                     ▼
┌─────────────────────────────────────────────────────────┐
│              CONTROLLER LAYER                            │
│  - Validación de entrada (@Valid)                        │
│  - Mapeo de endpoints REST                               │
│  - Manejo de Response Entities                           │
│  - Códigos HTTP apropiados                               │
└────────────────────┬────────────────────────────────────┘
                     │ DTOs (entrada)
                     │ DTOs (salida)
                     ▼
┌─────────────────────────────────────────────────────────┐
│               SERVICE LAYER                              │
│  - Lógica de negocio                                     │
│  - Conversión DTO ↔ Entity                              │
│  - Validaciones de negocio                               │
│  - Manejo de excepciones                                 │
└────────────────────┬────────────────────────────────────┘
                     │ Entity (Book)
                     ▼
┌─────────────────────────────────────────────────────────┐
│             REPOSITORY LAYER                             │
│  - Operaciones CRUD vía JPA                              │
│  - Consultas custom                                      │
└────────────────────┬────────────────────────────────────┘
                     │ SQL
                     ▼
┌─────────────────────────────────────────────────────────┐
│                   DATABASE (H2)                          │
│             Tabla: books                                 │
└─────────────────────────────────────────────────────────┘
```

## 🗂️ Estructura de Archivos Implementada

```
src/main/java/com/library/catalog/
├── entity/
│   └── Book.java                      [168 líneas]
│       • @Entity, @Table
│       • ISBN como @Id (String)
│       • Validaciones JPA completas
│       • Getters/Setters
│
├── dto/
│   ├── BookListDTO.java               [45 líneas]
│   │   • Vista resumida para listados
│   │   • Método fromEntity()
│   │
│   ├── BookDetailDTO.java             [92 líneas]
│   │   • Vista completa para detalles
│   │   • Método fromEntity()
│   │
│   ├── BookCreateDTO.java             [128 líneas]
│   │   • DTO de entrada con validaciones
│   │   • Método toEntity()
│   │
│   └── BookStockUpdateDTO.java        [28 líneas]
│       • Operación específica de stock
│
├── repository/
│   └── BookRepository.java            [18 líneas]
│       • extends JpaRepository<Book, String>
│       • Método custom: existsByIsbn()
│
├── service/
│   └── BookService.java               [185 líneas]
│       • 6 métodos públicos
│       • Conversiones DTO ↔ Entity
│       • Manejo de Optional
│       • @Transactional
│
└── controller/
    └── BookController.java            [142 líneas]
        • 6 endpoints REST
        • @Valid para validaciones
        • ResponseEntity con códigos HTTP
        • Header Location en POST

Total: ~806 líneas de código
```

## 📝 Detalles de Implementación por Clase

### 1. Book.java (Entity)

**Decisiones Técnicas**:

```java
@Entity
@Table(name = "books")
public class Book {
    
    // ISBN como PK - No es autoincremental
    @Id
    @Column(length = 20)
    private String isbn;
    
    // Validaciones a nivel JPA
    @NotNull
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;
    
    // BigDecimal para precisión monetaria
    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    // Stock con valor por defecto
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer stock = 0;
    
    // Campos opcionales sin @NotNull
    @Size(max = 100)
    @Column(length = 100)
    private String publisher;
    
    @Size(max = 1000)
    @Column(length = 1000)
    private String synopsis;
    
    @Column(length = 500)
    private String imageUrl;
    
    // ... getters y setters
}
```

**Puntos Clave**:
- **ISBN como String**: ISBNs no son numéricos (contienen guiones)
- **No @GeneratedValue**: El ISBN viene del exterior
- **BigDecimal para price**: Evita problemas de redondeo con double/float
- **Default en stock**: Nuevo libro sin stock especificado tiene 0
- **@Size en lugar de @Max para Strings**: @Max es para números
- **Campos opcionales**: publisher, synopsis, imageUrl pueden ser null

---

### 2. BookListDTO.java (Vista Resumida)

**Propósito**: Listados, catálogos, búsquedas - información esencial

```java
public class BookListDTO {
    private String isbn;
    private String title;
    private String author;
    private BigDecimal price;
    private Integer stock;
    
    /**
     * Convierte una entidad Book a su vista resumida
     * Patrón: Factory method estático
     */
    public static BookListDTO fromEntity(Book book) {
        BookListDTO dto = new BookListDTO();
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPrice(book.getPrice());
        dto.setStock(book.getStock());
        return dto;
    }
    
    // ... getters y setters
}
```

**Decisiones**:
- **Solo 5 campos**: Reduce payload en ~60% vs entidad completa
- **Método estático fromEntity**: Evita new en Service, más legible
- **Sin validaciones**: Es DTO de salida, no necesita validar

**Reducción de Payload**:
```json
// Entidad completa: ~450 bytes
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
  "synopsis": "A comprehensive guide...",
  "imageUrl": "https://..."
}

// BookListDTO: ~180 bytes (-60%)
{
  "isbn": "978-0-134-68599-1",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "price": 45.99,
  "stock": 12
}
```

---

### 3. BookDetailDTO.java (Vista Completa)

**Propósito**: Páginas de detalle, respuestas de creación/actualización

```java
public class BookDetailDTO {
    // Todos los campos de Book
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private Integer pages;
    private String language;
    private BigDecimal price;
    private Integer stock;
    private String synopsis;
    private String imageUrl;
    
    /**
     * Convierte Book a vista completa
     * Incluye todos los campos para visualización detallada
     */
    public static BookDetailDTO fromEntity(Book book) {
        BookDetailDTO dto = new BookDetailDTO();
        // Mapeo de todos los campos...
        return dto;
    }
}
```

**Decisiones**:
- **Espejo de la entidad**: Mismo contenido pero sin anotaciones JPA
- **Usado en respuestas**: POST, PUT, PATCH, GET individual
- **Confirmación completa**: Cliente ve exactamente lo que se guardó

---

### 4. BookCreateDTO.java (DTO de Entrada)

**Propósito**: Validar datos de entrada en POST/PUT

```java
public class BookCreateDTO {
    
    @NotBlank(message = "ISBN is required")
    private String isbn;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Author is required")
    @Size(max = 100, message = "Author cannot exceed 100 characters")
    private String author;
    
    @Size(max = 100, message = "Publisher cannot exceed 100 characters")
    private String publisher; // Opcional
    
    @NotNull(message = "Publication year is required")
    @Min(value = 1450, message = "Publication year must be after 1450")
    @Max(value = 2100, message = "Publication year cannot exceed 2100")
    private Integer publicationYear;
    
    @NotNull(message = "Pages is required")
    @Positive(message = "Pages must be greater than zero")
    private Integer pages;
    
    @NotBlank(message = "Language is required")
    @Size(min = 2, max = 2, message = "Language must be ISO 639-1 code (2 characters)")
    private String language;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private BigDecimal price;
    
    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;
    
    @Size(max = 1000, message = "Synopsis cannot exceed 1000 characters")
    private String synopsis; // Opcional
    
    @Pattern(regexp = "^https?://.*", message = "Image URL must be valid")
    private String imageUrl; // Opcional
    
    /**
     * Convierte este DTO a una entidad Book
     * Patrón: DTO → Entity para operaciones de creación
     */
    public Book toEntity() {
        Book book = new Book();
        book.setIsbn(this.isbn);
        book.setTitle(this.title);
        // ... resto de campos
        return book;
    }
}
```

**Decisiones**:
- **Validaciones detalladas**: Mensajes personalizados
- **Reutilizado en POST y PUT**: Mismo contrato
- **Método toEntity()**: Centraliza la conversión
- **Sin @Id**: El DTO no sabe que ISBN es PK

**Ejemplo de Validación Fallida**:
```json
// Request
POST /api/books
{
  "isbn": "",
  "title": "A",
  "price": -10,
  "publicationYear": 1200
}

// Response 400 Bad Request
{
  "isbn": "ISBN is required",
  "price": "Price must be at least 0.01",
  "publicationYear": "Publication year must be after 1450"
}
```

---

### 5. BookStockUpdateDTO.java (Operación Específica)

**Propósito**: Actualización atómica de stock (inventario, ventas)

```java
public class BookStockUpdateDTO {
    
    @NotNull(message = "Stock value is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;
    
    // Constructor, getters, setters
}
```

**Ventajas de este DTO específico**:
1. **Seguridad**: No puedes sobrescribir accidentalmente title, price, etc.
2. **Claridad**: El contrato de API es explícito
3. **Integraciones**: Sistemas de inventario pueden usar solo este endpoint
4. **Validación**: Solo valida el campo relevante

**Ejemplo de Uso**:
```java
// Sistema de ventas reduce stock después de compra
PATCH /api/books/978-0-134-68599-1/stock
{ "stock": 8 }  // Antes era 10, vendieron 2
```

---

### 6. BookRepository.java

```java
@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    
    /**
     * Verifica si existe un libro con el ISBN dado
     * Útil para validaciones en Service antes de crear duplicados
     */
    boolean existsByIsbn(String isbn);
}
```

**Decisiones**:
- **JpaRepository<Book, String>**: String porque ISBN es la PK
- **Método custom**: Spring Data JPA lo implementa automáticamente
- **@Repository opcional**: JpaRepository ya tiene @Repository internamente

---

### 7. BookService.java (Lógica Core)

```java
@Service
public class BookService {
    
    private final BookRepository bookRepository;
    
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    /**
     * Obtiene listado resumido de todos los libros
     * Convierte cada Book a BookListDTO para reducir payload
     */
    public List<BookListDTO> findAll() {
        return bookRepository.findAll().stream()
                .map(BookListDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca libro por ISBN y devuelve vista completa
     * @throws RuntimeException si no existe el ISBN
     */
    public BookDetailDTO findByIsbn(String isbn) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbn));
        return BookDetailDTO.fromEntity(book);
    }
    
    /**
     * Crea nuevo libro validando unicidad de ISBN
     * @throws RuntimeException si ISBN ya existe
     */
    @Transactional
    public BookDetailDTO create(BookCreateDTO dto) {
        // Validación de negocio: ISBN único
        if (bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new RuntimeException("Book already exists with ISBN: " + dto.getIsbn());
        }
        
        Book book = dto.toEntity();
        Book savedBook = bookRepository.save(book);
        return BookDetailDTO.fromEntity(savedBook);
    }
    
    /**
     * Actualiza todos los campos excepto ISBN (PK no mutable)
     * @throws RuntimeException si no existe el libro
     */
    @Transactional
    public BookDetailDTO update(String isbn, BookCreateDTO dto) {
        Book existingBook = bookRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbn));
        
        // Actualizar campos (ISBN no se cambia)
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
        
        Book updatedBook = bookRepository.save(existingBook);
        return BookDetailDTO.fromEntity(updatedBook);
    }
    
    /**
     * Actualización específica de stock
     * Operación atómica para sistemas de inventario
     */
    @Transactional
    public BookDetailDTO updateStock(String isbn, BookStockUpdateDTO dto) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbn));
        
        book.setStock(dto.getStock());
        Book updatedBook = bookRepository.save(book);
        return BookDetailDTO.fromEntity(updatedBook);
    }
    
    /**
     * Elimina libro por ISBN
     * @throws RuntimeException si no existe
     */
    @Transactional
    public void delete(String isbn) {
        if (!bookRepository.existsById(isbn)) {
            throw new RuntimeException("Book not found with ISBN: " + isbn);
        }
        bookRepository.deleteById(isbn);
    }
}
```

**Puntos Clave**:
- **Todos los métodos públicos devuelven DTOs**: Controller nunca ve Entities
- **@Transactional en escrituras**: Garantiza atomicidad
- **Validaciones de negocio**: existsByIsbn antes de crear
- **Manejo de Optional**: orElseThrow con mensajes descriptivos
- **Conversiones centralizadas**: Usa fromEntity() y toEntity()

---

### 8. BookController.java (API REST)

```java
@RestController
@RequestMapping("/api/books")
public class BookController {
    
    private final BookService bookService;
    
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    
    /**
     * GET /api/books
     * Lista todos los libros en formato resumido
     */
    @GetMapping
    public ResponseEntity<List<BookListDTO>> findAll() {
        List<BookListDTO> books = bookService.findAll();
        return ResponseEntity.ok(books);
    }
    
    /**
     * GET /api/books/{isbn}
     * Obtiene detalle completo de un libro
     */
    @GetMapping("/{isbn}")
    public ResponseEntity<BookDetailDTO> findByIsbn(@PathVariable String isbn) {
        try {
            BookDetailDTO book = bookService.findByIsbn(isbn);
            return ResponseEntity.ok(book);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * POST /api/books
     * Crea nuevo libro
     * Devuelve 201 Created con header Location
     */
    @PostMapping
    public ResponseEntity<BookDetailDTO> create(@Valid @RequestBody BookCreateDTO dto) {
        try {
            BookDetailDTO created = bookService.create(dto);
            // Header Location: /api/books/{isbn}
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{isbn}")
                    .buildAndExpand(created.getIsbn())
                    .toUri();
            return ResponseEntity.created(location).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * PUT /api/books/{isbn}
     * Actualiza libro completo (excepto ISBN)
     */
    @PutMapping("/{isbn}")
    public ResponseEntity<BookDetailDTO> update(
            @PathVariable String isbn,
            @Valid @RequestBody BookCreateDTO dto) {
        try {
            BookDetailDTO updated = bookService.update(isbn, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * PATCH /api/books/{isbn}/stock
     * Actualización específica de stock
     * Endpoint para operaciones de inventario
     */
    @PatchMapping("/{isbn}/stock")
    public ResponseEntity<BookDetailDTO> updateStock(
            @PathVariable String isbn,
            @Valid @RequestBody BookStockUpdateDTO dto) {
        try {
            BookDetailDTO updated = bookService.updateStock(isbn, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * DELETE /api/books/{isbn}
     * Elimina libro del catálogo
     */
    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> delete(@PathVariable String isbn) {
        try {
            bookService.delete(isbn);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
```

**Decisiones**:
- **ResponseEntity para control fino**: Códigos HTTP específicos
- **@Valid en DTOs de entrada**: Activa validaciones Jakarta
- **try-catch simplificado**: En producción usar @ControllerAdvice
- **Header Location en POST**: Indica dónde encontrar el recurso creado
- **204 No Content en DELETE**: Respuesta sin cuerpo
- **PATCH vs PUT**: PATCH para actualizaciones parciales (stock)

---

## 🔄 Flujos de Datos Completos

### Flujo 1: Listar Libros (GET /api/books)

```
1. Cliente hace: GET /api/books
2. BookController.findAll()
3. → bookService.findAll()
4.   → bookRepository.findAll() → devuelve List<Book>
5.   → Stream + map(BookListDTO::fromEntity)
6.   ← devuelve List<BookListDTO>
7. ← ResponseEntity.ok(List<BookListDTO>)
8. Cliente recibe JSON con vista resumida
```

### Flujo 2: Crear Libro (POST /api/books)

```
1. Cliente envía: POST /api/books + BookCreateDTO JSON
2. @Valid valida el DTO
3. Si válido → BookController.create(dto)
4. → bookService.create(dto)
5.   → existsByIsbn() para verificar unicidad
6.   → dto.toEntity() convierte a Book
7.   → bookRepository.save(book)
8.   → BookDetailDTO.fromEntity(savedBook)
9.   ← devuelve BookDetailDTO
10. ← ResponseEntity.created(location).body(dto)
11. Cliente recibe: 201 Created + Location + JSON completo
```

### Flujo 3: Actualizar Stock (PATCH)

```
1. Cliente envía: PATCH /api/books/{isbn}/stock + { "stock": 20 }
2. @Valid valida BookStockUpdateDTO
3. BookController.updateStock(isbn, dto)
4. → bookService.updateStock(isbn, dto)
5.   → findById(isbn) para obtener Book existente
6.   → book.setStock(dto.getStock())
7.   → save(book)
8.   → BookDetailDTO.fromEntity(updatedBook)
9.   ← devuelve BookDetailDTO
10. ← ResponseEntity.ok(dto)
11. Cliente recibe libro completo con stock actualizado
```

## 🎯 Patrones y Mejores Prácticas Aplicadas

### 1. DTO Pattern (Data Transfer Object)
**Propósito**: Desacoplar capa de presentación de capa de persistencia

**Beneficios implementados**:
- ✅ Reducción de payload (BookListDTO -60%)
- ✅ Versionado de API independiente de BD
- ✅ Validaciones específicas por caso de uso
- ✅ Seguridad: No exponer estructura interna

### 2. Factory Method Pattern
**Implementación**: `fromEntity()` en DTOs de salida

```java
// En lugar de:
BookListDTO dto = new BookListDTO();
dto.setIsbn(book.getIsbn());
// ...

// Usamos:
BookListDTO dto = BookListDTO.fromEntity(book);
```

**Ventajas**:
- Código más legible
- Centraliza lógica de conversión
- Facilita mantenimiento

### 3. Builder Pattern (implícito en toEntity)
**Implementación**: `toEntity()` en DTOs de entrada

```java
public Book toEntity() {
    Book book = new Book();
    // Configuración centralizada
    return book;
}
```

### 4. Repository Pattern
**Implementación**: Spring Data JPA con JpaRepository

**Beneficios**:
- Abstracción de acceso a datos
- Métodos CRUD sin implementar
- Queries derivadas (existsByIsbn)

### 5. Service Layer Pattern
**Implementación**: BookService como capa intermedia

**Responsabilidades**:
- Lógica de negocio
- Conversiones DTO ↔ Entity
- Transacciones
- Validaciones complejas

### 6. Dependency Injection
**Implementación**: Constructor injection

```java
public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
}
```

**Ventajas sobre @Autowired**:
- Inmutabilidad (final)
- Testeable (mock fácil)
- No depende de Spring

### 7. RESTful API Design
**Implementación**: Verbos HTTP correctos, códigos de estado apropiados

| Operación | Verbo | Endpoint | Código Éxito |
|-----------|-------|----------|--------------|
| Listar | GET | /api/books | 200 OK |
| Detalle | GET | /api/books/{id} | 200 OK |
| Crear | POST | /api/books | 201 Created + Location |
| Actualizar todo | PUT | /api/books/{id} | 200 OK |
| Actualizar parcial | PATCH | /api/books/{id}/stock | 200 OK |
| Eliminar | DELETE | /api/books/{id} | 204 No Content |

## 📊 Comparativa: Sin DTOs vs Con DTOs

### Arquitectura Sin DTOs (❌ Anti-patrón)

```java
// Controller expone Entity directamente
@GetMapping
public List<Book> findAll() {
    return bookRepository.findAll(); // ❌ Expone Entity
}
```

**Problemas**:
1. Frontend acoplado a estructura de BD
2. Cambios en Entity rompen frontend
3. Posible exposición de datos sensibles
4. Payload innecesariamente grande
5. Dificultad para añadir campos calculados

### Arquitectura Con DTOs (✅ Correcta)

```java
// Controller expone DTO
@GetMapping
public List<BookListDTO> findAll() {
    return bookService.findAll(); // ✅ Devuelve DTO
}
```

**Beneficios**:
1. Frontend desacoplado de BD
2. Cambios internos no afectan API
3. Control fino de qué exponer
4. Performance optimizada
5. Versionado de API facilitado

## 🧪 Testing Manual con Postman

### Escenario de Prueba Completo

#### 1. Crear Primer Libro
```http
POST http://localhost:8080/api/books
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
  "synopsis": "Guide to Java best practices",
  "imageUrl": "https://example.com/ej.jpg"
}
```
**Verificar**: 201 Created, Header Location, JSON con BookDetailDTO

#### 2. Intentar Crear Duplicado
```http
POST http://localhost:8080/api/books
{ ...mismo ISBN... }
```
**Verificar**: 400 Bad Request

#### 3. Listar Todos
```http
GET http://localhost:8080/api/books
```
**Verificar**: 200 OK, Array con BookListDTO (5 campos)

#### 4. Obtener Detalle
```http
GET http://localhost:8080/api/books/978-0-134-68599-1
```
**Verificar**: 200 OK, JSON con BookDetailDTO (11 campos)

#### 5. Actualizar Stock
```http
PATCH http://localhost:8080/api/books/978-0-134-68599-1/stock
{ "stock": 5 }
```
**Verificar**: 200 OK, stock cambiado a 5, otros campos intactos

#### 6. Actualizar Libro Completo
```http
PUT http://localhost:8080/api/books/978-0-134-68599-1
{ ...todos los campos con title modificado... }
```
**Verificar**: 200 OK, title actualizado

#### 7. Eliminar Libro
```http
DELETE http://localhost:8080/api/books/978-0-134-68599-1
```
**Verificar**: 204 No Content

#### 8. Verificar Eliminación
```http
GET http://localhost:8080/api/books/978-0-134-68599-1
```
**Verificar**: 404 Not Found

## 🚀 Mejoras Futuras (Proyectos 11-20)

### Proyecto 11: Añadir Relaciones
```java
@Entity
public class Book {
    // ...
    
    @ManyToMany
    private List<Category> categories;
    
    @ManyToOne
    private Publisher publisher; // Convertir String a entidad
}
```

### Proyecto 16: Testing Completo
```java
@Test
void testBookListDTOReducesPayload() {
    Book book = createFullBook();
    BookListDTO dto = BookListDTO.fromEntity(book);
    
    // Verificar que DTO solo tiene campos esenciales
    assertNotNull(dto.getIsbn());
    assertNull(dto.getSynopsis()); // No existe en DTO
}
```

### Proyecto 18: Paginación y Ordenamiento
```java
@GetMapping
public Page<BookListDTO> findAll(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "title") String sortBy) {
    // ...
}
```

## 📈 Métricas del Proyecto

| Métrica | Valor |
|---------|-------|
| Clases implementadas | 8 |
| Líneas de código | ~806 |
| Endpoints REST | 6 |
| DTOs creados | 4 |
| Métodos en Service | 6 |
| Validaciones implementadas | 11 |
| Reducción payload (List) | ~60% |
| Cobertura conceptos Spring | 95% |

## 🎓 Conceptos Aprendidos

✅ DTO Pattern completo  
✅ Múltiples DTOs para misma entidad  
✅ Conversiones bidireccionales (DTO ↔ Entity)  
✅ Validaciones Jakarta en DTOs  
✅ Validaciones JPA en Entities  
✅ Operaciones específicas (PATCH)  
✅ ResponseEntity con códigos HTTP  
✅ Header Location en POST  
✅ Constructor Injection  
✅ @Transactional en escrituras  
✅ Manejo de Optional  
✅ Stream API para conversiones  
✅ RESTful API Design  
✅ Separación de responsabilidades  

---

**Conclusión**: Este proyecto establece las bases sólidas para el trabajo con DTOs en Spring Boot, un patrón fundamental que se mantiene hasta proyectos empresariales complejos. La separación entre entidades de persistencia y objetos de transferencia es una práctica profesional indispensable.