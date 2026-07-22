package com.puntomaya.service;

import com.puntomaya.util.Conexion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * Genera respaldos de todos los datos del sistema, en dos formatos:
 * - .sql (para restaurar la base completa tal cual, corriéndolo en Workbench)
 * - .csv, uno por tabla (para abrir en Excel y revisar los datos a simple vista)
 */
public class BackupService {

    // Orden importante: primero las tablas sin FK, al final las que dependen de otras.
    private static final String[] TABLAS = {
            "usuario", "cliente", "proveedor", "producto",
            "venta", "detalle_venta", "inventario", "corte_caja",
            "ticket", "abono", "bitacora"
    };

    /** Genera un archivo .sql con todos los datos de todas las tablas. */
    public void generarRespaldoSQL(File archivoDestino) throws IOException {
        try (Connection con = Conexion.conectar();
             PrintWriter escritor = new PrintWriter(new FileWriter(archivoDestino))) {

            escritor.println("-- Respaldo de datos de PuntoMaya");
            escritor.println("-- Generado: " + LocalDateTime.now());
            escritor.println("USE puntomaya;");
            escritor.println("SET FOREIGN_KEY_CHECKS=0;");
            escritor.println();

            for (String tabla : TABLAS) {
                respaldarTablaSQL(con, tabla, escritor);
            }

            escritor.println("SET FOREIGN_KEY_CHECKS=1;");

        } catch (SQLException e) {
            throw new RuntimeException("Error al generar el respaldo", e);
        }
    }

    private void respaldarTablaSQL(Connection con, String tabla, PrintWriter escritor) throws SQLException {
        String sql = "SELECT * FROM " + tabla;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnas = meta.getColumnCount();

            escritor.println("-- Datos de la tabla: " + tabla);
            escritor.println("DELETE FROM " + tabla + ";");

            int filas = 0;
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO ").append(tabla).append(" VALUES (");
                for (int i = 1; i <= columnas; i++) {
                    Object valor = rs.getObject(i);
                    if (i > 1) sb.append(", ");
                    if (valor == null) {
                        sb.append("NULL");
                    } else if (valor instanceof Number || valor instanceof Boolean) {
                        sb.append(valor);
                    } else {
                        String texto = valor.toString().replace("'", "''");
                        sb.append("'").append(texto).append("'");
                    }
                }
                sb.append(");");
                escritor.println(sb);
                filas++;
            }
            escritor.println("-- (" + filas + " filas)");
            escritor.println();
        }
    }

    /** Genera un archivo .csv por cada tabla, dentro de la carpeta indicada. */
    public void exportarCSV(File carpetaDestino) throws IOException {
        try (Connection con = Conexion.conectar()) {
            for (String tabla : TABLAS) {
                File archivo = new File(carpetaDestino, tabla + ".csv");
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT * FROM " + tabla);
                     PrintWriter escritor = new PrintWriter(new FileWriter(archivo))) {

                    ResultSetMetaData meta = rs.getMetaData();
                    int columnas = meta.getColumnCount();

                    StringBuilder encabezado = new StringBuilder();
                    for (int i = 1; i <= columnas; i++) {
                        if (i > 1) encabezado.append(",");
                        encabezado.append(meta.getColumnName(i));
                    }
                    escritor.println(encabezado);

                    while (rs.next()) {
                        StringBuilder fila = new StringBuilder();
                        for (int i = 1; i <= columnas; i++) {
                            Object valor = rs.getObject(i);
                            if (i > 1) fila.append(",");
                            if (valor != null) {
                                String texto = valor.toString().replace("\"", "\"\"");
                                if (texto.contains(",") || texto.contains("\"")) {
                                    texto = "\"" + texto + "\"";
                                }
                                fila.append(texto);
                            }
                        }
                        escritor.println(fila);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al exportar a CSV", e);
        }
    }
}