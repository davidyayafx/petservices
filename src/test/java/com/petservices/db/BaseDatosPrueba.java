package com.petservices.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuración de la base de datos para las pruebas.
 *
 * Maven ya define {@code petservices.db.path} y {@code petservices.db.reset}
 * en el pom. Este auxiliar cubre el caso de ejecutar las pruebas desde un IDE,
 * donde esas propiedades no existen: crea una base temporal y la reinicia.
 *
 * Nunca se usa la base real de {@code datos/petservices.db}.
 */
public final class BaseDatosPrueba {

    private static boolean configurada = false;

    private BaseDatosPrueba() {
    }

    public static synchronized void configurar() {
        if (configurada) {
            BaseDatos.inicializar();
            return;
        }
        if (System.getProperty(BaseDatos.PROP_RUTA) == null) {
            try {
                Path carpeta = Files.createTempDirectory("petservices-test-");
                System.setProperty(BaseDatos.PROP_RUTA, carpeta.resolve("petservices.db").toString());
                System.setProperty(BaseDatos.PROP_REINICIAR, "true");
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo crear la base de datos temporal de pruebas", e);
            }
        }
        BaseDatos.inicializar();
        configurada = true;
    }
}
