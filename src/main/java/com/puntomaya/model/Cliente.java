package com.puntomaya.model;

/**
 * Representa a un cliente que puede comprar de contado o a crédito (fiado).
 */
public class Cliente {

    private int id;
    private String nombre;
    private String telefono;
    private double limiteCredito;
    private double saldoActual;

    public Cliente() {
    }

    public Cliente(int id, String nombre, String telefono, double limiteCredito, double saldoActual) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.limiteCredito = limiteCredito;
        this.saldoActual = saldoActual;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public double getLimiteCredito() {
        return limiteCredito;
    }

    public void setLimiteCredito(double limiteCredito) {
        this.limiteCredito = limiteCredito;
    }

    public double getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(double saldoActual) {
        this.saldoActual = saldoActual;
    }

    /**
     * Indica si el cliente puede fiar "monto" más sin rebasar su límite de crédito.
     */
    public boolean puedeComprarFiado(double monto) {
        return (saldoActual + monto) <= limiteCredito;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
