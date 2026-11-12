package org.example.controllers.profControllers;

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
import org.example.dao.ProvaDAO;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.Questao;
import org.example.entity.profEntity.SessaoApp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Editor dinâmico que permite criar provas com tipos de questão mistos. */
public class FormEditorController {

    @FXML
    private VBox container_dinamico;

    @FXML
    private Button btn_adicionar;

    @FXML
    private Button btn_cancelar_form;

    private final List<EditorQuestao> editores = new ArrayList<>();

    private static class EditorQuestao {
        private final VBox painel;
        private final TextField enunciado;
        private final TextField pontos;
        private final ComboBox<String> tipo;
        private final VBox areaAlternativas;
        private final Label tituloAlternativas;
        private final Button btnAdicionarAlternativa;
        private final Label tituloModelo;
        private final TextArea respostaModelo;
        private final List<EditorAlternativa> alternativas = new ArrayList<>();
        private final VBox areaCorrespondencia;
        private final VBox areaItensCorrespondencia;
        private final VBox areaOpcoesCorrespondencia;
        private final List<TextField> itensCorrespondencia = new ArrayList<>();
        private final List<EditorCorrespondenciaOpcao> opcoesCorrespondencia = new ArrayList<>();

        private EditorQuestao(
                VBox painel,
                TextField enunciado,
                TextField pontos,
                ComboBox<String> tipo,
                VBox areaAlternativas,
                Label tituloAlternativas,
                Button btnAdicionarAlternativa,
                VBox areaCorrespondencia,
                VBox areaItensCorrespondencia,
                VBox areaOpcoesCorrespondencia,
                Label tituloModelo,
                TextArea respostaModelo){
            this.painel = painel;
            this.enunciado = enunciado;
            this.pontos = pontos;
            this.tipo = tipo;
            this.areaAlternativas = areaAlternativas;
            this.tituloAlternativas = tituloAlternativas;
            this.btnAdicionarAlternativa = btnAdicionarAlternativa;
            this.tituloModelo = tituloModelo;
            this.respostaModelo = respostaModelo;
            this.areaCorrespondencia = areaCorrespondencia;
            this.areaItensCorrespondencia = areaItensCorrespondencia;
            this.areaOpcoesCorrespondencia = areaOpcoesCorrespondencia;
        }
    }

    private static class EditorCorrespondenciaOpcao {

        private final TextField texto;
        private final ComboBox<Integer> numeroCorreto;

        private EditorCorrespondenciaOpcao(
                TextField texto,
                ComboBox<Integer> numeroCorreto
        ) {
            this.texto = texto;
            this.numeroCorreto = numeroCorreto;
        }
    }

    private static class EditorAlternativa {
        private final HBox painel;
        private final Label numero;
        private final TextField texto;
        private final CheckBox correta;

        private EditorAlternativa(HBox painel, Label numero,
                                  TextField texto, CheckBox correta) {
            this.painel = painel;
            this.numero = numero;
            this.texto = texto;
            this.correta = correta;
        }
    }

