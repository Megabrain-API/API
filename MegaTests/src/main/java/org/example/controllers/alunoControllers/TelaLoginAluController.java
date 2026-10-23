package org.example.controllers.alunoControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.App;
import org.example.controllers.profControllers.SessaoUsuarioController;
import org.example.dao.AlunoDAO;

import java.io.IOException;

public class TelaLoginAluController {

    @FXML
    private Button entrar;

    @FXML
    private Hyperlink forgetsenha;

    @FXML
    private Hyperlink hyp_cadastraraluno;

    @FXML
    private TextField emailInput;

    @FXML
    private PasswordField senhaInput;

    @FXML
    private Button voltar;

    private AlunoDAO AlunoDAO = new AlunoDAO();
    @FXML
    void entrar_aluno(ActionEvent event) throws IOException{
        // Pega os textos digitados pelo aluno na tela
        String email = emailInput.getText();
        String senha = senhaInput.getText();

        // Chama o DAO para consultar no MySQL
        Long matriculaLogada = AlunoDAO.autenticarAluno(email, senha);

        // Valida a resposta do banco
        if (matriculaLogada != null) {
            // Sucesso: Guarda a matrícula na sessão do sistema
            SessaoUsuarioController.iniciarSessao(matriculaLogada, "Aluno");
            // Redireciona para o Menu Principal do Aluno
            App.setRoot("alunoFluxo/MenuAluno");
        } else {
            // Falha: Exibe alerta de credenciais inválidas
            System.out.println("E-mail ou senha inválidos!");
            // Se tiver um Label de erro na tela, você pode fazer: labelErro.setText("E-mail ou senha inválidos!");
        }

    }

    @FXML
    void hyper_cad_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/TelaCadAlu");
    }

    @FXML
    void hyper_esqueceu(ActionEvent event) {

    }

    @FXML
    void voltar_inicio(ActionEvent event) throws IOException {
        App.setRoot("telainicio");

    }

}