package com.petservices.api;

import com.petservices.dao.ClienteDAO;
import com.petservices.dao.IClienteDAO;
import com.petservices.dao.IMascotaDAO;
import com.petservices.dao.MascotaDAO;
import com.petservices.modelo.Cliente;
import com.petservices.modelo.Mascota;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.petservices.api.util.ApiUtil.*;

/**
 * Servicio REST: /api/mascotas
 * ──────────────────────────────────────────────────────────────────────────
 * GET    /api/mascotas               -> lista todas las mascotas
 * GET    /api/mascotas/{id}          -> obtiene una mascota por id
 * GET    /api/mascotas?idCliente=1   -> lista las mascotas de un cliente
 * POST   /api/mascotas               -> crea una mascota (body JSON)
 * PUT    /api/mascotas/{id}          -> actualiza una mascota (body JSON)
 * DELETE /api/mascotas/{id}          -> elimina una mascota
 *
 * El PUT es parcial: actualiza nombre, especie, raza, edad, dueño y servicio
 * solo cuando vienen en el cuerpo. Si se cambia el dueño, se valida que exista
 * y se recalcula 'nombreCliente'.
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
@WebServlet("/api/mascotas/*")
public class MascotaApiServlet extends HttpServlet {

    private final IMascotaDAO mascotaDAO = new MascotaDAO();
    private final IClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id != ID_AUSENTE) {
            Mascota mascota = mascotaDAO.buscarPorId(id);
            if (mascota == null) {
                enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Mascota no encontrada con id " + id);
            } else {
                enviarJson(response, HttpServletResponse.SC_OK, mascota);
            }
            return;
        }
        String idClienteParam = request.getParameter("idCliente");
        if (idClienteParam == null) {
            enviarJson(response, HttpServletResponse.SC_OK, mascotaDAO.listarTodos());
            return;
        }
        int idCliente;
        try {
            idCliente = Integer.parseInt(idClienteParam.trim());
        } catch (NumberFormatException e) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "El parámetro 'idCliente' debe ser un número entero");
            return;
        }
        List<Mascota> lista = mascotaDAO.buscarPorCliente(idCliente);
        enviarJson(response, HttpServletResponse.SC_OK, lista);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        MascotaEntrada datos = leerJson(request, MascotaEntrada.class);
        if (datos == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }
        if (datos.nombre == null || datos.nombre.trim().isEmpty()
                || datos.raza == null || datos.raza.trim().isEmpty()) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Los campos 'nombre' y 'raza' son obligatorios");
            return;
        }

        Mascota nueva = new Mascota();
        if (!aplicarDueno(datos, nueva, response)) {
            return;
        }
        datos.volcarEn(nueva);
        if (!mascotaDAO.insertar(nueva)) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "No se pudo registrar la mascota: el dueño o el servicio indicado no existe");
            return;
        }
        enviarJson(response, HttpServletResponse.SC_CREATED, nueva);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id == ID_AUSENTE) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el id de la mascota en la URL: /api/mascotas/{id}");
            return;
        }
        Mascota existente = mascotaDAO.buscarPorId(id);
        if (existente == null) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Mascota no encontrada con id " + id);
            return;
        }
        request.setCharacterEncoding("UTF-8");
        MascotaEntrada datos = leerJson(request, MascotaEntrada.class);
        if (datos == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }
        if (!aplicarDueno(datos, existente, response)) {
            return;
        }
        datos.volcarEn(existente);
        if (!mascotaDAO.actualizar(existente)) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "No se pudo actualizar la mascota: el dueño o el servicio indicado no existe");
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
        if (id == ID_AUSENTE) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el id de la mascota en la URL: /api/mascotas/{id}");
            return;
        }
        if (mascotaDAO.tieneCitas(id)) {
            enviarError(response, HttpServletResponse.SC_CONFLICT,
                    "No se puede eliminar la mascota: tiene citas asociadas");
            return;
        }
        boolean eliminado = mascotaDAO.eliminar(id);
        if (eliminado) {
            enviarJson(response, HttpServletResponse.SC_OK, new MensajeDTO("Mascota eliminada correctamente"));
        } else {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Mascota no encontrada con id " + id);
        }
    }

    /**
     * Resuelve y valida el dueño cuando viene en el cuerpo.
     *
     * @return {@code false} si ya se respondió con un error (cliente inexistente).
     */
    private boolean aplicarDueno(MascotaEntrada datos, Mascota mascota,
                                 HttpServletResponse response) throws IOException {
        if (datos.idCliente == null) {
            return true;
        }
        if (datos.idCliente <= 0) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "El campo 'idCliente' debe ser mayor que 0");
            return false;
        }
        Cliente cliente = clienteDAO.buscarPorId(datos.idCliente);
        if (cliente == null) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND,
                    "El cliente con id " + datos.idCliente + " no existe");
            return false;
        }
        mascota.setIdCliente(cliente.getIdCliente());
        mascota.setNombreCliente(cliente.getNombre());
        return true;
    }

    /**
     * Cuerpo de entrada de POST/PUT. Usa tipos envoltorio para distinguir
     * "campo no enviado" (null) de un valor real.
     */
    static class MascotaEntrada {
        String nombre;
        String especie;
        String raza;
        Integer edad;
        Integer idCliente;
        String nombreCliente;
        Integer idServicio;

        void volcarEn(Mascota mascota) {
            if (nombre != null) mascota.setNombre(nombre.trim());
            if (especie != null) mascota.setEspecie(especie.trim());
            if (raza != null) mascota.setRaza(raza.trim());
            if (edad != null) mascota.setEdad(edad);
            if (idCliente != null) mascota.setIdCliente(idCliente);
            if (nombreCliente != null && mascota.getNombreCliente() == null) {
                mascota.setNombreCliente(nombreCliente.trim());
            }
            if (idServicio != null) mascota.setIdServicio(idServicio);
        }
    }
}