    @FXML
    private void adicionarQuestao() {
        VBox painel = new VBox(8);
        painel.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 6; -fx-padding: 12;");

        Label titulo = new Label("Nova questão " + (editores.size() + 1));

        TextField txtPergunta = new TextField();
        txtPergunta.setPromptText("Digite o enunciado da questão...");
        txtPergunta.setMaxWidth(Double.MAX_VALUE);

        // Campo de pontuação inicia vazio usando promptText (marca d'água opaca)
        TextField txtPontos = new TextField();
        txtPontos.setPromptText("Ex: 1.0 (Padrão)");
        txtPontos.setMaxWidth(180);

        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll(
                "Múltipla escolha" ,
                "Checklist numerado",
                "Dissertativa" ,
                "Correspondência"
        );
        cmbTipo.setValue("Múltipla escolha");
        cmbTipo.setMaxWidth(Double.MAX_VALUE);

        Label tituloAlternativas = new Label("Alternativas / itens:");
        VBox areaAlternativas = new VBox(6);

        Button btnAdicionarAlternativa = new Button("Adicionar alternativa");

        Label tituloModelo = new Label("Resposta de referência (opcional):");
        TextArea txtRespostaModelo = new TextArea();
        txtRespostaModelo.setPromptText("Digite uma resposta de referência...");
        txtRespostaModelo.setWrapText(true);
        txtRespostaModelo.setPrefRowCount(3);

        VBox areaCorrespondencia = new VBox(10);

        Label tituloItens = new Label("Itens:");
        VBox areaItensCorrespondencia = new VBox(6);
        Button btnAdicionarItem = new Button("Adicionar item");

        Label tituloOpcoes = new Label("Alternativas:");
        VBox areaOpcoesCorrespondencia = new VBox(6);
        Button btnAdicionarOpcao = new Button("Adicionar Opção");

        EditorQuestao editor = new EditorQuestao(
                painel, txtPergunta, txtPontos, cmbTipo, areaAlternativas,
                tituloAlternativas, btnAdicionarAlternativa, areaCorrespondencia, areaItensCorrespondencia,
                areaOpcoesCorrespondencia, tituloModelo, txtRespostaModelo
        );

        btnAdicionarItem.setOnAction(
                event -> adicionarItemCorrespondencia(editor)
        );

        btnAdicionarOpcao.setOnAction(
                event -> adicionarOpcaoCorrespondencia(editor)
        );
        btnAdicionarAlternativa.setOnAction(event -> adicionarAlternativa(editor));
        cmbTipo.setOnAction(event -> atualizarTipo(editor));

        Button btnRemover = new Button("Remover questão");
        btnRemover.setOnAction(event -> {
            container_dinamico.getChildren().remove(painel);
            editores.remove(editor);
            atualizarTitulosQuestoes();
        });

        areaCorrespondencia.getChildren().addAll(
                tituloItens,
                areaItensCorrespondencia,
                btnAdicionarItem,
                tituloOpcoes,
                areaOpcoesCorrespondencia,
                btnAdicionarOpcao
        );

        painel.getChildren().addAll(
                titulo,
                new Label("Tipo de questão:"),
                cmbTipo,
                new Label("Pontuação / Valor da questão:"),
                txtPontos,
                new Label("Enunciado:"),
                txtPergunta,
                tituloAlternativas,
                areaAlternativas,
                btnAdicionarAlternativa,
                areaCorrespondencia,
                tituloModelo,
                txtRespostaModelo,
                btnRemover
        );

        editores.add(editor);
        container_dinamico.getChildren().add(painel);
        atualizarTipo(editor);
    }

    private void atualizarTipo(EditorQuestao editor) {
        String tipo = editor.tipo.getValue();

        boolean dissertativa = "Dissertativa".equals(tipo);
        boolean correspondencia = "Correspondência".equals(tipo);

        // Visível apenas para Múltipla Escolha ou Checklist numerado
        boolean objetivaOuChecklist = !dissertativa && !correspondencia;

        // Controle das alternativas comuns (Múltipla escolha / Checklist)
        editor.areaAlternativas.setVisible(objetivaOuChecklist);
        editor.areaAlternativas.setManaged(objetivaOuChecklist);
        editor.tituloAlternativas.setVisible(objetivaOuChecklist);
        editor.tituloAlternativas.setManaged(objetivaOuChecklist);
        editor.btnAdicionarAlternativa.setVisible(objetivaOuChecklist);
        editor.btnAdicionarAlternativa.setManaged(objetivaOuChecklist);

        // Controle da resposta (Dissertativa)
        editor.respostaModelo.setVisible(dissertativa);
        editor.respostaModelo.setManaged(dissertativa);
        editor.tituloModelo.setVisible(dissertativa);
        editor.tituloModelo.setManaged(dissertativa);

        // Controle da área de correspondência (Correspondência)
        editor.areaCorrespondencia.setVisible(correspondencia);
        editor.areaCorrespondencia.setManaged(correspondencia);
    }

