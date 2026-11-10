# 🐾 PetServices (PetConnect) — SENA 

Aplicación web Java (**Servlets 4.0 + JSP**) con **API REST JSON**, empaquetada como
`petservices.war` para **Apache Tomcat 9**. Gestiona clientes, mascotas, citas, servicios,
productos y pedidos de una clínica veterinaria / petshop.

| Evidencia | Contenido |
|---|---|
| **AA2-EV02** | Módulo web (Servlets + JSP): registro, login y CRUD de clientes |
| **AA5-EV03** | Servicios web REST en JSON — ver [`DOCUMENTACION_API.md`](./DOCUMENTACION_API.md) |
| **AA7-EV01** | Unión de anexos (dashboard, servicios, páginas de error), pruebas y despliegue |

> Documentación general del repositorio: [`../README.md`](../README.md) y
> [`../DOCUMENTACION/`](../DOCUMENTACION/).

---

## Requisitos

| Componente | Versión | Nota |
|---|---|---|
| JDK | 11+ (probado con 21 y 26) | `source/target 11` |
| Maven | 3.9.16 | **No está en el PATH**: usar `..\herramientas\apache-maven\bin\mvn.cmd` |
| Tomcat | 9.x | Portátil en `..\herramientas\tomcat9` (puerto 8080) |

No hace falta instalar Maven ni Tomcat: están en `herramientas/`, junto al proyecto.

---

## Paquete portable (sin instalar nada)

Si solo necesitas **ver la aplicación funcionando**, no hace falta compilar ni configurar Tomcat:
usa el paquete ya armado que está en **Releases**.

