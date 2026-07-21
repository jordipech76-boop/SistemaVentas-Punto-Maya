package com.puntomaya.model;

import java.time.LocalDateTime;

/**
 * Representa el comprobante que se le da al cliente al terminar una venta.
 * Cada venta tiene exactamente un ticket (relación 1 a 1).
 */
public class Ticket {

    private int folio;
    private int idVenta;
    private LocalDateTime fechaEmision;

    public Ticket() {
    }

    public Ticket(int idVenta, LocalDateTime fechaEmision) {
        this.idVenta = idVenta;
        this.fechaEmision = fechaEmision;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
}