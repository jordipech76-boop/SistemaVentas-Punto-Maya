package com.puntomaya.dao;

import com.puntomaya.model.DetalleVenta;
import com.puntomaya.model.FormaPago;
import com.puntomaya.model.Venta;
import com.puntomaya.util.Conexion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de las tablas VENTA y DETALLE_VENTA.
 * Guardar una venta implica guardar el encabezado y todas sus líneas
 * en una sola transacción (o se guarda todo, o no se guarda nada).
 */
public class VentaDAO {

    /**
     * Guarda una venta junto con todos sus detalles, en una sola transacción.
     */
    public void guardar(Venta venta) {
        String sqlVenta = "INSERT INTO venta (fecha, subtotal, descuento, total, forma_pago, "
                + "es_fiado, cancelada, id_cliente, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, "
                + "precio_unitario, importe) VALUES (?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = Conexion.conectar();
            con.setAutoCommit(false);

            try (PreparedStatement psVenta = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                psVenta.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                psVenta.setDouble(2, venta.getSubtotal());
                psVenta.setDouble(3, venta.getDescuento());
                psVenta.setDouble(4, venta.getTotal());
                psVenta.setString(5, venta.getFormaPago().name());
                psVenta.setBoolean(6, venta.isEsFiado());
                psVenta.setBoolean(7, false);
                if (venta.getIdCliente() != null) {
                    psVenta.setInt(8, venta.getIdCliente());
                } else {
                    psVenta.setNull(8, Types.INTEGER);
                }
                psVenta.setInt(9, venta.getIdUsuario());
                psVenta.executeUpdate();

                try (ResultSet rs = psVenta.getGeneratedKeys()) {
                    if (rs.next()) {
                        venta.setId(rs.getInt(1));
                    }
                }
            }

            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                for (DetalleVenta d : venta.getDetalles()) {
                    psDetalle.setInt(1, venta.getId());
                    psDetalle.setInt(2, d.getIdProducto());
                    psDetalle.setDouble(3, d.getCantidad());
                    psDetalle.setDouble(4, d.getPrecioUnitario());
                    psDetalle.setDouble(5, d.getImporte());
                    psDetalle.addBatch();
                }
                psDetalle.executeBatch();
            }

            con.commit();

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Error al revertir la venta", ex);
                }
            }
            throw new RuntimeException("Error al guardar la venta", e);
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public Optional<Venta> buscarPorId(int id) {
        String sqlVenta = "SELECT * FROM venta WHERE id_venta = ?";
        String sqlDetalle = "SELECT dv.*, p.nombre AS nombre_producto FROM detalle_venta dv "
                + "JOIN producto p ON p.id_producto = dv.id_producto WHERE dv.id_venta = ?";

        try (Connection con = Conexion.conectar()) {

            Venta venta = null;
            try (PreparedStatement ps = con.prepareStatement(sqlVenta)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        venta = mapearVenta(rs);
                    }
                }
            }

            if (venta == null) {
                return Optional.empty();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        venta.agregarDetalle(mapearDetalle(rs));
                    }
                }
            }

            return Optional.of(venta);

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar venta por id", e);
        }
    }

    public List<Venta> listarPorFecha(LocalDate fecha) {
        String sql = "SELECT * FROM venta WHERE DATE(fecha) = ? AND cancelada = FALSE ORDER BY fecha";
        List<Venta> ventas = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapearVenta(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar ventas por fecha", e);
        }
        return ventas;
    }

    /**
     * Suma el total de ventas en efectivo de un rango de fechas/horas (usado en corte de caja).
     */
    public double sumarVentasEfectivo(LocalDateTime desde, LocalDateTime hasta) {
        String sql = "SELECT COALESCE(SUM(total), 0) AS total FROM venta "
                + "WHERE forma_pago = 'EFECTIVO' AND cancelada = FALSE AND fecha BETWEEN ? AND ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al sumar ventas en efectivo", e);
        }
        return 0;
    }

    /**
     * Marca una venta como cancelada (no la borra, para dejar rastro/auditoría).
     */
    public void cancelar(int idVenta) {
        String sql = "UPDATE venta SET cancelada = TRUE WHERE id_venta = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al cancelar la venta", e);
        }
    }

    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setId(rs.getInt("id_venta"));
        v.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        v.setSubtotal(rs.getDouble("subtotal"));
        v.setDescuento(rs.getDouble("descuento"));
        v.setTotal(rs.getDouble("total"));
        v.setFormaPago(FormaPago.valueOf(rs.getString("forma_pago")));
        v.setEsFiado(rs.getBoolean("es_fiado"));
        v.setCancelada(rs.getBoolean("cancelada"));
        int idCliente = rs.getInt("id_cliente");
        v.setIdCliente(rs.wasNull() ? null : idCliente);
        v.setIdUsuario(rs.getInt("id_usuario"));
        return v;
    }

    private DetalleVenta mapearDetalle(ResultSet rs) throws SQLException {
        DetalleVenta d = new DetalleVenta();
        d.setId(rs.getInt("id_detalle"));
        d.setIdVenta(rs.getInt("id_venta"));
        d.setIdProducto(rs.getInt("id_producto"));
        d.setNombreProducto(rs.getString("nombre_producto"));
        d.setCantidad(rs.getDouble("cantidad"));
        d.setPrecioUnitario(rs.getDouble("precio_unitario"));
        d.setImporte(rs.getDouble("importe"));
        return d;
    }
}
