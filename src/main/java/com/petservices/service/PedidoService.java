package com.petservices.service;

import com.petservices.dao.PedidoDAO;
import com.petservices.dao.ProductoDAO;
import com.petservices.db.BaseDatos;
import com.petservices.db.Jdbc;
import com.petservices.modelo.DetallePedido;
import com.petservices.modelo.Pedido;
import com.petservices.modelo.Producto;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Servicio transaccional de pedidos.
 *
 * Centraliza las operaciones que afectan al mismo tiempo la tabla de pedidos,
 * su detalle y el stock de productos. Todo se ejecuta dentro de una única
 * transacción SQLite: si falla cualquier paso, se revierte completo.
 */
public class PedidoService {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    /** El stock disponible no alcanza para la cantidad solicitada. */
    public static class StockInsuficienteException extends RuntimeException {
        private final int idProducto;
        private final String nombreProducto;
        private final int disponible;
        private final int solicitado;

        public StockInsuficienteException(int idProducto, String nombreProducto,
                                          int disponible, int solicitado) {
            super("Stock insuficiente para " + nombreProducto + ": disponible "
                    + disponible + ", solicitado " + solicitado + ".");
            this.idProducto = idProducto;
            this.nombreProducto = nombreProducto;
            this.disponible = disponible;
            this.solicitado = solicitado;
        }

        public int getIdProducto() {
            return idProducto;
        }

        public String getNombreProducto() {
            return nombreProducto;
        }

        public int getDisponible() {
            return disponible;
        }

        public int getSolicitado() {
            return solicitado;
        }
    }

    /** Los datos del pedido no son válidos (cliente, producto, estado, etc.). */
    public static class DatosPedidoInvalidosException extends RuntimeException {
        public DatosPedidoInvalidosException(String mensaje) {
            super(mensaje);
        }
    }

    /** El pedido que se intenta editar o eliminar no existe. */
    public static class PedidoNoEncontradoException extends RuntimeException {
        public PedidoNoEncontradoException(int id) {
            super("No se encontró el pedido con id " + id + ".");
        }
    }

    /**
     * Crea un pedido con detalle, valida stock y lo descuenta en una transacción.
     * El total y la cantidad se recalculan con el precio vigente del producto.
     */
    public Pedido crear(Pedido pedido) {
        if (pedido == null) {
            throw new DatosPedidoInvalidosException("El pedido está vacío.");
        }
        return BaseDatos.enTransaccion(conexion -> {
            normalizarCabecera(conexion, pedido);
            prepararDetallesYDescontar(conexion, pedido);
            pedidoDAO.insertar(conexion, pedido);
            return pedido;
        });
    }

    /**
     * Crea un pedido sin detalle (modo de compatibilidad con el total enviado
     * manualmente por la API). No toca stock.
     */
    public Pedido crearSinDetalle(Pedido pedido) {
        if (pedido == null) {
            throw new DatosPedidoInvalidosException("El pedido está vacío.");
        }
        return BaseDatos.enTransaccion(conexion -> {
            normalizarCabecera(conexion, pedido);
            if (pedido.getTotal() < 0) {
                throw new DatosPedidoInvalidosException("El total no puede ser negativo.");
            }
            pedido.setCantidad(Math.max(0, pedido.getCantidad()));
            pedido.setDetalles(List.of());
            pedidoDAO.insertar(conexion, pedido);
            return pedido;
        });
    }

    /**
     * Reemplaza un pedido completo y ajusta el stock: devuelve las unidades del
     * pedido anterior y descuenta las del nuevo, todo en una transacción.
     */
    public Pedido actualizarConDetalle(int id, Pedido pedido) {
        if (pedido == null) {
            throw new DatosPedidoInvalidosException("El pedido está vacío.");
        }
        return BaseDatos.enTransaccion(conexion -> {
            Pedido anterior = pedidoDAO.buscarPorId(conexion, id);
            if (anterior == null) {
                throw new PedidoNoEncontradoException(id);
            }
            conservarCamposNoEnviados(pedido, anterior);
            devolverStock(conexion, anterior);
            normalizarCabecera(conexion, pedido);
            prepararDetallesYDescontar(conexion, pedido);
            pedido.setIdPedido(id);
            pedidoDAO.actualizar(conexion, pedido);
            return pedido;
        });
    }

    /**
     * Actualiza solo la cabecera (estado y/o total) sin tocar el detalle ni el
     * stock. La cantidad no se puede cambiar por esta vía: para eso se usa el
     * detalle y {@link #actualizarConDetalle(int, Pedido)}.
     */
    public Pedido actualizarSinDetalle(int id, String estado, Double total) {
        return BaseDatos.enTransaccion(conexion -> {
            Pedido pedido = pedidoDAO.buscarPorId(conexion, id);
            if (pedido == null) {
                throw new PedidoNoEncontradoException(id);
            }
            if (estado != null && !estado.trim().isEmpty()) {
                String canonico = normalizarEstado(estado);
                if (canonico == null) {
                    throw new DatosPedidoInvalidosException(
                            "Estado inválido. Valores permitidos: Pendiente, Pagado, Entregado.");
                }
                pedido.setEstado(canonico);
            }
            if (total != null) {
                if (total < 0) {
                    throw new DatosPedidoInvalidosException("El total no puede ser negativo.");
                }
                pedido.setTotal(total);
            }
            pedidoDAO.actualizarCabecera(conexion, pedido);
            return pedido;
        });
    }

