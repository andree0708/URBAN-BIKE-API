# urban-bike-api

API REST para gestionar el alquiler de bicicletas en una ciudad. Resuelve el problema de registrar bicis, saber cuáles están disponibles, iniciar y cerrar alquileres, y calcular cuánto debe pagar el cliente según el tiempo real de uso y si se pasó de las horas estimadas.

## Arquitectura

Elegí una arquitectura en capas: **Controller → Service → Repository**.

En los controllers solo recibo las peticiones HTTP y delego. La lógica de negocio vive en los services (`BicycleService`, `RentalService`) y el acceso a datos quedó en los repositorios de Spring Data JPA. Lo separé así porque cada capa tiene una responsabilidad clara: si mañana cambio la base de datos o el formato de la API, no tengo que reescribir todo mezclado.

Además saqué el cálculo de costos a `RentalCalculatorService`, que no toca JPA ni la base. Eso me sirvió mucho para testear la facturación con tests unitarios puros, sin levantar Spring ni la base de datos. Los services orquestan (validan estado de la bici, guardan el alquiler, etc.) y el calculador solo hace números.

## Tecnologías utilizadas

| Tecnología | Versión | Para qué se usa |
|------------|---------|-----------------|
| Java | 21 | Lenguaje base del proyecto |
| Spring Boot | 4.0.6 | Framework principal, arranque de la app |
| Spring Data JPA | (starter del parent) | Persistencia y repositorios |
| H2 Database | (runtime) | Base en memoria (desarrollo local) |
| Lombok | (starter del parent) | Reducir boilerplate en entidades y DTOs |
| JUnit 5 | (vía spring-boot-starter-test) | Tests unitarios |
| Mockito | (incluido en el starter de test) | Mocks en tests de servicios |
| Maven | 3.x (wrapper incluido) | Build y dependencias |

## Requisitos para ejecutar

- Java 21
- Maven (viene incluido con `mvnw` / `mvnw.cmd`)

## Cómo correr el proyecto localmente

```bash
git clone <url-del-repositorio>
cd urban-bike-api
```

**En PowerShell (Windows)** — el `.\` al inicio es obligatorio:

```powershell
.\mvnw.cmd spring-boot:run
```

**En Git Bash / Linux / macOS:**

```bash
./mvnw spring-boot:run
```

Por defecto usa el perfil **`dev`** con **H2 en memoria** (no hace falta Docker ni instalar PostgreSQL). API abierta (sin API Key).

La app queda en http://localhost:8080

Al arrancar se cargan 5 bicicletas de ejemplo si la base está vacía (`DataInitializer`).

**Consola H2** (opcional, para ver tablas):

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:urbanbike`
- Usuario: `sa` | Contraseña: (vacío)

## Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/health` | Health check |
| POST | `/api/v1/bicycles` | Registrar una bicicleta nueva |
| GET | `/api/v1/bicycles/available` | Listar bicicletas disponibles (filtro opcional por tipo) |
| POST | `/api/v1/rentals` | Iniciar un alquiler |
| PUT | `/api/v1/rentals/{id}/finish` | Finalizar un alquiler y calcular el costo |
| GET | `/api/v1/rentals/bicycle/{code}/history` | Ver el historial de alquileres de una bici |

## Ejemplos de uso

Para registrar una bicicleta nueva:

```bash
curl -X POST http://localhost:8080/api/v1/bicycles \
  -H "Content-Type: application/json" \
  -d "{\"code\": \"BIC-010\", \"type\": \"URBAN\"}"
```

Para ver las bicicletas disponibles (todas o solo de un tipo):

```bash
curl http://localhost:8080/api/v1/bicycles/available
```

```bash
curl "http://localhost:8080/api/v1/bicycles/available?type=MOUNTAIN"
```

Para iniciar un alquiler:

```bash
curl -X POST http://localhost:8080/api/v1/rentals \
  -H "Content-Type: application/json" \
  -d "{\"bikeCode\": \"BIC-001\", \"clientName\": \"Ana García\", \"estimatedHours\": 2}"
```

Para finalizar un alquiler (reemplazá `{id}` por el id que devolvió el POST anterior):

```bash
curl -X PUT http://localhost:8080/api/v1/rentals/1/finish
```

Para ver el historial de una bicicleta:

```bash
curl http://localhost:8080/api/v1/rentals/bicycle/BIC-001/history
```

También dejé una colección de Postman en la raíz: `urban-bike-api.postman_collection.json`.

## Reglas de negocio implementadas

**Tarifas por tipo de bicicleta.** Cada tipo tiene una tarifa por hora definida en el enum `BikeType`: URBAN $3.500, MOUNTAIN $5.000, ELECTRIC $7.500. La multa por retraso es el 50 % de esa tarifa horaria, aplicada por cada hora de demora (también redondeada al alza).

**Redondeo al alza y mínimo de 1 hora.** El tiempo real se mide en minutos entre `startTime` y `endTime`, se convierte a horas con `ceil` (cualquier fracción cuenta como hora completa), y si el resultado da 0 horas igual se cobra 1 hora mínima. Ejemplo: si alquilás 30 segundos, se factura 1 hora.

**Multa por devolver tarde.** Si el cliente se pasa del tiempo estimado, se calculan minutos de retraso y se cobra la penalización por horas de demora (mínimo 1 hora de multa si hubo cualquier retraso). Ejemplo con MOUNTAIN, 2 horas estimadas y devolución a las 3 h 20 min: son 4 horas reales de uso ($20.000) más 2 horas de demora × $2.500 = $5.000 de multa → **total $25.000**.

## Cómo correr los tests

```bash
./mvnw test
```

En Windows: `.\mvnw.cmd test`

Son **14 tests** en total: 7 para el cálculo de facturación (`RentalCalculatorServiceTest`), 6 para la capa de servicio con mocks (`RentalServiceImplTest`), y 1 que verifica que el contexto de Spring levanta (`ApplicationTests`).

Los **13 tests** de lógica y servicios no necesitan base de datos. El test de contexto (`ApplicationTests`) levanta H2 en perfil `dev`.

## Decisiones de diseño

**H2 en memoria.** Para la prueba técnica no quería que el evaluador instale PostgreSQL. H2 arranca solo y la consola web ayuda a revisar datos si hace falta.

**`RentalCalculatorService` aparte.** La facturación tiene reglas que cambian fácil (redondeos, mínimos, multas). Al no mezclarla con JPA, la probé con tests rápidos sin mocks de base de datos.

**DTOs de request y response.** No expongo las entidades JPA directo en la API porque no quiero filtrar campos internos ni acoplar el contrato HTTP al modelo de persistencia.

**Inyección por constructor.** Dependencias explícitas; en tests con Mockito paso los mocks por constructor sin trucos.

**Código de bici duplicado.** Devuelve **409 Conflict** con mensaje claro en lugar de un error genérico de base de datos.

## Supuestos

- El tiempo mínimo facturable de un alquiler es **1 hora**, aunque dure menos.
- El retraso mínimo facturable como multa es **1 hora**, si hubo cualquier minuto de demora.
- Los minutos parciales **siempre redondean hacia arriba** al calcular horas.
- Los enums en JSON usan nombres en inglés (`URBAN`, `AVAILABLE`) por convención Java; el dominio es el mismo del enunciado.
- Incluí la colección de Postman en la raíz del proyecto.

Si el evaluador nota alguna decisión que pudo tomarse diferente, quedé abierta a discutirla — el enunciado da libertad arquitectónica y estas fueron mis decisiones justificadas.
