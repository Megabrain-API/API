package org.example.controllers.profControllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.example.App;
import org.example.dao.DisciplinaDAO;
import org.example.dao.QuestaoDAO;
import org.example.entity.profEntity.Questao;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class BancoQuestoesController implements Initializable {

    private static final String TODAS_DISCIPLINAS = "Todas as disciplinas";
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

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
    private final List<Questao> todasQuestoes = new ArrayList<>();
    private Button[] botoesMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        CarregaCombobox();
        configurarBarraLateral();
        carregarQuestoes();
        configurarListenersFiltro();
    }

    public void CarregaCombobox() {
        DisciplinaDAO dao = new DisciplinaDAO();
        List<String> listaNomes = dao.listarNomesDisciplinas();
        ObservableList<String> dadosObservable = FXCollections.observableArrayList(listaNomes);
        cb_filtro_disciplina.setItems(dadosObservable);
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
        todasQuestoes.clear();
        try {
            QuestaoDAO dao = new QuestaoDAO();
            todasQuestoes.addAll(dao.listarQuestoes());
            popularFiltroDisciplinas();
            aplicarFiltros();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText("Não foi possível carregar o banco de questões");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

    private void popularFiltroDisciplinas() {
        Set<String> disciplinas = new LinkedHashSet<>();
        disciplinas.add(TODAS_DISCIPLINAS);

        for (Questao q : todasQuestoes) {
            if (q.getDisciplina() != null && !q.getDisciplina().isBlank()) {
                disciplinas.add(q.getDisciplina().trim());
            }
        }

        String selecionadaAnterior = cb_filtro_disciplina.getValue();
        cb_filtro_disciplina.setItems(FXCollections.observableArrayList(disciplinas));

        if (selecionadaAnterior != null && disciplinas.contains(selecionadaAnterior)) {
            cb_filtro_disciplina.setValue(selecionadaAnterior);
        } else {
            cb_filtro_disciplina.setValue(TODAS_DISCIPLINAS);
        }
    }

    private void configurarListenersFiltro() {
        txt_filtro_enunciado.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cb_filtro_disciplina.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String termo = txt_filtro_enunciado.getText() == null
                ? ""
                : txt_filtro_enunciado.getText().trim().toLowerCase(PT_BR);

        String disciplinaSelecionada = cb_filtro_disciplina.getValue();

        List<Questao> filtradas = todasQuestoes.stream()
                .filter(q -> {
                    boolean bateEnunciado = termo.isEmpty() ||
                            (q.getEnunciado() != null && q.getEnunciado().toLowerCase(PT_BR).contains(termo));

                    boolean bateDisciplina = disciplinaSelecionada == null
                            || TODAS_DISCIPLINAS.equals(disciplinaSelecionada)
                            || (q.getDisciplina() != null && disciplinaSelecionada.equalsIgnoreCase(q.getDisciplina().trim()));

                    return bateEnunciado && bateDisciplina;
                })
                .collect(Collectors.toList());

        renderizarQuestoes(filtradas);
    }

    private void renderizarQuestoes(List<Questao> lista) {
        vbox_questoes_banco.getChildren().clear();

        lbl_total_questoes.setText(lista.size() == 1
                ? "1 questão"
                : lista.size() + " questões");

        if (lista.isEmpty()) {
            Label vazio = new Label("Nenhuma questão encontrada com os filtros atuais.");
            vazio.setStyle("-fx-text-fill: #5F6368; -fx-font-size: 14px; -fx-font-weight: bold;");
            vbox_questoes_banco.getChildren().add(vazio);
            return;
        }

        for (int i = 0; i < lista.size(); i++) {
            vbox_questoes_banco.getChildren().add(criarRetanguloQuestao(lista.get(i), i + 1));
        }
    }

    private VBox criarRetanguloQuestao(Questao q, int numeroExibicao) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-color: #DCE3EC;" +
                        "-fx-border-width: 1.5px;"
        );

        // Topo do retângulo: disciplina, tipo e pontuação
        HBox topo = new HBox(10);
        topo.setAlignment(Pos.CENTER_LEFT);

        String disciplinaTexto = (q.getDisciplina() == null || q.getDisciplina().isBlank())
                ? "Sem disciplina"
                : q.getDisciplina();
        Label lblDisciplina = new Label(disciplinaTexto.toUpperCase(PT_BR));
        lblDisciplina.setStyle(
                "-fx-background-color: #E6F2EF;" +
                        "-fx-text-fill: #00674F;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 4px 8px;"
        );

        Label lblTipo = new Label(formatarTipo(q.getTipo()));
        lblTipo.setStyle(
                "-fx-background-color: #F1F4F8;" +
                        "-fx-text-fill: #2B3340;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 4px 8px;"
        );

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        Label lblPontos = new Label(String.format(PT_BR, "%.2f pt(s)", q.getPontos()));
        lblPontos.setStyle("-fx-text-fill: #5F6368; -fx-font-weight: bold; -fx-font-size: 12px;");

        topo.getChildren().addAll(lblDisciplina, lblTipo, espacador, lblPontos);

        // Enunciado
        Label lblEnunciado = new Label(numeroExibicao + ". " + (q.getEnunciado() != null ? q.getEnunciado() : ""));
        lblEnunciado.setWrapText(true);
        lblEnunciado.setFont(Font.font("System", FontWeight.BOLD, 15));
        lblEnunciado.setStyle("-fx-text-fill: #1B1B1B;");

        card.getChildren().addAll(topo, lblEnunciado, new Separator());

        // Corpo específico de acordo com o tipo da questão
        if (Questao.DISSERTATIVA.equals(q.getTipo())) {
            card.getChildren().add(criarBlocoGabaritoDissertativa(q.getRespostaModelo()));
        } else if (Questao.CORRESPONDENCIA.equals(q.getTipo())) {
            card.getChildren().add(criarBlocoCorrespondencia(q));
        } else {
            card.getChildren().add(criarBlocoAlternativas(q));
        }

        return card;
    }

    private VBox criarBlocoAlternativas(Questao q) {
        VBox box = new VBox(6);
        List<String> alternativas = q.getAlternativas();
        List<Integer> corretas = q.getRespostasCorretas();

        for (int i = 0; i < alternativas.size(); i++) {
            boolean ehCorreta = corretas.contains(i);
            char letra = (char) ('A' + i);

            Label lblAlt = new Label(letra + ") " + alternativas.get(i) + (ehCorreta ? "  (Correta)" : ""));
            lblAlt.setWrapText(true);
            lblAlt.setPadding(new Insets(6, 10, 6, 10));
            lblAlt.setMaxWidth(Double.MAX_VALUE);

            if (ehCorreta) {
                lblAlt.setStyle(
                        "-fx-background-color: #E4EFE7;" +
                                "-fx-text-fill: #1E7E34;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 6px;" +
                                "-fx-border-color: #A3CFBB;" +
                                "-fx-border-radius: 6px;"
                );
            } else {
                lblAlt.setStyle(
                        "-fx-background-color: #F8FAFC;" +
                                "-fx-text-fill: #2B3340;" +
                                "-fx-background-radius: 6px;"
                );
            }
            box.getChildren().add(lblAlt);
        }
        return box;
    }

    private VBox criarBlocoGabaritoDissertativa(String gabarito) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10));
        box.setStyle(
                "-fx-background-color: #E4EFE7;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: #A3CFBB;" +
                        "-fx-border-radius: 8px;"
        );

        Label titulo = new Label("Gabarito / Resposta esperada:");
        titulo.setStyle("-fx-text-fill: #1E7E34; -fx-font-weight: bold; -fx-font-size: 12px;");

        Label texto = new Label((gabarito == null || gabarito.isBlank()) ? "Não informado" : gabarito);
        texto.setWrapText(true);
        texto.setStyle("-fx-text-fill: #1E7E34; -fx-font-size: 13px;");

        box.getChildren().addAll(titulo, texto);
        return box;
    }

    private VBox criarBlocoCorrespondencia(Questao q) {
        VBox box = new VBox(8);

        List<String> itens = q.getItensCorrespondencia();
        for (int i = 0; i < itens.size(); i++) {
            Label lblItem = new Label((i + 1) + ". " + itens.get(i));
            lblItem.setWrapText(true);
            lblItem.setStyle("-fx-text-fill: #2B3340; -fx-font-weight: bold; -fx-font-size: 13px;");
            box.getChildren().add(lblItem);
        }

        List<String> alts = q.getAlternativasCorrespondencia();
        List<Integer> gabarito = q.getGabaritoCorrespondencia();
        for (int i = 0; i < alts.size(); i++) {
            String numCorreto = i < gabarito.size() ? String.valueOf(gabarito.get(i)) : "?";
            Label lblAlt = new Label("[" + numCorreto + "] " + alts.get(i));
            lblAlt.setWrapText(true);
            lblAlt.setPadding(new Insets(5, 8, 5, 8));
            lblAlt.setStyle(
                    "-fx-background-color: #E4EFE7;" +
                            "-fx-text-fill: #1E7E34;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6px;"
            );
            box.getChildren().add(lblAlt);
        }

        return box;
    }

    private String formatarTipo(String tipo) {
        if (tipo == null) return "Questão";
        switch (tipo) {
            case Questao.MULTIPLA_ESCOLHA:
                return "Múltipla Escolha";
            case Questao.CHECKLIST:
                return "Checklist";
            case Questao.DISSERTATIVA:
                return "Dissertativa";
            case Questao.CORRESPONDENCIA:
                return "Correspondência";
            default:
                return tipo;
        }
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