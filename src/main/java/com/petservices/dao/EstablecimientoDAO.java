package com.petservices.dao;

import com.petservices.modelo.Establecimiento;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DAO: EstablecimientoDAO — simulación en memoria sobre {@link ConcurrentHashMap},
 * con la misma estrategia que el resto de DAOs (hilo-seguro e interfaz
 * {@link IEstablecimientoDAO}).
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
public class EstablecimientoDAO implements IEstablecimientoDAO {

    private static final ConcurrentHashMap<Long, Establecimiento> BASE_DATOS = new ConcurrentHashMap<>();
    private static final AtomicLong CONTADOR_ID = new AtomicLong(1);

    static {
        guardarDummy("Veterinaria San Juan", "veterinaria", "Calle 15 #23-45 Tunja", "3204567890");
        guardarDummy("Pet Shop Amigos", "petshop", "Carrera 10 #8-20 Tunja", "3109876543");
        guardarDummy("Groomer Happy Pets", "mixto", "Av. Oriental #45-67 Tunja", "3187654321");
    }

    private static void guardarDummy(String nombre, String tipo, String direccion, String telefono) {
        long id = CONTADOR_ID.getAndIncrement();
        Establecimiento establecimiento = new Establecimiento((int) id, nombre, tipo, direccion, telefono);
        BASE_DATOS.put(id, establecimiento);
    }

    @Override
    public boolean insertar(Establecimiento establecimiento) {
        if (establecimiento == null) {
            return false;
        }
        long id = CONTADOR_ID.getAndIncrement();
        establecimiento.setIdEstablecimiento((int) id);
        BASE_DATOS.put(id, establecimiento);
        return true;
    }

    @Override
    public List<Establecimiento> listarTodos() {
        List<Establecimiento> lista = new ArrayList<>(BASE_DATOS.values());
        lista.sort(Comparator.comparingInt(Establecimiento::getIdEstablecimiento));
        return lista;
    }

    @Override
    public Establecimiento buscarPorId(int id) {
        return BASE_DATOS.get((long) id);
    }

    @Override
    public boolean actualizar(Establecimiento establecimiento) {
        if (establecimiento == null || !BASE_DATOS.containsKey((long) establecimiento.getIdEstablecimiento())) {
            return false;
        }
        BASE_DATOS.put((long) establecimiento.getIdEstablecimiento(), establecimiento);
        return true;
    }

    @Override
    public boolean eliminar(int id) {
        return BASE_DATOS.remove((long) id) != null;
    }
}
