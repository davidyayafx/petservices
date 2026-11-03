package com.petservices.dao;

import com.petservices.modelo.Cliente;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ClienteDAO implements IClienteDAO {

    private static final ConcurrentHashMap<Long, Cliente> BASE_DATOS = new ConcurrentHashMap<>();
    private static final AtomicLong CONTADOR_ID = new AtomicLong(1);

    static {
        guardarDummy("Alejandro Puerto", "alejandro@gmail.com", "123456", "2026-01-15");
        guardarDummy("Daniel Mejia", "daniel@gmail.com", "123456", "2026-01-16");
        guardarDummy("David Yaya", "david@gmail.com", "123456", "2026-01-17");
    }

    private static void guardarDummy(String nombre, String correo, String contrasena, String fechaRegistro) {
        long id = CONTADOR_ID.getAndIncrement();
        Cliente cliente = new Cliente((int) id, nombre, correo, contrasena, fechaRegistro);
        BASE_DATOS.put(id, cliente);
    }

    @Override
    public boolean insertar(Cliente cliente) {
        if (cliente == null || cliente.getCorreo() == null || buscarPorCorreo(cliente.getCorreo()) != null) {
            return false;
        }

        long id = CONTADOR_ID.getAndIncrement();
        cliente.setIdCliente((int) id);
        cliente.setFechaRegistro(LocalDate.now().toString());
        BASE_DATOS.put(id, cliente);
        return true;
    }

    @Override
    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>(BASE_DATOS.values());
        lista.sort(Comparator.comparingInt(Cliente::getIdCliente));
        return lista;
    }

    @Override
    public Cliente buscarPorId(int id) {
        return BASE_DATOS.get((long) id);
    }

    @Override
    public Cliente buscarPorCorreo(String correo) {
        if (correo == null) {
            return null;
        }
        for (Cliente cliente : BASE_DATOS.values()) {
            if (cliente.getCorreo() != null && cliente.getCorreo().equalsIgnoreCase(correo.trim())) {
                return cliente;
            }
        }
        return null;
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        if (cliente == null || !BASE_DATOS.containsKey((long) cliente.getIdCliente())) {
            return false;
        }

        // El objeto recibido suele ser la MISMA instancia guardada en el mapa, así que
        // ya trae el correo nuevo: hay que comparar contra los demás ids, no con
        // buscarPorCorreo (que devolvería el propio cliente y ocultaría el duplicado).
        for (Cliente otro : BASE_DATOS.values()) {
            if (otro.getIdCliente() != cliente.getIdCliente()
                    && otro.getCorreo() != null
                    && cliente.getCorreo() != null
                    && otro.getCorreo().equalsIgnoreCase(cliente.getCorreo().trim())) {
                return false;
            }
        }

        BASE_DATOS.put((long) cliente.getIdCliente(), cliente);
        return true;
    }

    @Override
    public boolean eliminar(int id) {
        return BASE_DATOS.remove((long) id) != null;
    }

    @Override
    public Cliente validarLogin(String correo, String contrasena) {
        Cliente cliente = buscarPorCorreo(correo);
        if (cliente == null) {
            return null;
        }
        return cliente.getContrasena() != null && cliente.getContrasena().equals(contrasena) ? cliente : null;
    }
}
