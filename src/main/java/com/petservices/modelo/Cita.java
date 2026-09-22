package com.petservices.modelo;

public class Cita {

    private int idCita;
    private int idCliente;
    private String nombreCliente;
    private int idMascota;
    private String nombreMascota;
    private String fechaHora;
    private String servicio;
    private String estado;
    private int idEstablecimiento;

    public Cita() {
    }

    public Cita(int idCliente, String nombreCliente, int idMascota, String nombreMascota,
                String fechaHora, String servicio, String estado) {
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.idMascota = idMascota;
        this.nombreMascota = nombreMascota;
        this.fechaHora = fechaHora;
        this.servicio = servicio;
        this.estado = estado;
    }

    public Cita(int idCita, int idCliente, String nombreCliente, int idMascota, String nombreMascota,
                String fechaHora, String servicio, String estado) {
        this(idCliente, nombreCliente, idMascota, nombreMascota, fechaHora, servicio, estado);
        this.idCita = idCita;
    }

    public Cita(String fecha, String hora, int idMascota, int idEstablecimiento) {
        this.fechaHora = construirFechaHora(fecha, hora);
        this.idMascota = idMascota;
        this.idEstablecimiento = idEstablecimiento;
        this.estado = "pendiente";
        this.servicio = "Consulta";
    }

    public Cita(int idCita, String fecha, String hora, String estado, int idMascota, int idEstablecimiento) {
        this(fecha, hora, idMascota, idEstablecimiento);
        this.idCita = idCita;
        this.estado = estado;
    }

    public int getId() {
        return idCita;
    }

    public void setId(int id) {
        this.idCita = id;
    }

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(int idCita) {
        this.idCita = idCita;
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

    public int getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(int idMascota) {
        this.idMascota = idMascota;
    }

    public String getNombreMascota() {
        return nombreMascota;
    }

    public void setNombreMascota(String nombreMascota) {
        this.nombreMascota = nombreMascota;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getFecha() {
        if (fechaHora == null || !fechaHora.contains("T")) {
            return fechaHora;
        }
        return fechaHora.substring(0, fechaHora.indexOf('T'));
    }

    public void setFecha(String fecha) {
        this.fechaHora = construirFechaHora(fecha, getHora());
    }

    public String getHora() {
        if (fechaHora == null || !fechaHora.contains("T")) {
            return "";
        }
        return fechaHora.substring(fechaHora.indexOf('T') + 1);
    }

    public void setHora(String hora) {
        this.fechaHora = construirFechaHora(getFecha(), hora);
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
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

    private static String construirFechaHora(String fecha, String hora) {
        String fechaSegura = fecha == null ? "" : fecha.trim();
        String horaSegura = hora == null || hora.trim().isEmpty() ? "00:00" : hora.trim();
        return fechaSegura + "T" + horaSegura;
    }
}
