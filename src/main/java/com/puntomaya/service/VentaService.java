package com.puntomaya.service;

import com.puntomaya.dao.BitacoraDAO;
import com.puntomaya.dao.ClienteDAO;
import com.puntomaya.dao.ProductoDAO;
import com.puntomaya.dao.TicketDAO;
import com.puntomaya.dao.VentaDAO;
import com.puntomaya.model.Bitacora;
import com.puntomaya.model.Cliente;
import com.puntomaya.model.DetalleVenta;
import com.puntomaya.model.Producto;
import com.puntomaya.model.Ticket;
import com.puntomaya.model.Venta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio para registrar una venta:
 * - Verificar stock suficiente.
 * - Calcular el total.
 * - Validar límite de crédito si es fiado.
 * - Descontar inventario.
 * - Guardar la venta.
 * - Generar su ticket.
 */
public class VentaService {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final BitacoraDAO bitacoraDAO = new BitacoraDAO();

    public Ticket realizarVenta(Venta venta) {
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta no puede estar vacía");
        }

        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = productoDAO.buscarPorId(detalle.getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            if (producto.getStock() < detalle.getCantidad()) {
                throw new IllegalStateException(
                        "No hay suficiente existencia de " + producto.getNombre()
                                + " (disponible: " + producto.getStock() + ")");
            }
        }

        if (venta.isEsFiado()) {
            if (venta.getIdCliente() == null) {
                throw new IllegalArgumentException("Debes seleccionar un cliente para vender a crédito");
            }
            Cliente cliente = clienteDAO.buscarPorId(venta.getIdCliente())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
            if (!cliente.puedeComprarFiado(venta.getTotal())) {
                throw new IllegalStateException(
                        "El cliente " + cliente.getNombre() + " rebasaría su límite de crédito");
            }
        }

        venta.recalcularTotales();
        ventaDAO.guardar(venta);

        for (DetalleVenta detalle : venta.getDetalles()) {
            productoDAO.ajustarStock(detalle.getIdProducto(), -detalle.getCantidad());
        }

        if (venta.isEsFiado()) {
            clienteDAO.ajustarSaldo(venta.getIdCliente(), venta.getTotal());
        }

        Ticket ticket = new Ticket(venta.getId(), LocalDateTime.now());
        ticketDAO.guardar(ticket);

        return ticket;
    }

    public Optional<Venta> buscarPorId(int id) {
        return ventaDAO.buscarPorId(id);
    }

    public List<Venta> listarPorFecha(java.time.LocalDate fecha) {
        return ventaDAO.listarPorFecha(fecha);
    }

    public void cancelarVenta(int idVenta, int idUsuarioQueCancela) {
        Venta venta = ventaDAO.buscarPorId(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        for (DetalleVenta detalle : venta.getDetalles()) {
            productoDAO.ajustarStock(detalle.getIdProducto(), detalle.getCantidad());
        }

        if (venta.isEsFiado() && venta.getIdCliente() != null) {
            clienteDAO.ajustarSaldo(venta.getIdCliente(), -venta.getTotal());
        }

        ventaDAO.cancelar(idVenta);

        bitacoraDAO.guardar(new Bitacora(idUsuarioQueCancela, "Canceló la venta #" + idVenta));
    }
}