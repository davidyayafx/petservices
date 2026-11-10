# Documentación de la API REST — PetServices (PetConnect)

**Evidencia:** GA7-220501096-AA5-EV03 — Diseño y desarrollo de servicios web
**Proyecto formativo:** PetConnect (gestión de clientes, mascotas, citas, establecimientos, servicios, productos y pedidos)
**Base URL local:** `http://localhost:8080/petservices`

Todos los servicios reciben y devuelven **JSON** (`Content-Type: application/json`) y siguen
el estilo arquitectónico **REST**: un recurso por URL, verbos HTTP para las operaciones
(`GET`, `POST`, `PUT`, `DELETE`) y códigos de estado HTTP estándar en la respuesta.

Capa técnica: `HttpServlet` (Jakarta/Java Servlet 4.0) + `Gson` para (de)serialización JSON.
Persistencia (Fases 2 y 3): SQLite local en `datos/petservices.db` para **todos los módulos**
(clientes, mascotas, citas, servicios, productos, pedidos/detalle y establecimientos). El
contrato JSON se mantiene; se añaden respuestas `409` para duplicados y borrados protegidos, y
validaciones de stock en pedidos.

---

## Convenciones generales

| Código HTTP | Significado en esta API |
|---|---|
| `200 OK` | Operación de lectura, actualización o eliminación exitosa |
| `201 Created` | Recurso creado correctamente |
| `400 Bad Request` | Faltan campos obligatorios o el valor enviado no es válido |
| `401 Unauthorized` | Credenciales incorrectas, o falta el token / no es válido |
| `404 Not Found` | El recurso solicitado (por id) no existe |
| `409 Conflict` | Duplicado (correo, servicio, cita en el mismo horario) o borrado bloqueado por registros relacionados |

### Autenticación obligatoria

**Todas** las rutas `/api/*` exigen la cabecera:

```
Authorization: Bearer <token>
```

La única excepción es `POST /api/auth/login`, que es público y devuelve el token.
Sin cabecera, o con un token inválido/caducado, la respuesta es `401 Unauthorized` con
`WWW-Authenticate: Bearer`. Los tokens duran **8 horas** y se pierden al reiniciar Tomcat.

Formato de error homogéneo:
```json
{ "error": "descripción del problema" }
```

Un cuerpo vacío o con JSON mal formado responde `400 Bad Request` con
`{ "error": "El cuerpo de la petición debe contener un JSON válido." }`.

Las respuestas que devuelven clientes (incluido el login) **no incluyen** el campo
`contrasena`: solo se acepta en el cuerpo de `POST`/`PUT` y en el login.

Los `PUT` son **parciales**: los campos que no se envían conservan su valor actual.

Si la ruta trae un id que no es numérico (por ejemplo `/api/clientes/abc`), la API
responde `404 Not Found` con `{ "error": "El id de la URL debe ser un número entero." }`.

---

## 1. Autenticación — `/api/auth`

### POST /api/auth/login
Valida credenciales y retorna los datos del cliente **más el token** de acceso.
Es la única ruta pública.

**Body:**
```json
{ "correo": "alejandro@gmail.com", "contrasena": "123456" }
```
**Respuesta 200:**
```json
{
  "idCliente": 1,
  "nombre": "Alejandro Puerto",
  "correo": "alejandro@gmail.com",
  "fechaRegistro": "2026-01-15",
  "token": "3fK9... (32 bytes en Base64 URL-safe)"
}
```
**Respuesta 401:** `{ "error": "Correo o contraseña incorrectos" }`

**Uso del token:**
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/petservices/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"alejandro@gmail.com","contrasena":"123456"}' | jq -r .token)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/petservices/api/clientes
```

### POST /api/auth/logout
Invalida el token enviado en `Authorization` (requiere token válido).

**Respuesta 200:** `{ "mensaje": "Sesión cerrada correctamente" }`

---

## 2. Clientes — `/api/clientes`

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/clientes` | Lista todos los clientes |
| GET | `/api/clientes/{id}` | Obtiene un cliente por id |
| POST | `/api/clientes` | Crea un cliente |
| PUT | `/api/clientes/{id}` | Actualiza un cliente |
| DELETE | `/api/clientes/{id}` | Elimina un cliente |

