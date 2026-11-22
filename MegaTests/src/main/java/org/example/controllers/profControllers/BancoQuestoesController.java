package org.example.controllers.profControllers;

import javafx.collections.FXCollections;
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
import org.example.App;
import org.example.dao.DisciplinaDAO;
import org.example.dao.QuestaoDAO;
import org.example.entity.profEntity.Questao;
import org.example.entity.profEntity.SessaoApp;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class BancoQuestoesController implements Initializable {

    private static final String TODAS_DISCIPLINAS = "Todas as disciplinas";
    private static final String TODOS_TIPOS = "Todos os tipos";
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    // Controla se a tela foi aberta para gerenciar/criar questões ou para importar para uma prova
    private static boolean modoSelecaoProva = false;
    private static String disciplinaPreSelecionada = null;

    public static void configurarModoGerenciamento() {
        modoSelecaoProva = false;
        disciplinaPreSelecionada = null;
    }

    public static void configurarModoSelecaoParaProva(String disciplinaDaProva) {
        modoSelecaoProva = true;
        disciplinaPreSelecionada = disciplinaDaProva;
    }

    @FXML
    private Label lbl_subtitulo_topo;

    @FXML
    private Label lbl_total_questoes;

    @FXML
    private TextField txt_filtro_enunciado;

    @FXML
    private ComboBox<String> cb_filtro_disciplina;

    @FXML
    private ComboBox<String> cb_filtro_tipo;

    @FXML
    private VBox vbox_questoes_banco;

    @FXML
    private HBox rodape_selecao;

    @FXML
    private Label lbl_selecionadas_banco;

    private final Map<Long, Questao> selecionadasParaLevar = new LinkedHashMap<>();
    private final List<Questao> todasQuestoes = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarModoVisual();
        popularComboTipos();
        carregarQuestoes();
        configurarListenersFiltro();
    }

    private void configurarModoVisual() {
        if (rodape_selecao != null) {
            rodape_selecao.setVisible(modoSelecaoProva);
            rodape_selecao.setManaged(modoSelecaoProva);
        }
        if (lbl_subtitulo_topo != null) {
            lbl_subtitulo_topo.setText(modoSelecaoProva
                    ? "SELECIONAR QUESTÕES PARA A PROVA"
                    : "REPOSITÓRIO DO PROFESSOR");
        }
        atualizarContadorSelecionadas();
    }

    private void popularComboTipos() {
        if (cb_filtro_tipo != null) {
            cb_filtro_tipo.setItems(FXCollections.observableArrayList(
                    TODOS_TIPOS,
                    "Múltipla Escolha",
                    "Checklist",
                    "Dissertativa",
                    "Correspondência"
            ));
            cb_filtro_tipo.setValue(TODOS_TIPOS);
        }
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

        for (String d : new DisciplinaDAO().listarNomesDisciplinas()) {
            if (d != null && !d.isBlank()) {
                disciplinas.add(d.trim());
            }
        }
        for (Questao q : todasQuestoes) {
            if (q.getDisciplina() != null && !q.getDisciplina().isBlank()) {
                disciplinas.add(q.getDisciplina().trim());
            }
        }

        String selecionadaAnterior = cb_filtro_disciplina.getValue();
        cb_filtro_disciplina.setItems(FXCollections.observableArrayList(disciplinas));

        if (disciplinaPreSelecionada != null && disciplinas.contains(disciplinaPreSelecionada)) {
            cb_filtro_disciplina.setValue(disciplinaPreSelecionada);
            disciplinaPreSelecionada = null; // Aplica apenas na abertura inicial
        } else if (selecionadaAnterior != null && disciplinas.contains(selecionadaAnterior)) {
            cb_filtro_disciplina.setValue(selecionadaAnterior);
        } else {
            cb_filtro_disciplina.setValue(TODAS_DISCIPLINAS);
        }
    }

    private void configurarListenersFiltro() {
        txt_filtro_enunciado.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cb_filtro_disciplina.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        if (cb_filtro_tipo != null) {
            cb_filtro_tipo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        }
    }

    private void aplicarFiltros() {
        String termo = txt_filtro_enunciado.getText() == null
                ? ""
                : txt_filtro_enunciado.getText().trim().toLowerCase(PT_BR);

        String disciplinaSelecionada = cb_filtro_disciplina.getValue();
        String tipoSelecionado = cb_filtro_tipo != null ? cb_filtro_tipo.getValue() : TODOS_TIPOS;

        List<Questao> filtradas = todasQuestoes.stream()
                .filter(q -> {
                    boolean bateEnunciado = termo.isEmpty() ||
                            (q.getEnunciado() != null && q.getEnunciado().toLowerCase(PT_BR).contains(termo));

                    boolean bateDisciplina = disciplinaSelecionada == null
                            || TODAS_DISCIPLINAS.equals(disciplinaSelecionada)
                            || (q.getDisciplina() != null && disciplinaSelecionada.equalsIgnoreCase(q.getDisciplina().trim()));

                    boolean bateTipo = tipoSelecionado == null
                            || TODOS_TIPOS.equals(tipoSelecionado)
                            || formatarTipo(q.getTipo()).equalsIgnoreCase(tipoSelecionado);

                    return bateEnunciado && bateDisciplina && bateTipo;
                })
                .collect(Collectors.toList());

        renderizarQuestoes(filtradas);
    }

    private void atualizarContadorSelecionadas() {
        if (lbl_selecionadas_banco == null) return;
        int qtd = selecionadasParaLevar.size();
        lbl_selecionadas_banco.setText(qtd == 1 ? "1 questão selecionada" : qtd + " questões selecionadas");
    }

    private void renderizarQuestoes(List<Questao> lista) {
        vbox_questoes_banco.getChildren().clear();

        lbl_total_questoes.setText(lista.size() == 1
                ? "1 questão encontrada"
                : lista.size() + " questões encontradas");

        if (lista.isEmpty()) {
            VBox cardVazio = new VBox(8);
            cardVazio.setAlignment(Pos.CENTER);
            cardVazio.setPadding(new Insets(36, 24, 36, 24));
            cardVazio.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.96);" +
                            "-fx-background-radius: 12px;" +
                            "-fx-border-color: #D4DFD8;" +
                            "-fx-border-radius: 12px;" +
                            "-fx-border-width: 1px;"
            );
            Label lblTitulo = new Label("Nenhuma questão encontrada");
            lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));
            lblTitulo.setStyle("-fx-text-fill: #1B1B1B;");

            Label lblSub = new Label("Em Minhas Provas, clique em \"+ Criar Questão\" para cadastrar uma questão neste repositório.");
            lblSub.setWrapText(true);
            lblSub.setStyle("-fx-text-fill: #66706A; -fx-font-size: 13px;");

            cardVazio.getChildren().addAll(lblTitulo, lblSub);
            vbox_questoes_banco.getChildren().add(cardVazio);
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
                "-fx-background-color: rgba(255, 255, 255, 0.96);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-color: #D4DFD8;" +
                        "-fx-border-width: 1.2px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2);"
        );

        // Topo do card: disciplina e tipo
        HBox topo = new HBox(10);
        topo.setAlignment(Pos.CENTER_LEFT);

        String disciplinaTexto = (q.getDisciplina() == null || q.getDisciplina().isBlank())
                ? "Sem disciplina"
                : q.getDisciplina();
        Label lblDisciplina = new Label(disciplinaTexto);
        lblDisciplina.setStyle(
                "-fx-background-color: #DDEEE2;" +
                        "-fx-text-fill: #285A36;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 4px 10px;"
        );

        Label lblTipo = new Label(formatarTipo(q.getTipo()));
        lblTipo.setStyle(
                "-fx-background-color: #F1F4F2;" +
                        "-fx-text-fill: #2B362F;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 4px 10px;"
        );

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        topo.getChildren().addAll(lblDisciplina, lblTipo, espacador);

        if (modoSelecaoProva) {
            CheckBox chkReutilizar = new CheckBox("Selecionar para a prova");
            chkReutilizar.setStyle("-fx-text-fill: #285A36; -fx-font-weight: bold; -fx-cursor: hand;");
            chkReutilizar.setSelected(selecionadasParaLevar.containsKey(q.getId()));
            chkReutilizar.setOnAction(e -> {
                if (chkReutilizar.isSelected()) {
                    selecionadasParaLevar.put(q.getId(), q);
                } else {
                    selecionadasParaLevar.remove(q.getId());
                }
                atualizarContadorSelecionadas();
            });
            topo.getChildren().add(chkReutilizar);
        }

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

            Label lblAlt = new Label(letra + ") " + alternativas.get(i) + (ehCorreta ? "  ✓ (Correta)" : ""));
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
                        "-fx-background-color: #F6F8F7;" +
                                "-fx-text-fill: #2B362F;" +
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
                "-fx-background-color: #EEF5F0;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: #C4D6CA;" +
                        "-fx-border-radius: 8px;"
        );

        Label titulo = new Label("Resposta de referência / gabarito:");
        titulo.setStyle("-fx-text-fill: #285A36; -fx-font-weight: bold; -fx-font-size: 12px;");

        Label texto = new Label((gabarito == null || gabarito.isBlank()) ? "Não informado" : gabarito);
        texto.setWrapText(true);
        texto.setStyle("-fx-text-fill: #285A36; -fx-font-size: 13px;");

        box.getChildren().addAll(titulo, texto);
        return box;
    }

    private VBox criarBlocoCorrespondencia(Questao q) {
        VBox box = new VBox(8);

        List<String> itens = q.getItensCorrespondencia();
        for (int i = 0; i < itens.size(); i++) {
            Label lblItem = new Label((i + 1) + ". " + itens.get(i));
            lblItem.setWrapText(true);
            lblItem.setStyle("-fx-text-fill: #2B362F; -fx-font-weight: bold; -fx-font-size: 13px;");
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

    @FXML
    private void levarQuestoesParaEditor(ActionEvent event) throws IOException {
        if (selecionadasParaLevar.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Atenção");
            alerta.setHeaderText("Nenhuma questão selecionada");
            alerta.setContentText("Marque \"Selecionar para a prova\" em pelo menos uma questão antes de continuar.");
            alerta.showAndWait();
            return;
        }

        FormEditorController.receberQuestoesDoBanco(new ArrayList<>(selecionadasParaLevar.values()));
        App.setRoot("professorFluxo/FormEditor");
    }

    @FXML
    private void voltarOrigem(ActionEvent event) throws IOException {
        if (modoSelecaoProva) {
            App.setRoot("professorFluxo/FormEditor");
        } else {
            App.setRoot("professorFluxo/ListaProva");
        }
    }
}