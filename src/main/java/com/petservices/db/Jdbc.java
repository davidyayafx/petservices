package com.petservices.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades JDBC compartidas por los DAO.
 *
 * Centraliza el enlace de parámetros, la lectura de columnas opcionales y la
 * conversión de {@link ResultSet} a los JavaBeans del proyecto. No oculta SQL:
 * cada DAO conserva sus consultas para que sean auditables.
 */
public final class Jdbc {

    private Jdbc() {
    }

    @FunctionalInterface
    public interface Mapeador<T> {
        T mapear(ResultSet resultado) throws SQLException;
    }

    /** Enlaza los parámetros en orden (1..n), admitiendo valores null. */
    public static void parametros(PreparedStatement sentencia, Object... valores) throws SQLException {
        for (int i = 0; i < valores.length; i++) {
            sentencia.setObject(i + 1, valores[i]);
        }
    }

    /** Ejecuta un INSERT/UPDATE/DELETE y devuelve las filas afectadas. */
    public static int ejecutar(Connection conexion, String sql, Object... valores) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            parametros(sentencia, valores);
            return sentencia.executeUpdate();
        }
    }

    /**
     * Ejecuta un INSERT y devuelve la clave primaria generada.
     * Usa {@code getGeneratedKeys} y deja un respaldo con {@code last_insert_rowid()}.
     */
    public static long insertar(Connection conexion, String sql, Object... valores) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            parametros(sentencia, valores);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getLong(1);
                }
            }
        }
        try (Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery("SELECT last_insert_rowid()")) {
            return resultado.next() ? resultado.getLong(1) : 0L;
        }
    }

    /** Ejecuta una consulta que devuelve como máximo una fila. */
    public static <T> T uno(Connection conexion, String sql, Mapeador<T> mapeador, Object... valores)
            throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            parametros(sentencia, valores);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapeador.mapear(resultado) : null;
            }
        }
    }

    /** Ejecuta una consulta y devuelve todas las filas mapeadas. */
    public static <T> List<T> lista(Connection conexion, String sql, Mapeador<T> mapeador, Object... valores)
            throws SQLException {
        List<T> lista = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            parametros(sentencia, valores);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    lista.add(mapeador.mapear(resultado));
                }
            }
        }
        return lista;
    }

    /** Lee un entero que puede ser NULL. */
    public static Integer enteroNullable(ResultSet resultado, String columna) throws SQLException {
        int valor = resultado.getInt(columna);
        return resultado.wasNull() ? null : valor;
    }

    /** Lee un texto que puede ser NULL. */
    public static String textoNullable(ResultSet resultado, String columna) throws SQLException {
        return resultado.getString(columna);
    }
}
