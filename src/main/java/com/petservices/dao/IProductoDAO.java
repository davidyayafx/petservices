package com.petservices.dao;

import com.petservices.modelo.Producto;

import java.util.List;

public interface IProductoDAO {

    boolean insertar(Producto producto);

    List<Producto> listarTodos();

    Producto buscarPorId(int id);

    boolean actualizar(Producto producto);

    boolean eliminar(int id);
}
