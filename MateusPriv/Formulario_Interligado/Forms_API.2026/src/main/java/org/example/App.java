package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    private static final double ASPECT_RATIO = 16.0 / 9.0;
    private boolean resizing = false;

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Inicializa em 16:9 (960x540)
        scene = new Scene(loadFXML("telainicio"), 960, 540);
        stage.setScene(scene);

        // 2. Redimensiona a altura se puxar a largura
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (resizing) return;
            resizing = true;
            stage.setHeight(newVal.doubleValue() / ASPECT_RATIO);
            resizing = false;
        });

        // 3. Redimensiona a largura se puxar a altura
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (resizing) return;
            resizing = true;
            stage.setWidth(newVal.doubleValue() * ASPECT_RATIO);
            resizing = false;
        });

        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}