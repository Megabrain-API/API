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
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.example.App;
import org.example.controllers.profControllers.SessaoUsuarioController;
import org.example.dao.ProvaDAO;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.SessaoApp;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ProvasAlunoController implements Initializable {

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

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
    private Label lbl_resumo_pendentes;

    @FXML
    private VBox vb_provacontainer_aluno;

    private final Map<Button, String> textosOriginais = new HashMap<>();
    private Button[] botoesMenu;

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
        provaSelecionadaParaAbertura = null;

        try {
            Long raAlunoLogado = SessaoUsuarioController.getIdUsuarioLogado();

            if (raAlunoLogado == null) {
                throw new IllegalStateException("Nenhum aluno autenticado na sessão atual.");
            }

            ProvaDAO dao = new ProvaDAO();
            List<Prova> listaProvas = dao.listarProvasPorAluno(raAlunoLogado);

            if (listaProvas == null || listaProvas.isEmpty()) {
                if (lbl_resumo_pendentes != null) {
                    lbl_resumo_pendentes.setText("0 prova(s) pendente(s) no momento");
                }
                mostrarEstadoSemProvas();
                return;
            }

            Map<String, List<Prova>> provasPorMateria = new LinkedHashMap<>();
            for (Prova prova : listaProvas) {
                String nomeMateria = (prova.getDisciplina() != null && !prova.getDisciplina().isBlank())
                        ? prova.getDisciplina()
                        : "Outras Disciplinas";
                provasPorMateria.computeIfAbsent(nomeMateria, k -> new ArrayList<>()).add(prova);
            }

            if (lbl_resumo_pendentes != null) {
                lbl_resumo_pendentes.setText(
                        listaProvas.size() + " prova(s) pendente(s) distribuída(s) em "
                                + provasPorMateria.size() + " matéria(s)"
                );
            }

            for (Map.Entry<String, List<Prova>> entry : provasPorMateria.entrySet()) {
                vb_provacontainer_aluno.getChildren().add(
                        criarCardMateria(entry.getKey(), entry.getValue())
                );
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

    private VBox criarCardMateria(String nomeMateria, List<Prova> provas) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.96);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: #D4DFEC;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);"
        );

        HBox topoCard = new HBox(12);
        topoCard.setAlignment(Pos.CENTER_LEFT);

        Label lblMateria = new Label(nomeMateria);
        lblMateria.setFont(Font.font("System", FontWeight.BOLD, 17));
        lblMateria.setStyle("-fx-text-fill: #0066FF;");

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        Label badgeQtd = new Label(
                provas.size() + (provas.size() == 1 ? " prova pendente" : " provas pendentes")
        );
        badgeQtd.setPadding(new Insets(4, 12, 4, 12));
        badgeQtd.setStyle(
                "-fx-background-color: #E3EEFF;" +
                        "-fx-text-fill: #004EC2;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 12px;"
        );

        topoCard.getChildren().addAll(lblMateria, espacador, badgeQtd);
        card.getChildren().addAll(topoCard, new Separator());

        VBox listaLinhas = new VBox(10);
        for (Prova prova : provas) {
            listaLinhas.getChildren().add(criarLinhaProva(prova));
        }
        card.getChildren().add(listaLinhas);

        return card;
    }

    private HBox criarLinhaProva(Prova prova) {
        HBox linha = new HBox(14);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setPadding(new Insets(12, 16, 12, 16));
        linha.setStyle(
                "-fx-background-color: #F7FAFF;" +
                        "-fx-background-radius: 9px;" +
                        "-fx-border-color: #DCE6F5;" +
                        "-fx-border-radius: 9px;" +
                        "-fx-border-width: 1px;"
        );

        VBox infoProva = new VBox(3);

        Label lblNomeProva = new Label(prova.getNome());
        lblNomeProva.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblNomeProva.setStyle("-fx-text-fill: #1B1B1B;");

        Label lblDetalhes = new Label(montarDetalhesProva(prova));
        lblDetalhes.setFont(Font.font("System", 12));
        lblDetalhes.setStyle("-fx-text-fill: #5F6368;");

        infoProva.getChildren().addAll(lblNomeProva, lblDetalhes);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        Button btnAcessar = new Button("Acessar Prova");
        btnAcessar.setPadding(new Insets(8, 18, 8, 18));
        String estiloNormal =
                "-fx-background-color: #0066FF;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 7px;" +
                        "-fx-cursor: hand;";
        String estiloHover =
                "-fx-background-color: #0052CC;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 7px;" +
                        "-fx-cursor: hand;";

        btnAcessar.setStyle(estiloNormal);
        btnAcessar.setOnMouseEntered(e -> btnAcessar.setStyle(estiloHover));
        btnAcessar.setOnMouseExited(e -> btnAcessar.setStyle(estiloNormal));
        btnAcessar.setOnAction(evento -> abrirProva(prova));

        linha.getChildren().addAll(infoProva, espacador, btnAcessar);
        return linha;
    }

    private String montarDetalhesProva(Prova prova) {
        List<String> partes = new ArrayList<>();

        if (prova.getDuracaoMinutos() > 0) {
            partes.add("Duração: " + prova.getDuracaoMinutos() + " min");
        }

        if (prova.getDataLimite() != null) {
            partes.add("Disponível até " + prova.getDataLimite().format(FORMATO_DATA_HORA));
        } else {
            partes.add("Sem prazo limite definido");
        }

        return String.join("  •  ", partes);
    }

    private void mostrarEstadoSemProvas() {
        VBox cardVazio = new VBox(8);
        cardVazio.setAlignment(Pos.CENTER);
        cardVazio.setPadding(new Insets(36, 24, 36, 24));
        cardVazio.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.96);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: #D4DFEC;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);"
        );

        Label lblTituloVazio = new Label("Nenhuma prova pendente no momento");
        lblTituloVazio.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTituloVazio.setStyle("-fx-text-fill: #1B1B1B;");

        Label lblSubVazio = new Label(
                "Você já concluiu todas as avaliações disponíveis ou ainda não há provas liberadas para o seu curso."
        );
        lblSubVazio.setWrapText(true);
        lblSubVazio.setStyle("-fx-text-fill: #5F6368; -fx-font-size: 13px;");

        cardVazio.getChildren().addAll(lblTituloVazio, lblSubVazio);
        vb_provacontainer_aluno.getChildren().add(cardVazio);
    }

    private void abrirProva(Prova prova) {
        if (prova.getDataLimite() != null && LocalDateTime.now().isAfter(prova.getDataLimite())) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Prazo encerrado");
            alerta.setHeaderText("Esta prova não está mais disponível");
            alerta.setContentText("O prazo limite para iniciar esta avaliação já foi ultrapassado.");
            alerta.showAndWait();
            carregaProvasSalvas();
            return;
        }

        provaSelecionadaParaAbertura = prova;
        SessaoApp.provaAtual = prova;
        try {
            App.setRoot("alunoFluxo/FazerProvaAlu");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText("Não foi possível abrir a prova");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
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