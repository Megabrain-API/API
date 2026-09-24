package org.example;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ListaProvaController {

    @FXML
    private Button btn_acessar;

    // Variável para guardar qual prova o utilizador clicou/selecionou na lista
    private Prova provaSelecionadaTemporaria;

    // Variável para guardar o botão clicado para podermos mudar a cor dele
    private Button botaoSelecionadoAnteriormente;

    @FXML
    private VBox vb_provacontainer;

    public void initialize() {
        carregaProvasSalvas();
    }

    private void carregaProvasSalvas() {
        // Aponta para a pasta onde os JSONs estão guardados
        File pasta = new File("provas_salvas");

        // Verifica se a pasta existe
        if (pasta.exists() && pasta.isDirectory()) {

            // Pega apenas os arquivos que terminam com .json
            File[] arquivosJson = pasta.listFiles((dir, nome) -> nome.endsWith(".json"));

            if (arquivosJson != null) {
                Gson gson = new Gson();

                // Passa por cada arquivo JSON encontrado
                for (File arquivo : arquivosJson) {
                    try {
                        // Lê o texto do arquivo
                        String conteudo = Files.readString(arquivo.toPath());

                        // Transforma o texto JSON de volta no objeto Prova
                        Prova provaRecuperada = gson.fromJson(conteudo, Prova.class);

                        // Cria o botão da prova
                        Button btnProva = new Button(provaRecuperada.getNome());
                        btnProva.setMaxWidth(Double.MAX_VALUE);
                        btnProva.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand;");

                        // Cria o botão de remover (adicionei uma cor vermelha para destacar)
                        Button btnRemove = new Button("Remover");
                        btnRemove.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand; -fx-background-color: #ff4444; -fx-text-fill: white;");

                        // Cria a caixa horizontal (HBox) com um espaçamento de 10px entre os itens
                        HBox linhaDaProva = new HBox(10);
                        linhaDaProva.setAlignment(Pos.CENTER_LEFT);

                        // Faz o botão da prova esticar para ocupar o espaço vazio, empurrando o "Remover" para a direita
                        HBox.setHgrow(btnProva, Priority.ALWAYS);

                        // Define a ação do botão da Prova (abrir)
                        btnProva.setOnAction(evento -> {
                          // Guarda a prova selecionada na variável temporária
                                    provaSelecionadaTemporaria = provaRecuperada;

                            // Tira a cor "selecionada" do botão anterior (se houver)
                            if (botaoSelecionadoAnteriormente != null) {
                                botaoSelecionadoAnteriormente.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand;");
                            }

                            // Pinta o botão atual para mostrar que está selecionado (ex: azul claro)
                            btnProva.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand; -fx-background-color: #add8e6;");

                            // Guarda este botão como o último selecionado
                            botaoSelecionadoAnteriormente = btnProva;
                        });

                        // Define a ação do botão Remover (apagar ficheiro e remover do ecrã)
                        btnRemove.setOnAction(evento -> {
                            try {
                                // Apaga o ficheiro JSON da pasta 'provas_salvas'
                                Files.delete(arquivo.toPath());

                                // Remove o HBox inteiro (botão prova + botão remover) da tela
                                vb_provacontainer.getChildren().remove(linhaDaProva);

                                // Confirma a Criaçao da prova no Terminal
                                System.out.println("Prova apagada com sucesso: " + arquivo.getName());
                            } catch (Exception e) {
                                System.out.println("Erro ao tentar apagar o ficheiro: " + arquivo.getName());
                                e.printStackTrace();
                            }
                        });

                        // Coloca os dois botões dentro do HBox
                        linhaDaProva.getChildren().addAll(btnProva, btnRemove);

                        // Adiciona o HBox (com os dois botões) ao VBox principal da tela
                        vb_provacontainer.getChildren().add(linhaDaProva);

                    } catch (Exception e) {
                        System.out.println("Erro ao ler o arquivo: " + arquivo.getName());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("A pasta provas_salvas ainda não existe ou está vazia.");
        }
    }


    private void abrirProva(Prova provaSelecionada) {
        // Guarda a prova selecionada na sessão para a próxima tela usar
        SessaoApp.provaAtual = provaSelecionada;

        Alert aviso = new Alert(Alert.AlertType.INFORMATION);
        aviso.setTitle("Prova Selecionada");
        aviso.setHeaderText("Você clicou na prova: " + provaSelecionada.getNome());
        aviso.setContentText("A disciplina é: " + provaSelecionada.getDisciplina() +
                "\nTotal de questões: " + provaSelecionada.getQuestoes().size());
        aviso.showAndWait();

        // Aqui você pode usar o App.setRoot("SuaProximaTela"); para mudar de tela

    }

    @FXML
    void AcessaProva(ActionEvent event) {
        // Verifica se o utilizador clicou numa prova antes de clicar em Acessar
        if (provaSelecionadaTemporaria == null) {
            Alert erro = new Alert(Alert.AlertType.WARNING);
            erro.setTitle("Aviso");
            erro.setHeaderText("Nenhuma prova selecionada!");
            erro.setContentText("Por favor, clique no nome de uma prova na lista antes de clicar em Acessar.");
            erro.showAndWait();
            return;
        }

        // Se tiver selecionado, guarda na sessão global e muda de tela
        SessaoApp.provaAtual = provaSelecionadaTemporaria;
        try {
            System.out.println("URL: " + App.class.getResource("PreviewFormulario.fxml"));
            System.out.println("URL Lista: " + App.class.getResource("ListaProva.fxml"));

            // Carrega o FXML diretamente à força
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("PreviewFormulario.fxml"));
            javafx.scene.Parent root = loader.load();

            // Substitui o conteúdo da tela atual pela tela nova
            vb_provacontainer.getScene().setRoot(root);

            //App.setRoot("PrewiewFormulario"); // Mude para o nome do seu novo arquivo FXML
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void CriarProva(ActionEvent event) throws IOException {
        App.setRoot("CriaProva");
    }

    @FXML
    void VoltarInicio(ActionEvent event) {

    }
}