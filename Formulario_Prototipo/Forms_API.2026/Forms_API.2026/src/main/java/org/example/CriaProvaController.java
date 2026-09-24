package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.io.IOException;

public class CriaProvaController {

    @FXML
    private Button confirmCriarProva;

    @FXML
    private TextField nomeProva;

    @FXML
    private TextField prazoEntrega;

    @FXML
    private ComboBox<?> selectDisciplina;

    @FXML
    private ComboBox<?> selectQuestoes;

    @FXML
    void confirmCriarProva(ActionEvent event) throws IOException {
        SessaoApp.provaAtual = new Prova();

        SessaoApp.provaAtual.setNome(nomeProva.getText());
        SessaoApp.provaAtual.setPrazo(prazoEntrega.getText());

        App.setRoot("FormEditor");
    }

}