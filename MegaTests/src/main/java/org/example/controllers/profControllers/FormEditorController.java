package org.example.controllers.profControllers;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.example.App;
import org.example.dao.ProvaDAO;
import org.example.dao.QuestaoDAO;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.Questao;
import org.example.entity.profEntity.SessaoApp;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Editor dinâmico que permite criar provas com tipos de questão mistos. */
public class FormEditorController {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_HORA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final double PONTUACAO_PADRAO_PUXADA = 1.0;
    private static final List<Questao> filaBancoQuestoes = new ArrayList<>();
    private static final List<Questao> rascunhoEditorAtual = new ArrayList<>();

    @FXML
    private VBox container_dinamico;

    @FXML
    private Button btn_adicionar;

    @FXML
    private Button btn_banco_questoes;

    @FXML
    private Button btn_cancelar_form;

    @FXML
    private Button btn_finalizar;

    @FXML
    private Label lbl_titulo_editor;

    @FXML
    private Label lbl_disciplina_editor;

    @FXML
    private Label lbl_info_editor;

    @FXML
    private Label lbl_resumo_questoes_editor;

    @FXML
    private Label lbl_resumo_pontos_editor;

    private final List<EditorQuestao> editores = new ArrayList<>();

    private static class EditorQuestao {
        private final VBox painel;
        private final Label tituloQuestao;
        private final TextField enunciado;
        private final TextField pontos;
        private final ComboBox<String> tipo;
        private final VBox areaAlternativas;
        private final Label tituloAlternativas;
        private final Button btnAdicionarAlternativa;
        private final Label tituloModelo;
        private final TextArea respostaModelo;
        private final List<EditorAlternativa> alternativas = new ArrayList<>();
        private final VBox areaCorrespondencia;
        private final VBox areaItensCorrespondencia;
        private final VBox areaOpcoesCorrespondencia;
        private final List<TextField> itensCorrespondencia = new ArrayList<>();
        private final List<EditorCorrespondenciaOpcao> opcoesCorrespondencia = new ArrayList<>();

        private Long idOrigemBanco;
        private boolean vindaDoBanco;
        private CheckBox chkReutilizarSemDuplicar;
        private Questao snapshotOriginal;

        private EditorQuestao(
                VBox painel,
                Label tituloQuestao,
                TextField enunciado,
                TextField pontos,
                ComboBox<String> tipo,
                VBox areaAlternativas,
                Label tituloAlternativas,
                Button btnAdicionarAlternativa,
                VBox areaCorrespondencia,
                VBox areaItensCorrespondencia,
                VBox areaOpcoesCorrespondencia,
                Label tituloModelo,
                TextArea respostaModelo,
                Long idOrigemBanco,
                boolean vindaDoBanco,
                CheckBox chkReutilizarSemDuplicar,
                Questao snapshotOriginal) {
            this.painel = painel;
            this.tituloQuestao = tituloQuestao;
            this.enunciado = enunciado;
            this.pontos = pontos;
            this.tipo = tipo;
            this.areaAlternativas = areaAlternativas;
            this.tituloAlternativas = tituloAlternativas;
            this.btnAdicionarAlternativa = btnAdicionarAlternativa;
            this.tituloModelo = tituloModelo;
            this.respostaModelo = respostaModelo;
            this.areaCorrespondencia = areaCorrespondencia;
            this.areaItensCorrespondencia = areaItensCorrespondencia;
            this.areaOpcoesCorrespondencia = areaOpcoesCorrespondencia;
            this.idOrigemBanco = idOrigemBanco;
            this.vindaDoBanco = vindaDoBanco;
            this.chkReutilizarSemDuplicar = chkReutilizarSemDuplicar;
            this.snapshotOriginal = snapshotOriginal;
        }
    }

    private static class EditorCorrespondenciaOpcao {
        private final TextField texto;
        private final ComboBox<Integer> numeroCorreto;

        private EditorCorrespondenciaOpcao(TextField texto, ComboBox<Integer> numeroCorreto) {
            this.texto = texto;
            this.numeroCorreto = numeroCorreto;
        }
    }

    private static class EditorAlternativa {
        private final HBox painel;
        private final Label numero;
        private final TextField texto;
        private final CheckBox correta;

        private EditorAlternativa(HBox painel, Label numero, TextField texto, CheckBox correta) {
            this.painel = painel;
            this.numero = numero;
            this.texto = texto;
            this.correta = correta;
        }
    }

    @FXML
    public void initialize() {
        preencherCabecalho();

        if (!rascunhoEditorAtual.isEmpty()) {
            List<Questao> rascunhoParaRestaurar = new ArrayList<>(rascunhoEditorAtual);
            rascunhoEditorAtual.clear();
            for (Questao q : rascunhoParaRestaurar) {
                restaurarEditorDeRascunho(q);
            }
        }

        if (!filaBancoQuestoes.isEmpty()) {
            List<Questao> paraCarregar = new ArrayList<>(filaBancoQuestoes);
            filaBancoQuestoes.clear();
            for (Questao q : paraCarregar) {
                adicionarQuestaoDoBanco(q);
            }
        }

        atualizarResumoCabecalho();
    }

    private void preencherCabecalho() {
        Prova prova = SessaoApp.provaAtual;
        if (prova == null) {
            return;
        }

        if (lbl_titulo_editor != null && prova.getNome() != null && !prova.getNome().isBlank()) {
            lbl_titulo_editor.setText(prova.getNome());
        }

        if (lbl_disciplina_editor != null) {
            String disc = prova.getDisciplina();
            lbl_disciplina_editor.setText((disc != null && !disc.isBlank()) ? "•  " + disc : "");
        }

        if (lbl_info_editor != null) {
            StringBuilder info = new StringBuilder();
            if (prova.getDuracaoMinutos() != null && prova.getDuracaoMinutos() > 0) {
                info.append("Duração: ").append(prova.getDuracaoMinutos()).append(" min");
            }
            if (prova.getDataInicial() != null) {
                if (info.length() > 0) info.append("  •  ");
                info.append("Início: ").append(prova.getDataInicial().format(DATA_HORA_FMT));
            }
            if (prova.getDataLimite() != null) {
                if (info.length() > 0) info.append("  •  ");
                info.append("Limite: ").append(prova.getDataLimite().format(DATA_HORA_FMT));
            }
            if (info.length() > 0) {
                lbl_info_editor.setText(info.toString());
            }
        }
    }

