package org.example.controllers.alunoControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import org.example.App;

import java.io.IOException;

public class MenuAlunoController {

    @FXML
    private MenuItem dados_pessoais_alu;

    @FXML
    private MenuItem menu_meucurso;

    @FXML
    private MenuItem menu_sair_aluno;

    @FXML
    private MenuItem minhas_provas_aluno;

    @FXML
    void deslogar_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/TelaLoginAlu");
    }

    @FXML
    void entra_dados_pessoais_alu(ActionEvent event) {

    }

    @FXML
    void entra_menuprova_aluno(ActionEvent event) {

    }

    @FXML
    void entra_meucurso(ActionEvent event) {

    }

}
