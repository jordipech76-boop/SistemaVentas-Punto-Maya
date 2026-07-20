package com.puntomaya.model;

import java.time.LocalDate;

/**
 * Representa un producto del catálogo de PuntoMaya.
 * Esta clase NO consulta la base de datos, solo guarda los datos.
 */
public class Producto {

    private int id;
    private String codigoBarras;
    private String nombre;
    private double precioVenta;
    private double precioCosto;
    private double stock;
    private boolean esGranel;
    private double puntoReorden;
    private LocalDate fechaCaducidad;

    public Producto() {
    }

    public Producto(int id, String codigoBarras, String nombre, double precioVenta,
                     double precioCosto, double stock, boolean esGranel,
                     double puntoReorden, LocalDate fechaCaducidad) {
        this.id = id;
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.precioCosto = precioCosto;
        this.stock = stock;
        this.esGranel = esGranel;
        this.puntoReorden = puntoReorden;
        this.fechaCaducidad = fechaCaducidad;
    }

    // ---- Getters y setters ----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public double getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(double precioCosto) {
        this.precioCosto = precioCosto;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public boolean isEsGranel() {
        return esGranel;
    }

    public void setEsGranel(boolean esGranel) {
        this.esGranel = esGranel;
    }

    public double getPuntoReorden() {
        return puntoReorden;
    }

    public void setPuntoReorden(double puntoReorden) {
        this.puntoReorden = puntoReorden;
    }

    public LocalDate getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(LocalDate fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    /**
     * Ganancia unitaria = precio de venta - precio de costo.
     */
    public double calcularGanancia() {
        return precioVenta - precioCosto;
    }

    public boolean estaEnStockBajo() {
        return stock <= puntoReorden;
    }

    @Override
    public String toString() {
        return nombre + " - $" + precioVenta;
    }
}
