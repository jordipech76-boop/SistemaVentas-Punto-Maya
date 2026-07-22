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
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de la pantalla de ventas: agregar productos, cobrar,
 * vender a crédito, cancelar antes de cobrar, calcular cambio,
 * generar ticket y (solo Administrador) cancelar una venta ya cobrada.
 */
public class VentaController {

    @FXML
    private TextField txtCodigoBarras;

    @FXML
    private TextField txtBuscarProducto;

    @FXML
    private TextField txtCantidad;

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

    @FXML
    private Label lblSaldoCliente;

    @FXML
    private TextField txtMontoRecibido;

    @FXML
    private Label lblCambio;

    @FXML
    private Button btnCancelarVentaCobrada;

    @FXML
    private Button btnAgregarProductoNuevo;

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

        chkEsFiado.selectedProperty().addListener((obs, anterior, esFiado) -> {
            comboCliente.setDisable(!esFiado);
            if (!esFiado) lblSaldoCliente.setText("");
        });

        comboCliente.valueProperty().addListener((obs, anterior, cliente) -> {
            if (cliente != null) {
                lblSaldoCliente.setText("Saldo actual: " + Utilidades.formatearMoneda(cliente.getSaldoActual())
                        + "  /  Límite: " + Utilidades.formatearMoneda(cliente.getLimiteCredito()));
            } else {
                lblSaldoCliente.setText("");
            }
        });

        txtMontoRecibido.textProperty().addListener((obs, anterior, nuevo) -> actualizarCambio());
        comboFormaPago.valueProperty().addListener((obs, anterior, forma) -> {
            boolean esEfectivo = forma == FormaPago.EFECTIVO;
            txtMontoRecibido.setDisable(!esEfectivo);
            if (!esEfectivo) {
                txtMontoRecibido.clear();
                lblCambio.setText("");
            }
        });

        if (btnCancelarVentaCobrada != null) {
            var usuario = SesionActual.getUsuario();
            btnCancelarVentaCobrada.setVisible(usuario != null && usuario.esAdministrador());
            btnCancelarVentaCobrada.setManaged(usuario != null && usuario.esAdministrador());
        }

        if (btnAgregarProductoNuevo != null) {
            var usuarioActual = SesionActual.getUsuario();
            btnAgregarProductoNuevo.setVisible(usuarioActual != null && usuarioActual.esAdministrador());
            btnAgregarProductoNuevo.setManaged(usuarioActual != null && usuarioActual.esAdministrador());
        }

