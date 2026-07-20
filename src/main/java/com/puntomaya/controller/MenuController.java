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
 * Controlador del menú principal: desde aquí se navega a Ventas, Clientes,
 * Productos e Inventario, según el rol del usuario que inició sesión.
 */
public class MenuController {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Button btnVentas;

    @FXML
    private Button btnClientes;

    @FXML
    private Button btnProductos;

    @FXML
    private Button btnInventario;

    @FXML
    public void initialize() {
        var usuario = SesionActual.getUsuario();
        if (usuario != null) {
            lblBienvenida.setText("Hola, " + usuario.getNombre() + " (" + usuario.getRol() + ")");
            aplicarPermisosPorRol(usuario.getRol());
        }
    }

    /**
     * Oculta las opciones que no le corresponden al rol actual.
     * Cajero: solo Ventas y Clientes (para consultar fiados).
     * Almacenista: solo Productos e Inventario.
     * Administrador: ve todo.
     */
    private void aplicarPermisosPorRol(RolUsuario rol) {
        switch (rol) {
            case CAJERO -> {
                btnProductos.setVisible(false);
                btnInventario.setVisible(false);
            }
            case ALMACENISTA -> {
                btnVentas.setVisible(false);
                btnClientes.setVisible(false);
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
    private void abrirProductos(ActionEvent event) throws IOException {
        cambiarPantalla(event, "/com/puntomaya/view/Productos.fxml", "PuntoMaya - Productos");
    }

    @FXML
    private void abrirInventario(ActionEvent event) throws IOException {
        cambiarPantalla(event, "/com/puntomaya/view/Inventario.fxml", "PuntoMaya - Inventario");
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
