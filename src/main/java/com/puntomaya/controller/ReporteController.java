package com.puntomaya.controller;

import com.puntomaya.dao.VentaDAO;
import com.puntomaya.util.Alertas;
import com.puntomaya.model.Cliente;
import com.puntomaya.service.ReporteService;
import com.puntomaya.util.Utilidades;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador de la pantalla de Reportes: ventas del día, productos
 * más vendidos, ganancia, fiados totales y sugerencia de compra.
 * Solo el Administrador puede entrar aquí (Miguel / Doña Rosa).
 */
public class ReporteController {

    @FXML
    private Label lblNumVentasHoy;

    @FXML
    private Label lblTotalVentasHoy;

    @FXML
    private Label lblGananciaHoy;

    @FXML
    private Label lblTotalFiados;

    @FXML
    private TableView<VentaDAO.ProductoVendido> tablaMasVendidos;

    @FXML
    private TableColumn<VentaDAO.ProductoVendido, String> colNombreVendido;

    @FXML
    private TableColumn<VentaDAO.ProductoVendido, Double> colCantidadVendida;

    @FXML
    private TableView<VentaDAO.ProductoVendido> tablaSugerencia;

    @FXML
    private TableColumn<VentaDAO.ProductoVendido, String> colNombreSugerido;

    @FXML
    private TableColumn<VentaDAO.ProductoVendido, Double> colCantidadSugerida;

    @FXML
    private TableView<Cliente> tablaFiados;

    @FXML
    private TableColumn<Cliente, String> colClienteFiado;

    @FXML
    private TableColumn<Cliente, Double> colSaldoFiado;

    private final ReporteService reporteService = new ReporteService();
    private final com.puntomaya.service.BackupService backupService = new com.puntomaya.service.BackupService();
    private final ObservableList<VentaDAO.ProductoVendido> listaMasVendidos = FXCollections.observableArrayList();
    private final ObservableList<VentaDAO.ProductoVendido> listaSugerencia = FXCollections.observableArrayList();
    private final ObservableList<Cliente> listaFiados = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombreVendido.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidadVendida.setCellValueFactory(new PropertyValueFactory<>("cantidadVendida"));
        tablaMasVendidos.setItems(listaMasVendidos);

        colNombreSugerido.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidadSugerida.setCellValueFactory(new PropertyValueFactory<>("cantidadVendida"));
        tablaSugerencia.setItems(listaSugerencia);

        colClienteFiado.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colSaldoFiado.setCellValueFactory(new PropertyValueFactory<>("saldoActual"));
        tablaFiados.setItems(listaFiados);

        cargarTodo();
    }

    /** Vuelve a calcular y mostrar todos los reportes con datos frescos de MySQL. */
    private void cargarTodo() {
        ReporteService.ResumenVentasDia ventasHoy = reporteService.ventasDeHoy();
        lblNumVentasHoy.setText(String.valueOf(ventasHoy.getNumeroDeVentas()));
        lblTotalVentasHoy.setText(Utilidades.formatearMoneda(ventasHoy.getTotalVendido()));

        lblGananciaHoy.setText(Utilidades.formatearMoneda(reporteService.gananciaDeHoy()));

        listaMasVendidos.setAll(reporteService.productosMasVendidos(30, 10));
        listaSugerencia.setAll(reporteService.sugerenciaDeCompra());

        ReporteService.ResumenFiados fiados = reporteService.fiadosTotales();
        listaFiados.setAll(fiados.getClientes());
        lblTotalFiados.setText(Utilidades.formatearMoneda(fiados.getTotalAdeudado()));
    }

    @FXML
    private void actualizar(ActionEvent event) {
        cargarTodo();
    }

    /** Botón "Respaldar datos (.sql)": guarda TODOS los datos en un solo archivo. */
    @FXML
    private void respaldarSQL(ActionEvent event) {
        javafx.stage.FileChooser selector = new javafx.stage.FileChooser();
        selector.setTitle("Guardar respaldo de la base de datos");
        selector.setInitialFileName("puntomaya_respaldo_" + java.time.LocalDate.now() + ".sql");
        selector.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Archivo SQL", "*.sql"));

        java.io.File archivo = selector.showSaveDialog(tablaMasVendidos.getScene().getWindow());
        if (archivo == null) return;

        try {
            backupService.generarRespaldoSQL(archivo);
            Alertas.mostrarInformacion("Listo", "Respaldo guardado en:\n" + archivo.getAbsolutePath());
        } catch (Exception e) {
            Alertas.mostrarError("Error", "No se pudo generar el respaldo.\n" + e.getMessage());
        }
    }

    /** Botón "Exportar a Excel (CSV)": guarda cada tabla como un archivo CSV, para abrir en Excel. */
    @FXML
    private void exportarCSV(ActionEvent event) {
        javafx.stage.DirectoryChooser selector = new javafx.stage.DirectoryChooser();
        selector.setTitle("Selecciona la carpeta donde guardar los archivos CSV");

        java.io.File carpeta = selector.showDialog(tablaMasVendidos.getScene().getWindow());
        if (carpeta == null) return;

        try {
            backupService.exportarCSV(carpeta);
            Alertas.mostrarInformacion("Listo", "Archivos CSV guardados en:\n" + carpeta.getAbsolutePath());
        } catch (Exception e) {
            Alertas.mostrarError("Error", "No se pudo exportar a CSV.\n" + e.getMessage());
        }
    }

    @FXML
    private void regresarAlMenu(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/puntomaya/view/Menu.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) tablaMasVendidos.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("PuntoMaya - Menú principal");
    }
}