        actualizarTotal();
    }

    @FXML
    private void buscarPorCodigoBarras(ActionEvent event) {
        String codigo = txtCodigoBarras.getText().trim();
        if (codigo.isEmpty()) return;

        double cantidad = leerCantidad();
        if (cantidad <= 0) return;

        Optional<Producto> producto = productoService.buscarPorCodigoBarras(codigo);
        if (producto.isPresent()) {
            agregarProductoAlCarrito(producto.get(), cantidad);
        } else {
            Alertas.mostrarAdvertencia("No encontrado", "Ningún producto tiene ese código de barras.");
        }
        txtCodigoBarras.clear();
        txtCantidad.setText("1");
        txtCodigoBarras.requestFocus();
    }

    @FXML
    private void buscarPorNombre(ActionEvent event) {
        String texto = txtBuscarProducto.getText().trim();
        if (texto.isEmpty()) return;

        double cantidad = leerCantidad();
        if (cantidad <= 0) return;

        List<Producto> encontrados = productoService.buscarPorNombre(texto);
        if (encontrados.isEmpty()) {
            Alertas.mostrarAdvertencia("Sin resultados", "No se encontraron productos con ese nombre.");
            return;
        }
        agregarProductoAlCarrito(encontrados.get(0), cantidad);
        txtBuscarProducto.clear();
        txtCantidad.setText("1");
    }

    /**
     * Lee la cantidad escrita en el campo de cantidad. Si está vacío o
     * inválido, asume 1 (para no romper el flujo si alguien lo deja en blanco).
     */
    private double leerCantidad() {
        String texto = txtCantidad.getText().trim();
        if (texto.isEmpty()) return 1;
        try {
            double cantidad = Double.parseDouble(texto);
            if (cantidad <= 0) {
                Alertas.mostrarAdvertencia("Cantidad inválida", "La cantidad debe ser mayor que cero.");
                return 0;
            }
            return cantidad;
        } catch (NumberFormatException e) {
            Alertas.mostrarAdvertencia("Cantidad inválida", "Escribe un número válido en el campo de cantidad.");
            return 0;
        }
    }

    private void agregarProductoAlCarrito(Producto producto, double cantidad) {
        DetalleVenta detalle = new DetalleVenta(producto.getId(), producto.getNombre(),
                cantidad, producto.getPrecioVenta());
        carrito.add(detalle);
        ventaActual.setDetalles(carrito);
        ventaActual.recalcularTotales();
        actualizarTotal();
        actualizarCambio();
    }

    @FXML
    private void quitarProductoSeleccionado(ActionEvent event) {
        DetalleVenta seleccionado = tablaCarrito.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            carrito.remove(seleccionado);
            ventaActual.recalcularTotales();
            actualizarTotal();
            actualizarCambio();
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
            actualizarCambio();
        }
    }

    private void actualizarTotal() {
        lblTotal.setText(Utilidades.formatearMoneda(ventaActual.getTotal()));
    }

    private void actualizarCambio() {
        if (lblCambio == null || txtMontoRecibido == null) return;
        try {
            double recibido = Double.parseDouble(txtMontoRecibido.getText());
            double cambio = recibido - ventaActual.getTotal();
            if (cambio < 0) {
                lblCambio.setText("Falta: " + Utilidades.formatearMoneda(-cambio));
            } else {
                lblCambio.setText("Cambio: " + Utilidades.formatearMoneda(cambio));
            }
        } catch (NumberFormatException e) {
            lblCambio.setText("");
        }
    }

    @FXML
    private void cobrarVenta(ActionEvent event) {
        if (carrito.isEmpty()) {
            Alertas.mostrarAdvertencia("Venta vacía", "Agrega al menos un producto antes de cobrar.");
            return;
        }

        FormaPago formaPago = comboFormaPago.getValue();
        ventaActual.setFormaPago(formaPago);
        ventaActual.setEsFiado(chkEsFiado.isSelected());
        ventaActual.setIdUsuario(SesionActual.getUsuario().getId());

        if (formaPago == FormaPago.EFECTIVO) {
            try {
                double recibido = Double.parseDouble(txtMontoRecibido.getText());
                if (recibido < ventaActual.getTotal()) {
                    Alertas.mostrarAdvertencia("Falta dinero", "El monto recibido es menor al total de la venta.");
                    return;
                }
            } catch (NumberFormatException e) {
                Alertas.mostrarAdvertencia("Falta el monto recibido", "Escribe cuánto dinero entregó el cliente.");
                return;
            }
        }

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
            Ticket ticket = ventaService.realizarVenta(ventaActual);
            mostrarTicket(ticket, ventaActual);

            carrito.clear();
            ventaActual = new Venta();
            chkEsFiado.setSelected(false);
            txtMontoRecibido.clear();
            lblCambio.setText("");
            actualizarTotal();

        } catch (IllegalArgumentException | IllegalStateException e) {
            Alertas.mostrarAdvertencia("No se pudo completar la venta", e.getMessage());
        } catch (RuntimeException e) {
            Alertas.mostrarError("Error de conexión",
                    "No se pudo conectar a la base de datos. Verifica que MySQL esté encendido.");
        }
    }

    /**
     * Botón "+ Producto nuevo" (solo Administrador): abre una ventana rápida
     * para dar de alta un producto sin salir de la pantalla de Vender, y lo
     * agrega directo al carrito de la venta actual.
     */
    @FXML
    private void abrirFormularioProductoNuevo(ActionEvent event) {
        TextField campoCodigo = new TextField();
        campoCodigo.setPromptText("Código de barras (opcional)");

        TextField campoNombre = new TextField();
        campoNombre.setPromptText("Nombre del producto");

        TextField campoPrecioVenta = new TextField();
        campoPrecioVenta.setPromptText("Precio de venta");

        TextField campoPrecioCosto = new TextField();
        campoPrecioCosto.setPromptText("Precio de costo");

        TextField campoStock = new TextField();
        campoStock.setPromptText("Existencia inicial");
        campoStock.setText("0");

        TextField campoPuntoReorden = new TextField();
        campoPuntoReorden.setPromptText("Punto de reorden");
        campoPuntoReorden.setText("5");

        CheckBox campoGranel = new CheckBox("Se vende a granel (por kilo)");

        Button botonGuardar = new Button("Guardar y agregar a la venta");

        VBox formulario = new VBox(10,
                new Label("Nuevo producto"),
                campoCodigo, campoNombre, campoPrecioVenta, campoPrecioCosto,
                campoStock, campoPuntoReorden, campoGranel, botonGuardar);
        formulario.setStyle("-fx-padding: 20; -fx-alignment: center-left;");

        Stage ventanaNueva = new Stage();
        ventanaNueva.setTitle("Agregar producto nuevo");
        ventanaNueva.setScene(new Scene(formulario, 320, 400));

        botonGuardar.setOnAction(e -> {
            try {
                Producto nuevo = new Producto();
                nuevo.setCodigoBarras(campoCodigo.getText().isBlank() ? null : campoCodigo.getText());
                nuevo.setNombre(campoNombre.getText());
                nuevo.setPrecioVenta(Double.parseDouble(campoPrecioVenta.getText()));
                nuevo.setPrecioCosto(campoPrecioCosto.getText().isBlank() ? 0 : Double.parseDouble(campoPrecioCosto.getText()));
                nuevo.setStock(Double.parseDouble(campoStock.getText()));
                nuevo.setPuntoReorden(Double.parseDouble(campoPuntoReorden.getText()));
                nuevo.setEsGranel(campoGranel.isSelected());

                productoService.guardar(nuevo);

                agregarProductoAlCarrito(nuevo, 1);

                Alertas.mostrarInformacion("Listo", "Producto agregado al catálogo y a la venta actual.");
                ventanaNueva.close();

            } catch (NumberFormatException ex) {
                Alertas.mostrarAdvertencia("Dato inválido", "Revisa que los precios y números sean válidos.");
            } catch (IllegalArgumentException ex) {
                Alertas.mostrarAdvertencia("Datos incompletos", ex.getMessage());
            }
        });

        ventanaNueva.show();
    }

    private void mostrarTicket(Ticket ticket, Venta venta) {
        StringBuilder sb = new StringBuilder();
        sb.append("PUNTOMAYA\n");
        sb.append("Ticket #").append(ticket.getFolio()).append("\n");
        sb.append(ticket.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        sb.append("------------------------------\n");
        for (DetalleVenta d : venta.getDetalles()) {
            sb.append(String.format("%-16s x%-4s %8s%n",
                    d.getNombreProducto(), d.getCantidad(), Utilidades.formatearMoneda(d.getImporte())));
        }
        sb.append("------------------------------\n");
        sb.append(String.format("%-20s %9s%n", "TOTAL", Utilidades.formatearMoneda(venta.getTotal())));
        sb.append("Forma de pago: ").append(venta.getFormaPago()).append("\n");
        sb.append("\n¡Gracias por su compra!");

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(Font.font("Consolas", 12));
        textArea.setPrefSize(300, 380);

        Button btnImprimir = new Button("Imprimir");
        btnImprimir.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(textArea.getScene().getWindow())) {
                boolean ok = job.printPage(textArea);
                if (ok) job.endJob();
            }
        });

        VBox contenedor = new VBox(10, textArea, btnImprimir);
        contenedor.setStyle("-fx-padding: 16; -fx-alignment: center;");

        Stage ventana = new Stage();
        ventana.setTitle("Ticket de venta");
        ventana.setScene(new Scene(contenedor));
        ventana.show();
    }

    @FXML
    private void cancelarVentaCobrada(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar venta ya cobrada");
        dialog.setHeaderText(null);
        dialog.setContentText("Número de venta (id) a cancelar:");
        Optional<String> resultado = dialog.showAndWait();

        resultado.ifPresent(texto -> {
            try {
                int idVenta = Integer.parseInt(texto.trim());
                boolean confirmar = Alertas.confirmar("Confirmar cancelación",
                        "¿Seguro que quieres cancelar la venta #" + idVenta + "? Esta acción repone el inventario y queda registrada.");
                if (confirmar) {
                    ventaService.cancelarVenta(idVenta, SesionActual.getUsuario().getId());
                    Alertas.mostrarInformacion("Listo", "La venta #" + idVenta + " fue cancelada.");
                }
            } catch (NumberFormatException e) {
                Alertas.mostrarAdvertencia("Dato inválido", "Escribe solo el número de la venta.");
            } catch (IllegalArgumentException e) {
                Alertas.mostrarAdvertencia("No se pudo cancelar", e.getMessage());
            }
        });
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