**Descarga publicada:** [`PetServices-Portable.zip` — v1.0.0](https://github.com/davidyayafx/petservices/releases/tag/v1.0.0) (13 MB).
**Versión vigente local (2026-11-10):** `..\EVIDENCIAS_SENA\Paquete_Portable\PetServices-Portable-v1.1.0.zip`
(26,7 MB), con copias de seguridad y restauración.

1. Descomprime el ZIP en una carpeta propia.
2. Doble clic en `Iniciar-PetServices.bat` (requiere **Java 11+**; el lanzador avisa si falta).
3. Se abre el navegador en `http://localhost:8080/petservices/login`.
4. Para cerrar: `Detener-PetServices.bat`.

Cuenta de prueba: `alejandro@gmail.com` / `123456` (también `daniel@gmail.com` y `david@gmail.com`).

El paquete incluye Tomcat 9 portátil, el WAR compilado, el código fuente, el icono y un `LEEME.txt`.
Para compilar desde el código, usa las secciones siguientes.

---

## Build y despliegue

### 1. Compilar el WAR

```powershell
cd petservices
..\herramientas\apache-maven\bin\mvn.cmd -o clean package
```

Salida: `target/petservices.war` (~14,8 MB, incluye el driver SQLite).
`-o` compila en modo offline con el repositorio local ya descargado. La primera compilación
necesita internet para descargar `sqlite-jdbc`; después queda en `~/.m2`.

O bien, desde la raíz del repositorio:

```powershell
powershell -ExecutionPolicy Bypass -File ..\scripts\compilar_war.ps1 -Limpiar
```

### 2. Desplegar en Tomcat

```powershell
powershell -ExecutionPolicy Bypass -File ..\scripts\desplegar_tomcat.ps1
```

Copia el WAR a `herramientas/tomcat9/webapps`, arranca Tomcat y espera a que responda
`http://localhost:8080/petservices/login`.

Detener:

```powershell
$env:CATALINA_HOME = "C:\Users\USER\Documents\Proyectos PROGRAMACION\proyecto grupo SENA\herramientas\tomcat9"
$env:CATALINA_BASE = $env:CATALINA_HOME
& "$env:CATALINA_HOME\bin\shutdown.bat"
```

> Los `.bat` `desplegar.bat` e `iniciar-local.bat` asumen `mvn` en el PATH; en este equipo
> no está, así que use los scripts de PowerShell.

### 3. Alternativa: plugin Maven embebido

```powershell
..\herramientas\apache-maven\bin\mvn.cmd tomcat7:run -Dapp.port=8081
# http://localhost:8081/petservices/login
```

Detalle completo (despliegue limpio, configuración y problemas frecuentes) en
[`../DOCUMENTACION/INSTALACION.md`](../DOCUMENTACION/INSTALACION.md).

---

## Cuentas de prueba

Datos iniciales que se cargan una sola vez en `datos/petservices.db` y después se conservan:

| Correo | Contraseña |
|---|---|
| `alejandro@gmail.com` | `123456` |
| `daniel@gmail.com` | `123456` |
| `david@gmail.com` | `123456` |

> También puedes crear una cuenta nueva desde `/registro` o con `POST /api/clientes`.

---

## URLs del módulo

| URL (`http://localhost:8080/petservices/…`) | Descripción |
|---|---|
| `/` | Redirige a login o al dashboard si hay sesión |
| `/login` · `/registro` | Acceso y creación de cuenta |
| `/dashboard` | KPIs y últimas 5 citas |
| `/clientes` | CRUD de clientes |
| `/mascotas` | CRUD de mascotas (vista pasaporte y tabla) |
| `/citas` | CRUD de citas |
| `/servicios` | CRUD del catálogo de servicios |
| `/productos` | CRUD de productos |
| `/pedidos` | CRUD de pedidos |
| `/logout` | Cierre de sesión |
| `/api/…` | API REST (ver abajo) |

---

## API REST

| Recurso | Ruta base | Servlet |
|---|---|---|
| Autenticación | `/api/auth/login` | `AuthApiServlet` |
| Clientes | `/api/clientes` | `ClienteApiServlet` |
| Mascotas | `/api/mascotas` | `MascotaApiServlet` |
| Establecimientos | `/api/establecimientos` | `EstablecimientoApiServlet` |
| Servicios | `/api/servicios` | `ServicioApiServlet` |
| Citas | `/api/citas` | `CitaApiServlet` |
| Productos | `/api/productos` | `ProductoApiServlet` |
| Pedidos | `/api/pedidos` | `PedidoApiServlet` |

Métodos, cuerpos JSON, filtros y códigos de estado en
**[`DOCUMENTACION_API.md`](./DOCUMENTACION_API.md)**.

Notas del contrato vigente:

- **Autenticación:** `POST /api/auth/login` devuelve un `token`; el resto de rutas exigen
  `Authorization: Bearer <token>` (8 h de vigencia). `POST /api/auth/logout` lo revoca.
- Las respuestas **no incluyen** `contrasena`; se guarda hasheada con PBKDF2.
- Cuerpo vacío o JSON mal formado → `400`.
- `POST /api/citas` acepta `fecha` + `hora` o `fechaHora`.
- El estado de una cita se acepta en cualquier caja y se guarda canónico
  (`Pendiente`, `Confirmada`, `Cancelada`, `Completada`).
- Un `PUT` parcial conserva los campos no enviados (incluida la hora).

Prueba rápida (hace login y prueba los 14 casos):

```powershell
powershell -ExecutionPolicy Bypass -File ..\scripts\probar_api.ps1
```

---

## Estructura

```
petservices/
├── pom.xml                        ← Maven: WAR, finalName, dependencias, plugin Tomcat
├── desplegar.bat                  ← Build + verificación del WAR (requiere mvn en PATH)
├── iniciar-local.bat              ← mvn tomcat7:run en el puerto 8081
├── DOCUMENTACION_API.md           ← Contrato de la API (única copia vigente)
├── postman/                       ← Colección y entorno de Postman
└── src/main/
    ├── java/com/petservices/
    │   ├── modelo/                ← JavaBeans del modelo (8 clases)
    │   ├── db/                    ← SQLite: conexión, migraciones, semillas, errores
    │   ├── dao/                   ← 7 DAO + 7 interfaces I*DAO (JDBC sobre SQLite)
    │   ├── service/               ← PedidoService (pedido + stock transaccional)
    │   ├── servlet/               ← Registro, Login, Logout, Cliente
    │   ├── controller/            ← BaseModuleServlet + Dashboard + 6 CRUD
    │   └── api/                   ← 7 *ApiServlet + AuthApiServlet + util/ApiUtil
    └── webapp/
        ├── index.jsp
        ├── vistas/                ← login, registro, dashboard, 6 CRUD, error404/500/503
        ├── css/app.css
        └── WEB-INF/
            ├── web.xml            ← welcome-file, sesión 30 min, páginas de error
            └── jsp/               ← head, sidebar, scripts, util, modal-confirmacion
```

Arquitectura detallada en [`../DOCUMENTACION/ARQUITECTURA.md`](../DOCUMENTACION/ARQUITECTURA.md).

---

## Métodos HTTP implementados

### Panel web (JSP)

| Servlet | `doGet` | `doPost` |
|---|---|---|
| `RegistroServlet` | Formulario de registro | Crear cliente |
| `LoginServlet` | Formulario de login | Validar y abrir sesión |
| `DashboardServlet` | KPIs + últimas citas | — |
| `ClienteServlet` | Listar, editar, eliminar | Crear, actualizar |
| `MascotaServlet` | Listar, editar | Crear, actualizar, eliminar |
| `CitaServlet` | Listar, editar | Crear, actualizar, eliminar |
| `ServicioServlet` | Listar, editar, eliminar | Crear, actualizar |
| `ProductoServlet` | Listar, editar | Crear, actualizar, eliminar |
| `PedidoServlet` | Listar, editar | Crear, actualizar, eliminar |
| `LogoutServlet` | Invalidar sesión | — |

### API REST

| Servlet | `GET` | `POST` | `PUT` | `DELETE` |
|---|---|---|---|---|
| `AuthApiServlet` | — | Login | — | — |
| `ClienteApiServlet` | Listar / por id | Crear | Actualizar | Eliminar |
| `MascotaApiServlet` | Listar / por id / por cliente | Crear | Actualizar | Eliminar |
| `EstablecimientoApiServlet` | Listar / por id | Crear | Actualizar | Eliminar |
| `ServicioApiServlet` | Listar / por id | Crear | Actualizar | Eliminar |
| `CitaApiServlet` | Listar / por id / por estado | Agendar | Actualizar | Eliminar |
| `ProductoApiServlet` | Listar / por id / stock bajo | Crear | Actualizar | Eliminar |
| `PedidoApiServlet` | Listar / por id | Crear | Actualizar | Eliminar |

---

## Tecnologías

- **Java 11** (compilado con JDK 21/26) — lógica y Servlets
- **javax.servlet 4.0** + **JSP 2.3** — vistas renderizadas en servidor
- **Gson 2.10.1** — (de)serialización JSON
- **Apache Tomcat 9** — servidor de aplicaciones
- **Maven 3.9** — build y dependencias
- **SQLite** (`org.xerial:sqlite-jdbc`) — base local embebida en `datos/petservices.db`

---

## Limitaciones conocidas

- **Persistencia completa**: clientes, mascotas, citas, servicios, productos, pedidos y
  establecimientos están en SQLite y sobreviven a los reinicios.
- **La API exige token Bearer**; las contraseñas se guardan con PBKDF2-HMAC-SHA256.
- **Pruebas**: 56 unitarias (JUnit 5) + 14 casos de humo de API; ejecute
  `..\scripts\verificar_todo.ps1`.
- **Copias**: la página `/respaldos` crea, descarga y restaura copias en `..\datos\copias\`,
  con copia automática al arrancar y retención configurable. Los scripts
  `respaldar_bd.ps1` y `restaurar_bd.ps1` operan con el servidor detenido.
- **Stock de pedidos**: se descuenta al crear, se ajusta al editar y se devuelve al eliminar,
  todo dentro de una transacción SQLite.
- Los borrados del panel van por **POST con token CSRF** (ya no por GET).
- Los pedidos **descuentan stock** al crearse y lo devuelven al editarse o eliminarse.
- `desplegar_tomcat.ps1 -Accion Detener` necesita `CATALINA_HOME` (ver Instalación).

Análisis completo y pendientes en
[`../DOCUMENTACION/CAMBIOS_REALIZADOS.md`](../DOCUMENTACION/CAMBIOS_REALIZADOS.md) y
[`../DOCUMENTACION/FUNCIONAMIENTO.md`](../DOCUMENTACION/FUNCIONAMIENTO.md).

---

## Datos del proyecto

- **Sistema:** PetServices / PetConnect
- **Ficha SENA:** 3186628
- **Evidencias:** GA7-220501096-AA2-EV02 · GA7-220501096-AA5-EV03 · GA10-220501097-AA7-EV01 · AA10-EV01 (manual técnico)
- **Competencia:** Módulos de software codificados y probados
- **Repositorio:** `https://github.com/davidyayafx/PetServices` (rama `main`)
- **Manual técnico:** [`../EVIDENCIAS_SENA/AA10_Documentacion_Tecnica/README.md`](../EVIDENCIAS_SENA/AA10_Documentacion_Tecnica/README.md)
