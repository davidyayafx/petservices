package com.petservices.api;

import com.petservices.api.util.TokenStore;
import com.petservices.dao.ClienteDAO;
import com.petservices.modelo.Cliente;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.petservices.api.util.ApiUtil.*;

/**
 * Servicio REST: /api/auth
 * ──────────────────────────────────────────────────────────────────────────
 * POST /api/auth/login   -> valida credenciales y devuelve el cliente + token
 * POST /api/auth/logout  -> invalida el token enviado en Authorization
 *
 * El login es el único endpoint público de la API. El resto exige la cabecera
 * {@code Authorization: Bearer <token>} (ver ApiAuthFilter).
 *
 * La respuesta del login mantiene los campos del cliente en la raíz y añade
 * {@code token}; nunca incluye {@code contrasena}.
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
@WebServlet(urlPatterns = {"/api/auth/login", "/api/auth/logout"})
public class AuthApiServlet extends HttpServlet {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getServletPath().endsWith("/logout")) {
            cerrarSesion(request, response);
            return;
        }
        iniciarSesion(request, response);
    }

    private void iniciarSesion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        Credenciales credenciales = leerJson(request, Credenciales.class);
        if (credenciales == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, MSG_JSON_INVALIDO);
            return;
        }
        if (credenciales.correo == null || credenciales.contrasena == null) {
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, "Los campos 'correo' y 'contrasena' son obligatorios");
            return;
        }
        Cliente cliente = clienteDAO.validarLogin(credenciales.correo, credenciales.contrasena);
        if (cliente == null) {
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Correo o contraseña incorrectos");
            return;
        }
        String token = TokenStore.emitir();
        enviarJson(response, HttpServletResponse.SC_OK, new LoginRespuesta(cliente, token));
    }

    private void cerrarSesion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        TokenStore.revocar(ApiAuthFilter.tokenDe(request));
        enviarJson(response, HttpServletResponse.SC_OK, new MensajeDTO("Sesión cerrada correctamente"));
    }

    /** DTO de entrada para el login. */
    static class Credenciales {
        String correo;
        String contrasena;
    }

    /** Respuesta del login: campos del cliente en la raíz + token. */
    static class LoginRespuesta {
        int idCliente;
        String nombre;
        String correo;
        String fechaRegistro;
        String token;

        LoginRespuesta(Cliente cliente, String token) {
            this.idCliente = cliente.getIdCliente();
            this.nombre = cliente.getNombre();
            this.correo = cliente.getCorreo();
            this.fechaRegistro = cliente.getFechaRegistro();
            this.token = token;
        }
    }
}
