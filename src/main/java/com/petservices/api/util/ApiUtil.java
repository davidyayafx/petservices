package com.petservices.api.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.petservices.modelo.Cliente;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * ApiUtil
 * ──────────────────────────────────────────────────────────────────────────
 * Utilidades comunes para todos los servicios REST del proyecto PetServices.
 * Centraliza:
 *   - La instancia de Gson usada para (de)serializar JSON.
 *   - La variante que omite la contraseña de Cliente en las respuestas.
 *   - El envío de respuestas JSON con el código de estado HTTP correcto.
 *   - La lectura del cuerpo (body) como JSON, devolviendo {@code null}
 *     cuando el cuerpo está vacío o mal formado (el servlet responde 400).
 *   - La extracción del identificador numérico de la URL (pathInfo),
 *     por ejemplo /api/clientes/3 -> 3
 *   - El DTO homogéneo de mensajes de confirmación y de errores.
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
public final class ApiUtil {

    /** Gson estándar: se usa para peticiones y para modelos sin datos sensibles. */
    public static final Gson GSON = new Gson();

    /**
     * Gson que NO serializa {@code Cliente.contrasena}.
     * Se aplica solo en las respuestas: la lectura de peticiones sigue usando
     * {@link #GSON}, de modo que crear o actualizar un cliente con contraseña
     * continúa funcionando igual que antes.
     */
    private static final Gson GSON_SIN_CONTRASENA = new GsonBuilder()
            .addSerializationExclusionStrategy(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes campo) {
                    return campo.getDeclaringClass() == Cliente.class
                            && "contrasena".equals(campo.getName());
                }

                @Override
                public boolean shouldSkipClass(Class<?> clase) {
                    return false;
                }
            })
            .create();

    /** Mensaje único para cuerpos vacíos o con JSON inválido (respuesta 400). */
    public static final String MSG_JSON_INVALIDO =
            "El cuerpo de la petición debe contener un JSON válido.";

    /** La ruta no trae id (colección). */
    public static final int ID_AUSENTE = -1;

    /** La ruta trae un id que no es numérico. */
    public static final int ID_INVALIDO = -2;

    private ApiUtil() {}

    /** Escribe un objeto como JSON en la respuesta, con el status HTTP indicado. */
    public static void enviarJson(HttpServletResponse response, int status, Object cuerpo)
            throws IOException {
        escribirJson(response, status, cuerpo, GSON);
    }

    /**
     * Igual que {@link #enviarJson}, pero omite el campo {@code contrasena}
     * de los objetos {@link Cliente} (incluidos los que van dentro de listas).
     */
    public static void enviarJsonSinContrasena(HttpServletResponse response, int status, Object cuerpo)
            throws IOException {
        escribirJson(response, status, cuerpo, GSON_SIN_CONTRASENA);
    }

    private static void escribirJson(HttpServletResponse response, int status, Object cuerpo, Gson gson)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(cuerpo));
        }
    }

    /** Envía un mensaje de error homogéneo: {"error": "mensaje"} */
    public static void enviarError(HttpServletResponse response, int status, String mensaje)
            throws IOException {
        enviarJson(response, status, new ErrorDTO(mensaje));
    }

    /** Lee el cuerpo completo de la petición como String (UTF-8). */
    public static String leerBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append(linea);
            }
        }
        return sb.toString();
    }

    /**
     * Lee el cuerpo de la petición y lo convierte al tipo indicado.
     *
     * @return el objeto convertido, o {@code null} si el cuerpo está vacío o
     *         no es un JSON válido para ese tipo. En ambos casos el servlet
     *         debe responder 400 con {@link #MSG_JSON_INVALIDO}.
     */
    public static <T> T leerJson(HttpServletRequest request, Class<T> tipo) throws IOException {
        String cuerpo = leerBody(request);
        if (cuerpo == null || cuerpo.trim().isEmpty()) {
            return null;
        }
        try {
            return GSON.fromJson(cuerpo, tipo);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * Extrae el id numérico del pathInfo de la petición.
     * Ejemplo: pathInfo = "/3" -> 3 ; pathInfo = null o "/" -> -1 (no hay id);
     * pathInfo no numérico (ej: "/abc") -> -2 (id inválido).
     */
    public static int extraerId(HttpServletRequest request) {
        String pathInfo = request.getPathInfo(); // ej: "/3"
        if (pathInfo == null || pathInfo.equals("/")) {
            return ID_AUSENTE;
        }
        String valor = pathInfo.substring(1); // quita el "/"
        // Si viene con más segmentos (ej: /3/algo) solo tomamos el primero
        int barra = valor.indexOf('/');
        if (barra != -1) {
            valor = valor.substring(0, barra);
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return ID_INVALIDO;
        }
    }

    /**
     * Responde 404 cuando la ruta trae un id que no es numérico.
     *
     * @return {@code false} si ya se envió la respuesta y el servlet debe salir.
     */
    public static boolean idRutaValido(HttpServletResponse response, int id) throws IOException {
        if (id == ID_INVALIDO) {
            enviarError(response, HttpServletResponse.SC_NOT_FOUND,
                    "El id de la URL debe ser un número entero.");
            return false;
        }
        return true;
    }

    /** DTO simple para representar errores en formato JSON. */
    public static class ErrorDTO {
        public String error;
        public ErrorDTO(String error) { this.error = error; }
    }

    /** DTO simple y homogéneo para mensajes de confirmación. */
    public static class MensajeDTO {
        public String mensaje;
        public MensajeDTO(String mensaje) { this.mensaje = mensaje; }
    }
}
