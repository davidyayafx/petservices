package com.petservices.dao;

import com.petservices.db.BaseDatos;
import com.petservices.db.BaseDatosException;
import com.petservices.db.Jdbc;
import com.petservices.modelo.Producto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO de productos e inventario sobre SQLite.
 *
 * El estado se calcula a partir del stock para evitar incoherencias:
 * stock &gt; 0 = disponible; stock = 0 = agotado. El nombre es único sin
 * distinguir mayúsculas.
 */
public class ProductoDAO implements IProductoDAO {

    private static final String COLUMNAS =
            "id_producto, nombre, categoria, precio, stock, descripcion, estado, "
            + "id_establecimiento, id_cliente";

    @Override
    public boolean insertar(Producto producto) {
        if (!datosValidos(producto)) {
            return false;
        }
        Integer idEstablecimiento = normalizarId(producto.getIdEstablecimiento());
        Integer idCliente = normalizarId(producto.getIdCliente());
        String descripcion = descripcion(producto);
        String estado = estado(producto.getStock());

        String sql = "INSERT INTO productos "
                + "(nombre, categoria, precio, stock, descripcion, estado, "
                + " id_establecimiento, id_cliente) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = BaseDatos.conexion()) {
            long id = Jdbc.insertar(conexion, sql, producto.getNombre().trim(),
                    producto.getCategoria().trim(), producto.getPrecio(), producto.getStock(),
                    descripcion, estado, idEstablecimiento, idCliente);
            producto.setIdProducto((int) id);
            producto.setDescripcion(descripcion);
            producto.setEstado(estado);
            producto.setIdEstablecimiento(idEstablecimiento == null ? 0 : idEstablecimiento);
            producto.setIdCliente(idCliente);
            return true;
        } catch (SQLException e) {
            if (BaseDatosException.esDuplicado(e) || BaseDatosException.esRestriccion(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "insertar producto");
        }
    }

    @Override
    public List<Producto> listarTodos() {
        String sql = "SELECT " + COLUMNAS + " FROM productos ORDER BY id_producto";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.lista(conexion, sql, ProductoDAO::mapear);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "listar productos");
        }
    }

    @Override
    public Producto buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        String sql = "SELECT " + COLUMNAS + " FROM productos WHERE id_producto = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.uno(conexion, sql, ProductoDAO::mapear, id);
        } catch (SQLException e) {
            throw BaseDatosException.desde(e, "buscar producto por id");
        }
    }

    @Override
    public boolean actualizar(Producto producto) {
        if (!datosValidos(producto) || producto.getIdProducto() <= 0) {
            return false;
        }
        if (buscarPorId(producto.getIdProducto()) == null) {
            return false;
        }
        Integer idEstablecimiento = normalizarId(producto.getIdEstablecimiento());
        Integer idCliente = normalizarId(producto.getIdCliente());
        String descripcion = descripcion(producto);
        String estado = estado(producto.getStock());

        String sql = "UPDATE productos SET nombre = ?, categoria = ?, precio = ?, stock = ?, "
                + "descripcion = ?, estado = ?, id_establecimiento = ?, id_cliente = ? "
                + "WHERE id_producto = ?";
        try (Connection conexion = BaseDatos.conexion()) {
            int filas = Jdbc.ejecutar(conexion, sql, producto.getNombre().trim(),
                    producto.getCategoria().trim(), producto.getPrecio(), producto.getStock(),
                    descripcion, estado, idEstablecimiento, idCliente, producto.getIdProducto());
            if (filas > 0) {
                producto.setDescripcion(descripcion);
                producto.setEstado(estado);
                producto.setIdEstablecimiento(idEstablecimiento == null ? 0 : idEstablecimiento);
                producto.setIdCliente(idCliente);
            }
            return filas > 0;
        } catch (SQLException e) {
            if (BaseDatosException.esDuplicado(e) || BaseDatosException.esRestriccion(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "actualizar producto");
        }
    }

    @Override
    public boolean eliminar(int id) {
        if (id <= 0) {
            return false;
        }
        try (Connection conexion = BaseDatos.conexion()) {
            return Jdbc.ejecutar(conexion, "DELETE FROM productos WHERE id_producto = ?", id) > 0;
        } catch (SQLException e) {
            if (BaseDatosException.esRestriccion(e)) {
                return false;
            }
            throw BaseDatosException.desde(e, "eliminar producto");
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Operaciones dentro de una transacción (usadas por PedidoService)
    // ──────────────────────────────────────────────────────────────────────

    /** Busca un producto reutilizando la conexión de una transacción. */
    public Producto buscarPorId(Connection conexion, int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM productos WHERE id_producto = ?";
        return Jdbc.uno(conexion, sql, ProductoDAO::mapear, id);
    }

    /** Descuenta stock solo si hay existencias suficientes; devuelve false si no alcanza. */
    public boolean descontarStock(Connection conexion, int idProducto, int cantidad) throws SQLException {
        return Jdbc.ejecutar(conexion,
                "UPDATE productos SET stock = stock - ?, "
                        + "estado = CASE WHEN (stock - ?) > 0 THEN 'disponible' ELSE 'agotado' END "
                        + "WHERE id_producto = ? AND stock >= ?",
                cantidad, cantidad, idProducto, cantidad) > 0;
    }

    /** Devuelve unidades al inventario (al editar o eliminar un pedido). */
    public void devolverStock(Connection conexion, int idProducto, int cantidad) throws SQLException {
        Jdbc.ejecutar(conexion,
                "UPDATE productos SET stock = stock + ?, "
                        + "estado = CASE WHEN (stock + ?) > 0 THEN 'disponible' ELSE 'agotado' END "
                        + "WHERE id_producto = ?",
                cantidad, cantidad, idProducto);
    }

    private static boolean datosValidos(Producto producto) {
        return producto != null
                && producto.getNombre() != null && !producto.getNombre().trim().isEmpty()
                && producto.getCategoria() != null && !producto.getCategoria().trim().isEmpty()
                && producto.getPrecio() > 0
                && producto.getStock() >= 0;
    }

    private static String descripcion(Producto producto) {
        String descripcion = producto.getDescripcion();
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return producto.getCategoria().trim();
        }
        return descripcion.trim();
    }

    private static String estado(int stock) {
        return stock > 0 ? "disponible" : "agotado";
    }

    private static Integer normalizarId(Integer id) {
        return id == null || id <= 0 ? null : id;
    }

    private static Producto mapear(ResultSet resultado) throws SQLException {
        Producto producto = new Producto(
                resultado.getInt("id_producto"),
                resultado.getString("nombre"),
                resultado.getString("categoria"),
                resultado.getDouble("precio"),
                resultado.getInt("stock"));
        producto.setDescripcion(resultado.getString("descripcion"));
        producto.setEstado(resultado.getString("estado"));
        Integer idEstablecimiento = Jdbc.enteroNullable(resultado, "id_establecimiento");
        producto.setIdEstablecimiento(idEstablecimiento == null ? 0 : idEstablecimiento);
        producto.setIdCliente(Jdbc.enteroNullable(resultado, "id_cliente"));
        return producto;
    }
}
