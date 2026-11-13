package org.example.controllers.profControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class TelaCorrecaoController {

    @FXML
    private Button btnFinalizar;

    @FXML
    private Button btnSalvar;

    @FXML
    private Label lblNomeAluno;

    @FXML
    private Label lblNomeProva;

    @FXML
    private Label lblNotaTotal;

    @FXML
    private Label lblQuestoesCorrigidas;

    @FXML
    private Label lblRaAluno;

    @FXML
    private TextField txtNotaExemplo;

    @FXML
    private TextArea txtObservacaoExemplo;

    @FXML
    private TextArea txtRespostaExemplo;

    @FXML
    private VBox vboxQuestoes;

}
