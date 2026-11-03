package com.petservices.controller;

import com.petservices.dao.CitaDAO;
import com.petservices.dao.ClienteDAO;
import com.petservices.dao.IClienteDAO;
import com.petservices.dao.ICitaDAO;
import com.petservices.dao.IMascotaDAO;
import com.petservices.dao.MascotaDAO;
import com.petservices.modelo.Cita;
import com.petservices.modelo.Cliente;
import com.petservices.modelo.Mascota;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/citas")
public class CitaServlet extends BaseModuleServlet {

    private static final Set<String> SERVICIOS_VALIDOS = new HashSet<>(
            Arrays.asList("Consulta", "Vacunación", "Baño/Peluquería", "Cirugía"));
    private static final Set<String> ESTADOS_VALIDOS = new HashSet<>(
            Arrays.asList("Pendiente", "Confirmada", "Cancelada"));

    private final ICitaDAO citaDAO = new CitaDAO();
    private final IClienteDAO clienteDAO = new ClienteDAO();
    private final IMascotaDAO mascotaDAO = new MascotaDAO();

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
                enviarMensaje(request, "❌ El identificador de la cita es inválido.", "error");
            } else {
                Cita cita = citaDAO.buscarPorId(id);
                if (cita != null) {
                    request.setAttribute("citaEditar", cita);
                } else {
                    enviarMensaje(request, "❌ La cita seleccionada no existe.", "error");
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
            eliminarCita(request);
        } else if ("actualizar".equalsIgnoreCase(accion)) {
            actualizarCita(request);
        } else {
            guardarCita(request);
        }

        cargarVista(request, response);
    }

    private void guardarCita(HttpServletRequest request) {
        Cita cita = construirCitaDesdeRequest(request, 0);
        if (cita == null) {
            return;
        }

        if (citaDAO.insertar(cita)) {
            enviarMensaje(request, "✅ Cita registrada correctamente.", "exito");
        } else {
            enviarMensaje(request, "❌ No fue posible registrar la cita.", "error");
        }
    }

    private void actualizarCita(HttpServletRequest request) {
        int id = parseEntero(request.getParameter("id"));
        Cita existente = citaDAO.buscarPorId(id);
        if (existente == null) {
            enviarMensaje(request, "❌ La cita que intentas editar no existe.", "error");
            return;
        }

        Cita actualizada = construirCitaDesdeRequest(request, id);
        if (actualizada == null) {
            request.setAttribute("citaEditar", existente);
            return;
        }

        actualizada.setIdCita(id);
        if (citaDAO.actualizar(actualizada)) {
            enviarMensaje(request, "✅ Cita actualizada correctamente.", "exito");
        } else {
            enviarMensaje(request, "❌ No se pudo actualizar la cita.", "error");
            request.setAttribute("citaEditar", existente);
        }
    }

    private void eliminarCita(HttpServletRequest request) {
        int id = parseEntero(request.getParameter("id"));
        if (id <= 0) {
            enviarMensaje(request, "❌ El ID de la cita es inválido.", "error");
            return;
        }

        if (citaDAO.eliminar(id)) {
            enviarMensaje(request, "✅ Cita eliminada correctamente.", "exito");
        } else {
            enviarMensaje(request, "❌ No se encontró la cita a eliminar.", "error");
        }
    }

    private Cita construirCitaDesdeRequest(HttpServletRequest request, int idActual) {
        int idCliente = parseEntero(request.getParameter("idCliente"));
        int idMascota = parseEntero(request.getParameter("idMascota"));
        String fechaHora = limpiar(request.getParameter("fechaHora"));
        String servicio = limpiar(request.getParameter("servicio"));
        String estado = limpiar(request.getParameter("estado"));

        if (idCliente <= 0 || idMascota <= 0 || fechaHora.isEmpty() || servicio.isEmpty() || estado.isEmpty()) {
            enviarMensaje(request, "❌ Todos los campos de la cita son obligatorios.", "error");
            return null;
        }

        if (!SERVICIOS_VALIDOS.contains(servicio)) {
            enviarMensaje(request, "❌ El servicio seleccionado no es válido.", "error");
            return null;
        }

        if (!ESTADOS_VALIDOS.contains(estado)) {
            enviarMensaje(request, "❌ El estado de la cita no es válido.", "error");
            return null;
        }

        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        Mascota mascota = mascotaDAO.buscarPorId(idMascota);

        if (cliente == null) {
            enviarMensaje(request, "❌ Debes seleccionar un cliente válido.", "error");
            return null;
        }

        if (mascota == null) {
            enviarMensaje(request, "❌ Debes seleccionar una mascota válida.", "error");
            return null;
        }

        if (mascota.getIdCliente() != idCliente) {
            enviarMensaje(request, "❌ La mascota seleccionada no pertenece al cliente elegido.", "error");
            return null;
        }

        Cita cita = new Cita(idActual, idCliente, cliente.getNombre(), idMascota,
                mascota.getNombre(), fechaHora, servicio, estado);
        cita.setIdCita(idActual);
        return cita;
    }

    private void cargarVista(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Cita> citas = citaDAO.listarTodos();
        List<Cliente> clientes = clienteDAO.listarTodos();
        List<Mascota> mascotas = mascotaDAO.listarTodos();
        request.setAttribute("listaCitas", citas);
        request.setAttribute("listaClientes", clientes);
        request.setAttribute("listaMascotas", mascotas);
        request.getRequestDispatcher("/vistas/citas.jsp").forward(request, response);
    }
}
