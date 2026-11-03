package com.petservices.controller;

import com.petservices.dao.IProductoDAO;
import com.petservices.dao.ProductoDAO;
import com.petservices.modelo.Producto;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/productos")
public class ProductoServlet extends BaseModuleServlet {

    private static final Set<String> CATEGORIAS_VALIDAS = new HashSet<>(
            Arrays.asList("Alimento", "Medicamento", "Accesorio", "Juguete"));

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
                enviarMensaje(request, "❌ El identificador del producto no es válido.", "error");
            } else {
                Producto producto = productoDAO.buscarPorId(id);
                if (producto != null) {
                    request.setAttribute("productoEditar", producto);
                } else {
                    enviarMensaje(request, "❌ El producto seleccionado no existe.", "error");
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

        if (!validarCsrf(request, response)) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        prepararCabecera(request);
        String accion = request.getParameter("accion");

        if ("eliminar".equalsIgnoreCase(accion)) {
            eliminarProducto(request);
        } else if ("actualizar".equalsIgnoreCase(accion)) {
            actualizarProducto(request);
        } else {
            guardarProducto(request);
        }

        cargarVista(request, response);
    }

    private void guardarProducto(HttpServletRequest request) {
        Producto producto = construirProductoDesdeRequest(request, 0);
        if (producto == null) {
            return;
        }

        if (productoDAO.insertar(producto)) {
            enviarMensaje(request, "✅ Producto registrado correctamente.", "exito");
        } else {
            enviarMensaje(request, "❌ No fue posible registrar el producto.", "error");
        }
    }

    private void actualizarProducto(HttpServletRequest request) {
        int id = parseEntero(request.getParameter("id"));
        Producto existente = productoDAO.buscarPorId(id);
        if (existente == null) {
            enviarMensaje(request, "❌ El producto que intentas actualizar no existe.", "error");
            return;
        }

        Producto producto = construirProductoDesdeRequest(request, id);
        if (producto == null) {
            request.setAttribute("productoEditar", existente);
            return;
        }

        producto.setIdProducto(id);
        if (productoDAO.actualizar(producto)) {
            enviarMensaje(request, "✅ Producto actualizado correctamente.", "exito");
        } else {
            enviarMensaje(request, "❌ No se pudo actualizar el producto.", "error");
            request.setAttribute("productoEditar", existente);
        }
    }

    private void eliminarProducto(HttpServletRequest request) {
        int id = parseEntero(request.getParameter("id"));
        if (id <= 0) {
            enviarMensaje(request, "❌ El ID del producto es inválido.", "error");
            return;
        }

        if (productoDAO.eliminar(id)) {
            enviarMensaje(request, "✅ Producto eliminado correctamente.", "exito");
        } else {
            enviarMensaje(request, "❌ No se encontró el producto a eliminar.", "error");
        }
    }

    private Producto construirProductoDesdeRequest(HttpServletRequest request, int idActual) {
        String nombre = limpiar(request.getParameter("nombre"));
        String categoria = limpiar(request.getParameter("categoria"));
        double precio = parseDecimal(request.getParameter("precio"));
        int stock = parseInt(request.getParameter("stock"), -1);

        if (nombre.isEmpty() || categoria.isEmpty()) {
            enviarMensaje(request, "❌ Nombre y categoría son obligatorios.", "error");
            return null;
        }

        if (!CATEGORIAS_VALIDAS.contains(categoria)) {
            enviarMensaje(request, "❌ La categoría del producto no es válida.", "error");
            return null;
        }

        if (precio <= 0 || stock < 0) {
            enviarMensaje(request, "❌ El precio debe ser mayor a cero y el stock no puede ser negativo.", "error");
            return null;
        }

        Producto producto = new Producto(idActual, nombre, categoria, precio, stock);
        producto.setCategoria(categoria);
        producto.setDescripcion(categoria);
        producto.setEstado(stock > 0 ? "disponible" : "agotado");
        return producto;
    }

    private void cargarVista(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Producto> productos = productoDAO.listarTodos();
        request.setAttribute("listaProductos", productos);
        request.getRequestDispatcher("/vistas/productos.jsp").forward(request, response);
    }
}
