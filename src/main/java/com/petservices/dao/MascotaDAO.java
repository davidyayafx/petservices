package com.petservices.dao;

import com.petservices.db.BaseDatos;
import com.petservices.db.BaseDatosException;
import com.petservices.db.Jdbc;
import com.petservices.modelo.Mascota;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO de mascotas sobre SQLite.
 * El nombre del dueño se guarda denormalizado para las vistas, pero siempre
 * se recalcula desde la tabla {@code clientes} al insertar o actualizar.
 */
public class MascotaDAO implements IMascotaDAO {

    private static final String COLUMNAS =
            "id_mascota, nombre, especie, raza, edad, id_cliente, nombre_cliente, id_servicio";

    @Override
    public boolean insertar(Mascota mascota) {
        if (mascota == null || mascota.getNombre() == null || mascota.getNombre().trim().isEmpty()
                || mascota.getRaza() == null || mascota.getRaza().trim().isEmpty()
                || mascota.getEdad() < 0 || mascota.getIdCliente() <= 0) {
            return false;
        }

        String nombreCliente = nombreCliente(mascota.getIdCliente());
        if (nombreCliente == null) {
            return false;
        }
        mascota.setNombre(mascota.getNombre().trim());
        mascota.setRaza(mascota.getRaza().trim());
        mascota.setNombreCliente(nombreCliente);
        Integer idServicio = normalizarIdServicio(mascota.getIdServicio());

        String sql = "INSERT INTO mascotas "
                + "(nombre, especie, raza, edad, id_cliente, nombre_cliente, id_servicio) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = BaseDatos.conexion()) {
            long id = Jdbc.insertar(conexion, sql, mascota.getNombre(), mascota.getEspecie(),
                    mascota.getRaza(), mascota.getEdad(), mascota.getIdCliente(),
                    nombreCliente, idServicio);
            mascota.setIdMascota((int) id);
            mascota.setIdServicio(idServicio);
            return true;
        } catch (SQLException e) {
            if (BaseDatosException.esRestriccion(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "insertar mascota");
        }
    }

    @Override
    public List<Mascota> listarTodos() {
        String sql = "SELECT " + COLUMNAS + " FROM mascotas ORDER BY id_mascota";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.lista(conexion, sql, MascotaDAO::mapear);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "listar mascotas");
        }
    }

    @Override
    public Mascota buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM mascotas WHERE id_mascota = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.uno(conexion, sql, MascotaDAO::mapear, id);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "buscar mascota por id");
        }
    }

    @Override
    public List<Mascota> buscarPorCliente(int idCliente) {
        if (idCliente <= 0) {
            return List.of();
        }
        String sql = "SELECT " + COLUMNAS + " FROM mascotas WHERE id_cliente = ? ORDER BY id_mascota";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.lista(conexion, sql, MascotaDAO::mapear, idCliente);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "listar mascotas por cliente");
        }
    }

    @Override
    public boolean actualizar(Mascota mascota) {
        if (mascota == null || mascota.getIdMascota() <= 0
                || mascota.getNombre() == null || mascota.getNombre().trim().isEmpty()
                || mascota.getRaza() == null || mascota.getRaza().trim().isEmpty()
                || mascota.getEdad() < 0 || mascota.getIdCliente() <= 0) {
            return false;
        }
        if (buscarPorId(mascota.getIdMascota()) == null) {
            return false;
        }

        String nombreCliente = nombreCliente(mascota.getIdCliente());
        if (nombreCliente == null) {
            return false;
        }
        mascota.setNombre(mascota.getNombre().trim());
        mascota.setRaza(mascota.getRaza().trim());
        mascota.setNombreCliente(nombreCliente);
        Integer idServicio = normalizarIdServicio(mascota.getIdServicio());

        String sql = "UPDATE mascotas SET nombre = ?, especie = ?, raza = ?, edad = ?, "
                + "id_cliente = ?, nombre_cliente = ?, id_servicio = ? WHERE id_mascota = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            int filas = Jdbc.ejecutar(conexion, sql, mascota.getNombre(), mascota.getEspecie(),
                    mascota.getRaza(), mascota.getEdad(), mascota.getIdCliente(), nombreCliente,
                    idServicio, mascota.getIdMascota());
            return filas > 0;
        } catch (SQLException e) {
            if (BaseDatosException.esRestriccion(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "actualizar mascota");
        }
    }

    @Override
    public boolean eliminar(int id) {
        if (id <= 0) {
            return false;
        }
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.ejecutar(conexion, "DELETE FROM mascotas WHERE id_mascota = ?", id) > 0;
        } catch (SQLException e) {
            if (BaseDatosException.esRestriccion(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "eliminar mascota");
        }
    }

    @Override
    public boolean tieneCitas(int id) {
        if (id <= 0) {
            return false;
        }
        String sql = "SELECT COUNT(*) AS total FROM citas WHERE id_mascota = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            Integer total = Jdbc.uno(conexion, sql, rs -> rs.getInt("total"), id);
            return total != null && total > 0;
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "comprobar citas de la mascota");
        }
    }

    private static String nombreCliente(int idCliente) {
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.uno(conexion, "SELECT nombre FROM clientes WHERE id_cliente = ?",
                    rs -> rs.getString(1), idCliente);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "resolver dueño de la mascota");
        }
    }

    private static Integer normalizarIdServicio(Integer idServicio) {
        return idServicio == null || idServicio <= 0 ? null : idServicio;
    }

    private static Mascota mapear(ResultSet resultado) throws SQLException {
        Mascota mascota = new Mascota(
                resultado.getInt("id_mascota"),
                resultado.getString("nombre"),
                resultado.getString("especie"),
                resultado.getString("raza"),
                resultado.getInt("edad"),
                resultado.getInt("id_cliente"),
                resultado.getString("nombre_cliente"));
        mascota.setIdServicio(Jdbc.enteroNullable(resultado, "id_servicio"));
        return mascota;
    }
}
