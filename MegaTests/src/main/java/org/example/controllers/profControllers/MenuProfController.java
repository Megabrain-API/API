package org.example.controllers.profControllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.App;

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

    @FXML private Button banco_questoes;

    @FXML private Label lbl_boas_vindas_prof;

    @FXML private Label lbl_subtitulo_prof;

    private final Map<Button, String> textosOriginais = new HashMap<>();
    private Button[] botoesMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        botoesMenu = new Button[]{menu_minhaturma, minhas_provas, banco_questoes, menu_dados_pessoais_prof, menu_sair_prof};

        for (Button btn : botoesMenu) {
            if (btn != null) {
                textosOriginais.put(btn, btn.getText());
            }
        }

        ocultarTextosBotoes();
        carregarSaudacaoUsuario();
    }

    private void carregarSaudacaoUsuario() {
        String nome = SessaoUsuarioController.getNomeUsuarioLogado();
        Long matricula = SessaoUsuarioController.getIdUsuarioLogado();

        if (lbl_boas_vindas_prof != null) {
            if (nome != null && !nome.isBlank()) {
                lbl_boas_vindas_prof.setText("Bem-vindo(a), " + nome + "!");
            } else {
                lbl_boas_vindas_prof.setText("Bem-vindo(a) ao MegaTests!");
            }
        }

        if (lbl_subtitulo_prof != null && matricula != null) {
            lbl_subtitulo_prof.setText("Matrícula: " + matricula + "  •  Selecione uma opção abaixo ou na barra lateral para começar.");
        }
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
    void deslogar_prof(ActionEvent event) throws IOException {
        SessaoUsuarioController.encerrarSessao();
        App.setRoot("professorFluxo/TelaLoginProf");
    }

    @FXML
    void entra_dados_pessoais_prof(ActionEvent event) throws IOException {
        App.abrirDadosPessoais();
    }

    @FXML
    void entra_listaprova(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/ListaProva");
    }

    @FXML
    void entra_banco_questoes(ActionEvent event) throws IOException {
        BancoQuestoesController.configurarModoGerenciamento();
        App.setRoot("professorFluxo/BancoQuestoes");
    }

    @FXML
    void entra_minhaturma(ActionEvent event) throws IOException {
        TurmasProfessorController.limparContexto();
        App.setRoot("professorFluxo/turmas_professor");
    }
}