    private void atualizarComboCorrespondencia(EditorQuestao editor) {
        int quantidadeItens = editor.itensCorrespondencia.size();

        for (EditorCorrespondenciaOpcao opcao : editor.opcoesCorrespondencia) {
            Integer selecionado = opcao.numeroCorreto.getValue();
            opcao.numeroCorreto.getItems().clear();

            for (int i = 1; i <= quantidadeItens; i++) {
                opcao.numeroCorreto.getItems().add(i);
            }

            if (selecionado != null && selecionado <= quantidadeItens) {
                opcao.numeroCorreto.setValue(selecionado);
            }
        }
    }

    private void atualizarNumerosCorrespondencia(EditorQuestao editor) {
        for (int i = 0; i < editor.areaItensCorrespondencia.getChildren().size(); i++) {
            HBox linha = (HBox) editor.areaItensCorrespondencia.getChildren().get(i);
            Label numero = (Label) linha.getChildren().get(0);
            numero.setText((i + 1) + ".");
        }
    }

    private void adicionarAlternativa(EditorQuestao editor) {
        Label numero = new Label();
        TextField txtAlternativa = new TextField();
        txtAlternativa.setPromptText("Digite a alternativa / item");
        txtAlternativa.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(txtAlternativa, Priority.ALWAYS);

        CheckBox chkCorreta = new CheckBox("Correta");
        HBox linha = new HBox(8);
        linha.setAlignment(Pos.CENTER_LEFT);

        EditorAlternativa alternativa = new EditorAlternativa(linha, numero, txtAlternativa, chkCorreta);
        Button btnRemover = new Button("Remover");
        btnRemover.setOnAction(event -> {
            editor.areaAlternativas.getChildren().remove(linha);
            editor.alternativas.remove(alternativa);
            atualizarNumeracao(editor);
        });

        linha.getChildren().addAll(numero, txtAlternativa, chkCorreta, btnRemover);
        editor.alternativas.add(alternativa);
        editor.areaAlternativas.getChildren().add(linha);
        atualizarNumeracao(editor);
    }

    private void adicionarOpcaoCorrespondencia(EditorQuestao editor) {
        HBox linha = new HBox(8);
        linha.setAlignment(Pos.CENTER_LEFT);

        ComboBox<Integer> numero = new ComboBox<>();
        TextField texto = new TextField();
        texto.setPromptText("Digite a alternativa...");
        texto.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(texto, Priority.ALWAYS);

        EditorCorrespondenciaOpcao opcao = new EditorCorrespondenciaOpcao(texto, numero);

        Button remover = new Button("Remover");
        remover.setOnAction(event -> {
            editor.areaOpcoesCorrespondencia.getChildren().remove(linha);
            editor.opcoesCorrespondencia.remove(opcao);
        });

        linha.getChildren().addAll(numero, texto, remover);
        editor.opcoesCorrespondencia.add(opcao);
        editor.areaOpcoesCorrespondencia.getChildren().add(linha);

        atualizarComboCorrespondencia(editor);
    }

    private void adicionarItemCorrespondencia(EditorQuestao editor) {
        HBox linha = new HBox(8);
        linha.setAlignment(Pos.CENTER_LEFT);

        Label numero = new Label((editor.itensCorrespondencia.size() + 1) + ".");

        TextField campo = new TextField();
        campo.setPromptText("Digite o item...");
        campo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(campo, Priority.ALWAYS);

        Button remover = new Button("Remover");
        remover.setOnAction(event -> {
            editor.areaItensCorrespondencia.getChildren().remove(linha);
            editor.itensCorrespondencia.remove(campo);
            atualizarNumerosCorrespondencia(editor);
            atualizarComboCorrespondencia(editor);
        });

        linha.getChildren().addAll(numero, campo, remover);
        editor.itensCorrespondencia.add(campo);
        editor.areaItensCorrespondencia.getChildren().add(linha);

        atualizarComboCorrespondencia(editor);
    }

