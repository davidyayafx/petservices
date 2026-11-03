package com.petservices.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * PasswordUtil
 * ──────────────────────────────────────────────────────────────────────────
 * Hash y verificación de contraseñas con PBKDF2-HMAC-SHA256 (JDK, sin
 * dependencias externas).
 *
 * Formato almacenado: {@code pbkdf2$<iteraciones>$<salBase64>$<hashBase64>}
 *
 * - Cada contraseña usa una sal aleatoria de 16 bytes.
 * - La comparación es en tiempo constante ({@link MessageDigest#isEqual}).
 * - {@link #verificar} acepta también valores antiguos en texto plano para no
 *   romper datos previos (migración transparente): al siguiente cambio de
 *   contraseña se guarda ya hasheada.
 *
 * Proyecto: PetServices - SENA
 */
public final class PasswordUtil {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120_000;
    private static final int BITS_CLAVE = 256;
    private static final int BYTES_SAL = 16;
    private static final String PREFIJO = "pbkdf2";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    /** Genera el hash de una contraseña en claro. */
    public static String hashear(String contrasena) {
        if (contrasena == null) {
            return null;
        }
        byte[] sal = new byte[BYTES_SAL];
        RANDOM.nextBytes(sal);
        byte[] hash = pbkdf2(contrasena.toCharArray(), sal, ITERACIONES, BITS_CLAVE);
        return PREFIJO + "$" + ITERACIONES + "$" + b64(sal) + "$" + b64(hash);
    }

    /** Indica si el valor ya está almacenado en formato PBKDF2. */
    public static boolean esHash(String valor) {
        return valor != null && valor.startsWith(PREFIJO + "$") && valor.split("\\$").length == 4;
    }

    /** Verifica una contraseña en claro contra el valor almacenado. */
    public static boolean verificar(String contrasena, String almacenado) {
        if (contrasena == null || almacenado == null) {
            return false;
        }
        if (!esHash(almacenado)) {
            // Compatibilidad con datos antiguos en texto plano.
            return almacenado.equals(contrasena);
        }
        String[] partes = almacenado.split("\\$");
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = pbkdf2(contrasena.toCharArray(), sal, iteraciones, esperado.length * 8);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            // Formato corrupto: se rechaza el acceso.
            return false;
        }
    }

    private static byte[] pbkdf2(char[] contrasena, byte[] sal, int iteraciones, int bits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena, sal, iteraciones, bits);
            try {
                return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }

    private static String b64(byte[] datos) {
        return Base64.getEncoder().encodeToString(datos);
    }
}
