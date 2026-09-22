package com.petservices.dao;

import com.petservices.modelo.Cita;

import java.util.List;

public interface ICitaDAO {

    boolean insertar(Cita cita);

    List<Cita> listarTodos();

    Cita buscarPorId(int id);

    boolean actualizar(Cita cita);

    boolean eliminar(int id);
}
