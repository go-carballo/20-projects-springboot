# Proyecto 7: Catálogo de Libros

## 📚 Descripción General

Sistema de gestión de catálogo de libros que implementa diferentes vistas de información mediante DTOs. Este proyecto introduce el concepto de **transformación de datos** según el caso de uso: vistas resumidas para listados, vistas completas para detalles, y DTOs específicos para operaciones de creación y actualización.

## 🎯 Objetivos de Aprendizaje

- Implementar **múltiples DTOs** para una misma entidad según el contexto
- Aplicar el patrón **DTO para separación de capas**
- Diseñar **vistas diferenciadas** según necesidades del frontend
- Gestionar **transformaciones bidireccionales** (Entity ↔ DTO)
- Manejar **operaciones parciales** (ej: actualizar solo stock)

## 📋 Requisitos Funcionales

### Gestión de Libros

**RF-01: Listar todos los libros** (vista resumida)
- Endpoint: `GET /api/books`
- Respuesta: Lista de `BookListDTO` con información esencial
- Casos de uso: Catálogos, grids, búsquedas
- Información incluida: ISBN, título, autor, precio, stock

**RF-02: Obtener detalle de un libro**
- Endpoint: `GET /api/books/{isbn}`
- Respuesta: `BookDetailDTO` con información completa
- Casos de uso: Páginas de detalle, fichas técnicas
- Información incluida: Todos los campos de la entidad

**RF-03: Crear nuevo libro**
- Endpoint: `POST /api/books`
- Request body: `BookCreateDTO`
- Validaciones obligatorias en DTO de entrada
- Response: `BookDetailDTO` del libro creado

**RF-04: Actualizar información completa**
- Endpoint: `PUT /api/books/{isbn}`
- Request body: `BookCreateDTO` (reutilizado)
- Actualización de todos los campos excepto ISBN
- Response: `BookDetailDTO` actualizado

**RF-05: Actualizar solo el stock**
- Endpoint: `PATCH /api/books/{isbn}/stock`
- Request body: `BookStockUpdateDTO`
- Operación específica para inventario
- Response: `BookDetailDTO` actualizado

**RF-06: Eliminar libro**
- Endpoint: `DELETE /api/books/{isbn}`
- Response: 204 No Content

## 📊 Modelo de Datos

### Entidad: Book

```java
Book {
    String isbn             // PK, formato: XXX-X-XXXX-XXXX-X
    String title            // NOT NULL, max 200 caracteres
    String author           // NOT NULL, max 100 caracteres
    String publisher        // max 100 caracteres
    Integer publicationYear // NOT NULL, rango: 1450-2100
    Integer pages           // NOT NULL, min 1
    String language         // NOT NULL, código ISO 639-1
    BigDecimal price        // NOT NULL, min 0.01, 2 decimales
    Integer stock           // NOT NULL, min 0, default 0
    String synopsis         // TEXT, hasta 1000 caracteres
    String imageUrl         // URL válida
}
```

### DTOs Requeridos

#### 1. BookListDTO (Vista Resumida - Listados)
```java
BookListDTO {
    String isbn
    String title
    String author
    BigDecimal price
    Integer stock
}
```
**Uso**: Endpoints que devuelven listas, búsquedas, catálogos  
**Razón**: Reduce payload, mejora performance, información esencial

#### 2. BookDetailDTO (Vista Completa - Detalle)
```java
BookDetailDTO {
    String isbn
    String title
    String author
    String publisher
    Integer publicationYear
    Integer pages
    String language
    BigDecimal price
    Integer stock
    String synopsis
    String imageUrl
}
```
**Uso**: Endpoints de detalle individual, respuestas de creación/actualización  
**Razón**: Información completa para páginas de detalle, confirmaciones

#### 3. BookCreateDTO (Entrada de Datos)
```java
BookCreateDTO {
    @NotBlank String isbn
    @NotBlank @Size(max=200) String title
    @NotBlank @Size(max=100) String author
    @Size(max=100) String publisher
    @NotNull @Min(1450) @Max(2100) Integer publicationYear
    @NotNull @Positive Integer pages
    @NotBlank @Size(min=2, max=2) String language
    @NotNull @DecimalMin("0.01") BigDecimal price
    @NotNull @Min(0) Integer stock
    @Size(max=1000) String synopsis
    @Pattern(regexp="^https?://.*") String imageUrl
}
```
**Uso**: POST y PUT (creación y actualización completa)  
**Razón**: Validaciones estrictas, no expone implementación interna

#### 4. BookStockUpdateDTO (Operación Específica)
```java
BookStockUpdateDTO {
    @NotNull @Min(0) Integer stock
}
```
**Uso**: PATCH para actualización parcial de stock  
**Razón**: Operación atómica para inventario, simplifica integraciones

## 🏗️ Estructura Técnica

### Capas de la Aplicación

```
com.library.catalog
├── entity
│   └── Book.java
├── dto
│   ├── BookListDTO.java
│   ├── BookDetailDTO.java
│   ├── BookCreateDTO.java
│   └── BookStockUpdateDTO.java
├── repository
│   └── BookRepository.java
├── service
│   └── BookService.java
└── controller
    └── BookController.java
```

### Dependencias Maven Necesarias

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- H2 Database (desarrollo) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok (opcional) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## 🔄 Flujo de Transformaciones

### Entity → DTO (Salida)

```
Service recibe Entity desde Repository
    ↓
Service convierte a DTO apropiado según contexto
    ↓
Controller devuelve DTO al cliente
```

**Ejemplo**: 
- `findAll()` → List<Book> → List<BookListDTO>
- `findById()` → Book → BookDetailDTO

