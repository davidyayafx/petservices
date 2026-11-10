package com.petservices.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Punto único de acceso a la base de datos local SQLite.
 *
 * Responsabilidades:
 *  - Resolver la ruta del archivo (propiedad, variable de entorno o carpeta datos).
 *  - Crear la carpeta y el archivo cuando sea necesario.
 *  - Aplicar los PRAGMA de integridad y durabilidad.
 *  - Ejecutar migraciones y cargar las semillas una sola vez.
 *  - Ofrecer conexiones y transacciones a los DAO.
 *
 * No mantiene datos de negocio en memoria: cada operación consulta el archivo.
 */
public final class BaseDatos {

    private static final Logger LOG = Logger.getLogger(BaseDatos.class.getName());

    /** Propiedad del sistema para elegir el archivo (pruebas, instalaciones especiales). */
    public static final String PROP_RUTA = "petservices.db.path";

    /** Si es true, borra la base antes de abrirla. Solo para pruebas. */
    public static final String PROP_REINICIAR = "petservices.db.reset";

    /** Variable de entorno para instalaciones portables. */
    public static final String ENV_RUTA = "PETSERVICES_DB";

    private static final String NOMBRE_ARCHIVO = "petservices.db";
    private static final String CARPETA_DATOS = "datos";
    private static final Object CANDADO = new Object();

    private static volatile boolean inicializada = false;
    private static boolean reinicioAplicado = false;
    private static volatile boolean restaurando = false;
    private static Path ruta;

    private BaseDatos() {
    }

    /** Inicializa la base si aún no se ha hecho. Es idempotente. */
    public static void inicializar() {
        if (inicializada) {
            return;
        }
        synchronized (CANDADO) {
            if (inicializada) {
                return;
            }
            try {
                ruta = resolverRuta();
                crearCarpeta(ruta);
                if (debeReiniciar()) {
                    borrarArchivos(ruta);
                }
                Class.forName("org.sqlite.JDBC");
                if (!restaurando) {
                    // Copia automática antes de las migraciones (Fase 4).
                    RespaldoService.copiaAutomaticaAlArrancar(ruta);
                }
                prepararEsquema(ruta);
                inicializada = true;
                LOG.info("Base de datos SQLite lista en: " + ruta.toAbsolutePath());
            } catch (BaseDatosException e) {
                throw e;
            } catch (ClassNotFoundException e) {
                throw new BaseDatosException(BaseDatosException.Codigo.CONEXION,
                        "No se encontró el driver de SQLite. Revisa las dependencias del proyecto.",
                        "No se pudo cargar org.sqlite.JDBC", e);
            } catch (Exception e) {
                throw new BaseDatosException(BaseDatosException.Codigo.CONEXION,
                        "No se pudo abrir la base de datos local. Revisa la carpeta datos y los permisos.",
                        "Error inicializando " + (ruta == null ? "la base" : ruta), e);
            }
        }
    }

    /** Devuelve una conexión nueva, ya configurada, a la base local. */
    public static Connection conexion() throws SQLException {
        esperarSiRestaurando();
        inicializar();
        return nuevaConexion();
    }

