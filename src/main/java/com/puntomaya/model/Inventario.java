package com.puntomaya.model;

import java.time.LocalDateTime;

/**
 * Representa un movimiento de inventario: una entrada de mercancía,
 * una merma, una devolución a proveedor o un ajuste manual.
 */
public class Inventario {

    private int id;
    private int idProducto;
    private Integer idProveedor; // solo aplica para ENTRADA y DEVOLUCION
    private TipoMovimiento tipo;
    private double cantidad;
    private String motivo; // usado en MERMA y AJUSTE
    private LocalDateTime fecha;
    private int idUsuario;

    public Inventario() {
    }

    public Inventario(int idProducto, Integer idProveedor, TipoMovimiento tipo,
                       double cantidad, String motivo, int idUsuario) {
        this.idProducto = idProducto;
        this.idProveedor = idProveedor;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.idUsuario = idUsuario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Indica si este movimiento suma al stock (true) o lo resta (false).
     */
    public boolean sumaAlStock() {
        return tipo == TipoMovimiento.ENTRADA;
    }
}
