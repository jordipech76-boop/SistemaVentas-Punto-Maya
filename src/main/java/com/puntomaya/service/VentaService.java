package com.puntomaya.service;

import com.puntomaya.dao.ClienteDAO;
import com.puntomaya.dao.ProductoDAO;
import com.puntomaya.dao.VentaDAO;
import com.puntomaya.model.Cliente;
import com.puntomaya.model.DetalleVenta;
import com.puntomaya.model.Producto;
import com.puntomaya.model.Venta;

import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio para registrar una venta:
 * - Verificar stock suficiente.
 * - Calcular el total.
 * - Validar límite de crédito si es fiado.
 * - Descontar inventario.
 * - Guardar la venta.
 *
 * Esta clase usa los DAO, pero no contiene SQL directamente.
 */
public class VentaService {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    /**
     * Registra una venta completa: valida, descuenta inventario, ajusta saldo del
     * cliente si es fiado, y guarda todo. Regresa true si la venta se realizó con éxito.
     */
    public boolean realizarVenta(Venta venta) {
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta no puede estar vacía");
        }

        // 1. Verificar stock suficiente de cada producto
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = productoDAO.buscarPorId(detalle.getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            if (producto.getStock() < detalle.getCantidad()) {
                throw new IllegalStateException(
                        "No hay suficiente existencia de " + producto.getNombre()
                                + " (disponible: " + producto.getStock() + ")");
            }
        }

        // 2. Si es fiado, validar límite de crédito del cliente
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

        // 3. Calcular totales (por si no se recalcularon antes de llamar aquí)
        venta.recalcularTotales();

        // 4. Guardar venta + detalles (transacción en el DAO)
        ventaDAO.guardar(venta);

        // 5. Descontar inventario de cada producto vendido
        for (DetalleVenta detalle : venta.getDetalles()) {
            productoDAO.ajustarStock(detalle.getIdProducto(), -detalle.getCantidad());
        }

        // 6. Si es fiado, aumentar el saldo pendiente del cliente
        if (venta.isEsFiado()) {
            clienteDAO.ajustarSaldo(venta.getIdCliente(), venta.getTotal());
        }

        return true;
    }

    public Optional<Venta> buscarPorId(int id) {
        return ventaDAO.buscarPorId(id);
    }

    public List<Venta> listarPorFecha(java.time.LocalDate fecha) {
        return ventaDAO.listarPorFecha(fecha);
    }

    /**
     * Cancela una venta ya cobrada (solo debe permitirse a rol Administrador;
     * esa validación de permiso se hace en el Controller antes de llamar aquí).
     * Repone el inventario y revierte el saldo del cliente si era fiado.
     */
    public void cancelarVenta(int idVenta) {
        Venta venta = ventaDAO.buscarPorId(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        for (DetalleVenta detalle : venta.getDetalles()) {
            productoDAO.ajustarStock(detalle.getIdProducto(), detalle.getCantidad());
        }

        if (venta.isEsFiado() && venta.getIdCliente() != null) {
            clienteDAO.ajustarSaldo(venta.getIdCliente(), -venta.getTotal());
        }

        ventaDAO.cancelar(idVenta);
    }
}
