package com.petservices.dao;

import com.petservices.modelo.Mascota;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MascotaDAO implements IMascotaDAO {

    private static final ConcurrentHashMap<Long, Mascota> BASE_DATOS = new ConcurrentHashMap<>();
    private static final AtomicLong CONTADOR_ID = new AtomicLong(1);

    static {
        guardarDummy("Max", "Perro", "Labrador", 5, 1, "Alejandro Puerto");
        guardarDummy("Luna", "Gato", "Siamés", 3, 2, "Daniel Mejia");
        guardarDummy("Rocky", "Perro", "Golden Retriever", 4, 1, "Alejandro Puerto");
        guardarDummy("Thor", "Perro", "Pastor Alemán", 2, 3, "David Yaya");
    }

    private static void guardarDummy(String nombre, String especie, String raza, int edad,
                                     int idCliente, String nombreCliente) {
        long id = CONTADOR_ID.getAndIncrement();
        Mascota mascota = new Mascota((int) id, nombre, especie, raza, edad, idCliente, nombreCliente);
        BASE_DATOS.put(id, mascota);
    }

    @Override
    public boolean insertar(Mascota mascota) {
        if (mascota == null) {
            return false;
        }
        long id = CONTADOR_ID.getAndIncrement();
        mascota.setIdMascota((int) id);
        BASE_DATOS.put(id, mascota);
        return true;
    }

    @Override
    public List<Mascota> listarTodos() {
        List<Mascota> lista = new ArrayList<>(BASE_DATOS.values());
        lista.sort(Comparator.comparingInt(Mascota::getIdMascota));
        return lista;
    }

    @Override
    public Mascota buscarPorId(int id) {
        return BASE_DATOS.get((long) id);
    }

    @Override
    public List<Mascota> buscarPorCliente(int idCliente) {
        List<Mascota> resultado = new ArrayList<>();
        for (Mascota mascota : listarTodos()) {
            if (mascota.getIdCliente() == idCliente) {
                resultado.add(mascota);
            }
        }
        return resultado;
    }

    @Override
    public boolean actualizar(Mascota mascota) {
        if (mascota == null || !BASE_DATOS.containsKey((long) mascota.getIdMascota())) {
            return false;
        }
        BASE_DATOS.put((long) mascota.getIdMascota(), mascota);
        return true;
    }

    @Override
    public boolean eliminar(int id) {
        return BASE_DATOS.remove((long) id) != null;
    }
}
