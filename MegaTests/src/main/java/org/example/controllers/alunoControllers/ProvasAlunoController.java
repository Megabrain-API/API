package org.example.controllers.alunoControllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.App;
import org.example.controllers.profControllers.SessaoUsuarioController;
import org.example.dao.ProvaDAO;
import org.example.entity.profEntity.Prova;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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

    private Prova provaSelecionadaTemporaria;
    private Button botaoSelecionadoAnteriormente;

    private static Prova provaSelecionadaParaAbertura;

    public static Prova getProvaSelecionadaParaAbertura() {
        return provaSelecionadaParaAbertura;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        botoesMenu = new Button[]{
                menu_meucurso,
                minhas_provas_aluno,
                dados_pessoais_alu,
                menu_sair_aluno
        };

        for (Button btn : botoesMenu) {
            if (btn != null) {
                textosOriginais.put(btn, btn.getText());
            }
        }

        ocultarTextosBotoes();
        carregaProvasSalvas();
    }

    @FXML
    private void expande_barra() {
        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(130),
                        new KeyValue(barra_lateral.prefWidthProperty(), 170.0)
                )
        );

        timeline.setOnFinished(event -> mostrarTextosBotoes());
        timeline.play();
    }

    @FXML
    private void minimiza_barra() {
        ocultarTextosBotoes();

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(150),
                        new KeyValue(barra_lateral.prefWidthProperty(), 60.0)
                )
        );

        timeline.play();
    }

    private void ocultarTextosBotoes() {
        if (botoesMenu == null) {
            return;
        }

        for (Button btn : botoesMenu) {
            if (btn != null) {
                btn.setText("");
            }
        }
    }

    private void mostrarTextosBotoes() {
        if (botoesMenu == null) {
            return;
        }

        for (Button btn : botoesMenu) {
            if (btn != null) {
                btn.setText(textosOriginais.get(btn));
            }
        }
    }

    private void carregaProvasSalvas() {
        if (vb_provacontainer_aluno == null) {
            return;
        }

        vb_provacontainer_aluno.getChildren().clear();
        provaSelecionadaTemporaria = null;
        provaSelecionadaParaAbertura = null;
        botaoSelecionadoAnteriormente = null;

        try {
            Long raAlunoLogado = SessaoUsuarioController.getIdUsuarioLogado();

            if (raAlunoLogado == null) {
                throw new IllegalStateException("Nenhum aluno autenticado na sessão atual.");
            }

            ProvaDAO dao = new ProvaDAO();
            // Mostra somente provas de disciplinas que pertencem ao curso deste aluno.
            List<Prova> listaProvas = dao.listarProvasPorAluno(raAlunoLogado);

            if (listaProvas == null || listaProvas.isEmpty()) {
                return;
            }

            for (Prova provaRecuperada : listaProvas) {
                String textoBotao = provaRecuperada.getNome();
                if (provaRecuperada.getDisciplina() != null) {
                    textoBotao += " - " + provaRecuperada.getDisciplina();
                }

                Button btnProva = new Button(textoBotao);

                btnProva.setMaxWidth(Double.MAX_VALUE);
                btnProva.setStyle(
                        "-fx-font-size: 14px;" +
                                "-fx-padding: 10px;" +
                                "-fx-cursor: hand;" +
                                "-fx-background-radius: 9px;"
                );

                HBox linhaDaProva = new HBox(10);
                linhaDaProva.setAlignment(Pos.CENTER_LEFT);
                linhaDaProva.setPadding(new Insets(5, 15, 5, 15));

                HBox.setHgrow(btnProva, Priority.ALWAYS);

                btnProva.setOnAction(evento -> {
                    provaSelecionadaTemporaria = provaRecuperada;

                    if (botaoSelecionadoAnteriormente != null) {
                        botaoSelecionadoAnteriormente.setStyle(
                                "-fx-font-size: 14px;" +
                                        "-fx-padding: 10px;" +
                                        "-fx-cursor: hand;" +
                                        "-fx-background-radius: 9px;"
                        );
                    }

                    btnProva.setStyle(
                            "-fx-font-size: 14px;" +
                                    "-fx-padding: 10px;" +
                                    "-fx-cursor: hand;" +
                                    "-fx-background-radius: 9px;" +
                                    "-fx-background-color: #add8e6;"
                    );

                    botaoSelecionadoAnteriormente = btnProva;
                });

                linhaDaProva.getChildren().add(btnProva);
                vb_provacontainer_aluno.getChildren().add(linhaDaProva);
            }
        } catch (Exception e) {
            e.printStackTrace();

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText("Não foi possível carregar as provas");
            alerta.setContentText(
                    "Verifique a conexão com o banco de dados e tente novamente."
            );
            alerta.showAndWait();
        }
    }

    @FXML
    void AcessaProva_aluno(ActionEvent event) throws IOException {
        if (provaSelecionadaTemporaria == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Nenhuma prova selecionada");
            alerta.setHeaderText("Selecione uma prova");
            alerta.setContentText(
                    "Clique no nome da prova que deseja acessar antes de continuar."
            );
            alerta.showAndWait();
            return;
        }

        provaSelecionadaParaAbertura = provaSelecionadaTemporaria;
        App.setRoot("alunoFluxo/FazerProvaAlu");
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