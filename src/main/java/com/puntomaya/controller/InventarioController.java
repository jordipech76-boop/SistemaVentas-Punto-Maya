package com.puntomaya.controller;

import com.puntomaya.dao.InventarioDAO;
import com.puntomaya.model.Producto;
import com.puntomaya.model.Proveedor;
import com.puntomaya.service.InventarioService;
import com.puntomaya.service.ProductoService;
import com.puntomaya.service.ProveedorService;
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

/**
 * Controlador de la pantalla de inventario: entradas de mercancía,
 * mermas, devoluciones a proveedor, alerta de stock bajo, alerta
 * de productos por caducar, y ahora también el historial de movimientos.
 */
public class InventarioController {

    @FXML private ComboBox<Producto> comboProducto;
    @FXML private ComboBox<Proveedor> comboProveedor;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtMotivo;

    @FXML private TableView<Producto> tablaStockBajo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colStock;
    @FXML private TableColumn<Producto, Double> colPuntoReorden;

    @FXML private TableView<Producto> tablaCaducidad;
    @FXML private TableColumn<Producto, String> colNombreCad;
    @FXML private TableColumn<Producto, java.time.LocalDate> colFechaCaducidad;

    @FXML private TableView<InventarioDAO.MovimientoDetalle> tablaHistorial;
    @FXML private TableColumn<InventarioDAO.MovimientoDetalle, String> colFechaMov;
    @FXML private TableColumn<InventarioDAO.MovimientoDetalle, String> colProductoMov;
    @FXML private TableColumn<InventarioDAO.MovimientoDetalle, String> colProveedorMov;
    @FXML private TableColumn<InventarioDAO.MovimientoDetalle, String> colTipoMov;
    @FXML private TableColumn<InventarioDAO.MovimientoDetalle, Double> colCantidadMov;
    @FXML private TableColumn<InventarioDAO.MovimientoDetalle, String> colMotivoMov;

    private final InventarioService inventarioService = new InventarioService();
    private final ProductoService productoService = new ProductoService();
    private final ProveedorService proveedorService = new ProveedorService();

    private final ObservableList<Producto> listaStockBajo = FXCollections.observableArrayList();
    private final ObservableList<Producto> listaPorCaducar = FXCollections.observableArrayList();
    private final ObservableList<InventarioDAO.MovimientoDetalle> listaHistorial = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colPuntoReorden.setCellValueFactory(new PropertyValueFactory<>("puntoReorden"));
        tablaStockBajo.setItems(listaStockBajo);

        if (tablaCaducidad != null) {
            colNombreCad.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            colFechaCaducidad.setCellValueFactory(new PropertyValueFactory<>("fechaCaducidad"));
            tablaCaducidad.setItems(listaPorCaducar);
        }

        if (tablaHistorial != null) {
            colFechaMov.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getFecha().format(Utilidades.FORMATO_FECHA_HORA)));
            colProductoMov.setCellValueFactory(new PropertyValueFactory<>("producto"));
            colProveedorMov.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getProveedor() == null ? "—" : c.getValue().getProveedor()));
            colTipoMov.setCellValueFactory(new PropertyValueFactory<>("tipo"));
            colCantidadMov.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
            colMotivoMov.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getMotivo() == null ? "—" : c.getValue().getMotivo()));
            tablaHistorial.setItems(listaHistorial);
        }

        comboProducto.setItems(FXCollections.observableArrayList(productoService.listar()));

        if (comboProveedor != null) {
            comboProveedor.setItems(FXCollections.observableArrayList(proveedorService.listar()));
        }

        cargarStockBajo();
        cargarPorCaducar();
        cargarHistorial();
    }

    private void cargarStockBajo() {
        listaStockBajo.setAll(inventarioService.obtenerProductosStockBajo());
    }

    private void cargarPorCaducar() {
        if (tablaCaducidad == null) return;
        listaPorCaducar.setAll(productoService.listarPorCaducar(7));
    }

    /** Trae los últimos 20 movimientos de inventario (entradas, mermas, devoluciones). */
    private void cargarHistorial() {
        if (tablaHistorial == null) return;
        listaHistorial.setAll(inventarioService.historialReciente(20));
    }

    @FXML
    private void registrarEntrada(ActionEvent event) {
        Producto producto = comboProducto.getValue();
        if (producto == null) {
            Alertas.mostrarAdvertencia("Selecciona un producto", "Elige el producto que llegó.");
            return;
        }
        Proveedor proveedor = comboProveedor != null ? comboProveedor.getValue() : null;
        if (proveedor == null) {
            Alertas.mostrarAdvertencia("Selecciona un proveedor", "Elige de quién llegó la mercancía.");
            return;
        }
        try {
            double cantidad = Double.parseDouble(txtCantidad.getText());
            int idUsuario = SesionActual.getUsuario().getId();

            inventarioService.registrarEntrada(producto.getId(), proveedor.getId(), cantidad, idUsuario);

            Alertas.mostrarInformacion("Listo", "Entrada de mercancía registrada.");
            limpiarFormulario();
            cargarStockBajo();
            cargarHistorial();

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
            cargarPorCaducar();
            cargarHistorial();

        } catch (NumberFormatException e) {
            Alertas.mostrarAdvertencia("Dato inválido", "La cantidad debe ser un número.");
        } catch (IllegalArgumentException e) {
            Alertas.mostrarAdvertencia("Datos incompletos", e.getMessage());
        }
    }

    @FXML
    private void registrarDevolucion(ActionEvent event) {
        Producto producto = comboProducto.getValue();
        if (producto == null) {
            Alertas.mostrarAdvertencia("Selecciona un producto", "Elige el producto a devolver.");
            return;
        }
        Proveedor proveedor = comboProveedor != null ? comboProveedor.getValue() : null;
        if (proveedor == null) {
            Alertas.mostrarAdvertencia("Selecciona un proveedor", "Elige a quién se le devuelve.");
            return;
        }
        try {
            double cantidad = Double.parseDouble(txtCantidad.getText());
            String motivo = txtMotivo.getText().isBlank() ? "Devolución a proveedor" : txtMotivo.getText();
            int idUsuario = SesionActual.getUsuario().getId();

            inventarioService.registrarDevolucion(producto.getId(), proveedor.getId(), cantidad, motivo, idUsuario);

            Alertas.mostrarInformacion("Listo", "Devolución a proveedor registrada.");
            limpiarFormulario();
            cargarStockBajo();
            cargarHistorial();

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
        if (comboProveedor != null) comboProveedor.getSelectionModel().clearSelection();
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