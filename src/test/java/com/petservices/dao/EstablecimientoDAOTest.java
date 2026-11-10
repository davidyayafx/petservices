package com.petservices.dao;

import com.petservices.db.BaseDatos;
import com.petservices.db.BaseDatosPrueba;
import com.petservices.modelo.Establecimiento;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Persistencia y unicidad de establecimientos.
 */
class EstablecimientoDAOTest {

    @BeforeAll
    static void prepararBaseDeDatos() {
        BaseDatosPrueba.configurar();
    }

    private final EstablecimientoDAO dao = new EstablecimientoDAO();

    @Test
    void persisteYActualizaTrasReinicio() {
        String sufijo = String.valueOf(System.nanoTime());
        Establecimiento establecimiento = new Establecimiento(
                "Establecimiento " + sufijo, "veterinaria", "Calle 1 #2-3", "3000000000");
        assertTrue(dao.insertar(establecimiento));
        try {
            BaseDatos.reiniciarParaPruebas();

            Establecimiento recuperado = new EstablecimientoDAO().buscarPorId(
                    establecimiento.getIdEstablecimiento());
            assertNotNull(recuperado);
            assertEquals("Establecimiento " + sufijo, recuperado.getNombre());
            assertEquals("veterinaria", recuperado.getTipo());

            recuperado.setTelefono("3111111111");
            assertTrue(dao.actualizar(recuperado));
            assertEquals("3111111111", dao.buscarPorId(recuperado.getIdEstablecimiento()).getTelefono());
        } finally {
            dao.eliminar(establecimiento.getIdEstablecimiento());
        }
    }

    @Test
    void rechazaNombreDuplicado() {
        String sufijo = String.valueOf(System.nanoTime());
        Establecimiento primero = new Establecimiento(
                "Duplicado " + sufijo, "petshop", "Cra 1", "3000000001");
        assertTrue(dao.insertar(primero));
        try {
            Establecimiento segundo = new Establecimiento(
                    "Duplicado " + sufijo, "mixto", "Cra 2", "3000000002");
            assertFalse(dao.insertar(segundo));
        } finally {
            dao.eliminar(primero.getIdEstablecimiento());
        }
    }
}
