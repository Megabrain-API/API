package org.example.controllers.alunoControllers;

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
import org.example.controllers.profControllers.SessaoUsuarioController;

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

    @FXML
    private Label lbl_boas_vindas_aluno;

    @FXML
    private Label lbl_subtitulo_aluno;

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
        carregarSaudacaoUsuario();
    }

    private void carregarSaudacaoUsuario() {
        String nome = SessaoUsuarioController.getNomeUsuarioLogado();
        Long ra = SessaoUsuarioController.getIdUsuarioLogado();

        if (lbl_boas_vindas_aluno != null) {
            if (nome != null && !nome.isBlank()) {
                lbl_boas_vindas_aluno.setText("Bem-vindo(a), " + nome + "!");
            } else {
                lbl_boas_vindas_aluno.setText("Bem-vindo(a) ao MegaTests!");
            }
        }

        if (lbl_subtitulo_aluno != null && ra != null) {
            lbl_subtitulo_aluno.setText("RA: " + ra + "  •  Selecione uma opção abaixo ou na barra lateral para começar.");
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
    void deslogar_aluno(ActionEvent event) throws IOException {
        SessaoUsuarioController.encerrarSessao();
        App.setRoot("alunoFluxo/TelaLoginAlu");
    }

    @FXML
    void entra_dados_pessoais_alu(ActionEvent event) throws IOException {
        App.abrirDadosPessoais();
    }

    @FXML
    void entra_menuprova_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/ProvasAluno");
    }

    @FXML
    void entra_meucurso(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/MeuCursoAluno");
    }
}