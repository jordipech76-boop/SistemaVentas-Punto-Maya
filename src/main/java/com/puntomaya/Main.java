package com.puntomaya;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

/**
 * Punto de entrada del sistema PuntoMaya.
 * Carga la pantalla de Login como primera ventana.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/puntomaya/view/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/puntomaya/view/estilos.css").toExternalForm());

        stage.setTitle("PuntoMaya - Iniciar sesión");
        stage.getIcons().add(new Image(getClass().getResourceAsStream
                ("/com/puntomaya/view/imagenes/icono_puntomaya.png")));
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
