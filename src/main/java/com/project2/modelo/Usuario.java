package com.project2.modelo;

public class Usuario {
    private int idUsuario;
    private String nombre;
    private String ubicacionActual;
    private double presupuestoMax;

    public Usuario(int idUsuario, String nombre, String ubicacionActual, double presupuestoMax) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.ubicacionActual = ubicacionActual;
        this.presupuestoMax = presupuestoMax;
    }

    public int getIdUsuario() {
        return idUsuario; 
    }
    public String getNombre() {
        return nombre; 
    }
    public String getUbicacionActual() {
        return ubicacionActual; 
    }
    public double getPresupuestoMax() {
        return presupuestoMax; 
    }
}