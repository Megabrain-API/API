package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class FormEditorController {

    @FXML
    private VBox container_dinamico; // O VBox principal onde as questões entram

    @FXML
    private Button btn_adicionar;

    @FXML
    private void adicionarQuestao() {
        // Cria um pequeno container para a nova questão
        VBox novaQuestaoBox = new VBox(5);
        novaQuestaoBox.setStyle("-fx-border-color: #ccc; -fx-padding: 10; -fx-border-radius: 5;");

        // Campo de texto para a pergunta
        TextField txtPergunta = new TextField();
        txtPergunta.setPromptText("Digite sua nova pergunta...");
        TextField txtAlternativa = new TextField();
        txtAlternativa.setPromptText("Digite a Alternativa...");


        // Botão para remover esta questão específica (opcional, igual ao Forms)
        Button btnRemover = new Button("Remover");
        btnRemover.setOnAction(e -> container_dinamico.getChildren().remove(novaQuestaoBox));



        // botao pra adicionar alternativa na questao
        Button btnAddAlt = new Button("Adicionar Alternativa");
        btnAddAlt.setOnAction(a -> {

            TextField novo = new TextField();
            novo.setPromptText("digite alternativa");
            novaQuestaoBox.getChildren().add(novo);

            Button btnRemoveralt = new Button("Remover Alternativa");
            btnRemoveralt.setOnAction(e -> {
                novaQuestaoBox.getChildren().remove(novo);

                novaQuestaoBox.getChildren().remove(btnRemoveralt);
            });
            novaQuestaoBox.getChildren().add(btnRemoveralt);
        });

        // Adiciona os elementos dentro do bloco da questão
        novaQuestaoBox.getChildren().addAll(txtPergunta, btnRemover, btnAddAlt);

        // Adiciona o bloco da questão no container principal da tela
        container_dinamico.getChildren().add(novaQuestaoBox);
    }
    void adAlternativa(ActionEvent event){}
}