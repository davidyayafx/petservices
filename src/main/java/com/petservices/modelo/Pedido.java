package com.petservices.modelo;

import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private int idPedido;
    private int idCliente;
    private String nombreCliente;
    private String fecha;
    private double total;
    private String estado;
    private int cantidad;
    private int idEstablecimiento;
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
    }

    public Pedido(int idCliente, String nombreCliente, String fecha, double total, String estado) {
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.total = total;
        this.estado = estado;
    }

    public Pedido(int idPedido, int idCliente, String nombreCliente, String fecha, double total, String estado) {
        this(idCliente, nombreCliente, fecha, total, estado);
        this.idPedido = idPedido;
    }

    public Pedido(double total, int cantidad, int idCliente, int idEstablecimiento) {
        this.total = total;
        this.estado = "pendiente";
        this.cantidad = cantidad;
        this.idCliente = idCliente;
        this.idEstablecimiento = idEstablecimiento;
    }

    public Pedido(int idPedido, String fecha, double total, String estado,
                  int cantidad, int idCliente, int idEstablecimiento) {
        this(total, cantidad, idCliente, idEstablecimiento);
        this.idPedido = idPedido;
        this.fecha = fecha;
        this.estado = estado;
    }

    public int getId() {
        return idPedido;
    }

    public void setId(int id) {
        this.idPedido = id;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getIdEstablecimiento() {
        return idEstablecimiento;
    }

    public void setIdEstablecimiento(int idEstablecimiento) {
        this.idEstablecimiento = idEstablecimiento;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles == null ? new ArrayList<>() : detalles;
    }
}
