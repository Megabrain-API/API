package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controllers.alunoControllers.FazerProvaController;
import org.example.dao.ConexaoDAO;

import java.io.IOException;
import java.sql.Connection;

public class App extends Application {

    private static Scene scene;
    private static Parent telaAntesDosDados;

    @Override
    public void start(Stage stage) throws IOException {

        // teste do banco de dados
        try (Connection conn = new ConexaoDAO().conectaBD()) {
            System.out.println("Conectado ao banco: " + conn.getCatalog());

            // Testa listar as tabelas que o Java realmente vê lá dentro
            var metaData = conn.getMetaData();
            var rs = metaData.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"});

            System.out.println("--- Tabelas visíveis pelo Java ---");
            while (rs.next()) {
                System.out.println("Tabela encontrada: " + rs.getString("TABLE_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Criando a tabela mg_prova_questao a força
        /*try (Connection conn = new ConexaoDAO().conectaBD();
             Statement stmt = conn.createStatement()) {

            String sqlCriarTabela = "CREATE TABLE IF NOT EXISTS mg_prova_questao (" +
                    "id_prova BIGINT NOT NULL, " +
                    "id_questao BIGINT NOT NULL, " +
                    "ordem INT, " +
                    "PRIMARY KEY (id_prova, id_questao), " +
                    "FOREIGN KEY (id_prova) REFERENCES mg_provas(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (id_questao) REFERENCES mg_questoes(id) ON DELETE CASCADE" +
                    ")";

            stmt.executeUpdate(sqlCriarTabela);
            System.out.println("Tabela mg_prova_questao garantida com sucesso via Java!");
        } catch (Exception e) {
            e.printStackTrace();
        } */

        // Inicializa a cena com a resolução padrão
        scene = new Scene(loadFXML("professorFluxo/BancoQuestoes"), 960, 540);

        // Carrega o arquivo de estilos
        scene.getStylesheets().add(App.class.getResource("estilo.css").toExternalForm());

        // Configurações da janela
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setResizable(true);

        // --- SUBMISSÃO AUTOMÁTICA SE FECHAR A JANELA ---
        stage.setOnCloseRequest(event -> {
            if (FazerProvaController.isProvaEmAndamento()) {
                FazerProvaController.submeterProvaAutomaticamenteNoEncerramento();
            }
        });

        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        // Se mudar de tela enquanto estiver a fazer a prova, submete automaticamente
        if (FazerProvaController.isProvaEmAndamento()) {
            FazerProvaController.submeterProvaAutomaticamenteNoEncerramento();
        }
        Parent destino = loadFXML(fxml);
        telaAntesDosDados = null;
        scene.setRoot(destino);
    }

    /** Mantém a tela anterior viva, incluindo respostas e cronômetro de uma prova. */
    public static void abrirDadosPessoais() throws IOException {
        if (telaAntesDosDados != null) return;
        Parent dados = loadFXML("DadosPessoais");
        telaAntesDosDados = scene.getRoot();
        scene.setRoot(dados);
    }

    public static void voltarDosDadosPessoais() throws IOException {
        if (telaAntesDosDados != null) {
            Parent anterior = telaAntesDosDados;
            telaAntesDosDados = null;
            scene.setRoot(anterior);
        } else {
            boolean aluno = "Aluno".equalsIgnoreCase(
                    org.example.controllers.profControllers.SessaoUsuarioController.getTipoUsuario());
            setRoot(aluno ? "alunoFluxo/MenuAluno" : "professorFluxo/MenuProf");
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}