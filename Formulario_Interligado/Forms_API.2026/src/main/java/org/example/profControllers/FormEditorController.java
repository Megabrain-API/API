package org.example.profControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.App;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FormEditorController {

    @FXML
    private VBox container_dinamico; // O VBox principal onde as questões entram

    @FXML
    private Button btn_adicionar;

    @FXML
    void GeraJson(ActionEvent event) throws IOException {
        // Limpa a lista de questões caso o utilizador clique em salvar duas vezes
        org.example.SessaoApp.provaAtual.getQuestoes().clear();

        // Percorre cada VBox de questão que você adicionou no container_dinamico
        for (Node nodeQuestao : container_dinamico.getChildren()) {
            if (nodeQuestao instanceof VBox) {
                VBox questaoBox = (VBox) nodeQuestao;
                org.example.Questao questaoObj = new org.example.Questao();

                // 3. Percorre os elementos dentro do VBox da questão (TextFields, CheckBoxes...)
                for (Node elemento : questaoBox.getChildren()) {

                    // Se for um TextField e for a Pergunta (primeiro campo)
                    if (elemento instanceof TextField) {
                        TextField tf = (TextField) elemento;

                        if (tf.getPromptText().equals("Digite sua nova pergunta...")) {
                            questaoObj.setEnunciado(tf.getText());
                        } else if (tf.getPromptText().equals("digite alternativa")) {
                            // Se for uma alternativa, adiciona na lista da questão
                            questaoObj.getAlternativas().add(tf.getText());
                        }
                    }
                    // Você também pode verificar os CheckBoxes aqui para saber qual é a correta
                }

                // Adiciona a questão lida à nossa prova
                org.example.SessaoApp.provaAtual.getQuestoes().add(questaoObj);
            }
            // 4. Gera o ficheiro JSON usando Gson
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonFinal = gson.toJson(org.example.SessaoApp.provaAtual);

                // 1. Proteção: Se a prova não tiver nome (ou for nulo), damos um nome padrão
                String nomeOriginal = org.example.SessaoApp.provaAtual.getNome();
                if (nomeOriginal == null || nomeOriginal.trim().isEmpty()) {
                    nomeOriginal = "Prova_Sem_Nome";
                }

                // 2. Limpa o nome de caracteres especiais
                String nomeLimpo = nomeOriginal.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                String nomeDoFicheiro = nomeLimpo + ".json";

                // 3. Garante que a pasta existe (createDirectories no plural cria todas as pastas necessárias)
                Path pastaDestino = Path.of("provas_salvas");
                if (!Files.exists(pastaDestino)) {
                    Files.createDirectories(pastaDestino);
                }

                // 4. Cria o caminho completo e guarda o ficheiro
                Path caminhoCompleto = pastaDestino.resolve(nomeDoFicheiro);
                Files.writeString(caminhoCompleto, jsonFinal);

                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Sucesso!");
                alerta.setHeaderText("Prova salva com sucesso!");
                alerta.setContentText("O ficheiro foi salvo em:\n" + caminhoCompleto.toAbsolutePath());
                alerta.showAndWait();
                App.setRoot("ListaProva");

            } catch (Exception e) {
                Alert erro = new Alert(Alert.AlertType.ERROR);
                erro.setTitle("Erro");
                erro.setHeaderText("Erro ao salvar a prova");

                // Aqui adicionamos o nome da exceção (e.getClass()) para ser mais fácil descobrir futuros erros
                erro.setContentText(e.getClass().getSimpleName() + ": " + e.getMessage());
                erro.showAndWait();

                e.printStackTrace();
            }
        }

    }

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

            CheckBox check = new CheckBox();
            novaQuestaoBox.getChildren().add(check);
            TextField novo = new TextField();
            novo.setPromptText("digite alternativa");
            novaQuestaoBox.getChildren().add(novo);

            Button btnRemoveralt = new Button("Remover Alternativa");
            btnRemoveralt.setOnAction(e -> {
                novaQuestaoBox.getChildren().remove(check);
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