# 🐾 PetServices (PetConnect) — SENA 

Proyecto web Java con **Servlets + JSP** para las evidencias de competencia del SENA
(ficha 3186628). Implementa la gestión de clientes (interfaz web) y, a partir de la
evidencia AA5-EV03, una **API REST en JSON** que expone las operaciones sobre todas
las entidades del modelo físico `petconnect_completo.sql`.

| Evidencia | Contenido |
|---|---|
| **AA2-EV02** | Módulo web (Servlets + JSP): registro, login y CRUD de clientes |
| **AA5-EV03** | Diseño y codificación de los **servicios web (API REST)** del proyecto — ver [`DOCUMENTACION_API.md`](./DOCUMENTACION_API.md) |
| **AA7-EV01** | Unión de anexos (Dashboard, Servicios, páginas de error), pruebas funcionales y despliegue — informe GA10-220501097-AA7-EV01 |

---

## 🌐 API REST (AA5-EV03)

8 servicios REST en JSON bajo `/api`, uno por entidad del modelo físico, más el login por API:

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

Documentación completa de cada endpoint (métodos, cuerpos de petición, códigos de
estado y ejemplos con cURL) en **[`DOCUMENTACION_API.md`](./DOCUMENTACION_API.md)**.

---

## 📁 Estructura del Proyecto

```
petservices/                              ← Raíz del proyecto Maven
│
├── pom.xml                               ← Configuración Maven (dependencias + plugin Tomcat)
├── desplegar.bat                         ← Script de build y verificación
├── iniciar-local.bat                     ← Arranque rápido con mvn tomcat7:run
└── src/
    └── main/
        │
        ├── java/com/petservices/         ← Código fuente Java
        │   ├── modelo/                   ← Entidades JavaBean del modelo físico
        │   │   ├── Cliente.java
        │   │   ├── Mascota.java
        │   │   ├── Establecimiento.java
        │   │   ├── Servicio.java
        │   │   ├── Cita.java
        │   │   ├── Producto.java
        │   │   └── Pedido.java
        │   ├── dao/                      ← Acceso a datos (simulado en RAM, 1 DAO por entidad)
        │   │   ├── ClienteDAO.java
        │   │   ├── MascotaDAO.java
        │   │   ├── EstablecimientoDAO.java
        │   │   ├── ServicioDAO.java
        │   │   ├── CitaDAO.java
        │   │   ├── ProductoDAO.java
        │   │   └── PedidoDAO.java
        │   ├── servlet/                  ← Controladores web (JSP), evidencia AA2-EV02
        │   │   ├── RegistroServlet.java  ← Controlador: /registro
        │   │   ├── LoginServlet.java     ← Controlador: /login
        │   │   ├── ClienteServlet.java   ← Controlador: /clientes (CRUD)
        │   │   └── LogoutServlet.java    ← Controlador: /logout
        │   ├── controller/               ← Controladores CRUD web (AA7-EV01)
        │   │   ├── BaseModuleServlet.java    ← Base común (validación sesión, helpers)
        │   │   ├── DashboardServlet.java     ← Controlador: /dashboard (KPIs)
        │   │   ├── MascotaServlet.java       ← Controlador: /mascotas (CRUD)
        │   │   ├── CitaServlet.java          ← Controlador: /citas (CRUD)
        │   │   ├── ServicioServlet.java      ← Controlador: /servicios (CRUD)
        │   │   ├── ProductoServlet.java      ← Controlador: /productos (CRUD)
        │   │   └── PedidoServlet.java        ← Controlador: /pedidos (CRUD)
        │   └── api/                      ← Servicios REST JSON, evidencia AA5-EV03
        │       ├── util/ApiUtil.java     ← JSON, lectura de body, extracción de id
        │       ├── AuthApiServlet.java       → /api/auth/login
        │       ├── ClienteApiServlet.java    → /api/clientes
        │       ├── MascotaApiServlet.java    → /api/mascotas
        │       ├── EstablecimientoApiServlet.java → /api/establecimientos
        │       ├── ServicioApiServlet.java   → /api/servicios
        │       ├── CitaApiServlet.java       → /api/citas
        │       ├── ProductoApiServlet.java   → /api/productos
        │       └── PedidoApiServlet.java     → /api/pedidos
        │
        └── webapp/                       ← Recursos web (WEB-INF, JSPs, CSS)
            ├── index.jsp                 ← Redirección automática
            ├── vistas/
            │   ├── registro.jsp          ← Formulario de registro
            │   ├── login.jsp             ← Formulario de inicio de sesión
            │   ├── dashboard.jsp         ← Dashboard con KPIs y últimas citas
            │   ├── clientes.jsp          ← Panel CRUD de gestión
            │   ├── mascotas.jsp          ← Panel CRUD mascotas
            │   ├── citas.jsp             ← Panel CRUD citas
            │   ├── servicios.jsp         ← Panel CRUD servicios
            │   ├── productos.jsp         ← Panel CRUD productos
            │   ├── pedidos.jsp           ← Panel CRUD pedidos
            │   ├── error404.jsp          ← Página 404 personalizada
            │   └── error500.jsp          ← Página 500 personalizada
            └── WEB-INF/
                └── web.xml               ← Descriptor de despliegue
```

