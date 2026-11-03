package com.petservices.dao;

import com.petservices.modelo.Cliente;
import com.petservices.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del DAO de clientes: semillas, CRUD, duplicados y contraseña hasheada.
 * Los DAO son estáticos; cada prueba usa correos propios para no interferir.
 */
class ClienteDAOTest {

    private final ClienteDAO dao = new ClienteDAO();

    @Test
    void cargaLasSemillas() {
        List<Cliente> lista = dao.listarTodos();
        assertTrue(lista.size() >= 3);
        assertNotNull(dao.buscarPorCorreo("alejandro@gmail.com"));
        assertTrue(lista.get(0).getIdCliente() <= lista.get(lista.size() - 1).getIdCliente(),
                "listarTodos debe venir ordenado por id");
    }

    @Test
    void insertaHasheaYValidaLogin() {
        Cliente nuevo = new Cliente("Prueba Hash", "hash.test@example.com", "clave123");
        assertTrue(dao.insertar(nuevo));
        assertTrue(nuevo.getContrasena().startsWith("pbkdf2$"));
        assertTrue(PasswordUtil.esHash(nuevo.getContrasena()));

        Cliente guardado = dao.buscarPorCorreo("HASH.TEST@EXAMPLE.COM");
        assertNotNull(guardado, "buscarPorCorreo debe ignorar mayúsculas");

        assertNotNull(dao.validarLogin("hash.test@example.com", "clave123"));
        assertNull(dao.validarLogin("hash.test@example.com", "incorrecta"));

        dao.eliminar(guardado.getIdCliente());
    }

    @Test
    void rechazaCorreoDuplicadoAlInsertar() {
        assertTrue(dao.insertar(new Cliente("Dup A", "dup.insert@example.com", "x")));
        assertFalse(dao.insertar(new Cliente("Dup B", "dup.insert@example.com", "y")));

        Cliente a = dao.buscarPorCorreo("dup.insert@example.com");
        dao.eliminar(a.getIdCliente());
    }

    @Test
    void rechazaCorreoDuplicadoAlActualizarSinTocarElOriginal() {
        Cliente a = new Cliente("Act A", "act.a@example.com", "x");
        Cliente b = new Cliente("Act B", "act.b@example.com", "x");
        assertTrue(dao.insertar(a));
        assertTrue(dao.insertar(b));

        // Copia con el id de B pero el correo de A (sin mutar la instancia guardada).
        Cliente copia = new Cliente(b.getIdCliente(), "Act B", "act.a@example.com", null, null);
        assertFalse(dao.actualizar(copia));

        assertEquals("act.b@example.com", dao.buscarPorId(b.getIdCliente()).getCorreo(),
                "el cliente B no debe quedar modificado");

        dao.eliminar(a.getIdCliente());
        dao.eliminar(b.getIdCliente());
    }

    @Test
    void eliminaUnCliente() {
        Cliente c = new Cliente("Borrar", "borrar.test@example.com", "x");
        assertTrue(dao.insertar(c));
        assertTrue(dao.eliminar(c.getIdCliente()));
        assertNull(dao.buscarPorId(c.getIdCliente()));
        assertFalse(dao.eliminar(999_999));
    }
}
