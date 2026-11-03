package com.petservices.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de PasswordUtil (PBKDF2).
 */
class PasswordUtilTest {

    @Test
    void elHashNoEsLaContrasenaEnClaro() {
        String hash = PasswordUtil.hashear("123456");
        assertTrue(PasswordUtil.esHash(hash), "debe tener formato pbkdf2$...");
        assertNotEquals("123456", hash);
        assertFalse(hash.contains("123456"));
    }

    @Test
    void verificaLaContrasenaCorrectaYRechazaLaIncorrecta() {
        String hash = PasswordUtil.hashear("claveSegura");
        assertTrue(PasswordUtil.verificar("claveSegura", hash));
        assertFalse(PasswordUtil.verificar("claveSegura1", hash));
        assertFalse(PasswordUtil.verificar("CLAVESEGURA", hash));
    }

    @Test
    void dosHasheosDeLaMismaClaveSonDistintosPorLaSal() {
        assertNotEquals(PasswordUtil.hashear("igual"), PasswordUtil.hashear("igual"));
    }

    @Test
    void aceptaDatosAntiguosEnTextoPlano() {
        assertTrue(PasswordUtil.verificar("vieja", "vieja"));
        assertFalse(PasswordUtil.verificar("otra", "vieja"));
    }

    @Test
    void manejaValoresNulos() {
        assertNull(PasswordUtil.hashear(null));
        assertFalse(PasswordUtil.verificar(null, "x"));
        assertFalse(PasswordUtil.verificar("x", null));
        assertFalse(PasswordUtil.esHash(null));
    }

    @Test
    void rechazaUnHashCorrupto() {
        assertFalse(PasswordUtil.verificar("x", "pbkdf2$120000$no-base64$tampoco"));
    }
}
