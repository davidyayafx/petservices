package com.petservices.dao;

import com.petservices.modelo.Mascota;

import java.util.List;

public interface IMascotaDAO {

    boolean insertar(Mascota mascota);

    List<Mascota> listarTodos();

    Mascota buscarPorId(int id);

    List<Mascota> buscarPorCliente(int idCliente);

    boolean actualizar(Mascota mascota);

    boolean eliminar(int id);

    /** Indica si la mascota tiene citas que impiden su borrado. */
    boolean tieneCitas(int id);
}
