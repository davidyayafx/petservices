package com.petservices.util;

import com.petservices.api.util.TokenStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de TokenStore (tokens Bearer en memoria).
 */
class TokenStoreTest {

    @Test
    void emiteTokensValidosYDistintos() {
        String a = TokenStore.emitir();
        String b = TokenStore.emitir();
        assertNotNull(a);
        assertTrue(a.length() >= 40);
        assertNotEquals(a, b);
        assertTrue(TokenStore.valido(a));
        assertTrue(TokenStore.valido(b));
    }

    @Test
    void revocarInvalidaElToken() {
        String token = TokenStore.emitir();
        TokenStore.revocar(token);
        assertFalse(TokenStore.valido(token));
    }

    @Test
    void rechazaNulosVaciosYDesconocidos() {
        assertFalse(TokenStore.valido(null));
        assertFalse(TokenStore.valido(""));
        assertFalse(TokenStore.valido("token-inventado"));
    }
}
