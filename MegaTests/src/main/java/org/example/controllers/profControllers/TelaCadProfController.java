package org.example.controllers.profControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import org.example.App;
import org.example.dao.ProfessorDAO;

import java.io.IOException;
import java.sql.SQLException;

public class TelaCadProfController {

    @FXML
    private Button btn_cadastro_prof;

    @FXML
    private TextField cad_confirmar_senha_prof;

    @FXML
    private TextField cad_email_prof;

    @FXML
    private TextField cad_nome_prof;

    @FXML
    private TextField cad_senha_prof;

    @FXML
    private Hyperlink hyper_entrar_prof;

    @FXML
    void cad_prof(ActionEvent event)throws IOException {
        try {
            String nome = cad_nome_prof.getText();
            String email = cad_email_prof.getText();
            String senha = cad_senha_prof.getText();
            String confirmarSenha = cad_confirmar_senha_prof.getText();
            // Mantém uma validação simples antes de mandar os dados para o DAO.
            if (nome == null || nome.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    senha == null || senha.isEmpty()) {
                throw new IllegalArgumentException("Preencha nome, e-mail e senha.");
            }

            if (!senha.equals(confirmarSenha)) {
                throw new IllegalArgumentException("As senhas não coincidem.");
            }

            ProfessorDAO professorDAO = new ProfessorDAO();
            professorDAO.criaProfessor(nome, email, senha);
            System.out.println("Professor criado com sucesso!");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao cadastrar Professor");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return; // Sai do método sem prosseguir com o cadastro
        }

        App.setRoot("professorFluxo/TelaLoginProf");

    }

    @FXML
    void entrar_login_prof(ActionEvent event)throws IOException {
        App.setRoot("professorFluxo/TelaLoginProf");

    }

}