---

## 🚀 Cómo ejecutar en Tomcat

### Opción A — Eclipse / IntelliJ con Tomcat integrado
1. Importar como **Maven Project**
2. Configurar un servidor **Apache Tomcat 9.x** en el IDE
3. Click derecho → **Run on Server**
4. Abrir: `http://localhost:8080/petservices/`

### Opción B — Maven + Tomcat manual
```bash
# Compilar y empacar
mvn clean package

# El archivo .war quedará en: target/petservices.war
# Copiarlo a: TOMCAT_HOME/webapps/petservices.war
# Iniciar Tomcat: TOMCAT_HOME/bin/startup.sh (Linux) o startup.bat (Windows)
```

### Cuentas de prueba (precargadas en memoria)
| Correo                    | Contraseña |
|---------------------------|-----------|
| alejandro@gmail.com       | 123456    |
| daniel@gmail.com          | 123456    |
| david@gmail.com           | 123456    |

---

## 🌐 URLs del módulo

| URL                                 | Descripción                      |
|-------------------------------------|----------------------------------|
| `http://localhost:8080/petservices/`        | Redirige al login automáticamente |
| `http://localhost:8080/petservices/login`   | Formulario de inicio de sesión   |
| `http://localhost:8080/petservices/registro`| Formulario de registro           |
| `http://localhost:8080/petservices/dashboard`| Dashboard con KPIs y últimas citas (requiere sesión) |
| `http://localhost:8080/petservices/clientes`| Panel CRUD clientes (requiere sesión)     |
| `http://localhost:8080/petservices/mascotas`| Panel CRUD mascotas (requiere sesión)     |
| `http://localhost:8080/petservices/citas`   | Panel CRUD citas (requiere sesión)        |
| `http://localhost:8080/petservices/servicios`| Panel CRUD servicios (requiere sesión)    |
| `http://localhost:8080/petservices/productos`| Panel CRUD productos (requiere sesión)    |
| `http://localhost:8080/petservices/pedidos` | Panel CRUD pedidos (requiere sesión)      |
| `http://localhost:8080/petservices/logout`  | Cierre de sesión                 |

---

## 🔄 Flujo de la aplicación

```
index.jsp
    ↓ (sin sesión)
login.jsp  ←──────────────── logout
    ↓ (doPost: validar)
LoginServlet
    ↓ (sesión creada)
ClienteServlet (doGet: listar)
    ↓
clientes.jsp
    ├── Crear  → doPost accion=crear
    ├── Editar → doGet  accion=editar → doPost accion=actualizar
    └── Borrar → doGet  accion=eliminar
```

---

## 📋 Métodos HTTP implementados

### Módulo web (JSP) — AA2-EV02 + AA7-EV01

| Servlet           | doGet                              | doPost                         |
|-------------------|------------------------------------|-------------------------------|
| RegistroServlet   | Cargar formulario vacío            | Procesar y guardar nuevo cliente |
| LoginServlet      | Cargar formulario de login         | Validar credenciales, crear sesión |
| DashboardServlet  | Mostrar KPIs y últimas 5 citas     | —                              |
| ClienteServlet    | Listar, cargar edición, eliminar   | Crear cliente, actualizar cliente |
| MascotaServlet    | Listar, cargar edición, eliminar   | Crear mascota, actualizar mascota |
| CitaServlet       | Listar, cargar edición, eliminar   | Crear cita, actualizar cita    |
| ServicioServlet   | Listar, cargar edición, eliminar   | Crear servicio, actualizar servicio |
| ProductoServlet   | Listar, cargar edición, eliminar   | Crear producto, actualizar producto |
| PedidoServlet     | Listar, cargar edición, eliminar   | Crear pedido, actualizar pedido |
| LogoutServlet     | Invalidar sesión, redirigir        | —                              |

### API REST (JSON) — AA5-EV03

