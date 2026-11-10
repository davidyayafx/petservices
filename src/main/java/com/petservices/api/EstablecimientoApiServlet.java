package com.petservices.api;

import com.petservices.dao.EstablecimientoDAO;
import com.petservices.dao.IEstablecimientoDAO;
import com.petservices.modelo.Establecimiento;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.petservices.api.util.ApiUtil.*;

/**
 * Servicio REST: /api/establecimientos
 * ──────────────────────────────────────────────────────────────────────────
 * GET    /api/establecimientos       -> lista todos los establecimientos
 * GET    /api/establecimientos/{id}  -> obtiene un establecimiento por id
 * POST   /api/establecimientos       -> crea un establecimiento (body JSON)
 * PUT    /api/establecimientos/{id}  -> actualiza un establecimiento (body JSON)
 * DELETE /api/establecimientos/{id}  -> elimina un establecimiento
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
@WebServlet("/api/establecimientos/*")
public class EstablecimientoApiServlet extends HttpServlet {

    private final IEstablecimientoDAO establecimientoDAO = new EstablecimientoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id == -1) {
            enviarJson(response, HttpServletResponse.SC_OK, establecimientoDAO.listarTodos());
        } else {
            Establecimiento e = establecimientoDAO.buscarPorId(id);
            if (e == null) {
                enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Establecimiento no encontrado con id " + id);
            } else {
                enviarJson(response, HttpServletResponse.SC_OK, e);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        Establecimiento nuevo = leerJson(request, Establecimiento.class);
        if (nuevo == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }
        if (nuevo.getNombre() == null || nuevo.getNombre().trim().isEmpty()
                || nuevo.getTipo() == null || nuevo.getTipo().trim().isEmpty()) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Los campos 'nombre' y 'tipo' son obligatorios");
            return;
        }
        if (!establecimientoDAO.insertar(nuevo)) {
            enviarError(response, HttpServletResponse.SC_CONFLICT,
                    "Ya existe un establecimiento con ese nombre");
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
        if (id == -1) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el id en la URL: /api/establecimientos/{id}");
            return;
        }
        Establecimiento existente = establecimientoDAO.buscarPorId(id);
        if (existente == null) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Establecimiento no encontrado con id " + id);
            return;
        }
        request.setCharacterEncoding("UTF-8");
        Establecimiento datos = leerJson(request, Establecimiento.class);
        if (datos == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }
        if (datos.getNombre() != null) existente.setNombre(datos.getNombre());
        if (datos.getTipo() != null) existente.setTipo(datos.getTipo());
        if (datos.getDireccion() != null) existente.setDireccion(datos.getDireccion());
        if (datos.getTelefono() != null) existente.setTelefono(datos.getTelefono());
        if (existente.getNombre() == null || existente.getNombre().trim().isEmpty()
                || existente.getTipo() == null || existente.getTipo().trim().isEmpty()) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Los campos 'nombre' y 'tipo' no pueden quedar vacíos");
            return;
        }
        if (!establecimientoDAO.actualizar(existente)) {
            enviarError(response, HttpServletResponse.SC_CONFLICT,
                    "No se pudo actualizar: el nombre ya existe o los datos no son válidos");
            return;
        }
        enviarJson(response, HttpServletResponse.SC_OK, existente);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id == -1) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el id en la URL: /api/establecimientos/{id}");
            return;
        }
        boolean eliminado = establecimientoDAO.eliminar(id);
        if (eliminado) {
            enviarJson(response, HttpServletResponse.SC_OK, new MensajeDTO("Establecimiento eliminado correctamente"));
        } else {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Establecimiento no encontrado con id " + id);
        }
    }
}
