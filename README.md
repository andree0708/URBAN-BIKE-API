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
| Spring Security | (starter del parent) | API Key en producción, protección de endpoints |
| H2 Database | (runtime) | Base en memoria en perfil `dev` (local, sin instalar nada) |
| PostgreSQL | (runtime) | Base en perfil `prod` (Render en la nube) |
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

> Si más adelante instalás Docker, podés usar `docker compose up -d` y el perfil `prod` local con Postgres; en la nube (Render) siempre es PostgreSQL.

## Seguridad

Uso **perfiles de Spring** para separar desarrollo y producción:

| Perfil | Base de datos | API Key |
|--------|---------------|---------|
| `dev` (local) | H2 en memoria | No requerida |
| `prod` (Render) | PostgreSQL | Obligatoria (`X-API-Key`) |

En producción cada request (excepto `GET /health`) debe incluir:

```http
X-API-Key: <valor-de-la-variable-API_KEY>
```

Si la clave falta o es incorrecta, la API responde **401** con un `ErrorResponse` en JSON.

También validé códigos de bici duplicados con **409 Conflict** en lugar de dejar que la base devuelva un error 500.

## Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/health` | Health check (público, para Render) |
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

**En producción (Render)** agregá el header a cada llamada:

```bash
curl https://tu-app.onrender.com/api/v1/bicycles/available \
  -H "X-API-Key: TU_API_KEY"
```

También dejé una colección de Postman en la raíz: `urban-bike-api.postman_collection.json`. En producción configurá la variable `apiKey` en Postman.

## Reglas de negocio implementadas

**Tarifas por tipo de bicicleta.** Cada tipo tiene una tarifa por hora definida en el enum `BikeType`: URBAN $3.500, MOUNTAIN $5.000, ELECTRIC $7.500. La multa por retraso es el 50 % de esa tarifa horaria, aplicada por cada hora de demora (también redondeada al alza).

**Redondeo al alza y mínimo de 1 hora.** El tiempo real se mide en minutos entre `startTime` y `endTime`, se convierte a horas con `ceil` (cualquier fracción cuenta como hora completa), y si el resultado da 0 horas igual se cobra 1 hora mínima. Ejemplo: si alquilás 30 segundos, se factura 1 hora.

**Multa por devolver tarde.** Si el cliente se pasa del tiempo estimado, se calculan minutos de retraso y se cobra la penalización por horas de demora (mínimo 1 hora de multa si hubo cualquier retraso). Ejemplo con MOUNTAIN, 2 horas estimadas y devolución a las 3 h 20 min: son 4 horas reales de uso ($20.000) más 2 horas de demora × $2.500 = $5.000 de multa → **total $25.000**.

## Cómo correr los tests

```bash
./mvnw test
```

En Windows: `mvnw.cmd test`

Son **14 tests** en total: 7 para el cálculo de facturación (`RentalCalculatorServiceTest`), 6 para la capa de servicio con mocks (`RentalServiceImplTest`), y 1 que verifica que el contexto de Spring levanta (`ApplicationTests`).

Los **13 tests** de lógica y servicios no necesitan base de datos. El test de contexto (`ApplicationTests`) levanta H2 en perfil `dev`.

## Despliegue en Render (nube)

**¿Por qué no Vercel?** Vercel está pensado para frontends y funciones serverless cortas. Esta API es un JAR de Spring Boot con JVM; encaja mejor en **Render**.

### Pasos en Render

1. Subí el repo a **GitHub** (público).
2. En [render.com](https://render.com) → **New** → **Blueprint** y conectá el repo (usa el `render.yaml` de la raíz).
3. Render crea la base PostgreSQL y el Web Service Java automáticamente.
4. Copiá el valor de `API_KEY` que genera Render (Environment → `API_KEY`).
5. Probá: `GET https://tu-app.onrender.com/health` (sin key) y luego los endpoints con `X-API-Key`.

Variables que Render configura vía `render.yaml`:

- `SPRING_PROFILES_ACTIVE=prod`
- `API_KEY` (generada)
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (desde la base)

El plan gratis de Render **duerme** el servicio tras inactividad; el primer request puede tardar ~30 s en despertar.

## Decisiones de diseño

**H2 en local, PostgreSQL en Render.** En la PC uso H2 para no depender de Docker ni instalar Postgres; en la nube uso PostgreSQL porque los datos deben persistir. El código JPA es el mismo en ambos.

**`RentalCalculatorService` aparte.** La facturación tiene reglas que cambian fácil (redondeos, mínimos, multas). Al no mezclarla con JPA, la probé con tests rápidos sin mocks de base de datos.

**DTOs de request y response.** No expongo las entidades JPA directo en la API porque no quiero filtrar campos internos ni acoplar el contrato HTTP al modelo de persistencia.

**Inyección por constructor.** Dependencias explícitas; en tests con Mockito paso los mocks por constructor sin trucos.

**API Key en producción.** Es seguridad básica y demostrable sin montar JWT/OAuth para una prueba de practicante. HTTPS lo provee Render.

## Supuestos

- El tiempo mínimo facturable de un alquiler es **1 hora**, aunque dure menos.
- El retraso mínimo facturable como multa es **1 hora**, si hubo cualquier minuto de demora.
- Los minutos parciales **siempre redondean hacia arriba** al calcular horas.
- Los enums en JSON usan nombres en inglés (`URBAN`, `AVAILABLE`) por convención Java; el dominio es el mismo del enunciado.
- Incluí la colección de Postman en la raíz del proyecto.

Si el evaluador nota alguna decisión que pudo tomarse diferente, quedé abierta a discutirla — el enunciado da libertad arquitectónica y estas fueron mis decisiones justificadas.
