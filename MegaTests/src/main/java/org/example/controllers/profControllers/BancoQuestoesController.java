package org.example.controllers.profControllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.App;
import org.example.dao.DisciplinaDAO;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BancoQuestoesController implements Initializable {

    // --- Barra lateral do Professor ---
    @FXML
    private VBox barra_lateral;

    @FXML
    private Button menu_minhaturma;

    @FXML
    private Button minhas_provas;

    @FXML
    private Button menu_dados_pessoais_prof;

    @FXML
    private Button menu_sair_prof;

    // --- Filtros e Listagem do Banco de Questões ---
    @FXML
    private Label lbl_total_questoes;

    @FXML
    private TextField txt_filtro_enunciado;

    @FXML
    private ComboBox<String> cb_filtro_disciplina;

    @FXML
    private VBox vbox_questoes_banco;

    private final Map<Button, String> textosOriginais = new HashMap<>();
    private Button[] botoesMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        CarregaCombobox();
        configurarBarraLateral();
        carregarQuestoes();
        configurarListenersFiltro();
    }

    private void configurarBarraLateral() {
        botoesMenu = new Button[]{
                menu_minhaturma,
                minhas_provas,
                menu_dados_pessoais_prof,
                menu_sair_prof
        };

        for (Button btn : botoesMenu) {
            if (btn != null) {
                textosOriginais.put(btn, btn.getText());
            }
        }

        ocultarTextosBotoes();
    }

    private void carregarQuestoes() {
        // TODO: Buscar questões via QuestaoDAO e renderizar os cards em vbox_questoes_banco
    }

    private void configurarListenersFiltro() {
        // TODO: Adicionar listeners em txt_filtro_enunciado e cb_filtro_disciplina para filtrar a lista
    }

    // --- Animação da Barra Lateral ---
    @FXML
    private void expande_barra() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(130), new KeyValue(barra_lateral.prefWidthProperty(), 170.0))
        );
        timeline.setOnFinished(event -> mostrarTextosBotoes());
        timeline.play();
    }

    @FXML
    private void minimiza_barra() {
        ocultarTextosBotoes();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(150), new KeyValue(barra_lateral.prefWidthProperty(), 60.0))
        );
        timeline.play();
    }

    private void ocultarTextosBotoes() {
        if (botoesMenu == null) return;
        for (Button btn : botoesMenu) {
            if (btn != null) btn.setText("");
        }
    }

    public void CarregaCombobox() {
        DisciplinaDAO dao = new DisciplinaDAO();
        List<String> listaNomes = dao.listarNomesDisciplinas();
        ObservableList<String> dadosObservable = FXCollections.observableArrayList(listaNomes);
        cb_filtro_disciplina.setItems(dadosObservable);
    }


    private void mostrarTextosBotoes() {
        if (botoesMenu == null) return;
        for (Button btn : botoesMenu) {
            if (btn != null) btn.setText(textosOriginais.get(btn));
        }
    }

    // --- Navegação do Menu do Professor ---
    @FXML
    void entra_minhaturma(ActionEvent event) throws IOException {
        // TODO: Ajustar para o nome exato do FXML de turmas do professor
        // App.setRoot("professorFluxo/MinhaTurmaProf");
    }

    @FXML
    void entra_listaprova(ActionEvent event) throws IOException {
        // TODO: Ajustar para o nome exato do FXML de lista de provas do professor
        // App.setRoot("professorFluxo/ListaProvas");
    }

    @FXML
    void entra_dados_pessoais_prof(ActionEvent event) throws IOException {
        // TODO: Implementar navegação para dados pessoais do professor
    }

    @FXML
    void deslogar_prof(ActionEvent event) throws IOException {
        // TODO: Ajustar para o nome exato da tela de login do professor
        // App.setRoot("professorFluxo/TelaLoginProf");
    }

    @FXML
    void voltarMenuProfessor(ActionEvent event) throws IOException {
        // TODO: Ajustar para o menu principal do professor
        // App.setRoot("professorFluxo/MenuProf");
    }
}