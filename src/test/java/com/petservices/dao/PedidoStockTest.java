package com.petservices.dao;

import com.petservices.db.BaseDatos;
import com.petservices.db.BaseDatosPrueba;
import com.petservices.modelo.Cliente;
import com.petservices.modelo.DetallePedido;
import com.petservices.modelo.Pedido;
import com.petservices.modelo.Producto;
import com.petservices.service.PedidoService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del stock transaccional y de la persistencia de pedidos/detalles.
 */
class PedidoStockTest {

    @BeforeAll
    static void prepararBaseDeDatos() {
        BaseDatosPrueba.configurar();
    }

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final PedidoService pedidoService = new PedidoService();

    @Test
    void crearDescuentaStockYElPedidoSobreviveAlReinicio() {
        String sufijo = String.valueOf(System.nanoTime());
        Producto producto = crearProducto("Pedido " + sufijo, 10, 1000.0);
        Pedido pedido = nuevoPedido(producto, 2, "2032-01-10");
        try {
            pedidoService.crear(pedido);

            assertTrue(pedido.getIdPedido() > 0);
            assertEquals(2, pedido.getCantidad());
            assertEquals(2000.0, pedido.getTotal());
            assertEquals(8, productoDAO.buscarPorId(producto.getIdProducto()).getStock());

            Pedido guardado = pedidoDAO.buscarPorId(pedido.getIdPedido());
            assertNotNull(guardado);
            assertEquals(1, guardado.getDetalles().size());
            assertEquals(producto.getIdProducto(), guardado.getDetalles().get(0).getIdProducto());
            assertEquals(1000.0, guardado.getDetalles().get(0).getPrecioUnitario());
            assertEquals(2000.0, guardado.getDetalles().get(0).getSubtotal());

            BaseDatos.reiniciarParaPruebas();

            Pedido recuperado = new PedidoDAO().buscarPorId(pedido.getIdPedido());
            assertNotNull(recuperado);
            assertEquals(2000.0, recuperado.getTotal());
            assertEquals(8, new ProductoDAO().buscarPorId(producto.getIdProducto()).getStock());
        } finally {
            if (pedido.getIdPedido() > 0) {
                pedidoService.eliminar(pedido.getIdPedido());
            }
            productoDAO.eliminar(producto.getIdProducto());
        }
    }

    @Test
    void rechazaStockInsuficienteSinCrearNiDescontar() {
        String sufijo = String.valueOf(System.nanoTime());
        Producto producto = crearProducto("Sin stock " + sufijo, 1, 500.0);
        Pedido pedido = nuevoPedido(producto, 2, "2032-01-11");
        int pedidosAntes = pedidoDAO.listarTodos().size();
        try {
            assertThrows(PedidoService.StockInsuficienteException.class,
                    () -> pedidoService.crear(pedido));
            assertEquals(1, productoDAO.buscarPorId(producto.getIdProducto()).getStock());
            assertEquals(pedidosAntes, pedidoDAO.listarTodos().size());
        } finally {
            productoDAO.eliminar(producto.getIdProducto());
        }
    }

    @Test
    void editarAjustaStockYTotal() {
        String sufijo = String.valueOf(System.nanoTime());
        Producto producto = crearProducto("Editar " + sufijo, 10, 1000.0);
        Pedido pedido = nuevoPedido(producto, 2, "2032-01-12");
        try {
            pedidoService.crear(pedido);
            assertEquals(8, productoDAO.buscarPorId(producto.getIdProducto()).getStock());

            Pedido cambio = nuevoPedido(producto, 3, "2032-01-12");
            Pedido actualizado = pedidoService.actualizarConDetalle(pedido.getIdPedido(), cambio);

            assertEquals(3, actualizado.getCantidad());
            assertEquals(3000.0, actualizado.getTotal());
            assertEquals(7, productoDAO.buscarPorId(producto.getIdProducto()).getStock());
        } finally {
            if (pedido.getIdPedido() > 0) {
                pedidoService.eliminar(pedido.getIdPedido());
            }
            productoDAO.eliminar(producto.getIdProducto());
        }
    }

