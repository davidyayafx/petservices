package com.petservices.controller;

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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/pedidos")
public class PedidoServlet extends BaseModuleServlet {

    private static final Set<String> ESTADOS_VALIDOS = new HashSet<>(
            Arrays.asList("Pendiente", "Pagado", "Entregado"));

    private final IPedidoDAO pedidoDAO = new PedidoDAO();
    private final IClienteDAO clienteDAO = new ClienteDAO();
    private final IProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!validarSesion(request, response)) {
            return;
        }

        prepararCabecera(request);
        String accion = request.getParameter("accion");
        if ("editar".equalsIgnoreCase(accion)) {
            int id = parseEntero(request.getParameter("id"));
            if (id <= 0) {
                enviarMensaje(request, "❌ El identificador del pedido no es válido.", "error");
            } else {
                Pedido pedido = pedidoDAO.buscarPorId(id);
                if (pedido != null) {
                    request.setAttribute("pedidoEditar", pedido);
                } else {
                    enviarMensaje(request, "❌ El pedido seleccionado no existe.", "error");
                }
            }
        }

        cargarVista(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!validarSesion(request, response)) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        prepararCabecera(request);
        String accion = request.getParameter("accion");

        if ("eliminar".equalsIgnoreCase(accion)) {
            eliminarPedido(request);
        } else if ("actualizar".equalsIgnoreCase(accion)) {
            actualizarPedido(request);
        } else {
            guardarPedido(request);
        }

        cargarVista(request, response);
    }

    private void guardarPedido(HttpServletRequest request) {
        Pedido pedido = construirPedidoDesdeRequest(request, 0);
        if (pedido == null) {
            return;
        }

        if (pedidoDAO.insertar(pedido)) {
            enviarMensaje(request, "✅ Pedido registrado correctamente.", "exito");
        } else {
            enviarMensaje(request, "❌ No fue posible registrar el pedido.", "error");
        }
    }

    private void actualizarPedido(HttpServletRequest request) {
        int id = parseEntero(request.getParameter("id"));
        Pedido existente = pedidoDAO.buscarPorId(id);
        if (existente == null) {
            enviarMensaje(request, "❌ El pedido que intentas actualizar no existe.", "error");
            return;
        }

        Pedido pedido = construirPedidoDesdeRequest(request, id);
        if (pedido == null) {
            request.setAttribute("pedidoEditar", existente);
            return;
        }

        pedido.setIdPedido(id);
        if (pedidoDAO.actualizar(pedido)) {
            enviarMensaje(request, "✅ Pedido actualizado correctamente.", "exito");
        } else {
            enviarMensaje(request, "❌ No se pudo actualizar el pedido.", "error");
            request.setAttribute("pedidoEditar", existente);
        }
    }

    private void eliminarPedido(HttpServletRequest request) {
        int id = parseEntero(request.getParameter("id"));
        if (id <= 0) {
            enviarMensaje(request, "❌ El ID del pedido es inválido.", "error");
            return;
        }

        if (pedidoDAO.eliminar(id)) {
            enviarMensaje(request, "✅ Pedido eliminado correctamente.", "exito");
        } else {
            enviarMensaje(request, "❌ No se encontró el pedido a eliminar.", "error");
        }
    }

    private Pedido construirPedidoDesdeRequest(HttpServletRequest request, int idActual) {
        int idCliente = parseEntero(request.getParameter("idCliente"));
        int idProducto = parseEntero(request.getParameter("idProducto"));
        int cantidad = parseEntero(request.getParameter("cantidad"));
        String fecha = limpiar(request.getParameter("fecha"));
        String estado = limpiar(request.getParameter("estado"));

        if (idCliente <= 0 || idProducto <= 0 || cantidad <= 0 || fecha.isEmpty() || estado.isEmpty()) {
            enviarMensaje(request, "❌ Todos los campos del pedido son obligatorios.", "error");
            return null;
        }

        if (!ESTADOS_VALIDOS.contains(estado)) {
            enviarMensaje(request, "❌ El estado del pedido no es válido.", "error");
            return null;
        }

        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        Producto producto = productoDAO.buscarPorId(idProducto);
        if (cliente == null) {
            enviarMensaje(request, "❌ Debes seleccionar un cliente válido.", "error");
            return null;
        }

        if (producto == null) {
            enviarMensaje(request, "❌ Debes seleccionar un producto válido.", "error");
            return null;
        }

        double total = producto.getPrecio() * cantidad;
        DetallePedido detalle = new DetallePedido(producto.getIdProducto(), producto.getNombre(), cantidad, producto.getPrecio());

        Pedido pedido = new Pedido(idActual, idCliente, cliente.getNombre(),
                fecha.isEmpty() ? LocalDate.now().toString() : fecha, total, estado);
        pedido.setCantidad(cantidad);
        pedido.setDetalles(new ArrayList<>());
        pedido.getDetalles().add(detalle);
        return pedido;
    }

    private void cargarVista(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Pedido> pedidos = pedidoDAO.listarTodos();
        List<Cliente> clientes = clienteDAO.listarTodos();
        List<Producto> productos = productoDAO.listarTodos();
        request.setAttribute("listaPedidos", pedidos);
        request.setAttribute("listaClientes", clientes);
        request.setAttribute("listaProductos", productos);
        request.getRequestDispatcher("/vistas/pedidos.jsp").forward(request, response);
    }
}
