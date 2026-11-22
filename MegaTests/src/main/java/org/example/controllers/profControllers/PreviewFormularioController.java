package org.example.controllers.profControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.App;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.Questao;
import org.example.entity.profEntity.SessaoApp;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Pré-visualização de prova no padrão visual das telas de correção e aplicação. */
public class PreviewFormularioController {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_HORA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    @FXML
    private VBox vb_formulario;

    @FXML
    private Label lbl_titulo_preview;

    @FXML
    private Label lbl_disciplina_preview;

    @FXML
    private Label lbl_info_preview;

    @FXML
    private Label lbl_resumo_questoes;

    @FXML
    private Label lbl_resumo_pontos;

    @FXML
    private Button btn_voltar;

    @FXML
    public void initialize() {
        if (vb_formulario == null) {
            return;
        }
        vb_formulario.getChildren().clear();

        Prova prova = SessaoApp.provaAtual;

        if (prova == null) {
            Label vazio = new Label("Nenhuma prova foi selecionada para pré-visualização.");
            vazio.setStyle("-fx-text-fill: #1B1B1B; -fx-font-size: 14px;");
            vb_formulario.getChildren().add(vazio);
            return;
        }

        preencherCabecalho(prova);

        List<Questao> questoes = prova.getQuestoes();
        if (questoes == null || questoes.isEmpty()) {
            Label semQuestoes = new Label("Esta prova ainda não possui questões cadastradas.");
            semQuestoes.setStyle("-fx-text-fill: #5F6368; -fx-font-size: 14px;");
            vb_formulario.getChildren().add(semQuestoes);
            return;
        }

        int numeroQuestao = 1;
        for (Questao questao : questoes) {
            vb_formulario.getChildren().add(criarCardQuestao(questao, numeroQuestao));
            numeroQuestao++;
        }
    }

    private void preencherCabecalho(Prova prova) {
        if (lbl_titulo_preview != null) {
            lbl_titulo_preview.setText(valorOuVazio(prova.getNome()));
        }

        if (lbl_disciplina_preview != null) {
            String disc = prova.getDisciplina();
            lbl_disciplina_preview.setText((disc != null && !disc.isBlank()) ? "•  " + disc : "");
        }

        if (lbl_info_preview != null) {
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
            if (info.length() == 0) {
                info.append("Visualizando estrutura completa e gabaritos da avaliação");
            }
            lbl_info_preview.setText(info.toString());
        }

        int totalQuestoes = prova.getQuestoes() != null ? prova.getQuestoes().size() : 0;
        double totalPontos = prova.getQuestoes() != null
                ? prova.getQuestoes().stream().mapToDouble(Questao::getPontos).sum()
                : 0.0;

        if (lbl_resumo_questoes != null) {
            lbl_resumo_questoes.setText(totalQuestoes + (totalQuestoes == 1 ? " questão" : " questões"));
        }
        if (lbl_resumo_pontos != null) {
            lbl_resumo_pontos.setText(String.format(PT_BR, "Total: %.2f pts", totalPontos));
        }
    }

