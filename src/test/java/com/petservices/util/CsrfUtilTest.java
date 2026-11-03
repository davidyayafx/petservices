package com.petservices.util;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de CsrfUtil con sesiones y peticiones simuladas mediante proxies.
 */
class CsrfUtilTest {

    private static HttpSession sesion(Map<String, Object> atributos) {
        return (HttpSession) Proxy.newProxyInstance(
                CsrfUtilTest.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, metodo, args) -> {
                    switch (metodo.getName()) {
                        case "getAttribute":
                            return atributos.get((String) args[0]);
                        case "setAttribute":
                            atributos.put((String) args[0], args[1]);
                            return null;
                        case "removeAttribute":
                            atributos.remove((String) args[0]);
                            return null;
                        case "toString":
                            return "SesionFalsa";
                        default:
                            return null;
                    }
                });
    }

    private static HttpServletRequest peticion(HttpSession sesion, String token) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                CsrfUtilTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, metodo, args) -> {
                    switch (metodo.getName()) {
                        case "getSession":
                            return sesion;
                        case "getParameter":
                            return CsrfUtil.PARAMETRO.equals(args[0]) ? token : null;
                        default:
                            return null;
                    }
                });
    }

    @Test
    void elTokenEsEstableDentroDeLaSesion() {
        Map<String, Object> atributos = new HashMap<>();
        HttpSession sesion = sesion(atributos);
        String primero = CsrfUtil.token(sesion);
        String segundo = CsrfUtil.token(sesion);
        assertNotNull(primero);
        assertFalse(primero.isEmpty());
        assertEquals(primero, segundo);
    }

    @Test
    void validaElTokenCorrecto() {
        Map<String, Object> atributos = new HashMap<>();
        HttpSession sesion = sesion(atributos);
        String token = CsrfUtil.token(sesion);
        assertTrue(CsrfUtil.valido(peticion(sesion, token)));
    }

    @Test
    void rechazaTokenIncorrectoAusenteOSinSesion() {
        Map<String, Object> atributos = new HashMap<>();
        HttpSession sesion = sesion(atributos);
        CsrfUtil.token(sesion);
        assertFalse(CsrfUtil.valido(peticion(sesion, "otro")));
        assertFalse(CsrfUtil.valido(peticion(sesion, null)));
        assertFalse(CsrfUtil.valido(peticion(null, "x")));
    }

    @Test
    void devuelveVacioSinSesion() {
        assertEquals("", CsrfUtil.token(null));
    }
}
