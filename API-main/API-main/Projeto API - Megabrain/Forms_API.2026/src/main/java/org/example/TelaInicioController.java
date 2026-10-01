package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class TelaInicioController {

    @FXML
    private Button btn_aluno;

    @FXML
    private Button btn_docente;

    @FXML
    void entrar_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/TelaLoginAlu");

    }

    @FXML
    void entrar_prof(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/TelaLoginProf");

    }

}
