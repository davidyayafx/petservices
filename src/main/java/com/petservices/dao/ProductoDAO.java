package com.petservices.dao;

import com.petservices.modelo.Producto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ProductoDAO implements IProductoDAO {

    private static final ConcurrentHashMap<Long, Producto> BASE_DATOS = new ConcurrentHashMap<>();
    private static final AtomicLong CONTADOR_ID = new AtomicLong(1);

    static {
        guardarDummy("Croquetas Pedigree Adulto 10kg", "Alimento", 45000.00, 30);
        guardarDummy("Shampoo Antipulgas", "Medicamento", 18000.00, 50);
        guardarDummy("Collar Reflectivo", "Accesorio", 25000.00, 15);
        guardarDummy("Juguete Mordedor", "Juguete", 22000.00, 40);
    }

    private static void guardarDummy(String nombre, String categoria, double precio, int stock) {
        long id = CONTADOR_ID.getAndIncrement();
        Producto producto = new Producto((int) id, nombre, categoria, precio, stock);
        BASE_DATOS.put(id, producto);
    }

    @Override
    public boolean insertar(Producto producto) {
        if (producto == null) {
            return false;
        }
        long id = CONTADOR_ID.getAndIncrement();
        producto.setIdProducto((int) id);
        if (producto.getEstado() == null || producto.getEstado().trim().isEmpty()) {
            producto.setEstado("disponible");
        }
        BASE_DATOS.put(id, producto);
        return true;
    }

    @Override
    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>(BASE_DATOS.values());
        lista.sort(Comparator.comparingInt(Producto::getIdProducto));
        return lista;
    }

    @Override
    public Producto buscarPorId(int id) {
        return BASE_DATOS.get((long) id);
    }

    @Override
    public boolean actualizar(Producto producto) {
        if (producto == null || !BASE_DATOS.containsKey((long) producto.getIdProducto())) {
            return false;
        }
        BASE_DATOS.put((long) producto.getIdProducto(), producto);
        return true;
    }

    @Override
    public boolean eliminar(int id) {
        return BASE_DATOS.remove((long) id) != null;
    }
}
