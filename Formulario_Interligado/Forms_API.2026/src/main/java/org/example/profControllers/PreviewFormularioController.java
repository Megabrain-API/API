package org.example;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.event.ActionEvent;

public class PreviewFormularioController {

    @FXML
    private VBox vb_formulario;

    @FXML
    public void initialize() {
        // Pega a prova que está na sessão
        Prova prova = SessaoApp.provaAtual;

        // Título da Prova
        Label titulo = new Label("Visualizando: " + prova.getNome());
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setPadding(new Insets(10, 0, 20, 0));
        vb_formulario.getChildren().add(titulo);

        // Monta as Questões visualmente
        int numeroQuestao = 1;
        for (Questao q : prova.getQuestoes()) {

            Label lblPergunta = new Label(numeroQuestao + ". " + q.getEnunciado());
            lblPergunta.setFont(Font.font("System", FontWeight.BOLD, 14));
            lblPergunta.setPadding(new Insets(15, 0, 5, 0));
            lblPergunta.setWrapText(true);
            vb_formulario.getChildren().add(lblPergunta);

            for (String alternativa : q.getAlternativas()) {
                CheckBox cbAlternativa = new CheckBox(alternativa);
                cbAlternativa.setPadding(new Insets(0, 0, 5, 20));

                // Opcional: Desativa o clique para ficar claro que é só visualização
                cbAlternativa.setDisable(true);

                vb_formulario.getChildren().add(cbAlternativa);
            }
            numeroQuestao++;
        }
    }

    // Método para você colocar num botão "Voltar" no Scene Builder
    @FXML
    void VoltarLista(ActionEvent event) {
        try {
            // Carrega o FXML diretamente à força
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("ListaProva.fxml"));
            javafx.scene.Parent root = loader.load();

            // Substitui o conteúdo da tela atual pela tela nova
            vb_formulario.getScene().setRoot(root);


            //App.setRoot("VisualizaProva"); // Volta para a tela da lista
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}