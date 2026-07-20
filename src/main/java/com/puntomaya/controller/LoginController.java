package com.puntomaya.controller;

import com.puntomaya.model.Usuario;
import com.puntomaya.service.UsuarioService;
import com.puntomaya.util.Alertas;
import com.puntomaya.util.SesionActual;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * Controlador de la pantalla de inicio de sesión.
 */
public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Button btnIngresar;

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    private void iniciarSesion(ActionEvent event) {
        String nombreUsuario = txtUsuario.getText();
        String contrasena = txtContrasena.getText();

        try {
            Optional<Usuario> resultado = usuarioService.iniciarSesion(nombreUsuario, contrasena);

            if (resultado.isEmpty()) {
                Alertas.mostrarError("Acceso denegado", "Usuario o contraseña incorrectos.");
                return;
            }

            SesionActual.iniciar(resultado.get());
            abrirMenuPrincipal(event);

        } catch (IllegalArgumentException e) {
            Alertas.mostrarAdvertencia("Datos incompletos", e.getMessage());
        } catch (RuntimeException e) {
            Alertas.mostrarError("Error de conexión",
                    "No se pudo conectar a la base de datos. Verifica que MySQL esté encendido.\n\n"
                            + e.getMessage());
        }
    }

    private void abrirMenuPrincipal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/puntomaya/view/Menu.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PuntoMaya - Menú principal");
            stage.centerOnScreen();

        } catch (IOException e) {
            Alertas.mostrarError("Error", "No se pudo abrir el menú principal.\n" + e.getMessage());
        }
    }
}
