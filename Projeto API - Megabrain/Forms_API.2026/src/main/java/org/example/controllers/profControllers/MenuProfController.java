package org.example.controllers.profControllers;

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
import org.example.controllers.profControllers.ListaProvaController;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MenuProfController implements Initializable {

    @FXML private VBox barra_lateral;

    @FXML private Button menu_dados_pessoais_prof;

    @FXML private Button menu_minhaturma;

    @FXML private Button menu_sair_prof;

    @FXML private Button minhas_provas;

    // Mapa para salvar dinamicamente os textos configurados no FXML/Scene Builder
    private final Map<Button, String> textosOriginais = new HashMap<>();
    private Button[] botoesMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        botoesMenu = new Button[]{menu_minhaturma, minhas_provas, menu_dados_pessoais_prof, menu_sair_prof};

        // 1. Salva na memória o texto de cada botão definido no FXML
        for (Button btn : botoesMenu) {
            if (btn != null) {
                textosOriginais.put(btn, btn.getText());
            }
        }

        // 2. Garante que os botões comecem ocultando os textos (barra inicializada em 60px)
        ocultarTextosBotoes();
    }

    @FXML
    private void expande_barra() {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(barra_lateral.prefWidthProperty(), 170.0);
        KeyFrame kf = new KeyFrame(Duration.millis(130), kv);
        timeline.getKeyFrames().add(kf);

        // O texto só reaparece quando a animação de abertura chega ao fim
        timeline.setOnFinished(event -> mostrarTextosBotoes());
        timeline.play();
    }

    @FXML
    private void minimiza_barra() {
        // Remove os textos instantaneamente antes de começar a fechar
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
                btn.setText(""); // Esvazia o texto para fixar o ícone na esquerda
            }
        }
    }

    private void mostrarTextosBotoes() {
        for (Button btn : botoesMenu) {
            if (btn != null) {
                btn.setText(textosOriginais.get(btn)); // Devolve o texto salvo originalmente
            }
        }
    }

    @FXML
    void deslogar_prof(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/TelaLoginProf");
    }

    @FXML
    void entra_dados_pessoais_prof(ActionEvent event) {
        // Lógica futura
    }

    @FXML
    void entra_listaprova(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/ListaProva");
    }

    @FXML
    void entra_minhaturma(ActionEvent event) {
        // Lógica futura
    }
}