    private void atualizarResumoCabecalho() {
        int qtd = editores.size();
        double somaPontos = 0.0;

        for (EditorQuestao editor : editores) {
            String txtP = editor.pontos.getText() == null ? "" : editor.pontos.getText().trim().replace(",", ".");
            if (txtP.isEmpty()) {
                somaPontos += 1.0;
            } else {
                try {
                    double val = Double.parseDouble(txtP);
                    if (val > 0) {
                        somaPontos += val;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (lbl_resumo_questoes_editor != null) {
            lbl_resumo_questoes_editor.setText(qtd + (qtd == 1 ? " questão" : " questões"));
        }
        if (lbl_resumo_pontos_editor != null) {
            lbl_resumo_pontos_editor.setText(String.format(PT_BR, "Total: %.2f pts", somaPontos));
        }
    }

    @FXML
    private void adicionarQuestao() {
        VBox painel = new VBox(12);
        painel.setPadding(new Insets(16));
        painel.setMaxWidth(Double.MAX_VALUE);
        painel.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.96);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: #C4D6CA;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1.5px;"
        );

        HBox topoCard = new HBox(10);
        topoCard.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("Questão " + (editores.size() + 1));
        titulo.setFont(Font.font("System", FontWeight.BOLD, 16));
        titulo.setStyle("-fx-text-fill: #285A36;");

        Region espacadorTopo = new Region();
        HBox.setHgrow(espacadorTopo, Priority.ALWAYS);

        Button btnRemover = new Button("Remover questão");
        btnRemover.setStyle(
                "-fx-background-color: #FCE8E6;" +
                        "-fx-border-color: #F2B8B5;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-text-fill: #B3261E;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 4 10 4 10;" +
                        "-fx-cursor: hand;"
        );

        topoCard.getChildren().addAll(titulo, espacadorTopo, btnRemover);

        HBox linhaConfig = new HBox(14);
        linhaConfig.setAlignment(Pos.CENTER_LEFT);

        VBox colTipo = new VBox(4);
        HBox.setHgrow(colTipo, Priority.ALWAYS);
        Label lblTipo = new Label("Tipo de questão:");
        lblTipo.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B1B1B; -fx-font-size: 12px;");

        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll(
                "Múltipla escolha",
                "Checklist numerado",
                "Dissertativa",
                "Correspondência"
        );
        cmbTipo.setValue("Múltipla escolha");
        cmbTipo.setMaxWidth(Double.MAX_VALUE);
        colTipo.getChildren().addAll(lblTipo, cmbTipo);

        VBox colPontos = new VBox(4);
        colPontos.setPrefWidth(190);
        Label lblPontos = new Label("Pontuação (pts):");
        lblPontos.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B1B1B; -fx-font-size: 12px;");

        TextField txtPontos = new TextField();
        txtPontos.setPromptText("Ex: 1.0 (Padrão)");
        txtPontos.setMaxWidth(Double.MAX_VALUE);
        txtPontos.textProperty().addListener((obs, oldVal, newVal) -> atualizarResumoCabecalho());
        colPontos.getChildren().addAll(lblPontos, txtPontos);

        linhaConfig.getChildren().addAll(colTipo, colPontos);

        VBox colEnunciado = new VBox(4);
        Label lblEnunciado = new Label("Enunciado da questão:");
        lblEnunciado.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B1B1B; -fx-font-size: 12px;");

        TextField txtPergunta = new TextField();
        txtPergunta.setPromptText("Digite o enunciado da questão...");
        txtPergunta.setMaxWidth(Double.MAX_VALUE);
        colEnunciado.getChildren().addAll(lblEnunciado, txtPergunta);

        Label tituloAlternativas = new Label("Alternativas (marque a(s) correta(s)):");
        tituloAlternativas.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B1B1B; -fx-font-size: 12px;");
        VBox areaAlternativas = new VBox(8);

        Button btnAdicionarAlternativa = new Button("+ Adicionar alternativa");
        btnAdicionarAlternativa.setStyle(estilizarBotaoSecundario());

        Label tituloModelo = new Label("Resposta de referência / gabarito (opcional):");
        tituloModelo.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B1B1B; -fx-font-size: 12px;");
        TextArea txtRespostaModelo = new TextArea();
        txtRespostaModelo.setPromptText("Digite uma resposta de referência para auxiliar na correção...");
        txtRespostaModelo.setWrapText(true);
        txtRespostaModelo.setPrefRowCount(3);

        VBox areaCorrespondencia = new VBox(10);
        areaCorrespondencia.setStyle("-fx-background-color: #F6F8F7; -fx-background-radius: 8px; -fx-padding: 12;");

        Label tituloItens = new Label("Coluna 1 — Itens numerados:");
        tituloItens.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B1B1B; -fx-font-size: 12px;");
        VBox areaItensCorrespondencia = new VBox(6);
        Button btnAdicionarItem = new Button("+ Adicionar item");
        btnAdicionarItem.setStyle(estilizarBotaoSecundario());

        Label tituloOpcoes = new Label("Coluna 2 — Alternativas e número correspondente:");
        tituloOpcoes.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B1B1B; -fx-font-size: 12px;");
        VBox areaOpcoesCorrespondencia = new VBox(6);
        Button btnAdicionarOpcao = new Button("+ Adicionar opção");
        btnAdicionarOpcao.setStyle(estilizarBotaoSecundario());

        EditorQuestao editor = new EditorQuestao(
                painel, titulo, txtPergunta, txtPontos, cmbTipo, areaAlternativas,
                tituloAlternativas, btnAdicionarAlternativa, areaCorrespondencia, areaItensCorrespondencia,
                areaOpcoesCorrespondencia, tituloModelo, txtRespostaModelo, null, false, null, null
        );

        btnAdicionarItem.setOnAction(event -> adicionarItemCorrespondencia(editor));
        btnAdicionarOpcao.setOnAction(event -> adicionarOpcaoCorrespondencia(editor));
        btnAdicionarAlternativa.setOnAction(event -> adicionarAlternativa(editor));
        cmbTipo.setOnAction(event -> atualizarTipo(editor));

        btnRemover.setOnAction(event -> {
            container_dinamico.getChildren().remove(painel);
            editores.remove(editor);
            atualizarTitulosQuestoes();
            atualizarResumoCabecalho();
        });

        areaCorrespondencia.getChildren().addAll(
                tituloItens,
                areaItensCorrespondencia,
                btnAdicionarItem,
                tituloOpcoes,
                areaOpcoesCorrespondencia,
                btnAdicionarOpcao
        );

        painel.getChildren().addAll(
                topoCard,
                linhaConfig,
                colEnunciado,
                tituloAlternativas,
                areaAlternativas,
                btnAdicionarAlternativa,
                areaCorrespondencia,
                tituloModelo,
                txtRespostaModelo
        );

        editores.add(editor);
        container_dinamico.getChildren().add(painel);
        atualizarTipo(editor);
        atualizarResumoCabecalho();
    }

    private void configurarOpcaoReutilizacaoBanco(EditorQuestao editor, Long idOrigem, Boolean estadoMarcado) {
        editor.idOrigemBanco = idOrigem;
        editor.vindaDoBanco = (idOrigem != null);

        if (!editor.vindaDoBanco) {
            return;
        }

        Label avisoBanco = new Label("Puxada do Banco de Questões • Valor inicial 1.0 (ajuste o campo Pontuação para esta prova)");
        avisoBanco.setStyle(
                "-fx-background-color: #DDEEE2;" +
                        "-fx-text-fill: #285A36;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 4 10 4 10;" +
                        "-fx-background-radius: 6px;"
        );

        if (editor.chkReutilizarSemDuplicar == null) {
            CheckBox chk = new CheckBox("Manter vinculada ao banco (desmarque se alterar conteúdo e quiser enviar como nova)");
            chk.setSelected(estadoMarcado == null || estadoMarcado);
            chk.setDisable(false);
            chk.setVisible(true);
            chk.setManaged(true);
            chk.setStyle(
                    "-fx-text-fill: #285A36;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 11px;"
            );
            editor.chkReutilizarSemDuplicar = chk;

            VBox barraBanco = new VBox(6, avisoBanco, chk);
            barraBanco.setStyle(
                    "-fx-background-color: #F4F9F5;" +
                            "-fx-border-color: #C4D6CA;" +
                            "-fx-border-radius: 8px;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-padding: 8 10 8 10;"
            );
            editor.painel.getChildren().add(1, barraBanco);
        } else {
            editor.chkReutilizarSemDuplicar.setSelected(estadoMarcado == null || estadoMarcado);
            editor.chkReutilizarSemDuplicar.setDisable(false);
            editor.chkReutilizarSemDuplicar.setVisible(true);
            editor.chkReutilizarSemDuplicar.setManaged(true);
        }
    }

    private String estilizarBotaoSecundario() {
        return "-fx-background-color: #EEF5F0;" +
                "-fx-border-color: #C4D6CA;" +
                "-fx-border-radius: 6px;" +
                "-fx-background-radius: 6px;" +
                "-fx-text-fill: #285A36;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 5 12 5 12;" +
                "-fx-cursor: hand;";
    }

    private String estilizarBotaoRemoverPequeno() {
        return "-fx-background-color: #F4F4F4;" +
                "-fx-border-color: #D0D0D0;" +
                "-fx-border-radius: 6px;" +
                "-fx-background-radius: 6px;" +
                "-fx-text-fill: #555555;" +
                "-fx-font-size: 11px;" +
                "-fx-padding: 4 9 4 9;" +
                "-fx-cursor: hand;";
    }

    private void atualizarTipo(EditorQuestao editor) {
        String tipo = editor.tipo.getValue();

        boolean dissertativa = "Dissertativa".equals(tipo);
        boolean correspondencia = "Correspondência".equals(tipo);
        boolean objetivaOuChecklist = !dissertativa && !correspondencia;

        editor.areaAlternativas.setVisible(objetivaOuChecklist);
        editor.areaAlternativas.setManaged(objetivaOuChecklist);
        editor.tituloAlternativas.setVisible(objetivaOuChecklist);
        editor.tituloAlternativas.setManaged(objetivaOuChecklist);
        editor.btnAdicionarAlternativa.setVisible(objetivaOuChecklist);
        editor.btnAdicionarAlternativa.setManaged(objetivaOuChecklist);

        editor.respostaModelo.setVisible(dissertativa);
        editor.respostaModelo.setManaged(dissertativa);
        editor.tituloModelo.setVisible(dissertativa);
        editor.tituloModelo.setManaged(dissertativa);

        editor.areaCorrespondencia.setVisible(correspondencia);
        editor.areaCorrespondencia.setManaged(correspondencia);
    }

    private void atualizarComboCorrespondencia(EditorQuestao editor) {
        int quantidadeItens = editor.itensCorrespondencia.size();

        for (EditorCorrespondenciaOpcao opcao : editor.opcoesCorrespondencia) {
            Integer selecionado = opcao.numeroCorreto.getValue();
            opcao.numeroCorreto.getItems().clear();

            for (int i = 1; i <= quantidadeItens; i++) {
                opcao.numeroCorreto.getItems().add(i);
            }

            if (selecionado != null && selecionado <= quantidadeItens) {
                opcao.numeroCorreto.setValue(selecionado);
            }
        }
    }

    private void atualizarNumerosCorrespondencia(EditorQuestao editor) {
        for (int i = 0; i < editor.areaItensCorrespondencia.getChildren().size(); i++) {
            HBox linha = (HBox) editor.areaItensCorrespondencia.getChildren().get(i);
            Label numero = (Label) linha.getChildren().get(0);
            numero.setText((i + 1) + ".");
        }
    }

    private void adicionarAlternativa(EditorQuestao editor) {
        Label numero = new Label();
        numero.setStyle("-fx-font-weight: bold; -fx-text-fill: #285A36; -fx-min-width: 22;");

        TextField txtAlternativa = new TextField();
        txtAlternativa.setPromptText("Digite a alternativa...");
        txtAlternativa.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(txtAlternativa, Priority.ALWAYS);

        CheckBox chkCorreta = new CheckBox("Correta");
        chkCorreta.setStyle("-fx-text-fill: #285A36; -fx-font-weight: bold;");

        HBox linha = new HBox(8);
        linha.setAlignment(Pos.CENTER_LEFT);

        EditorAlternativa alternativa = new EditorAlternativa(linha, numero, txtAlternativa, chkCorreta);
        Button btnRemover = new Button("Remover");
        btnRemover.setStyle(estilizarBotaoRemoverPequeno());
        btnRemover.setOnAction(event -> {
            editor.areaAlternativas.getChildren().remove(linha);
            editor.alternativas.remove(alternativa);
            atualizarNumeracao(editor);
        });

        linha.getChildren().addAll(numero, txtAlternativa, chkCorreta, btnRemover);
        editor.alternativas.add(alternativa);
        editor.areaAlternativas.getChildren().add(linha);
        atualizarNumeracao(editor);
    }

    private void adicionarOpcaoCorrespondencia(EditorQuestao editor) {
        HBox linha = new HBox(8);
        linha.setAlignment(Pos.CENTER_LEFT);

        ComboBox<Integer> numero = new ComboBox<>();
        numero.setPromptText("Item");
        numero.setPrefWidth(80);

        TextField texto = new TextField();
        texto.setPromptText("Digite a alternativa correspondente...");
        texto.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(texto, Priority.ALWAYS);

        EditorCorrespondenciaOpcao opcao = new EditorCorrespondenciaOpcao(texto, numero);

        Button remover = new Button("Remover");
        remover.setStyle(estilizarBotaoRemoverPequeno());
        remover.setOnAction(event -> {
            editor.areaOpcoesCorrespondencia.getChildren().remove(linha);
            editor.opcoesCorrespondencia.remove(opcao);
        });

        linha.getChildren().addAll(numero, texto, remover);
        editor.opcoesCorrespondencia.add(opcao);
        editor.areaOpcoesCorrespondencia.getChildren().add(linha);

        atualizarComboCorrespondencia(editor);
    }

    private void adicionarItemCorrespondencia(EditorQuestao editor) {
        HBox linha = new HBox(8);
        linha.setAlignment(Pos.CENTER_LEFT);

        Label numero = new Label((editor.itensCorrespondencia.size() + 1) + ".");
        numero.setStyle("-fx-font-weight: bold; -fx-text-fill: #285A36; -fx-min-width: 22;");

        TextField campo = new TextField();
        campo.setPromptText("Digite o item...");
        campo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(campo, Priority.ALWAYS);

        Button remover = new Button("Remover");
        remover.setStyle(estilizarBotaoRemoverPequeno());
        remover.setOnAction(event -> {
            editor.areaItensCorrespondencia.getChildren().remove(linha);
            editor.itensCorrespondencia.remove(campo);
            atualizarNumerosCorrespondencia(editor);
            atualizarComboCorrespondencia(editor);
        });

        linha.getChildren().addAll(numero, campo, remover);
        editor.itensCorrespondencia.add(campo);
        editor.areaItensCorrespondencia.getChildren().add(linha);

        atualizarComboCorrespondencia(editor);
    }

    private void atualizarNumeracao(EditorQuestao editor) {
        for (int i = 0; i < editor.alternativas.size(); i++) {
            editor.alternativas.get(i).numero.setText((i + 1) + ".");
        }
    }

    private void atualizarTitulosQuestoes() {
        for (int i = 0; i < editores.size(); i++) {
            editores.get(i).tituloQuestao.setText("Questão " + (i + 1));
        }
    }

    private void restaurarEditorDeRascunho(Questao q) {
        adicionarQuestao();
        EditorQuestao editor = editores.get(editores.size() - 1);

        editor.enunciado.setText(q.getEnunciado() != null ? q.getEnunciado() : "");
        if (q.getPontos() > 0) {
            editor.pontos.setText(String.valueOf(q.getPontos()));
        }

        String tipoCombo = mapearTipoParaCombo(q.getTipo());
        editor.tipo.setValue(tipoCombo);
        atualizarTipo(editor);

        if (Questao.DISSERTATIVA.equals(q.getTipo())) {
            editor.respostaModelo.setText(q.getRespostaModelo() != null ? q.getRespostaModelo() : "");
        } else if (Questao.CORRESPONDENCIA.equals(q.getTipo())) {
            for (String item : q.getItensCorrespondencia()) {
                adicionarItemCorrespondencia(editor);
                TextField campoItem = editor.itensCorrespondencia.get(editor.itensCorrespondencia.size() - 1);
                campoItem.setText(item);
            }
            List<String> altsCorr = q.getAlternativasCorrespondencia();
            List<Integer> gabCorr = q.getGabaritoCorrespondencia();
            for (int i = 0; i < altsCorr.size(); i++) {
                adicionarOpcaoCorrespondencia(editor);
                EditorCorrespondenciaOpcao op = editor.opcoesCorrespondencia.get(editor.opcoesCorrespondencia.size() - 1);
                op.texto.setText(altsCorr.get(i));
                if (i < gabCorr.size()) {
                    op.numeroCorreto.setValue(gabCorr.get(i));
                }
            }
        } else {
            List<String> alts = q.getAlternativas();
            List<Integer> corretas = q.getRespostasCorretas();
            for (int i = 0; i < alts.size(); i++) {
                adicionarAlternativa(editor);
                EditorAlternativa alt = editor.alternativas.get(editor.alternativas.size() - 1);
                alt.texto.setText(alts.get(i));
                alt.correta.setSelected(corretas.contains(i));
            }
        }

        if (q.getIdOrigemBanco() != null) {
            configurarOpcaoReutilizacaoBanco(editor, q.getIdOrigemBanco(), q.isReutilizadaDoBanco());
            editor.snapshotOriginal = normalizarSnapshot(q, PONTUACAO_PADRAO_PUXADA);
        }
    }

    public void adicionarQuestaoDoBanco(Questao q) {
        adicionarQuestao();
        EditorQuestao editor = editores.get(editores.size() - 1);

        editor.enunciado.setText(q.getEnunciado() != null ? q.getEnunciado() : "");
        // Sempre inicia com peso padrão 1.0, mas editável pelo professor para esta prova
        editor.pontos.setText(String.format(Locale.US, "%.1f", PONTUACAO_PADRAO_PUXADA));
        editor.pontos.setDisable(false);
        editor.pontos.setEditable(true);

        String tipoCombo = mapearTipoParaCombo(q.getTipo());
        editor.tipo.setValue(tipoCombo);
        atualizarTipo(editor);

        if (Questao.DISSERTATIVA.equals(q.getTipo())) {
            editor.respostaModelo.setText(q.getRespostaModelo() != null ? q.getRespostaModelo() : "");
        } else if (Questao.CORRESPONDENCIA.equals(q.getTipo())) {
            for (String item : q.getItensCorrespondencia()) {
                adicionarItemCorrespondencia(editor);
                TextField campoItem = editor.itensCorrespondencia.get(editor.itensCorrespondencia.size() - 1);
                campoItem.setText(item);
            }
            List<String> altsCorr = q.getAlternativasCorrespondencia();
            List<Integer> gabCorr = q.getGabaritoCorrespondencia();
            for (int i = 0; i < altsCorr.size(); i++) {
                adicionarOpcaoCorrespondencia(editor);
                EditorCorrespondenciaOpcao op = editor.opcoesCorrespondencia.get(editor.opcoesCorrespondencia.size() - 1);
                op.texto.setText(altsCorr.get(i));
                if (i < gabCorr.size()) {
                    op.numeroCorreto.setValue(gabCorr.get(i));
                }
            }
        } else {
            List<String> alts = q.getAlternativas();
            List<Integer> corretas = q.getRespostasCorretas();
            for (int i = 0; i < alts.size(); i++) {
                adicionarAlternativa(editor);
                EditorAlternativa alt = editor.alternativas.get(editor.alternativas.size() - 1);
                alt.texto.setText(alts.get(i));
                alt.correta.setSelected(corretas.contains(i));
            }
        }

        Long idBanco = (q.getIdOrigemBanco() != null && q.getIdOrigemBanco() > 0)
                ? q.getIdOrigemBanco()
                : (q.getId() > 0 ? q.getId() : null);

        configurarOpcaoReutilizacaoBanco(editor, idBanco, true);
        editor.snapshotOriginal = normalizarSnapshot(q, PONTUACAO_PADRAO_PUXADA);
        atualizarResumoCabecalho();
    }

    private Questao normalizarSnapshot(Questao original, double pontosFixos) {
        if (original == null) {
            return null;
        }

        Questao copia = new Questao();
        copia.setId(original.getId());
        copia.setTipo(original.getTipo() == null ? "" : original.getTipo().trim());
        copia.setEnunciado(original.getEnunciado() == null ? "" : original.getEnunciado().trim());
        copia.setPontos(pontosFixos);
        copia.setRespostaModelo(original.getRespostaModelo() == null ? "" : original.getRespostaModelo().trim());
        copia.setIdOrigemBanco(original.getIdOrigemBanco());
        copia.setReutilizadaDoBanco(original.isReutilizadaDoBanco());
        copia.setDisciplina(original.getDisciplina());
        copia.setProva(original.getProva());
        copia.setMatriculaAutor(original.getMatriculaAutor());
        copia.setNomeAutor(original.getNomeAutor());

        if (original.getAlternativas() != null) {
            for (String s : original.getAlternativas()) {
                copia.getAlternativas().add(s == null ? "" : s.trim());
            }
        }
        if (original.getRespostasCorretas() != null) {
            copia.getRespostasCorretas().addAll(original.getRespostasCorretas());
        }
        if (original.getItensCorrespondencia() != null) {
            for (String s : original.getItensCorrespondencia()) {
                copia.getItensCorrespondencia().add(s == null ? "" : s.trim());
            }
        }
        if (original.getAlternativasCorrespondencia() != null) {
            for (String s : original.getAlternativasCorrespondencia()) {
                copia.getAlternativasCorrespondencia().add(s == null ? "" : s.trim());
            }
        }
        if (original.getGabaritoCorrespondencia() != null) {
            copia.getGabaritoCorrespondencia().addAll(original.getGabaritoCorrespondencia());
        }

        return copia;
    }

    private Questao clonarQuestao(Questao original) {
        if (original == null) {
            return null;
        }
        return normalizarSnapshot(original, original.getPontos());
    }

    private String mapearTipoParaCombo(String tipoQuestao) {
        if (Questao.CHECKLIST.equals(tipoQuestao)) return "Checklist numerado";
        if (Questao.DISSERTATIVA.equals(tipoQuestao)) return "Dissertativa";
        if (Questao.CORRESPONDENCIA.equals(tipoQuestao)) return "Correspondência";
        return "Múltipla escolha";
    }

    public static void receberQuestoesDoBanco(List<Questao> questoes) {
        filaBancoQuestoes.clear();
        if (questoes != null) {
            filaBancoQuestoes.addAll(questoes);
        }
    }

    private boolean conteudoFoiAlterado(Questao original, Questao atual) {
        if (original == null || atual == null) {
            return false;
        }

        Questao origNorm = normalizarSnapshot(original, original.getPontos());
        Questao atualNorm = normalizarSnapshot(atual, atual.getPontos());

        String tipoOrig = origNorm.getTipo() == null ? "" : origNorm.getTipo().trim();
        String tipoAtual = atualNorm.getTipo() == null ? "" : atualNorm.getTipo().trim();
        if (!tipoOrig.equals(tipoAtual)) {
            return true;
        }

        String enunOrig = origNorm.getEnunciado() == null ? "" : origNorm.getEnunciado().trim();
        String enunAtual = atualNorm.getEnunciado() == null ? "" : atualNorm.getEnunciado().trim();
        if (!enunOrig.equals(enunAtual)) {
            return true;
        }

        if (Questao.DISSERTATIVA.equals(tipoAtual)) {
            String modOrig = origNorm.getRespostaModelo() == null ? "" : origNorm.getRespostaModelo().trim();
            String modAtual = atualNorm.getRespostaModelo() == null ? "" : atualNorm.getRespostaModelo().trim();
            return !modOrig.equals(modAtual);
        }

        if (Questao.CORRESPONDENCIA.equals(tipoAtual)) {
            return !origNorm.getItensCorrespondencia().equals(atualNorm.getItensCorrespondencia())
                    || !origNorm.getAlternativasCorrespondencia().equals(atualNorm.getAlternativasCorrespondencia())
                    || !origNorm.getGabaritoCorrespondencia().equals(atualNorm.getGabaritoCorrespondencia());
        }

        return !origNorm.getAlternativas().equals(atualNorm.getAlternativas())
                || !origNorm.getRespostasCorretas().equals(atualNorm.getRespostasCorretas());
    }

    @FXML
    private void GeraProva(ActionEvent event) {
        Prova prova = SessaoApp.provaAtual;
        if (prova == null) {
            mostrarErro("Nenhuma prova foi inicializada. Volte e crie a prova novamente.");
            return;
        }
        if (editores.isEmpty()) {
            mostrarErro("Adicione pelo menos uma questão.");
            return;
        }

        List<Questao> questoesNovas = new ArrayList<>();
        boolean temMultiplaEscolha = false;
        boolean temChecklist = false;
        boolean temDissertativa = false;
        boolean temCorrespondencia = false;

        for (int i = 0; i < editores.size(); i++) {
            EditorQuestao editor = editores.get(i);
            String enunciado = editor.enunciado.getText() == null
                    ? "" : editor.enunciado.getText().trim();
            if (enunciado.isEmpty()) {
                mostrarErro("Preencha o enunciado da questão " + (i + 1) + ".");
                return;
            }

            double pontos = 1.0;
            String txtP = editor.pontos.getText() == null ? "" : editor.pontos.getText().trim().replace(",", ".");

            if (!txtP.isEmpty()) {
                try {
                    pontos = Double.parseDouble(txtP);
                    if (pontos <= 0) {
                        mostrarErro("A pontuação da questão " + (i + 1) + " deve ser maior que zero.");
                        return;
                    }
                    if (pontos > 9.99) {
                        mostrarErro("A pontuação da questão " + (i + 1) + " não pode ser maior que 9.99.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarErro("Informe um valor numérico válido para a pontuação da questão " + (i + 1) + " (ex: 1.5 ou 2.50).");
                    return;
                }
            }

            Questao questao = new Questao();
            questao.setEnunciado(enunciado);
            questao.setPontos(pontos);
            String tipoSelecionado = editor.tipo.getValue();

            if ("Múltipla escolha".equals(tipoSelecionado)) {
                questao.setTipo(Questao.MULTIPLA_ESCOLHA);
                temMultiplaEscolha = true;
            } else if ("Checklist numerado".equals(tipoSelecionado)) {
                questao.setTipo(Questao.CHECKLIST);
                temChecklist = true;
            } else if ("Dissertativa".equals(tipoSelecionado)) {
                questao.setTipo(Questao.DISSERTATIVA);
                temDissertativa = true;
            } else if ("Correspondência".equals(tipoSelecionado)) {
                questao.setTipo(Questao.CORRESPONDENCIA);
                temCorrespondencia = true;
            }

            if (Questao.DISSERTATIVA.equals(questao.getTipo())) {
                String modelo = editor.respostaModelo.getText();
                questao.setRespostaModelo(modelo == null ? "" : modelo.trim());

            } else if (Questao.CORRESPONDENCIA.equals(questao.getTipo())) {
                if (editor.itensCorrespondencia.size() < 2) {
                    mostrarErro("A questão de correspondência " + (i + 1) + " precisa ter pelo menos dois itens.");
                    return;
                }
                if (editor.opcoesCorrespondencia.size() < 2) {
                    mostrarErro("A questão de correspondência " + (i + 1) + " precisa ter pelo menos duas opções.");
                    return;
                }

                for (TextField campoItem : editor.itensCorrespondencia) {
                    String textoItem = campoItem.getText() == null ? "" : campoItem.getText().trim();
                    if (textoItem.isEmpty()) {
                        mostrarErro("Preencha todos os textos dos itens de correspondência na questão " + (i + 1) + ".");
                        return;
                    }
                    questao.getItensCorrespondencia().add(textoItem);
                }

                for (EditorCorrespondenciaOpcao opcao : editor.opcoesCorrespondencia) {
                    String textoOpcao = opcao.texto.getText() == null ? "" : opcao.texto.getText().trim();
                    Integer numCorreto = opcao.numeroCorreto.getValue();

                    if (textoOpcao.isEmpty()) {
                        mostrarErro("Preencha o texto de todas as opções de correspondência na questão " + (i + 1) + ".");
                        return;
                    }
                    if (numCorreto == null) {
                        mostrarErro("Selecione o número correspondente correto para todas as opções na questão " + (i + 1) + ".");
                        return;
                    }

                    questao.getAlternativasCorrespondencia().add(textoOpcao);
                    questao.getGabaritoCorrespondencia().add(numCorreto);
                }

            } else {
                if (editor.alternativas.size() < 2) {
                    mostrarErro("A questão " + (i + 1) + " precisa ter pelo menos duas alternativas.");
                    return;
                }

                int quantidadeCorretas = 0;
                for (int j = 0; j < editor.alternativas.size(); j++) {
                    EditorAlternativa alternativa = editor.alternativas.get(j);
                    String texto = alternativa.texto.getText() == null
                            ? "" : alternativa.texto.getText().trim();
                    if (texto.isEmpty()) {
                        mostrarErro("Preencha todas as alternativas da questão " + (i + 1) + ".");
                        return;
                    }
                    questao.getAlternativas().add(texto);
                    if (alternativa.correta.isSelected()) {
                        questao.getRespostasCorretas().add(j);
                        quantidadeCorretas++;
                    }
                }

                if (quantidadeCorretas == 0) {
                    mostrarErro("Marque pelo menos uma alternativa correta na questão " + (i + 1) + ".");
                    return;
                }
                if (Questao.MULTIPLA_ESCOLHA.equals(questao.getTipo()) && quantidadeCorretas != 1) {
                    mostrarErro("A questão de múltipla escolha " + (i + 1) + " deve ter exatamente uma alternativa correta.");
                    return;
                }
            }

            if (editor.idOrigemBanco != null) {
                boolean marcouManterVinculo = editor.chkReutilizarSemDuplicar == null
                        || editor.chkReutilizarSemDuplicar.isSelected();
                boolean conteudoAlterado = conteudoFoiAlterado(editor.snapshotOriginal, questao);

                if (!marcouManterVinculo && conteudoAlterado) {
                    questao.setIdOrigemBanco(null);
                    questao.setReutilizadaDoBanco(false);
                } else {
                    questao.setIdOrigemBanco(editor.idOrigemBanco);
                    questao.setReutilizadaDoBanco(true);
                }
            } else {
                questao.setReutilizadaDoBanco(false);
            }

            questoesNovas.add(questao);
        }

        prova.getQuestoes().clear();
        prova.getQuestoes().addAll(questoesNovas);

        int tiposUsados = (temMultiplaEscolha ? 1 : 0)
                + (temChecklist ? 1 : 0) + (temDissertativa ? 1 : 0) + (temCorrespondencia ? 1 : 0);

        prova.setTipoQuestoes(tiposUsados > 1 ? "Mista"
                : temMultiplaEscolha ? "Múltipla escolha"
                : temChecklist ? "Checklist numerado"
                : temCorrespondencia ? "Correspondência" : "Dissertativa");

        try {
            new ProvaDAO().salvaProvaCompleta(prova);
            rascunhoEditorAtual.clear();
            filaBancoQuestoes.clear();

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Sucesso");
            alerta.setHeaderText("Prova salva com sucesso!");
            alerta.setContentText("A prova, suas questões, alternativas e gabaritos foram gravados com sucesso no banco de dados.");
            alerta.showAndWait();

            App.setRoot("professorFluxo/ListaProva");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Não foi possível salvar a prova no banco de dados. "
                    + "Confira a conexão, a disciplina selecionada e a sessão do professor.\n"
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void mostrarErro(String mensagem) {
        Alert erro = new Alert(Alert.AlertType.ERROR);
        erro.setTitle("Erro");
        erro.setHeaderText("Não foi possível concluir a operação");
        erro.setContentText(mensagem);
        erro.showAndWait();
    }

    @FXML
    void entraa_banco_questoes(ActionEvent event) {
        if (SessaoApp.provaAtual == null) {
            mostrarErro("Nenhuma prova em montagem foi encontrada.");
            return;
        }

        List<Questao> disponiveis;
        try {
            disponiveis = new QuestaoDAO().listarQuestoes();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Não foi possível carregar as questões do banco:\n" + e.getMessage());
            return;
        }

        if (disponiveis == null || disponiveis.isEmpty()) {
            mostrarErro("Não há questões cadastradas no banco de questões.");
            return;
        }

        Questao selecionada = abrirModalPuxarQuestao(disponiveis);
        if (selecionada != null) {
            adicionarQuestaoDoBanco(clonarQuestao(selecionada));
        }
    }

    private Questao abrirModalPuxarQuestao(List<Questao> questoes) {
        Dialog<Questao> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (container_dinamico != null && container_dinamico.getScene() != null) {
            Window owner = container_dinamico.getScene().getWindow();
            if (owner != null) {
                dialog.initOwner(owner);
            }
        }

        dialog.setTitle("Banco de Questões — Puxar para a Prova");
        dialog.setResizable(true);
        DialogPane painel = dialog.getDialogPane();
        painel.setPrefSize(900, 600);
        painel.getStyleClass().add("seletor-questoes");
        painel.getStylesheets().add(FormEditorController.class
                .getResource("/org/example/professorFluxo/SeletorQuestoes.css").toExternalForm());

        ButtonType confirmar = new ButtonType("Puxar para a prova", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        painel.getButtonTypes().addAll(confirmar, cancelar);
        painel.lookupButton(confirmar).getStyleClass().add("seletor-confirmar");

        Label titulo = new Label("Banco de Questões");
        titulo.getStyleClass().add("seletor-titulo");
        Label subtitulo = new Label("Escolha uma questão para a prova. O valor inicial é 1,00 ponto e pode ser alterado para esta prova.");
        subtitulo.setWrapText(true);
        subtitulo.getStyleClass().add("seletor-secundario");

        String todasDisciplinas = "Todas as disciplinas";
        String todosTipos = "Todos os tipos";
        TextField busca = new TextField();
        busca.setPromptText("Buscar pelo enunciado");
        busca.setAccessibleText("Buscar pelo enunciado");
        HBox.setHgrow(busca, Priority.ALWAYS);

        ComboBox<AutoriaQuestao.Filtro> autor = new ComboBox<>();
        autor.setAccessibleText("Filtrar por professor");
        autor.setPrefWidth(170);
        autor.getItems().setAll(AutoriaQuestao.filtros(questoes, SessaoUsuarioController.getIdUsuarioLogado()));
        autor.getSelectionModel().selectFirst();

        ComboBox<String> disciplina = new ComboBox<>();
        disciplina.setAccessibleText("Filtrar por disciplina");
        disciplina.getItems().add(todasDisciplinas);
        questoes.stream().map(Questao::getDisciplina)
                .filter(d -> d != null && !d.isBlank()).map(String::trim)
                .distinct().sorted(String.CASE_INSENSITIVE_ORDER).forEach(disciplina.getItems()::add);
        disciplina.setValue(todasDisciplinas);

        ComboBox<String> tipo = new ComboBox<>();
        tipo.setAccessibleText("Filtrar por tipo");
        tipo.getItems().add(todosTipos);
        questoes.stream().map(q -> mapearTipoParaCombo(q.getTipo()))
                .distinct().sorted().forEach(tipo.getItems()::add);
        tipo.setValue(todosTipos);

        Button limpar = new Button("Limpar filtros");
        limpar.setOnAction(e -> {
            busca.clear();
            autor.getSelectionModel().selectFirst();
            disciplina.setValue(todasDisciplinas);
            tipo.setValue(todosTipos);
        });
        HBox filtros = new HBox(10, busca, autor, disciplina, tipo, limpar);
        filtros.setAlignment(Pos.CENTER_LEFT);

        FilteredList<Questao> filtradas = new FilteredList<>(FXCollections.observableArrayList(questoes));
        ListView<Questao> listaQuestoes = new ListView<>(filtradas);
        listaQuestoes.getStyleClass().add("seletor-resultados");
        listaQuestoes.setAccessibleText("Questões encontradas");
        listaQuestoes.setPlaceholder(new Label("Nenhuma questão corresponde aos filtros."));
        listaQuestoes.setCellFactory(lv -> new ListCell<Questao>() {
            @Override
            protected void updateItem(Questao q, boolean vazio) {
                super.updateItem(q, vazio);
                setText(null);
                if (vazio || q == null) {
                    setGraphic(null);
                    return;
                }

                String nome = q.getDisciplina();
                Label tagDisciplina = new Label(nome == null || nome.isBlank() ? "Sem disciplina" : nome.trim());
                tagDisciplina.getStyleClass().add("seletor-tag-disciplina");
                Label tagTipo = new Label(mapearTipoParaCombo(q.getTipo()));
                tagTipo.getStyleClass().add("seletor-tag-tipo");
                javafx.scene.layout.FlowPane etiquetas = new javafx.scene.layout.FlowPane(6, 4, tagDisciplina, tagTipo);

                Label enunciado = new Label(q.getEnunciado());
                enunciado.setWrapText(true);
                enunciado.maxWidthProperty().bind(widthProperty().subtract(44));
                enunciado.getStyleClass().add("seletor-enunciado");
                Label autoria = new Label(AutoriaQuestao.formatar(q));
                autoria.setWrapText(true);
                autoria.maxWidthProperty().bind(enunciado.maxWidthProperty());
                autoria.getStyleClass().add("seletor-secundario");
                VBox cartao = new VBox(7, etiquetas, enunciado, autoria);
                cartao.getStyleClass().add("seletor-cartao");
                setGraphic(cartao);
            }
        });

        Label contador = new Label();
        contador.getStyleClass().add("seletor-secundario");
        Runnable atualizarContador = () -> contador.setText(filtradas.size() == 1
                ? "1 questão encontrada" : filtradas.size() + " questões encontradas");
        atualizarContador.run();

        Runnable filtrar = () -> {
            Questao selecionada = listaQuestoes.getSelectionModel().getSelectedItem();
            String termo = busca.getText().trim().toLowerCase(PT_BR);
            AutoriaQuestao.Filtro autorSelecionado = autor.getValue();
            String disciplinaSelecionada = disciplina.getValue();
            String tipoSelecionado = tipo.getValue();
            filtradas.setPredicate(q ->
                    (termo.isEmpty() || (q.getEnunciado() != null
                            && q.getEnunciado().toLowerCase(PT_BR).contains(termo)))
                            && (todasDisciplinas.equals(disciplinaSelecionada) || (q.getDisciplina() != null
                            && q.getDisciplina().trim().equalsIgnoreCase(disciplinaSelecionada)))
                            && (todosTipos.equals(tipoSelecionado) || mapearTipoParaCombo(q.getTipo()).equals(tipoSelecionado))
                            && (autorSelecionado == null || autorSelecionado.aceita(q)));
            if (selecionada != null && filtradas.contains(selecionada)) {
                listaQuestoes.getSelectionModel().select(selecionada);
            } else {
                listaQuestoes.getSelectionModel().clearSelection();
            }
            atualizarContador.run();
        };
        busca.textProperty().addListener((obs, anterior, atual) -> filtrar.run());
        autor.valueProperty().addListener((obs, anterior, atual) -> filtrar.run());
        disciplina.valueProperty().addListener((obs, anterior, atual) -> filtrar.run());
        tipo.valueProperty().addListener((obs, anterior, atual) -> filtrar.run());

        Label tituloPrevia = new Label("Prévia da questão");
        tituloPrevia.getStyleClass().add("seletor-enunciado");
        VBox conteudoPrevia = new VBox(8);
        ScrollPane scrollPrevia = new ScrollPane(conteudoPrevia);
        scrollPrevia.setFitToWidth(true);
        scrollPrevia.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPrevia, Priority.ALWAYS);
        VBox painelPrevia = new VBox(12, tituloPrevia, scrollPrevia);
        painelPrevia.getStyleClass().add("seletor-previa");

        listaQuestoes.getSelectionModel().selectedItemProperty().addListener((obs, anterior, atual) -> {
            atualizarPreviaModal(conteudoPrevia, atual);
            scrollPrevia.setVvalue(0);
        });
        atualizarPreviaModal(conteudoPrevia, null);
        painel.lookupButton(confirmar).disableProperty()
                .bind(listaQuestoes.getSelectionModel().selectedItemProperty().isNull());

        VBox resultados = new VBox(8, contador, listaQuestoes);
        VBox.setVgrow(listaQuestoes, Priority.ALWAYS);
        HBox.setHgrow(resultados, Priority.ALWAYS);
        HBox.setHgrow(painelPrevia, Priority.ALWAYS);
        resultados.setPrefWidth(390);
        painelPrevia.setPrefWidth(390);
        HBox paineis = new HBox(14, resultados, painelPrevia);
        VBox topo = new VBox(6, titulo, subtitulo, filtros);
        topo.getStyleClass().add("seletor-topo");
        VBox corpo = new VBox(14, topo, paineis);
        VBox.setVgrow(paineis, Priority.ALWAYS);
        painel.setContent(corpo);

        dialog.setResultConverter(botao ->
                botao == confirmar ? listaQuestoes.getSelectionModel().getSelectedItem() : null);

        return dialog.showAndWait().orElse(null);
    }

    private void atualizarPreviaModal(VBox alvo, Questao q) {
        alvo.getChildren().clear();

        if (q == null) {
            Label vazio = new Label("Nenhuma questão selecionada.");
            vazio.setStyle("-fx-text-fill: #5F6368;");
            alvo.getChildren().add(vazio);
            return;
        }

        String tipoLabel = mapearTipoParaCombo(q.getTipo());
        String discLabel = (q.getDisciplina() != null && !q.getDisciplina().isBlank())
                ? q.getDisciplina()
                : "Sem disciplina";

        Label meta = new Label("Tipo: " + tipoLabel + "   •   Disciplina: " + discLabel + "   •   Valor inicial: 1.00 pts");
        meta.setStyle("-fx-text-fill: #4A7C59; -fx-font-weight: bold; -fx-font-size: 12px;");

        Label enun = new Label("Enunciado: " + (q.getEnunciado() != null ? q.getEnunciado() : ""));
        enun.setWrapText(true);
        enun.setStyle("-fx-text-fill: #1B1B1B; -fx-font-weight: bold; -fx-font-size: 13px;");

        meta.setWrapText(true);
        Label autoria = new Label(AutoriaQuestao.formatar(q));
        autoria.setWrapText(true);
        autoria.getStyleClass().add("seletor-secundario");
        alvo.getChildren().addAll(meta, autoria, enun);

        if (Questao.DISSERTATIVA.equals(q.getTipo())) {
            String modelo = (q.getRespostaModelo() != null && !q.getRespostaModelo().isBlank())
                    ? q.getRespostaModelo()
                    : "(Sem resposta de referência cadastrada)";
            Label lblMod = new Label("Resposta de referência / gabarito: " + modelo);
            lblMod.setWrapText(true);
            lblMod.setStyle("-fx-text-fill: #285A36; -fx-font-size: 12px;");
            alvo.getChildren().add(lblMod);

        } else if (Questao.CORRESPONDENCIA.equals(q.getTipo())) {
            if (!q.getItensCorrespondencia().isEmpty()) {
                Label lblItensTitulo = new Label("Itens (Coluna 1):");
                lblItensTitulo.setStyle("-fx-text-fill: #285A36; -fx-font-weight: bold; -fx-font-size: 12px;");
                alvo.getChildren().add(lblItensTitulo);
                for (int i = 0; i < q.getItensCorrespondencia().size(); i++) {
                    Label itemLbl = new Label("  " + (i + 1) + ". " + q.getItensCorrespondencia().get(i));
                    itemLbl.setWrapText(true);
                    itemLbl.setStyle("-fx-text-fill: #1B1B1B; -fx-font-size: 12px;");
                    alvo.getChildren().add(itemLbl);
                }
            }
            if (!q.getAlternativasCorrespondencia().isEmpty()) {
                Label lblOpTitulo = new Label("Opções e gabarito (Coluna 2):");
                lblOpTitulo.setStyle("-fx-text-fill: #285A36; -fx-font-weight: bold; -fx-font-size: 12px;");
                alvo.getChildren().add(lblOpTitulo);
                List<Integer> gab = q.getGabaritoCorrespondencia();
                for (int i = 0; i < q.getAlternativasCorrespondencia().size(); i++) {
                    String numGab = (gab != null && i < gab.size() && gab.get(i) != null)
                            ? "  →  Correto: Item " + gab.get(i)
                            : "";
                    Label opLbl = new Label("  • " + q.getAlternativasCorrespondencia().get(i) + numGab);
                    opLbl.setWrapText(true);
                    opLbl.setStyle("-fx-text-fill: #1B1B1B; -fx-font-size: 12px;");
                    alvo.getChildren().add(opLbl);
                }
            }

        } else {
            Label lblAltTitulo = new Label("Alternativas:");
            lblAltTitulo.setStyle("-fx-text-fill: #285A36; -fx-font-weight: bold; -fx-font-size: 12px;");
            alvo.getChildren().add(lblAltTitulo);

            List<String> alts = q.getAlternativas();
            List<Integer> corretas = q.getRespostasCorretas();
            for (int i = 0; i < alts.size(); i++) {
                boolean ehCorreta = corretas != null && corretas.contains(i);
                String marcador = ehCorreta ? "  ✔ " + (i + 1) + ". " : "  • " + (i + 1) + ". ";
                String sufixo = ehCorreta ? "  [CORRETA]" : "";

                Label altLbl = new Label(marcador + alts.get(i) + sufixo);
                altLbl.setWrapText(true);
                altLbl.setStyle(ehCorreta
                        ? "-fx-text-fill: #285A36; -fx-font-weight: bold; -fx-font-size: 12px;"
                        : "-fx-text-fill: #1B1B1B; -fx-font-size: 12px;");
                alvo.getChildren().add(altLbl);
            }
        }
    }

    @FXML
    private void cancelaFormulario(ActionEvent event) throws IOException {
        rascunhoEditorAtual.clear();
        filaBancoQuestoes.clear();
        App.setRoot("professorFluxo/ListaProva");
    }
}