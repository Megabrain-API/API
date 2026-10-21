package org.example.controllers.profControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

            ProfessorDAO professorDAO = new ProfessorDAO();
            professorDAO.criaProfessor(nome, email, senha);
            System.out.println("Professor criado com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao validar as senhas: " + e.getMessage());
            return; // Sai do método sem prosseguir com o cadastro
        }

        App.setRoot("professorFluxo/TelaLoginProf");

    }

    @FXML
    void entrar_login_prof(ActionEvent event)throws IOException {
        App.setRoot("professorFluxo/TelaLoginProf");

    }

}
