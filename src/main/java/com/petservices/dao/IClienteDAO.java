package com.petservices.dao;

import com.petservices.modelo.Cliente;

import java.util.List;

public interface IClienteDAO {

    boolean insertar(Cliente cliente);

    List<Cliente> listarTodos();

    Cliente buscarPorId(int id);

    Cliente buscarPorCorreo(String correo);

    boolean actualizar(Cliente cliente);

    boolean eliminar(int id);

    Cliente validarLogin(String correo, String contrasena);

    /** Indica si el cliente tiene mascotas o citas que impiden su borrado. */
    boolean tieneDependencias(int id);
}
