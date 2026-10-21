package org.example.controllers.alunoControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.App;
import org.example.dao.ProvaDAO;
import org.example.entity.profEntity.Prova;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ProvasAlunoController implements Initializable {

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
    private VBox vb_provacontainer_aluno;

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

    // Variável para guardar qual prova que o usuario clicou
    private Prova provaSelecionadaTemporaria;

    // Variável para guardar o botão clicado
    private Button botaoSelecionadoAnteriormente;

    public void initialize() {
        carregaProvasSalvas();
    }

    // carregando as provas por meio do banco de dados
    private void carregaProvasSalvas() {
        ProvaDAO dao = new ProvaDAO();
        List<Prova> listaProvas = dao.listarNomeProvas();

        vb_provacontainer_aluno.getChildren().clear();

        // Passa por cada nome de prova retornado do banco
        for (Prova provaRecuperada : listaProvas) {

            try {
                // Cria o botão da prova
                Button btnProva = new Button(provaRecuperada.getNome());
                btnProva.setMaxWidth(Double.MAX_VALUE);
                btnProva.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand; -fx-background-radius: 9px;");

                // Cria o botão de remover (adicionei uma cor vermelha para destacar)
                Button btnRemove = new Button("Remover");
                btnRemove.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand; -fx-background-radius: 9px; -fx-background-color: #ff4444; -fx-text-fill: white;");

                // Cria a caixa horizontal (HBox) com um espaçamento de 10px entre os itens
                HBox linhaDaProva = new HBox(10);
                linhaDaProva.setAlignment(Pos.CENTER_LEFT);

                //Dá um espaçamento interno nas bordas da linha (cima, direita, baixo, esquerda)
                linhaDaProva.setPadding(new Insets(5, 15, 5, 15));

                // Faz o botão da prova esticar para ocupar o espaço vazio, empurrando o "Remover" para a direita
                HBox.setHgrow(btnProva, Priority.ALWAYS);

                // Define a ação do botão da Prova (abrir)
                btnProva.setOnAction(evento -> {
                    // Guarda a prova selecionada na variável temporária
                    provaSelecionadaTemporaria = provaRecuperada;

                    // Tira a cor "selecionada" do botão anterior (se houver)
                    if (botaoSelecionadoAnteriormente != null) {
                        botaoSelecionadoAnteriormente.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand; -fx-background-radius: 9px;");
                    }
                    // Pinta o botão atual para mostrar que está selecionado (ex: azul claro)
                    btnProva.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand; -fx-background-radius: 9px; -fx-background-color: #add8e6;");

                    // Guarda este botão como o último selecionado
                    botaoSelecionadoAnteriormente = btnProva;
                });

                // Coloca o botão dentro do HBox
                linhaDaProva.getChildren().addAll(btnProva);

                // Adiciona o HBox (com os dois botões) ao VBox principal da tela
                vb_provacontainer_aluno.getChildren().add(linhaDaProva);
            } catch (Exception e) {
                System.out.println("Erro ao buscar prova");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void AcessaProva_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/ProvaConfirma" );

    }

    @FXML
    void VoltarInicio_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/MenuAluno");
    }

    @FXML
    void deslogar_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/TelaLoginAlu");
    }
    @FXML
    void entra_dados_pessoais_alu(ActionEvent event) {

    }

    @FXML
    void entra_menuprova_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/ProvasAluno");

    }

    @FXML
    void entra_meucurso(ActionEvent event) {

    }


}
