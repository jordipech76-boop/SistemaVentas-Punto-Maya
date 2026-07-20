package com.puntomaya.util;

/**
 * Funciones reutilizables de validación de datos capturados en pantalla.
 */
public class Validaciones {

    private Validaciones() {
    }

    public static boolean esTextoVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    public static boolean esNumeroValido(String texto) {
        if (esTextoVacio(texto)) return false;
        try {
            Double.parseDouble(texto);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean esPrecioValido(double precio) {
        return precio > 0;
    }

    public static boolean esCantidadValida(double cantidad) {
        return cantidad > 0;
    }
}
