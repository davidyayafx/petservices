package com.petservices.dao;

import com.petservices.db.BaseDatos;
import com.petservices.db.BaseDatosPrueba;
import com.petservices.modelo.Producto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Persistencia y reglas del inventario en SQLite.
 */
class ProductoDAOTest {

    @BeforeAll
    static void prepararBaseDeDatos() {
        BaseDatosPrueba.configurar();
    }

    private final ProductoDAO dao = new ProductoDAO();

    @Test
    void persisteProductoYCambiosTrasReinicio() {
        String sufijo = String.valueOf(System.nanoTime());
        Producto producto = new Producto("Producto " + sufijo, "Alimento", 12000.0, 5);
        assertTrue(dao.insertar(producto));
        assertTrue(producto.getIdProducto() > 0);
        try {
            assertEquals("disponible", producto.getEstado());

            producto.setPrecio(15000.0);
            producto.setStock(0);
            assertTrue(dao.actualizar(producto));
            assertEquals("agotado", dao.buscarPorId(producto.getIdProducto()).getEstado());

            BaseDatos.reiniciarParaPruebas();

            Producto recuperado = new ProductoDAO().buscarPorId(producto.getIdProducto());
            assertNotNull(recuperado);
            assertEquals("Producto " + sufijo, recuperado.getNombre());
            assertEquals(15000.0, recuperado.getPrecio());
            assertEquals(0, recuperado.getStock());
            assertEquals("agotado", recuperado.getEstado());
        } finally {
            dao.eliminar(producto.getIdProducto());
        }
    }

    @Test
    void rechazaNombreDuplicado() {
        String sufijo = String.valueOf(System.nanoTime());
        Producto primero = new Producto("Duplicado " + sufijo, "Juguete", 5000.0, 1);
        assertTrue(dao.insertar(primero));
        try {
            Producto segundo = new Producto("Duplicado " + sufijo, "Juguete", 6000.0, 2);
            assertFalse(dao.insertar(segundo));
        } finally {
            dao.eliminar(primero.getIdProducto());
        }
    }

    @Test
    void calculaElEstadoSegunElStock() {
        String sufijo = String.valueOf(System.nanoTime());
        Producto producto = new Producto("Estado " + sufijo, "Accesorio", 8000.0, 0);
        assertTrue(dao.insertar(producto));
        try {
            assertEquals("agotado", producto.getEstado());
            producto.setStock(4);
            assertTrue(dao.actualizar(producto));
            assertEquals("disponible", dao.buscarPorId(producto.getIdProducto()).getEstado());
        } finally {
            dao.eliminar(producto.getIdProducto());
        }
    }
}
