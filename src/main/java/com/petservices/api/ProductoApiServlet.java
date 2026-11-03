package com.petservices.api;

import com.petservices.dao.IProductoDAO;
import com.petservices.dao.ProductoDAO;
import com.petservices.modelo.Producto;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.petservices.api.util.ApiUtil.*;

/**
 * Servicio REST: /api/productos
 * ──────────────────────────────────────────────────────────────────────────
 * GET    /api/productos                 -> lista todos los productos
 * GET    /api/productos/{id}            -> obtiene un producto por id
 * GET    /api/productos?stockMenorA=20  -> filtra productos con stock bajo
 * POST   /api/productos                 -> crea un producto (body JSON)
 * PUT    /api/productos/{id}            -> actualiza un producto (body JSON)
 * DELETE /api/productos/{id}            -> elimina un producto
 *
 * El PUT es parcial: los campos que no se envían conservan su valor
 * (antes, omitir 'stock' lo dejaba en 0).
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
@WebServlet("/api/productos/*")
public class ProductoApiServlet extends HttpServlet {

    private final IProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id != ID_AUSENTE) {
            Producto p = productoDAO.buscarPorId(id);
            if (p == null) {
                enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Producto no encontrado con id " + id);
            } else {
                enviarJson(response, HttpServletResponse.SC_OK, p);
            }
            return;
        }
        String stockMenorA = request.getParameter("stockMenorA");
        List<Producto> lista = productoDAO.listarTodos();
        if (stockMenorA != null) {
            int limite;
            try {
                limite = Integer.parseInt(stockMenorA.trim());
            } catch (NumberFormatException e) {
                enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "El parámetro 'stockMenorA' debe ser un número entero");
                return;
            }
            lista.removeIf(p -> p.getStock() >= limite);
        }
        enviarJson(response, HttpServletResponse.SC_OK, lista);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        ProductoEntrada datos = leerJson(request, ProductoEntrada.class);
        if (datos == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }
        if (datos.nombre == null || datos.nombre.trim().isEmpty()) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "El campo 'nombre' es obligatorio");
            return;
        }

        Producto nuevo = new Producto();
        datos.volcarEn(nuevo);
        productoDAO.insertar(nuevo);
        enviarJson(response, HttpServletResponse.SC_CREATED, nuevo);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id == ID_AUSENTE) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el id en la URL: /api/productos/{id}");
            return;
        }
        Producto existente = productoDAO.buscarPorId(id);
        if (existente == null) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Producto no encontrado con id " + id);
            return;
        }
        request.setCharacterEncoding("UTF-8");
        ProductoEntrada datos = leerJson(request, ProductoEntrada.class);
        if (datos == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }

        datos.volcarEn(existente);
        productoDAO.actualizar(existente);
        enviarJson(response, HttpServletResponse.SC_OK, existente);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id == ID_AUSENTE) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el id en la URL: /api/productos/{id}");
            return;
        }
        boolean eliminado = productoDAO.eliminar(id);
        if (eliminado) {
            enviarJson(response, HttpServletResponse.SC_OK, new MensajeDTO("Producto eliminado correctamente"));
        } else {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Producto no encontrado con id " + id);
        }
    }

    /**
     * Cuerpo de entrada de POST/PUT. Usa tipos envoltorio para distinguir
     * "campo no enviado" (null) de un valor real, incluido 0.
     */
    static class ProductoEntrada {
        String nombre;
        String categoria;
        String descripcion;
        Double precio;
        Integer stock;
        String estado;
        Integer idEstablecimiento;
        Integer idCliente;

        void volcarEn(Producto producto) {
            if (nombre != null) producto.setNombre(nombre.trim());
            if (categoria != null) producto.setCategoria(categoria.trim());
            if (descripcion != null) producto.setDescripcion(descripcion.trim());
            if (precio != null) producto.setPrecio(precio);
            if (stock != null) producto.setStock(stock);
            if (estado != null) producto.setEstado(estado.trim());
            if (idEstablecimiento != null) producto.setIdEstablecimiento(idEstablecimiento);
            if (idCliente != null) producto.setIdCliente(idCliente);
        }
    }
}
