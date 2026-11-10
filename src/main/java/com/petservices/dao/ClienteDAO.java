package com.petservices.dao;

import com.petservices.db.BaseDatos;
import com.petservices.db.BaseDatosException;
import com.petservices.db.Jdbc;
import com.petservices.modelo.Cliente;
import com.petservices.util.PasswordUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * DAO de clientes sobre SQLite.
 *
 * Conserva el contrato de {@link IClienteDAO}: devuelve {@code false} para los
 * casos que el usuario puede corregir (correo duplicado, datos inválidos) y
 * lanza {@link BaseDatosException} solo ante fallos técnicos.
 */
public class ClienteDAO implements IClienteDAO {

    private static final String COLUMNAS =
            "id_cliente, nombre, correo, contrasena, fecha_registro";

    @Override
    public boolean insertar(Cliente cliente) {
        if (cliente == null || cliente.getCorreo() == null
                || cliente.getCorreo().trim().isEmpty()
                || cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            return false;
        }

        String correo = cliente.getCorreo().trim().toLowerCase(Locale.ROOT);
        if (buscarPorCorreo(correo) != null) {
            return false;
        }

        cliente.setNombre(cliente.getNombre().trim());
        cliente.setCorreo(correo);
        cliente.setFechaRegistro(LocalDate.now().toString());
        if (cliente.getContrasena() != null && !PasswordUtil.esHash(cliente.getContrasena())) {
            cliente.setContrasena(PasswordUtil.hashear(cliente.getContrasena()));
        }

        String sql = "INSERT INTO clientes (nombre, correo, contrasena, fecha_registro) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conexion = BaseDatos.conexion()) {
            long id = Jdbc.insertar(conexion, sql, cliente.getNombre(), correo,
                    cliente.getContrasena(), cliente.getFechaRegistro());
            cliente.setIdCliente((int) id);
            return true;
        } catch (SQLException e) {
            if (BaseDatosException.esDuplicado(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "insertar cliente");
        }
    }

    @Override
    public List<Cliente> listarTodos() {
        String sql = "SELECT " + COLUMNAS + " FROM clientes ORDER BY id_cliente";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.lista(conexion, sql, ClienteDAO::mapear);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "listar clientes");
        }
    }

    @Override
    public Cliente buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM clientes WHERE id_cliente = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.uno(conexion, sql, ClienteDAO::mapear, id);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "buscar cliente por id");
        }
    }

    @Override
    public Cliente buscarPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM clientes WHERE lower(correo) = lower(?)";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.uno(conexion, sql, ClienteDAO::mapear, correo.trim());
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "buscar cliente por correo");
        }
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        if (cliente == null || cliente.getIdCliente() <= 0
                || cliente.getCorreo() == null || cliente.getCorreo().trim().isEmpty()
                || cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            return false;
        }

        Cliente actual = buscarPorId(cliente.getIdCliente());
        if (actual == null) {
            return false;
        }

        String correo = cliente.getCorreo().trim().toLowerCase(Locale.ROOT);
        Cliente conEseCorreo = buscarPorCorreo(correo);
        if (conEseCorreo != null && conEseCorreo.getIdCliente() != cliente.getIdCliente()) {
            return false;
        }

        if (cliente.getContrasena() != null && !PasswordUtil.esHash(cliente.getContrasena())) {
            cliente.setContrasena(PasswordUtil.hashear(cliente.getContrasena()));
        }
        if (cliente.getFechaRegistro() == null || cliente.getFechaRegistro().trim().isEmpty()) {
            cliente.setFechaRegistro(actual.getFechaRegistro());
        }
        cliente.setNombre(cliente.getNombre().trim());
        cliente.setCorreo(correo);

        String sql = "UPDATE clientes SET nombre = ?, correo = ?, "
                + "contrasena = COALESCE(?, contrasena), fecha_registro = ? "
                + "WHERE id_cliente = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            int filas = Jdbc.ejecutar(conexion, sql, cliente.getNombre(), correo,
                    cliente.getContrasena(), cliente.getFechaRegistro(), cliente.getIdCliente());
            return filas > 0;
        } catch (SQLException e) {
            if (BaseDatosException.esDuplicado(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "actualizar cliente");
        }
    }

    @Override
    public boolean eliminar(int id) {
        if (id <= 0) {
            return false;
        }
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.ejecutar(conexion, "DELETE FROM clientes WHERE id_cliente = ?", id) > 0;
        } catch (SQLException e) {
            if (BaseDatosException.esRestriccion(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "eliminar cliente");
        }
    }

    @Override
    public Cliente validarLogin(String correo, String contrasena) {
        Cliente cliente = buscarPorCorreo(correo);
        if (cliente == null || contrasena == null) {
            return null;
        }
        return PasswordUtil.verificar(contrasena, cliente.getContrasena()) ? cliente : null;
    }

    /**
     * Indica si el cliente tiene mascotas o citas que impiden borrarlo.
     * Los pedidos no cuentan: conservan el nombre del cliente como historial.
     */
    @Override
    public boolean tieneDependencias(int id) {
        if (id <= 0) {
            return false;
        }
        String sql = "SELECT (SELECT COUNT(*) FROM mascotas WHERE id_cliente = ?) "
                + "+ (SELECT COUNT(*) FROM citas WHERE id_cliente = ?) AS total";
        try (Connection conexion = BaseDatos.conexion()) {
            Integer total = Jdbc.uno(conexion, sql, rs -> rs.getInt("total"), id, id);
            return total != null && total > 0;
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "comprobar dependencias del cliente");
        }
    }

    private static Cliente mapear(ResultSet resultado) throws SQLException {
        return new Cliente(
                resultado.getInt("id_cliente"),
                resultado.getString("nombre"),
                resultado.getString("correo"),
                resultado.getString("contrasena"),
                resultado.getString("fecha_registro"));
    }
}
