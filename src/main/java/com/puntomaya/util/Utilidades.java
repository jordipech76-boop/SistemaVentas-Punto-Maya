package com.puntomaya.util;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Funciones generales reutilizables: formato de moneda, fechas, etc.
 */
public class Utilidades {

    private static final NumberFormat FORMATO_MONEDA =
            NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    public static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Utilidades() {
    }

    public static String formatearMoneda(double valor) {
        return FORMATO_MONEDA.format(valor);
    }
}