**Body para POST:**
```json
{ "nombre": "Daniel Mejia", "correo": "daniel@gmail.com", "contrasena": "123456" }
```
Reglas: `nombre` y `correo` obligatorios. El correo debe tener formato `usuario@dominio.tld`
(si no, `400 Bad Request`) y se guarda en **minúsculas**. Si el correo ya existe —al crear o al
actualizar— responde `409 Conflict`. El `DELETE` responde `409` si el cliente tiene mascotas o
citas asociadas; los pedidos conservan su nombre como historial y no bloquean el borrado.

---

## 3. Mascotas — `/api/mascotas`

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/mascotas` | Lista todas las mascotas |
| GET | `/api/mascotas/{id}` | Obtiene una mascota por id |
| GET | `/api/mascotas?idCliente={id}` | Lista las mascotas de un cliente |
| POST | `/api/mascotas` | Registra una mascota |
| PUT | `/api/mascotas/{id}` | Actualiza una mascota |
| DELETE | `/api/mascotas/{id}` | Elimina una mascota |

**Body para POST:**
```json
{ "raza": "Labrador", "nombre": "Max", "idCliente": 1 }
```
Reglas: `raza` y `nombre` obligatorios. Si se envía `idCliente`, debe existir (si no, `404`)
y se recalcula `nombreCliente` con el nombre del dueño.
El `PUT` es parcial: actualiza solo `nombre`, `especie`, `raza`, `edad`, `idCliente` e `idServicio`
cuando vienen en el cuerpo; si cambia el dueño, se vuelve a resolver `nombreCliente`.
El `DELETE` responde `409` si la mascota tiene citas asociadas.

---

## 4. Establecimientos — `/api/establecimientos`

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/establecimientos` | Lista todos los establecimientos |
| GET | `/api/establecimientos/{id}` | Obtiene un establecimiento por id |
| POST | `/api/establecimientos` | Crea un establecimiento |
| PUT | `/api/establecimientos/{id}` | Actualiza un establecimiento |
| DELETE | `/api/establecimientos/{id}` | Elimina un establecimiento |

**Body para POST:**
```json
{ "nombre": "Veterinaria Norte", "tipo": "veterinaria", "direccion": "Cra 5 #10-20 Tunja", "telefono": "3201234567" }
```
Reglas: `nombre` y `tipo` obligatorios y no vacíos. El nombre es único: un duplicado responde
`409 Conflict`. `tipo` corresponde a `petshop | veterinaria | mixto` (regla definida en el modelo
físico).

---

## 5. Servicios — `/api/servicios`

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/servicios` | Lista todos los tipos de servicio |
| GET | `/api/servicios/{id}` | Obtiene un servicio por id |
| POST | `/api/servicios` | Crea un servicio |
| PUT | `/api/servicios/{id}` | Actualiza un servicio |
| DELETE | `/api/servicios/{id}` | Elimina un servicio |

**Body para POST:**
```json
{ "tipo": "Consulta", "nombre": "Desparasitación" }
```
Reglas: `tipo` y `nombre` obligatorios y no vacíos. La combinación `tipo + nombre` es única:
un duplicado responde `409 Conflict` (también al actualizar).

---

## 6. Citas — `/api/citas`

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/citas` | Lista todas las citas |
| GET | `/api/citas/{id}` | Obtiene una cita por id |
| GET | `/api/citas?estado={estado}` | Filtra citas por estado |
| POST | `/api/citas` | Agenda una cita |
| PUT | `/api/citas/{id}` | Actualiza fecha, hora o estado |
| DELETE | `/api/citas/{id}` | Elimina definitivamente una cita |

**Body para POST:**
```json
{ "fecha": "2026-08-01", "hora": "10:00:00", "idMascota": 1, "idEstablecimiento": 1 }
```
Reglas: `fecha` y `hora` obligatorios. `estado` solo admite `pendiente | confirmada | cancelada | completada`
**sin distinguir mayúsculas** (si se envía uno distinto, responde `400 Bad Request`). El estado se guarda
en su forma canónica (`Pendiente`, `Confirmada`, `Cancelada`, `Completada`); si no se envía, se asigna
`Pendiente`. También se acepta `fechaHora` en formato `2026-08-01T10:00` en lugar del par `fecha` + `hora`.

