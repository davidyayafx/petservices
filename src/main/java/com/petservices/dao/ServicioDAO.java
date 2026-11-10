package com.petservices.dao;

import com.petservices.db.BaseDatos;
import com.petservices.db.BaseDatosException;
import com.petservices.db.Jdbc;
import com.petservices.modelo.Servicio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO del catálogo de servicios sobre SQLite.
 * Evita duplicados por combinación de tipo y nombre.
 */
public class ServicioDAO implements IServicioDAO {

    private static final String COLUMNAS = "id_servicio, tipo, nombre, id_mascota";

    @Override
    public boolean insertar(Servicio servicio) {
        if (!datosValidos(servicio)) {
            return false;
        }
        Integer idMascota = normalizarId(servicio.getIdMascota());
        String sql = "INSERT INTO servicios (tipo, nombre, id_mascota) VALUES (?, ?, ?)";
        try (Connection conexion = BaseDatos.conexion()) {
            long id = Jdbc.insertar(conexion, sql, servicio.getTipo().trim(),
                    servicio.getNombre().trim(), idMascota);
            servicio.setIdServicio((int) id);
            servicio.setIdMascota(idMascota);
            return true;
        } catch (SQLException e) {
            if (BaseDatosException.esDuplicado(e) || BaseDatosException.esRestriccion(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "insertar servicio");
        }
    }

    @Override
    public List<Servicio> listarTodos() {
        String sql = "SELECT " + COLUMNAS + " FROM servicios ORDER BY id_servicio";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.lista(conexion, sql, ServicioDAO::mapear);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "listar servicios");
        }
    }

    @Override
    public Servicio buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM servicios WHERE id_servicio = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.uno(conexion, sql, ServicioDAO::mapear, id);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "buscar servicio por id");
        }
    }

    @Override
    public boolean actualizar(Servicio servicio) {
        if (!datosValidos(servicio) || servicio.getIdServicio() <= 0) {
            return false;
        }
        if (buscarPorId(servicio.getIdServicio()) == null) {
            return false;
        }
        Integer idMascota = normalizarId(servicio.getIdMascota());
        String sql = "UPDATE servicios SET tipo = ?, nombre = ?, id_mascota = ? "
                + "WHERE id_servicio = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            int filas = Jdbc.ejecutar(conexion, sql, servicio.getTipo().trim(),
                    servicio.getNombre().trim(), idMascota, servicio.getIdServicio());
            return filas > 0;
        } catch (SQLException e) {
            if (BaseDatosException.esDuplicado(e) || BaseDatosException.esRestriccion(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "actualizar servicio");
        }
    }

    @Override
    public boolean eliminar(int id) {
        if (id <= 0) {
            return false;
        }
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.ejecutar(conexion, "DELETE FROM servicios WHERE id_servicio = ?", id) > 0;
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "eliminar servicio");
        }
    }

    private static boolean datosValidos(Servicio servicio) {
        return servicio != null
                && servicio.getTipo() != null && !servicio.getTipo().trim().isEmpty()
                && servicio.getNombre() != null && !servicio.getNombre().trim().isEmpty();
    }

    private static Integer normalizarId(Integer id) {
        return id == null || id <= 0 ? null : id;
    }

    private static Servicio mapear(ResultSet resultado) throws SQLException {
        return new Servicio(
                resultado.getInt("id_servicio"),
                resultado.getString("tipo"),
                resultado.getString("nombre"),
                Jdbc.enteroNullable(resultado, "id_mascota"));
    }
}
