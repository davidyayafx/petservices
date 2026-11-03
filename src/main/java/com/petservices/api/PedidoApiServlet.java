package com.petservices.api;

import com.petservices.dao.ClienteDAO;
import com.petservices.dao.IClienteDAO;
import com.petservices.dao.IPedidoDAO;
import com.petservices.dao.IProductoDAO;
import com.petservices.dao.PedidoDAO;
import com.petservices.dao.ProductoDAO;
import com.petservices.modelo.Cliente;
import com.petservices.modelo.DetallePedido;
import com.petservices.modelo.Pedido;
import com.petservices.modelo.Producto;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

import static com.petservices.api.util.ApiUtil.*;

/**
 * Servicio REST: /api/pedidos
 * ──────────────────────────────────────────────────────────────────────────
 * GET    /api/pedidos       -> lista todos los pedidos
 * GET    /api/pedidos/{id}  -> obtiene un pedido por id
 * POST   /api/pedidos       -> crea un pedido (body JSON)
 * PUT    /api/pedidos/{id}  -> actualiza el estado/datos de un pedido
 * DELETE /api/pedidos/{id}  -> elimina un pedido y devuelve su stock
 *
 * Si el POST incluye "idProducto" y "cantidad", el servidor valida el stock,
 * calcula el total con el precio vigente y descuenta las unidades. Sin esos
 * campos se mantiene el comportamiento anterior (total enviado a mano).
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
@WebServlet("/api/pedidos/*")
public class PedidoApiServlet extends HttpServlet {

    private final IPedidoDAO pedidoDAO = new PedidoDAO();
    private final IProductoDAO productoDAO = new ProductoDAO();
    private final IClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id == ID_AUSENTE) {
            enviarJson(response, HttpServletResponse.SC_OK, pedidoDAO.listarTodos());
        } else {
            Pedido p = pedidoDAO.buscarPorId(id);
            if (p == null) {
                enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Pedido no encontrado con id " + id);
            } else {
                enviarJson(response, HttpServletResponse.SC_OK, p);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        PedidoEntrada datos = leerJson(request, PedidoEntrada.class);
        if (datos == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }

        if (datos.idProducto != null && datos.idProducto > 0) {
            crearConProducto(datos, response);
            return;
        }

        // ── Modo anterior: total enviado manualmente ──
        if (datos.total == null || datos.total <= 0) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "El campo 'total' debe ser mayor que 0");
            return;
        }
        Pedido nuevo = new Pedido(
                datos.idCliente == null ? 0 : datos.idCliente,
                datos.nombreCliente,
                datos.fecha == null ? LocalDate.now().toString() : datos.fecha,
                datos.total,
                datos.estado == null ? "Pendiente" : datos.estado);
        if (datos.cantidad != null) {
            nuevo.setCantidad(datos.cantidad);
        }
        pedidoDAO.insertar(nuevo);
        enviarJson(response, HttpServletResponse.SC_CREATED, nuevo);
    }

    /** Crea un pedido a partir de un producto: valida stock, calcula total y descuenta. */
    private void crearConProducto(PedidoEntrada datos, HttpServletResponse response) throws IOException {
        if (datos.cantidad == null || datos.cantidad <= 0) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "El campo 'cantidad' debe ser mayor que 0 cuando se envía 'idProducto'");
            return;
        }
        Producto producto = productoDAO.buscarPorId(datos.idProducto);
        if (producto == null) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND,
                    "El producto con id " + datos.idProducto + " no existe");
            return;
        }
        if (datos.cantidad > producto.getStock()) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Stock insuficiente: disponible " + producto.getStock()
                            + ", solicitado " + datos.cantidad);
            return;
        }

        Cliente cliente = null;
        if (datos.idCliente != null && datos.idCliente > 0) {
            cliente = clienteDAO.buscarPorId(datos.idCliente);
            if (cliente == null) {
                enviarError(response, HttpServletResponse.SC_NOT_FOUND,
                        "El cliente con id " + datos.idCliente + " no existe");
                return;
            }
        }

        double total = producto.getPrecio() * datos.cantidad;
        Pedido nuevo = new Pedido(
                cliente == null ? 0 : cliente.getIdCliente(),
                cliente == null ? datos.nombreCliente : cliente.getNombre(),
                datos.fecha == null ? LocalDate.now().toString() : datos.fecha,
                total,
                datos.estado == null ? "Pendiente" : datos.estado);
        nuevo.setCantidad(datos.cantidad);
        nuevo.setDetalles(new ArrayList<>());
        nuevo.getDetalles().add(new DetallePedido(
                producto.getIdProducto(), producto.getNombre(), datos.cantidad, producto.getPrecio()));

        pedidoDAO.insertar(nuevo);
        producto.setStock(producto.getStock() - datos.cantidad);
        productoDAO.actualizar(producto);
        enviarJson(response, HttpServletResponse.SC_CREATED, nuevo);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id == ID_AUSENTE) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el id en la URL: /api/pedidos/{id}");
            return;
        }
        Pedido existente = pedidoDAO.buscarPorId(id);
        if (existente == null) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Pedido no encontrado con id " + id);
            return;
        }
        request.setCharacterEncoding("UTF-8");
        PedidoEntrada datos = leerJson(request, PedidoEntrada.class);
        if (datos == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }
        if (datos.estado != null) existente.setEstado(datos.estado);
        if (datos.total != null && datos.total > 0) existente.setTotal(datos.total);
        if (datos.cantidad != null && datos.cantidad > 0) existente.setCantidad(datos.cantidad);
        pedidoDAO.actualizar(existente);
        enviarJson(response, HttpServletResponse.SC_OK, existente);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id == ID_AUSENTE) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el id en la URL: /api/pedidos/{id}");
            return;
        }
        Pedido pedido = pedidoDAO.buscarPorId(id);
        boolean eliminado = pedidoDAO.eliminar(id);
        if (eliminado) {
            devolverStock(pedido);
            enviarJson(response, HttpServletResponse.SC_OK, new MensajeDTO("Pedido eliminado correctamente"));
        } else {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Pedido no encontrado con id " + id);
        }
    }

    private void devolverStock(Pedido pedido) {
        if (pedido == null || pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            return;
        }
        DetallePedido detalle = pedido.getDetalles().get(0);
        Producto producto = productoDAO.buscarPorId(detalle.getIdProducto());
        if (producto == null) {
            return;
        }
        producto.setStock(producto.getStock() + pedido.getCantidad());
        productoDAO.actualizar(producto);
    }

    /** Cuerpo de entrada de POST/PUT (campos opcionales distinguen "no enviado"). */
    static class PedidoEntrada {
        Integer idCliente;
        String nombreCliente;
        String fecha;
        Double total;
        String estado;
        Integer cantidad;
        Integer idProducto;
        Integer idEstablecimiento;
    }
}
