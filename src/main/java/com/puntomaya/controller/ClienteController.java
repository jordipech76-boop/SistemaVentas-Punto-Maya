package com.puntomaya.controller;

import com.puntomaya.model.Cliente;
import com.puntomaya.service.ClienteService;
import com.puntomaya.util.Alertas;
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

/**
 * Controlador de la pantalla de clientes: alta de clientes, consulta de
 * saldo de fiados y registro de abonos.
 */
public class ClienteController {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtLimiteCredito;

    @FXML
    private TableView<Cliente> tablaClientes;

    @FXML
    private TableColumn<Cliente, String> colNombre;

    @FXML
    private TableColumn<Cliente, String> colTelefono;

    @FXML
    private TableColumn<Cliente, Double> colLimite;

    @FXML
    private TableColumn<Cliente, Double> colSaldo;

    @FXML
    private TextField txtMontoAbono;

    private final ClienteService clienteService = new ClienteService();
    private final ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colLimite.setCellValueFactory(new PropertyValueFactory<>("limiteCredito"));
        colSaldo.setCellValueFactory(new PropertyValueFactory<>("saldoActual"));
        tablaClientes.setItems(listaClientes);
        cargarClientes();
    }

    private void cargarClientes() {
        listaClientes.setAll(clienteService.listar());
    }

    @FXML
    private void guardarCliente(ActionEvent event) {
        try {
            Cliente cliente = new Cliente();
            cliente.setNombre(txtNombre.getText());
            cliente.setTelefono(txtTelefono.getText());
            cliente.setLimiteCredito(Double.parseDouble(
                    txtLimiteCredito.getText().isBlank() ? "0" : txtLimiteCredito.getText()));
            cliente.setSaldoActual(0);

            clienteService.guardar(cliente);
            Alertas.mostrarInformacion("Listo", "Cliente guardado correctamente.");
            limpiarFormulario();
            cargarClientes();

        } catch (NumberFormatException e) {
            Alertas.mostrarAdvertencia("Dato inválido", "El límite de crédito debe ser un número.");
        } catch (IllegalArgumentException e) {
            Alertas.mostrarAdvertencia("Datos incompletos", e.getMessage());
        }
    }

    @FXML
    private void registrarAbono(ActionEvent event) {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.mostrarAdvertencia("Selecciona un cliente", "Elige un cliente de la tabla primero.");
            return;
        }
        try {
            double monto = Double.parseDouble(txtMontoAbono.getText());
            clienteService.registrarAbono(seleccionado.getId(), monto);
            Alertas.mostrarInformacion("Abono registrado",
                    "Se registró un abono de " + Utilidades.formatearMoneda(monto)
                            + " para " + seleccionado.getNombre());
            txtMontoAbono.clear();
            cargarClientes();

        } catch (NumberFormatException e) {
            Alertas.mostrarAdvertencia("Dato inválido", "El monto del abono debe ser un número.");
        } catch (IllegalArgumentException e) {
            Alertas.mostrarAdvertencia("No se pudo registrar", e.getMessage());
        }
    }

    @FXML
    private void eliminarCliente(ActionEvent event) {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        boolean confirmar = Alertas.confirmar("Eliminar cliente",
                "¿Seguro que quieres eliminar a " + seleccionado.getNombre() + "?");
        if (confirmar) {
            clienteService.eliminar(seleccionado.getId());
            cargarClientes();
        }
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtTelefono.clear();
        txtLimiteCredito.clear();
    }

    @FXML
    private void regresarAlMenu(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/puntomaya/view/Menu.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) tablaClientes.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("PuntoMaya - Menú principal");
    }
}
