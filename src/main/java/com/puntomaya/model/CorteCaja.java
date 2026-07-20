package com.puntomaya.model;

import java.time.LocalDate;

/**
 * Representa el corte de caja de un turno: compara el efectivo esperado
 * (según lo vendido) contra el efectivo realmente contado.
 */
public class CorteCaja {

    private int id;
    private LocalDate fecha;
    private String turno;
    private int idUsuario;
    private double fondoInicial;
    private double efectivoEsperado;
    private double efectivoReal;
    private double diferencia;

    public CorteCaja() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public double getFondoInicial() {
        return fondoInicial;
    }

    public void setFondoInicial(double fondoInicial) {
        this.fondoInicial = fondoInicial;
    }

    public double getEfectivoEsperado() {
        return efectivoEsperado;
    }

    public void setEfectivoEsperado(double efectivoEsperado) {
        this.efectivoEsperado = efectivoEsperado;
    }

    public double getEfectivoReal() {
        return efectivoReal;
    }

    public void setEfectivoReal(double efectivoReal) {
        this.efectivoReal = efectivoReal;
        this.diferencia = efectivoReal - efectivoEsperado;
    }

    public double getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(double diferencia) {
        this.diferencia = diferencia;
    }

    public boolean cuadra() {
        return Math.abs(diferencia) < 0.01;
    }
}
