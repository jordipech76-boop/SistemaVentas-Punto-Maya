package com.puntomaya.controller;

import com.puntomaya.model.Producto;
import com.puntomaya.model.RolUsuario;
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
 * Controlador de la pantalla de productos (catálogo y precios).
 * Solo el rol ADMINISTRADOR puede modificar precios; se valida aquí
 * antes de llamar al service.
 */
public class ProductoController {

    @FXML
    private TextField txtCodigoBarras;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPrecioVenta;

    @FXML
    private TextField txtPrecioCosto;

    @FXML
    private TextField txtStock;

    @FXML
    private TextField txtPuntoReorden;

    @FXML
    private CheckBox chkEsGranel;

    @FXML
    private TableView<Producto> tablaProductos;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, Double> colPrecioVenta;

    @FXML
    private TableColumn<Producto, Double> colStock;

    @FXML
    private Button btnGuardar;

    private final ProductoService productoService = new ProductoService();
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        tablaProductos.setItems(listaProductos);
        cargarProductos();

        // Solo el administrador puede dar de alta/editar precios
        var usuario = SesionActual.getUsuario();
        if (usuario != null && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            btnGuardar.setDisable(true);
        }
    }

    private void cargarProductos() {
        listaProductos.setAll(productoService.listar());
    }

    @FXML
    private void guardarProducto(ActionEvent event) {
        try {
            Producto producto = new Producto();
            producto.setCodigoBarras(txtCodigoBarras.getText().isBlank() ? null : txtCodigoBarras.getText());
            producto.setNombre(txtNombre.getText());
            producto.setPrecioVenta(Double.parseDouble(txtPrecioVenta.getText()));
            producto.setPrecioCosto(Double.parseDouble(
                    txtPrecioCosto.getText().isBlank() ? "0" : txtPrecioCosto.getText()));
            producto.setStock(Double.parseDouble(txtStock.getText().isBlank() ? "0" : txtStock.getText()));
            producto.setPuntoReorden(Double.parseDouble(
                    txtPuntoReorden.getText().isBlank() ? "5" : txtPuntoReorden.getText()));
            producto.setEsGranel(chkEsGranel.isSelected());

            productoService.guardar(producto);
            Alertas.mostrarInformacion("Listo", "Producto guardado correctamente.");
            limpiarFormulario();
            cargarProductos();

        } catch (NumberFormatException e) {
            Alertas.mostrarAdvertencia("Dato inválido", "Revisa que precio, costo y stock sean números.");
        } catch (IllegalArgumentException e) {
            Alertas.mostrarAdvertencia("Datos incompletos", e.getMessage());
        }
    }

    @FXML
    private void eliminarProducto(ActionEvent event) {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        boolean confirmar = Alertas.confirmar("Eliminar producto",
                "¿Seguro que quieres eliminar " + seleccionado.getNombre() + "?");
        if (confirmar) {
            productoService.eliminar(seleccionado.getId());
            cargarProductos();
        }
    }

    private void limpiarFormulario() {
        txtCodigoBarras.clear();
        txtNombre.clear();
        txtPrecioVenta.clear();
        txtPrecioCosto.clear();
        txtStock.clear();
        txtPuntoReorden.clear();
        chkEsGranel.setSelected(false);
    }

    @FXML
    private void regresarAlMenu(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/puntomaya/view/Menu.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) tablaProductos.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("PuntoMaya - Menú principal");
    }
}
