package com.petservices.api;

import com.petservices.dao.CitaDAO;
import com.petservices.dao.ClienteDAO;
import com.petservices.dao.IClienteDAO;
import com.petservices.dao.IMascotaDAO;
import com.petservices.dao.IServicioDAO;
import com.petservices.dao.MascotaDAO;
import com.petservices.dao.ServicioDAO;
import com.petservices.modelo.Cita;
import com.petservices.modelo.Cliente;
import com.petservices.modelo.Mascota;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.petservices.api.util.ApiUtil.*;

/**
 * Servicio REST: /api/citas
 * ──────────────────────────────────────────────────────────────────────────
 * GET    /api/citas              -> lista todas las citas
 * GET    /api/citas/{id}         -> obtiene una cita por id
 * GET    /api/citas?estado=X     -> filtra citas por estado (sin distinguir mayúsculas)
 * POST   /api/citas              -> agenda una cita (body JSON)
 * PUT    /api/citas/{id}         -> actualiza una cita (fecha/hora/estado)
 * DELETE /api/citas/{id}         -> elimina definitivamente una cita
 *
 * Reglas:
 *  - 'estado' admite pendiente, confirmada, cancelada y completada sin distinguir
 *    mayúsculas; se guarda en su forma canónica.
 *  - El cuerpo acepta "fechaHora" o el par "fecha" + "hora".
 *  - 'idMascota' es obligatorio al crear; la mascota debe existir. Si se envía
 *    'idCliente', debe existir y ser el dueño de la mascota. Si no se envía,
 *    se deduce del dueño de la mascota.
 *  - Los nombres de cliente y mascota se rellenan desde los DAO (no se confían
 *    al cuerpo de la petición).
 *  - El PUT es parcial: lo que no se envía conserva su valor.
 *  - DELETE elimina la cita; para cancelarla sin borrarla use
 *    PUT /api/citas/{id} con {"estado":"cancelada"}.
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
@WebServlet("/api/citas/*")
public class CitaApiServlet extends HttpServlet {

    /** Valor canónico que se guarda para cada estado aceptado. */
    private static final Map<String, String> ESTADOS_CANONICOS = new LinkedHashMap<>();

    static {
        ESTADOS_CANONICOS.put("pendiente",  "Pendiente");
        ESTADOS_CANONICOS.put("confirmada", "Confirmada");
        ESTADOS_CANONICOS.put("cancelada",  "Cancelada");
        ESTADOS_CANONICOS.put("completada", "Completada");
    }

    private static final List<String> ESTADOS_VALIDOS =
            Arrays.asList("pendiente", "confirmada", "cancelada", "completada");

    private final CitaDAO citaDAO = new CitaDAO();
    private final IServicioDAO servicioDAO = new ServicioDAO();
    private final IMascotaDAO mascotaDAO = new MascotaDAO();
    private final IClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = extraerId(request);
        if (!idRutaValido(response, id)) {
            return;
        }
        if (id != ID_AUSENTE) {
            Cita cita = citaDAO.buscarPorId(id);
            if (cita == null) {
                enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Cita no encontrada con id " + id);
            } else {
                enviarJson(response, HttpServletResponse.SC_OK, cita);
            }
            return;
        }
        String estado = request.getParameter("estado");
        List<Cita> lista = citaDAO.listarTodos();
        if (estado != null) {
            final String buscado = estado.trim();
            lista.removeIf(c -> {
                String canonico = normalizarEstado(c.getEstado());
                return canonico == null || !canonico.equalsIgnoreCase(buscado);
            });
        }
        enviarJson(response, HttpServletResponse.SC_OK, lista);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        CitaEntrada datos = leerJson(request, CitaEntrada.class);
        if (datos == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }

        String fechaHora = datos.fechaHoraEfectiva();
        if (fechaHora == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Los campos 'fechaHora' (o 'fecha' y 'hora') son obligatorios");
            return;
        }

        String estado = "Pendiente";
        if (datos.estado != null && !datos.estado.trim().isEmpty()) {
            estado = normalizarEstado(datos.estado);
            if (estado == null) {
                enviarError(response, HttpServletResponse.SC_BAD_REQUEST, mensajeEstadoInvalido());
                return;
            }
        }

        if (datos.idServicio != null && datos.idServicio > 0
                && servicioDAO.buscarPorId(datos.idServicio) == null) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND,
                    "El servicio con id " + datos.idServicio + " no existe");
            return;
        }

        Cita nueva = new Cita();
        nueva.setFechaHora(fechaHora);
        nueva.setEstado(estado);
        datos.volcarEn(nueva);

        if (!resolverRelaciones(datos, nueva, response, true)) {
            return;
        }
        if (!citaDAO.insertar(nueva)) {
            enviarError(response, HttpServletResponse.SC_CONFLICT,
                    "No se pudo registrar la cita: ya existe una cita activa para esa mascota "
                            + "en el mismo horario, o los datos relacionados no son válidos");
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
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el id en la URL: /api/citas/{id}");
            return;
        }
        Cita existente = citaDAO.buscarPorId(id);
        if (existente == null) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Cita no encontrada con id " + id);
            return;
        }
        request.setCharacterEncoding("UTF-8");
        CitaEntrada datos = leerJson(request, CitaEntrada.class);
        if (datos == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }

        // Fecha/hora: solo se tocan si vienen en el cuerpo; la parte no enviada se conserva.
        if (datos.fechaHora != null && !datos.fechaHora.trim().isEmpty()) {
            existente.setFechaHora(datos.fechaHora.trim());
        } else if (datos.fecha != null && !datos.fecha.trim().isEmpty()) {
            String hora = (datos.hora == null || datos.hora.trim().isEmpty())
                    ? existente.getHora()
                    : datos.hora.trim();
            existente.setFechaHora(datos.fecha.trim() + "T" + (hora.isEmpty() ? "00:00" : hora));
        } else if (datos.hora != null && !datos.hora.trim().isEmpty()) {
            existente.setHora(datos.hora.trim());
        }

        if (datos.estado != null && !datos.estado.trim().isEmpty()) {
            String estado = normalizarEstado(datos.estado);
            if (estado == null) {
                enviarError(response, HttpServletResponse.SC_BAD_REQUEST, mensajeEstadoInvalido());
                return;
            }
            existente.setEstado(estado);
        }

        if (datos.idServicio != null && datos.idServicio > 0
                && servicioDAO.buscarPorId(datos.idServicio) == null) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND,
                    "El servicio con id " + datos.idServicio + " no existe");
            return;
        }

        if (!resolverRelaciones(datos, existente, response, false)) {
            return;
        }
        if (!citaDAO.actualizar(existente)) {
            enviarError(response, HttpServletResponse.SC_CONFLICT,
                    "No se pudo actualizar la cita: ya existe otra cita activa para esa mascota "
                            + "en el mismo horario, o los datos relacionados no son válidos");
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
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el id en la URL: /api/citas/{id}");
            return;
        }
        boolean eliminada = citaDAO.eliminar(id);
        if (eliminada) {
            enviarJson(response, HttpServletResponse.SC_OK, new MensajeDTO("Cita eliminada correctamente"));
        } else {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND, "Cita no encontrada con id " + id);
        }
    }

    /**
     * Valida mascota/cliente y rellena id y nombres denormalizados.
     *
     * @return {@code false} si ya se respondió con un error.
     */
    private boolean resolverRelaciones(CitaEntrada datos, Cita cita,
                                       HttpServletResponse response, boolean esNueva) throws IOException {
        Integer idMascota = datos.idMascota;
        Integer idCliente = datos.idCliente;

        if (esNueva && (idMascota == null || idMascota <= 0)) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "El campo 'idMascota' es obligatorio y debe ser mayor que 0");
            return false;
        }
        if (idMascota != null && idMascota <= 0) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "El campo 'idMascota' debe ser mayor que 0");
            return false;
        }

        Mascota mascota = null;
        if (idMascota != null) {
            mascota = mascotaDAO.buscarPorId(idMascota);
            if (mascota == null) {
                enviarError(response, HttpServletResponse.SC_NOT_FOUND,
                        "La mascota con id " + idMascota + " no existe");
                return false;
            }
            cita.setIdMascota(mascota.getIdMascota());
            cita.setNombreMascota(mascota.getNombre());
            if (idCliente == null && mascota.getIdCliente() > 0) {
                idCliente = mascota.getIdCliente(); // se deduce del dueño de la mascota
            }
        } else if (!esNueva && cita.getIdMascota() > 0) {
            mascota = mascotaDAO.buscarPorId(cita.getIdMascota());
        }

        if (idCliente != null) {
            if (idCliente <= 0) {
                enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "El campo 'idCliente' debe ser mayor que 0");
                return false;
            }
            Cliente cliente = clienteDAO.buscarPorId(idCliente);
            if (cliente == null) {
                enviarError(response, HttpServletResponse.SC_NOT_FOUND,
                        "El cliente con id " + idCliente + " no existe");
                return false;
            }
            if (mascota != null && mascota.getIdCliente() != cliente.getIdCliente()) {
                enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "La mascota " + mascota.getIdMascota() + " no pertenece al cliente " + idCliente);
                return false;
            }
            cita.setIdCliente(cliente.getIdCliente());
            cita.setNombreCliente(cliente.getNombre());
        }
        return true;
    }

    /** Devuelve el valor canónico del estado, o null si no es válido. */
    private static String normalizarEstado(String estado) {
        if (estado == null) {
            return null;
        }
        return ESTADOS_CANONICOS.get(estado.trim().toLowerCase());
    }

    private static String mensajeEstadoInvalido() {
        return "Estado inválido. Valores permitidos: " + ESTADOS_VALIDOS
                + " (sin distinguir mayúsculas)";
    }

    /**
     * Cuerpo de entrada de POST/PUT. Acepta "fechaHora" o el par "fecha"+"hora"
     * y permite enviar solo los campos que se quieren modificar en un PUT.
     */
    static class CitaEntrada {
        String fechaHora;
        String fecha;
        String hora;
        String estado;
        Integer idCliente;
        Integer idMascota;
        String servicio;
        Integer idServicio;
        Integer idEstablecimiento;

        /** Fecha y hora combinadas tal como las envió el cliente, o null. */
        String fechaHoraEfectiva() {
            if (fechaHora != null && !fechaHora.trim().isEmpty()) {
                return fechaHora.trim();
            }
            if (fecha != null && !fecha.trim().isEmpty()) {
                String h = (hora == null || hora.trim().isEmpty()) ? "00:00" : hora.trim();
                return fecha.trim() + "T" + h;
            }
            if (hora != null && !hora.trim().isEmpty()) {
                return hora.trim();
            }
            return null;
        }

        /** Copia los campos simples (los id de cliente/mascota los resuelve el servlet). */
        void volcarEn(Cita cita) {
            if (servicio != null) cita.setServicio(servicio.trim());
            if (idServicio != null) cita.setIdServicio(idServicio);
            if (idEstablecimiento != null) cita.setIdEstablecimiento(idEstablecimiento);
            if (cita.getServicio() == null) {
                cita.setServicio("Consulta");
            }
        }
    }
}
