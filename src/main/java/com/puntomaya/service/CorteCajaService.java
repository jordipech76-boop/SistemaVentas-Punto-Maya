package com.puntomaya.service;

import com.puntomaya.dao.CorteCajaDAO;
import com.puntomaya.dao.VentaDAO;
import com.puntomaya.model.CorteCaja;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Reglas de negocio para el corte de caja: calcula el efectivo esperado
 * a partir de las ventas del día y lo compara contra el efectivo contado.
 */
public class CorteCajaService {

    private final CorteCajaDAO corteCajaDAO = new CorteCajaDAO();
    private final VentaDAO ventaDAO = new VentaDAO();

    /**
     * Genera y guarda el corte de caja de un turno.
     *
     * @param fecha           fecha del corte
     * @param turno           nombre del turno (ej. "Matutino", "Vespertino")
     * @param idUsuario       usuario que hace el corte
     * @param fondoInicial    dinero con el que se abrió la caja
     * @param efectivoContado dinero contado físicamente al cerrar
     */
    public CorteCaja realizarCorte(LocalDate fecha, String turno, int idUsuario,
                                    double fondoInicial, double efectivoContado) {

        LocalDateTime desde = LocalDateTime.of(fecha, LocalTime.MIN);
        LocalDateTime hasta = LocalDateTime.of(fecha, LocalTime.MAX);

        double ventasEfectivo = ventaDAO.sumarVentasEfectivo(desde, hasta);
        double efectivoEsperado = fondoInicial + ventasEfectivo;

        CorteCaja corte = new CorteCaja();
        corte.setFecha(fecha);
        corte.setTurno(turno);
        corte.setIdUsuario(idUsuario);
        corte.setFondoInicial(fondoInicial);
        corte.setEfectivoEsperado(efectivoEsperado);
        corte.setEfectivoReal(efectivoContado); // esto también calcula la diferencia

        corteCajaDAO.guardar(corte);
        return corte;
    }

    public List<CorteCaja> listarPorUsuario(int idUsuario) {
        return corteCajaDAO.listarPorUsuario(idUsuario);
    }

    public List<CorteCaja> listarTodos() {
        return corteCajaDAO.listarTodos();
    }
}
