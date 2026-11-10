package com.petservices.db;

import com.petservices.dao.ClienteDAO;
import com.petservices.modelo.Cliente;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la Fase 4: creación, validación, retención y restauración de copias
 * de seguridad. Nunca tocan la base real: usan la base aislada de {@code target}.
 */
class RespaldoTest {

    private static final ClienteDAO CLIENTES = new ClienteDAO();

    @BeforeAll
    static void prepararBaseDeDatos() {
        BaseDatosPrueba.configurar();
    }

    /** Cada prueba parte de una carpeta de copias vacía. */
    @BeforeEach
    void limpiarCopias() throws IOException {
        Path carpeta = RespaldoService.carpetaCopias();
        if (carpeta == null || !Files.isDirectory(carpeta)) {
            return;
        }
        try (Stream<Path> flujo = Files.list(carpeta)) {
            for (Path archivo : flujo.collect(Collectors.toList())) {
                Files.deleteIfExists(archivo);
            }
        }
    }

    @Test
    void creaUnaCopiaValidaYLaLista() {
        Path copia = RespaldoService.crearCopia();

        assertTrue(Files.isRegularFile(copia), "la copia debe existir");
        assertTrue(RespaldoService.esNombreSeguro(copia.getFileName().toString()),
                "el nombre debe cumplir el patrón");
        assertDoesNotThrow(() -> RespaldoService.validar(copia),
                "la copia debe pasar la validación completa");

        List<RespaldoService.Copia> copias = RespaldoService.listar();
        assertEquals(1, copias.size(), "debe listarse la copia recién creada");
        assertEquals(copia.getFileName().toString(), copias.get(0).getNombre());
        assertTrue(copias.get(0).getBytes() > 0, "el tamaño debe ser mayor que cero");
        assertFalse(copias.get(0).getFecha().isEmpty());
        assertFalse(copias.get(0).getTamanoLegible().isEmpty());
    }

    @Test
    void restauraLosDatosDeLaCopia() {
        String sufijo = String.valueOf(System.nanoTime());
        Cliente antes = new Cliente("Cliente Antes " + sufijo,
                "respaldo.antes." + sufijo + "@example.com", "clave123");
        assertTrue(CLIENTES.insertar(antes), "debe insertar el cliente previo a la copia");

        Path copia = RespaldoService.crearCopia();

        Cliente despues = new Cliente("Cliente Después " + sufijo,
                "respaldo.despues." + sufijo + "@example.com", "clave123");
        assertTrue(CLIENTES.insertar(despues), "debe insertar el cliente posterior a la copia");
        assertNotNull(CLIENTES.buscarPorCorreo(despues.getCorreo()));

        RespaldoService.Resultado resultado =
                RespaldoService.restaurar(copia.getFileName().toString());

        assertEquals(copia.getFileName().toString(), resultado.getCopiaRestaurada());
        assertFalse(resultado.getCopiaDeSeguridad().isEmpty(),
                "debe guardarse una copia del estado anterior");
        assertTrue(resultado.getClientes() > 0);

        assertNotNull(CLIENTES.buscarPorCorreo(antes.getCorreo()),
                "el cliente anterior a la copia debe volver");
        assertNull(CLIENTES.buscarPorCorreo(despues.getCorreo()),
                "el cliente posterior a la copia no debe seguir existiendo");
        assertDoesNotThrow(() -> RespaldoService.validar(BaseDatos.ruta()),
                "la base restaurada debe seguir siendo válida");

        // Limpieza: el cliente restaurado vuelve a existir; el posterior ya no.
        Cliente recuperado = CLIENTES.buscarPorCorreo(antes.getCorreo());
        if (recuperado != null) {
            CLIENTES.eliminar(recuperado.getIdCliente());
        }
        CLIENTES.eliminar(despues.getIdCliente());
    }

    @Test
    void rechazaUnArchivoQueNoEsSqliteSinTocarLaBase() throws IOException {
        Cliente cliente = nuevoCliente("invalido");
        Path carpeta = RespaldoService.carpetaCopias();
        Files.createDirectories(carpeta);
        Path falsa = carpeta.resolve("petservices-2000-01-01-000000.db");
        Files.write(falsa, "esto no es una base de datos".getBytes(StandardCharsets.UTF_8));

        RespaldoException error = assertThrows(RespaldoException.class,
                () -> RespaldoService.restaurar(falsa.getFileName().toString()));
        assertTrue(error.getMessage().toLowerCase().contains("sqlite"),
                "el mensaje debe explicar que no es SQLite: " + error.getMessage());

        assertNotNull(CLIENTES.buscarPorCorreo(cliente.getCorreo()),
                "la base actual no debe modificarse");
        CLIENTES.eliminar(cliente.getIdCliente());
    }

