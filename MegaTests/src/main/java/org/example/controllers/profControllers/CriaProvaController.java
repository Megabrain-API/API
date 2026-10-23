package org.example.controllers.profControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.example.App;
import org.example.dao.DisciplinaDAO;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.SessaoApp;

import java.io.IOException;
import java.util.List;

public class CriaProvaController {

    @FXML
    private Button confirmCriarProva;

    @FXML
    private Button voltarCriaProva;

    @FXML
    private TextField nomeProva;

    @FXML
    private TextField prazoEntrega;

    @FXML
    private ComboBox<String> selectDisciplina;

    @FXML
    private ComboBox<?> selectQuestoes;

    @FXML
    public void initialize() {
        CarregaCombobox();
    }

    public void CarregaCombobox(){
        DisciplinaDAO dao = new DisciplinaDAO();
        List<String> listaNomes = dao.listarNomesDisciplinas();

        // Converte a lista normal do Java para uma ObservableList (que o JavaFX aceita)
        ObservableList<String> dadosObservable = FXCollections.observableArrayList(listaNomes);

        // Joga os dados para dentro do ComboBox
        selectDisciplina.setItems(dadosObservable);
    }

    @FXML
    void confirmCriarProva(ActionEvent event) throws IOException {
        SessaoApp.provaAtual = new Prova();

        String disciplinaEscolhida = selectDisciplina.getValue();
        if (disciplinaEscolhida == null || disciplinaEscolhida.isEmpty()) {
            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setTitle("Erro");
            erro.setHeaderText("Disciplina não selecionada");
        }

        SessaoApp.provaAtual.setNome(nomeProva.getText());
        SessaoApp.provaAtual.setPrazo(prazoEntrega.getText());
        SessaoApp.provaAtual.setDisciplina(disciplinaEscolhida);

        App.setRoot("professorFluxo/FormEditor");
    }
    @FXML
    void voltar_criaProva(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/ListaProva");
    }

}