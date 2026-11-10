package com.petservices.dao;

import com.petservices.db.BaseDatos;
import com.petservices.db.BaseDatosException;
import com.petservices.db.Jdbc;
import com.petservices.modelo.Establecimiento;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO de establecimientos sobre SQLite.
 * El nombre es único sin distinguir mayúsculas.
 */
public class EstablecimientoDAO implements IEstablecimientoDAO {

    private static final String COLUMNAS =
            "id_establecimiento, nombre, tipo, direccion, telefono";

    @Override
    public boolean insertar(Establecimiento establecimiento) {
        if (!datosValidos(establecimiento)) {
            return false;
        }
        String sql = "INSERT INTO establecimientos (nombre, tipo, direccion, telefono) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conexion = BaseDatos.conexion()) {
            long id = Jdbc.insertar(conexion, sql, establecimiento.getNombre().trim(),
                    establecimiento.getTipo().trim(), limpiar(establecimiento.getDireccion()),
                    limpiar(establecimiento.getTelefono()));
            establecimiento.setIdEstablecimiento((int) id);
            establecimiento.setNombre(establecimiento.getNombre().trim());
            establecimiento.setTipo(establecimiento.getTipo().trim());
            return true;
        } catch (SQLException e) {
            if (BaseDatosException.esDuplicado(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "insertar establecimiento");
        }
    }

    @Override
    public List<Establecimiento> listarTodos() {
        String sql = "SELECT " + COLUMNAS + " FROM establecimientos ORDER BY id_establecimiento";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.lista(conexion, sql, EstablecimientoDAO::mapear);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "listar establecimientos");
        }
    }

    @Override
    public Establecimiento buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM establecimientos WHERE id_establecimiento = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.uno(conexion, sql, EstablecimientoDAO::mapear, id);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "buscar establecimiento por id");
        }
    }

    @Override
    public boolean actualizar(Establecimiento establecimiento) {
        if (!datosValidos(establecimiento) || establecimiento.getIdEstablecimiento() <= 0) {
            return false;
        }
        if (buscarPorId(establecimiento.getIdEstablecimiento()) == null) {
            return false;
        }
        String sql = "UPDATE establecimientos SET nombre = ?, tipo = ?, direccion = ?, "
                + "telefono = ? WHERE id_establecimiento = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            int filas = Jdbc.ejecutar(conexion, sql, establecimiento.getNombre().trim(),
                    establecimiento.getTipo().trim(), limpiar(establecimiento.getDireccion()),
                    limpiar(establecimiento.getTelefono()), establecimiento.getIdEstablecimiento());
            if (filas > 0) {
                establecimiento.setNombre(establecimiento.getNombre().trim());
                establecimiento.setTipo(establecimiento.getTipo().trim());
            }
            return filas > 0;
        } catch (SQLException e) {
            if (BaseDatosException.esDuplicado(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "actualizar establecimiento");
        }
    }

    @Override
    public boolean eliminar(int id) {
        if (id <= 0) {
            return false;
        }
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.ejecutar(conexion,
                    "DELETE FROM establecimientos WHERE id_establecimiento = ?", id) > 0;
        } catch (SQLException e) {
            if (BaseDatosException.esRestriccion(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "eliminar establecimiento");
        }
    }

    private static boolean datosValidos(Establecimiento establecimiento) {
        return establecimiento != null
                && establecimiento.getNombre() != null && !establecimiento.getNombre().trim().isEmpty()
                && establecimiento.getTipo() != null && !establecimiento.getTipo().trim().isEmpty();
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static Establecimiento mapear(ResultSet resultado) throws SQLException {
        return new Establecimiento(
                resultado.getInt("id_establecimiento"),
                resultado.getString("nombre"),
                resultado.getString("tipo"),
                resultado.getString("direccion"),
                resultado.getString("telefono"));
    }
}
