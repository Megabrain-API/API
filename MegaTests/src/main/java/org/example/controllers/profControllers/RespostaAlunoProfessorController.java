package org.example.controllers.profControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Mostra ao professor a prova respondida pelo aluno com cálculo automático
 * integral (múltipla escolha), proporcional (checklist e correspondência)
 * e avaliação manual com nota livre (dissertativa).
 */
public class RespostaAlunoProfessorController implements Initializable {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    @FXML
    private Label lbl_titulo_correcao;

    @FXML
    private Label lbl_info_correcao;

    @FXML
    private Label lbl_resumo_nota;

    @FXML
    private VBox vb_respostas_professor;

    private final TurmaProfessorDAO dao = new TurmaProfessorDAO();
    private List<RespostaAlunoProva> respostasAtuais = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        carregarRespostas();
    }

    private void carregarRespostas() {
        Prova prova = TurmasProfessorController.getProvaSelecionada();
        AlunoProva aluno = TurmasProfessorController.getAlunoSelecionado();
        TurmaItem curso = TurmasProfessorController.getCursoSelecionado();
        Long matriculaProfessor = SessaoUsuarioController.getIdUsuarioLogado();

        if (prova == null || aluno == null || matriculaProfessor == null) {
            mostrarErro("Não foi possível identificar a prova ou o aluno selecionado.");
            return;
        }

        lbl_titulo_correcao.setText(prova.getNome());

        String info = "Aluno: " + aluno.getNome() + "  •  RA: " + aluno.getRa();
        if (curso != null) {
            info += "  •  Curso: " + curso.getNome();
        }
        if (prova.getDisciplina() != null) {
            info += "  •  Disciplina: " + prova.getDisciplina();
        }
        lbl_info_correcao.setText(info);

        try {
            respostasAtuais = dao.buscarRespostasDoAluno(
                    prova.getId(),
                    aluno.getRa(),
                    matriculaProfessor
            );

            vb_respostas_professor.getChildren().clear();

            if (respostasAtuais.isEmpty()) {
                Label vazio = new Label("Nenhuma resposta foi encontrada para este aluno.");
                vazio.setStyle("-fx-text-fill: #1B1B1B; -fx-font-size: 14px;");
                vb_respostas_professor.getChildren().add(vazio);
                atualizarResumoNota();
                return;
            }

            for (RespostaAlunoProva resposta : respostasAtuais) {
                vb_respostas_professor.getChildren().add(criarBlocoQuestao(resposta));
            }

            atualizarResumoNota();
        } catch (RuntimeException erro) {
            erro.printStackTrace();
            mostrarErro(erro.getMessage());
        }
    }

    private VBox criarBlocoQuestao(RespostaAlunoProva resposta) {
        VBox bloco = new VBox(9);
        bloco.setPadding(new Insets(15));
        bloco.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(bloco, Priority.NEVER);
        aplicarEstiloBordaBloco(bloco, resposta);

        Label enunciado = new Label(
                resposta.getOrdem() + ". " + valorOuVazio(resposta.getEnunciado()));
        enunciado.setFont(Font.font("System", FontWeight.BOLD, 14));
        enunciado.setWrapText(true);
        enunciado.setMaxWidth(Double.MAX_VALUE);
        enunciado.setStyle("-fx-text-fill: #1B1B1B;");
        bloco.getChildren().add(enunciado);

        Label tipo = new Label(String.format(
                PT_BR,
                "%s  •  Vale %.2f pt(s)",
                textoTipo(resposta.getTipo()),
                resposta.getPontosQuestao()
        ));
        tipo.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
        bloco.getChildren().add(tipo);

        Label resultado = new Label();
        resultado.setFont(Font.font("System", FontWeight.BOLD, 12));
        atualizarBadgeResultado(resultado, resposta);

        if (Questao.DISSERTATIVA.equals(resposta.getTipo())) {
            bloco.getChildren().add(criarTextoResposta(
                    "Resposta do aluno:", resposta.getRespostaAluno()));
            bloco.getChildren().add(criarTextoResposta(
                    "Gabarito / Resposta de referência:", resposta.getGabarito()));

            HBox barraCorrecaoManual = new HBox(8);
            barraCorrecaoManual.setAlignment(Pos.CENTER_LEFT);
            barraCorrecaoManual.setPadding(new Insets(4, 0, 2, 0));

            Label lblNota = new Label("Nota atribuída:");
            lblNota.setFont(Font.font("System", FontWeight.BOLD, 12));
            lblNota.setStyle("-fx-text-fill: #1B1B1B;");

            TextField txtNota = new TextField();
            txtNota.setPrefWidth(75);
            txtNota.setPromptText("0,00");
            if (resposta.isCorrigida()) {
                txtNota.setText(String.format(PT_BR, "%.2f", resposta.getNota()));
            }

            Label lblMax = new Label(String.format(PT_BR, "/ %.2f pts", resposta.getPontosQuestao()));
            lblMax.setStyle("-fx-text-fill: #444444; -fx-font-size: 12px;");

            Label lblAvisoNota = new Label();
            lblAvisoNota.setStyle("-fx-text-fill: #B3261E; -fx-font-size: 11px; -fx-font-weight: bold;");

            txtNota.textProperty().addListener((obs, antigo, novo) -> {
                String limpo = novo == null ? "" : novo.trim().replace(",", ".");
                if (limpo.isEmpty()) {
                    resposta.setCorrigida(false);
                    resposta.setNota(0.0);
                    resposta.setCorreta(false);
                    lblAvisoNota.setText("");
                    aplicarEstiloBordaBloco(bloco, resposta);
                    atualizarBadgeResultado(resultado, resposta);
                    atualizarResumoNota();
                    return;
                }

                try {
                    double valor = Double.parseDouble(limpo);
                    if (valor < 0 || valor > resposta.getPontosQuestao()) {
                        resposta.setCorrigida(false);
                        lblAvisoNota.setText(String.format(
                                PT_BR, "Digite entre 0,00 e %.2f", resposta.getPontosQuestao()));
                    } else {
                        lblAvisoNota.setText("");
                        resposta.setNota(Math.round(valor * 100.0) / 100.0);
                        resposta.setCorrigida(true);
                        resposta.setCorreta(resposta.getNota() > 0.0);
                    }
                } catch (NumberFormatException e) {
                    resposta.setCorrigida(false);
                    lblAvisoNota.setText("Valor inválido");
                }

                aplicarEstiloBordaBloco(bloco, resposta);
                atualizarBadgeResultado(resultado, resposta);
                atualizarResumoNota();
            });

            Button btnZero = new Button("0,00");
            btnZero.setStyle(
                    "-fx-background-color: #F9E2E0; -fx-text-fill: #8C1D18; " +
                            "-fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;"
            );
            btnZero.setOnAction(e -> txtNota.setText("0,00"));

            Button btnMaxima = new Button(String.format(PT_BR, "Máx (%.2f)", resposta.getPontosQuestao()));
            btnMaxima.setStyle(
                    "-fx-background-color: #E4EFE7; -fx-text-fill: #285A36; " +
                            "-fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;"
            );
            btnMaxima.setOnAction(e -> txtNota.setText(String.format(PT_BR, "%.2f", resposta.getPontosQuestao())));

            barraCorrecaoManual.getChildren().addAll(
                    lblNota, txtNota, lblMax, btnZero, btnMaxima, lblAvisoNota
            );
            bloco.getChildren().add(barraCorrecaoManual);
        } else if (Questao.CORRESPONDENCIA.equals(resposta.getTipo())) {
            bloco.getChildren().add(criarTextoResposta(
                    "Associações feitas pelo aluno:", resposta.getRespostaAluno()));
            bloco.getChildren().add(criarTextoResposta(
                    "Gabarito (Associações corretas):", resposta.getGabarito()));
        } else {
            for (RespostaAlunoProva.AlternativaCorrecao alternativa : resposta.getAlternativas()) {
                bloco.getChildren().add(criarAlternativa(alternativa));
            }
        }

        bloco.getChildren().add(resultado);

        return bloco;
    }

    private void aplicarEstiloBordaBloco(VBox bloco, RespostaAlunoProva resposta) {
        String corBorda;
        if (!resposta.isCorrigida()) {
            corBorda = "#D9822B"; // Laranja: aguardando correção manual
        } else if (resposta.isParcial()) {
            corBorda = "#C68B00"; // Âmbar: acerto parcial
        } else if (resposta.getNota() > 0.0) {
            corBorda = "#4A7C59"; // Verde: nota máxima
        } else {
            corBorda = "#B3261E"; // Vermelho: zerada
        }

        bloco.setStyle(
                "-fx-background-color: rgba(255,255,255,0.96);" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-color: " + corBorda + ";"
        );
    }

    private void atualizarBadgeResultado(Label resultado, RespostaAlunoProva resposta) {
        String detalhe = (resposta.getDetalheCorrecao() != null && !resposta.getDetalheCorrecao().isEmpty())
                ? " (" + resposta.getDetalheCorrecao() + ")"
                : "";

        if (!resposta.isCorrigida()) {
            resultado.setText(String.format(
                    PT_BR, "⏳ AGUARDANDO NOTA MANUAL (0,00 / %.2f pts)", resposta.getPontosQuestao()));
            resultado.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-background-color: #D9822B;" +
                            "-fx-background-radius: 7px;" +
                            "-fx-padding: 6px 10px;"
            );
        } else if (resposta.isParcial()) {
            resultado.setText(String.format(
                    PT_BR, "◐ PONTUAÇÃO PARCIAL%s  •  +%.2f / %.2f pts",
                    detalhe, resposta.getNota(), resposta.getPontosQuestao()));
            resultado.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-background-color: #C68B00;" +
                            "-fx-background-radius: 7px;" +
                            "-fx-padding: 6px 10px;"
            );
        } else if (resposta.getNota() > 0.0) {
            resultado.setText(String.format(
                    PT_BR, "✓ CORRETA%s  •  +%.2f / %.2f pts",
                    detalhe, resposta.getNota(), resposta.getPontosQuestao()));
            resultado.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-background-color: #4A7C59;" +
                            "-fx-background-radius: 7px;" +
                            "-fx-padding: 6px 10px;"
            );
        } else {
            resultado.setText(String.format(
                    PT_BR, "✗ INCORRETA%s  •  0,00 / %.2f pts",
                    detalhe, resposta.getPontosQuestao()));
            resultado.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-background-color: #B3261E;" +
                            "-fx-background-radius: 7px;" +
                            "-fx-padding: 6px 10px;"
            );
        }
    }

    private void atualizarResumoNota() {
        if (lbl_resumo_nota == null) {
            return;
        }

        if (respostasAtuais.isEmpty()) {
            lbl_resumo_nota.setText("Pontuação: 0,00 / 0,00 pts");
            return;
        }

        double pontosObtidos = 0.0;
        double pontosMaximos = 0.0;
        int pendentes = 0;

        for (RespostaAlunoProva r : respostasAtuais) {
            pontosMaximos += r.getPontosQuestao();
            if (!r.isCorrigida()) {
                pendentes++;
            } else {
                pontosObtidos += r.getNota();
            }
        }

        String resumo = String.format(
                PT_BR,
                "Pontuação calculada: %.2f / %.2f pts",
                pontosObtidos, pontosMaximos
        );

        if (pendentes > 0) {
            resumo += "  •  Pendentes: " + pendentes + " dissertativa(s)";
        }

        lbl_resumo_nota.setText(resumo);
    }

    @FXML
    private void salvarERetornarCorrecao() {
        Prova prova = TurmasProfessorController.getProvaSelecionada();
        AlunoProva aluno = TurmasProfessorController.getAlunoSelecionado();

        if (prova == null || aluno == null || respostasAtuais.isEmpty()) {
            mostrarErro("Não há respostas carregadas para salvar a correção.");
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
            aviso.setHeaderText("Informe uma nota válida para todas as questões dissertativas");
            aviso.setContentText("Questões pendentes ou com valor fora do limite: " + questoesPendentes);
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
            sucesso.setTitle("Prova corrigida");
            sucesso.setHeaderText("Correção salva e retornada ao aluno!");
            sucesso.setContentText(String.format(
                    PT_BR,
                    "Aluno: %s\nNota final calculada: %.2f / %.2f pts",
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

    private VBox criarTextoResposta(String titulo, String texto) {
        VBox caixa = new VBox(3);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblTitulo.setStyle("-fx-text-fill: #1B1B1B;");

        Label lblTexto = new Label(valorOuVazio(texto));
        lblTexto.setWrapText(true);
        lblTexto.setMaxWidth(Double.MAX_VALUE);
        lblTexto.setStyle(
                "-fx-text-fill: #1B1B1B;" +
                        "-fx-background-color: #F1F1F1;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 8px;"
        );

        caixa.getChildren().addAll(lblTitulo, lblTexto);
        return caixa;
    }

    private Label criarAlternativa(RespostaAlunoProva.AlternativaCorrecao alternativa) {
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
            fundo = "";
            corTexto = "#285A36";
        }

        Label label = new Label(prefixo + valorOuVazio(alternativa.getTexto()));
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle(
                "-fx-text-fill: " + corTexto + ";" +
                        "-fx-background-color: " + fundo + ";" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 7px 9px;"
        );
        return label;
    }

    private String textoTipo(String tipo) {
        if (Questao.DISSERTATIVA.equals(tipo)) {
            return "Questão dissertativa (Correção manual)";
        }
        if (Questao.CHECKLIST.equals(tipo)) {
            return "Questão de múltiplas respostas (Pontuação proporcional)";
        }
        if (Questao.CORRESPONDENCIA.equals(tipo)) {
            return "Correspondência (Pontuação proporcional)";
        }
        return "Questão de múltipla escolha (Tudo ou nada)";
    }

    private String valorOuVazio(String valor) {
        return valor == null || valor.trim().isEmpty() ? "(sem resposta)" : valor;
    }

    private void mostrarErro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro ao abrir respostas");
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