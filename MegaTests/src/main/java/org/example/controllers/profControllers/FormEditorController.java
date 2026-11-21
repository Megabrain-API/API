package org.example.controllers.profControllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.App;
import org.example.dao.ProvaDAO;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.Questao;
import org.example.entity.profEntity.SessaoApp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Editor dinâmico que permite criar provas com tipos de questão mistos. */
public class FormEditorController {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_HORA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final List<Questao> filaBancoQuestoes = new ArrayList<>();

    @FXML
    private VBox container_dinamico;

    @FXML
    private Button btn_adicionar;

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

        // Cabeçalho do card da questão
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

        // Linha de configuração: Tipo de questão + Pontuação
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

        // Enunciado
        VBox colEnunciado = new VBox(4);
        Label lblEnunciado = new Label("Enunciado da questão:");
        lblEnunciado.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B1B1B; -fx-font-size: 12px;");

        TextField txtPergunta = new TextField();
        txtPergunta.setPromptText("Digite o enunciado da questão...");
        txtPergunta.setMaxWidth(Double.MAX_VALUE);
        colEnunciado.getChildren().addAll(lblEnunciado, txtPergunta);

        // Alternativas (Múltipla escolha / Checklist)
        Label tituloAlternativas = new Label("Alternativas (marque a(s) correta(s)):");
        tituloAlternativas.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B1B1B; -fx-font-size: 12px;");
        VBox areaAlternativas = new VBox(8);

        Button btnAdicionarAlternativa = new Button("+ Adicionar alternativa");
        btnAdicionarAlternativa.setStyle(estilizarBotaoSecundario());

        // Dissertativa
        Label tituloModelo = new Label("Resposta de referência / gabarito (opcional):");
        tituloModelo.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B1B1B; -fx-font-size: 12px;");
        TextArea txtRespostaModelo = new TextArea();
        txtRespostaModelo.setPromptText("Digite uma resposta de referência para auxiliar na correção...");
        txtRespostaModelo.setWrapText(true);
        txtRespostaModelo.setPrefRowCount(3);

        // Correspondência
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
                areaOpcoesCorrespondencia, tituloModelo, txtRespostaModelo, null, null, null
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

    public void adicionarQuestaoDoBanco(Questao q) {
        adicionarQuestao();
        EditorQuestao editor = editores.get(editores.size() - 1);

        editor.enunciado.setText(q.getEnunciado() != null ? q.getEnunciado() : "");
        editor.pontos.setText(String.valueOf(q.getPontos()));

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

        editor.idOrigemBanco = q.getId();

        CheckBox chkNaoDuplicar = new CheckBox("Reaproveitar no banco sem duplicar (se alterar conteúdo, desmarque para criar nova)");
        chkNaoDuplicar.setSelected(true);
        chkNaoDuplicar.setStyle("-fx-text-fill: #285A36; -fx-font-size: 12px;");
        editor.chkReutilizarSemDuplicar = chkNaoDuplicar;
        editor.painel.getChildren().add(1, chkNaoDuplicar);

        editor.snapshotOriginal = clonarQuestao(q);
        atualizarResumoCabecalho();
    }

    private Questao clonarQuestao(Questao original) {
        if (original == null) {
            return null;
        }

        Questao copia = new Questao();
        copia.setId(original.getId());
        copia.setTipo(original.getTipo());
        copia.setEnunciado(original.getEnunciado());
        copia.setPontos(original.getPontos());
        copia.setRespostaModelo(original.getRespostaModelo());
        copia.setIdOrigemBanco(original.getIdOrigemBanco());
        copia.setReutilizadaDoBanco(original.isReutilizadaDoBanco());

        if (original.getAlternativas() != null) {
            copia.getAlternativas().addAll(original.getAlternativas());
        }
        if (original.getRespostasCorretas() != null) {
            copia.getRespostasCorretas().addAll(original.getRespostasCorretas());
        }
        if (original.getItensCorrespondencia() != null) {
            copia.getItensCorrespondencia().addAll(original.getItensCorrespondencia());
        }
        if (original.getAlternativasCorrespondencia() != null) {
            copia.getAlternativasCorrespondencia().addAll(original.getAlternativasCorrespondencia());
        }
        if (original.getGabaritoCorrespondencia() != null) {
            copia.getGabaritoCorrespondencia().addAll(original.getGabaritoCorrespondencia());
        }

        return copia;
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

        String tipoOrig = original.getTipo() == null ? "" : original.getTipo().trim();
        String tipoAtual = atual.getTipo() == null ? "" : atual.getTipo().trim();
        if (!tipoOrig.equals(tipoAtual)) {
            return true;
        }

        String enunOrig = original.getEnunciado() == null ? "" : original.getEnunciado().trim();
        String enunAtual = atual.getEnunciado() == null ? "" : atual.getEnunciado().trim();
        if (!enunOrig.equals(enunAtual)) {
            return true;
        }

        if (Questao.DISSERTATIVA.equals(tipoAtual)) {
            String modOrig = original.getRespostaModelo() == null ? "" : original.getRespostaModelo().trim();
            String modAtual = atual.getRespostaModelo() == null ? "" : atual.getRespostaModelo().trim();
            return !modOrig.equals(modAtual);
        }

        if (Questao.CORRESPONDENCIA.equals(tipoAtual)) {
            return !original.getItensCorrespondencia().equals(atual.getItensCorrespondencia())
                    || !original.getAlternativasCorrespondencia().equals(atual.getAlternativasCorrespondencia())
                    || !original.getGabaritoCorrespondencia().equals(atual.getGabaritoCorrespondencia());
        }

        return !original.getAlternativas().equals(atual.getAlternativas())
                || !original.getRespostasCorretas().equals(atual.getRespostasCorretas());
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

            // Controle de reuso do Banco de Questões (sem duplicar ao mudar só pontos)
            if (editor.idOrigemBanco != null) {
                boolean marcouReutilizar = editor.chkReutilizarSemDuplicar == null
                        || editor.chkReutilizarSemDuplicar.isSelected();
                boolean conteudoIgualAoOriginal = !conteudoFoiAlterado(editor.snapshotOriginal, questao);

                questao.setIdOrigemBanco(editor.idOrigemBanco);
                questao.setReutilizadaDoBanco(marcouReutilizar && conteudoIgualAoOriginal);
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
    void entraa_banco_questoes(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/BancoQuestoes");
    }

    @FXML
    private void cancelaFormulario(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/ListaProva");
    }
}