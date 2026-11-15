package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.example.App;
import org.example.controllers.profControllers.SessaoUsuarioController;
import org.example.dao.DadosPessoaisDAO;

import java.io.IOException;
import java.sql.SQLException;

public class DadosPessoaisController {

    @FXML
    private BorderPane telaDados;

    @FXML
    private Button botaoVoltar;

    @FXML
    private Label perfil;

    @FXML
    private Label identificadorLabel;

    @FXML
    private Label mensagem;

    @FXML
    private TextField nome;

    @FXML
    private TextField email;

    @FXML
    private TextField identificador;

    @FXML
    private TextField curso;

    @FXML
    private VBox grupoCurso;

    @FXML
    private void initialize() {
        Long id = SessaoUsuarioController.getIdUsuarioLogado();
        String tipo = SessaoUsuarioController.getTipoUsuario();
        boolean aluno = "Aluno".equalsIgnoreCase(tipo);
        boolean professor = "Professor".equalsIgnoreCase(tipo);
        configurarLayout(aluno);
        grupoCurso.setVisible(aluno);
        grupoCurso.setManaged(aluno);
        perfil.setText(aluno ? "Perfil do aluno" : "Perfil do professor");
        identificadorLabel.setText(aluno ? "RA" : "Matrícula");
        if (id == null || (!aluno && !professor)) {
            mostrarErro("Sessão não identificada. Volte e faça login novamente.");
            return;
        }
        try {
            String[] dados = new DadosPessoaisDAO().buscar(id, aluno);
            nome.setText(dados[0]);
            email.setText(dados[1]);
            identificador.setText(dados[2]);
            curso.setText(dados[3] == null ? "Curso não informado" : dados[3]);
        } catch (SQLException erro) {
            mostrarErro("Não foi possível carregar seus dados. Verifique a conexão e tente novamente.");
        }
    }

    private void configurarLayout(boolean aluno) {
        String cor = aluno ? "#0066ff" : "#00674f";
        botaoVoltar.setStyle("-fx-background-color: " + cor
                + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
        String barra = aluno ? "alunoFluxo/BarraLateralAluno.fxml"
                : "professorFluxo/BarraLateralProf.fxml";
        try {
            telaDados.setLeft(FXMLLoader.load(App.class.getResource(barra)));
        } catch (IOException erro) {
            mostrarErro("Não foi possível carregar a barra lateral.");
        }
    }

    private void mostrarErro(String texto) {
        mensagem.setText(texto);
        mensagem.setVisible(true);
        mensagem.setManaged(true);
    }

    @FXML
    private void voltar() throws IOException {
        App.voltarDosDadosPessoais();
    }
}
