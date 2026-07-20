package com.puntomaya.util;

import com.puntomaya.model.Usuario;

/**
 * Guarda el usuario que inició sesión, para que cualquier controlador
 * sepa quién es y qué rol tiene sin tener que pasarlo de pantalla en pantalla.
 */
public class SesionActual {

    private static Usuario usuario;

    private SesionActual() {
    }

    public static void iniciar(Usuario u) {
        usuario = u;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void cerrar() {
        usuario = null;
    }
}
