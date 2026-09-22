package com.petservices.controller;

import com.petservices.dao.ClienteDAO;
import com.petservices.dao.IClienteDAO;
import com.petservices.dao.IMascotaDAO;
import com.petservices.dao.MascotaDAO;
import com.petservices.modelo.Cliente;
import com.petservices.modelo.Mascota;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/mascotas")
public class MascotaServlet extends BaseModuleServlet {

    private final IMascotaDAO mascotaDAO = new MascotaDAO();
    private final IClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!validarSesion(request, response)) {
            return;
        }

        prepararCabecera(request);
        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            accion = "listar";
        }

        if ("editar".equalsIgnoreCase(accion)) {
            try {
                int id = parseEntero(request.getParameter("id"));
                Mascota mascota = mascotaDAO.buscarPorId(id);
                if (mascota != null) {
                    request.setAttribute("mascotaEditar", mascota);
                } else {
                    enviarMensaje(request, "❌ No se encontró la mascota seleccionada.", "error");
                }
            } catch (NumberFormatException ex) {
                enviarMensaje(request, "❌ El identificador de la mascota no es válido.", "error");
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

        try {
            if ("eliminar".equalsIgnoreCase(accion)) {
                eliminarMascota(request);
            } else if ("actualizar".equalsIgnoreCase(accion)) {
                actualizarMascota(request);
            } else {
                guardarMascota(request);
            }
        } catch (NumberFormatException ex) {
            enviarMensaje(request, "❌ Verifica la edad y el cliente seleccionado.", "error");
        }

        cargarVista(request, response);
    }

    private void guardarMascota(HttpServletRequest request) {
        String nombre = obtenerTexto(request.getParameter("nombre"));
        String especie = obtenerTexto(request.getParameter("especie"));
        String raza = obtenerTexto(request.getParameter("raza"));
        int edad = parseEntero(request.getParameter("edad"));
        int idCliente = parseEntero(request.getParameter("idCliente"));

        if (nombre.isEmpty() || especie.isEmpty() || raza.isEmpty() || edad < 0 || idCliente <= 0) {
            enviarMensaje(request, "❌ Todos los campos de la mascota son obligatorios.", "error");
            return;
        }

        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null) {
            enviarMensaje(request, "❌ Debes seleccionar un dueño válido.", "error");
            return;
        }

        Mascota mascota = new Mascota(nombre, especie, raza, edad, idCliente, cliente.getNombre());
        if (mascotaDAO.insertar(mascota)) {
            enviarMensaje(request, "✅ Mascota registrada correctamente.", "exito");
        } else {
            enviarMensaje(request, "❌ No fue posible registrar la mascota.", "error");
        }
    }

    private void actualizarMascota(HttpServletRequest request) {
        int id = parseEntero(request.getParameter("id"));
        Mascota existente = mascotaDAO.buscarPorId(id);
        if (existente == null) {
            enviarMensaje(request, "❌ La mascota que intentas actualizar no existe.", "error");
            return;
        }

        String nombre = obtenerTexto(request.getParameter("nombre"));
        String especie = obtenerTexto(request.getParameter("especie"));
        String raza = obtenerTexto(request.getParameter("raza"));
        int edad = parseEntero(request.getParameter("edad"));
        int idCliente = parseEntero(request.getParameter("idCliente"));

        if (nombre.isEmpty() || especie.isEmpty() || raza.isEmpty() || edad < 0 || idCliente <= 0) {
            enviarMensaje(request, "❌ Completa todos los datos antes de actualizar.", "error");
            request.setAttribute("mascotaEditar", existente);
            return;
        }

        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null) {
            enviarMensaje(request, "❌ El dueño seleccionado no existe.", "error");
            request.setAttribute("mascotaEditar", existente);
            return;
        }

        existente.setNombre(nombre);
        existente.setEspecie(especie);
        existente.setRaza(raza);
        existente.setEdad(edad);
        existente.setIdCliente(idCliente);
        existente.setNombreCliente(cliente.getNombre());

        if (mascotaDAO.actualizar(existente)) {
            enviarMensaje(request, "✅ Mascota actualizada correctamente.", "exito");
        } else {
            enviarMensaje(request, "❌ No se pudo actualizar la mascota.", "error");
            request.setAttribute("mascotaEditar", existente);
        }
    }

    private void eliminarMascota(HttpServletRequest request) {
        int id = parseEntero(request.getParameter("id"));
        if (id <= 0) {
            enviarMensaje(request, "❌ El ID de la mascota es inválido.", "error");
            return;
        }

        if (mascotaDAO.eliminar(id)) {
            enviarMensaje(request, "✅ Mascota eliminada correctamente.", "exito");
        } else {
            enviarMensaje(request, "❌ No se encontró la mascota a eliminar.", "error");
        }
    }

    private void cargarVista(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Mascota> mascotas = mascotaDAO.listarTodos();
        List<Cliente> clientes = clienteDAO.listarTodos();
        request.setAttribute("listaMascotas", mascotas);
        request.setAttribute("listaClientes", clientes);
        request.getRequestDispatcher("/vistas/mascotas.jsp").forward(request, response);
    }

    private String obtenerTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
