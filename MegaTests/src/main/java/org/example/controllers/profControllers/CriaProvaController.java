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
            mostrarErro("Disciplina não selecionada", "Selecione uma disciplina para a prova.");
            return;
        }

        if (nomeProva.getText() == null || nomeProva.getText().trim().isEmpty()) {
            mostrarErro("Nome da prova não preenchido", "Informe o nome da prova.");
            return;
        }

        int minutos = 0;
        try {
            minutos = Integer.parseInt(tempoMinutos.getText().trim());
            if (minutos <= 0) {
                mostrarErro("Duração inválida", "O tempo da prova deve ser maior que zero.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarErro("Duração inválida", "Informe o tempo da prova em minutos utilizando apenas números.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
                .withResolverStyle(java.time.format.ResolverStyle.STRICT);
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime dataInicialParsed = null;
        LocalDateTime dataLimiteParsed = null;

        String strDataIni = dataInicio.getText() == null ? "" : dataInicio.getText().trim();
        String strHoraIni = horarioInicio.getText() == null ? "" : horarioInicio.getText().trim();
        String strDataFim = dataFim.getText() == null ? "" : dataFim.getText().trim();
        String strHoraFim = horarioFim.getText() == null ? "" : horarioFim.getText().trim();

        // Valida se preencheu apenas data ou apenas hora
        if (strDataIni.isEmpty() != strHoraIni.isEmpty()) {
            mostrarErro("Início incompleto", "Preencha tanto a data quanto o horário inicial, ou deixe ambos em branco.");
            return;
        }
        if (strDataFim.isEmpty() != strHoraFim.isEmpty()) {
            mostrarErro("Prazo final incompleto", "Preencha tanto a data quanto o horário limite, ou deixe ambos em branco.");
            return;
        }

        try {
            if (!strDataIni.isEmpty()) {
                dataInicialParsed = LocalDateTime.parse(strDataIni + " " + strHoraIni, formatter);
                if (dataInicialParsed.isBefore(agora)) {
                    mostrarErro("Data inicial inválida", "A data e o horário inicial não podem estar no passado.");
                    return;
                }
            }

            if (!strDataFim.isEmpty()) {
                dataLimiteParsed = LocalDateTime.parse(strDataFim + " " + strHoraFim, formatter);
                if (dataLimiteParsed.isBefore(agora)) {
                    mostrarErro("Data limite inválida", "A data e o horário limite não podem estar no passado.");
                    return;
                }
            }

            if (dataInicialParsed != null && dataLimiteParsed != null && !dataLimiteParsed.isAfter(dataInicialParsed)) {
                mostrarErro("Intervalo inválido", "A data e o horário limite devem ser posteriores ao início da prova.");
                return;
            }
        } catch (Exception e) {
            mostrarErro("Formato de data/horário inválido", "Verifique se a data existe e use o formato DD/MM/AAAA e HH:MM.");
            return;
        }

        SessaoApp.provaAtual = new Prova();
        SessaoApp.provaAtual.setNome(nomeProva.getText().trim());
        SessaoApp.provaAtual.setDisciplina(disciplinaEscolhida);
        SessaoApp.provaAtual.setDuracaoMinutos(minutos);
        SessaoApp.provaAtual.setDataInicial(dataInicialParsed);
        SessaoApp.provaAtual.setDataLimite(dataLimiteParsed);

        App.setRoot("professorFluxo/FormEditor");
    }

    private void mostrarErro(String cabecalho, String conteudo) {
        Alert erro = new Alert(Alert.AlertType.ERROR);
        erro.setTitle("Erro");
        erro.setHeaderText(cabecalho);
        erro.setContentText(conteudo);
        erro.showAndWait();
    }

    @FXML
    void voltarCriaProva(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/ListaProva");
    }
}