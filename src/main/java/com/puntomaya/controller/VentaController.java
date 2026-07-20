package com.puntomaya.controller;

import com.puntomaya.model.*;
import com.puntomaya.service.ClienteService;
import com.puntomaya.service.ProductoService;
import com.puntomaya.service.VentaService;
import com.puntomaya.util.Alertas;
import com.puntomaya.util.SesionActual;
import com.puntomaya.util.Utilidades;
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
import java.util.List;
import java.util.Optional;

/**
 * Controlador de la pantalla de ventas: agregar productos, cobrar,
 * vender a crédito, cancelar antes de cobrar.
 */
public class VentaController {

    @FXML
    private TextField txtCodigoBarras;

    @FXML
    private TextField txtBuscarProducto;

    @FXML
    private TableView<DetalleVenta> tablaCarrito;

    @FXML
    private TableColumn<DetalleVenta, String> colProducto;

    @FXML
    private TableColumn<DetalleVenta, Double> colCantidad;

    @FXML
    private TableColumn<DetalleVenta, Double> colPrecio;

    @FXML
    private TableColumn<DetalleVenta, Double> colImporte;

    @FXML
    private Label lblTotal;

    @FXML
    private ComboBox<FormaPago> comboFormaPago;

    @FXML
    private CheckBox chkEsFiado;

    @FXML
    private ComboBox<Cliente> comboCliente;

    private final ProductoService productoService = new ProductoService();
    private final ClienteService clienteService = new ClienteService();
    private final VentaService ventaService = new VentaService();

    private final ObservableList<DetalleVenta> carrito = FXCollections.observableArrayList();
    private Venta ventaActual = new Venta();

    @FXML
    public void initialize() {
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colImporte.setCellValueFactory(new PropertyValueFactory<>("importe"));
        tablaCarrito.setItems(carrito);

        comboFormaPago.setItems(FXCollections.observableArrayList(FormaPago.values()));
        comboFormaPago.getSelectionModel().select(FormaPago.EFECTIVO);

        comboCliente.setItems(FXCollections.observableArrayList(clienteService.listar()));
        comboCliente.setDisable(true);

        chkEsFiado.selectedProperty().addListener((obs, anterior, esFiado) ->
                comboCliente.setDisable(!esFiado));

        actualizarTotal();
    }

    /**
     * Se llama cuando la cajera pasa el lector de código de barras
     * (el lector "escribe" el código y presiona Enter automáticamente).
     */
    @FXML
    private void buscarPorCodigoBarras(ActionEvent event) {
        String codigo = txtCodigoBarras.getText().trim();
        if (codigo.isEmpty()) return;

        Optional<Producto> producto = productoService.buscarPorCodigoBarras(codigo);
        if (producto.isPresent()) {
            agregarProductoAlCarrito(producto.get(), 1);
        } else {
            Alertas.mostrarAdvertencia("No encontrado", "Ningún producto tiene ese código de barras.");
        }
        txtCodigoBarras.clear();
        txtCodigoBarras.requestFocus();
    }

    /**
     * Búsqueda por nombre, para productos a granel o sin código de barras.
     */
    @FXML
    private void buscarPorNombre(ActionEvent event) {
        String texto = txtBuscarProducto.getText().trim();
        if (texto.isEmpty()) return;

        List<Producto> encontrados = productoService.buscarPorNombre(texto);
        if (encontrados.isEmpty()) {
            Alertas.mostrarAdvertencia("Sin resultados", "No se encontraron productos con ese nombre.");
            return;
        }
        // Se toma el primero como ejemplo simple; se puede cambiar por una ventana de selección.
        agregarProductoAlCarrito(encontrados.get(0), 1);
        txtBuscarProducto.clear();
    }

    private void agregarProductoAlCarrito(Producto producto, double cantidad) {
        DetalleVenta detalle = new DetalleVenta(producto.getId(), producto.getNombre(),
                cantidad, producto.getPrecioVenta());
        carrito.add(detalle);
        ventaActual.setDetalles(carrito);
        ventaActual.recalcularTotales();
        actualizarTotal();
    }

    @FXML
    private void quitarProductoSeleccionado(ActionEvent event) {
        DetalleVenta seleccionado = tablaCarrito.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            carrito.remove(seleccionado);
            ventaActual.recalcularTotales();
            actualizarTotal();
        }
    }

    @FXML
    private void cancelarVentaActual(ActionEvent event) {
        if (carrito.isEmpty()) return;
        boolean confirmar = Alertas.confirmar("Cancelar venta", "¿Quitar todos los productos de la venta actual?");
        if (confirmar) {
            carrito.clear();
            ventaActual = new Venta();
            actualizarTotal();
        }
    }

    private void actualizarTotal() {
        lblTotal.setText(Utilidades.formatearMoneda(ventaActual.getTotal()));
    }

    @FXML
    private void cobrarVenta(ActionEvent event) {
        if (carrito.isEmpty()) {
            Alertas.mostrarAdvertencia("Venta vacía", "Agrega al menos un producto antes de cobrar.");
            return;
        }

        ventaActual.setFormaPago(comboFormaPago.getValue());
        ventaActual.setEsFiado(chkEsFiado.isSelected());
        ventaActual.setIdUsuario(SesionActual.getUsuario().getId());

        if (chkEsFiado.isSelected()) {
            Cliente cliente = comboCliente.getValue();
            if (cliente == null) {
                Alertas.mostrarAdvertencia("Falta cliente", "Selecciona el cliente para vender a crédito.");
                return;
            }
            ventaActual.setIdCliente(cliente.getId());
        } else {
            ventaActual.setIdCliente(null);
        }

        try {
            ventaService.realizarVenta(ventaActual);
            Alertas.mostrarInformacion("Venta registrada", "La venta se guardó correctamente.\nTotal: "
                    + Utilidades.formatearMoneda(ventaActual.getTotal()));

            // Limpiar para la siguiente venta
            carrito.clear();
            ventaActual = new Venta();
            chkEsFiado.setSelected(false);
            actualizarTotal();

        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarAdvertencia("No se pudo completar la venta", e.getMessage());
        } catch (RuntimeException e) {
            Alertas.mostrarError("Error de conexión",
                    "No se pudo conectar a la base de datos. Verifica que MySQL esté encendido.");
        }
    }

    @FXML
    private void regresarAlMenu(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/puntomaya/view/Menu.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) tablaCarrito.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("PuntoMaya - Menú principal");
    }
}
