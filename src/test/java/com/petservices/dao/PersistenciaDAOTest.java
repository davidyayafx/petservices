package com.petservices.dao;

import com.petservices.db.BaseDatos;
import com.petservices.db.BaseDatosPrueba;
import com.petservices.modelo.Cita;
import com.petservices.modelo.Cliente;
import com.petservices.modelo.Mascota;
import com.petservices.modelo.Servicio;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprueba que los datos de la Fase 2 sobreviven a un cierre y reapertura de
 * la base (equivalente a reiniciar Tomcat): clientes, mascotas, citas y
 * servicios, incluidos sus identificadores y relaciones.
 */
class PersistenciaDAOTest {

    @BeforeAll
    static void prepararBaseDeDatos() {
        BaseDatosPrueba.configurar();
    }

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final MascotaDAO mascotaDAO = new MascotaDAO();
    private final CitaDAO citaDAO = new CitaDAO();
    private final ServicioDAO servicioDAO = new ServicioDAO();

    @Test
    void losDatosSobrevivenAlReinicioDeLaBase() {
        String sufijo = String.valueOf(System.nanoTime());
        String correo = "persistencia." + sufijo + "@example.com";

        Cliente cliente = new Cliente("Cliente Persistencia", correo, "clave123");
        assertTrue(clienteDAO.insertar(cliente), "debe insertar el cliente");

        Servicio servicio = new Servicio("Prueba", "Servicio " + sufijo);
        assertTrue(servicioDAO.insertar(servicio), "debe insertar el servicio");

        Mascota mascota = new Mascota("Mascota " + sufijo, "Perro", "Mestizo",
                2, cliente.getIdCliente(), cliente.getNombre());
        mascota.setIdServicio(servicio.getIdServicio());
        assertTrue(mascotaDAO.insertar(mascota), "debe insertar la mascota");

        Cita cita = new Cita(cliente.getIdCliente(), cliente.getNombre(),
                mascota.getIdMascota(), mascota.getNombre(),
                "2030-01-15T09:30", "Consulta", "Pendiente");
        assertTrue(citaDAO.insertar(cita), "debe insertar la cita");

        // Simula un reinicio del servidor: cierra y reabre la misma base.
        BaseDatos.reiniciarParaPruebas();

        Cliente clienteRecuperado = new ClienteDAO().buscarPorCorreo(correo);
        assertNotNull(clienteRecuperado, "el cliente debe seguir existiendo tras el reinicio");
        assertEquals(cliente.getIdCliente(), clienteRecuperado.getIdCliente());
        assertEquals("Cliente Persistencia", clienteRecuperado.getNombre());
        assertNotNull(new ClienteDAO().validarLogin(correo, "clave123"),
                "la contraseña PBKDF2 debe seguir validando");

        Mascota mascotaRecuperada = new MascotaDAO().buscarPorId(mascota.getIdMascota());
        assertNotNull(mascotaRecuperada, "la mascota debe seguir existiendo");
        assertEquals(cliente.getIdCliente(), mascotaRecuperada.getIdCliente());
        assertEquals(cliente.getNombre(), mascotaRecuperada.getNombreCliente());
        assertEquals(servicio.getIdServicio(), mascotaRecuperada.getIdServicio());

        Cita citaRecuperada = new CitaDAO().buscarPorId(cita.getIdCita());
        assertNotNull(citaRecuperada, "la cita debe seguir existiendo");
        assertEquals(cliente.getIdCliente(), citaRecuperada.getIdCliente());
        assertEquals(mascota.getIdMascota(), citaRecuperada.getIdMascota());
        assertEquals("Cliente Persistencia", citaRecuperada.getNombreCliente());
        assertEquals(mascota.getNombre(), citaRecuperada.getNombreMascota());
        assertEquals("2030-01-15T09:30", citaRecuperada.getFechaHora());

        Servicio servicioRecuperado = new ServicioDAO().buscarPorId(servicio.getIdServicio());
        assertNotNull(servicioRecuperado, "el servicio debe seguir existiendo");
        assertEquals(servicio.getNombre(), servicioRecuperado.getNombre());

        // Limpieza para no interferir con otras pruebas.
        new CitaDAO().eliminar(cita.getIdCita());
        new MascotaDAO().eliminar(mascota.getIdMascota());
        new ClienteDAO().eliminar(cliente.getIdCliente());
        new ServicioDAO().eliminar(servicio.getIdServicio());
    }
}
