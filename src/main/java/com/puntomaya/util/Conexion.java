package com.puntomaya.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase responsable de abrir la conexión a la base de datos MySQL.
 *
 * IMPORTANTE: ajusta USUARIO y PASSWORD según tu instalación de MySQL
 * (si usas MySQL Workbench con instalación normal, usa el usuario y
 * contraseña que configuraste al instalar MySQL; si usas XAMPP, el
 * usuario suele ser "root" con password vacío "").
 */
public class Conexion {

    private static final String HOST = "localhost";
    private static final int PUERTO = 3306;
    private static final String BASE_DATOS = "puntomaya";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "8014a8899";

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE_DATOS
                    + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private Conexion() {
        // Clase de utilidad: no se instancia
    }

    /**
     * Abre y regresa una nueva conexión a la base de datos.
     * Cada DAO abre su propia conexión y la cierra al terminar (try-with-resources).
     */
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
