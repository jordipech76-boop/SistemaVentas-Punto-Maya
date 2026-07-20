package com.puntomaya.dao;

import com.puntomaya.model.Cliente;
import com.puntomaya.util.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla CLIENTE.
 */
public class ClienteDAO {

    public List<Cliente> listar() {
        String sql = "SELECT * FROM cliente ORDER BY nombre";
        List<Cliente> clientes = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientes.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar clientes", e);
        }
        return clientes;
    }

    public List<Cliente> listarConAdeudo() {
        String sql = "SELECT * FROM cliente WHERE saldo_actual > 0 ORDER BY saldo_actual DESC";
        List<Cliente> clientes = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientes.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar clientes con adeudo", e);
        }
        return clientes;
    }

    public Optional<Cliente> buscarPorId(int id) {
        String sql = "SELECT * FROM cliente WHERE id_cliente = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente por id", e);
        }
        return Optional.empty();
    }

    public List<Cliente> buscarPorNombre(String texto) {
        String sql = "SELECT * FROM cliente WHERE nombre LIKE ? ORDER BY nombre";
        List<Cliente> clientes = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar clientes por nombre", e);
        }
        return clientes;
    }

    public void guardar(Cliente cliente) {
        String sql = "INSERT INTO cliente (nombre, telefono, limite_credito, saldo_actual) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.setDouble(3, cliente.getLimiteCredito());
            ps.setDouble(4, cliente.getSaldoActual());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar cliente", e);
        }
    }

    public void actualizar(Cliente cliente) {
        String sql = "UPDATE cliente SET nombre = ?, telefono = ?, limite_credito = ?, "
                + "saldo_actual = ? WHERE id_cliente = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.setDouble(3, cliente.getLimiteCredito());
            ps.setDouble(4, cliente.getSaldoActual());
            ps.setInt(5, cliente.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cliente", e);
        }
    }

    /**
     * Suma (venta a crédito) o resta (abono) el saldo del cliente.
     */
    public void ajustarSaldo(int idCliente, double diferencia) {
        String sql = "UPDATE cliente SET saldo_actual = saldo_actual + ? WHERE id_cliente = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, diferencia);
            ps.setInt(2, idCliente);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al ajustar saldo del cliente", e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM cliente WHERE id_cliente = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar cliente", e);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("nombre"));
        c.setTelefono(rs.getString("telefono"));
        c.setLimiteCredito(rs.getDouble("limite_credito"));
        c.setSaldoActual(rs.getDouble("saldo_actual"));
        return c;
    }
}