### DTO → Entity (Entrada)

```
Controller recibe DTO validado
    ↓
Service convierte DTO a Entity
    ↓
Repository persiste Entity
    ↓
Service convierte Entity guardada a DTO de respuesta
```

**Ejemplo**:
- POST: BookCreateDTO → Book → save() → BookDetailDTO

## ⚙️ Reglas de Negocio

1. **ISBN único**: No pueden existir dos libros con el mismo ISBN
2. **Stock nunca negativo**: El stock mínimo es 0
3. **Precio positivo**: El precio debe ser mayor a 0.01
4. **Año de publicación válido**: Entre 1450 (invención imprenta) y año actual + 2
5. **Idioma en formato ISO**: Código de 2 letras (es, en, fr, etc.)
6. **URL de imagen opcional**: Si se proporciona, debe ser URL válida
7. **Sinopsis opcional**: Máximo 1000 caracteres

## 📡 API Endpoints Detallados

### 1. Listar Libros
```http
GET /api/books
Response: 200 OK
[
  {
    "isbn": "978-0-134-68599-1",
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "price": 45.99,
    "stock": 12
  }
]
```

### 2. Obtener Detalle
```http
GET /api/books/978-0-134-68599-1
Response: 200 OK
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
  "synopsis": "A comprehensive guide to best practices...",
  "imageUrl": "https://example.com/effective-java.jpg"
}
```

### 3. Crear Libro
```http
POST /api/books
Content-Type: application/json
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
  "imageUrl": "https://example.com/effective-java.jpg"
}
Response: 201 Created
Location: /api/books/978-0-134-68599-1
```

### 4. Actualizar Stock
```http
PATCH /api/books/978-0-134-68599-1/stock
Content-Type: application/json
{
  "stock": 20
}
Response: 200 OK
```

### 5. Actualizar Libro Completo
```http
PUT /api/books/978-0-134-68599-1
Content-Type: application/json
{
  "isbn": "978-0-134-68599-1",
  "title": "Effective Java (3rd Edition)",
  ...
}
Response: 200 OK
```

### 6. Eliminar Libro
```http
DELETE /api/books/978-0-134-68599-1
Response: 204 No Content
```

## 🎯 Casos de Uso y Contextos

### Contexto 1: Catálogo en Tienda Online
- Usuario navega listado de libros
- **DTO usado**: BookListDTO
- **Razón**: Carga rápida, información esencial, menor ancho de banda

### Contexto 2: Página de Detalle del Libro
- Usuario hace clic en un libro específico
- **DTO usado**: BookDetailDTO
- **Razón**: Información completa para decisión de compra

### Contexto 3: Administración - Alta de Libro
- Administrador crea nuevo libro en sistema
- **DTO usado**: BookCreateDTO → BookDetailDTO
- **Razón**: Validación estricta entrada, confirmación completa

### Contexto 4: Gestión de Inventario
- Sistema de almacén actualiza stock tras recepción
- **DTO usado**: BookStockUpdateDTO → BookDetailDTO
- **Razón**: Operación específica, evita sobrescribir otros campos por error

## 🔍 Validaciones Detalladas

### Validaciones en BookCreateDTO

| Campo | Validaciones |
|-------|-------------|
| isbn | @NotBlank, formato ISBN válido |
| title | @NotBlank, @Size(max=200) |
| author | @NotBlank, @Size(max=100) |
| publisher | @Size(max=100), opcional |
| publicationYear | @NotNull, @Min(1450), @Max(2100) |
| pages | @NotNull, @Positive |
| language | @NotBlank, @Size(min=2, max=2) |
| price | @NotNull, @DecimalMin("0.01") |
| stock | @NotNull, @Min(0) |
| synopsis | @Size(max=1000), opcional |
| imageUrl | @Pattern(regexp URL), opcional |

### Manejo de Errores

```json
// Respuesta de validación fallida
{
  "timestamp": "2024-12-08T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "price": "must be greater than or equal to 0.01",
    "publicationYear": "must be between 1450 and 2100"
  }
}
```

## 🧪 Configuración de Base de Datos

### application.properties
```properties
# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Datasource
spring.datasource.url=jdbc:h2:mem:bookdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

## 📝 Notas de Implementación

### Conversión de DTOs

Se recomienda implementar métodos estáticos de conversión en cada DTO:

```java
// En BookListDTO
public static BookListDTO fromEntity(Book book) { ... }

// En BookDetailDTO
public static BookDetailDTO fromEntity(Book book) { ... }

// En BookCreateDTO
public Book toEntity() { ... }
```

### Consideraciones de Performance

- **BookListDTO**: Reduce ~60% el tamaño de payload vs enviar entidad completa
- **Queries optimizadas**: JPA debe cargar solo campos necesarios (aunque por defecto carga todos)
- **Cache futuro**: Los DTOs facilitan implementar caché de respuestas

### Extensibilidad

Este proyecto sienta las bases para futuras mejoras:
- Proyecto 11+: Añadir relación con Categorías
- Proyecto 16+: Añadir testing completo de conversiones DTO
- Mejoras futuras: Paginación de BookListDTO, búsquedas avanzadas

## ✅ Criterios de Completitud

- [ ] Entidad Book con todas las validaciones JPA
- [ ] 4 DTOs implementados correctamente
- [ ] Repository con métodos básicos
- [ ] Service con lógica de conversión DTO ↔ Entity
- [ ] Controller con 6 endpoints REST
- [ ] Validaciones funcionando en DTOs de entrada
- [ ] Pruebas manuales con Postman exitosas
- [ ] READMEs completos en las 3 ubicaciones
- [ ] Código comentado siguiendo mejores prácticas
