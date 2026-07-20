package com.puntomaya.service;

import com.puntomaya.dao.ProductoDAO;
import com.puntomaya.model.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio relacionadas a productos. No tiene SQL, no tiene botones;
 * usa el DAO para leer/escribir y aquí solo se validan las reglas.
 */
public class ProductoService {

    private final ProductoDAO productoDAO = new ProductoDAO();

    public List<Producto> listar() {
        return productoDAO.listar();
    }

    public List<Producto> buscarPorNombre(String texto) {
        return productoDAO.buscarPorNombre(texto);
    }

    public Optional<Producto> buscarPorCodigoBarras(String codigo) {
        return productoDAO.buscarPorCodigoBarras(codigo);
    }

    public List<Producto> listarStockBajo() {
        return productoDAO.listarStockBajo();
    }

    public void guardar(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (producto.getPrecioVenta() <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero");
        }
        if (producto.getPrecioCosto() < 0) {
            throw new IllegalArgumentException("El precio de costo no puede ser negativo");
        }
        productoDAO.guardar(producto);
    }

    public void actualizar(Producto producto) {
        if (producto.getPrecioVenta() <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero");
        }
        productoDAO.actualizar(producto);
    }

    public void eliminar(int id) {
        productoDAO.eliminar(id);
    }

    /**
     * Calcula la ganancia total potencial del catálogo (solo referencia).
     */
    public double calcularGananciaUnitaria(Producto producto) {
        return producto.calcularGanancia();
    }
}
