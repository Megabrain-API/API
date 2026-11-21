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
import org.example.dao.AlunoDAO;
import org.example.entity.alunoEntity.CursoAluno;
import org.example.entity.alunoEntity.MateriaAluno;
import org.example.entity.alunoEntity.ProvaCorrigidaAluno;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class MeuCursoAlunoController implements Initializable {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

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
    private Label lblNomeCurso;

    @FXML
    private Label lblResumoCurso;

    @FXML
    private VBox vboxMaterias;

    private final Map<Button, String> textosOriginais = new HashMap<>();
    private Button[] botoesMenu;

    private static ProvaCorrigidaAluno provaSelecionadaParaVisualizacao;
    private static String nomeCursoAtual;

    public static ProvaCorrigidaAluno getProvaSelecionadaParaVisualizacao() {
        return provaSelecionadaParaVisualizacao;
    }

    public static String getNomeCursoAtual() {
        return nomeCursoAtual;
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
        carregarCursoEMaterias();
    }

    private void carregarCursoEMaterias() {
        vboxMaterias.getChildren().clear();

        Long raAluno = SessaoUsuarioController.getIdUsuarioLogado();
        if (raAluno == null) {
            lblNomeCurso.setText("Aluno não autenticado");
            lblResumoCurso.setText("Faça login novamente para acessar seu curso.");
            return;
        }

        try {
            AlunoDAO dao = new AlunoDAO();
            CursoAluno curso = dao.buscarCursoComMateriasEProvasCorrigidas(raAluno);

            if (curso == null) {
                lblNomeCurso.setText("Nenhum curso vinculado");
                lblResumoCurso.setText("Não foi encontrado um curso associado ao seu cadastro.");
                return;
            }

            nomeCursoAtual = curso.getNomeCurso();
            lblNomeCurso.setText(curso.getNomeCurso());

            int totalMaterias = curso.getMaterias().size();
            int totalProvasCorrigidas = curso.getMaterias().stream()
                    .mapToInt(m -> m.getProvasCorrigidas().size())
                    .sum();

            lblResumoCurso.setText(String.format(
                    PT_BR,
                    "%d matéria(s) no curso  •  %d prova(s) corrigida(s) disponível(is)",
                    totalMaterias,
                    totalProvasCorrigidas
            ));

            if (curso.getMaterias().isEmpty()) {
                Label vazio = new Label("Nenhuma matéria cadastrada para este curso ainda.");
                vazio.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px;");
                vboxMaterias.getChildren().add(vazio);
                return;
            }

            for (MateriaAluno materia : curso.getMaterias()) {
                vboxMaterias.getChildren().add(criarCardMateria(materia));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText("Não foi possível carregar os dados do curso");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

    private VBox criarCardMateria(MateriaAluno materia) {
        VBox cardMateria = new VBox(12);
        cardMateria.setPadding(new Insets(18));
        cardMateria.setMaxWidth(Double.MAX_VALUE);
        cardMateria.setStyle(
                "-fx-background-color: rgba(255,255,255,0.97);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-border-color: #dce1e7;"
        );

        HBox cabecalhoMateria = new HBox(10);
        cabecalhoMateria.setAlignment(Pos.CENTER_LEFT);

        Label lblDisciplina = new Label(materia.getNomeDisciplina());
        lblDisciplina.setFont(Font.font("System", FontWeight.BOLD, 17));
        lblDisciplina.setStyle("-fx-text-fill: #0066ff;");

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        int qtdProvas = materia.getProvasCorrigidas().size();
        Label badgeQtd = new Label(qtdProvas == 1
                ? "1 prova corrigida"
                : qtdProvas + " provas corrigidas");
        badgeQtd.setStyle(
                "-fx-background-color: " + (qtdProvas > 0 ? "#E4EFE7" : "#F1F3F5") + ";" +
                        "-fx-text-fill: " + (qtdProvas > 0 ? "#285A36" : "#687386") + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 5px 10px;"
        );

        cabecalhoMateria.getChildren().addAll(lblDisciplina, espacador, badgeQtd);
        cardMateria.getChildren().addAll(cabecalhoMateria, new Separator());

        if (materia.getProvasCorrigidas().isEmpty()) {
            Label semProvas = new Label("Nenhuma prova corrigida nesta matéria até o momento.");
            semProvas.setStyle("-fx-text-fill: #687386; -fx-font-size: 13px;");
            cardMateria.getChildren().add(semProvas);
            return cardMateria;
        }

        for (ProvaCorrigidaAluno prova : materia.getProvasCorrigidas()) {
            cardMateria.getChildren().add(criarLinhaProvaCorrigida(prova));
        }

        return cardMateria;
    }

    private HBox criarLinhaProvaCorrigida(ProvaCorrigidaAluno prova) {
        HBox linha = new HBox(14);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setPadding(new Insets(12, 14, 12, 14));
        linha.setStyle(
                "-fx-background-color: #F8FAFC;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-border-color: #E2E8F0;"
        );

        VBox infoProva = new VBox(3);
        Label lblTitulo = new Label(prova.getNomeProva());
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTitulo.setStyle("-fx-text-fill: #1B1B1B;");

        String dataTexto = prova.getDataEnvio() != null
                ? "Entregue em " + prova.getDataEnvio().format(FORMATO_DATA)
                : "Correção disponível";
        Label lblSub = new Label(dataTexto);
        lblSub.setStyle("-fx-text-fill: #687386; -fx-font-size: 12px;");

        infoProva.getChildren().addAll(lblTitulo, lblSub);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        Label lblNota = new Label(String.format(
                PT_BR,
                "Nota: %.2f / %.2f",
                prova.getNotaTotal(),
                prova.getPontosMaximos()
        ));
        lblNota.setStyle(
                "-fx-background-color: #DDEEE2;" +
                        "-fx-text-fill: #285A36;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 6px 12px;"
        );

        Button btnVerCorrecao = new Button("Ver Correção");
        btnVerCorrecao.setStyle(
                "-fx-background-color: #0066ff;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 7px;" +
                        "-fx-padding: 7px 14px;" +
                        "-fx-cursor: hand;"
        );
        btnVerCorrecao.setOnAction(e -> abrirProvaCorrigida(prova));

        linha.getChildren().addAll(infoProva, espacador, lblNota, btnVerCorrecao);
        return linha;
    }

    private void abrirProvaCorrigida(ProvaCorrigidaAluno prova) {
        provaSelecionadaParaVisualizacao = prova;
        try {
            App.setRoot("alunoFluxo/ProvaCorrigidaAluno");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText("Não foi possível abrir a prova corrigida");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

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

    private void mostrarTextosBotoes() {
        if (botoesMenu == null) return;
        for (Button btn : botoesMenu) {
            if (btn != null) btn.setText(textosOriginais.get(btn));
        }
    }

    @FXML
    void voltarInicio(ActionEvent event) throws IOException {
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
        carregarCursoEMaterias();
    }
}