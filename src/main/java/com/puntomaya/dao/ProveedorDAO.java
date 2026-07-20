package com.puntomaya.dao;

import com.puntomaya.model.Proveedor;
import com.puntomaya.util.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla PROVEEDOR.
 */
public class ProveedorDAO {

    public List<Proveedor> listar() {
        String sql = "SELECT * FROM proveedor ORDER BY nombre";
        List<Proveedor> proveedores = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                proveedores.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar proveedores", e);
        }
        return proveedores;
    }

    public Optional<Proveedor> buscarPorId(int id) {
        String sql = "SELECT * FROM proveedor WHERE id_proveedor = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar proveedor por id", e);
        }
        return Optional.empty();
    }

    public void guardar(Proveedor proveedor) {
        String sql = "INSERT INTO proveedor (nombre) VALUES (?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, proveedor.getNombre());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    proveedor.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar proveedor", e);
        }
    }

    public void actualizar(Proveedor proveedor) {
        String sql = "UPDATE proveedor SET nombre = ? WHERE id_proveedor = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, proveedor.getNombre());
            ps.setInt(2, proveedor.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar proveedor", e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM proveedor WHERE id_proveedor = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar proveedor", e);
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getInt("id_proveedor"));
        p.setNombre(rs.getString("nombre"));
        return p;
    }
}
