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

    /**
     * Los movimientos más recientes de TODOS los productos, con nombres
     * legibles (no solo IDs), para mostrar en la pantalla de Inventario.
     */
    public List<MovimientoDetalle> listarRecientes(int limite) {
        String sql = "SELECT i.fecha, p.nombre AS producto, pr.nombre AS proveedor, "
                + "i.tipo, i.cantidad, i.motivo "
                + "FROM inventario i "
                + "JOIN producto p ON p.id_producto = i.id_producto "
                + "LEFT JOIN proveedor pr ON pr.id_proveedor = i.id_proveedor "
                + "ORDER BY i.fecha DESC LIMIT ?";
        List<MovimientoDetalle> resultado = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new MovimientoDetalle(
                            rs.getTimestamp("fecha").toLocalDateTime(),
                            rs.getString("producto"),
                            rs.getString("proveedor"),
                            rs.getString("tipo"),
                            rs.getDouble("cantidad"),
                            rs.getString("motivo")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar movimientos recientes", e);
        }
        return resultado;
    }

    /** Fila de historial ya con nombres legibles, lista para mostrar en pantalla. */
    public static class MovimientoDetalle {
        private final java.time.LocalDateTime fecha;
        private final String producto;
        private final String proveedor;
        private final String tipo;
        private final double cantidad;
        private final String motivo;

        public MovimientoDetalle(java.time.LocalDateTime fecha, String producto, String proveedor,
                                 String tipo, double cantidad, String motivo) {
            this.fecha = fecha;
            this.producto = producto;
            this.proveedor = proveedor;
            this.tipo = tipo;
            this.cantidad = cantidad;
            this.motivo = motivo;
        }

        public java.time.LocalDateTime getFecha() { return fecha; }
        public String getProducto() { return producto; }
        public String getProveedor() { return proveedor; }
        public String getTipo() { return tipo; }
        public double getCantidad() { return cantidad; }
        public String getMotivo() { return motivo; }
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
