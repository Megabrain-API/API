package org.example.controllers.profControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.App;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.Questao;
import org.example.entity.profEntity.SessaoApp;

/** Pré-visualização de prova com questões objetivas, checklist e dissertativas. */
public class PreviewFormularioController {

    @FXML
    private VBox vb_formulario;

    @FXML
    public void initialize() {
        if (vb_formulario == null) {
            return;
        }
        vb_formulario.getChildren().clear();
        Prova prova = SessaoApp.provaAtual;
        if (prova == null) {
            vb_formulario.getChildren().add(new Label("Nenhuma prova foi selecionada."));
            return;
        }

        Label titulo = new Label("Visualizando: " + valorOuVazio(prova.getNome()));
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setPadding(new Insets(10, 0, 20, 0));
        titulo.setWrapText(true);
        vb_formulario.getChildren().add(titulo);

        if (prova.getQuestoes() == null || prova.getQuestoes().isEmpty()) {
            Label semQuestoes = new Label(
                    "Não há detalhes das questões disponíveis para esta prova. "
                    + "Para provas mistas, é necessário o arquivo JSON salvo em 'provas_salvas'.");
            semQuestoes.setWrapText(true);
            vb_formulario.getChildren().add(semQuestoes);
            return;
        }

        int numeroQuestao = 1;
        for (Questao questao : prova.getQuestoes()) {
            VBox bloco = new VBox(7);
            bloco.setPadding(new Insets(10, 0, 12, 0));

            Label pergunta = new Label(numeroQuestao + ". " + valorOuVazio(questao.getEnunciado()));
            pergunta.setFont(Font.font("System", FontWeight.BOLD, 14));
            pergunta.setWrapText(true);
            bloco.getChildren().add(pergunta);

            String tipo = tipoNormalizado(questao.getTipo());
            Label rotuloTipo = new Label("Tipo: " + textoTipo(tipo));
            rotuloTipo.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
            bloco.getChildren().add(rotuloTipo);

            if (Questao.DISSERTATIVA.equals(tipo)) {
                TextArea resposta = new TextArea();
                resposta.setPromptText("Resposta do aluno (pré-visualização)");
                resposta.setWrapText(true);
                resposta.setPrefRowCount(3);
                resposta.setEditable(false);
                resposta.setDisable(true);
                bloco.getChildren().add(resposta);

                String modelo = questao.getRespostaModelo();
                if (modelo != null && !modelo.trim().isEmpty()) {
                    Label respostaModelo = new Label("Resposta de referência: " + modelo.trim());
                    respostaModelo.setWrapText(true);
                    respostaModelo.setStyle("-fx-text-fill: #555555;");
                    bloco.getChildren().add(respostaModelo);
                }
            } else if (Questao.CHECKLIST.equals(tipo)) {
                for (String alternativa : questao.getAlternativas()) {
                    CheckBox item = new CheckBox(alternativa);
                    item.setPadding(new Insets(0, 0, 5, 20));
                    item.setDisable(true);
                    bloco.getChildren().add(item);
                }
            } else {
                ToggleGroup grupo = new ToggleGroup();
                for (String alternativa : questao.getAlternativas()) {
                    RadioButton opcao = new RadioButton(alternativa);
                    opcao.setToggleGroup(grupo);
                    opcao.setPadding(new Insets(0, 0, 5, 20));
                    opcao.setDisable(true);
                    bloco.getChildren().add(opcao);
                }
            }

            vb_formulario.getChildren().add(bloco);
            numeroQuestao++;
        }
    }

    private String tipoNormalizado(String tipo) {
        if (tipo == null) {
            // Compatibilidade com provas antigas, que não guardavam o tipo.
            return Questao.MULTIPLA_ESCOLHA;
        }
        String normalizado = tipo.trim().toUpperCase();
        if ("DISSERTATIVA".equals(normalizado)) {
            return Questao.DISSERTATIVA;
        }
        if ("CHECKLIST".equals(normalizado)) {
            return Questao.CHECKLIST;
        }
        return Questao.MULTIPLA_ESCOLHA;
    }

    private String textoTipo(String tipo) {
        if (Questao.DISSERTATIVA.equals(tipo)) return "Dissertativa";
        if (Questao.CHECKLIST.equals(tipo)) return "Checklist numerado";
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
