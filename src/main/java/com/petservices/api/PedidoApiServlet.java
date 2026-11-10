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
import com.petservices.service.PedidoService;

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
 * PUT    /api/pedidos/{id}  -> actualiza el estado, el total o el producto/cantidad
 * DELETE /api/pedidos/{id}  -> elimina un pedido y devuelve su stock
 *
 * Si el POST incluye "idProducto" y "cantidad", el servidor valida el stock,
 * calcula el total con el precio vigente y descuenta las unidades en una
 * transacción. Sin esos campos se mantiene el modo anterior (total manual).
 *
 * Si el PUT incluye "idProducto" o "cantidad", se reemplaza el detalle y se
 * ajusta el stock; si solo incluye "estado"/"total", no se toca el inventario.
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
@WebServlet("/api/pedidos/*")
public class PedidoApiServlet extends HttpServlet {

    private final IPedidoDAO pedidoDAO = new PedidoDAO();
    private final IProductoDAO productoDAO = new ProductoDAO();
    private final IClienteDAO clienteDAO = new ClienteDAO();
    private final PedidoService pedidoService = new PedidoService();

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

        // ── Modo anterior: total enviado manualmente, sin detalle ni stock ──
        if (datos.total == null || datos.total <= 0) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "El campo 'total' debe ser mayor que 0");
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
        Pedido nuevo = new Pedido(
                cliente == null ? 0 : cliente.getIdCliente(),
                cliente == null ? datos.nombreCliente : cliente.getNombre(),
                datos.fecha == null ? LocalDate.now().toString() : datos.fecha,
                datos.total,
                datos.estado == null ? "Pendiente" : datos.estado);
        if (datos.cantidad != null) {
            nuevo.setCantidad(datos.cantidad);
        }
        try {
            pedidoService.crearSinDetalle(nuevo);
        } catch (PedidoService.DatosPedidoInvalidosException e) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }
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

        Cliente cliente = null;
        if (datos.idCliente != null && datos.idCliente > 0) {
            cliente = clienteDAO.buscarPorId(datos.idCliente);
            if (cliente == null) {
                enviarError(response, HttpServletResponse.SC_NOT_FOUND,
                        "El cliente con id " + datos.idCliente + " no existe");
                return;
            }
        }

        Pedido nuevo = new Pedido(
                cliente == null ? 0 : cliente.getIdCliente(),
                cliente == null ? datos.nombreCliente : cliente.getNombre(),
                datos.fecha == null ? LocalDate.now().toString() : datos.fecha,
                0,
                datos.estado == null ? "Pendiente" : datos.estado);
        nuevo.setIdEstablecimiento(datos.idEstablecimiento == null ? 0 : datos.idEstablecimiento);
        nuevo.setCantidad(datos.cantidad);
        nuevo.setDetalles(new ArrayList<>());
        nuevo.getDetalles().add(new DetallePedido(
                producto.getIdProducto(), producto.getNombre(), datos.cantidad, producto.getPrecio()));

        try {
            pedidoService.crear(nuevo);
        } catch (PedidoService.StockInsuficienteException e) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        } catch (PedidoService.DatosPedidoInvalidosException e) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }
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

        boolean cambiaDetalle = (datos.idProducto != null && datos.idProducto > 0)
                || datos.cantidad != null;
        if (cambiaDetalle) {
            actualizarConDetalle(existente, datos, response);
            return;
        }

        try {
            Pedido actualizado = pedidoService.actualizarSinDetalle(id, datos.estado, datos.total);
            enviarJson(response, HttpServletResponse.SC_OK, actualizado);
        } catch (PedidoService.DatosPedidoInvalidosException e) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (PedidoService.PedidoNoEncontradoException e) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    private void actualizarConDetalle(Pedido existente, PedidoEntrada datos,
                                      HttpServletResponse response) throws IOException {
        Integer idProducto = datos.idProducto;
        if (idProducto == null || idProducto <= 0) {
            if (existente.getDetalles() == null || existente.getDetalles().isEmpty()) {
                enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Para cambiar la cantidad debes indicar 'idProducto'");
                return;
            }
            idProducto = existente.getDetalles().get(0).getIdProducto();
        }
        Producto producto = productoDAO.buscarPorId(idProducto);
        if (producto == null) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND,
                    "El producto con id " + idProducto + " no existe");
            return;
        }
        int cantidad = datos.cantidad != null ? datos.cantidad : existente.getCantidad();
        if (cantidad <= 0) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "La cantidad debe ser mayor que 0");
            return;
        }

        Pedido nuevo = new Pedido(existente.getIdPedido(), existente.getIdCliente(),
                existente.getNombreCliente(),
                datos.fecha != null && !datos.fecha.trim().isEmpty()
                        ? datos.fecha : existente.getFecha(),
                0,
                datos.estado != null && !datos.estado.trim().isEmpty()
                        ? datos.estado : existente.getEstado());
        nuevo.setIdEstablecimiento(datos.idEstablecimiento != null
                ? datos.idEstablecimiento : existente.getIdEstablecimiento());
        nuevo.setCantidad(cantidad);
        nuevo.setDetalles(new ArrayList<>());
        nuevo.getDetalles().add(new DetallePedido(
                producto.getIdProducto(), producto.getNombre(), cantidad, producto.getPrecio()));

        try {
            Pedido actualizado = pedidoService.actualizarConDetalle(existente.getIdPedido(), nuevo);
            enviarJson(response, HttpServletResponse.SC_OK, actualizado);
        } catch (PedidoService.StockInsuficienteException e) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (PedidoService.DatosPedidoInvalidosException e) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (PedidoService.PedidoNoEncontradoException e) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
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
        if (pedidoService.eliminar(id)) {
            enviarJson(response, HttpServletResponse.SC_OK, new MensajeDTO("Pedido eliminado correctamente"));
        } else {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Pedido no encontrado con id " + id);
        }
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