    /** Elimina un pedido y devuelve su stock en una transacción. */
    public boolean eliminar(int id) {
        return BaseDatos.enTransaccion(conexion -> {
            Pedido pedido = pedidoDAO.buscarPorId(conexion, id);
            if (pedido == null) {
                return false;
            }
            devolverStock(conexion, pedido);
            return pedidoDAO.eliminar(conexion, id);
        });
    }

    // ──────────────────────────────────────────────────────────────────────
    // Internos
    // ──────────────────────────────────────────────────────────────────────

    private void normalizarCabecera(Connection conexion, Pedido pedido) throws SQLException {
        String estado = normalizarEstado(pedido.getEstado());
        if (estado == null) {
            throw new DatosPedidoInvalidosException(
                    "Estado inválido. Valores permitidos: Pendiente, Pagado, Entregado.");
        }
        pedido.setEstado(estado);

        String fecha = pedido.getFecha();
        pedido.setFecha(fecha == null || fecha.trim().isEmpty()
                ? LocalDate.now().toString() : fecha.trim());

        if (pedido.getIdCliente() > 0) {
            String nombre = Jdbc.uno(conexion,
                    "SELECT nombre FROM clientes WHERE id_cliente = ?",
                    resultado -> resultado.getString(1), pedido.getIdCliente());
            if (nombre == null) {
                throw new DatosPedidoInvalidosException(
                        "El cliente con id " + pedido.getIdCliente() + " no existe.");
            }
            pedido.setNombreCliente(nombre);
        } else {
            pedido.setIdCliente(0);
        }

        if (pedido.getIdEstablecimiento() > 0) {
            Integer existe = Jdbc.uno(conexion,
                    "SELECT 1 FROM establecimientos WHERE id_establecimiento = ?",
                    resultado -> 1, pedido.getIdEstablecimiento());
            if (existe == null) {
                throw new DatosPedidoInvalidosException(
                        "El establecimiento con id " + pedido.getIdEstablecimiento() + " no existe.");
            }
        } else {
            pedido.setIdEstablecimiento(0);
        }
    }

    private void prepararDetallesYDescontar(Connection conexion, Pedido pedido) throws SQLException {
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new DatosPedidoInvalidosException(
                    "El pedido debe incluir al menos un producto y una cantidad.");
        }

        int cantidadTotal = 0;
        double total = 0;
        for (DetallePedido detalle : pedido.getDetalles()) {
            if (detalle == null || detalle.getIdProducto() <= 0) {
                throw new DatosPedidoInvalidosException("Cada detalle debe indicar un producto válido.");
            }
            if (detalle.getCantidad() <= 0) {
                throw new DatosPedidoInvalidosException("La cantidad de cada producto debe ser mayor que 0.");
            }

            Producto producto = productoDAO.buscarPorId(conexion, detalle.getIdProducto());
            if (producto == null) {
                throw new DatosPedidoInvalidosException(
                        "El producto con id " + detalle.getIdProducto() + " no existe.");
            }
            if (producto.getStock() < detalle.getCantidad()) {
                throw new StockInsuficienteException(producto.getIdProducto(), producto.getNombre(),
                        producto.getStock(), detalle.getCantidad());
            }

            detalle.setIdProducto(producto.getIdProducto());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setPrecioUnitario(producto.getPrecio());
            double subtotal = detalle.getCantidad() * producto.getPrecio();
            detalle.setSubtotal(subtotal);

            if (!productoDAO.descontarStock(conexion, producto.getIdProducto(), detalle.getCantidad())) {
                Producto actual = productoDAO.buscarPorId(conexion, producto.getIdProducto());
                int disponible = actual == null ? 0 : actual.getStock();
                throw new StockInsuficienteException(producto.getIdProducto(), producto.getNombre(),
                        disponible, detalle.getCantidad());
            }

            cantidadTotal += detalle.getCantidad();
            total += subtotal;
        }
        pedido.setCantidad(cantidadTotal);
        pedido.setTotal(total);
    }

    private void devolverStock(Connection conexion, Pedido pedido) throws SQLException {
        if (pedido.getDetalles() == null) {
            return;
        }
        for (DetallePedido detalle : pedido.getDetalles()) {
            if (detalle != null && detalle.getIdProducto() > 0 && detalle.getCantidad() > 0) {
                productoDAO.devolverStock(conexion, detalle.getIdProducto(), detalle.getCantidad());
            }
        }
    }

    private static void conservarCamposNoEnviados(Pedido nuevo, Pedido anterior) {
        if (nuevo.getFecha() == null || nuevo.getFecha().trim().isEmpty()) {
            nuevo.setFecha(anterior.getFecha());
        }
        if (nuevo.getEstado() == null || nuevo.getEstado().trim().isEmpty()) {
            nuevo.setEstado(anterior.getEstado());
        }
        if (nuevo.getIdCliente() <= 0) {
            nuevo.setIdCliente(anterior.getIdCliente());
            nuevo.setNombreCliente(anterior.getNombreCliente());
        }
        if (nuevo.getIdEstablecimiento() <= 0) {
            nuevo.setIdEstablecimiento(anterior.getIdEstablecimiento());
        }
    }

    /** Acepta el estado sin distinguir mayúsculas y devuelve su forma canónica. */
    public static String normalizarEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return "Pendiente";
        }
        switch (estado.trim().toLowerCase(Locale.ROOT)) {
            case "pendiente":
                return "Pendiente";
            case "pagado":
                return "Pagado";
            case "entregado":
                return "Entregado";
            default:
                return null;
        }
    }
}
