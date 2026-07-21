package com.puntomaya.dao;

import com.puntomaya.model.Bitacora;
import com.puntomaya.util.Conexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla BITACORA (registro de auditoría).
 */
public class BitacoraDAO {

    /** Guarda una acción en la bitácora (ej. "Canceló la venta #48"). */
    public void guardar(Bitacora registro) {
        String sql = "INSERT INTO bitacora (id_usuario, accion, fecha) VALUES (?, ?, ?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, registro.getIdUsuario());
            ps.setString(2, registro.getAccion());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar en la bitácora", e);
        }
    }

    /** Trae todo el historial de la bitácora, del más reciente al más viejo. */
    public List<Bitacora> listarTodo() {
        String sql = "SELECT * FROM bitacora ORDER BY fecha DESC";
        List<Bitacora> registros = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Bitacora b = new Bitacora();
                b.setId(rs.getInt("id_bitacora"));
                b.setIdUsuario(rs.getInt("id_usuario"));
                b.setAccion(rs.getString("accion"));
                b.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                registros.add(b);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar la bitácora", e);
        }
        return registros;
    }
}