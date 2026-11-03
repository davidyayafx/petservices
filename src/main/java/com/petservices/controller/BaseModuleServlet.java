package com.petservices.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * BaseModuleServlet
 * ──────────────────────────────────────────────────────────────────────────
 * Clase base de los controladores de módulo (clientes, mascotas, citas,
 * servicios, productos y pedidos). Concentra las utilidades comunes:
 *
 *   - Validación de sesión y forward a las vistas.
 *   - Limpieza homogénea de parámetros ({@link #limpiar(String)}).
 *   - Conversión tolerante de números ({@link #parseInt}, {@link #parseEntero},
 *     {@link #parseDecimal}): si el valor falta o no es numérico devuelven el
 *     valor por defecto en lugar de lanzar una excepción.
 *   - Mensajes flash y datos de cabecera de la vista.
 *
 * Proyecto: PetServices - SENA GA7-220501097
 */
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

    /** Devuelve el texto sin espacios sobrantes; null se convierte en "". */
    protected String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    /** Convierte un texto a entero; si falta o no es numérico devuelve el valor por defecto. */
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

    /** Convierte un texto a decimal (admite coma); si falta o no es numérico devuelve el valor por defecto. */
    protected double parseDecimal(String valor, double defecto) {
        if (valor == null || valor.trim().isEmpty()) {
            return defecto;
        }
        try {
            return Double.parseDouble(valor.trim().replace(",", "."));
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

    /** Atajo de {@link #parseInt(String, int)} con 0 como valor por defecto. */
    protected int parseEntero(String valor) {
        return parseInt(valor, 0);
    }

    /** Atajo de {@link #parseDecimal(String, double)} con 0 como valor por defecto. */
    protected double parseDecimal(String valor) {
        return parseDecimal(valor, 0);
    }
}