| Servlet | doGet | doPost | doPut | doDelete |
|---|---|---|---|---|
| AuthApiServlet | — | Validar login | — | — |
| ClienteApiServlet | Listar / por id | Crear | Actualizar | Eliminar |
| MascotaApiServlet | Listar / por id / por cliente | Crear | Actualizar | Eliminar |
| EstablecimientoApiServlet | Listar / por id | Crear | Actualizar | Eliminar |
| ServicioApiServlet | Listar / por id | Crear | Actualizar | Eliminar |
| CitaApiServlet | Listar / por id / por estado | Crear (agendar) | Actualizar (fecha/hora/estado) | Cancelar |
| ProductoApiServlet | Listar / por id / stock bajo | Crear | Actualizar | Eliminar |
| PedidoApiServlet | Listar / por id | Crear | Actualizar | Eliminar |

Detalle de rutas, cuerpos JSON y códigos de estado en [`DOCUMENTACION_API.md`](./DOCUMENTACION_API.md).

---

## 🔧 Tecnologías usadas

- **Java 11** — Lógica de negocio y Servlets
- **javax.servlet 4.0** — API de Servlets
- **JSP 2.3** — Vistas dinámicas (scriptlets, expresiones)
- **Gson 2.10.1** — Serialización/deserialización JSON de los servicios REST (AA5-EV03)
- **Apache Tomcat 9.x** — Servidor de aplicaciones
- **Maven** — Gestión de dependencias
- **Simulación DAO en RAM** — Sin necesidad de BD para la demo (estructurada para migrar a JDBC)

---

## 💾 Comandos Git esenciales

Este proyecto ya tiene repositorio Git inicializado desde la evidencia AA2-EV02
(remoto: `https://github.com/SoyAbejo/Modulo-de-registro.git`). Para la evidencia
AA5-EV03 solo se agregan los nuevos archivos y se sube un nuevo commit:

```bash
cd petservices

# 1. Ver los archivos nuevos/modificados
git status

# 2. Agregar todo lo nuevo (modelos, DAOs, servicios REST, documentación)
git add .

# 3. Commit de la evidencia AA5-EV03
git commit -m "feat: servicios web REST del proyecto - GA7-220501096-AA5-EV03

- 8 servicios REST en JSON bajo /api (Cliente, Mascota, Establecimiento,
  Servicio, Cita, Producto, Pedido, Auth)
- Modelos JavaBean y DAO en memoria para cada entidad del modelo físico
- ApiUtil: utilidades comunes de JSON, lectura de body y extracción de id
- Gson agregado como dependencia Maven para (de)serialización JSON
- DOCUMENTACION_API.md: documentación completa de cada endpoint"

# 4. Subir el commit al repositorio remoto
git push origin main
```

Para la evidencia **AA7-EV01** (unión de anexos, pruebas y despliegue):

```bash
# 1. Ver los archivos nuevos/modificados
git status

# 2. Agregar todo lo nuevo (Dashboard, Servicios, error pages, postman, scripts, pom, web.xml, README)
git add .

# 3. Commit de la evidencia AA7-EV01
git commit -m "feat: unión de anexos + pruebas + despliegue - GA10-220501097-AA7-EV01

- DashboardServlet + dashboard.jsp con KPIs y últimas citas
- ServicioServlet + servicios.jsp (CRUD completo)
- Páginas error404.jsp y error500.jsp con diseño propio
- Colección Postman + environment en postman/
- Scripts desplegar.bat e iniciar-local.bat
- pom.xml: propiedades app.port/app.contextPath + plugin tomcat7-maven-plugin
- web.xml: declaraciones explícitas de los 10 servlets web
- README actualizado con nuevos módulos y URLs
- LoginServlet redirige a /dashboard tras login exitoso"

# 4. Subir el commit al repositorio remoto
git push origin main
```

# ── Commits futuros (buenas prácticas) ──
# git add .
# git commit -m "fix: validación de estado en CitaApiServlet"
# git push
```

---

## 📦 Formato de entrega SENA — AA5-EV03

Estructura interna del ZIP entregado (nombre: `NOMBRE_APELLIDO_AA5_EV03.zip`):

```
ALEJANDRO_PUERTO_AA5_EV03/
├── petservices/                  ← Proyecto completo (código fuente + API REST)
├── DOCUMENTACION_API.md          ← Documentación de cada servicio
└── enlace_repositorio.txt        ← URL del repositorio Git
```

---

## 👨‍💻 Datos del proyecto

- **Sistema:** PetServices / PetConnect
- **Ficha SENA:** 3186628
- **Evidencias:** GA7-220501096-AA2-EV02 (módulo web), GA7-220501096-AA5-EV03 (servicios web / API REST) y GA10-220501097-AA7-EV01 (pruebas funcionales y despliegue)
- **Competencia:** Módulos de software codificados y probados
- **Repositorio:** https://github.com/SoyAbejo/Modulo-de-registro
