package com.petservices.dao;

import com.petservices.modelo.Servicio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DAO: ServicioDAO — simulación en memoria sobre {@link ConcurrentHashMap},
 * con la misma estrategia que ClienteDAO, MascotaDAO y CitaDAO
 * (hilo-seguro e interfaz {@link IServicioDAO}).
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
public class ServicioDAO implements IServicioDAO {

    private static final ConcurrentHashMap<Long, Servicio> BASE_DATOS = new ConcurrentHashMap<>();
    private static final AtomicLong CONTADOR_ID = new AtomicLong(1);

    static {
        guardarDummy("Consulta", "Vacunación antirrábica", null);
        guardarDummy("Baño y corte", "Grooming completo", null);
        guardarDummy("Cirugía", "Esterilización", null);
    }

    private static void guardarDummy(String tipo, String nombre, Integer idMascota) {
        long id = CONTADOR_ID.getAndIncrement();
        Servicio servicio = new Servicio((int) id, tipo, nombre, idMascota);
        BASE_DATOS.put(id, servicio);
    }

    @Override
    public boolean insertar(Servicio servicio) {
        if (servicio == null) {
            return false;
        }
        long id = CONTADOR_ID.getAndIncrement();
        servicio.setIdServicio((int) id);
        BASE_DATOS.put(id, servicio);
        return true;
    }

    @Override
    public List<Servicio> listarTodos() {
        List<Servicio> lista = new ArrayList<>(BASE_DATOS.values());
        lista.sort(Comparator.comparingInt(Servicio::getIdServicio));
        return lista;
    }

    @Override
    public Servicio buscarPorId(int id) {
        return BASE_DATOS.get((long) id);
    }

    @Override
    public boolean actualizar(Servicio servicio) {
        if (servicio == null || !BASE_DATOS.containsKey((long) servicio.getIdServicio())) {
            return false;
        }
        BASE_DATOS.put((long) servicio.getIdServicio(), servicio);
        return true;
    }

    @Override
    public boolean eliminar(int id) {
        return BASE_DATOS.remove((long) id) != null;
    }
}
