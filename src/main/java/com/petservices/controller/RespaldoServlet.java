package com.petservices.controller;

import com.petservices.db.BaseDatos;
import com.petservices.db.BaseDatosException;
import com.petservices.db.RespaldoException;
import com.petservices.db.RespaldoService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet: RespaldoServlet
 * URL de mapeo: /respaldos
 * ──────────────────────────────────────────────────────────────────────────
 * Gestiona las copias de seguridad de la base SQLite:
 *
 *  doGet  → listar copias (vacío) o descargar una (accion=descargar)
 *  doPost → crear una copia (accion=crear) o restaurar una (accion=restaurar)
 *
 * Seguridad: exige sesión, valida el token CSRF en todos los POST y solo
 * resuelve nombres de archivo que cumplen el patrón de copia (sin rutas).
 *
 * Proyecto: PetServices - SENA (Fase 4)
 */
@WebServlet("/respaldos")
public class RespaldoServlet extends BaseModuleServlet {

    private static final Logger LOG = Logger.getLogger(RespaldoServlet.class.getName());

    // ──────────────────────────────────────────────────────────────────────
    // doGet: listado y descarga
    // ──────────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException, ServletException {

        if (!validarSesion(request, response)) {
            return;
        }

        if ("descargar".equalsIgnoreCase(limpiar(request.getParameter("accion")))) {
            descargar(request, response);
            return;
        }

        mostrar(request, response);
    }

    // ──────────────────────────────────────────────────────────────────────
    // doPost: crear y restaurar
    // ──────────────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException, ServletException {

        if (!validarSesion(request, response)) {
            return;
        }
        if (!validarCsrf(request, response)) {
            return;
        }

        String accion = limpiar(request.getParameter("accion"));
        try {
            if ("crear".equalsIgnoreCase(accion)) {
                Path copia = RespaldoService.crearCopia();
                enviarMensaje(request,
                        "✅ Copia de seguridad creada: " + copia.getFileName() + ".", "exito");
            } else if ("restaurar".equalsIgnoreCase(accion)) {
                RespaldoService.Resultado resultado =
                        RespaldoService.restaurar(limpiar(request.getParameter("nombre")));
                enviarMensaje(request, "✅ Copia restaurada: " + resultado.getCopiaRestaurada()
                        + ". El estado anterior quedó guardado en "
                        + resultado.getCopiaDeSeguridad() + ".", "exito");
            } else {
                enviarMensaje(request, "❌ Acción no reconocida.", "error");
            }
        } catch (RespaldoException e) {
            enviarMensaje(request, "❌ " + e.getMessage(), "error");
        } catch (BaseDatosException e) {
            enviarMensaje(request, "❌ " + e.getMensajeUsuario(), "error");
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Fallo inesperado en una operación de respaldo", e);
            enviarMensaje(request,
                    "❌ No se pudo completar la operación. Revisa los logs de la aplicación.", "error");
        }

        mostrar(request, response);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Auxiliares
    // ──────────────────────────────────────────────────────────────────────

    private void descargar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String nombre = limpiar(request.getParameter("nombre"));
        try {
            Path archivo = RespaldoService.resolverCopia(nombre);
            response.setContentType("application/x-sqlite3");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + nombre + "\"");
            response.setContentLengthLong(Files.size(archivo));
            try (OutputStream salida = response.getOutputStream()) {
                Files.copy(archivo, salida);
            }
        } catch (RespaldoException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        List<RespaldoService.Copia> copias = RespaldoService.listar();
        Path carpeta = RespaldoService.carpetaCopias();
        request.setAttribute("listaCopias", copias);
        request.setAttribute("carpetaCopias", carpeta == null ? "(sin inicializar)" : carpeta.toString());
        request.setAttribute("rutaBaseDatos", BaseDatos.descripcionRuta());
        request.setAttribute("retencionCopias", RespaldoService.retencion());
        prepararCabecera(request);
        forward(request, response, "/vistas/respaldos.jsp");
    }
}
