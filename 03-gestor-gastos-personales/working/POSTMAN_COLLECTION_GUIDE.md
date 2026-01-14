# Colección de Postman - Gestor de Gastos Personales

## Cómo Importar la Colección

1. Abre Postman
2. Haz clic en "Import" en la esquina superior izquierda
3. Selecciona el archivo: `Gestor_Gastos_Personales_API.postman_collection.json`
4. La colección aparecerá en tu barra lateral

## Estructura de la Colección

### 📁 1. CRUD Básico
Operaciones fundamentales para gestionar gastos:

- **POST** Crear Gasto - Crea un nuevo gasto
- **GET** Obtener Todos los Gastos - Lista todos los gastos
- **GET** Obtener Gasto por ID - Obtiene un gasto específico
- **PUT** Actualizar Gasto - Modifica un gasto existente
- **DELETE** Eliminar Gasto - Elimina un gasto

### 📁 2. Filtros y Consultas
Endpoints para filtrar y buscar gastos:

- **GET** Filtrar por Categoría - FOOD, TRANSPORT, ENTERTAINMENT
- **GET** Filtrar por Rango de Fechas - Entre dos fechas
- **GET** Filtrar por Método de Pago - CREDIT_CARD, CASH, etc.

### 📁 3. Datos de Prueba
10 requests pre-configuradas para crear datos de ejemplo en todas las categorías:

- 2 gastos de FOOD
- 2 gastos de TRANSPORT
- 1 gasto de ENTERTAINMENT
- 1 gasto de HEALTH
- 1 gasto de EDUCATION
- 1 gasto de UTILITIES
- 1 gasto de SHOPPING
- 1 gasto de OTHER

### 📁 4. Validaciones - Casos de Error
Requests que deberían fallar para probar las validaciones:

- Monto negativo (debe fallar)
- Fecha futura (debe fallar)
- Descripción vacía (debe fallar)
- Descripción muy corta (debe fallar)
- Campo nulo (debe fallar)
- ID no existe (debe retornar 404)

## Categorías Disponibles

| Categoría | Valor en JSON |
|-----------|---------------|
| Alimentación | `FOOD` |
| Transporte | `TRANSPORT` |
| Entretenimiento | `ENTERTAINMENT` |
| Salud | `HEALTH` |
| Educación | `EDUCATION` |
| Servicios | `UTILITIES` |
| Compras | `SHOPPING` |
| Otros | `OTHER` |

## Métodos de Pago Disponibles

| Método de Pago | Valor en JSON |
|----------------|---------------|
| Efectivo | `CASH` |
| Tarjeta de débito | `DEBIT_CARD` |
| Tarjeta de crédito | `CREDIT_CARD` |
| Transferencia bancaria | `BANK_TRANSFER` |
| Billetera digital | `DIGITAL_WALLET` |

## Ejemplo de Request Body

```json
{
  "description": "Almuerzo en restaurante",
  "amount": 25.50,
  "category": "FOOD",
  "date": "2024-12-15",
  "paymentMethod": "CREDIT_CARD"
}
```

## Validaciones

### Descripción
- ✅ Obligatorio
- ✅ Mínimo 3 caracteres
- ✅ Máximo 200 caracteres

### Amount (Monto)
- ✅ Obligatorio
- ✅ Mayor que 0
- ✅ Máximo 2 decimales

### Category (Categoría)
- ✅ Obligatorio
- ✅ Debe ser uno de los valores del enum

### Date (Fecha)
- ✅ Obligatorio
- ✅ No puede ser fecha futura
- ✅ Formato: YYYY-MM-DD

### PaymentMethod (Método de Pago)
- ✅ Obligatorio
- ✅ Debe ser uno de los valores del enum

## Respuestas de Error

### 400 Bad Request - Validación
```json
{
  "timestamp": "2024-12-17T16:00:00",
  "status": 400,
  "error": "Validation Error",
  "errors": {
    "description": "La descripción debe tener entre 3 y 200 caracteres",
    "amount": "El monto debe ser mayor que 0"
  }
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-12-17T16:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "No se encontró el gasto con ID: 999"
}
```

## Flujo de Prueba Recomendado

1. **Crear datos de prueba**
   - Ejecuta todos los requests de la carpeta "Datos de Prueba"
   - Esto creará 10 gastos de ejemplo

2. **Probar listado y filtros**
   - GET Obtener Todos los Gastos
   - GET Filtrar por Categoría - FOOD
   - GET Filtrar por Rango de Fechas

3. **Probar CRUD**
   - GET Obtener Gasto por ID (usa ID 1)
   - PUT Actualizar Gasto (usa ID 1)
   - DELETE Eliminar Gasto (usa ID 10)

4. **Probar validaciones**
   - Ejecuta los requests de "Validaciones - Casos de Error"
   - Verifica que retornen errores apropiados

## Variables de Entorno

La colección usa una variable:
- `baseUrl`: http://localhost:8080

Si tu servidor corre en otro puerto, actualiza esta variable en Postman.

## Notas

- Todos los endpoints retornan JSON
- Las fechas usan formato ISO 8601 (YYYY-MM-DD)
- Los montos usan BigDecimal con hasta 2 decimales
- Los gastos se ordenan por fecha descendente por defecto
