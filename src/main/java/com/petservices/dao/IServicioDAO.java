package com.petservices.dao;

import com.petservices.modelo.Servicio;

import java.util.List;

/**
 * Contrato del DAO de servicios (catálogo de tipos de servicio).
 *
 * Proyecto: PetServices - SENA GA7-220501096-AA5-EV03
 */
public interface IServicioDAO {

    boolean insertar(Servicio servicio);

    List<Servicio> listarTodos();

    Servicio buscarPorId(int id);

    boolean actualizar(Servicio servicio);

    boolean eliminar(int id);
}
