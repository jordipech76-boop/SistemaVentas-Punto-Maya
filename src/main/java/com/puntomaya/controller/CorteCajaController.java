package com.puntomaya.controller;

import com.puntomaya.model.CorteCaja;
import com.puntomaya.service.CorteCajaService;
import com.puntomaya.util.Alertas;
import com.puntomaya.util.SesionActual;
import com.puntomaya.util.Utilidades;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Controlador de la pantalla de Corte de Caja: compara el efectivo que
 * debería haber (fondo inicial + ventas en efectivo del turno) contra
 * lo que el cajero contó de verdad al cerrar.
 */
public class CorteCajaController {

    @FXML private TextField txtTurno;
    @FXML private TextField txtFondoInicial;
    @FXML private TextField txtEfectivoContado;

    @FXML private Label lblEsperado;
    @FXML private Label lblReal;
    @FXML private Label lblDiferencia;

    @FXML private TableView<CorteCaja> tablaHistorial;
    @FXML private TableColumn<CorteCaja, String> colFecha;
    @FXML private TableColumn<CorteCaja, String> colTurno;
    @FXML private TableColumn<CorteCaja, Double> colFondo;
    @FXML private TableColumn<CorteCaja, Double> colEsperado;
    @FXML private TableColumn<CorteCaja, Double> colReal;
    @FXML private TableColumn<CorteCaja, Double> colDiferencia;

    private final CorteCajaService corteCajaService = new CorteCajaService();
    private final ObservableList<CorteCaja> historial = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFecha().format(Utilidades.FORMATO_FECHA)));
        colTurno.setCellValueFactory(new PropertyValueFactory<>("turno"));
        colFondo.setCellValueFactory(new PropertyValueFactory<>("fondoInicial"));
        colEsperado.setCellValueFactory(new PropertyValueFactory<>("efectivoEsperado"));
        colReal.setCellValueFactory(new PropertyValueFactory<>("efectivoReal"));
        colDiferencia.setCellValueFactory(new PropertyValueFactory<>("diferencia"));
        tablaHistorial.setItems(historial);

        cargarHistorial();
    }

    /** Trae el historial de cortes del cajero que inició sesión (para rastrear si un faltante se repite). */
    private void cargarHistorial() {
        int idUsuario = SesionActual.getUsuario().getId();
        historial.setAll(corteCajaService.listarPorUsuario(idUsuario));
    }

    /** Botón "Generar corte": calcula el efectivo esperado y lo compara contra lo contado. */
    @FXML
    private void generarCorte(ActionEvent event) {
        String turno = txtTurno.getText().trim();
        if (turno.isEmpty()) {
            Alertas.mostrarAdvertencia("Falta el turno", "Escribe el nombre del turno (ej. Matutino, Vespertino).");
            return;
        }
        try {
            double fondoInicial = Double.parseDouble(txtFondoInicial.getText());
            double efectivoContado = Double.parseDouble(txtEfectivoContado.getText());

            CorteCaja corte = corteCajaService.realizarCorte(
                    LocalDate.now(), turno, SesionActual.getUsuario().getId(), fondoInicial, efectivoContado);

            lblEsperado.setText(Utilidades.formatearMoneda(corte.getEfectivoEsperado()));
            lblReal.setText(Utilidades.formatearMoneda(corte.getEfectivoReal()));

            if (corte.cuadra()) {
                lblDiferencia.setText("Cuadra exacto ✓");
                lblDiferencia.setStyle("-fx-text-fill: #2e7d4f; -fx-font-size: 22px; -fx-font-weight: bold;");
            } else if (corte.getDiferencia() < 0) {
                lblDiferencia.setText("Faltante: " + Utilidades.formatearMoneda(-corte.getDiferencia()));
                lblDiferencia.setStyle("-fx-text-fill: #a33a3a; -fx-font-size: 22px; -fx-font-weight: bold;");
            } else {
                lblDiferencia.setText("Sobrante: " + Utilidades.formatearMoneda(corte.getDiferencia()));
                lblDiferencia.setStyle("-fx-text-fill: #b5762b; -fx-font-size: 22px; -fx-font-weight: bold;");
            }

            Alertas.mostrarInformacion("Corte generado", "El corte de caja se registró correctamente.");
            cargarHistorial();

        } catch (NumberFormatException e) {
            Alertas.mostrarAdvertencia("Datos inválidos", "Fondo inicial y efectivo contado deben ser números.");
        } catch (RuntimeException e) {
            Alertas.mostrarError("Error de conexión", "No se pudo conectar a la base de datos.");
        }
    }

    @FXML
    private void regresarAlMenu(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/puntomaya/view/Menu.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) tablaHistorial.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("PuntoMaya - Menú principal");
    }
}
