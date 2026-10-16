package org.example.controllers.profControllers;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import org.example.App;
import org.example.dao.ProvaDAO;
import org.example.entity.profEntity.ListaProva;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.SessaoApp;
import javafx.geometry.Insets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

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

    // carregando as provas por meio do banco de dados
    private void carregaProvasSalvas() {
        //Cria o objeto da classe ProvaDAO
        ProvaDAO dao = new ProvaDAO();
        List<Prova> listaProvas = dao.listarNomeProvas();

        vb_provacontainer.getChildren().clear();


                // Passa por cada nome de prova retornado do banco
                for (Prova provaRecuperada : listaProvas) {
                    try {

                        // Cria o botão da prova
                        Button btnProva = new Button(provaRecuperada.getNome());
                        btnProva.setMaxWidth(Double.MAX_VALUE);
                        btnProva.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand; -fx-background-radius: 9px;");

                        // Cria o botão de remover (adicionei uma cor vermelha para destacar)
                        Button btnRemove = new Button("Remover");
                        btnRemove.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand; -fx-background-radius: 9px; -fx-background-color: #ff4444; -fx-text-fill: white;");

                        // Cria a caixa horizontal (HBox) com um espaçamento de 10px entre os itens
                        HBox linhaDaProva = new HBox(10);
                        linhaDaProva.setAlignment(Pos.CENTER_LEFT);

                        //Dá um espaçamento interno nas bordas da linha (cima, direita, baixo, esquerda)
                        linhaDaProva.setPadding(new Insets(5, 15, 5, 15));

                        // Faz o botão da prova esticar para ocupar o espaço vazio, empurrando o "Remover" para a direita
                        HBox.setHgrow(btnProva, Priority.ALWAYS);

                        // Define a ação do botão da Prova (abrir)
                        btnProva.setOnAction(evento -> {
                          // Guarda a prova selecionada na variável temporária
                                    provaSelecionadaTemporaria = provaRecuperada;

                            // Tira a cor "selecionada" do botão anterior (se houver)
                            if (botaoSelecionadoAnteriormente != null) {
                                botaoSelecionadoAnteriormente.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand; -fx-background-radius: 9px;");
                            }

                            // Pinta o botão atual para mostrar que está selecionado (ex: azul claro)
                            btnProva.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-cursor: hand; -fx-background-radius: 9px; -fx-background-color: #add8e6;");

                            // Guarda este botão como o último selecionado
                            botaoSelecionadoAnteriormente = btnProva;
                        });

                        // Define a ação do botão Remover (apagar ficheiro e remover do ecrã)
                        btnRemove.setOnAction(evento -> {
                            //TODO: chamar o metodo de excluir prova
                        });

                        // Coloca os dois botões dentro do HBox
                        linhaDaProva.getChildren().addAll(btnProva, btnRemove);

                        // Adiciona o HBox (com os dois botões) ao VBox principal da tela
                        vb_provacontainer.getChildren().add(linhaDaProva);

                    } catch (Exception e) {
                        System.out.println("Erro ao buscar prova");
                        e.printStackTrace();
                    }
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
            // Carrega o FXML diretamente à força
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(org.example.App.class.getResource("professorFluxo/PreviewFormulario.fxml"));
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
        App.setRoot("professorFluxo/CriaProva");
    }

    @FXML
    void VoltarInicio(ActionEvent event) throws IOException{
        App.setRoot("professorFluxo/MenuProf");
    }
}