`idMascota` es obligatorio al crear (`400` si falta, `404` si no existe). `idCliente` es opcional: si se
envía debe existir (`404`) y ser el dueño de la mascota (`400` si no coincide); si no se envía, se deduce
del dueño de la mascota. **Los nombres de cliente y mascota se rellenan desde los datos guardados**, no se
toman del cuerpo de la petición.

**PUT parcial:** si el cuerpo solo trae `estado`, la fecha y la hora existentes se conservan
(antes se perdía la hora). Lo mismo aplica a los demás campos no enviados.

**DELETE:** elimina la cita. Para **cancelarla sin borrarla**, use
`PUT /api/citas/{id}` con `{"estado":"cancelada"}`.

**Duplicados:** no se permite más de una cita **activa** para la misma mascota en el mismo
`fechaHora`; el intento responde `409 Conflict`. Las citas canceladas no cuentan, de modo que
se puede reagendar ese horario.

---

## 7. Productos — `/api/productos`

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/productos` | Lista todos los productos |
| GET | `/api/productos/{id}` | Obtiene un producto por id |
| GET | `/api/productos?stockMenorA={n}` | Lista productos con stock por debajo de `n` |
| POST | `/api/productos` | Crea un producto |
| PUT | `/api/productos/{id}` | Actualiza un producto |
| DELETE | `/api/productos/{id}` | Elimina un producto |

**Body para POST:**
```json
{ "nombre": "Collar antipulgas", "descripcion": "Talla M", "precio": 32000.0, "stock": 25, "idEstablecimiento": 2 }
```
Reglas: `nombre` y `categoria` obligatorios; `precio > 0`; `stock >= 0`. El nombre es único sin
distinguir mayúsculas: un duplicado responde `409 Conflict`. El `PUT` es parcial: `stock` (y el
resto de campos) solo se modifica cuando se envía; antes, omitir `stock` lo dejaba en 0. El
`estado` se calcula desde el stock (`disponible`/`agotado`).

---

## 8. Pedidos — `/api/pedidos`

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/pedidos` | Lista todos los pedidos |
| GET | `/api/pedidos/{id}` | Obtiene un pedido por id |
| POST | `/api/pedidos` | Crea un pedido |
| PUT | `/api/pedidos/{id}` | Actualiza estado, total o cantidad |
| DELETE | `/api/pedidos/{id}` | Elimina un pedido |

**Body para POST con producto (recomendado):**
```json
{ "idCliente": 1, "idProducto": 1, "cantidad": 2, "fecha": "2026-08-01", "estado": "Pendiente" }
```
El servidor valida el stock, calcula el total con el precio vigente, descuenta las unidades e
inserta pedido + detalle en una sola transacción.

**Body para POST con total manual (compatibilidad):**
```json
{ "total": 45000.0, "cantidad": 1, "idCliente": 1, "idEstablecimiento": 2 }
```
Reglas: `total` debe ser mayor que 0. Este modo no toca stock.

**PUT:** si envía `idProducto` o `cantidad`, se reemplaza el detalle y se ajusta el stock
(devuelve el anterior y descuenta el nuevo). Si solo envía `estado`/`total`, no se toca el
inventario. Un pedido sin stock suficiente responde `400` y no crea ni modifica nada.

---

## Ejemplo de prueba con cURL

```bash
# Listar clientes
curl -X GET http://localhost:8080/petservices/api/clientes

# Crear una mascota
curl -X POST http://localhost:8080/petservices/api/mascotas \
     -H "Content-Type: application/json" \
     -d '{"raza":"Bulldog","nombre":"Toby","idCliente":1}'

# Confirmar una cita
curl -X PUT http://localhost:8080/petservices/api/citas/2 \
     -H "Content-Type: application/json" \
     -d '{"estado":"confirmada"}'

# Eliminar un producto
curl -X DELETE http://localhost:8080/petservices/api/productos/3
```

---

## Verificación de compilación

El código fue compilado con `javac` (JDK 21, API objetivo `javax.servlet` 4.0 + Gson 2.10.1)
sin errores ni advertencias, confirmando la corrección sintáctica y de tipos de los 8 servicios
REST y sus respectivos modelos y DAOs.
