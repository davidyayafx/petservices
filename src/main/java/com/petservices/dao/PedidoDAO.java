package com.petservices.dao;

import com.petservices.modelo.DetallePedido;
import com.petservices.modelo.Pedido;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PedidoDAO implements IPedidoDAO {

    private static final ConcurrentHashMap<Long, Pedido> BASE_DATOS = new ConcurrentHashMap<>();
    private static final AtomicLong CONTADOR_ID = new AtomicLong(1);

    static {
        Pedido pedido1 = new Pedido((int) CONTADOR_ID.getAndIncrement(), 1, "Alejandro Puerto",
                "2026-08-10", 90000.00, "Pagado");
        pedido1.setCantidad(2);
        pedido1.setDetalles(new ArrayList<>());
        pedido1.getDetalles().add(new DetallePedido(1, "Croquetas Pedigree Adulto 10kg", 2, 45000.00));
        BASE_DATOS.put((long) pedido1.getIdPedido(), pedido1);

        Pedido pedido2 = new Pedido((int) CONTADOR_ID.getAndIncrement(), 2, "Daniel Mejia",
                "2026-08-12", 25000.00, "Entregado");
        pedido2.setCantidad(1);
        pedido2.setDetalles(new ArrayList<>());
        pedido2.getDetalles().add(new DetallePedido(3, "Collar Reflectivo", 1, 25000.00));
        BASE_DATOS.put((long) pedido2.getIdPedido(), pedido2);
    }

    @Override
    public boolean insertar(Pedido pedido) {
        if (pedido == null) {
            return false;
        }
        long id = CONTADOR_ID.getAndIncrement();
        pedido.setIdPedido((int) id);
        if (pedido.getFecha() == null || pedido.getFecha().trim().isEmpty()) {
            pedido.setFecha(LocalDate.now().toString());
        }
        if (pedido.getEstado() == null || pedido.getEstado().trim().isEmpty()) {
            pedido.setEstado("Pendiente");
        }
        BASE_DATOS.put(id, pedido);
        return true;
    }

    @Override
    public List<Pedido> listarTodos() {
        List<Pedido> lista = new ArrayList<>(BASE_DATOS.values());
        lista.sort(Comparator.comparingInt(Pedido::getIdPedido));
        return lista;
    }

    @Override
    public Pedido buscarPorId(int id) {
        return BASE_DATOS.get((long) id);
    }

    @Override
    public boolean actualizar(Pedido pedido) {
        if (pedido == null || !BASE_DATOS.containsKey((long) pedido.getIdPedido())) {
            return false;
        }
        BASE_DATOS.put((long) pedido.getIdPedido(), pedido);
        return true;
    }

    @Override
    public boolean eliminar(int id) {
        return BASE_DATOS.remove((long) id) != null;
    }
}
