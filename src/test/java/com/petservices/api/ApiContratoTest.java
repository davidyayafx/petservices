package com.petservices.api;

import com.petservices.dao.CitaDAO;
import com.petservices.dao.ClienteDAO;
import com.petservices.db.BaseDatosPrueba;
import com.petservices.modelo.Cita;
import com.petservices.modelo.Cliente;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de contrato de la API sin contenedor: se invocan los servlets con
 * peticiones y respuestas simuladas mediante proxies dinámicos.
 *
 * Cubre los códigos y reglas de PETIMPROVE F1/F2: 400 por correo inválido,
 * 409 por correo duplicado, 404 por id/ mascota inexistente y 201 con dueño
 * derivado en citas.
 */
class ApiContratoTest {

    @BeforeAll
    static void prepararBaseDeDatos() {
        BaseDatosPrueba.configurar();
    }

    // ── Infraestructura de simulación ──────────────────────────────────

    private static final class Respuesta {
        int status = 200;
        final StringWriter cuerpo = new StringWriter();
        final PrintWriter writer = new PrintWriter(cuerpo);
    }

    private static HttpServletRequest peticion(String metodo, String pathInfo, String cuerpo,
                                               Map<String, String> parametros) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                ApiContratoTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, m, args) -> {
                    switch (m.getName()) {
                        case "getMethod":
                            return metodo;
                        case "getPathInfo":
                            return pathInfo;
                        case "getReader":
                            return new BufferedReader(new StringReader(cuerpo == null ? "" : cuerpo));
                        case "getParameter":
                            return parametros.get((String) args[0]);
                        case "getHeader":
                            return null;
                        case "setCharacterEncoding":
                            return null;
                        default:
                            return valorPorDefecto(m.getReturnType());
                    }
                });
    }

    private static HttpServletResponse respuesta(Respuesta r) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                ApiContratoTest.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, m, args) -> {
                    switch (m.getName()) {
                        case "setStatus":
                            r.status = (Integer) args[0];
                            return null;
                        case "getStatus":
                            return r.status;
                        case "getWriter":
                            return r.writer;
                        case "setContentType":
                        case "setCharacterEncoding":
                            return null;
                        default:
                            return valorPorDefecto(m.getReturnType());
                    }
                });
    }

    private static Object valorPorDefecto(Class<?> tipo) {
        if (tipo == boolean.class) return false;
        if (tipo == int.class) return 0;
        if (tipo == long.class) return 0L;
        if (tipo == double.class) return 0.0;
        return null;
    }

    private static Respuesta ejecutar(Object servlet, String metodo, String ruta, String cuerpo,
                                      Map<String, String> parametros) throws Exception {
        Respuesta r = new Respuesta();
        HttpServletRequest req = peticion(metodo, ruta, cuerpo, parametros);
        HttpServletResponse res = respuesta(r);
        if (metodo.equals("GET")) {
            servlet.getClass().getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class)
                    .invoke(servlet, req, res);
        } else if (metodo.equals("POST")) {
            servlet.getClass().getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class)
                    .invoke(servlet, req, res);
        } else if (metodo.equals("PUT")) {
            servlet.getClass().getDeclaredMethod("doPut", HttpServletRequest.class, HttpServletResponse.class)
                    .invoke(servlet, req, res);
        }
        r.writer.flush();
        return r;
    }

    private static Map<String, String> sinParametros() {
        return new HashMap<>();
    }

    // ── Clientes ──────────────────────────────────────────────────────

    @Test
    void correoInvalidoDevuelve400() throws Exception {
        Respuesta r = ejecutar(new ClienteApiServlet(), "POST", null,
                "{\"nombre\":\"X\",\"correo\":\"no-es-correo\"}", sinParametros());
        assertEquals(400, r.status);
        assertTrue(r.cuerpo.toString().contains("formato"));
    }

    @Test
    void clienteValidoDevuelve201SinContrasena() throws Exception {
        String correo = "contrato.test@example.com";
        Respuesta r = ejecutar(new ClienteApiServlet(), "POST", null,
                "{\"nombre\":\"Contrato\",\"correo\":\"" + correo + "\",\"contrasena\":\"123456\"}",
                sinParametros());
        try {
            assertEquals(201, r.status);
            assertFalse(r.cuerpo.toString().contains("contrasena"));
            assertTrue(r.cuerpo.toString().contains("Contrato"));
        } finally {
            Cliente c = new ClienteDAO().buscarPorCorreo(correo);
            if (c != null) {
                new ClienteDAO().eliminar(c.getIdCliente());
            }
        }
    }

    @Test
    void correoDuplicadoEnPutDevuelve409() throws Exception {
        Respuesta r = ejecutar(new ClienteApiServlet(), "PUT", "/1",
                "{\"correo\":\"daniel@gmail.com\"}", sinParametros());
        assertEquals(409, r.status);
        assertEquals("alejandro@gmail.com", new ClienteDAO().buscarPorId(1).getCorreo(),
                "el cliente 1 no debe quedar modificado");
    }

    @Test
    void idNoNumericoDevuelve404() throws Exception {
        Respuesta r = ejecutar(new ClienteApiServlet(), "GET", "/abc", null, sinParametros());
        assertEquals(404, r.status);
        assertTrue(r.cuerpo.toString().contains("número entero"));
    }

    // ── Citas ─────────────────────────────────────────────────────────

    @Test
    void citaSinMascotaDevuelve400() throws Exception {
        Respuesta r = ejecutar(new CitaApiServlet(), "POST", null,
                "{\"fechaHora\":\"2026-12-01T10:00\"}", sinParametros());
        assertEquals(400, r.status);
    }

    @Test
    void citaConMascotaInexistenteDevuelve404() throws Exception {
        Respuesta r = ejecutar(new CitaApiServlet(), "POST", null,
                "{\"fechaHora\":\"2026-12-01T10:00\",\"idMascota\":99999}", sinParametros());
        assertEquals(404, r.status);
    }

    @Test
    void citaValidaDerivaElDuenoYLosNombres() throws Exception {
        String fechaHora = "2026-12-03T08:15";
        Respuesta r = ejecutar(new CitaApiServlet(), "POST", null,
                "{\"fechaHora\":\"" + fechaHora + "\",\"idMascota\":1}", sinParametros());
        try {
            assertEquals(201, r.status);
            assertTrue(r.cuerpo.toString().contains("\"idCliente\":1"));
            assertTrue(r.cuerpo.toString().contains("\"nombreMascota\":\"Max\""));
            assertTrue(r.cuerpo.toString().contains("\"nombreCliente\":\"Alejandro Puerto\""));
        } finally {
            List<Cita> citas = new CitaDAO().listarTodos();
            for (Cita c : citas) {
                if (fechaHora.equals(c.getFechaHora())) {
                    new CitaDAO().eliminar(c.getIdCita());
                }
            }
        }
    }
}
