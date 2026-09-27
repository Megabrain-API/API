package org.example.profControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import org.example.App;

import java.io.IOException;

public class TelaLoginProfController {

    @FXML
    private Button entrar;

    @FXML
    private Hyperlink forgetsenha;

    @FXML
    private Hyperlink hyp_profcadastro;

    @FXML
    private Button voltar;

    @FXML
    void entrar_prof(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/MenuProf");

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
