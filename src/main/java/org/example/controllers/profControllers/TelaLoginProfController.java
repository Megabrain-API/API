package org.example.controllers.profControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.App;
import org.example.dao.ProfessorDAO;
import org.example.controllers.profControllers.SessaoUsuarioController;

import java.io.IOException;

public class TelaLoginProfController {

    @FXML
    private TextField emailInput; // ID do campo "Insira seu email" no FXML

    @FXML
    private PasswordField senhaInput; // ID do campo "Insira sua senha" no FXML

    private ProfessorDAO professorDAO = new ProfessorDAO();

    @FXML
    void entrar_prof(ActionEvent event) throws IOException {
        // 1. Pega os textos digitados pelo professor na tela
        String email = emailInput.getText();
        String senha = senhaInput.getText();

        // 2. Chama o DAO para consultar no MySQL
        Long matriculaLogada = professorDAO.autenticarProfessor(email, senha);

        // 3. Valida a resposta do banco
        if (matriculaLogada != null) {
            // Sucesso: Guarda a matrícula na sessão do sistema
            SessaoUsuarioController.iniciarSessao(matriculaLogada, "Professor");
            // Redireciona para o Menu Principal do Professor
            App.setRoot("professorFluxo/MenuProf");
        } else {
            // Falha: Exibe alerta de credenciais inválidas
            System.out.println("E-mail ou senha inválidos!");
            // Se tiver um Label de erro na tela, você pode fazer: labelErro.setText("E-mail ou senha inválidos!");
        }
    }

    @FXML
    void hyper_prof_cadastro(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/TelaCadProf");
    }

    @FXML
    void voltar_inicio(ActionEvent event) throws IOException {
        App.setRoot("telainicio");
    }
}