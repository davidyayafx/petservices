package com.petservices.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

abstract class BaseModuleServlet extends HttpServlet {

    protected boolean validarSesion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("clienteSesion") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String ruta)
            throws ServletException, IOException {
        request.getRequestDispatcher(ruta).forward(request, response);
    }

    protected String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    protected int parseInt(String valor, int defecto) {
        if (valor == null || valor.trim().isEmpty()) {
            return defecto;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return defecto;
        }
    }

    protected void prepararCabecera(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        String nombreUsuario = "Usuario";
        if (sesion != null) {
            Object usuario = sesion.getAttribute("usuario");
            Object nombreSesion = sesion.getAttribute("nombreUsuario");
            if (usuario instanceof String && !((String) usuario).trim().isEmpty()) {
                nombreUsuario = (String) usuario;
            } else if (nombreSesion instanceof String && !((String) nombreSesion).trim().isEmpty()) {
                nombreUsuario = (String) nombreSesion;
            }
        }
        request.setAttribute("usuarioActivo", nombreUsuario);
    }

    protected void enviarMensaje(HttpServletRequest request, String mensaje, String tipo) {
        request.setAttribute("mensaje", mensaje);
        request.setAttribute("tipoMensaje", tipo);
    }

    protected int parseEntero(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0;
        }
        return Integer.parseInt(valor.trim());
    }

    protected double parseDecimal(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0;
        }
        return Double.parseDouble(valor.trim().replace(",", "."));
    }
}
