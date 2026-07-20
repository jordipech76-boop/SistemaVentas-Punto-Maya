package com.puntomaya;

import com.puntomaya.util.Conexion;
import java.sql.Connection;

public class TestConexion {
    public static void main(String[] args) {
        try (Connection con = Conexion.conectar()) {
            System.out.println(" Conexión exitosa a MySQL: " + con.isValid(2));
        } catch (Exception e) {
            System.out.println(" Error al conectar:");
            e.printStackTrace();
        }
    }
}
