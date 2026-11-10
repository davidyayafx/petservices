package com.petservices.db;

import java.sql.SQLException;
import java.util.Locale;

/**
 * Excepción de la capa de persistencia.
 *
 * Distingue entre errores esperables de negocio (duplicado, restricción de
 * integridad, base ocupada) y fallos técnicos (conexión, disco, esquema).
 * Los DAO devuelven {@code false} para los casos que el usuario puede corregir
 * (por ejemplo, un correo duplicado) y lanzan esta excepción solo cuando no
 * pueden completar la operación por un problema técnico.
 */
public class BaseDatosException extends RuntimeException {

    public enum Codigo {
        CONEXION,
        ESQUEMA,
        DUPLICADO,
        RESTRICCION,
        BLOQUEO,
        DISCO,
        DESCONOCIDA
    }

    private final Codigo codigo;
    private final String mensajeUsuario;

    public BaseDatosException(Codigo codigo, String mensajeUsuario, String detalle, Throwable causa) {
        super(detalle, causa);
        this.codigo = codigo;
        this.mensajeUsuario = mensajeUsuario;
    }

    public Codigo getCodigo() {
        return codigo;
    }

    public String getMensajeUsuario() {
        return mensajeUsuario;
    }

    /** Indica si el error se puede mostrar al usuario sin riesgo técnico. */
    public boolean esErrorDeNegocio() {
        return codigo == Codigo.DUPLICADO || codigo == Codigo.RESTRICCION;
    }

    /** Traduce una {@link SQLException} de SQLite a un error con mensaje apto para el usuario. */
    public static BaseDatosException desde(SQLException error, String contexto) {
        String texto = error.getMessage() == null ? "" : error.getMessage().toLowerCase(Locale.ROOT);

        if (texto.contains("unique constraint") || texto.contains("primary key")) {
            return new BaseDatosException(Codigo.DUPLICADO,
                    "Ya existe un registro con esos datos. Revisa los campos únicos.",
                    contexto + ": " + error.getMessage(), error);
        }
        if (texto.contains("foreign key")) {
            return new BaseDatosException(Codigo.RESTRICCION,
                    "El registro está relacionado con otros datos y no se puede completar la operación.",
                    contexto + ": " + error.getMessage(), error);
        }
        if (texto.contains("database is locked") || texto.contains("database table is locked")
                || texto.contains("busy")) {
            return new BaseDatosException(Codigo.BLOQUEO,
                    "La base de datos está ocupada en este momento. Inténtalo de nuevo en unos segundos.",
                    contexto + ": " + error.getMessage(), error);
        }
        if (texto.contains("readonly") || texto.contains("read-only")) {
            return new BaseDatosException(Codigo.DISCO,
                    "La base de datos es de solo lectura. Verifica los permisos de la carpeta datos.",
                    contexto + ": " + error.getMessage(), error);
        }
        if (texto.contains("disk i/o") || texto.contains("no space")
                || texto.contains("unable to open") || texto.contains("permission denied")) {
            return new BaseDatosException(Codigo.DISCO,
                    "No se pudo escribir en el disco. Verifica el espacio libre y los permisos.",
                    contexto + ": " + error.getMessage(), error);
        }
        return new BaseDatosException(Codigo.CONEXION,
                "No se pudo acceder a la base de datos local. Revisa los logs de la aplicación.",
                contexto + ": " + error.getMessage(), error);
    }

    /** Comprueba si la excepción corresponde a una violación de unicidad. */
    public static boolean esDuplicado(SQLException error) {
        String texto = error.getMessage() == null ? "" : error.getMessage().toLowerCase(Locale.ROOT);
        return texto.contains("unique constraint") || texto.contains("primary key");
    }

    /** Comprueba si la excepción corresponde a una violación de clave foránea. */
    public static boolean esRestriccion(SQLException error) {
        String texto = error.getMessage() == null ? "" : error.getMessage().toLowerCase(Locale.ROOT);
        return texto.contains("foreign key");
    }
}
