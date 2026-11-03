package com.petservices.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SecurityHeadersFilter
 * ──────────────────────────────────────────────────────────────────────────
 * Añade cabeceras de seguridad a todas las respuestas.
 *
 * La CSP permite scripts y estilos en línea porque las vistas usan
 * {@code <script>} y atributos {@code onclick}; aun así restringe orígenes,
 * formularios y marcos. Las cookies de sesión se marcan HttpOnly en web.xml
 * y SameSite=Lax en META-INF/context.xml.
 *
 * Proyecto: PetServices - SENA
 */
@WebFilter(urlPatterns = "/*")
public class SecurityHeadersFilter implements Filter {

    private static final String CSP =
            "default-src 'self'; "
            + "img-src 'self' data:; "
            + "style-src 'self' 'unsafe-inline'; "
            + "script-src 'self' 'unsafe-inline'; "
            + "font-src 'self'; "
            + "form-action 'self'; "
            + "frame-ancestors 'self'; "
            + "base-uri 'self'";

    @Override
    public void init(FilterConfig filterConfig) {
        // sin configuración
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse respuesta = (HttpServletResponse) response;
            respuesta.setHeader("X-Content-Type-Options", "nosniff");
            respuesta.setHeader("X-Frame-Options", "SAMEORIGIN");
            respuesta.setHeader("Referrer-Policy", "no-referrer");
            respuesta.setHeader("Content-Security-Policy", CSP);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // sin recursos que liberar
    }
}
