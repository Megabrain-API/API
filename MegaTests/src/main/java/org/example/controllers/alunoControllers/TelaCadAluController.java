package org.example.controllers.alunoControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import org.example.App;
import org.example.dao.AlunoDAO;

import java.io.IOException;

public class TelaCadAluController {

    @FXML
    private Button btn_cadastro_aluno;

    @FXML
    private TextField cad_confirmar_senha_alu;

    @FXML
    private TextField cad_nome_alu;

    @FXML
    private TextField cad_ra;

    @FXML
    private TextField cad_email_aluno;

    @FXML
    private TextField cad_senha_alu;

    @FXML
    private Hyperlink hyper_entrar;


    @FXML
    void cad_aluno(ActionEvent event) throws IOException {
        try {
            String nome = cad_nome_alu.getText();
            String email = cad_email_aluno.getText();
            String senha = cad_senha_alu.getText();

            AlunoDAO alunoDAO = new AlunoDAO();
            alunoDAO.CriaAluno(nome, email, senha);

            System.out.println("Aluno criado com sucesso!");
        }
        catch (Exception e){
            System.out.println("Erro ao validar as senhas: " + e.getMessage());
            return; // Sai do metodo sem prosseguir com o Cadastro
        }

        App.setRoot("alunoFluxo/TelaLoginAlu");

    }

    @FXML
    void entrar_login(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/TelaLoginAlu");

    }

}
