package com.puntomaya.dao;

import com.puntomaya.model.Ticket;
import com.puntomaya.util.Conexion;

import java.sql.*;
import java.util.Optional;

/**
 * Acceso a datos de la tabla TICKET.
 */
public class TicketDAO {

    /** Guarda el ticket de una venta y le asigna el folio que genera MySQL. */
    public void guardar(Ticket ticket) {
        String sql = "INSERT INTO ticket (id_venta, fecha_emision) VALUES (?, ?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, ticket.getIdVenta());
            ps.setTimestamp(2, Timestamp.valueOf(ticket.getFechaEmision()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    ticket.setFolio(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el ticket", e);
        }
    }

    /** Busca el ticket que corresponde a una venta específica. */
    public Optional<Ticket> buscarPorVenta(int idVenta) {
        String sql = "SELECT * FROM ticket WHERE id_venta = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ticket t = new Ticket();
                    t.setFolio(rs.getInt("folio"));
                    t.setIdVenta(rs.getInt("id_venta"));
                    t.setFechaEmision(rs.getTimestamp("fecha_emision").toLocalDateTime());
                    return Optional.of(t);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el ticket", e);
        }
        return Optional.empty();
    }
}