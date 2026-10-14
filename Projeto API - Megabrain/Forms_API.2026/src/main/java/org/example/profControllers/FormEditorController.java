
package org.example.profControllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.example.App;
import org.example.profControllers.Questao;
import org.example.SessaoApp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FormEditorController {

    @FXML
    private VBox container_dinamico;

    @FXML
    private Button btn_adicionar;

    @FXML
    private Button btn_cancelar_form;

    private static final String MULTIPLA_ESCOLHA =
            "MULTIPLA_ESCOLHA";

    private static final String CHECKLIST =
            "CHECKLIST";

    private static final String DISSERTATIVA =
            "DISSERTATIVA";

    // Guarda os controles de cada questão criada na tela.
    private final List<EditorQuestao> editores =
            new ArrayList<>();

    // Representa uma questão no editor.
    private static class EditorQuestao {

        VBox painel;
        TextField enunciado;
        ComboBox<String> tipo;
        VBox areaAlternativas;
        TextArea respostaModelo;

        List<EditorAlternativa> alternativas =
                new ArrayList<>();

        EditorQuestao(
                VBox painel,
                TextField enunciado,
                ComboBox<String> tipo,
                VBox areaAlternativas,
                TextArea respostaModelo) {

            this.painel = painel;
            this.enunciado = enunciado;
            this.tipo = tipo;
            this.areaAlternativas = areaAlternativas;
            this.respostaModelo = respostaModelo;
        }
    }

    // Representa uma alternativa dentro de uma questão.
    private static class EditorAlternativa {

        HBox painel;
        Label numero;
        TextField texto;
        CheckBox correta;

        EditorAlternativa(
                HBox painel,
                Label numero,
                TextField texto,
                CheckBox correta) {

            this.painel = painel;
            this.numero = numero;
            this.texto = texto;
            this.correta = correta;
        }
    }

    @FXML
    private void adicionarQuestao() {

        VBox painel = new VBox(8);

        painel.setStyle(
                "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 6;" +
                        "-fx-padding: 12;"
        );

        Label titulo = new Label("Nova questão");

        TextField txtPergunta = new TextField();
        txtPergunta.setPromptText("Digite sua nova pergunta...");
        txtPergunta.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cmbTipo = new ComboBox<>();

        cmbTipo.getItems().addAll(
                "Múltipla escolha",
                "Checklist numerado",
                "Dissertativa"
        );

        cmbTipo.setValue("Múltipla escolha");
        cmbTipo.setMaxWidth(Double.MAX_VALUE);

        Label tituloAlternativas =
                new Label("Alternativas / itens:");

        VBox areaAlternativas = new VBox(6);

        Button btnAdicionarAlternativa =
                new Button("Adicionar alternativa");

        Label tituloModelo =
                new Label("Resposta de referência (opcional):");

        TextArea txtRespostaModelo = new TextArea();

        txtRespostaModelo.setPromptText(
                "Digite uma resposta de referência..."
        );

        txtRespostaModelo.setWrapText(true);
        txtRespostaModelo.setPrefRowCount(3);

        EditorQuestao editor = new EditorQuestao(
                painel,
                txtPergunta,
                cmbTipo,
                areaAlternativas,
                txtRespostaModelo
        );

        // Adiciona alternativas ao clicar no botão.
        btnAdicionarAlternativa.setOnAction(event ->
                adicionarAlternativa(editor)
        );

        // Atualiza os campos quando o tipo é alterado.
        cmbTipo.setOnAction(event ->
                atualizarTipo(
                        editor,
                        tituloAlternativas,
                        btnAdicionarAlternativa,
                        tituloModelo
                )
        );

        Button btnRemover = new Button("Remover questão");

        btnRemover.setOnAction(event -> {
            container_dinamico.getChildren().remove(painel);
            editores.remove(editor);
        });

        painel.getChildren().addAll(
                titulo,
                new Label("Tipo de questão:"),
                cmbTipo,
                txtPergunta,
                tituloAlternativas,
                areaAlternativas,
                btnAdicionarAlternativa,
                tituloModelo,
                txtRespostaModelo,
                btnRemover
        );

        editores.add(editor);
        container_dinamico.getChildren().add(painel);

        atualizarTipo(
                editor,
                tituloAlternativas,
                btnAdicionarAlternativa,
                tituloModelo
        );
    }

    // Mostra somente os campos necessários para o tipo escolhido.
    private void atualizarTipo(
            EditorQuestao editor,
            Label tituloAlternativas,
            Button btnAdicionarAlternativa,
            Label tituloModelo) {

        String tipo = editor.tipo.getValue();

        boolean dissertativa =
                "Dissertativa".equals(tipo);

        editor.areaAlternativas.setVisible(!dissertativa);
        editor.areaAlternativas.setManaged(!dissertativa);

        tituloAlternativas.setVisible(!dissertativa);
        tituloAlternativas.setManaged(!dissertativa);

        btnAdicionarAlternativa.setVisible(!dissertativa);
        btnAdicionarAlternativa.setManaged(!dissertativa);

        editor.respostaModelo.setVisible(dissertativa);
        editor.respostaModelo.setManaged(dissertativa);

        tituloModelo.setVisible(dissertativa);
        tituloModelo.setManaged(dissertativa);
    }

    // Cria uma alternativa com um campo e uma caixa de seleção.
    private void adicionarAlternativa(EditorQuestao editor) {

        Label numero = new Label();

        TextField txtAlternativa = new TextField();
        txtAlternativa.setPromptText("Digite alternativa");
        txtAlternativa.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(
                txtAlternativa,
                Priority.ALWAYS
        );

        CheckBox chkCorreta = new CheckBox("Correta");

        HBox linha = new HBox(8);
        linha.setAlignment(Pos.CENTER_LEFT);

        Button btnRemover = new Button("Remover");

        EditorAlternativa alternativa =
                new EditorAlternativa(
                        linha,
                        numero,
                        txtAlternativa,
                        chkCorreta
                );

        btnRemover.setOnAction(event -> {

            editor.areaAlternativas.getChildren().remove(linha);
            editor.alternativas.remove(alternativa);

            atualizarNumeracao(editor);
        });

        linha.getChildren().addAll(
                numero,
                txtAlternativa,
                chkCorreta,
                btnRemover
        );

        editor.alternativas.add(alternativa);
        editor.areaAlternativas.getChildren().add(linha);

        atualizarNumeracao(editor);
    }

    // Numera as alternativas em sequência.
    private void atualizarNumeracao(EditorQuestao editor) {

        for (int i = 0; i < editor.alternativas.size(); i++) {

            EditorAlternativa alternativa =
                    editor.alternativas.get(i);

            alternativa.numero.setText((i + 1) + ".");
        }
    }

    @FXML
    private void GeraJson(ActionEvent event) {

        if (SessaoApp.provaAtual == null) {
            mostrarErro("Nenhuma prova foi inicializada.");
            return;
        }

        if (editores.isEmpty()) {
            mostrarErro("Adicione pelo menos uma questão.");
            return;
        }

        // Primeiro monta e valida tudo em memória.
        List<Questao> questoesNovas = new ArrayList<>();

        for (int i = 0; i < editores.size(); i++) {

            EditorQuestao editor = editores.get(i);

            String enunciado =
                    editor.enunciado.getText().trim();

            if (enunciado.isEmpty()) {
                mostrarErro(
                        "Preencha o enunciado da questão "
                                + (i + 1) + "."
                );
                return;
            }

            Questao questao = new Questao();

            questao.setEnunciado(enunciado);

            String tipoSelecionado = editor.tipo.getValue();

            if ("Múltipla escolha".equals(tipoSelecionado)) {

                questao.setTipo(MULTIPLA_ESCOLHA);

            } else if ("Checklist numerado".equals(tipoSelecionado)) {

                questao.setTipo(CHECKLIST);

            } else {

                questao.setTipo(DISSERTATIVA);
            }

            // Questões objetivas precisam de alternativas.
            if (!DISSERTATIVA.equals(questao.getTipo())) {

                if (editor.alternativas.isEmpty()) {
                    mostrarErro(
                            "Adicione alternativas à questão "
                                    + (i + 1) + "."
                    );
                    return;
                }

                int quantidadeCorretas = 0;

                for (int j = 0;
                     j < editor.alternativas.size();
                     j++) {

                    EditorAlternativa alternativa =
                            editor.alternativas.get(j);

                    String texto =
                            alternativa.texto.getText().trim();

                    if (texto.isEmpty()) {
                        mostrarErro(
                                "Preencha todas as alternativas da questão "
                                        + (i + 1) + "."
                        );
                        return;
                    }

                    questao.getAlternativas().add(texto);

                    if (alternativa.correta.isSelected()) {

                        // O índice corresponde à posição na lista.
                        questao.getRespostasCorretas().add(j);

                        quantidadeCorretas++;
                    }
                }

                if (quantidadeCorretas == 0) {
                    mostrarErro(
                            "Marque pelo menos uma alternativa correta " +
                                    "na questão " + (i + 1) + "."
                    );
                    return;
                }

                // Múltipla escolha permite apenas uma correta.
                if (MULTIPLA_ESCOLHA.equals(questao.getTipo())
                        && quantidadeCorretas != 1) {

                    mostrarErro(
                            "A questão de múltipla escolha "
                                    + (i + 1)
                                    + " deve ter exatamente uma correta."
                    );
                    return;
                }

            } else {

                // A dissertativa não precisa de alternativas.
                questao.setRespostaModelo(
                        editor.respostaModelo.getText().trim()
                );
            }

            questoesNovas.add(questao);
        }

        // Salva a prova somente depois de validar todas as questões.
        try {

            SessaoApp.provaAtual.getQuestoes().clear();

            SessaoApp.provaAtual.getQuestoes().addAll(
                    questoesNovas
            );

            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            String json = gson.toJson(
                    SessaoApp.provaAtual
            );

            String nome = SessaoApp.provaAtual.getNome();

            if (nome == null || nome.trim().isEmpty()) {
                nome = "Prova_Sem_Nome";
            }

            // Evita caracteres inválidos no nome do arquivo.
            nome = nome.replaceAll(
                    "[^a-zA-Z0-9._-]",
                    "_"
            );

            Path pasta = Path.of("provas_salvas");

            Files.createDirectories(pasta);

            Path arquivo = pasta.resolve(nome + ".json");

            Files.writeString(arquivo, json);

            Alert alerta = new Alert(
                    Alert.AlertType.INFORMATION
            );

            alerta.setTitle("Sucesso");
            alerta.setHeaderText("Prova salva com sucesso!");

            alerta.setContentText(
                    "Arquivo salvo em:\n"
                            + arquivo.toAbsolutePath()
            );

            alerta.showAndWait();

            App.setRoot("professorFluxo/ListaProva");

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro(
                    "Não foi possível salvar a prova:\n"
                            + e.getClass().getSimpleName()
                            + ": "
                            + e.getMessage()
            );
        }
    }

    private void mostrarErro(String mensagem) {

        Alert alerta = new Alert(
                Alert.AlertType.ERROR
        );

        alerta.setTitle("Erro");
        alerta.setHeaderText("Verifique a prova");
        alerta.setContentText(mensagem);

        alerta.showAndWait();
    }

    @FXML
    private void cancelaFormulario(ActionEvent event)
            throws IOException {

        App.setRoot("professorFluxo/ListaProva");
    }
}