    /**
     * Ejecuta un bloque dentro de una transacción SQLite.
     * Si el bloque lanza una excepción, se hace rollback y se propaga el error.
     */
    public static <T> T enTransaccion(Operacion<T> operacion) {
        esperarSiRestaurando();
        inicializar();
        try (Connection conexion = nuevaConexion()) {
            conexion.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(conexion);
                conexion.commit();
                return resultado;
            } catch (SQLException e) {
                revertir(conexion);
                throw BaseDatosException.desde(e, "transacción");
            } catch (RuntimeException e) {
                revertir(conexion);
                throw e;
            } finally {
                try {
                    conexion.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // La conexión se cierra a continuación; no hay nada que recuperar.
                }
            }
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "apertura de conexión");
        }
    }

    /** Cierra la conexión lógica y fuerza un checkpoint de WAL. */
    public static void cerrar() {
        synchronized (CANDADO) {
            if (!inicializada || ruta == null) {
                return;
            }
            try (Connection conexion = nuevaConexion();
                 Statement sentencia = conexion.createStatement()) {
                sentencia.execute("PRAGMA wal_checkpoint(TRUNCATE)");
            } catch (SQLException e) {
                LOG.log(Level.FINE, "No se pudo hacer checkpoint al cerrar la base", e);
            } finally {
                inicializada = false;
            }
        }
    }

    /**
     * Cierra y vuelve a abrir la misma base sin borrarla.
     * Se usa en las pruebas para simular un reinicio del servidor.
     */
    public static void reiniciarParaPruebas() {
        cerrar();
        inicializar();
    }

    /**
     * Sustituye el archivo de la base por una copia ya validada.
     *
     * Bloquea las nuevas conexiones ({@link #conexion()} espera), cierra la base,
     * reemplaza el archivo (reintentando mientras Windows lo tenga bloqueado por
     * una operación en curso) y vuelve a abrirla aplicando migraciones. Si algo
     * falla, el llamador debe recuperar la copia de seguridad previa.
     */
    public static void sustituirArchivo(Path nuevo) throws IOException {
        if (nuevo == null || !Files.isRegularFile(nuevo)) {
            throw new IOException("La copia de seguridad no existe: " + nuevo);
        }
        synchronized (CANDADO) {
            if (ruta == null) {
                throw new IOException("La base de datos todavía no está inicializada.");
            }
            restaurando = true;
            try {
                cerrar();
                Path destino = ruta;
                Path temporal = destino.resolveSibling(destino.getFileName() + ".restaurando");
                Files.copy(nuevo, temporal, StandardCopyOption.REPLACE_EXISTING);
                try {
                    moverConReintentos(temporal, destino);
                } finally {
                    Files.deleteIfExists(temporal);
                }
                inicializada = false;
                inicializar();
            } finally {
                restaurando = false;
                CANDADO.notifyAll();
            }
        }
    }

    /** Ruta absoluta del archivo de base de datos, o null si aún no se inicializó. */
    public static Path ruta() {
        return ruta == null ? null : ruta.toAbsolutePath();
    }

    public static boolean estaInicializada() {
        return inicializada;
    }

    @FunctionalInterface
    public interface Operacion<T> {
        T ejecutar(Connection conexion) throws SQLException;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Internos
    // ──────────────────────────────────────────────────────────────────────

    /** Detiene a los hilos que piden conexión mientras se reemplaza el archivo. */
    private static void esperarSiRestaurando() {
        synchronized (CANDADO) {
            while (restaurando) {
                try {
                    CANDADO.wait(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BaseDatosException(BaseDatosException.Codigo.BLOQUEO,
                            "La base de datos está en mantenimiento. Inténtalo de nuevo en unos segundos.",
                            "Espera interrumpida durante una restauración", e);
                }
            }
        }
    }

    /**
     * Reemplaza el archivo de la base reintentando durante unos segundos: en
     * Windows la sustitución falla mientras otra operación tenga el archivo
     * abierto. Los archivos auxiliares de WAL se borran justo antes de mover.
     */
    private static void moverConReintentos(Path origen, Path destino) throws IOException {
        IOException ultimo = null;
        for (int intento = 0; intento < 30; intento++) {
            try {
                borrarAuxiliares(destino);
                Files.move(origen, destino, StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (IOException e) {
                ultimo = e;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException interrumpido) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Restauración interrumpida.", interrumpido);
                }
            }
        }
        throw ultimo != null ? ultimo
                : new IOException("No se pudo reemplazar el archivo de la base de datos.");
    }

    private static void borrarAuxiliares(Path archivo) {
        for (String sufijo : new String[]{"-wal", "-shm", "-journal"}) {
            try {
                Files.deleteIfExists(Paths.get(archivo.toString() + sufijo));
            } catch (IOException ignored) {
                // Si otro proceso lo mantiene abierto, el move fallará y se reintenta.
            }
        }
    }

    private static Connection nuevaConexion() throws SQLException {
        Connection conexion = DriverManager.getConnection("jdbc:sqlite:" + ruta.toAbsolutePath());
        configurar(conexion);
        return conexion;
    }

    private static void prepararEsquema(Path archivo) throws SQLException {
        try (Connection conexion = DriverManager.getConnection("jdbc:sqlite:" + archivo.toAbsolutePath())) {
            configurar(conexion);
            try (Statement sentencia = conexion.createStatement()) {
                sentencia.execute("PRAGMA journal_mode = WAL");
                sentencia.execute("PRAGMA wal_autocheckpoint = 1000");
            }
            conexion.setAutoCommit(false);
            try {
                Migrador.migrar(conexion);
                Semillas.cargar(conexion);
                conexion.commit();
            } catch (SQLException e) {
                revertir(conexion);
                throw new BaseDatosException(BaseDatosException.Codigo.ESQUEMA,
                        "No se pudo preparar la estructura de la base de datos.",
                        "Error ejecutando migraciones/semillas en " + archivo, e);
            } catch (RuntimeException e) {
                revertir(conexion);
                throw e;
            } finally {
                try {
                    conexion.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // La conexión se cierra a continuación.
                }
            }
        }
    }

    private static void configurar(Connection conexion) throws SQLException {
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA foreign_keys = ON");
            sentencia.execute("PRAGMA busy_timeout = 5000");
            sentencia.execute("PRAGMA synchronous = FULL");
            sentencia.execute("PRAGMA temp_store = MEMORY");
        }
        conexion.setAutoCommit(true);
    }

    private static void revertir(Connection conexion) {
        try {
            conexion.rollback();
        } catch (SQLException ignored) {
            // Si el rollback falla, se conserva la excepción original.
        }
    }

    private static boolean debeReiniciar() {
        if (reinicioAplicado) {
            return false;
        }
        boolean reiniciar = Boolean.parseBoolean(System.getProperty(PROP_REINICIAR, "false"));
        if (reiniciar) {
            reinicioAplicado = true;
        }
        return reiniciar;
    }

    private static void crearCarpeta(Path archivo) {
        Path carpeta = archivo.getParent();
        if (carpeta == null) {
            return;
        }
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new BaseDatosException(BaseDatosException.Codigo.DISCO,
                    "No se pudo crear la carpeta de datos. Verifica los permisos.",
                    "No se pudo crear " + carpeta, e);
        }
    }

    private static void borrarArchivos(Path archivo) {
        for (String sufijo : new String[]{"", "-wal", "-shm"}) {
            try {
                Files.deleteIfExists(Paths.get(archivo.toString() + sufijo));
            } catch (IOException e) {
                throw new BaseDatosException(BaseDatosException.Codigo.DISCO,
                        "No se pudo reiniciar la base de datos de pruebas.",
                        "No se pudo borrar " + archivo + sufijo, e);
            }
        }
    }

    /**
     * Prioridad:
     *  1. -Dpetservices.db.path
     *  2. PETSERVICES_DB
     *  3. <raíz del proyecto>/datos/petservices.db (layout con herramientas/tomcat9)
     *  4. %USERPROFILE%/PetServices/datos/petservices.db
     */
    private static Path resolverRuta() {
        String propiedad = System.getProperty(PROP_RUTA);
        if (propiedad != null && !propiedad.trim().isEmpty()) {
            return Paths.get(propiedad.trim()).toAbsolutePath();
        }

        String entorno = System.getenv(ENV_RUTA);
        if (entorno != null && !entorno.trim().isEmpty()) {
            return Paths.get(entorno.trim()).toAbsolutePath();
        }

        Path raiz = detectarRaizProyecto();
        if (raiz != null) {
            return raiz.resolve(CARPETA_DATOS).resolve(NOMBRE_ARCHIVO);
        }

        return Paths.get(System.getProperty("user.home"), "PetServices", CARPETA_DATOS, NOMBRE_ARCHIVO);
    }

    private static Path detectarRaizProyecto() {
        String catalina = System.getProperty("catalina.base");
        if (catalina == null || catalina.trim().isEmpty()) {
            return null;
        }
        Path base = Paths.get(catalina).toAbsolutePath();
        Path nombre = base.getFileName();
        Path herramientas = base.getParent();
        if (nombre == null || herramientas == null
                || !"tomcat9".equalsIgnoreCase(nombre.toString())
                || !"herramientas".equalsIgnoreCase(herramientas.getFileName().toString())) {
            return null;
        }
        Path raiz = herramientas.getParent();
        if (raiz == null) {
            return null;
        }
        if (Files.isDirectory(raiz.resolve("petservices"))
                || Files.exists(raiz.resolve("Iniciar-PetServices.bat"))) {
            return raiz;
        }
        return null;
    }

    /** Describe la ubicación actual para la documentación y los logs. */
    public static String descripcionRuta() {
        Path actual = ruta();
        if (actual == null) {
            return "(sin inicializar)";
        }
        return actual.toString();
    }
}