    private void atualizarNumeracao(EditorQuestao editor) {
        for (int i = 0; i < editor.alternativas.size(); i++) {
            editor.alternativas.get(i).numero.setText((i + 1) + ".");
        }
    }

    private void atualizarTitulosQuestoes() {
        for (int i = 0; i < editores.size(); i++) {
            for (javafx.scene.Node node : editores.get(i).painel.getChildren()) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    if (label.getText().startsWith("Nova questão ")) {
                        label.setText("Nova questão " + (i + 1));
                        break;
                    }
                }
            }
        }
    }

    @FXML
    private void GeraProva(ActionEvent event){
        Prova prova = SessaoApp.provaAtual;
        if (prova == null) {
            mostrarErro("Nenhuma prova foi inicializada. Volte e crie a prova novamente.");
            return;
        }
        if (editores.isEmpty()) {
            mostrarErro("Adicione pelo menos uma questão.");
            return;
        }

        List<Questao> questoesNovas = new ArrayList<>();
        boolean temMultiplaEscolha = false;
        boolean temChecklist = false;
        boolean temDissertativa = false;
        boolean temCorrespondencia = false;

        for (int i = 0; i < editores.size(); i++) {
            EditorQuestao editor = editores.get(i);
            String enunciado = editor.enunciado.getText() == null
                    ? "" : editor.enunciado.getText().trim();
            if (enunciado.isEmpty()) {
                mostrarErro("Preencha o enunciado da questão " + (i + 1) + ".");
                return;
            }

            // Se o campo estiver vazio, mantém o valor padrão de 1.0 ponto
            double pontos = 1.0;
            String txtP = editor.pontos.getText() == null ? "" : editor.pontos.getText().trim().replace(",", ".");

            if (!txtP.isEmpty()) {
                try {
                    pontos = Double.parseDouble(txtP);
                    if (pontos <= 0) {
                        mostrarErro("A pontuação da questão " + (i + 1) + " deve ser maior que zero.");
                        return;
                    }
                    if (pontos > 9.99) {
                        mostrarErro("A pontuação da questão " + (i + 1) + " não pode ser maior que 9.99.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarErro("Informe um valor numérico válido para a pontuação da questão " + (i + 1) + " (ex: 1.5 ou 2.50).");
                    return;
                }
            }

            Questao questao = new Questao();
            questao.setEnunciado(enunciado);
            questao.setPontos(pontos);
            String tipoSelecionado = editor.tipo.getValue();

            if ("Múltipla escolha".equals(tipoSelecionado)) {
                questao.setTipo(Questao.MULTIPLA_ESCOLHA);
                temMultiplaEscolha = true;
            } else if ("Checklist numerado".equals(tipoSelecionado)) {
                questao.setTipo(Questao.CHECKLIST);
                temChecklist = true;
            } else if ("Dissertativa".equals(tipoSelecionado)) {
                questao.setTipo(Questao.DISSERTATIVA);
                temDissertativa = true;
            } else if ("Correspondência".equals(tipoSelecionado)) {
                questao.setTipo(Questao.CORRESPONDENCIA);
                temCorrespondencia = true;
            }

            // Dissertativa
            if (Questao.DISSERTATIVA.equals(questao.getTipo())) {
                String modelo = editor.respostaModelo.getText();
                questao.setRespostaModelo(modelo == null ? "" : modelo.trim());

            }
            // Correspondência
            else if (Questao.CORRESPONDENCIA.equals(questao.getTipo())) {
                if (editor.itensCorrespondencia.size() < 2) {
                    mostrarErro("A questão de correspondência " + (i + 1) + " precisa ter pelo menos dois itens.");
                    return;
                }
                if (editor.opcoesCorrespondencia.size() < 2) {
                    mostrarErro("A questão de correspondência " + (i + 1) + " precisa ter pelo menos duas opções.");
                    return;
                }

                for (TextField campoItem : editor.itensCorrespondencia) {
                    String textoItem = campoItem.getText() == null ? "" : campoItem.getText().trim();
                    if (textoItem.isEmpty()) {
                        mostrarErro("Preencha todos os textos dos itens de correspondência na questão " + (i + 1) + ".");
                        return;
                    }
                    questao.getItensCorrespondencia().add(textoItem);
                }

                for (EditorCorrespondenciaOpcao opcao : editor.opcoesCorrespondencia) {
                    String textoOpcao = opcao.texto.getText() == null ? "" : opcao.texto.getText().trim();
                    Integer numCorreto = opcao.numeroCorreto.getValue();

                    if (textoOpcao.isEmpty()) {
                        mostrarErro("Preencha o texto de todas as opções de correspondência na questão " + (i + 1) + ".");
                        return;
                    }
                    if (numCorreto == null) {
                        mostrarErro("Selecione o número correspondente correto para todas as opções na questão " + (i + 1) + ".");
                        return;
                    }

                    questao.getAlternativasCorrespondencia().add(textoOpcao);
                    questao.getGabaritoCorrespondencia().add(numCorreto);
                }

            }
            // Múltipla escolha ou Checklist
            else {
                if (editor.alternativas.size() < 2) {
                    mostrarErro("A questão " + (i + 1) + " precisa ter pelo menos duas alternativas.");
                    return;
                }

                int quantidadeCorretas = 0;
                for (int j = 0; j < editor.alternativas.size(); j++) {
                    EditorAlternativa alternativa = editor.alternativas.get(j);
                    String texto = alternativa.texto.getText() == null
                            ? "" : alternativa.texto.getText().trim();
                    if (texto.isEmpty()) {
                        mostrarErro("Preencha todas as alternativas da questão " + (i + 1) + ".");
                        return;
                    }
                    questao.getAlternativas().add(texto);
                    if (alternativa.correta.isSelected()) {
                        questao.getRespostasCorretas().add(j);
                        quantidadeCorretas++;
                    }
                }

                if (quantidadeCorretas == 0) {
                    mostrarErro("Marque pelo menos uma alternativa correta na questão " + (i + 1) + ".");
                    return;
                }
                if (Questao.MULTIPLA_ESCOLHA.equals(questao.getTipo()) && quantidadeCorretas != 1) {
                    mostrarErro("A questão de múltipla escolha " + (i + 1) + " deve ter exatamente uma alternativa correta.");
                    return;
                }
            }

            questoesNovas.add(questao);
        }

        prova.getQuestoes().clear();
        prova.getQuestoes().addAll(questoesNovas);

        int tiposUsados = (temMultiplaEscolha ? 1 : 0)
                + (temChecklist ? 1 : 0) + (temDissertativa ? 1 : 0) + (temCorrespondencia ? 1 : 0);

        prova.setTipoQuestoes(tiposUsados > 1 ? "Mista"
                : temMultiplaEscolha ? "Múltipla escolha"
                : temChecklist ? "Checklist numerado"
                : temCorrespondencia ? "Correspondência" : "Dissertativa");

        try {
            new ProvaDAO().salvaProvaCompleta(prova);

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Sucesso");
            alerta.setHeaderText("Prova salva com sucesso!");
            alerta.setContentText("A prova, suas questões, alternativas e gabaritos foram gravados com sucesso no banco de dados.");
            alerta.showAndWait();

            App.setRoot("professorFluxo/ListaProva");
        } catch(Exception e){
            e.printStackTrace();
            mostrarErro("Não foi possível salvar a prova no banco de dados. "
                    + "Confira a conexão, a disciplina selecionada e a sessão do professor.\n"
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void mostrarErro(String mensagem) {
        Alert erro = new Alert(Alert.AlertType.ERROR);
        erro.setTitle("Erro");
        erro.setHeaderText("Não foi possível concluir a operação");
        erro.setContentText(mensagem);
        erro.showAndWait();
    }

    @FXML
    private void cancelaFormulario(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/ListaProva");
    }
}