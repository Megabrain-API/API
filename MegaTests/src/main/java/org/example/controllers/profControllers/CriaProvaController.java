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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CriaProvaController {

    @FXML
    private Button confirmCriarProva;

    @FXML
    private Button voltarCriaProva;

    @FXML
    private TextField nomeProva;

    @FXML
    private TextField dataInicio;

    @FXML
    private TextField dataFim;

    @FXML
    private TextField horarioInicio;

    @FXML
    private TextField horarioFim;

    @FXML
    private TextField tempoMinutos;

    @FXML
    private ComboBox<String> selectDisciplina;

    @FXML
    private ComboBox<?> selectQuestoes;

    @FXML
    public void initialize() {
        CarregaCombobox();
        aplicarMascaraData(dataInicio);
        aplicarMascaraData(dataFim);
        aplicarMascaraHora(horarioInicio);
        aplicarMascaraHora(horarioFim);
        aplicarMascaraApenasNumeros(tempoMinutos);
    }

    public void CarregaCombobox() {
        DisciplinaDAO dao = new DisciplinaDAO();
        List<String> listaNomes = dao.listarNomesDisciplinas();
        ObservableList<String> dadosObservable = FXCollections.observableArrayList(listaNomes);
        selectDisciplina.setItems(dadosObservable);
    }

    private void aplicarMascaraData(TextField textField) {
        if (textField == null) return;
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            String apenasNumeros = newValue.replaceAll("[^\\d]", "");
            if (apenasNumeros.length() > 8) {
                apenasNumeros = apenasNumeros.substring(0, 8);
            }

            StringBuilder formatado = new StringBuilder();
            for (int i = 0; i < apenasNumeros.length(); i++) {
                if (i == 2 || i == 4) {
                    formatado.append("/");
                }
                formatado.append(apenasNumeros.charAt(i));
            }

            if (!newValue.equals(formatado.toString())) {
                textField.setText(formatado.toString());
                textField.positionCaret(formatado.length());
            }
        });
    }

    private void aplicarMascaraHora(TextField textField) {
        if (textField == null) return;
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            String apenasNumeros = newValue.replaceAll("[^\\d]", "");
            if (apenasNumeros.length() > 4) {
                apenasNumeros = apenasNumeros.substring(0, 4);
            }

            StringBuilder formatado = new StringBuilder();
            for (int i = 0; i < apenasNumeros.length(); i++) {
                if (i == 2) {
                    formatado.append(":");
                }
                formatado.append(apenasNumeros.charAt(i));
            }

            if (!newValue.equals(formatado.toString())) {
                textField.setText(formatado.toString());
                textField.positionCaret(formatado.length());
            }
        });
    }

    private void aplicarMascaraApenasNumeros(TextField textField) {
        if (textField == null) return;
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    void confirmCriarProva(ActionEvent event) throws IOException {
        String disciplinaEscolhida = selectDisciplina.getValue();
        if (disciplinaEscolhida == null || disciplinaEscolhida.isEmpty()) {
            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setTitle("Erro");
            erro.setHeaderText("Disciplina não selecionada");
            erro.showAndWait();
            return;
        }

        if (nomeProva.getText() == null || nomeProva.getText().trim().isEmpty()) {
            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setTitle("Erro");
            erro.setHeaderText("Nome da prova não preenchido");
            erro.showAndWait();
            return;
        }

        int minutos = 0;
        try {
            minutos = Integer.parseInt(tempoMinutos.getText().trim());
        } catch (NumberFormatException e) {
            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setTitle("Erro");
            erro.setHeaderText("Duração inválida");
            erro.setContentText("Informe o tempo da prova em minutos utilizando apenas números.");
            erro.showAndWait();
            return;
        }

        SessaoApp.provaAtual = new Prova();
        SessaoApp.provaAtual.setNome(nomeProva.getText());
        SessaoApp.provaAtual.setDisciplina(disciplinaEscolhida);
        SessaoApp.provaAtual.setDuracaoMinutos(minutos);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try {
            if (dataInicio.getText() != null && !dataInicio.getText().trim().isEmpty() &&
                    horarioInicio.getText() != null && !horarioInicio.getText().trim().isEmpty()) {

                String dataHoraIni = dataInicio.getText().trim() + " " + horarioInicio.getText().trim();
                SessaoApp.provaAtual.setDataInicial(LocalDateTime.parse(dataHoraIni, formatter));
            }

            if (dataFim.getText() != null && !dataFim.getText().trim().isEmpty() &&
                    horarioFim.getText() != null && !horarioFim.getText().trim().isEmpty()) {

                String dataHoraFim = dataFim.getText().trim() + " " + horarioFim.getText().trim();
                SessaoApp.provaAtual.setDataLimite(LocalDateTime.parse(dataHoraFim, formatter));
            }
        } catch (Exception e) {
            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setTitle("Erro");
            erro.setHeaderText("Formato de data/horário inválido");
            erro.setContentText("Use o formato DD/MM/AAAA para datas e HH:MM para horários.");
            erro.showAndWait();
            return;
        }

        App.setRoot("professorFluxo/FormEditor");
    }

    @FXML
    void voltarCriaProva(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/ListaProva");
    }
}