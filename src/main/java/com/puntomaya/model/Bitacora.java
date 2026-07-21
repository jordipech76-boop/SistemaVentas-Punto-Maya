package com.puntomaya.model;

import java.time.LocalDateTime;

/**
 * Registro de auditoría: guarda quién hizo una acción importante
 * (por ejemplo, cancelar una venta ya cobrada) y cuándo, para
 * poder rastrearlo después si hace falta.
 */
public class Bitacora {

    private int id;
    private int idUsuario;
    private String accion;
    private LocalDateTime fecha;

    public Bitacora() {
    }

    public Bitacora(int idUsuario, String accion) {
        this.idUsuario = idUsuario;
        this.accion = accion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}