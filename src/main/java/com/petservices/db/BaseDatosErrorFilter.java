package com.petservices.db;

import com.petservices.api.util.ApiUtil;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convierte los errores de persistencia en respuestas controladas.
 *
 * - Las rutas de la API reciben JSON con el contrato habitual.
 * - Las vistas reciben una página 500 o 503, sin detalles técnicos.
 * - El detalle completo queda en el log de Tomcat.
 */
@WebFilter(urlPatterns = "/*")
public class BaseDatosErrorFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(BaseDatosErrorFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) {
        // Sin configuración.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (BaseDatosException e) {
            manejar((HttpServletRequest) request, (HttpServletResponse) response, e);
        } catch (ServletException e) {
            if (e.getCause() instanceof BaseDatosException) {
                manejar((HttpServletRequest) request, (HttpServletResponse) response,
                        (BaseDatosException) e.getCause());
            } else {
                throw e;
            }
        }
    }

    private void manejar(HttpServletRequest peticion, HttpServletResponse respuesta, BaseDatosException error)
            throws IOException, ServletException {
        LOG.log(Level.SEVERE, "Error de base de datos en " + peticion.getRequestURI(), error);

        if (respuesta.isCommitted()) {
            throw error;
        }

        boolean noDisponible = error.getCodigo() == BaseDatosException.Codigo.CONEXION
                || error.getCodigo() == BaseDatosException.Codigo.ESQUEMA
                || error.getCodigo() == BaseDatosException.Codigo.BLOQUEO
                || error.getCodigo() == BaseDatosException.Codigo.DISCO;

        int estado = noDisponible ? HttpServletResponse.SC_SERVICE_UNAVAILABLE
                : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

        if (peticion.getRequestURI() != null && peticion.getRequestURI().contains("/api/")) {
            ApiUtil.enviarError(respuesta, estado, error.getMensajeUsuario());
            return;
        }

        respuesta.setStatus(estado);
        peticion.setAttribute("mensajeError", error.getMensajeUsuario());
        String destino = noDisponible ? "/vistas/error503.jsp" : "/vistas/error500.jsp";
        peticion.getRequestDispatcher(destino).forward(peticion, respuesta);
    }

    @Override
    public void destroy() {
        // Sin recursos que liberar.
    }
}