    private VBox criarCardQuestao(Questao questao, int numeroQuestao) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.96);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: #C4D6CA;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1.5px;"
        );

        String tipo = tipoNormalizado(questao.getTipo());

        // Topo do card: badge do tipo à esquerda e pontuação à direita
        HBox topoCard = new HBox(10);
        topoCard.setAlignment(Pos.CENTER_LEFT);

        Label badgeTipo = new Label(textoTipo(tipo));
        badgeTipo.setStyle(
                "-fx-background-color: #EEF5F0;" +
                        "-fx-text-fill: #285A36;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 3 10 3 10;" +
                        "-fx-background-radius: 8px;"
        );

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        Label badgePontos = new Label(String.format(PT_BR, "Vale %.2f pt(s)", questao.getPontos()));
        badgePontos.setStyle(
                "-fx-background-color: #F4F6F5;" +
                        "-fx-text-fill: #1B1B1B;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 3 10 3 10;" +
                        "-fx-background-radius: 8px;"
        );

        topoCard.getChildren().addAll(badgeTipo, espacador, badgePontos);
        card.getChildren().add(topoCard);

        // Enunciado
        Label pergunta = new Label(numeroQuestao + ". " + valorOuVazio(questao.getEnunciado()));
        pergunta.setFont(Font.font("System", FontWeight.BOLD, 15));
        pergunta.setStyle("-fx-text-fill: #1B1B1B;");
        pergunta.setWrapText(true);
        pergunta.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().add(pergunta);

        // Conteúdo específico de cada tipo de questão
        if (Questao.DISSERTATIVA.equals(tipo)) {
            Label campoSimulado = new Label("Área de resposta dissertativa do aluno...");
            campoSimulado.setMaxWidth(Double.MAX_VALUE);
            campoSimulado.setStyle(
                    "-fx-text-fill: #757575;" +
                            "-fx-font-style: italic;" +
                            "-fx-background-color: #F6F7F6;" +
                            "-fx-border-color: #DCDCDC;" +
                            "-fx-border-radius: 8px;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-padding: 12;"
            );
            card.getChildren().add(campoSimulado);

            String modelo = questao.getRespostaModelo();
            if (modelo != null && !modelo.trim().isEmpty()) {
                VBox caixaModelo = new VBox(4);
                Label lblTitModelo = new Label("Gabarito / Resposta de referência:");
                lblTitModelo.setFont(Font.font("System", FontWeight.BOLD, 12));
                lblTitModelo.setStyle("-fx-text-fill: #285A36;");

                Label lblTextoModelo = new Label(modelo.trim());
                lblTextoModelo.setWrapText(true);
                lblTextoModelo.setMaxWidth(Double.MAX_VALUE);
                lblTextoModelo.setStyle(
                        "-fx-text-fill: #285A36;" +
                                "-fx-background-color: #DDEEE2;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-padding: 9 12 9 12;"
                );

                caixaModelo.getChildren().addAll(lblTitModelo, lblTextoModelo);
                card.getChildren().add(caixaModelo);
            }

        } else if (Questao.CORRESPONDENCIA.equals(tipo)) {
            VBox caixaItens = new VBox(5);
            Label lblItens = new Label("Coluna 1 — Itens numerados:");
            lblItens.setFont(Font.font("System", FontWeight.BOLD, 12));
            lblItens.setStyle("-fx-text-fill: #1B1B1B;");
            caixaItens.getChildren().add(lblItens);

            List<String> itens = questao.getItensCorrespondencia();
            for (int i = 0; i < itens.size(); i++) {
                Label itemLbl = new Label((i + 1) + ". " + itens.get(i));
                itemLbl.setWrapText(true);
                itemLbl.setMaxWidth(Double.MAX_VALUE);
                itemLbl.setStyle(
                        "-fx-text-fill: #1B1B1B;" +
                                "-fx-background-color: #F4F5F4;" +
                                "-fx-background-radius: 6px;" +
                                "-fx-padding: 6 10 6 10;"
                );
                caixaItens.getChildren().add(itemLbl);
            }
            card.getChildren().add(caixaItens);

            VBox caixaGabarito = new VBox(5);
            Label lblGabarito = new Label("Coluna 2 — Alternativas e Gabarito:");
            lblGabarito.setFont(Font.font("System", FontWeight.BOLD, 12));
            lblGabarito.setStyle("-fx-text-fill: #285A36;");
            caixaGabarito.getChildren().add(lblGabarito);

            List<String> opcoes = questao.getAlternativasCorrespondencia();
            List<Integer> gabarito = questao.getGabaritoCorrespondencia();
            for (int i = 0; i < opcoes.size(); i++) {
                String opcaoTexto = opcoes.get(i);
                Integer numCorreto = i < gabarito.size() ? gabarito.get(i) : null;
                String nomeItemCorreto = (numCorreto != null && numCorreto >= 1 && numCorreto <= itens.size())
                        ? " (" + itens.get(numCorreto - 1) + ")"
                        : "";

                Label gabLbl = new Label("✓ " + opcaoTexto + "  ──>  Item " + numCorreto + nomeItemCorreto);
                gabLbl.setWrapText(true);
                gabLbl.setMaxWidth(Double.MAX_VALUE);
                gabLbl.setStyle(
                        "-fx-font-weight: bold;" +
                                "-fx-text-fill: #285A36;" +
                                "-fx-background-color: #DDEEE2;" +
                                "-fx-background-radius: 6px;" +
                                "-fx-padding: 7 10 7 10;"
                );
                caixaGabarito.getChildren().add(gabLbl);
            }
            card.getChildren().add(caixaGabarito);

        } else {
            boolean isChecklist = Questao.CHECKLIST.equals(tipo);
            List<String> alternativas = questao.getAlternativas();
            for (int indice = 0; indice < alternativas.size(); indice++) {
                String textoAlt = alternativas.get(indice);
                boolean correta = questao.getRespostasCorretas() != null
                        && questao.getRespostasCorretas().contains(indice);

                String marcador = isChecklist
                        ? (correta ? "☑ " : "☐ ")
                        : (correta ? "◉ " : "○ ");
                String sufixo = correta ? "  ✓ (Correta)" : "";

                Label lblAlt = new Label(marcador + valorOuVazio(textoAlt) + sufixo);
                lblAlt.setWrapText(true);
                lblAlt.setMaxWidth(Double.MAX_VALUE);

                if (correta) {
                    lblAlt.setStyle(
                            "-fx-font-weight: bold;" +
                                    "-fx-text-fill: #285A36;" +
                                    "-fx-background-color: #DDEEE2;" +
                                    "-fx-background-radius: 7px;" +
                                    "-fx-padding: 8 12 8 12;"
                    );
                } else {
                    lblAlt.setStyle(
                            "-fx-text-fill: #1B1B1B;" +
                                    "-fx-background-color: #F4F5F4;" +
                                    "-fx-background-radius: 7px;" +
                                    "-fx-padding: 8 12 8 12;"
                    );
                }
                card.getChildren().add(lblAlt);
            }
        }

        return card;
    }

    private String tipoNormalizado(String tipo) {
        if (tipo == null) {
            return Questao.MULTIPLA_ESCOLHA;
        }
        String normalizado = tipo.trim().toUpperCase();
        if ("DISSERTATIVA".equals(normalizado)) {
            return Questao.DISSERTATIVA;
        }
        if ("CHECKLIST".equals(normalizado)) {
            return Questao.CHECKLIST;
        }
        if ("CORRESPONDENCIA".equals(normalizado) || "CORRESPONDÊNCIA".equals(normalizado)
                || (Questao.CORRESPONDENCIA != null && Questao.CORRESPONDENCIA.equalsIgnoreCase(tipo))) {
            return Questao.CORRESPONDENCIA;
        }
        return Questao.MULTIPLA_ESCOLHA;
    }

    private String textoTipo(String tipo) {
        if (Questao.DISSERTATIVA.equals(tipo)) return "Questão dissertativa";
        if (Questao.CHECKLIST.equals(tipo)) return "Checklist numerado";
        if (Questao.CORRESPONDENCIA.equals(tipo)) return "Correspondência";
        return "Múltipla escolha";
    }

    private String valorOuVazio(String valor) {
        return valor == null ? "" : valor;
    }

    @FXML
    private void VoltarLista(ActionEvent event) {
        try {
            App.setRoot("professorFluxo/ListaProva");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
