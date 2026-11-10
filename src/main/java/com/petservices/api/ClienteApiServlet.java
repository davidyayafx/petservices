package com.petservices.api;

import com.petservices.dao.ClienteDAO;
import com.petservices.modelo.Cliente;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static com.petservices.api.util.ApiUtil.*;

/**
 * Servicio REST: /api/clientes
 * ──────────────────────────────────────────────────────────────────────────
 * GET    /api/clientes        -> lista todos los clientes
 * GET    /api/clientes/{id}   -> obtiene un cliente por id
 * POST   /api/clientes        -> crea un cliente (body JSON)
 * PUT    /api/clientes/{id}   -> actualiza un cliente (body JSON)
 * DELETE /api/clientes/{id}   -> elimina un cliente
 *
 * Reglas: 'nombre' y 'correo' son obligatorios; el correo debe tener formato
 * válido y se guarda en minúsculas. Un correo duplicado responde 409.
 * Las respuestas de este recurso nunca incluyen el campo "contrasena".
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
@WebServlet("/api/clientes/*")
public class ClienteApiServlet extends HttpServlet {

    /** Validación de formato mínima: usuario@dominio.tld */
    private static final Pattern CORREO_VALIDO =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id == ID_AUSENTE) {
            List<Cliente> lista = clienteDAO.listarTodos();
            enviarJsonSinContrasena(response, HttpServletResponse.SC_OK, lista);
        } else {
            Cliente cliente = clienteDAO.buscarPorId(id);
            if (cliente == null) {
                enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Cliente no encontrado con id " + id);
            } else {
                enviarJsonSinContrasena(response, HttpServletResponse.SC_OK, cliente);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        Cliente nuevo = leerJson(request, Cliente.class);
        if (nuevo == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }
        if (nuevo.getNombre() == null || nuevo.getNombre().trim().isEmpty()
                || nuevo.getCorreo() == null || nuevo.getCorreo().trim().isEmpty()) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Los campos 'nombre' y 'correo' son obligatorios");
            return;
        }
        if (!correoValido(nuevo.getCorreo())) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "El campo 'correo' no tiene un formato válido (usuario@dominio.tld)");
            return;
        }
        nuevo.setNombre(nuevo.getNombre().trim());
        nuevo.setCorreo(nuevo.getCorreo().trim().toLowerCase());

        boolean creado = clienteDAO.insertar(nuevo);
        if (creado) {
            enviarJsonSinContrasena(response, HttpServletResponse.SC_CREATED, nuevo);
        } else {
            enviarError(response, HttpServletResponse.SC_CONFLICT, "El correo '" + nuevo.getCorreo() + "' ya está registrado");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id == ID_AUSENTE) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el id del cliente en la URL: /api/clientes/{id}");
            return;
        }
        Cliente existente = clienteDAO.buscarPorId(id);
        if (existente == null) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Cliente no encontrado con id " + id);
            return;
        }
        request.setCharacterEncoding("UTF-8");
        Cliente datos = leerJson(request, Cliente.class);
        if (datos == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }
        if (datos.getNombre() != null && !datos.getNombre().trim().isEmpty()) {
            existente.setNombre(datos.getNombre().trim());
        }
        if (datos.getCorreo() != null && !datos.getCorreo().trim().isEmpty()) {
            if (!correoValido(datos.getCorreo())) {
                enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "El campo 'correo' no tiene un formato válido (usuario@dominio.tld)");
                return;
            }
            String correoNuevo = datos.getCorreo().trim().toLowerCase();
            // Se comprueba antes de mutar: 'existente' es la instancia guardada en el DAO.
            Cliente conEseCorreo = clienteDAO.buscarPorCorreo(correoNuevo);
            if (conEseCorreo != null && conEseCorreo.getIdCliente() != existente.getIdCliente()) {
                enviarError(response, HttpServletResponse.SC_CONFLICT,
                        "El correo '" + correoNuevo + "' ya está registrado por otro cliente");
                return;
            }
            existente.setCorreo(correoNuevo);
        }
        if (datos.getContrasena() != null && !datos.getContrasena().isEmpty()) {
            existente.setContrasena(datos.getContrasena());
        }

        if (!clienteDAO.actualizar(existente)) {
            enviarError(response, HttpServletResponse.SC_CONFLICT,
                    "El correo '" + existente.getCorreo() + "' ya está registrado por otro cliente");
            return;
        }
        enviarJsonSinContrasena(response, HttpServletResponse.SC_OK, existente);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id == ID_AUSENTE) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el id del cliente en la URL: /api/clientes/{id}");
            return;
        }
        if (clienteDAO.tieneDependencias(id)) {
            enviarError(response, HttpServletResponse.SC_CONFLICT,
                    "No se puede eliminar el cliente: tiene mascotas o citas asociadas");
            return;
        }
        boolean eliminado = clienteDAO.eliminar(id);
        if (eliminado) {
            enviarJson(response, HttpServletResponse.SC_OK, new MensajeDTO("Cliente eliminado correctamente"));
        } else {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Cliente no encontrado con id " + id);
        }
    }

    private static boolean correoValido(String correo) {
        return correo != null && CORREO_VALIDO.matcher(correo.trim()).matches();
    }
}
