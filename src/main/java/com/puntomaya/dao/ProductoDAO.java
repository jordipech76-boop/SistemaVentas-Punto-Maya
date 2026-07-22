package com.puntomaya.dao;

import com.puntomaya.model.Producto;
import com.puntomaya.util.Conexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla PRODUCTO. Aquí vive todo el SQL relacionado
 * a productos: SELECT, INSERT, UPDATE, DELETE.
 */
public class ProductoDAO {

    public List<Producto> listar() {
        String sql = "SELECT * FROM producto ORDER BY nombre";
        List<Producto> productos = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos", e);
        }
        return productos;
    }

    public Optional<Producto> buscarPorId(int id) {
        String sql = "SELECT * FROM producto WHERE id_producto = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto por id", e);
        }
        return Optional.empty();
    }

    public Optional<Producto> buscarPorCodigoBarras(String codigoBarras) {
        String sql = "SELECT * FROM producto WHERE codigo_barras = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigoBarras);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto por código de barras", e);
        }
        return Optional.empty();
    }

    public List<Producto> buscarPorNombre(String texto) {
        String sql = "SELECT * FROM producto WHERE nombre LIKE ? ORDER BY nombre";
        List<Producto> productos = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar productos por nombre", e);
        }
        return productos;
    }

    public List<Producto> listarStockBajo() {
        String sql = "SELECT * FROM producto WHERE stock <= punto_reorden ORDER BY stock ASC";
        List<Producto> productos = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos con stock bajo", e);
        }
        return productos;
    }

    /**
     * Productos con fecha de caducidad dentro de los próximos "dias", o ya vencidos.
     * Para la alerta que pidió Beto (evitar que se pierda producto por caducidad).
     */
    public List<Producto> listarPorCaducar(int dias) {
        String sql = "SELECT * FROM producto WHERE fecha_caducidad IS NOT NULL "
                + "AND fecha_caducidad <= DATE_ADD(CURDATE(), INTERVAL ? DAY) ORDER BY fecha_caducidad ASC";
        List<Producto> productos = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, dias);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos por caducar", e);
        }
        return productos;
    }

    public void guardar(Producto producto) {
        String sql = "INSERT INTO producto (codigo_barras, nombre, precio_venta, precio_costo, "
                + "stock, es_granel, punto_reorden, fecha_caducidad) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            establecerParametros(ps, producto);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    producto.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar producto", e);
        }
    }

    public void actualizar(Producto producto) {
        String sql = "UPDATE producto SET codigo_barras = ?, nombre = ?, precio_venta = ?, "
                + "precio_costo = ?, stock = ?, es_granel = ?, punto_reorden = ?, "
                + "fecha_caducidad = ? WHERE id_producto = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            establecerParametros(ps, producto);
            ps.setInt(9, producto.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar producto", e);
        }
    }

    /**
     * Suma o resta existencia de un producto directamente en la base
     * (usado por InventarioDAO/VentaDAO al registrar movimientos).
     */
    public void ajustarStock(int idProducto, double diferencia) {
        String sql = "UPDATE producto SET stock = stock + ? WHERE id_producto = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, diferencia);
            ps.setInt(2, idProducto);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al ajustar stock del producto", e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM producto WHERE id_producto = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar producto", e);
        }
    }

    // ---- helpers privados ----

    private void establecerParametros(PreparedStatement ps, Producto p) throws SQLException {
        ps.setString(1, p.getCodigoBarras());
        ps.setString(2, p.getNombre());
        ps.setDouble(3, p.getPrecioVenta());
        ps.setDouble(4, p.getPrecioCosto());
        ps.setDouble(5, p.getStock());
        ps.setBoolean(6, p.isEsGranel());
        ps.setDouble(7, p.getPuntoReorden());
        if (p.getFechaCaducidad() != null) {
            ps.setDate(8, Date.valueOf(p.getFechaCaducidad()));
        } else {
            ps.setNull(8, Types.DATE);
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id_producto"));
        p.setCodigoBarras(rs.getString("codigo_barras"));
        p.setNombre(rs.getString("nombre"));
        p.setPrecioVenta(rs.getDouble("precio_venta"));
        p.setPrecioCosto(rs.getDouble("precio_costo"));
        p.setStock(rs.getDouble("stock"));
        p.setEsGranel(rs.getBoolean("es_granel"));
        p.setPuntoReorden(rs.getDouble("punto_reorden"));
        Date fechaCad = rs.getDate("fecha_caducidad");
        p.setFechaCaducidad(fechaCad != null ? fechaCad.toLocalDate() : null);
        return p;
    }
}
