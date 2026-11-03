package com.petservices.api;

import com.petservices.api.util.ApiUtil;
import com.petservices.api.util.TokenStore;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ApiAuthFilter
 * ──────────────────────────────────────────────────────────────────────────
 * Exige un token Bearer válido en todas las rutas {@code /api/*}, excepto
 * {@code POST /api/auth/login} (y las peticiones preflight OPTIONS).
 *
 * Cabecera esperada: {@code Authorization: Bearer <token>}
 * Si falta o no es válido responde 401 con el formato de error de {@link ApiUtil}.
 *
 * Proyecto: PetServices - SENA
 */
@WebFilter(urlPatterns = "/api/*")
public class ApiAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // sin configuración
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest peticion = (HttpServletRequest) request;
        HttpServletResponse respuesta = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(peticion.getMethod()) || esLogin(peticion)) {
            chain.doFilter(request, response);
            return;
        }

        String token = tokenDe(peticion);
        if (!TokenStore.valido(token)) {
            respuesta.setHeader("WWW-Authenticate", "Bearer");
            ApiUtil.enviarError(respuesta, HttpServletResponse.SC_UNAUTHORIZED,
                    "No autorizado. Envíe un token válido en la cabecera Authorization: Bearer <token>.");
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // sin recursos que liberar
    }

    /** Ruta de login: pública. */
    private static boolean esLogin(HttpServletRequest peticion) {
        String uri = peticion.getRequestURI();
        return uri != null && uri.endsWith("/api/auth/login");
    }

    /** Extrae el token de la cabecera Authorization, o null si no viene. */
    public static String tokenDe(HttpServletRequest peticion) {
        String cabecera = peticion.getHeader("Authorization");
        if (cabecera == null) {
            return null;
        }
        String valor = cabecera.trim();
        if (valor.length() > 7 && valor.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return valor.substring(7).trim();
        }
        return null;
    }
}