    @Test
    void rechazaUnaCopiaConEsquemaNoCompatibleSinTocarLaBase() throws Exception {
        Cliente cliente = nuevoCliente("esquema");
        Path carpeta = RespaldoService.carpetaCopias();
        Files.createDirectories(carpeta);
        Path futura = carpeta.resolve("petservices-2000-01-02-000000.db");
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:sqlite:" + futura.toAbsolutePath());
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("CREATE TABLE schema_version ("
                    + "version INTEGER PRIMARY KEY, aplicada_en TEXT NOT NULL)");
            sentencia.execute("INSERT INTO schema_version VALUES (99, datetime('now'))");
        }

        RespaldoException error = assertThrows(RespaldoException.class,
                () -> RespaldoService.restaurar(futura.getFileName().toString()));
        assertTrue(error.getMessage().contains("99")
                        || error.getMessage().toLowerCase().contains("no compatible"),
                "el mensaje debe indicar la versión incompatible: " + error.getMessage());

        assertNotNull(CLIENTES.buscarPorCorreo(cliente.getCorreo()),
                "la base actual no debe modificarse");
        CLIENTES.eliminar(cliente.getIdCliente());
    }

    @Test
    void conservaSoloLaRetencionConfigurada() {
        String anterior = System.getProperty(RespaldoService.PROP_RETENCION);
        System.setProperty(RespaldoService.PROP_RETENCION, "3");
        try {
            for (int i = 0; i < 5; i++) {
                RespaldoService.crearCopia();
            }
            assertEquals(3, RespaldoService.retencion(), "la retención debe ser la configurada");
            assertEquals(3, RespaldoService.listar().size(),
                    "solo deben conservarse las 3 copias más recientes");
        } finally {
            if (anterior == null) {
                System.clearProperty(RespaldoService.PROP_RETENCION);
            } else {
                System.setProperty(RespaldoService.PROP_RETENCION, anterior);
            }
        }
    }

    @Test
    void restauraUnaCopiaAntiguaAunqueLaRetencionSeaBaja() {
        System.setProperty(RespaldoService.PROP_RETENCION, "5");
        Cliente antes = nuevoCliente("retencion-baja");
        Path copia = RespaldoService.crearCopia();

        Cliente despues = nuevoCliente("posterior");
        assertNotNull(CLIENTES.buscarPorCorreo(despues.getCorreo()));

        // Con retención 1, crear la copia de seguridad previa no debe borrar la
        // copia que se está restaurando antes de usarla.
        System.setProperty(RespaldoService.PROP_RETENCION, "1");
        try {
            RespaldoService.restaurar(copia.getFileName().toString());
            assertNotNull(CLIENTES.buscarPorCorreo(antes.getCorreo()),
                    "la copia antigua debe haberse restaurado");
            assertNull(CLIENTES.buscarPorCorreo(despues.getCorreo()));
        } finally {
            System.clearProperty(RespaldoService.PROP_RETENCION);
        }

        Cliente recuperado = CLIENTES.buscarPorCorreo(antes.getCorreo());
        if (recuperado != null) {
            CLIENTES.eliminar(recuperado.getIdCliente());
        }
        CLIENTES.eliminar(despues.getIdCliente());
    }

    @Test
    void validaNombresYRechazaRutas() {
        assertTrue(RespaldoService.esNombreSeguro("petservices-2026-11-10-094512.db"));
        assertTrue(RespaldoService.esNombreSeguro("petservices-2026-11-10-094512-2.db"));
        assertFalse(RespaldoService.esNombreSeguro("../petservices.db"));
        assertFalse(RespaldoService.esNombreSeguro("petservices.db"));
        assertFalse(RespaldoService.esNombreSeguro(
                "petservices-2026-11-10-094512.db/../otro.db"));
        assertFalse(RespaldoService.esNombreSeguro("otra-cosa.db"));
        assertFalse(RespaldoService.esNombreSeguro(null));

        assertThrows(RespaldoException.class,
                () -> RespaldoService.resolverCopia("../../datos/petservices.db"));
        assertThrows(RespaldoException.class,
                () -> RespaldoService.resolverCopia("petservices-2026-11-10-094512.db"));
    }

    private static Cliente nuevoCliente(String etiqueta) {
        String sufijo = System.nanoTime() + "-" + etiqueta;
        Cliente cliente = new Cliente("Cliente " + etiqueta,
                "respaldo." + sufijo + "@example.com", "clave123");
        assertTrue(CLIENTES.insertar(cliente), "debe insertar el cliente de prueba");
        return cliente;
    }
}
