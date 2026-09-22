package com.petservices.modelo;

public class Mascota {

    private int idMascota;
    private String nombre;
    private String especie;
    private String raza;
    private int edad;
    private int idCliente;
    private String nombreCliente;
    private Integer idServicio;

    public Mascota() {
    }

    public Mascota(String nombre, String especie, String raza, int edad,
                   int idCliente, String nombreCliente) {
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.edad = edad;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
    }

    public Mascota(int idMascota, String nombre, String especie, String raza, int edad,
                   int idCliente, String nombreCliente) {
        this(nombre, especie, raza, edad, idCliente, nombreCliente);
        this.idMascota = idMascota;
    }

    public Mascota(int idMascota, String raza, String nombre, int idCliente, Integer idServicio) {
        this.idMascota = idMascota;
        this.raza = raza;
        this.nombre = nombre;
        this.idCliente = idCliente;
        this.idServicio = idServicio;
        this.especie = "No especificada";
        this.edad = 0;
    }

    public int getId() {
        return idMascota;
    }

    public void setId(int id) {
        this.idMascota = id;
    }

    public int getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(int idMascota) {
        this.idMascota = idMascota;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
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

    public Integer getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Integer idServicio) {
        this.idServicio = idServicio;
    }
}
