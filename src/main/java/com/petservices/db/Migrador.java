package com.petservices.db;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Migrador sencillo y versionado.
 *
 * La tabla {@code schema_version} guarda la última versión aplicada. Al abrir
 * una base nueva se ejecuta el script V1; en arranques posteriores no se
 * vuelve a ejecutar, de modo que los datos del usuario nunca se pisan.
 */
public final class Migrador {

    public static final int VERSION_ACTUAL = 1;

    private static final String RECURSO_V1 =
            "/com/petservices/db/migraciones/V1__esquema_inicial.sql";

    private Migrador() {
    }

    /** Crea la tabla de versiones si no existe y aplica las migraciones pendientes. */
    public static void migrar(Connection conexion) throws SQLException {
        crearTablaVersion(conexion);

        int version = versionActual(conexion);
        if (version > VERSION_ACTUAL) {
            throw new SQLException("La base de datos fue creada por una versión más reciente ("
                    + version + "). Actualiza la aplicación para abrirla.");
        }

        if (version < 1) {
            ejecutarScript(conexion, RECURSO_V1);
            registrarVersion(conexion, 1);
        }
    }

    private static void crearTablaVersion(Connection conexion) throws SQLException {
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("CREATE TABLE IF NOT EXISTS schema_version ("
                    + "version INTEGER PRIMARY KEY, "
                    + "aplicada_en TEXT NOT NULL)");
        }
    }

    private static int versionActual(Connection conexion) throws SQLException {
        try (Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(
                     "SELECT COALESCE(MAX(version), 0) FROM schema_version")) {
            return resultado.next() ? resultado.getInt(1) : 0;
        }
    }

    private static void registrarVersion(Connection conexion, int version) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                "INSERT INTO schema_version (version, aplicada_en) VALUES (?, datetime('now'))")) {
            sentencia.setInt(1, version);
            sentencia.executeUpdate();
        }
    }

    /**
     * Ejecuta un script SQL separado por punto y coma. Los comentarios de línea
     * completa ({@code --}) se ignoran antes de dividir.
     */
    private static void ejecutarScript(Connection conexion, String recurso) throws SQLException {
        String script = leerRecurso(recurso);
        StringBuilder limpio = new StringBuilder();
        for (String linea : script.split("\\R")) {
            String recortada = linea.trim();
            if (recortada.isEmpty() || recortada.startsWith("--")) {
                continue;
            }
            limpio.append(linea).append('\n');
        }

        try (Statement sentencia = conexion.createStatement()) {
            for (String sql : limpio.toString().split(";")) {
                String instruccion = sql.trim();
                if (!instruccion.isEmpty()) {
                    sentencia.execute(instruccion);
                }
            }
        }
    }

    private static String leerRecurso(String recurso) throws SQLException {
        try (InputStream entrada = Migrador.class.getResourceAsStream(recurso)) {
            if (entrada == null) {
                throw new SQLException("No se encontró el recurso de migración: " + recurso);
            }
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int leidos;
            while ((leidos = entrada.read(buffer)) != -1) {
                salida.write(buffer, 0, leidos);
            }
            return salida.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new SQLException("No se pudo leer el recurso de migración: " + recurso, e);
        }
    }
}
