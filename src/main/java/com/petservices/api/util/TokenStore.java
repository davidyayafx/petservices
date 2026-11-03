package com.petservices.api.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TokenStore
 * ──────────────────────────────────────────────────────────────────────────
 * Almacén en memoria de tokens de acceso para la API REST.
 *
 * - Token opaco de 32 bytes aleatorios en Base64 URL-safe.
 * - Vigencia de 8 horas.
 * - Se pierde al reiniciar Tomcat (igual que los datos de los DAO).
 *
 * Proyecto: PetServices - SENA
 */
public final class TokenStore {

    private static final long VIGENCIA_MS = 8L * 60 * 60 * 1000; // 8 horas
    private static final Map<String, Long> TOKENS = new ConcurrentHashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenStore() {}

    /** Emite un token nuevo y devuelve su valor. */
    public static String emitir() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        TOKENS.put(token, System.currentTimeMillis() + VIGENCIA_MS);
        return token;
    }

    /** Indica si el token existe y no ha caducado. */
    public static boolean valido(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        Long expira = TOKENS.get(token);
        if (expira == null) {
            return false;
        }
        if (expira < System.currentTimeMillis()) {
            TOKENS.remove(token);
            return false;
        }
        return true;
    }

    /** Invalida un token (logout). */
    public static void revocar(String token) {
        if (token != null) {
            TOKENS.remove(token);
        }
    }
}
