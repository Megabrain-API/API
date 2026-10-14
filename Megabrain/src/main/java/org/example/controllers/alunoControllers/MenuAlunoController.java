package org.example.controllers.alunoControllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.App;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MenuAlunoController implements Initializable {

    @FXML
    private VBox barra_lateral;

    @FXML
    private Button dados_pessoais_alu;

    @FXML
    private Button menu_meucurso;

    @FXML
    private Button menu_sair_aluno;

    @FXML
    private Button minhas_provas_aluno;

    private final Map<Button, String> textosOriginais = new HashMap<>();
    private Button[] botoesMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        botoesMenu = new Button[]{menu_meucurso, minhas_provas_aluno, dados_pessoais_alu, menu_sair_aluno};

        for (Button btn : botoesMenu) {
            if (btn != null) {
                textosOriginais.put(btn, btn.getText());
            }
        }

        ocultarTextosBotoes();
    }

    @FXML
    private void expande_barra() {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(barra_lateral.prefWidthProperty(), 170.0);
        KeyFrame kf = new KeyFrame(Duration.millis(130), kv);
        timeline.getKeyFrames().add(kf);

        timeline.setOnFinished(event -> mostrarTextosBotoes());
        timeline.play();
    }

    @FXML
    private void minimiza_barra() {
        ocultarTextosBotoes();

        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(barra_lateral.prefWidthProperty(), 60.0);
        KeyFrame kf = new KeyFrame(Duration.millis(150), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    private void ocultarTextosBotoes() {
        for (Button btn : botoesMenu) {
            if (btn != null) {
                btn.setText("");
            }
        }
    }

    private void mostrarTextosBotoes() {
        for (Button btn : botoesMenu) {
            if (btn != null) {
                btn.setText(textosOriginais.get(btn));
            }
        }
    }

    @FXML
    void deslogar_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/TelaLoginAlu");
    }

    @FXML
    void entra_dados_pessoais_alu(ActionEvent event) {

    }

    @FXML
    void entra_menuprova_aluno(ActionEvent event) {

    }

    @FXML
    void entra_meucurso(ActionEvent event) {

    }

}
