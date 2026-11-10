package com.petservices.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copias de seguridad y restauración de la base SQLite.
 *
 * Reglas:
 *  - Las copias viven en {@code datos/copias/} (junto al archivo de la base).
 *  - El nombre es {@code petservices-AAAA-MM-DD-HHmmss.db}; si coinciden dos en
 *    el mismo segundo se añade {@code -2}, {@code -3}, etc.
 *  - La copia se toma con {@code VACUUM INTO}, que produce un archivo consistente
 *    aunque la aplicación esté en marcha y la base use WAL.
 *  - La retención (10 por defecto) se puede ajustar con la propiedad
 *    {@code petservices.copias.retencion}, la variable de entorno
 *    {@code PETSERVICES_COPIAS_RETENCION} o la clave {@code retencion_copias}
 *    de la tabla {@code configuracion}.
 *  - Antes de restaurar se valida el candidato (cabecera SQLite,
 *    {@code integrity_check}, versión de esquema y tablas) y se guarda una copia
 *    del estado actual. Si algo falla, se intenta volver al estado anterior.
 */
public final class RespaldoService {

    private static final Logger LOG = Logger.getLogger(RespaldoService.class.getName());

    /** Carpeta, relativa a la base, donde se guardan las copias. */
    public static final String CARPETA_COPIAS = "copias";

    /** Prefijo y extensión de los archivos de copia. */
    public static final String PREFIJO = "petservices-";
    public static final String EXTENSION = ".db";

    /** Propiedad del sistema y variable de entorno para la retención. */
    public static final String PROP_RETENCION = "petservices.copias.retencion";
    public static final String ENV_RETENCION = "PETSERVICES_COPIAS_RETENCION";

    /** Número de copias que se conservan cuando no hay configuración. */
    public static final int RETENCION_POR_DEFECTO = 10;

