package com.puntomaya.dao;

import com.puntomaya.model.CorteCaja;
import com.puntomaya.util.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla CORTE_CAJA.
 */
public class CorteCajaDAO {

    public void guardar(CorteCaja corte) {
        String sql = "INSERT INTO corte_caja (fecha, turno, id_usuario, fondo_inicial, "
                + "efectivo_esperado, efectivo_real, diferencia) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1, Date.valueOf(corte.getFecha()));
            ps.setString(2, corte.getTurno());
            ps.setInt(3, corte.getIdUsuario());
            ps.setDouble(4, corte.getFondoInicial());
            ps.setDouble(5, corte.getEfectivoEsperado());
            ps.setDouble(6, corte.getEfectivoReal());
            ps.setDouble(7, corte.getDiferencia());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    corte.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar corte de caja", e);
        }
    }

    public List<CorteCaja> listarPorUsuario(int idUsuario) {
        String sql = "SELECT * FROM corte_caja WHERE id_usuario = ? ORDER BY fecha DESC";
        List<CorteCaja> cortes = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cortes.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar cortes de caja por usuario", e);
        }
        return cortes;
    }

    public List<CorteCaja> listarTodos() {
        String sql = "SELECT * FROM corte_caja ORDER BY fecha DESC";
        List<CorteCaja> cortes = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cortes.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar cortes de caja", e);
        }
        return cortes;
    }

    private CorteCaja mapear(ResultSet rs) throws SQLException {
        CorteCaja c = new CorteCaja();
        c.setId(rs.getInt("id_corte"));
        c.setFecha(rs.getDate("fecha").toLocalDate());
        c.setTurno(rs.getString("turno"));
        c.setIdUsuario(rs.getInt("id_usuario"));
        c.setFondoInicial(rs.getDouble("fondo_inicial"));
        c.setEfectivoEsperado(rs.getDouble("efectivo_esperado"));
        c.setEfectivoReal(rs.getDouble("efectivo_real"));
        c.setDiferencia(rs.getDouble("diferencia"));
        return c;
    }
}
