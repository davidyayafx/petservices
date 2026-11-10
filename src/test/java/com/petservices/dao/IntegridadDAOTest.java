package com.petservices.dao;

import com.petservices.db.BaseDatosPrueba;
import com.petservices.modelo.Cita;
import com.petservices.modelo.Cliente;
import com.petservices.modelo.Mascota;
import com.petservices.modelo.Servicio;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reglas de integridad de la Fase 2: duplicados, claves foráneas y protección
 * contra borrados que dejarían registros relacionados huérfanos.
 */
class IntegridadDAOTest {

    @BeforeAll
    static void prepararBaseDeDatos() {
        BaseDatosPrueba.configurar();
    }

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final MascotaDAO mascotaDAO = new MascotaDAO();
    private final CitaDAO citaDAO = new CitaDAO();
    private final ServicioDAO servicioDAO = new ServicioDAO();

    @Test
    void rechazaServicioDuplicado() {
        String sufijo = String.valueOf(System.nanoTime());
        Servicio primero = new Servicio("Prueba duplicado", "Servicio " + sufijo);
        assertTrue(servicioDAO.insertar(primero));
        try {
            Servicio segundo = new Servicio("Prueba duplicado", "Servicio " + sufijo);
            assertFalse(servicioDAO.insertar(segundo),
                    "tipo + nombre debe ser único sin distinguir mayúsculas");
        } finally {
            servicioDAO.eliminar(primero.getIdServicio());
        }
    }

    @Test
    void rechazaCitaDuplicadaParaLaMismaMascotaYHorario() {
        String sufijo = String.valueOf(System.nanoTime());
        Cliente cliente = crearCliente(sufijo);
        Mascota mascota = crearMascota(cliente, sufijo);
        Cita primera = new Cita(cliente.getIdCliente(), cliente.getNombre(),
                mascota.getIdMascota(), mascota.getNombre(),
                "2030-02-01T10:00", "Consulta", "Pendiente");
        try {
            assertTrue(citaDAO.insertar(primera));
            Cita segunda = new Cita(cliente.getIdCliente(), cliente.getNombre(),
                    mascota.getIdMascota(), mascota.getNombre(),
                    "2030-02-01T10:00", "Consulta", "Confirmada");
            assertFalse(citaDAO.insertar(segunda),
                    "no debe permitir dos citas activas para la misma mascota y horario");
        } finally {
            if (primera.getIdCita() > 0) {
                citaDAO.eliminar(primera.getIdCita());
            }
            mascotaDAO.eliminar(mascota.getIdMascota());
            clienteDAO.eliminar(cliente.getIdCliente());
        }
    }

    @Test
    void noEliminaClienteConMascotas() {
        String sufijo = String.valueOf(System.nanoTime());
        Cliente cliente = crearCliente(sufijo);
        Mascota mascota = crearMascota(cliente, sufijo);
        try {
            assertTrue(clienteDAO.tieneDependencias(cliente.getIdCliente()));
            assertFalse(clienteDAO.eliminar(cliente.getIdCliente()),
                    "no debe borrar un cliente con mascotas");
        } finally {
            mascotaDAO.eliminar(mascota.getIdMascota());
            clienteDAO.eliminar(cliente.getIdCliente());
        }
    }

    @Test
    void noEliminaMascotaConCitas() {
        String sufijo = String.valueOf(System.nanoTime());
        Cliente cliente = crearCliente(sufijo);
        Mascota mascota = crearMascota(cliente, sufijo);
        Cita cita = new Cita(cliente.getIdCliente(), cliente.getNombre(),
                mascota.getIdMascota(), mascota.getNombre(),
                "2030-03-01T11:00", "Consulta", "Pendiente");
        try {
            assertTrue(citaDAO.insertar(cita));
            assertTrue(mascotaDAO.tieneCitas(mascota.getIdMascota()));
            assertFalse(mascotaDAO.eliminar(mascota.getIdMascota()),
                    "no debe borrar una mascota con citas");
        } finally {
            if (cita.getIdCita() > 0) {
                citaDAO.eliminar(cita.getIdCita());
            }
            mascotaDAO.eliminar(mascota.getIdMascota());
            clienteDAO.eliminar(cliente.getIdCliente());
        }
    }

    @Test
    void rechazaMascotaConClienteInexistente() {
        Mascota mascota = new Mascota("Sin dueño", "Perro", "Mestizo", 1,
                999_999, "Nadie");
        assertFalse(mascotaDAO.insertar(mascota));
    }

    @Test
    void rechazaCitaConMascotaDeOtroCliente() {
        String sufijo = String.valueOf(System.nanoTime());
        Cliente clienteA = crearCliente(sufijo + "a");
        Cliente clienteB = crearCliente(sufijo + "b");
        Mascota mascotaB = crearMascota(clienteB, sufijo + "b");
        try {
            Cita cita = new Cita(clienteA.getIdCliente(), clienteA.getNombre(),
                    mascotaB.getIdMascota(), mascotaB.getNombre(),
                    "2030-04-01T12:00", "Consulta", "Pendiente");
            assertFalse(citaDAO.insertar(cita),
                    "la mascota debe pertenecer al cliente indicado");
        } finally {
            mascotaDAO.eliminar(mascotaB.getIdMascota());
            clienteDAO.eliminar(clienteA.getIdCliente());
            clienteDAO.eliminar(clienteB.getIdCliente());
        }
    }

    private Cliente crearCliente(String sufijo) {
        Cliente cliente = new Cliente("Cliente " + sufijo,
                "integridad." + sufijo + "@example.com", "clave123");
        assertTrue(clienteDAO.insertar(cliente));
        return cliente;
    }

    private Mascota crearMascota(Cliente cliente, String sufijo) {
        Mascota mascota = new Mascota("Mascota " + sufijo, "Perro", "Mestizo",
                3, cliente.getIdCliente(), cliente.getNombre());
        assertTrue(mascotaDAO.insertar(mascota));
        return mascota;
    }
}
