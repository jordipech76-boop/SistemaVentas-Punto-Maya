package com.puntomaya.dao;

import com.puntomaya.model.Inventario;
import com.puntomaya.model.TipoMovimiento;
import com.puntomaya.util.Conexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla INVENTARIO (entradas, mermas, devoluciones, ajustes).
 */
public class InventarioDAO {

    public void guardar(Inventario movimiento) {
        String sql = "INSERT INTO inventario (id_producto, id_proveedor, tipo, cantidad, "
                + "motivo, fecha, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, movimiento.getIdProducto());
            if (movimiento.getIdProveedor() != null) {
                ps.setInt(2, movimiento.getIdProveedor());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, movimiento.getTipo().name());
            ps.setDouble(4, movimiento.getCantidad());
            ps.setString(5, movimiento.getMotivo());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(7, movimiento.getIdUsuario());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    movimiento.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar movimiento de inventario", e);
        }
    }

    public List<Inventario> listarPorProducto(int idProducto) {
        String sql = "SELECT * FROM inventario WHERE id_producto = ? ORDER BY fecha DESC";
        List<Inventario> movimientos = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar movimientos de inventario", e);
        }
        return movimientos;
    }

    public List<Inventario> listarPorTipo(TipoMovimiento tipo) {
        String sql = "SELECT * FROM inventario WHERE tipo = ? ORDER BY fecha DESC";
        List<Inventario> movimientos = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tipo.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar movimientos por tipo", e);
        }
        return movimientos;
    }

    private Inventario mapear(ResultSet rs) throws SQLException {
        Inventario m = new Inventario();
        m.setId(rs.getInt("id_movimiento"));
        m.setIdProducto(rs.getInt("id_producto"));
        int idProveedor = rs.getInt("id_proveedor");
        m.setIdProveedor(rs.wasNull() ? null : idProveedor);
        m.setTipo(TipoMovimiento.valueOf(rs.getString("tipo")));
        m.setCantidad(rs.getDouble("cantidad"));
        m.setMotivo(rs.getString("motivo"));
        m.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        m.setIdUsuario(rs.getInt("id_usuario"));
        return m;
    }
}
