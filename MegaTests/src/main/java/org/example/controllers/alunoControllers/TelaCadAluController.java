package org.example.controllers.alunoControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.example.App;
import org.example.dao.AlunoDAO;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TelaCadAluController implements Initializable {

    @FXML
    private Button btn_cadastro_aluno;

    @FXML
    private TextField cad_confirmar_senha_alu;

    @FXML
    private TextField cad_nome_alu;

    @FXML
    private TextField cad_email_aluno;

    @FXML
    private TextField cad_senha_alu;

    @FXML
    private ComboBox<String> cad_curso_alu;

    @FXML
    private Hyperlink hyper_entrar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        carregarCursos();
    }

    /**
     * Busca os cursos existentes no banco e coloca os nomes dentro do ComboBox.
     */
    private void carregarCursos() {
        AlunoDAO alunoDAO = new AlunoDAO();
        List<String> cursos = alunoDAO.listarCursos();

        cad_curso_alu.getItems().clear();
        cad_curso_alu.getItems().addAll(cursos);

        if (cursos.isEmpty()) {
            cad_curso_alu.setPromptText("Nenhum curso cadastrado");
        }
    }

    @FXML
    void cad_aluno(ActionEvent event) throws IOException {
        try {
            String nome = cad_nome_alu.getText();
            String email = cad_email_aluno.getText();
            String senha = cad_senha_alu.getText();
            String confirmarSenha = cad_confirmar_senha_alu.getText();
            String curso = cad_curso_alu.getValue();

            // Mantém uma validação simples antes de mandar os dados para o DAO.
            if (nome == null || nome.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    senha == null || senha.isEmpty()) {
                throw new IllegalArgumentException("Preencha nome, e-mail e senha.");
            }

            if (!senha.equals(confirmarSenha)) {
                throw new IllegalArgumentException("As senhas não coincidem.");
            }

            if (curso == null || curso.trim().isEmpty()) {
                throw new IllegalArgumentException("Selecione um curso.");
            }

            AlunoDAO alunoDAO = new AlunoDAO();
            alunoDAO.CriaAluno(nome, email, senha, curso);

            System.out.println("Aluno criado com sucesso!");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao cadastrar aluno");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }

        App.setRoot("alunoFluxo/TelaLoginAlu");
    }

    @FXML
    void entrar_login(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/TelaLoginAlu");
    }
}
