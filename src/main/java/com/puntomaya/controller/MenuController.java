package com.puntomaya.controller;

import com.puntomaya.model.RolUsuario;
import com.puntomaya.util.SesionActual;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador del menú principal.
 */
public class MenuController {

    @FXML private Label lblBienvenida;
    @FXML private Button btnVentas;
    @FXML private Button btnClientes;
    @FXML private Button btnCorteCaja;
    @FXML private Button btnProductos;
    @FXML private Button btnInventario;
    @FXML private Button btnReportes;

    @FXML
    public void initialize() {
        var usuario = SesionActual.getUsuario();
        if (usuario != null) {
            lblBienvenida.setText("Hola, " + usuario.getNombre() + " (" + usuario.getRol() + ")");
            aplicarPermisosPorRol(usuario.getRol());
        }
    }

    /**
     * Cajero: Ventas, Clientes y Corte de Caja.
     * Almacenista: Productos e Inventario.
     * Administrador: ve todo.
     */
    private void aplicarPermisosPorRol(RolUsuario rol) {
        switch (rol) {
            case CAJERO -> {
                btnProductos.setVisible(false);
                btnInventario.setVisible(false);
                btnReportes.setVisible(false);
            }
            case ALMACENISTA -> {
                btnVentas.setVisible(false);
                btnClientes.setVisible(false);
                btnCorteCaja.setVisible(false);
                btnReportes.setVisible(false);
            }
            case ADMINISTRADOR -> {
                // ve todas las opciones
            }
        }
    }

    @FXML
    private void abrirVentas(ActionEvent event) throws IOException {
        cambiarPantalla(event, "/com/puntomaya/view/Ventas.fxml", "PuntoMaya - Ventas");
    }

    @FXML
    private void abrirClientes(ActionEvent event) throws IOException {
        cambiarPantalla(event, "/com/puntomaya/view/Clientes.fxml", "PuntoMaya - Clientes");
    }

    @FXML
    private void abrirCorteCaja(ActionEvent event) throws IOException {
        cambiarPantalla(event, "/com/puntomaya/view/CorteCaja.fxml", "PuntoMaya - Corte de Caja");
    }

    @FXML
    private void abrirProductos(ActionEvent event) throws IOException {
        cambiarPantalla(event, "/com/puntomaya/view/Productos.fxml", "PuntoMaya - Productos");
    }

    @FXML
    private void abrirInventario(ActionEvent event) throws IOException {
        cambiarPantalla(event, "/com/puntomaya/view/Inventario.fxml", "PuntoMaya - Inventario");
    }

    @FXML
    private void abrirReportes(ActionEvent event) throws IOException {
        cambiarPantalla(event, "/com/puntomaya/view/Reportes.fxml", "PuntoMaya - Reportes");
    }

    @FXML
    private void cerrarSesion(ActionEvent event) throws IOException {
        SesionActual.cerrar();
        cambiarPantalla(event, "/com/puntomaya/view/Login.fxml", "PuntoMaya - Iniciar sesión");
    }

    private void cambiarPantalla(ActionEvent event, String rutaFxml, String titulo) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
        Parent root = loader.load();
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(titulo);
        stage.centerOnScreen();
    }
}