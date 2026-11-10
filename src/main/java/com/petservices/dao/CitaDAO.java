package com.petservices.dao;

import com.petservices.db.BaseDatos;
import com.petservices.db.BaseDatosException;
import com.petservices.db.Jdbc;
import com.petservices.modelo.Cita;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * DAO de citas sobre SQLite.
 * Valida y recalcula los nombres denormalizados de cliente y mascota desde las
 * tablas relacionadas, y evita citas duplicadas para la misma mascota/horario.
 */
public class CitaDAO implements ICitaDAO {

    private static final String COLUMNAS =
            "id_cita, id_cliente, nombre_cliente, id_mascota, nombre_mascota, "
            + "fecha_hora, servicio, estado, id_establecimiento, id_servicio";

    @Override
    public boolean insertar(Cita cita) {
        if (!datosValidos(cita)) {
            return false;
        }
        String estado = normalizarEstado(cita.getEstado());
        if (estado == null) {
            return false;
        }
        String[] relaciones = resolverRelaciones(cita.getIdCliente(), cita.getIdMascota());
        if (relaciones == null) {
            return false;
        }

        cita.setNombreCliente(relaciones[0]);
        cita.setNombreMascota(relaciones[1]);
        cita.setFechaHora(cita.getFechaHora().trim());
        cita.setServicio(cita.getServicio().trim());
        cita.setEstado(estado);
        Integer idEstablecimiento = normalizarId(cita.getIdEstablecimiento());
        Integer idServicio = normalizarId(cita.getIdServicio());

        String sql = "INSERT INTO citas "
                + "(id_cliente, nombre_cliente, id_mascota, nombre_mascota, fecha_hora, "
                + " servicio, estado, id_establecimiento, id_servicio) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = BaseDatos.conexion()) {
            long id = Jdbc.insertar(conexion, sql, cita.getIdCliente(), relaciones[0],
                    cita.getIdMascota(), relaciones[1], cita.getFechaHora(), cita.getServicio(),
                    estado, idEstablecimiento, idServicio);
            cita.setIdCita((int) id);
            cita.setIdEstablecimiento(idEstablecimiento == null ? 0 : idEstablecimiento);
            cita.setIdServicio(idServicio == null ? 0 : idServicio);
            return true;
        } catch (SQLException e) {
            if (BaseDatosException.esDuplicado(e) || BaseDatosException.esRestriccion(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "insertar cita");
        }
    }

    @Override
    public List<Cita> listarTodos() {
        String sql = "SELECT " + COLUMNAS + " FROM citas ORDER BY id_cita";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.lista(conexion, sql, CitaDAO::mapear);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "listar citas");
        }
    }

    @Override
    public Cita buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM citas WHERE id_cita = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.uno(conexion, sql, CitaDAO::mapear, id);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "buscar cita por id");
        }
    }

    @Override
    public boolean actualizar(Cita cita) {
        if (!datosValidos(cita) || cita.getIdCita() <= 0) {
            return false;
        }
        if (buscarPorId(cita.getIdCita()) == null) {
            return false;
        }
        String estado = normalizarEstado(cita.getEstado());
        if (estado == null) {
            return false;
        }
        String[] relaciones = resolverRelaciones(cita.getIdCliente(), cita.getIdMascota());
        if (relaciones == null) {
            return false;
        }

        cita.setNombreCliente(relaciones[0]);
        cita.setNombreMascota(relaciones[1]);
        cita.setFechaHora(cita.getFechaHora().trim());
        cita.setServicio(cita.getServicio().trim());
        cita.setEstado(estado);
        Integer idEstablecimiento = normalizarId(cita.getIdEstablecimiento());
        Integer idServicio = normalizarId(cita.getIdServicio());

        String sql = "UPDATE citas SET id_cliente = ?, nombre_cliente = ?, id_mascota = ?, "
                + "nombre_mascota = ?, fecha_hora = ?, servicio = ?, estado = ?, "
                + "id_establecimiento = ?, id_servicio = ? WHERE id_cita = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            int filas = Jdbc.ejecutar(conexion, sql, cita.getIdCliente(), relaciones[0],
                    cita.getIdMascota(), relaciones[1], cita.getFechaHora(), cita.getServicio(),
                    estado, idEstablecimiento, idServicio, cita.getIdCita());
            return filas > 0;
        } catch (SQLException e) {
            if (BaseDatosException.esDuplicado(e) || BaseDatosException.esRestriccion(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "actualizar cita");
        }
    }

    @Override
    public boolean eliminar(int id) {
        if (id <= 0) {
            return false;
        }
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.ejecutar(conexion, "DELETE FROM citas WHERE id_cita = ?", id) > 0;
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "eliminar cita");
        }
    }

    private static boolean datosValidos(Cita cita) {
        return cita != null
                && cita.getIdCliente() > 0
                && cita.getIdMascota() > 0
                && cita.getFechaHora() != null && !cita.getFechaHora().trim().isEmpty()
                && cita.getServicio() != null && !cita.getServicio().trim().isEmpty();
    }

    /**
     * Comprueba que el cliente y la mascota existan y que la mascota pertenezca
     * al cliente. Devuelve [nombreCliente, nombreMascota] o null.
     */
    private static String[] resolverRelaciones(int idCliente, int idMascota) {
        String sql = "SELECT c.nombre AS cliente, m.nombre AS mascota "
                + "FROM clientes c JOIN mascotas m ON m.id_cliente = c.id_cliente "
                + "WHERE c.id_cliente = ? AND m.id_mascota = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.uno(conexion, sql,
                    rs -> new String[]{rs.getString("cliente"), rs.getString("mascota")},
                    idCliente, idMascota);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "resolver relaciones de la cita");
        }
    }

    /** Acepta el estado sin distinguir mayúsculas y devuelve su forma canónica. */
    private static String normalizarEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return "Pendiente";
        }
        switch (estado.trim().toLowerCase(Locale.ROOT)) {
            case "pendiente":
                return "Pendiente";
            case "confirmada":
                return "Confirmada";
            case "cancelada":
                return "Cancelada";
            case "completada":
                return "Completada";
            default:
                return null;
        }
    }

    private static Integer normalizarId(int id) {
        return id <= 0 ? null : id;
    }

    private static Cita mapear(ResultSet resultado) throws SQLException {
        Cita cita = new Cita(
                resultado.getInt("id_cita"),
                resultado.getInt("id_cliente"),
                resultado.getString("nombre_cliente"),
                resultado.getInt("id_mascota"),
                resultado.getString("nombre_mascota"),
                resultado.getString("fecha_hora"),
                resultado.getString("servicio"),
                resultado.getString("estado"));
        cita.setIdEstablecimiento(resultado.getInt("id_establecimiento"));
        cita.setIdServicio(resultado.getInt("id_servicio"));
        return cita;
    }
}
