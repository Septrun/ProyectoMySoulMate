package com.project2.modelo;

public class Restaurante {
    private int idRestaurante;
    private String nombre;
    private String direccion;
    private double calificacion;
    private String rangoPrecio;

    public Restaurante(int idRestaurante, String nombre, String direccion, double calificacion, String rangoPrecio) {
        this.idRestaurante = idRestaurante;
        this.nombre = nombre;
        this.direccion = direccion;
        this.calificacion = calificacion;
        this.rangoPrecio = rangoPrecio;
    }

    public int getIdRestaurante() {
        return idRestaurante; 
    }
    public String getNombre() {
        return nombre; 
    }
    public String getDireccion() {
        return direccion; 
    }
    public double getCalificacion() {
        return calificacion; 
    }
    public String getRangoPrecio() {
        return rangoPrecio; 
    }
}