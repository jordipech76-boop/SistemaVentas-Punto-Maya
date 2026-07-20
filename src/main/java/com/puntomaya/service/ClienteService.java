package com.puntomaya.service;

import com.puntomaya.dao.ClienteDAO;
import com.puntomaya.model.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio relacionadas a clientes y fiados.
 */
public class ClienteService {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public List<Cliente> listar() {
        return clienteDAO.listar();
    }

    public List<Cliente> listarConAdeudo() {
        return clienteDAO.listarConAdeudo();
    }

    public List<Cliente> buscarPorNombre(String texto) {
        return clienteDAO.buscarPorNombre(texto);
    }

    public Optional<Cliente> buscarPorId(int id) {
        return clienteDAO.buscarPorId(id);
    }

    public void guardar(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        if (cliente.getLimiteCredito() < 0) {
            throw new IllegalArgumentException("El límite de crédito no puede ser negativo");
        }
        clienteDAO.guardar(cliente);
    }

    public void actualizar(Cliente cliente) {
        clienteDAO.actualizar(cliente);
    }

    public void eliminar(int id) {
        clienteDAO.eliminar(id);
    }

    /**
     * Verifica si el cliente puede fiar "monto" más sin rebasar su límite de crédito.
     */
    public boolean puedeFiar(Cliente cliente, double monto) {
        return cliente.puedeComprarFiado(monto);
    }

    /**
     * Registra un abono del cliente: reduce su saldo pendiente.
     */
    public void registrarAbono(int idCliente, double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto del abono debe ser mayor que cero");
        }
        clienteDAO.ajustarSaldo(idCliente, -monto);
    }
}
