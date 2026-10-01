package org.example.alunoControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import org.example.App;

import java.io.IOException;

public class TelaLoginAluController {

    @FXML
    private Button entrar;

    @FXML
    private Hyperlink forgetsenha;

    @FXML
    private Hyperlink hyp_cadastraraluno;

    @FXML
    private TextField txt_ra;

    @FXML
    private TextField txt_senha_aluno;

    @FXML
    private Button voltar;

    @FXML
    void entrar_aluno(ActionEvent event) throws IOException{
        App.setRoot("alunoFluxo/MenuAluno");

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
