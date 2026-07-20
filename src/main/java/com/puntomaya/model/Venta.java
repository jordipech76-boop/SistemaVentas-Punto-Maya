package com.puntomaya.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una venta (el "encabezado"); sus productos viven en DetalleVenta.
 */
public class Venta {

    private int id;
    private LocalDateTime fecha;
    private double subtotal;
    private double descuento;
    private double total;
    private FormaPago formaPago;
    private boolean esFiado;
    private boolean cancelada;
    private Integer idCliente; // puede ser null si la venta es de contado
    private int idUsuario;
    private List<DetalleVenta> detalles = new ArrayList<>();

    public Venta() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public boolean isEsFiado() {
        return esFiado;
    }

    public void setEsFiado(boolean esFiado) {
        this.esFiado = esFiado;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    public void setCancelada(boolean cancelada) {
        this.cancelada = cancelada;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    public void agregarDetalle(DetalleVenta detalle) {
        this.detalles.add(detalle);
        recalcularTotales();
    }

    public void quitarDetalle(DetalleVenta detalle) {
        this.detalles.remove(detalle);
        recalcularTotales();
    }

    /**
     * Recalcula subtotal y total a partir de los detalles actuales.
     */
    public void recalcularTotales() {
        this.subtotal = detalles.stream().mapToDouble(DetalleVenta::getImporte).sum();
        this.total = subtotal - descuento;
    }
}
