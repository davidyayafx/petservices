package com.petservices.dao;

import com.petservices.modelo.Pedido;

import java.util.List;

public interface IPedidoDAO {

    boolean insertar(Pedido pedido);

    List<Pedido> listarTodos();

    Pedido buscarPorId(int id);

    boolean actualizar(Pedido pedido);

    boolean eliminar(int id);
}
