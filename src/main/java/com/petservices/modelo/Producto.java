package com.petservices.modelo;

public class Producto {

    private int idProducto;
    private String nombre;
    private String categoria;
    private double precio;
    private int stock;
    private String descripcion;
    private String estado;
    private int idEstablecimiento;
    private Integer idCliente;

    public Producto() {
    }

    public Producto(String nombre, String categoria, double precio, int stock) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.descripcion = categoria;
        this.estado = "disponible";
    }

    public Producto(int idProducto, String nombre, String categoria, double precio, int stock) {
        this(nombre, categoria, precio, stock);
        this.idProducto = idProducto;
    }

    public Producto(String nombre, String descripcion, double precio, int stock, int idEstablecimiento) {
        this(nombre, descripcion, precio, stock);
        this.idEstablecimiento = idEstablecimiento;
        this.descripcion = descripcion;
        this.categoria = descripcion;
    }

    public Producto(int idProducto, String nombre, String descripcion, double precio,
                    String estado, int stock, int idEstablecimiento, Integer idCliente) {
        this(nombre, descripcion, precio, stock, idEstablecimiento);
        this.idProducto = idProducto;
        this.estado = estado;
        this.idCliente = idCliente;
    }

    public int getId() {
        return idProducto;
    }

    public void setId(int id) {
        this.idProducto = id;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
        this.descripcion = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        if (this.categoria == null || this.categoria.trim().isEmpty()) {
            this.categoria = descripcion;
        }
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdEstablecimiento() {
        return idEstablecimiento;
    }

    public void setIdEstablecimiento(int idEstablecimiento) {
        this.idEstablecimiento = idEstablecimiento;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }
}
