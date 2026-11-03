package com.petservices.dao;

import com.petservices.modelo.Establecimiento;

import java.util.List;

/**
 * Contrato del DAO de establecimientos (petshop, veterinaria, mixto).
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
public interface IEstablecimientoDAO {

    boolean insertar(Establecimiento establecimiento);

    List<Establecimiento> listarTodos();

    Establecimiento buscarPorId(int id);

    boolean actualizar(Establecimiento establecimiento);

    boolean eliminar(int id);
}
