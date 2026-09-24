package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

import java.io.IOException;

public class TelaCadProfController {

    @FXML
    private Button btn_cadastro_prof;

    @FXML
    private TextField cad_confirmar_senha_prof;

    @FXML
    private TextField cad_matricula;

    @FXML
    private TextField cad_nome_prof;

    @FXML
    private TextField cad_senha_prof;

    @FXML
    private Hyperlink hyper_entrar_prof;

    @FXML
    void cad_prof(ActionEvent event)throws IOException {
        App.setRoot("professorFluxo/TelaLoginProf");

    }

    @FXML
    void entrar_login_prof(ActionEvent event)throws IOException {
        App.setRoot("professorFluxo/TelaLoginProf");

    }

}
