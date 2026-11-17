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

        // Declara e obtém a prova atual da sessão de forma local e segura
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
                String modelo = questao.getRespostaModelo();
                if (modelo != null && !modelo.trim().isEmpty()) {
                    Label respostaModelo = new Label("Resposta de referência (Gabarito): " + modelo.trim());
                    respostaModelo.setWrapText(true);
                    respostaModelo.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                    bloco.getChildren().add(respostaModelo);
                }
            } else if (Questao.CORRESPONDENCIA.equals(tipo)) {
                Label lblItens = new Label("Itens para relacionar:");
                lblItens.setStyle("-fx-font-weight: bold; -fx-text-fill: #444444;");
                bloco.getChildren().add(lblItens);

                int idx = 1;
                for (String itemTexto : questao.getItensCorrespondencia()) {
                    Label itemLbl = new Label("  " + idx + ". " + itemTexto);
                    bloco.getChildren().add(itemLbl);
                    idx++;
                }

                Label lblGabarito = new Label("Gabarito (Associações corretas):");
                lblGabarito.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-padding: 5 0 0 0;");
                bloco.getChildren().add(lblGabarito);

                for (int i = 0; i < questao.getAlternativasCorrespondencia().size(); i++) {
                    String opcaoTexto = questao.getAlternativasCorrespondencia().get(i);
                    Integer numCorreto = questao.getGabaritoCorrespondencia().get(i);

                    Label gabLbl = new Label("  - " + opcaoTexto + " ──> Item [ " + numCorreto + "]");
                    gabLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
                    bloco.getChildren().add(gabLbl);
                }

            } else if (Questao.CHECKLIST.equals(tipo)) {
                int indice = 0;
                for (String alternativa : questao.getAlternativas()) {
                    CheckBox item = new CheckBox(alternativa);
                    item.setPadding(new Insets(0, 0, 5, 20));
                    item.setDisable(true);

                    // Destaca se for alternativa correta
                    if (questao.getRespostasCorretas() != null && questao.getRespostasCorretas().contains(indice)) {
                        item.setText(alternativa + " ✓ (Correta)");
                        item.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
                    }
                    bloco.getChildren().add(item);
                    indice++;
                }
            } else {
                ToggleGroup grupo = new ToggleGroup();
                int indice = 0;
                for (String alternativa : questao.getAlternativas()) {
                    RadioButton opcao = new RadioButton(alternativa);
                    opcao.setToggleGroup(grupo);
                    opcao.setPadding(new Insets(0, 0, 5, 20));
                    opcao.setDisable(true);

                    // Destaca se for alternativa correta
                    if (questao.getRespostasCorretas() != null && questao.getRespostasCorretas().contains(indice)) {
                        opcao.setText(alternativa + " ✓ (Correta)");
                        opcao.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
                    }
                    bloco.getChildren().add(opcao);
                    indice++;
                }
            }

            vb_formulario.getChildren().add(bloco);
            numeroQuestao++;
        }
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
        if ("CORRESPONDENCIA".equals(normalizado) || "CORRESPONDÊNCIA".equals(normalizado) || (Questao.CORRESPONDENCIA != null && Questao.CORRESPONDENCIA.equalsIgnoreCase(tipo))) {
            return Questao.CORRESPONDENCIA;
        }
        return Questao.MULTIPLA_ESCOLHA;
    }

    private String textoTipo(String tipo) {
        if (Questao.DISSERTATIVA.equals(tipo)) return "Dissertativa";
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
