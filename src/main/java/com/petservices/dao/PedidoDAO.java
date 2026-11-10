package com.petservices.dao;

import com.petservices.db.BaseDatos;
import com.petservices.db.BaseDatosException;
import com.petservices.db.Jdbc;
import com.petservices.modelo.DetallePedido;
import com.petservices.modelo.Pedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO de pedidos y su detalle sobre SQLite.
 *
 * Las operaciones que ajustan stock viven en {@code PedidoService}, que abre una
 * transacción y usa los métodos con {@link Connection} de este DAO. Los métodos
 * públicos de la interfaz hacen CRUD simple (sin tocar inventario) para mantener
 * el contrato existente y para consultas.
 */
public class PedidoDAO implements IPedidoDAO {

    private static final String COLUMNAS =
            "id_pedido, id_cliente, nombre_cliente, fecha, total, estado, cantidad, "
            + "id_establecimiento";

    @Override
    public boolean insertar(Pedido pedido) {
        if (pedido == null) {
            return false;
        }
        try {
            return BaseDatos.enTransaccion(conexion -> insertar(conexion, pedido));
        } catch (BaseDatosException e) {
            if (e.getCodigo() == BaseDatosException.Codigo.RESTRICCION) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public List<Pedido> listarTodos() {
        String sql = "SELECT " + COLUMNAS + " FROM pedidos ORDER BY id_pedido";
        try (Connection conexion = BaseDatos.conexion()) {
            List<Pedido> pedidos = Jdbc.lista(conexion, sql, PedidoDAO::mapear);
            cargarTodosLosDetalles(conexion, pedidos);
            return pedidos;
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "listar pedidos");
        }
    }

    @Override
    public Pedido buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        try (Connection conexion = BaseDatos.conexion()) {
            return buscarPorId(conexion, id);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "buscar pedido por id");
        }
    }

    @Override
    public boolean actualizar(Pedido pedido) {
        if (pedido == null || pedido.getIdPedido() <= 0) {
            return false;
        }
        try {
            return BaseDatos.enTransaccion(conexion -> actualizar(conexion, pedido));
        } catch (BaseDatosException e) {
            if (e.getCodigo() == BaseDatosException.Codigo.RESTRICCION) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public boolean eliminar(int id) {
        if (id <= 0) {
            return false;
        }
        return BaseDatos.enTransaccion(conexion -> eliminar(conexion, id));
    }

    // ──────────────────────────────────────────────────────────────────────
    // Operaciones dentro de una transacción (usadas por PedidoService)
    // ──────────────────────────────────────────────────────────────────────

    /** Busca un pedido con sus detalles usando la conexión de una transacción. */
    public Pedido buscarPorId(Connection conexion, int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM pedidos WHERE id_pedido = ?";
        Pedido pedido = Jdbc.uno(conexion, sql, PedidoDAO::mapear, id);
        if (pedido != null) {
            cargarDetalles(conexion, pedido);
        }
        return pedido;
    }

    /** Inserta la cabecera y los detalles; no ajusta stock. */
    public boolean insertar(Connection conexion, Pedido pedido) throws SQLException {
        String fecha = pedido.getFecha() == null || pedido.getFecha().trim().isEmpty()
                ? LocalDate.now().toString() : pedido.getFecha().trim();
        String estado = pedido.getEstado() == null || pedido.getEstado().trim().isEmpty()
                ? "Pendiente" : pedido.getEstado().trim();
        Integer idCliente = pedido.getIdCliente() > 0 ? pedido.getIdCliente() : null;
        Integer idEstablecimiento = pedido.getIdEstablecimiento() > 0
                ? pedido.getIdEstablecimiento() : null;

        String sql = "INSERT INTO pedidos "
                + "(id_cliente, nombre_cliente, fecha, total, estado, cantidad, "
                + " id_establecimiento) VALUES (?, ?, ?, ?, ?, ?, ?)";
        long id = Jdbc.insertar(conexion, sql, idCliente, pedido.getNombreCliente(), fecha,
                pedido.getTotal(), estado, pedido.getCantidad(), idEstablecimiento);
        pedido.setIdPedido((int) id);
        pedido.setFecha(fecha);
        pedido.setEstado(estado);
        pedido.setIdCliente(idCliente == null ? 0 : idCliente);
        pedido.setIdEstablecimiento(idEstablecimiento == null ? 0 : idEstablecimiento);
        insertarDetalles(conexion, pedido);
        return true;
    }

    /** Reemplaza la cabecera y los detalles; no ajusta stock. */
    public boolean actualizar(Connection conexion, Pedido pedido) throws SQLException {
        Integer idCliente = pedido.getIdCliente() > 0 ? pedido.getIdCliente() : null;
        Integer idEstablecimiento = pedido.getIdEstablecimiento() > 0
                ? pedido.getIdEstablecimiento() : null;
        String fecha = pedido.getFecha() == null || pedido.getFecha().trim().isEmpty()
                ? LocalDate.now().toString() : pedido.getFecha().trim();
        String estado = pedido.getEstado() == null || pedido.getEstado().trim().isEmpty()
                ? "Pendiente" : pedido.getEstado().trim();

        String sql = "UPDATE pedidos SET id_cliente = ?, nombre_cliente = ?, fecha = ?, "
                + "total = ?, estado = ?, cantidad = ?, id_establecimiento = ? "
                + "WHERE id_pedido = ?";
        int filas = Jdbc.ejecutar(conexion, sql, idCliente, pedido.getNombreCliente(), fecha,
                pedido.getTotal(), estado, pedido.getCantidad(), idEstablecimiento,
                pedido.getIdPedido());
        if (filas > 0) {
            Jdbc.ejecutar(conexion, "DELETE FROM detalle_pedido WHERE id_pedido = ?",
                    pedido.getIdPedido());
            pedido.setFecha(fecha);
            pedido.setEstado(estado);
            pedido.setIdCliente(idCliente == null ? 0 : idCliente);
            pedido.setIdEstablecimiento(idEstablecimiento == null ? 0 : idEstablecimiento);
            insertarDetalles(conexion, pedido);
        }
        return filas > 0;
    }

    /** Actualiza solo la cabecera; conserva los detalles existentes. */
    public boolean actualizarCabecera(Connection conexion, Pedido pedido) throws SQLException {
        Integer idCliente = pedido.getIdCliente() > 0 ? pedido.getIdCliente() : null;
        Integer idEstablecimiento = pedido.getIdEstablecimiento() > 0
                ? pedido.getIdEstablecimiento() : null;
        String fecha = pedido.getFecha() == null || pedido.getFecha().trim().isEmpty()
                ? LocalDate.now().toString() : pedido.getFecha().trim();
        String estado = pedido.getEstado() == null || pedido.getEstado().trim().isEmpty()
                ? "Pendiente" : pedido.getEstado().trim();

        String sql = "UPDATE pedidos SET id_cliente = ?, nombre_cliente = ?, fecha = ?, "
                + "total = ?, estado = ?, cantidad = ?, id_establecimiento = ? "
                + "WHERE id_pedido = ?";
        int filas = Jdbc.ejecutar(conexion, sql, idCliente, pedido.getNombreCliente(), fecha,
                pedido.getTotal(), estado, pedido.getCantidad(), idEstablecimiento,
                pedido.getIdPedido());
        if (filas > 0) {
            pedido.setFecha(fecha);
            pedido.setEstado(estado);
            pedido.setIdCliente(idCliente == null ? 0 : idCliente);
            pedido.setIdEstablecimiento(idEstablecimiento == null ? 0 : idEstablecimiento);
        }
        return filas > 0;
    }

    /** Elimina la cabecera; el detalle se elimina por ON DELETE CASCADE. */
    public boolean eliminar(Connection conexion, int id) throws SQLException {
        return Jdbc.ejecutar(conexion, "DELETE FROM pedidos WHERE id_pedido = ?", id) > 0;
    }

    private static void insertarDetalles(Connection conexion, Pedido pedido) throws SQLException {
        if (pedido.getDetalles() == null) {
            return;
        }
        String sql = "INSERT INTO detalle_pedido "
                + "(id_pedido, id_producto, nombre_producto, cantidad, precio_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        for (DetallePedido detalle : pedido.getDetalles()) {
            Integer idProducto = detalle.getIdProducto() > 0 ? detalle.getIdProducto() : null;
            double subtotal = detalle.getSubtotal();
            if (subtotal <= 0 && detalle.getPrecioUnitario() > 0) {
                subtotal = detalle.getCantidad() * detalle.getPrecioUnitario();
                detalle.setSubtotal(subtotal);
            }
            Jdbc.ejecutar(conexion, sql, pedido.getIdPedido(), idProducto,
                    detalle.getNombreProducto(), detalle.getCantidad(),
                    detalle.getPrecioUnitario(), subtotal);
        }
    }

    private static void cargarDetalles(Connection conexion, Pedido pedido) throws SQLException {
        String sql = "SELECT id_producto, nombre_producto, cantidad, precio_unitario, subtotal "
                + "FROM detalle_pedido WHERE id_pedido = ? ORDER BY id_detalle";
        List<DetallePedido> detalles = Jdbc.lista(conexion, sql, r -> {
            DetallePedido detalle = new DetallePedido(
                    r.getInt("id_producto"),
                    r.getString("nombre_producto"),
                    r.getInt("cantidad"),
                    r.getDouble("precio_unitario"));
            detalle.setSubtotal(r.getDouble("subtotal"));
            return detalle;
        }, pedido.getIdPedido());
        pedido.setDetalles(detalles);
    }

    private static void cargarTodosLosDetalles(Connection conexion, List<Pedido> pedidos)
            throws SQLException {
        if (pedidos.isEmpty()) {
            return;
        }
        Map<Integer, Pedido> porId = new LinkedHashMap<>();
        for (Pedido pedido : pedidos) {
            porId.put(pedido.getIdPedido(), pedido);
            pedido.setDetalles(new ArrayList<>());
        }
        String sql = "SELECT id_pedido, id_producto, nombre_producto, cantidad, "
                + "precio_unitario, subtotal FROM detalle_pedido ORDER BY id_pedido, id_detalle";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                Pedido pedido = porId.get(resultado.getInt("id_pedido"));
                if (pedido == null) {
                    continue;
                }
                DetallePedido detalle = new DetallePedido(
                        resultado.getInt("id_producto"),
                        resultado.getString("nombre_producto"),
                        resultado.getInt("cantidad"),
                        resultado.getDouble("precio_unitario"));
                detalle.setSubtotal(resultado.getDouble("subtotal"));
                pedido.getDetalles().add(detalle);
            }
        }
    }

    private static Pedido mapear(ResultSet resultado) throws SQLException {
        Pedido pedido = new Pedido(
                resultado.getInt("id_pedido"),
                resultado.getInt("id_cliente"),
                resultado.getString("nombre_cliente"),
                resultado.getString("fecha"),
                resultado.getDouble("total"),
                resultado.getString("estado"));
        pedido.setCantidad(resultado.getInt("cantidad"));
        pedido.setIdEstablecimiento(resultado.getInt("id_establecimiento"));
        pedido.setDetalles(new ArrayList<>());
        return pedido;
    }
}
