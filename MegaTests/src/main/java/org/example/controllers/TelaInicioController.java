package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.example.App;
import org.example.controllers.profControllers.SessaoUsuarioController;

import java.io.IOException;

public class TelaInicioController {

    @FXML
    private Button btn_aluno;

    @FXML
    private Button btn_docente;

    @FXML
    void entrar_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/TelaLoginAlu");
        // Atribui o tipo ALUNO no SessaoUsuarioController
        SessaoUsuarioController.iniciarSessao(null, "Aluno");

        //Redireciona para o fluxo do Aluno
        App.setRoot("alunoFluxo/TelaLoginAlu");
    }

    @FXML
    void entrar_prof(ActionEvent event) throws IOException {
        SessaoUsuarioController.iniciarSessao(null, "Professor");
        App.setRoot("professorFluxo/TelaLoginProf");
    }

}

