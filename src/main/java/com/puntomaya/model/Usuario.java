package com.puntomaya.model;

/**
 * Representa a un usuario del sistema (administrador, cajero o almacenista).
 */
public class Usuario {

    private int id;
    private String nombre;
    private String nombreUsuario;
    private String contrasena;
    private RolUsuario rol;
    private boolean activo;

    public Usuario() {
    }

    public Usuario(int id, String nombre, String nombreUsuario, String contrasena,
                    RolUsuario rol, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.activo = activo;
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

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean esAdministrador() {
        return rol == RolUsuario.ADMINISTRADOR;
    }

    public boolean esCajero() {
        return rol == RolUsuario.CAJERO;
    }

    public boolean esAlmacenista() {
        return rol == RolUsuario.ALMACENISTA;
    }

    @Override
    public String toString() {
        return nombre + " (" + rol + ")";
    }
}
