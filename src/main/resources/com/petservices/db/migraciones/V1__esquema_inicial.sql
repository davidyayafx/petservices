-- ============================================================================
-- PetServices · Esquema inicial de la base de datos local (SQLite)
-- Versión 1 · Fase 2
--
-- Este script se ejecuta una sola vez, dentro de una transacción, cuando la
-- base de datos se crea. Las modificaciones posteriores deben añadirse como
-- una nueva migración versionada.
-- ============================================================================

-- ── Clientes / usuarios ─────────────────────────────────────────────────────
CREATE TABLE clientes (
    id_cliente      INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre          TEXT    NOT NULL CHECK (length(trim(nombre)) > 0),
    correo          TEXT    NOT NULL CHECK (length(trim(correo)) > 0),
    contrasena      TEXT,
    fecha_registro  TEXT    NOT NULL
);

-- El correo es único sin distinguir mayúsculas (índice sobre lower()).
CREATE UNIQUE INDEX ux_clientes_correo ON clientes(lower(correo));

-- ── Establecimientos ────────────────────────────────────────────────────────
CREATE TABLE establecimientos (
    id_establecimiento INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre             TEXT NOT NULL CHECK (length(trim(nombre)) > 0),
    tipo               TEXT NOT NULL CHECK (length(trim(tipo)) > 0),
    direccion          TEXT,
    telefono           TEXT
);

CREATE UNIQUE INDEX ux_establecimientos_nombre ON establecimientos(lower(nombre));

-- ── Servicios ───────────────────────────────────────────────────────────────
-- La referencia a mascotas es opcional y se declara aquí; SQLite permite
-- crear la tabla antes que la tabla referenciada.
CREATE TABLE servicios (
    id_servicio INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo        TEXT    NOT NULL CHECK (length(trim(tipo)) > 0),
    nombre      TEXT    NOT NULL CHECK (length(trim(nombre)) > 0),
    id_mascota  INTEGER REFERENCES mascotas(id_mascota) ON DELETE SET NULL
);

CREATE UNIQUE INDEX ux_servicios_tipo_nombre ON servicios(lower(tipo), lower(nombre));

-- ── Mascotas ────────────────────────────────────────────────────────────────
CREATE TABLE mascotas (
    id_mascota      INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre          TEXT    NOT NULL CHECK (length(trim(nombre)) > 0),
    especie         TEXT,
    raza            TEXT    NOT NULL CHECK (length(trim(raza)) > 0),
    edad            INTEGER NOT NULL DEFAULT 0 CHECK (edad >= 0),
    id_cliente      INTEGER NOT NULL REFERENCES clientes(id_cliente) ON DELETE RESTRICT,
    nombre_cliente  TEXT,
    id_servicio     INTEGER REFERENCES servicios(id_servicio) ON DELETE SET NULL
);

CREATE INDEX ix_mascotas_cliente ON mascotas(id_cliente);

-- ── Citas ───────────────────────────────────────────────────────────────────
CREATE TABLE citas (
    id_cita             INTEGER PRIMARY KEY AUTOINCREMENT,
    id_cliente          INTEGER NOT NULL REFERENCES clientes(id_cliente) ON DELETE RESTRICT,
    nombre_cliente      TEXT    NOT NULL,
    id_mascota          INTEGER NOT NULL REFERENCES mascotas(id_mascota) ON DELETE RESTRICT,
    nombre_mascota      TEXT    NOT NULL,
    fecha_hora          TEXT    NOT NULL CHECK (length(trim(fecha_hora)) > 0),
    servicio            TEXT    NOT NULL CHECK (length(trim(servicio)) > 0),
    estado              TEXT    NOT NULL CHECK (estado IN ('Pendiente', 'Confirmada', 'Cancelada', 'Completada')),
    id_establecimiento  INTEGER REFERENCES establecimientos(id_establecimiento) ON DELETE SET NULL,
    id_servicio         INTEGER REFERENCES servicios(id_servicio) ON DELETE SET NULL
);

CREATE INDEX ix_citas_fecha ON citas(fecha_hora);
CREATE INDEX ix_citas_cliente ON citas(id_cliente);
CREATE INDEX ix_citas_mascota ON citas(id_mascota);

-- Una mascota no puede tener dos citas activas en el mismo horario.
-- Las citas canceladas quedan fuera del índice para permitir reagendar.
CREATE UNIQUE INDEX ux_citas_agenda
    ON citas(id_mascota, fecha_hora)
    WHERE estado <> 'Cancelada';

-- ── Productos / inventario ──────────────────────────────────────────────────
CREATE TABLE productos (
    id_producto         INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre              TEXT    NOT NULL CHECK (length(trim(nombre)) > 0),
    categoria           TEXT    NOT NULL CHECK (length(trim(categoria)) > 0),
    precio              REAL    NOT NULL CHECK (precio > 0),
    stock               INTEGER NOT NULL CHECK (stock >= 0),
    descripcion         TEXT,
    estado              TEXT    NOT NULL CHECK (estado IN ('disponible', 'agotado')),
    id_establecimiento  INTEGER REFERENCES establecimientos(id_establecimiento) ON DELETE SET NULL,
    id_cliente          INTEGER REFERENCES clientes(id_cliente) ON DELETE SET NULL
);

CREATE UNIQUE INDEX ux_productos_nombre ON productos(lower(nombre));
CREATE INDEX ix_productos_categoria ON productos(categoria);

-- ── Pedidos ─────────────────────────────────────────────────────────────────
CREATE TABLE pedidos (
    id_pedido           INTEGER PRIMARY KEY AUTOINCREMENT,
    id_cliente          INTEGER REFERENCES clientes(id_cliente) ON DELETE SET NULL,
    nombre_cliente      TEXT,
    fecha               TEXT    NOT NULL CHECK (length(trim(fecha)) > 0),
    total               REAL    NOT NULL CHECK (total >= 0),
    estado              TEXT    NOT NULL CHECK (estado IN ('Pendiente', 'Pagado', 'Entregado')),
    cantidad            INTEGER NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
    id_establecimiento  INTEGER REFERENCES establecimientos(id_establecimiento) ON DELETE SET NULL
);

CREATE INDEX ix_pedidos_cliente ON pedidos(id_cliente);
CREATE INDEX ix_pedidos_fecha ON pedidos(fecha);

-- ── Detalle de pedido ───────────────────────────────────────────────────────
CREATE TABLE detalle_pedido (
    id_detalle        INTEGER PRIMARY KEY AUTOINCREMENT,
    id_pedido         INTEGER NOT NULL REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
    id_producto       INTEGER REFERENCES productos(id_producto) ON DELETE SET NULL,
    nombre_producto   TEXT    NOT NULL,
    cantidad          INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario   REAL    NOT NULL CHECK (precio_unitario >= 0),
    subtotal          REAL    NOT NULL CHECK (subtotal >= 0)
);

CREATE INDEX ix_detalle_pedido ON detalle_pedido(id_pedido);
CREATE INDEX ix_detalle_producto ON detalle_pedido(id_producto);

-- ── Configuración interna ───────────────────────────────────────────────────
CREATE TABLE configuracion (
    clave TEXT PRIMARY KEY,
    valor TEXT NOT NULL
);
