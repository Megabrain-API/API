package org.example.controllers.alunoControllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.example.App;
import org.example.controllers.profControllers.SessaoUsuarioController;
import org.example.dao.AlunoDAO;
import org.example.entity.alunoEntity.ProvaCorrigidaAluno;
import org.example.entity.profEntity.Questao;
import org.example.entity.profEntity.RespostaAlunoProva;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class ProvaCorrigidaAlunoController implements Initializable {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

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
    private Label lblCurso;

    @FXML
    private Label lblDisciplina;

    @FXML
    private Label lblNomeProva;

    @FXML
    private Label lblNotaFinal;

    @FXML
    private VBox vboxQuestoes;

    private final Map<Button, String> textosOriginais = new HashMap<>();
    private Button[] botoesMenu;

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
        carregarCorrecao();
    }

    private void carregarCorrecao() {
        ProvaCorrigidaAluno prova = MeuCursoAlunoController.getProvaSelecionadaParaVisualizacao();
        Long raAluno = SessaoUsuarioController.getIdUsuarioLogado();

        if (prova == null || raAluno == null) {
            mostrarErro("Não foi possível identificar a prova corrigida selecionada.");
            return;
        }

        lblNomeProva.setText(prova.getNomeProva());
        lblDisciplina.setText(prova.getNomeDisciplina());
        lblCurso.setText(MeuCursoAlunoController.getNomeCursoAtual() != null
                ? MeuCursoAlunoController.getNomeCursoAtual()
                : "-");

        try {
            AlunoDAO dao = new AlunoDAO();
            List<RespostaAlunoProva> respostas = dao.buscarCorrecaoProvaDoAluno(prova.getIdProva(), raAluno);

            vboxQuestoes.getChildren().clear();

            double pontosObtidos = 0.0;
            double pontosMaximos = 0.0;

            for (RespostaAlunoProva resposta : respostas) {
                pontosObtidos += resposta.getNota();
                pontosMaximos += resposta.getPontosQuestao();
                vboxQuestoes.getChildren().add(criarCardQuestaoAluno(resposta));
            }

            if (pontosMaximos <= 0.0) {
                pontosObtidos = prova.getNotaTotal();
                pontosMaximos = prova.getPontosMaximos();
            }

            lblNotaFinal.setText(String.format(
                    PT_BR,
                    "Nota final: %.2f / %.2f pts",
                    pontosObtidos,
                    pontosMaximos
            ));
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro(e.getMessage());
        }
    }

    private VBox criarCardQuestaoAluno(RespostaAlunoProva resposta) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(card, Priority.NEVER);

        String corBorda;
        if (resposta.isParcial()) {
            corBorda = "#C68B00";
        } else if (resposta.getNota() > 0.0) {
            corBorda = "#4A7C59";
        } else {
            corBorda = "#B3261E";
        }

        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.97);" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-color: " + corBorda + ";"
        );

        Label enunciado = new Label(resposta.getOrdem() + ". " + valorOuVazio(resposta.getEnunciado()));
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

        if (Questao.DISSERTATIVA.equals(resposta.getTipo())) {
            card.getChildren().add(criarCampoTextoLeitura("Sua resposta", resposta.getRespostaAluno()));
            card.getChildren().add(criarCampoTextoLeitura("Gabarito / Resposta de referência", resposta.getGabarito()));
        } else if (Questao.CORRESPONDENCIA.equals(resposta.getTipo())) {
            card.getChildren().add(criarPainelItensCorrespondencia(resposta.getItensCorrespondencia()));
            card.getChildren().add(criarCampoTextoLeitura("Suas associações", resposta.getRespostaAluno()));
            card.getChildren().add(criarCampoTextoLeitura("Gabarito (Associações corretas)", resposta.getGabarito()));
        } else {
            for (RespostaAlunoProva.AlternativaCorrecao alternativa : resposta.getAlternativas()) {
                card.getChildren().add(criarLinhaAlternativa(alternativa));
            }
        }

        Label badge = new Label();
        badge.setFont(Font.font("System", FontWeight.BOLD, 12));
        if (resposta.isParcial()) {
            badge.setText(String.format(
                    PT_BR, "◐ PONTUAÇÃO PARCIAL  •  +%.2f / %.2f pts",
                    resposta.getNota(), resposta.getPontosQuestao()));
            badge.setStyle("-fx-text-fill: white; -fx-background-color: #C68B00; -fx-background-radius: 7px; -fx-padding: 6px 10px;");
        } else if (resposta.getNota() > 0.0) {
            badge.setText(String.format(
                    PT_BR, "✓ CORRETA  •  +%.2f / %.2f pts",
                    resposta.getNota(), resposta.getPontosQuestao()));
            badge.setStyle("-fx-text-fill: white; -fx-background-color: #4A7C59; -fx-background-radius: 7px; -fx-padding: 6px 10px;");
        } else {
            badge.setText(String.format(
                    PT_BR, "✗ INCORRETA  •  0,00 / %.2f pts",
                    resposta.getPontosQuestao()));
            badge.setStyle("-fx-text-fill: white; -fx-background-color: #B3261E; -fx-background-radius: 7px; -fx-padding: 6px 10px;");
        }

        card.getChildren().add(badge);
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
            prefixo = "✓ Sua escolha (Correta): ";
            fundo = "#DDEEE2";
            corTexto = "#285A36";
        } else if (alternativa.isSelecionada()) {
            prefixo = "✗ Sua escolha (Incorreta): ";
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

    private String textoTipo(String tipo) {
        if (Questao.DISSERTATIVA.equals(tipo)) return "Questão dissertativa";
        if (Questao.CHECKLIST.equals(tipo)) return "Checklist numerado";
        if (Questao.CORRESPONDENCIA.equals(tipo)) return "Correspondência";
        return "Múltipla escolha";
    }

    private String valorOuVazio(String valor) {
        return valor == null || valor.trim().isEmpty() ? "(sem resposta)" : valor;
    }

    private void mostrarErro(String msg) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro");
        alerta.setHeaderText("Não foi possível carregar a correção");
        alerta.setContentText(msg);
        alerta.showAndWait();
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
    void voltarAoCurso(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/MeuCursoAluno");
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
    void entra_meucurso(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/MeuCursoAluno");
    }
}