    @Test
    void editarSinStockRevierteElCambioCompleto() {
        String sufijo = String.valueOf(System.nanoTime());
        Producto producto = crearProducto("Reversion " + sufijo, 5, 1000.0);
        Pedido pedido = nuevoPedido(producto, 2, "2032-01-13");
        try {
            pedidoService.crear(pedido);
            assertEquals(3, productoDAO.buscarPorId(producto.getIdProducto()).getStock());

            Pedido cambio = nuevoPedido(producto, 6, "2032-01-13");
            assertThrows(PedidoService.StockInsuficienteException.class,
                    () -> pedidoService.actualizarConDetalle(pedido.getIdPedido(), cambio));

            Pedido original = pedidoDAO.buscarPorId(pedido.getIdPedido());
            assertEquals(2, original.getCantidad());
            assertEquals(2000.0, original.getTotal());
            assertEquals(3, productoDAO.buscarPorId(producto.getIdProducto()).getStock());
        } finally {
            if (pedido.getIdPedido() > 0) {
                pedidoService.eliminar(pedido.getIdPedido());
            }
            productoDAO.eliminar(producto.getIdProducto());
        }
    }

    @Test
    void eliminarDevuelveStockYPedidoManualNoTocaInventario() {
        String sufijo = String.valueOf(System.nanoTime());
        Producto producto = crearProducto("Eliminar " + sufijo, 10, 700.0);
        Pedido pedido = nuevoPedido(producto, 4, "2032-01-14");
        try {
            pedidoService.crear(pedido);
            assertEquals(6, productoDAO.buscarPorId(producto.getIdProducto()).getStock());

            assertTrue(pedidoService.eliminar(pedido.getIdPedido()));
            assertEquals(10, productoDAO.buscarPorId(producto.getIdProducto()).getStock());
            assertNull(pedidoDAO.buscarPorId(pedido.getIdPedido()));
            pedido.setIdPedido(0);

            int stockAntes = productoDAO.buscarPorId(producto.getIdProducto()).getStock();
            Pedido manual = new Pedido();
            manual.setIdCliente(1);
            manual.setNombreCliente("Alejandro Puerto");
            manual.setFecha("2032-01-15");
            manual.setTotal(1500.0);
            manual.setEstado("Pendiente");
            pedidoService.crearSinDetalle(manual);

            assertEquals(stockAntes,
                    productoDAO.buscarPorId(producto.getIdProducto()).getStock());
            assertTrue(pedidoService.eliminar(manual.getIdPedido()));
        } finally {
            if (pedido.getIdPedido() > 0) {
                pedidoService.eliminar(pedido.getIdPedido());
            }
            productoDAO.eliminar(producto.getIdProducto());
        }
    }

    @Test
    void elDetalleConservaElPrecioHistorico() {
        String sufijo = String.valueOf(System.nanoTime());
        Producto producto = crearProducto("Historico " + sufijo, 10, 1000.0);
        Pedido pedido = nuevoPedido(producto, 2, "2032-01-16");
        try {
            pedidoService.crear(pedido);

            Producto actual = productoDAO.buscarPorId(producto.getIdProducto());
            actual.setPrecio(2500.0);
            assertTrue(productoDAO.actualizar(actual));

            Pedido guardado = pedidoDAO.buscarPorId(pedido.getIdPedido());
            assertEquals(1000.0, guardado.getDetalles().get(0).getPrecioUnitario());
            assertEquals(2000.0, guardado.getTotal());
        } finally {
            if (pedido.getIdPedido() > 0) {
                pedidoService.eliminar(pedido.getIdPedido());
            }
            productoDAO.eliminar(producto.getIdProducto());
        }
    }

    private Producto crearProducto(String nombre, int stock, double precio) {
        Producto producto = new Producto(nombre, "Alimento", precio, stock);
        assertTrue(productoDAO.insertar(producto));
        return producto;
    }

    private Pedido nuevoPedido(Producto producto, int cantidad, String fecha) {
        Cliente cliente = clienteDAO.buscarPorId(1);
        assertNotNull(cliente, "las semillas deben incluir el cliente 1");
        Pedido pedido = new Pedido(cliente.getIdCliente(), cliente.getNombre(), fecha, 0, "Pendiente");
        pedido.setDetalles(new ArrayList<>());
        pedido.getDetalles().add(new DetallePedido(
                producto.getIdProducto(), producto.getNombre(), cantidad, producto.getPrecio()));
        pedido.setCantidad(cantidad);
        return pedido;
    }
}