    /** Cabecera de todo archivo SQLite. */
    private static final byte[] CABECERA_SQLITE =
            "SQLite format 3\u0000".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);

    /** Tablas que debe contener una copia restaurable. */
    private static final List<String> TABLAS_REQUERIDAS = Arrays.asList(
            "clientes", "mascotas", "citas", "servicios",
            "productos", "pedidos", "detalle_pedido", "establecimientos");

    private static final DateTimeFormatter MARCA_TIEMPO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");
    private static final DateTimeFormatter FECHA_VISIBLE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final Pattern NOMBRE_VALIDO =
            Pattern.compile("^petservices-\\d{4}-\\d{2}-\\d{2}-\\d{6}(-\\d+)?\\.db$");

    private RespaldoService() {
    }

    // ──────────────────────────────────────────────────────────────────────
    // Consulta y creación de copias
    // ──────────────────────────────────────────────────────────────────────

    /** Carpeta de copias de la base actual, o {@code null} si aún no se inicializó. */
    public static Path carpetaCopias() {
        Path base = BaseDatos.ruta();
        if (base == null) {
            return null;
        }
        Path carpeta = base.getParent();
        return carpeta == null ? null : carpeta.resolve(CARPETA_COPIAS);
    }

    /** Crea una copia manual y devuelve su ruta. */
    public static Path crearCopia() {
        Path base = BaseDatos.ruta();
        if (base == null || !Files.isRegularFile(base)) {
            throw new RespaldoException("La base de datos todavía no está disponible.");
        }
        return crearCopiaDe(base, "manual", true);
    }

    /** Lista las copias disponibles, de la más reciente a la más antigua. */
    public static List<Copia> listar() {
        Path carpeta = carpetaCopias();
        if (carpeta == null || !Files.isDirectory(carpeta)) {
            return Collections.emptyList();
        }
        try (Stream<Path> flujo = Files.list(carpeta)) {
            return flujo
                    .filter(Files::isRegularFile)
                    .filter(ruta -> esNombreSeguro(ruta.getFileName().toString()))
                    .map(RespaldoService::aCopia)
                    .sorted(Comparator.comparingLong(Copia::getModificadoEn).reversed()
                            .thenComparing(Copia::getNombre, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.log(Level.WARNING, "No se pudo listar la carpeta de copias " + carpeta, e);
            return Collections.emptyList();
        }
    }

    /** Retención efectiva: propiedad, variable de entorno, configuración de la base o 10. */
    public static int retencion() {
        String valor = System.getProperty(PROP_RETENCION);
        if (esVacio(valor)) {
            valor = System.getenv(ENV_RETENCION);
        }
        if (!esVacio(valor)) {
            Integer configurado = parsearRetencion(valor);
            if (configurado != null) {
                return configurado;
            }
            LOG.warning("Valor de retención de copias no válido (" + valor + "); se usa el de la base o "
                    + RETENCION_POR_DEFECTO + ".");
        }
        int deBase = retencionDeBaseDatos();
        return deBase > 0 ? deBase : RETENCION_POR_DEFECTO;
    }

    /** Elimina las copias que superan la retención vigente. */
    public static void aplicarRetencion() {
        aplicarRetencion(carpetaCopias());
    }

    /**
     * Copia automática al arrancar (antes de las migraciones). Nunca impide que
     * la aplicación arranque: si falla, se registra una advertencia.
     */
    public static void copiaAutomaticaAlArrancar(Path archivo) {
        try {
            if (archivo == null || !Files.isRegularFile(archivo) || Files.size(archivo) == 0) {
                return;
            }
            crearCopiaDe(archivo, "arranque", true);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "No se pudo crear la copia automática al arrancar", e);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Validación y restauración
    // ──────────────────────────────────────────────────────────────────────

    /** Comprueba que un archivo sea una copia SQLite válida y restaurable. */
    public static void validar(Path archivo) {
        if (archivo == null || !Files.isRegularFile(archivo)) {
            throw new RespaldoException("La copia de seguridad no existe.");
        }
        try {
            if (Files.size(archivo) < 512) {
                throw new RespaldoException("El archivo es demasiado pequeño para ser una base SQLite.");
            }
            byte[] cabecera = new byte[CABECERA_SQLITE.length];
            try (InputStream entrada = Files.newInputStream(archivo)) {
                int leidos = entrada.read(cabecera);
                if (leidos != cabecera.length || !Arrays.equals(cabecera, CABECERA_SQLITE)) {
                    throw new RespaldoException(
                            "El archivo no es una base de datos SQLite válida (cabecera incorrecta).");
                }
            }
        } catch (IOException e) {
            throw new RespaldoException("No se pudo leer la copia de seguridad.", e);
        }

        try (Connection conexion = DriverManager.getConnection("jdbc:sqlite:" + archivo.toAbsolutePath());
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA busy_timeout = 5000");
            comprobarIntegridad(sentencia);
            comprobarEsquema(sentencia);
        } catch (SQLException e) {
            throw new RespaldoException(
                    "El archivo no es una base de datos SQLite válida o está dañado.", e);
        }
    }

    /** Resuelve el nombre de una copia dentro de la carpeta de copias, sin permitir rutas. */
    public static Path resolverCopia(String nombre) {
        if (!esNombreSeguro(nombre)) {
            throw new RespaldoException("El nombre de la copia no es válido.");
        }
        Path carpeta = carpetaCopias();
        if (carpeta == null) {
            throw new RespaldoException("La base de datos todavía no está disponible.");
        }
        Path normalizada = carpeta.toAbsolutePath().normalize();
        Path archivo = normalizada.resolve(nombre).normalize();
        if (!normalizada.equals(archivo.getParent())) {
            throw new RespaldoException("El nombre de la copia no es válido.");
        }
        if (!Files.isRegularFile(archivo)) {
            throw new RespaldoException("No se encontró la copia de seguridad solicitada.");
        }
        return archivo;
    }

    /** Indica si un nombre cumple el patrón de copia y no contiene rutas. */
    public static boolean esNombreSeguro(String nombre) {
        return nombre != null && NOMBRE_VALIDO.matcher(nombre).matches();
    }

    /**
     * Restaura la copia indicada. Valida el archivo, guarda una copia del estado
     * actual, sustituye la base y vuelve a abrirla. Si la sustitución falla,
     * intenta recuperar el estado anterior.
     */
    public static Resultado restaurar(String nombre) {
        Path origen = resolverCopia(nombre);
        validar(origen);
        // La copia previa NO aplica retención todavía: la copia que se va a
        // restaurar podría ser la más antigua y quedar eliminada antes de usarla.
        Path seguridad = crearCopiaDe(BaseDatos.ruta(), "antes de restaurar", false);
        try {
            BaseDatos.sustituirArchivo(origen);
            validar(BaseDatos.ruta());
            aplicarRetencion();
            return new Resultado(nombre, seguridad.getFileName().toString(), contarClientes());
        } catch (IOException e) {
            throw recuperar(seguridad, e,
                    "No se pudo reemplazar el archivo de la base de datos (¿está en uso por otro proceso?).");
        } catch (RuntimeException e) {
            throw recuperar(seguridad, e, "La restauración de la copia falló.");
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Internos
    // ──────────────────────────────────────────────────────────────────────

    private static Path crearCopiaDe(Path origen, String motivo, boolean aplicarRetencion) {
        Path archivo = origen.toAbsolutePath();
        Path carpeta = archivo.getParent() == null
                ? null : archivo.getParent().resolve(CARPETA_COPIAS);
        if (carpeta == null) {
            throw new RespaldoException("No se pudo determinar la carpeta de copias.");
        }
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new RespaldoException("No se pudo crear la carpeta de copias (" + carpeta + ").", e);
        }

        Path destino = siguienteNombre(carpeta);
        try (Connection conexion = DriverManager.getConnection("jdbc:sqlite:" + archivo);
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA busy_timeout = 5000");
            // VACUUM INTO no admite parámetros en algunas versiones del driver:
            // se interpola la ruta escapando las comillas simples.
            String destinoSql = destino.toString().replace("'", "''");
            sentencia.execute("VACUUM INTO '" + destinoSql + "'");
        } catch (SQLException e) {
            borrarSilencioso(destino);
            throw new RespaldoException("No se pudo crear la copia de seguridad de la base de datos.", e);
        }

        try {
            validar(destino);
        } catch (RuntimeException e) {
            borrarSilencioso(destino);
            throw e;
        }

        LOG.info("Copia de seguridad (" + motivo + ") creada en " + destino);
        if (aplicarRetencion) {
            aplicarRetencion(carpeta);
        }
        return destino;
    }

    private static Path siguienteNombre(Path carpeta) {
        String marca = LocalDateTime.now().format(MARCA_TIEMPO);
        Path destino = carpeta.resolve(PREFIJO + marca + EXTENSION);
        int sufijo = 2;
        while (Files.exists(destino)) {
            destino = carpeta.resolve(PREFIJO + marca + "-" + sufijo + EXTENSION);
            sufijo++;
        }
        return destino;
    }

    private static void aplicarRetencion(Path carpeta) {
        if (carpeta == null || !Files.isDirectory(carpeta)) {
            return;
        }
        List<Path> archivos = archivosOrdenados(carpeta);
        int limite = retencion();
        for (int i = limite; i < archivos.size(); i++) {
            Path antiguo = archivos.get(i);
            try {
                Files.deleteIfExists(antiguo);
                LOG.info("Copia antigua eliminada por retención: " + antiguo.getFileName());
            } catch (IOException e) {
                LOG.log(Level.WARNING,
                        "No se pudo eliminar la copia antigua " + antiguo.getFileName(), e);
            }
        }
    }

    private static List<Path> archivosOrdenados(Path carpeta) {
        List<Path> archivos = new ArrayList<>();
        try (Stream<Path> flujo = Files.list(carpeta)) {
            flujo.filter(Files::isRegularFile)
                    .filter(ruta -> esNombreSeguro(ruta.getFileName().toString()))
                    .forEach(archivos::add);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "No se pudo leer la carpeta de copias " + carpeta, e);
            return archivos;
        }
        archivos.sort(Comparator
                .comparingLong((Path ruta) -> ruta.toFile().lastModified()).reversed()
                .thenComparing(ruta -> ruta.getFileName().toString(), Comparator.reverseOrder()));
        return archivos;
    }

    private static Copia aCopia(Path ruta) {
        long bytes;
        try {
            bytes = Files.size(ruta);
        } catch (IOException e) {
            bytes = 0;
        }
        return new Copia(ruta.getFileName().toString(), ruta, bytes, ruta.toFile().lastModified());
    }

    private static void comprobarIntegridad(Statement sentencia) throws SQLException {
        try (ResultSet resultado = sentencia.executeQuery("PRAGMA integrity_check")) {
            boolean correcta = false;
            while (resultado.next()) {
                String linea = resultado.getString(1);
                if ("ok".equalsIgnoreCase(linea)) {
                    correcta = true;
                } else {
                    throw new RespaldoException("La copia está dañada: " + linea);
                }
            }
            if (!correcta) {
                throw new RespaldoException("La copia está dañada: integrity_check no devolvió 'ok'.");
            }
        }
    }

    private static void comprobarEsquema(Statement sentencia) throws SQLException {
        int version;
        try (ResultSet resultado = sentencia.executeQuery(
                "SELECT COALESCE(MAX(version), 0) FROM schema_version")) {
            version = resultado.next() ? resultado.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RespaldoException(
                    "El archivo no es una copia de PetServices (no contiene schema_version).", e);
        }
        if (version < 1 || version > Migrador.VERSION_ACTUAL) {
            throw new RespaldoException("La copia usa una versión de esquema no compatible ("
                    + version + "). Esta aplicación admite hasta la versión "
                    + Migrador.VERSION_ACTUAL + ".");
        }

        Set<String> faltantes = new HashSet<>(TABLAS_REQUERIDAS);
        try (ResultSet resultado = sentencia.executeQuery(
                "SELECT name FROM sqlite_master WHERE type = 'table'")) {
            while (resultado.next()) {
                faltantes.remove(resultado.getString(1));
            }
        }
        if (!faltantes.isEmpty()) {
            List<String> ordenadas = new ArrayList<>(faltantes);
            Collections.sort(ordenadas);
            throw new RespaldoException("La copia no contiene todas las tablas de PetServices: "
                    + String.join(", ", ordenadas) + ".");
        }
    }

    private static int retencionDeBaseDatos() {
        Path base = BaseDatos.ruta();
        if (base == null || !Files.isRegularFile(base)) {
            return 0;
        }
        // Conexión directa: este método también se usa durante el arranque, antes
        // de que BaseDatos esté inicializada (y no debe volver a llamar a inicializar).
        try (Connection conexion = DriverManager.getConnection("jdbc:sqlite:" + base);
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA busy_timeout = 2000");
            try (ResultSet resultado = sentencia.executeQuery(
                    "SELECT valor FROM configuracion WHERE clave = 'retencion_copias'")) {
                if (resultado.next()) {
                    Integer valor = parsearRetencion(resultado.getString(1));
                    return valor == null ? 0 : valor;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "No se pudo leer la retención de copias de la base", e);
        }
        return 0;
    }

    private static Integer parsearRetencion(String valor) {
        if (esVacio(valor)) {
            return null;
        }
        try {
            int numero = Integer.parseInt(valor.trim());
            return numero < 1 ? 1 : numero;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private static int contarClientes() {
        try (Connection conexion = BaseDatos.conexion();
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery("SELECT COUNT(*) FROM clientes")) {
            return resultado.next() ? resultado.getInt(1) : 0;
        } catch (SQLException e) {
            LOG.log(Level.FINE, "No se pudo contar clientes tras la restauración", e);
            return -1;
        }
    }

    private static RespaldoException recuperar(Path seguridad, Exception causa, String mensaje) {
        try {
            BaseDatos.sustituirArchivo(seguridad);
            return new RespaldoException(mensaje
                    + " Se recuperó automáticamente el estado anterior.", causa);
        } catch (IOException | RuntimeException fallo) {
            return new RespaldoException(mensaje
                    + " No se pudo recuperar automáticamente el estado anterior. La copia de seguridad"
                    + " previa está en: " + seguridad, fallo);
        }
    }

    private static void borrarSilencioso(Path archivo) {
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException ignored) {
            // El archivo incompleto se queda; la retención lo eliminará.
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Tipos de apoyo
    // ──────────────────────────────────────────────────────────────────────

    /** Una copia de seguridad disponible. */
    public static final class Copia {

        private final String nombre;
        private final Path ruta;
        private final long bytes;
        private final long modificadoEn;

        Copia(String nombre, Path ruta, long bytes, long modificadoEn) {
            this.nombre = nombre;
            this.ruta = ruta;
            this.bytes = bytes;
            this.modificadoEn = modificadoEn;
        }

        public String getNombre() {
            return nombre;
        }

        public Path getRuta() {
            return ruta;
        }

        public long getBytes() {
            return bytes;
        }

        public long getModificadoEn() {
            return modificadoEn;
        }

        /** Fecha local legible, por ejemplo {@code 10/11/2026 09:45:12}. */
        public String getFecha() {
            return FECHA_VISIBLE.format(
                    Instant.ofEpochMilli(modificadoEn).atZone(ZoneId.systemDefault()));
        }

        /** Tamaño legible, por ejemplo {@code 245,3 KB}. */
        public String getTamanoLegible() {
            if (bytes < 1024) {
                return bytes + " B";
            }
            double kb = bytes / 1024.0;
            if (kb < 1024) {
                return String.format(java.util.Locale.ROOT, "%.1f KB", kb);
            }
            return String.format(java.util.Locale.ROOT, "%.1f MB", kb / 1024.0);
        }
    }

    /** Resultado de una restauración correcta. */
    public static final class Resultado {

        private final String copiaRestaurada;
        private final String copiaDeSeguridad;
        private final int clientes;

        Resultado(String copiaRestaurada, String copiaDeSeguridad, int clientes) {
            this.copiaRestaurada = copiaRestaurada;
            this.copiaDeSeguridad = copiaDeSeguridad;
            this.clientes = clientes;
        }

        public String getCopiaRestaurada() {
            return copiaRestaurada;
        }

        public String getCopiaDeSeguridad() {
            return copiaDeSeguridad;
        }

        public int getClientes() {
            return clientes;
        }
    }
}
