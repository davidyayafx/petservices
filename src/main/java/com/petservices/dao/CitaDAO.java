package com.petservices.dao;

import com.petservices.modelo.Cita;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CitaDAO implements ICitaDAO {

    private static final ConcurrentHashMap<Long, Cita> BASE_DATOS = new ConcurrentHashMap<>();
    private static final AtomicLong CONTADOR_ID = new AtomicLong(1);

    static {
        guardarDummy(1, "Alejandro Puerto", 1, "Max", "2026-08-20T10:00", "Consulta", "Confirmada");
        guardarDummy(2, "Daniel Mejia", 2, "Luna", "2026-08-22T14:30", "Vacunación", "Pendiente");
        guardarDummy(1, "Alejandro Puerto", 3, "Rocky", "2026-08-25T09:00", "Baño/Peluquería", "Pendiente");
        guardarDummy(3, "David Yaya", 4, "Thor", "2026-08-28T11:00", "Cirugía", "Cancelada");
    }

    private static void guardarDummy(int idCliente, String nombreCliente, int idMascota, String nombreMascota,
                                     String fechaHora, String servicio, String estado) {
        long id = CONTADOR_ID.getAndIncrement();
        Cita cita = new Cita((int) id, idCliente, nombreCliente, idMascota, nombreMascota, fechaHora, servicio, estado);
        BASE_DATOS.put(id, cita);
    }

    @Override
    public boolean insertar(Cita cita) {
        if (cita == null) {
            return false;
        }
        long id = CONTADOR_ID.getAndIncrement();
        cita.setIdCita((int) id);
        if (cita.getEstado() == null || cita.getEstado().trim().isEmpty()) {
            cita.setEstado("Pendiente");
        }
        BASE_DATOS.put(id, cita);
        return true;
    }

    @Override
    public List<Cita> listarTodos() {
        List<Cita> lista = new ArrayList<>(BASE_DATOS.values());
        lista.sort(Comparator.comparingInt(Cita::getIdCita));
        return lista;
    }

    @Override
    public Cita buscarPorId(int id) {
        return BASE_DATOS.get((long) id);
    }

    @Override
    public boolean actualizar(Cita cita) {
        if (cita == null || !BASE_DATOS.containsKey((long) cita.getIdCita())) {
            return false;
        }
        BASE_DATOS.put((long) cita.getIdCita(), cita);
        return true;
    }

    @Override
    public boolean eliminar(int id) {
        return BASE_DATOS.remove((long) id) != null;
    }
}
