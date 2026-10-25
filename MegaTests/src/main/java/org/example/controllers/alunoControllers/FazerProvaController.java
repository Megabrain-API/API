package org.example.controllers.alunoControllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.example.App;
import org.example.controllers.profControllers.SessaoUsuarioController;
import org.example.dao.AlunoDAO;
import org.example.dao.ProvaDAO;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.Questao;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class FazerProvaController implements Initializable {

    @FXML
    private VBox barra_lateral;

    @FXML
    private Button dados_pessoais_alu;

    @FXML
    private Button menu_meucurso;

    @FXML
    private Button menu_sair_aluno;

    @FXML
    private Button minhas_provas_aluno;

    @FXML
    private VBox vb_fazerprova_container;

    private final Map<Button, String> textosOriginais = new HashMap<>();
    private final List<RespostaQuestao> respostas = new ArrayList<>();
    private Button[] botoesMenu;
    private Prova provaAtual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        botoesMenu = new Button[]{menu_meucurso, minhas_provas_aluno, dados_pessoais_alu, menu_sair_aluno};

        for (Button btn : botoesMenu) {
            if (btn != null) {
                textosOriginais.put(btn, btn.getText());
            }
        }

        ocultarTextosBotoes();
        carregarProvaSelecionada();
    }

    private void carregarProvaSelecionada() {
        if (vb_fazerprova_container == null) {
            return;
        }

        vb_fazerprova_container.getChildren().clear();
        vb_fazerprova_container.setAlignment(Pos.TOP_LEFT);
        vb_fazerprova_container.setSpacing(14);
        vb_fazerprova_container.setPadding(new Insets(18));
        respostas.clear();

        Prova provaSelecionada = ProvasAlunoController.getProvaSelecionadaParaAbertura();

        if (provaSelecionada == null) {
            mostrarMensagemNaTela("Nenhuma prova foi selecionada.");
            return;
        }

        try {
            provaAtual = new ProvaDAO().buscarProvaCompleta(provaSelecionada.getId());

            if (provaAtual == null) {
                mostrarMensagemNaTela("A prova selecionada não foi encontrada no banco de dados.");
                return;
            }

            montarInterfaceDaProva();
        } catch (RuntimeException erro) {
            erro.printStackTrace();
            mostrarMensagemNaTela("Não foi possível carregar a prova. Verifique a conexão com o banco de dados.");

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro ao carregar prova");
            alerta.setHeaderText("Não foi possível abrir a prova selecionada");
            alerta.setContentText(erro.getMessage());
            alerta.showAndWait();
        }
    }

    private void montarInterfaceDaProva() {
        Label titulo = new Label(provaAtual.getNome() == null ? "Prova" : provaAtual.getNome());
        titulo.setFont(Font.font("System", FontWeight.BOLD, 22));
        titulo.setWrapText(true);
        titulo.setStyle("-fx-text-fill: #1B1B1B;");
        vb_fazerprova_container.getChildren().add(titulo);

        if (provaAtual.getDisciplina() != null && !provaAtual.getDisciplina().trim().isEmpty()) {
            Label disciplina = new Label("Disciplina: " + provaAtual.getDisciplina());
            disciplina.setStyle("-fx-text-fill: #1B1B1B; -fx-font-size: 13px;");
            vb_fazerprova_container.getChildren().add(disciplina);
        }

        if (provaAtual.getPrazo() != null && !provaAtual.getPrazo().trim().isEmpty()) {
            Label prazo = new Label("Prazo: " + provaAtual.getPrazo());
            prazo.setStyle("-fx-text-fill: #1B1B1B; -fx-font-size: 13px;");
            vb_fazerprova_container.getChildren().add(prazo);
        }

        if (provaAtual.getQuestoes() == null || provaAtual.getQuestoes().isEmpty()) {
            Label aviso = new Label("Esta prova não possui questões cadastradas.");
            aviso.setWrapText(true);
            aviso.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            vb_fazerprova_container.getChildren().add(aviso);
            return;
        }

        int numeroQuestao = 1;

        for (Questao questao : provaAtual.getQuestoes()) {
            VBox blocoQuestao = new VBox(10);
            blocoQuestao.setMaxWidth(Double.MAX_VALUE);
            VBox.setVgrow(blocoQuestao, Priority.NEVER);
            blocoQuestao.setPadding(new Insets(15));
            blocoQuestao.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.94);" +
                    "-fx-background-radius: 10px;"
            );

            Label enunciado = new Label(numeroQuestao + ". " + valorOuVazio(questao.getEnunciado()));
            enunciado.setFont(Font.font("System", FontWeight.BOLD, 14));
            enunciado.setWrapText(true);
            enunciado.setMaxWidth(Double.MAX_VALUE);
            blocoQuestao.getChildren().add(enunciado);

            String tipo = tipoNormalizado(questao.getTipo());

            Label tipoQuestao = new Label(textoTipo(tipo));
            tipoQuestao.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
            blocoQuestao.getChildren().add(tipoQuestao);

            if (Questao.DISSERTATIVA.equals(tipo)) {
                TextArea campoResposta = new TextArea();
                campoResposta.setPromptText("Digite sua resposta");
                campoResposta.setWrapText(true);
                campoResposta.setPrefRowCount(4);
                campoResposta.setMaxWidth(Double.MAX_VALUE);
                blocoQuestao.getChildren().add(campoResposta);
                respostas.add(RespostaQuestao.dissertativa(numeroQuestao, questao.getId(), campoResposta));
            } else if (Questao.CHECKLIST.equals(tipo)) {
                List<CheckBox> itens = new ArrayList<>();

                for (String alternativa : questao.getAlternativas()) {
                    CheckBox item = new CheckBox(alternativa);
                    item.setWrapText(true);
                    item.setMaxWidth(Double.MAX_VALUE);
                    item.setPadding(new Insets(2, 0, 2, 12));
                    itens.add(item);
                    blocoQuestao.getChildren().add(item);
                }

                if (itens.isEmpty()) {
                    blocoQuestao.getChildren().add(criarAvisoSemAlternativas());
                }

                respostas.add(RespostaQuestao.checklist(numeroQuestao, questao.getId(), itens));
            } else {
                ToggleGroup grupo = new ToggleGroup();
                int quantidadeAlternativas = 0;

                for (String alternativa : questao.getAlternativas()) {
                    RadioButton opcao = new RadioButton(alternativa);
                    opcao.setToggleGroup(grupo);
                    opcao.setWrapText(true);
                    opcao.setMaxWidth(Double.MAX_VALUE);
                    opcao.setPadding(new Insets(2, 0, 2, 12));
                    blocoQuestao.getChildren().add(opcao);
                    quantidadeAlternativas++;
                }

                if (quantidadeAlternativas == 0) {
                    blocoQuestao.getChildren().add(criarAvisoSemAlternativas());
                }

                respostas.add(RespostaQuestao.multiplaEscolha(numeroQuestao, questao.getId(), grupo, quantidadeAlternativas));
            }

            vb_fazerprova_container.getChildren().add(blocoQuestao);
            numeroQuestao++;
        }
    }

    private Label criarAvisoSemAlternativas() {
        Label aviso = new Label("Nenhuma alternativa foi cadastrada para esta questão.");
        aviso.setWrapText(true);
        aviso.setStyle("-fx-text-fill: #b00020; -fx-font-size: 12px;");
        return aviso;
    }

    private String tipoNormalizado(String tipo) {
        if (tipo == null) {
            return Questao.MULTIPLA_ESCOLHA;
        }

        String normalizado = tipo.trim().toUpperCase();

        if ("DISSERTATIVA".equals(normalizado)) {
            return Questao.DISSERTATIVA;
        }

        if ("CHECKLIST".equals(normalizado)) {
            return Questao.CHECKLIST;
        }

        return Questao.MULTIPLA_ESCOLHA;
    }

    private String textoTipo(String tipo) {
        if (Questao.DISSERTATIVA.equals(tipo)) {
            return "Questão dissertativa";
        }
        if (Questao.CHECKLIST.equals(tipo)) {
            return "Selecione todas as alternativas que considerar corretas";
        }
        return "Selecione uma alternativa";
    }

    private String valorOuVazio(String valor) {
        return valor == null ? "" : valor;
    }

    private void mostrarMensagemNaTela(String mensagem) {
        Label aviso = new Label(mensagem);
        aviso.setWrapText(true);
        aviso.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        vb_fazerprova_container.getChildren().add(aviso);
    }

    @FXML
    private void expande_barra() {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(barra_lateral.prefWidthProperty(), 170.0);
        KeyFrame kf = new KeyFrame(Duration.millis(130), kv);
        timeline.getKeyFrames().add(kf);

        timeline.setOnFinished(event -> mostrarTextosBotoes());
        timeline.play();
    }

    @FXML
    private void minimiza_barra() {
        ocultarTextosBotoes();

        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(barra_lateral.prefWidthProperty(), 60.0);
        KeyFrame kf = new KeyFrame(Duration.millis(150), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    private void ocultarTextosBotoes() {
        if (botoesMenu == null) {
            return;
        }

        for (Button btn : botoesMenu) {
            if (btn != null) {
                btn.setText("");
            }
        }
    }

    private void mostrarTextosBotoes() {
        if (botoesMenu == null) {
            return;
        }

        for (Button btn : botoesMenu) {
            if (btn != null) {
                btn.setText(textosOriginais.get(btn));
            }
        }
    }

    @FXML
    void Cancelar_Prova(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/ProvasAluno");
    }

    @FXML
    void EnviarProva_aluno(ActionEvent event) throws IOException {
        if (provaAtual == null || provaAtual.getQuestoes() == null || provaAtual.getQuestoes().isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Prova indisponível");
            alerta.setHeaderText("Não há uma prova válida para enviar");
            alerta.setContentText("Volte para a lista de provas e tente abrir a prova novamente.");
            alerta.showAndWait();
            return;
        }

        List<Integer> questoesSemResposta = new ArrayList<>();

        for (RespostaQuestao resposta : respostas) {
            if (!resposta.foiRespondida()) {
                questoesSemResposta.add(resposta.numeroQuestao);
            }
        }

        if (!questoesSemResposta.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Questões sem resposta");
            alerta.setHeaderText("Responda todas as questões antes de enviar");
            alerta.setContentText("Questões pendentes: " + juntarNumeros(questoesSemResposta));
            alerta.showAndWait();
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Enviar prova");
        confirmacao.setHeaderText("Confirmar envio da prova?");
        confirmacao.setContentText("Todas as questões foram respondidas.");

        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Long raAlunoLogado = SessaoUsuarioController.getIdUsuarioLogado();

            if (raAlunoLogado == null) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Erro ao enviar prova");
                alerta.setHeaderText("Aluno não identificado");
                alerta.setContentText("Faça login novamente antes de enviar a prova.");
                alerta.showAndWait();
                return;
            }

            // Mantém a ordem das questões e prepara o conteúdo para o AlunoDAO.
            Map<Long, String> respostasParaSalvar = new LinkedHashMap<>();
            for (RespostaQuestao resposta : respostas) {
                respostasParaSalvar.put(resposta.idQuestao, resposta.obterRespostaTexto());
            }

            try {
                AlunoDAO alunoDAO = new AlunoDAO();
                alunoDAO.salvarRespostasProva(raAlunoLogado, provaAtual.getId(), respostasParaSalvar);

                Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                sucesso.setTitle("Prova enviada");
                sucesso.setHeaderText("Respostas salvas com sucesso");
                sucesso.setContentText("Sua prova foi registrada no banco de dados.");
                sucesso.showAndWait();

                App.setRoot("alunoFluxo/MenuAluno");
            } catch (RuntimeException erro) {
                erro.printStackTrace();

                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Erro ao enviar prova");
                alerta.setHeaderText("Não foi possível salvar suas respostas");
                alerta.setContentText(erro.getMessage());
                alerta.showAndWait();
            }
        }
    }

    private String juntarNumeros(List<Integer> numeros) {
        StringBuilder texto = new StringBuilder();

        for (int i = 0; i < numeros.size(); i++) {
            if (i > 0) {
                texto.append(", ");
            }
            texto.append(numeros.get(i));
        }

        return texto.toString();
    }

    @FXML
    void deslogar_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/TelaLoginAlu");
    }

    @FXML
    void entra_dados_pessoais_alu(ActionEvent event) {
    }

    @FXML
    void entra_menuprova_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/ProvasAluno");
    }

    @FXML
    void entra_meucurso(ActionEvent event) {
    }

    private static class RespostaQuestao {
        private final int numeroQuestao;
        private final long idQuestao;
        private final String tipo;
        private final TextArea texto;
        private final ToggleGroup grupo;
        private final List<CheckBox> checklist;
        private final int quantidadeAlternativas;

        private RespostaQuestao(int numeroQuestao, long idQuestao, String tipo, TextArea texto,
                                ToggleGroup grupo, List<CheckBox> checklist,
                                int quantidadeAlternativas) {
            this.numeroQuestao = numeroQuestao;
            this.idQuestao = idQuestao;
            this.tipo = tipo;
            this.texto = texto;
            this.grupo = grupo;
            this.checklist = checklist;
            this.quantidadeAlternativas = quantidadeAlternativas;
        }

        private static RespostaQuestao dissertativa(int numeroQuestao, long idQuestao, TextArea texto) {
            return new RespostaQuestao(numeroQuestao, idQuestao, Questao.DISSERTATIVA, texto,
                    null, null, 0);
        }

        private static RespostaQuestao multiplaEscolha(int numeroQuestao, long idQuestao,
                                                        ToggleGroup grupo, int quantidadeAlternativas) {
            return new RespostaQuestao(numeroQuestao, idQuestao, Questao.MULTIPLA_ESCOLHA, null,
                    grupo, null, quantidadeAlternativas);
        }

        private static RespostaQuestao checklist(int numeroQuestao, long idQuestao, List<CheckBox> checklist) {
            return new RespostaQuestao(numeroQuestao, idQuestao, Questao.CHECKLIST, null,
                    null, checklist, checklist.size());
        }

        private boolean foiRespondida() {
            if (Questao.DISSERTATIVA.equals(tipo)) {
                return texto != null && texto.getText() != null && !texto.getText().trim().isEmpty();
            }

            if (quantidadeAlternativas == 0) {
                return false;
            }

            if (Questao.CHECKLIST.equals(tipo)) {
                if (checklist == null) {
                    return false;
                }

                for (CheckBox item : checklist) {
                    if (item.isSelected()) {
                        return true;
                    }
                }
                return false;
            }

            return grupo != null && grupo.getSelectedToggle() != null;
        }

        /**
         * Converte o componente JavaFX preenchido pelo aluno em texto para o banco.
         */
        private String obterRespostaTexto() {
            if (Questao.DISSERTATIVA.equals(tipo)) {
                return texto == null ? "" : texto.getText().trim();
            }

            if (Questao.CHECKLIST.equals(tipo)) {
                List<String> selecionadas = new ArrayList<>();

                if (checklist != null) {
                    for (CheckBox item : checklist) {
                        if (item.isSelected()) {
                            selecionadas.add(item.getText());
                        }
                    }
                }

                return String.join(" | ", selecionadas);
            }

            if (grupo != null && grupo.getSelectedToggle() instanceof RadioButton) {
                return ((RadioButton) grupo.getSelectedToggle()).getText();
            }

            return "";
        }
    }
}
