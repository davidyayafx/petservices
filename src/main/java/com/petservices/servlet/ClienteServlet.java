package com.petservices.servlet;

import com.petservices.dao.ClienteDAO;
import com.petservices.modelo.Cliente;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Servlet: ClienteServlet
 * URL de mapeo: /clientes
 * ──────────────────────────────────────────────────────────────────────────
 * Controlador principal del módulo de GESTIÓN DE CLIENTES (CRUD).
 * Usa el parámetro "accion" para determinar la operación a ejecutar.
 *
 *  doGet  → listar, editar (cargar formulario), eliminar
 *  doPost → crear, actualizar
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA2-EV02
 */
@WebServlet("/clientes")
public class ClienteServlet extends HttpServlet {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    /** Convierte el parámetro id a entero sin lanzar excepciones (0 si no es válido). */
    private int parseId(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // doGet: Operaciones de lectura y navegación
    // ──────────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Verificar que el usuario tenga sesión activa (protección básica)
        if (!tieneSesion(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Leer el parámetro "accion" de la URL (?accion=listar, ?accion=editar, etc.)
        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar"; // valor por defecto

        switch (accion) {

            case "editar":
                // Cargar los datos del cliente seleccionado en el formulario de edición
                int idEditar = parseId(request.getParameter("id"));
                Cliente clienteEditar = clienteDAO.buscarPorId(idEditar);
                if (clienteEditar != null) {
                    request.setAttribute("clienteEditar", clienteEditar);
                }
                // Siempre cargamos la lista para mostrar la tabla debajo del formulario
                cargarListaYForwardear(request, response);
                break;

            case "eliminar":
                // Eliminar el cliente con el id especificado
                int idEliminar = parseId(request.getParameter("id"));
                boolean eliminado = clienteDAO.eliminar(idEliminar);
                if (eliminado) {
                    request.setAttribute("mensaje", "✅ Cliente eliminado correctamente.");
                    request.setAttribute("tipoMensaje", "exito");
                } else {
                    request.setAttribute("mensaje", "❌ No se encontró el cliente.");
                    request.setAttribute("tipoMensaje", "error");
                }
                cargarListaYForwardear(request, response);
                break;

            case "listar":
            default:
                cargarListaYForwardear(request, response);
                break;
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // doPost: Operaciones de escritura (crear / actualizar)
    // ──────────────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // Verificar sesión
        if (!tieneSesion(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("crear".equals(accion)) {
            // ── CREAR nuevo cliente ──────────────────────────────────────
            String nombre    = request.getParameter("nombre");
            String correo    = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");

            Cliente nuevo = new Cliente(
                    nombre.trim(),
                    correo.trim().toLowerCase(),
                    contrasena
            );

            boolean creado = clienteDAO.insertar(nuevo);
            if (creado) {
                request.setAttribute("mensaje", "✅ Cliente '" + nombre + "' creado.");
                request.setAttribute("tipoMensaje", "exito");
            } else {
                request.setAttribute("mensaje",
                        "❌ El correo '" + correo + "' ya está registrado.");
                request.setAttribute("tipoMensaje", "error");
            }

        } else if ("actualizar".equals(accion)) {
            // ── ACTUALIZAR cliente existente ─────────────────────────────
            int    id        = parseId(request.getParameter("id"));
            String nombre    = request.getParameter("nombre");
            String correo    = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");

            // Recuperar datos actuales para no pisar la contraseña si viene vacía
            Cliente existente = clienteDAO.buscarPorId(id);
            if (existente != null) {
                String correoNormalizado = correo.trim().toLowerCase();
                // Comprobar el duplicado ANTES de mutar: 'existente' es la instancia del DAO.
                Cliente conEseCorreo = clienteDAO.buscarPorCorreo(correoNormalizado);
                if (conEseCorreo != null && conEseCorreo.getIdCliente() != id) {
                    request.setAttribute("mensaje",
                            "❌ El correo '" + correo + "' ya está registrado por otro cliente.");
                    request.setAttribute("tipoMensaje", "error");
                } else {
                    existente.setNombre(nombre.trim());
                    existente.setCorreo(correoNormalizado);
                    // Solo actualizar contraseña si el usuario ingresó una nueva
                    if (contrasena != null && !contrasena.trim().isEmpty()) {
                        existente.setContrasena(contrasena);
                    }
                    if (clienteDAO.actualizar(existente)) {
                        request.setAttribute("mensaje", "✅ Cliente actualizado correctamente.");
                        request.setAttribute("tipoMensaje", "exito");
                    } else {
                        request.setAttribute("mensaje",
                                "❌ No se pudo actualizar: el correo ya está registrado.");
                        request.setAttribute("tipoMensaje", "error");
                    }
                }
            } else {
                request.setAttribute("mensaje", "❌ Cliente no encontrado.");
                request.setAttribute("tipoMensaje", "error");
            }
        }

        // Tras crear/actualizar, siempre mostrar la lista actualizada
        cargarListaYForwardear(request, response);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Método auxiliar: carga la lista y hace forward al JSP de gestión
    // ──────────────────────────────────────────────────────────────────────
    private void cargarListaYForwardear(HttpServletRequest request,
                                        HttpServletResponse response)
            throws ServletException, IOException {
        List<Cliente> lista = clienteDAO.listarTodos();
        // Guardamos la lista como atributo del request para que el JSP la use
        request.setAttribute("listaClientes", lista);
        request.getRequestDispatcher("/vistas/clientes.jsp")
               .forward(request, response);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Verificar si existe una sesión de usuario activa
    // ──────────────────────────────────────────────────────────────────────
    private boolean tieneSesion(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false); // false = no crear nueva
        return sesion != null && sesion.getAttribute("clienteSesion") != null;
    }
}
