package com.puntomaya.controller;

import com.puntomaya.model.Producto;
import com.puntomaya.model.Proveedor;
import com.puntomaya.service.InventarioService;
import com.puntomaya.service.ProductoService;
import com.puntomaya.util.Alertas;
import com.puntomaya.util.SesionActual;
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

/**
 * Controlador de la pantalla de inventario: entradas de mercancía,
 * mermas y alerta de productos con stock bajo.
 */
public class InventarioController {

    @FXML
    private ComboBox<Producto> comboProducto;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TextField txtMotivo;

    @FXML
    private TableView<Producto> tablaStockBajo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, Double> colStock;

    @FXML
    private TableColumn<Producto, Double> colPuntoReorden;

    private final InventarioService inventarioService = new InventarioService();
    private final ProductoService productoService = new ProductoService();
    private final ObservableList<Producto> listaStockBajo = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colPuntoReorden.setCellValueFactory(new PropertyValueFactory<>("puntoReorden"));
        tablaStockBajo.setItems(listaStockBajo);

        comboProducto.setItems(FXCollections.observableArrayList(productoService.listar()));

        cargarStockBajo();
    }

    private void cargarStockBajo() {
        listaStockBajo.setAll(inventarioService.obtenerProductosStockBajo());
    }

    @FXML
    private void registrarEntrada(ActionEvent event) {
        Producto producto = comboProducto.getValue();
        if (producto == null) {
            Alertas.mostrarAdvertencia("Selecciona un producto", "Elige el producto que llegó.");
            return;
        }
        try {
            double cantidad = Double.parseDouble(txtCantidad.getText());
            int idUsuario = SesionActual.getUsuario().getId();

            // En una versión completa, aquí se elegiría el proveedor desde un ComboBox<Proveedor>.
            // Por simplicidad este ejemplo usa id_proveedor = 1; ajusta según tu catálogo real.
            inventarioService.registrarEntrada(producto.getId(), 1, cantidad, idUsuario);

            Alertas.mostrarInformacion("Listo", "Entrada de mercancía registrada.");
            limpiarFormulario();
            cargarStockBajo();

        } catch (NumberFormatException e) {
            Alertas.mostrarAdvertencia("Dato inválido", "La cantidad debe ser un número.");
        } catch (IllegalArgumentException e) {
            Alertas.mostrarAdvertencia("Datos incompletos", e.getMessage());
        }
    }

    @FXML
    private void registrarMerma(ActionEvent event) {
        Producto producto = comboProducto.getValue();
        if (producto == null) {
            Alertas.mostrarAdvertencia("Selecciona un producto", "Elige el producto afectado.");
            return;
        }
        try {
            double cantidad = Double.parseDouble(txtCantidad.getText());
            String motivo = txtMotivo.getText();
            int idUsuario = SesionActual.getUsuario().getId();

            inventarioService.registrarMerma(producto.getId(), cantidad, motivo, idUsuario);

            Alertas.mostrarInformacion("Listo", "Merma registrada.");
            limpiarFormulario();
            cargarStockBajo();

        } catch (NumberFormatException e) {
            Alertas.mostrarAdvertencia("Dato inválido", "La cantidad debe ser un número.");
        } catch (IllegalArgumentException e) {
            Alertas.mostrarAdvertencia("Datos incompletos", e.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtCantidad.clear();
        txtMotivo.clear();
        comboProducto.getSelectionModel().clearSelection();
    }

    @FXML
    private void regresarAlMenu(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/puntomaya/view/Menu.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) tablaStockBajo.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("PuntoMaya - Menú principal");
    }
}
