package com.puntomaya.model;

/**
 * Representa una línea dentro de una venta: un producto, su cantidad y su importe.
 */
public class DetalleVenta {

    private int id;
    private int idVenta;
    private int idProducto;
    private String nombreProducto; // solo para mostrar en pantalla, no se guarda aparte
    private double cantidad;
    private double precioUnitario;
    private double importe;

    public DetalleVenta() {
    }

    public DetalleVenta(int idProducto, String nombreProducto, double cantidad, double precioUnitario) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.importe = calcularImporte();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
        this.importe = calcularImporte();
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        this.importe = calcularImporte();
    }

    public double getImporte() {
        return importe;
    }

    public void setImporte(double importe) {
        this.importe = importe;
    }

    public double calcularImporte() {
        return cantidad * precioUnitario;
    }
}
