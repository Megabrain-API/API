package org.example.controllers.profControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.App;
import org.example.dao.TurmaProfessorDAO;
import org.example.entity.profEntity.AlunoProva;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.Questao;
import org.example.entity.profEntity.RespostaAlunoProva;
import org.example.entity.profEntity.TurmaItem;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class RespostaAlunoProfessorController implements Initializable {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    @FXML
    private Button btnFinalizar;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnVoltar;

    @FXML
    private Label lblNomeAluno;

    @FXML
    private Label lblNomeProva;

    @FXML
    private Label lblNotaTotal;

    @FXML
    private Label lblQuestoesCorrigidas;

    @FXML
    private Label lblRaAluno;

    @FXML
    private VBox vboxQuestoes;

    private final TurmaProfessorDAO dao = new TurmaProfessorDAO();
    private List<RespostaAlunoProva> respostasAtuais = new ArrayList<>();
    private final Map<RespostaAlunoProva, TextField> camposNotaChecklist = new LinkedHashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        carregarDadosCorrecao();
    }

    private void carregarDadosCorrecao() {
        Prova prova = TurmasProfessorController.getProvaSelecionada();
        AlunoProva aluno = TurmasProfessorController.getAlunoSelecionado();
        TurmaItem curso = TurmasProfessorController.getCursoSelecionado();
        Long matriculaProfessor = SessaoUsuarioController.getIdUsuarioLogado();

        if (prova == null || aluno == null || matriculaProfessor == null) {
            mostrarErro("Não foi possível identificar a prova ou o aluno selecionado.");
            return;
        }

        lblNomeAluno.setText(aluno.getNome());
        lblRaAluno.setText(String.valueOf(aluno.getRa()));

        String tituloProva = prova.getNome();
        if (prova.getDisciplina() != null) {
            tituloProva += " (" + prova.getDisciplina() + ")";
        }
        if (curso != null) {
            tituloProva += " • " + curso.getNome();
        }
        lblNomeProva.setText(tituloProva);

        try {
            respostasAtuais = dao.buscarRespostasDoAluno(
                    prova.getId(),
                    aluno.getRa(),
                    matriculaProfessor
            );

            vboxQuestoes.getChildren().clear();
            camposNotaChecklist.clear();

            if (respostasAtuais.isEmpty()) {
                Label vazio = new Label("Nenhuma resposta foi encontrada para este aluno.");
                vazio.setStyle("-fx-text-fill: #1B1B1B; -fx-font-size: 14px;");
                vboxQuestoes.getChildren().add(vazio);
                atualizarResumoRodape();
                return;
            }

            for (RespostaAlunoProva resposta : respostasAtuais) {
                vboxQuestoes.getChildren().add(criarCardQuestao(resposta));
            }

            atualizarResumoRodape();
        } catch (RuntimeException erro) {
            erro.printStackTrace();
            mostrarErro(erro.getMessage());
        }
    }

    private VBox criarCardQuestao(RespostaAlunoProva resposta) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(card, Priority.NEVER);
        aplicarEstiloBordaCard(card, resposta);

        Label enunciado = new Label(
                resposta.getOrdem() + ". " + valorOuVazio(resposta.getEnunciado()));
        enunciado.setFont(Font.font("System", FontWeight.BOLD, 15));
        enunciado.setWrapText(true);
        enunciado.setMaxWidth(Double.MAX_VALUE);
        enunciado.setStyle("-fx-text-fill: #1B1B1B;");

        Label subtituloTipo = new Label(String.format(
                PT_BR,
                "%s  •  Vale %.2f pt(s)",
                textoTipo(resposta.getTipo()),
                resposta.getPontosQuestao()
        ));
        subtituloTipo.setStyle("-fx-text-fill: #687386; -fx-font-size: 12px;");

        card.getChildren().addAll(enunciado, subtituloTipo, new Separator());

        Label badgeResultado = new Label();
        badgeResultado.setFont(Font.font("System", FontWeight.BOLD, 12));
        atualizarBadgeResultado(badgeResultado, resposta);

        if (Questao.DISSERTATIVA.equals(resposta.getTipo())) {
            card.getChildren().add(criarCampoTextoLeitura(
                    "Resposta do aluno", resposta.getRespostaAluno()));
            card.getChildren().add(criarCampoTextoLeitura(
                    "Gabarito / Resposta de referência", resposta.getGabarito()));

            Label lblObs = new Label("Observação do professor (opcional)");
            lblObs.setFont(Font.font("System", FontWeight.BOLD, 12));
            lblObs.setStyle("-fx-text-fill: #1B1B1B;");

            TextArea txtObservacao = new TextArea();
            txtObservacao.setPromptText("Escreva uma observação sobre a resposta...");
            txtObservacao.setWrapText(true);
            txtObservacao.setPrefRowCount(2);
            txtObservacao.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().addAll(lblObs, txtObservacao);

            Label lblNota = new Label("Nota atribuída");
            lblNota.setFont(Font.font("System", FontWeight.BOLD, 12));
            lblNota.setStyle("-fx-text-fill: #1B1B1B;");

            HBox linhaNota = new HBox(10);
            linhaNota.setAlignment(Pos.CENTER_LEFT);

            TextField txtNota = new TextField();
            txtNota.setPrefWidth(95);
            txtNota.setPromptText("Ex.: 1,50");
            if (resposta.isCorrigida()) {
                txtNota.setText(String.format(PT_BR, "%.2f", resposta.getNota()));
            }

            Label lblPontosMax = new Label(String.format(PT_BR, "/ %.2f Pontos", resposta.getPontosQuestao()));
            lblPontosMax.setStyle("-fx-text-fill: #444444; -fx-font-size: 12px;");

            Label lblErroNota = new Label();
            lblErroNota.setStyle("-fx-text-fill: #B3261E; -fx-font-size: 11px; -fx-font-weight: bold;");

            txtNota.textProperty().addListener((obs, antigo, novo) -> {
                String limpo = novo == null ? "" : novo.trim().replace(",", ".");
                if (limpo.isEmpty()) {
                    resposta.setCorrigida(false);
                    resposta.setNota(0.0);
                    resposta.setCorreta(false);
                    lblErroNota.setText("");
                    aplicarEstiloBordaCard(card, resposta);
                    atualizarBadgeResultado(badgeResultado, resposta);
                    atualizarResumoRodape();
                    return;
                }

                try {
                    double valor = Double.parseDouble(limpo);
                    if (valor < 0 || valor > resposta.getPontosQuestao()) {
                        resposta.setCorrigida(false);
                        lblErroNota.setText(String.format(
                                PT_BR, "Informe entre 0,00 e %.2f", resposta.getPontosQuestao()));
                    } else {
                        lblErroNota.setText("");
                        resposta.setNota(Math.round(valor * 100.0) / 100.0);
                        resposta.setCorrigida(true);
                        resposta.setCorreta(resposta.getNota() > 0.0);
                    }
                } catch (NumberFormatException e) {
                    resposta.setCorrigida(false);
                    lblErroNota.setText("Valor numérico inválido");
                }

                aplicarEstiloBordaCard(card, resposta);
                atualizarBadgeResultado(badgeResultado, resposta);
                atualizarResumoRodape();
            });

            Button btnZero = new Button("0,00");
            btnZero.setStyle(
                    "-fx-background-color: #F9E2E0; -fx-text-fill: #8C1D18; " +
                            "-fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;"
            );
            btnZero.setOnAction(e -> txtNota.setText("0,00"));

            Button btnMax = new Button(String.format(PT_BR, "Máx (%.2f)", resposta.getPontosQuestao()));
            btnMax.setStyle(
                    "-fx-background-color: #E4EFE7; -fx-text-fill: #285A36; " +
                            "-fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;"
            );
            btnMax.setOnAction(e -> txtNota.setText(String.format(PT_BR, "%.2f", resposta.getPontosQuestao())));

            linhaNota.getChildren().addAll(txtNota, lblPontosMax, btnZero, btnMax, lblErroNota);
            card.getChildren().addAll(lblNota, linhaNota);
        } else if (Questao.CORRESPONDENCIA.equals(resposta.getTipo())) {
            card.getChildren().add(criarPainelItensCorrespondencia(resposta.getItensCorrespondencia()));
            card.getChildren().add(criarCampoTextoLeitura(
                    "Associações feitas pelo aluno", resposta.getRespostaAluno()));
            card.getChildren().add(criarCampoTextoLeitura(
                    "Gabarito (Associações corretas)", resposta.getGabarito()));
        } else {
            for (RespostaAlunoProva.AlternativaCorrecao alternativa : resposta.getAlternativas()) {
                card.getChildren().add(criarLinhaAlternativa(alternativa));
            }

            if (Questao.CHECKLIST.equals(resposta.getTipo())) {
                resposta.setNotaAutomatica(resposta.getNota());

                HBox linhaNotaChecklist = new HBox(10);
                linhaNotaChecklist.setAlignment(Pos.CENTER_LEFT);
                linhaNotaChecklist.setPadding(new Insets(6, 0, 2, 0));

                Label lblNotaManual = new Label(String.format(
                        PT_BR, "Nota manual (opcional, máx. %.2f):", resposta.getPontosQuestao()));
                lblNotaManual.setFont(Font.font("System", FontWeight.BOLD, 12));
                lblNotaManual.setStyle("-fx-text-fill: #1B1B1B;");

                TextField txtNotaManual = new TextField();
                txtNotaManual.setPrefWidth(110);
                txtNotaManual.setPromptText(String.format(PT_BR, "Auto: %.2f", resposta.getNotaAutomatica()));

                Label lblErroChecklist = new Label();
                lblErroChecklist.setStyle("-fx-text-fill: #B3261E; -fx-font-size: 11px; -fx-font-weight: bold;");

                camposNotaChecklist.put(resposta, txtNotaManual);

                txtNotaManual.textProperty().addListener((obs, antigo, novo) -> {
                    String limpo = novo == null ? "" : novo.trim().replace(",", ".");
                    if (limpo.isEmpty()) {
                        lblErroChecklist.setText("");
                        resposta.setNota(resposta.getNotaAutomatica());
                        resposta.setCorreta(resposta.getNotaAutomatica() > 0.0);
                        aplicarEstiloBordaCard(card, resposta);
                        atualizarBadgeResultado(badgeResultado, resposta);
                        atualizarResumoRodape();
                        return;
                    }

                    try {
                        double valor = Double.parseDouble(limpo);
                        if (valor < 0 || valor > resposta.getPontosQuestao()) {
                            lblErroChecklist.setText(String.format(
                                    PT_BR, "Informe entre 0,00 e %.2f", resposta.getPontosQuestao()));
                            return;
                        }
                        lblErroChecklist.setText("");
                        resposta.setNota(Math.round(valor * 100.0) / 100.0);
                        resposta.setCorreta(resposta.getNota() > 0.0);
                        aplicarEstiloBordaCard(card, resposta);
                        atualizarBadgeResultado(badgeResultado, resposta);
                        atualizarResumoRodape();
                    } catch (NumberFormatException e) {
                        lblErroChecklist.setText("Valor numérico inválido");
                    }
                });

                linhaNotaChecklist.getChildren().addAll(lblNotaManual, txtNotaManual, lblErroChecklist);
                card.getChildren().add(linhaNotaChecklist);
            }
        }

        card.getChildren().add(badgeResultado);
        return card;
    }

    private VBox criarPainelItensCorrespondencia(List<String> itens) {
        VBox box = new VBox(4);

        Label lbl = new Label("Itens");
        lbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        lbl.setStyle("-fx-text-fill: #1B1B1B;");

        VBox listaItens = new VBox(4);
        listaItens.setPadding(new Insets(8));

        if (itens == null || itens.isEmpty()) {
            Label vazio = new Label("(nenhum item cadastrado)");
            vazio.setStyle("-fx-text-fill: #666666;");
            listaItens.getChildren().add(vazio);
        } else {
            for (int i = 0; i < itens.size(); i++) {
                Label lblItem = new Label((i + 1) + ". " + valorOuVazio(itens.get(i)));
                lblItem.setWrapText(true);
                lblItem.setStyle("-fx-text-fill: #1B1B1B; -fx-font-size: 12px;");
                listaItens.getChildren().add(lblItem);
            }
        }

        ScrollPane scrollItens = new ScrollPane(listaItens);
        scrollItens.setFitToWidth(true);
        scrollItens.setPrefViewportHeight(85);
        scrollItens.setMaxHeight(110);
        scrollItens.setStyle("-fx-background: #F4F6F8; -fx-background-color: #F4F6F8; -fx-border-color: #dce1e7; -fx-border-radius: 6px;");

        box.getChildren().addAll(lbl, scrollItens);
        return box;
    }

    private VBox criarCampoTextoLeitura(String titulo, String conteudo) {
        VBox box = new VBox(4);

        Label lbl = new Label(titulo);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        lbl.setStyle("-fx-text-fill: #1B1B1B;");

        TextArea area = new TextArea(valorOuVazio(conteudo));
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(2);
        area.setMaxWidth(Double.MAX_VALUE);
        area.setStyle("-fx-control-inner-background: #F4F6F8; -fx-text-fill: #1B1B1B;");

        box.getChildren().addAll(lbl, area);
        return box;
    }

    private Label criarLinhaAlternativa(RespostaAlunoProva.AlternativaCorrecao alternativa) {
        String prefixo = "• ";
        String fundo = "transparent";
        String corTexto = "#1B1B1B";

        if (alternativa.isSelecionada() && alternativa.isCorreta()) {
            prefixo = "✓ Selecionada e correta: ";
            fundo = "#DDEEE2";
            corTexto = "#285A36";
        } else if (alternativa.isSelecionada()) {
            prefixo = "✗ Selecionada e incorreta: ";
            fundo = "#F7DDDA";
            corTexto = "#8C1D18";
        } else if (alternativa.isCorreta()) {
            prefixo = "✓ Alternativa correta: ";
            fundo = "transparent";
            corTexto = "#285A36";
        }

        Label label = new Label(prefixo + valorOuVazio(alternativa.getTexto()));
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle(
                "-fx-text-fill: " + corTexto + ";" +
                        "-fx-background-color: " + fundo + ";" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 7px 10px;"
        );
        return label;
    }

    private void aplicarEstiloBordaCard(VBox card, RespostaAlunoProva resposta) {
        String corBorda;
        if (!resposta.isCorrigida()) {
            corBorda = "#D9822B"; // Laranja: aguardando nota manual
        } else if (resposta.isParcial()) {
            corBorda = "#C68B00"; // Âmbar: pontuação parcial
        } else if (resposta.getNota() > 0.0) {
            corBorda = "#4A7C59"; // Verde: nota máxima
        } else {
            corBorda = "#B3261E"; // Vermelho: zerada
        }

        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.97);" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-color: " + corBorda + ";"
        );
    }

    private void atualizarBadgeResultado(Label badge, RespostaAlunoProva resposta) {
        String detalhe = (resposta.getDetalheCorrecao() != null && !resposta.getDetalheCorrecao().isEmpty())
                ? " (" + resposta.getDetalheCorrecao() + ")"
                : "";

        if (!resposta.isCorrigida()) {
            badge.setText(String.format(
                    PT_BR, "⏳ AGUARDANDO NOTA MANUAL (0,00 / %.2f pts)", resposta.getPontosQuestao()));
            badge.setStyle(
                    "-fx-text-fill: white; -fx-background-color: #D9822B; " +
                            "-fx-background-radius: 7px; -fx-padding: 6px 10px;"
            );
        } else if (resposta.isParcial()) {
            badge.setText(String.format(
                    PT_BR, "◐ PONTUAÇÃO PARCIAL%s  •  +%.2f / %.2f pts",
                    detalhe, resposta.getNota(), resposta.getPontosQuestao()));
            badge.setStyle(
                    "-fx-text-fill: white; -fx-background-color: #C68B00; " +
                            "-fx-background-radius: 7px; -fx-padding: 6px 10px;"
            );
        } else if (resposta.getNota() > 0.0) {
            badge.setText(String.format(
                    PT_BR, "✓ CORRETA%s  •  +%.2f / %.2f pts",
                    detalhe, resposta.getNota(), resposta.getPontosQuestao()));
            badge.setStyle(
                    "-fx-text-fill: white; -fx-background-color: #4A7C59; " +
                            "-fx-background-radius: 7px; -fx-padding: 6px 10px;"
            );
        } else {
            badge.setText(String.format(
                    PT_BR, "✗ INCORRETA%s  •  0,00 / %.2f pts",
                    detalhe, resposta.getPontosQuestao()));
            badge.setStyle(
                    "-fx-text-fill: white; -fx-background-color: #B3261E; " +
                            "-fx-background-radius: 7px; -fx-padding: 6px 10px;"
            );
        }
    }

    private void atualizarResumoRodape() {
        int totalQuestoes = respostasAtuais.size();
        int corrigidas = 0;
        double pontosObtidos = 0.0;
        double pontosMaximos = 0.0;

        for (RespostaAlunoProva r : respostasAtuais) {
            pontosMaximos += r.getPontosQuestao();
            if (r.isCorrigida()) {
                corrigidas++;
                pontosObtidos += r.getNota();
            }
        }

        if (lblQuestoesCorrigidas != null) {
            lblQuestoesCorrigidas.setText("Questões corrigidas: " + corrigidas + "/" + totalQuestoes);
        }

        if (lblNotaTotal != null) {
            lblNotaTotal.setText(String.format(
                    PT_BR, "Nota total: %.2f / %.2f", pontosObtidos, pontosMaximos));
        }
    }

    private boolean validarNotasChecklist() {
        for (Map.Entry<RespostaAlunoProva, TextField> entry : camposNotaChecklist.entrySet()) {
            String limpo = entry.getValue().getText() == null ? "" : entry.getValue().getText().trim().replace(",", ".");
            if (!limpo.isEmpty()) {
                try {
                    double valor = Double.parseDouble(limpo);
                    if (valor < 0 || valor > entry.getKey().getPontosQuestao()) {
                        mostrarErro(String.format(PT_BR,
                                "A nota da questão %d deve estar entre 0,00 e %.2f.",
                                entry.getKey().getOrdem(), entry.getKey().getPontosQuestao()));
                        return false;
                    }
                } catch (NumberFormatException e) {
                    mostrarErro("Valor numérico inválido na nota manual da questão " + entry.getKey().getOrdem() + ".");
                    return false;
                }
            }
        }
        return true;
    }

    @FXML
    private void salvarRascunhoCorrecao() {
        Prova prova = TurmasProfessorController.getProvaSelecionada();
        AlunoProva aluno = TurmasProfessorController.getAlunoSelecionado();

        if (prova == null || aluno == null || respostasAtuais.isEmpty()) {
            mostrarErro("Não há respostas carregadas para salvar.");
            return;
        }

        if (!validarNotasChecklist()) {
            return;
        }

        try {
            double notaParcial = dao.salvarCorrecaoProva(
                    prova.getId(),
                    aluno.getRa(),
                    respostasAtuais
            );

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Progresso salvo");
            info.setHeaderText("Notas salvas com sucesso");
            info.setContentText(String.format(
                    PT_BR, "Pontuação atual registrada: %.2f pts", notaParcial));
            info.showAndWait();
        } catch (RuntimeException erro) {
            erro.printStackTrace();
            mostrarErro(erro.getMessage());
        }
    }

    @FXML
    private void finalizarCorrecao() {
        Prova prova = TurmasProfessorController.getProvaSelecionada();
        AlunoProva aluno = TurmasProfessorController.getAlunoSelecionado();

        if (prova == null || aluno == null || respostasAtuais.isEmpty()) {
            mostrarErro("Não há respostas carregadas para finalizar a correção.");
            return;
        }

        if (!validarNotasChecklist()) {
            return;
        }

        List<Integer> questoesPendentes = new ArrayList<>();
        for (RespostaAlunoProva r : respostasAtuais) {
            if (!r.isCorrigida()) {
                questoesPendentes.add(r.getOrdem());
            }
        }

        if (!questoesPendentes.isEmpty()) {
            Alert aviso = new Alert(Alert.AlertType.WARNING);
            aviso.setTitle("Correção incompleta");
            aviso.setHeaderText("Atribua uma nota válida para todas as questões dissertativas");
            aviso.setContentText("Questões pendentes ou com valor inválido: " + questoesPendentes);
            aviso.showAndWait();
            return;
        }

        try {
            double notaFinal = dao.salvarCorrecaoProva(
                    prova.getId(),
                    aluno.getRa(),
                    respostasAtuais
            );

            double pontosMaximos = respostasAtuais.stream()
                    .mapToDouble(RespostaAlunoProva::getPontosQuestao)
                    .sum();

            Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
            sucesso.setTitle("Correção finalizada");
            sucesso.setHeaderText("Prova corrigida e retornada ao aluno!");
            sucesso.setContentText(String.format(
                    PT_BR,
                    "Aluno: %s\nNota final: %.2f / %.2f pts",
                    aluno.getNome(),
                    notaFinal,
                    pontosMaximos
            ));
            sucesso.showAndWait();

            voltarParaTurmas();
        } catch (RuntimeException | IOException erro) {
            erro.printStackTrace();
            mostrarErro(erro.getMessage());
        }
    }

    private String textoTipo(String tipo) {
        if (Questao.DISSERTATIVA.equals(tipo)) {
            return "Questão dissertativa";
        }
        if (Questao.CHECKLIST.equals(tipo)) {
            return "Checklist numerado";
        }
        if (Questao.CORRESPONDENCIA.equals(tipo)) {
            return "Correspondência";
        }
        return "Múltipla escolha";
    }

    private String valorOuVazio(String valor) {
        return valor == null || valor.trim().isEmpty() ? "(sem resposta)" : valor;
    }

    private void mostrarErro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro na correção");
        alerta.setHeaderText("Não foi possível carregar ou salvar a correção");
        alerta.setContentText(mensagem == null ? "Erro desconhecido." : mensagem);
        alerta.showAndWait();
    }

    @FXML
    private void voltarParaTurmas() throws IOException {
        TurmasProfessorController.voltarParaAlunosAoAbrir();
        App.setRoot("professorFluxo/turmas_professor");
    }
}