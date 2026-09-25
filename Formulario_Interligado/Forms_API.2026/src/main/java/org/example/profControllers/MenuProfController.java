package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

import java.io.IOException;

public class MenuProfController {

    @FXML
    private MenuItem menu_dados_pessoais_prof;

    @FXML
    private MenuItem menu_faq;

    @FXML
    private MenuItem menu_minhaturma;

    @FXML
    private MenuItem menu_sair_prof;

    @FXML
    void deslogar_prof(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/TelaLoginProf");
    }

    @FXML
    void entra_dados_pessoais_prof(ActionEvent event) {

    }

    @FXML
    void entra_faq(ActionEvent event) {

    }

    @FXML
    void entra_minhaturma(ActionEvent event) {

    }

}


