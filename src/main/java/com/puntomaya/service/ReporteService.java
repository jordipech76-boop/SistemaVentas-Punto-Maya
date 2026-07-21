package com.puntomaya.service;

import com.puntomaya.dao.ClienteDAO;
import com.puntomaya.dao.VentaDAO;
import com.puntomaya.model.Cliente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Junta la información de varias tablas para armar los reportes que
 * pidió Miguel: ventas del día, productos más vendidos, ganancia,
 * fiados totales y sugerencia de compra. No tiene SQL directamente,
 * usa los DAO ya existentes.
 */
public class ReporteService {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    /** Cuántas ventas y cuánto se vendió hoy (todas las formas de pago). */
    public ResumenVentasDia ventasDeHoy() {
        LocalDate hoy = LocalDate.now();
        int numVentas = ventaDAO.contarVentasDia(hoy);
        double total = ventaDAO.sumarVentasTotalDia(hoy);
        return new ResumenVentasDia(numVentas, total);
    }

    /** Los productos que más se han vendido en los últimos "dias". */
    public List<VentaDAO.ProductoVendido> productosMasVendidos(int dias, int limite) {
        LocalDateTime desde = LocalDateTime.of(LocalDate.now().minusDays(dias), LocalTime.MIN);
        return ventaDAO.productosMasVendidos(desde, limite);
    }

    /** Ganancia total (venta - costo) del día de hoy. */
    public double gananciaDeHoy() {
        LocalDateTime desde = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        return ventaDAO.calcularGananciaDesde(desde);
    }

    /** Ganancia total de los últimos "dias" días. */
    public double gananciaUltimosDias(int dias) {
        LocalDateTime desde = LocalDateTime.of(LocalDate.now().minusDays(dias), LocalTime.MIN);
        return ventaDAO.calcularGananciaDesde(desde);
    }

    /** Lista de clientes con adeudo, y cuánto suman entre todos (total de fiados pendientes). */
    public ResumenFiados fiadosTotales() {
        List<Cliente> clientes = clienteDAO.listarConAdeudo();
        double total = clientes.stream().mapToDouble(Cliente::getSaldoActual).sum();
        return new ResumenFiados(clientes, total);
    }

    /**
     * Sugerencia de compra: los productos que más se vendieron en los
     * últimos 30 días, para que Beto tenga una idea de qué pedir
     * (en vez de pedir "a tanteo" como hacía antes).
     */
    public List<VentaDAO.ProductoVendido> sugerenciaDeCompra() {
        return productosMasVendidos(30, 10);
    }

    // ---- Clases pequeñas de apoyo para regresar varios datos juntos ----

    public static class ResumenVentasDia {
        private final int numeroDeVentas;
        private final double totalVendido;

        public ResumenVentasDia(int numeroDeVentas, double totalVendido) {
            this.numeroDeVentas = numeroDeVentas;
            this.totalVendido = totalVendido;
        }

        public int getNumeroDeVentas() {
            return numeroDeVentas;
        }

        public double getTotalVendido() {
            return totalVendido;
        }
    }

    public static class ResumenFiados {
        private final List<Cliente> clientes;
        private final double totalAdeudado;

        public ResumenFiados(List<Cliente> clientes, double totalAdeudado) {
            this.clientes = clientes;
            this.totalAdeudado = totalAdeudado;
        }

        public List<Cliente> getClientes() {
            return clientes;
        }

        public double getTotalAdeudado() {
            return totalAdeudado;
        }
    }
}