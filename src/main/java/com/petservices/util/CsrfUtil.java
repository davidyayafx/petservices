package com.petservices.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CsrfUtil
 * ──────────────────────────────────────────────────────────────────────────
 * Token CSRF por sesión para los formularios del panel web.
 *
 * - Se genera una sola vez por sesión y se guarda como atributo.
 * - Los JSP lo insertan en un campo oculto {@code csrfToken}.
 * - Los servlets de módulo lo validan en cada POST.
 *
 * Proyecto: PetServices - SENA
 */
public final class CsrfUtil {

    public static final String ATRIBUTO = "csrfToken";
    public static final String PARAMETRO = "csrfToken";

    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() {}

    /** Devuelve el token de la sesión, creándolo si aún no existe. */
    public static String token(HttpSession sesion) {
        if (sesion == null) {
            return "";
        }
        Object actual = sesion.getAttribute(ATRIBUTO);
        if (actual instanceof String && !((String) actual).isEmpty()) {
            return (String) actual;
        }
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        String nuevo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sesion.setAttribute(ATRIBUTO, nuevo);
        return nuevo;
    }

    /** Comprueba que el POST traiga el token de la sesión. */
    public static boolean valido(HttpServletRequest peticion) {
        HttpSession sesion = peticion.getSession(false);
        if (sesion == null) {
            return false;
        }
        Object esperado = sesion.getAttribute(ATRIBUTO);
        String recibido = peticion.getParameter(PARAMETRO);
        if (!(esperado instanceof String) || recibido == null) {
            return false;
        }
        return MessageDigest.isEqual(
                ((String) esperado).getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8));
    }
}
