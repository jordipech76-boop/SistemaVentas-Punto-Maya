package com.puntomaya.service;

import com.puntomaya.dao.InventarioDAO;
import com.puntomaya.dao.ProductoDAO;
import com.puntomaya.model.Inventario;
import com.puntomaya.model.Producto;
import com.puntomaya.model.TipoMovimiento;

import java.util.List;

/**
 * Reglas de negocio para el manejo de inventario: entradas de mercancía,
 * mermas, devoluciones a proveedor y ajustes manuales.
 */
public class InventarioService {

    private final InventarioDAO inventarioDAO = new InventarioDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    /**
     * Registra la llegada de mercancía de un proveedor y aumenta el stock del producto.
     */
    public void registrarEntrada(int idProducto, int idProveedor, double cantidad, int idUsuario) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        Inventario movimiento = new Inventario(idProducto, idProveedor, TipoMovimiento.ENTRADA,
                cantidad, null, idUsuario);
        inventarioDAO.guardar(movimiento);
        productoDAO.ajustarStock(idProducto, cantidad);
    }

    /**
     * Registra una merma (roto, caducado, robado) y descuenta el stock del producto.
     */
    public void registrarMerma(int idProducto, double cantidad, String motivo, int idUsuario) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debes indicar el motivo de la merma");
        }
        Inventario movimiento = new Inventario(idProducto, null, TipoMovimiento.MERMA,
                cantidad, motivo, idUsuario);
        inventarioDAO.guardar(movimiento);
        productoDAO.ajustarStock(idProducto, -cantidad);
    }

    /**
     * Registra una devolución de producto a un proveedor y descuenta el stock.
     */
    public void registrarDevolucion(int idProducto, int idProveedor, double cantidad,
                                     String motivo, int idUsuario) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        Inventario movimiento = new Inventario(idProducto, idProveedor, TipoMovimiento.DEVOLUCION,
                cantidad, motivo, idUsuario);
        inventarioDAO.guardar(movimiento);
        productoDAO.ajustarStock(idProducto, -cantidad);
    }

    /**
     * Lista los productos que ya llegaron a su punto de reorden (alerta de stock bajo).
     */
    public List<Producto> obtenerProductosStockBajo() {
        return productoDAO.listarStockBajo();
    }

    /** Los últimos movimientos de inventario de todos los productos (para mostrarlos en pantalla). */
    public List<InventarioDAO.MovimientoDetalle> historialReciente(int limite) {
        return inventarioDAO.listarRecientes(limite);
    }

    public List<Inventario> historialPorProducto(int idProducto) {
        return inventarioDAO.listarPorProducto(idProducto);
    }
}
