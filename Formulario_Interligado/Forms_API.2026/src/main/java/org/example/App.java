package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Carrega a tela inicial que está na raiz da pasta org.example
        scene = new Scene(loadFXML("professorFluxo/CriaProva"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        // Com o "/org/example/", ele consegue buscar tanto na raiz quanto em subpastas (ex: "alunoFluxo/tela")
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/org/example/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
