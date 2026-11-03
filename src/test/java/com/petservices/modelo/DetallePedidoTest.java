package com.petservices.modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del detalle de pedido (subtotal calculado).
 */
class DetallePedidoTest {

    @Test
    void calculaElSubtotalAlConstruir() {
        DetallePedido detalle = new DetallePedido(1, "Croquetas", 3, 45_000.0);
        assertEquals(135_000.0, detalle.getSubtotal(), 0.001);
    }

    @Test
    void recalculaElSubtotalAlCambiarCantidadOPrecio() {
        DetallePedido detalle = new DetallePedido(1, "Croquetas", 2, 10_000.0);
        detalle.setCantidad(5);
        assertEquals(50_000.0, detalle.getSubtotal(), 0.001);
        detalle.setPrecioUnitario(8_000.0);
        assertEquals(40_000.0, detalle.getSubtotal(), 0.001);
    }
}
