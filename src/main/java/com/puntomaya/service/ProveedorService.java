package com.puntomaya.service;

import com.puntomaya.dao.ProveedorDAO;
import com.puntomaya.model.Proveedor;

import java.util.List;

/**
 * Reglas de negocio para proveedores (por ahora son mínimas: solo
 * validar que el nombre no venga vacío antes de guardar).
 */
public class ProveedorService {

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    public List<Proveedor> listar() {
        return proveedorDAO.listar();
    }

    public void guardar(Proveedor proveedor) {
        if (proveedor.getNombre() == null || proveedor.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio");
        }
        proveedorDAO.guardar(proveedor);
    }

    public void eliminar(int id) {
        proveedorDAO.eliminar(id);
    }
}