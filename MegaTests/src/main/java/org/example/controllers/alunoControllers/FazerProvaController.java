package org.example.controllers.alunoControllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FazerProvaController implements Initializable {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

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
    private Label lbl_disciplina_prova;

    @FXML
    private Label lbl_titulo_prova;

    @FXML
    private Label lbl_periodo_prova;

    @FXML
    private Label lblTempoRestante;

    @FXML
    private Label lbl_progresso_questoes;

    @FXML
    private VBox vb_fazerprova_container;

    private final Map<Button, String> textosOriginais = new HashMap<>();
    private final List<RespostaQuestao> respostas = new ArrayList<>();
    private Button[] botoesMenu;
    private Prova provaAtual;

    // --- VARIÁVEIS DO CRONÓMETRO E CONTROLO DE FECHO ---
    private Timeline cronometro;
    private int segundosRestantes;
    private boolean submissaoPorTempoIniciada = false;
    private static boolean provaEmAndamento = false;
    private static FazerProvaController instanciaAtiva;

    public static boolean isProvaEmAndamento() {
        return provaEmAndamento;
    }

    public static void submeterProvaAutomaticamenteNoEncerramento() {
        if (instanciaAtiva != null && instanciaAtiva.provaAtual != null && provaEmAndamento) {
            provaEmAndamento = false;
            if (instanciaAtiva.cronometro != null) {
                instanciaAtiva.cronometro.stop();
            }

            Long raAlunoLogado = SessaoUsuarioController.getIdUsuarioLogado();
            if (raAlunoLogado != null) {
                Map<Long, String> respostasParaSalvar = new LinkedHashMap<>();
                for (RespostaQuestao resposta : instanciaAtiva.respostas) {
                    respostasParaSalvar.put(resposta.idQuestao, resposta.obterRespostaTexto());
                }

                try {
                    new AlunoDAO().salvarRespostasProva(raAlunoLogado, instanciaAtiva.provaAtual.getId(), respostasParaSalvar);
                    System.out.println("Prova enviada automaticamente devido ao encerramento.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instanciaAtiva = this;
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
        vb_fazerprova_container.setDisable(false);
        respostas.clear();
        submissaoPorTempoIniciada = false;

        Prova provaSelecionada = ProvasAlunoController.getProvaSelecionadaParaAbertura();

        if (provaSelecionada == null) {
            mostrarCardAviso("Nenhuma prova foi selecionada.");
            return;
        }

        try {
            provaAtual = new ProvaDAO().buscarProvaCompleta(provaSelecionada.getId());

            if (provaAtual == null) {
                mostrarCardAviso("A prova selecionada não foi encontrada no banco de dados.");
                return;
            }

            List<Questao> questoesEmbaralhadas = new ArrayList<>(provaAtual.getQuestoes());
            Collections.shuffle(questoesEmbaralhadas);
            provaAtual.setQuestoes(questoesEmbaralhadas);

            provaEmAndamento = true;
            montarInterfaceDaProva();
        } catch (RuntimeException erro) {
            erro.printStackTrace();
            mostrarCardAviso("Não foi possível carregar a prova. Verifique a conexão com o banco de dados.");

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro ao carregar prova");
            alerta.setHeaderText("Não foi possível abrir a prova selecionada");
            alerta.setContentText(erro.getMessage());
            alerta.showAndWait();
        }
    }

    private void montarInterfaceDaProva() {
        // --- CABEÇALHO DA PROVA ---
        if (lbl_disciplina_prova != null) {
            String disc = (provaAtual.getDisciplina() != null && !provaAtual.getDisciplina().trim().isEmpty())
                    ? provaAtual.getDisciplina().toUpperCase()
                    : "AVALIAÇÃO";
            lbl_disciplina_prova.setText(disc);
        }

        if (lbl_titulo_prova != null) {
            lbl_titulo_prova.setText(provaAtual.getNome() == null ? "Prova" : provaAtual.getNome());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dataIniStr = provaAtual.getDataInicial() != null ? provaAtual.getDataInicial().format(fmt) : "Imediato";
        String dataFimStr = provaAtual.getDataLimite() != null ? provaAtual.getDataLimite().format(fmt) : "Sem limite";

        if (lbl_periodo_prova != null) {
            lbl_periodo_prova.setText("Período disponível: " + dataIniStr + " até " + dataFimStr);
        }

        if (provaAtual.getQuestoes() == null || provaAtual.getQuestoes().isEmpty()) {
            mostrarCardAviso("Esta prova não possui questões cadastradas.");
            atualizarProgressoRodape();
            return;
        }

        // Monta as questões antes de verificar o cronômetro para que a lista 'respostas'
        // já contenha todas as questões (mesmo que zeradas) caso o tempo já tenha expirado.
        int numeroQuestao = 1;
        for (Questao questao : provaAtual.getQuestoes()) {
            VBox blocoQuestao = criarCardQuestao(questao, numeroQuestao);
            vb_fazerprova_container.getChildren().add(blocoQuestao);
            numeroQuestao++;
        }

        atualizarProgressoRodape();

        // --- CRONÓMETRO E DURAÇÃO (PERSISTENTE PELA HORA DE INÍCIO NO BANCO) ---
        if (provaAtual.getDuracaoMinutos() != null && provaAtual.getDuracaoMinutos() > 0) {
            if (lblTempoRestante != null) {
                lblTempoRestante.setVisible(true);
                lblTempoRestante.setManaged(true);
                lblTempoRestante.setText("Tempo restante: Calculando...");
            }

            Long raAlunoLogado = SessaoUsuarioController.getIdUsuarioLogado();
            AlunoDAO alunoDAO = new AlunoDAO();

            LocalDateTime dataInicioAluno;
            if (raAlunoLogado != null) {
                dataInicioAluno = alunoDAO.buscarDataInicioProva(raAlunoLogado, provaAtual.getId());

                if (dataInicioAluno == null) {
                    dataInicioAluno = LocalDateTime.now();
                    alunoDAO.registrarInicioProva(raAlunoLogado, provaAtual.getId(), dataInicioAluno);
                }
            } else {
                dataInicioAluno = LocalDateTime.now();
            }

            long segundosDecorridos = java.time.Duration.between(dataInicioAluno, LocalDateTime.now()).getSeconds();
            long duracaoTotalSegundos = provaAtual.getDuracaoMinutos() * 60L;

            segundosRestantes = (int) (duracaoTotalSegundos - segundosDecorridos);

            if (segundosRestantes <= 0) {
                if (lblTempoRestante != null) {
                    lblTempoRestante.setText("Tempo esgotado!");
                }
                submeterProvaPorTempoEsgotado();
            } else {
                iniciarCronometro();
            }
        } else if (lblTempoRestante != null) {
            lblTempoRestante.setVisible(false);
            lblTempoRestante.setManaged(false);
        }
    }

    private VBox criarCardQuestao(Questao questao, int numeroQuestao) {
        VBox blocoQuestao = new VBox(12);
        blocoQuestao.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(blocoQuestao, Priority.NEVER);
        blocoQuestao.setPadding(new Insets(18, 22, 18, 22));
        blocoQuestao.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.96);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: #D4DFEC;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);"
        );

        // Cabeçalho do card: enunciado + badge de pontuação
        HBox topoCard = new HBox(12);
        topoCard.setAlignment(Pos.TOP_LEFT);

        Label enunciado = new Label(numeroQuestao + ". " + valorOuVazio(questao.getEnunciado()));
        enunciado.setFont(Font.font("System", FontWeight.BOLD, 15));
        enunciado.setWrapText(true);
        enunciado.setMaxWidth(Double.MAX_VALUE);
        enunciado.setStyle("-fx-text-fill: #1B1B1B;");
        HBox.setHgrow(enunciado, Priority.ALWAYS);

        Label badgePontos = new Label(String.format(PT_BR, "Vale %.2f pt(s)", questao.getPontos()));
        badgePontos.setMinWidth(Region.USE_PREF_SIZE);
        badgePontos.setPadding(new Insets(4, 12, 4, 12));
        badgePontos.setStyle(
                "-fx-background-color: #E3EEFF;" +
                        "-fx-text-fill: #004EC2;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 12px;"
        );

        topoCard.getChildren().addAll(enunciado, badgePontos);

        String tipo = tipoNormalizado(questao.getTipo());

        Label tipoQuestao = new Label(textoTipo(tipo));
        tipoQuestao.setStyle("-fx-text-fill: #5F6368; -fx-font-size: 12px;");

        blocoQuestao.getChildren().addAll(topoCard, tipoQuestao, new Separator());

        // --- CHECKLIST ---
        if (Questao.CHECKLIST.equals(tipo)) {
            List<CheckBox> itens = new ArrayList<>();
            VBox listaOpcoes = new VBox(8);

            for (String alternativa : questao.getAlternativas()) {
                CheckBox item = new CheckBox(alternativa);
                item.setWrapText(true);
                item.setMaxWidth(Double.MAX_VALUE);
                item.setStyle(estiloOpcao());
                item.selectedProperty().addListener((obs, oldV, newV) -> atualizarProgressoRodape());
                itens.add(item);
                listaOpcoes.getChildren().add(item);
            }

            if (itens.isEmpty()) {
                blocoQuestao.getChildren().add(criarAvisoSemAlternativas());
            } else {
                blocoQuestao.getChildren().add(listaOpcoes);
            }

            respostas.add(RespostaQuestao.checklist(numeroQuestao, questao.getId(), itens));
        }
        // --- DISSERTATIVA ---
        else if (Questao.DISSERTATIVA.equals(tipo)) {
            TextArea campoResposta = new TextArea();
            campoResposta.setPromptText("Digite sua resposta aqui...");
            campoResposta.setWrapText(true);
            campoResposta.setPrefRowCount(4);
            campoResposta.setMaxWidth(Double.MAX_VALUE);
            campoResposta.setStyle(
                    "-fx-control-inner-background: #F7FAFF;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-border-color: #DCE6F5;" +
                            "-fx-border-radius: 8px;" +
                            "-fx-font-size: 13px;"
            );
            campoResposta.textProperty().addListener((obs, oldV, newV) -> atualizarProgressoRodape());

            blocoQuestao.getChildren().add(campoResposta);
            respostas.add(RespostaQuestao.dissertativa(numeroQuestao, questao.getId(), campoResposta));
        }
        // --- CORRESPONDÊNCIA ---
        else if (Questao.CORRESPONDENCIA.equals(tipo)) {
            VBox areaCorrespondencia = new VBox(10);

            VBox caixaItens = new VBox(6);
            caixaItens.setPadding(new Insets(12, 14, 12, 14));
            caixaItens.setStyle(
                    "-fx-background-color: #F7FAFF;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-border-color: #DCE6F5;" +
                            "-fx-border-radius: 8px;"
            );

            Label tituloItens = new Label("Itens de referência:");
            tituloItens.setStyle("-fx-font-weight: bold; -fx-text-fill: #0066FF; -fx-font-size: 12px;");
            caixaItens.getChildren().add(tituloItens);

            List<String> itens = questao.getItensCorrespondencia();
            for (int i = 0; i < itens.size(); i++) {
                Label item = new Label((i + 1) + ". " + itens.get(i));
                item.setWrapText(true);
                item.setStyle("-fx-text-fill: #1B1B1B; -fx-font-size: 13px;");
                caixaItens.getChildren().add(item);
            }

            areaCorrespondencia.getChildren().add(caixaItens);

            Label tituloAlternativas = new Label("Correspondências:");
            tituloAlternativas.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B1B1B; -fx-font-size: 13px;");
            areaCorrespondencia.getChildren().add(tituloAlternativas);

            List<ComboBox<Integer>> respostasCorrespondencia = new ArrayList<>();
            List<String> textosAlternativas = questao.getAlternativasCorrespondencia();

            for (String alternativa : textosAlternativas) {
                HBox linha = new HBox(12);
                linha.setAlignment(Pos.CENTER_LEFT);
                linha.setPadding(new Insets(8, 12, 8, 12));
                linha.setStyle(
                        "-fx-background-color: #F7FAFF;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-border-color: #DCE6F5;" +
                                "-fx-border-radius: 8px;"
                );

                ComboBox<Integer> numero = new ComboBox<>();
                numero.setPromptText("Nº");
                numero.setPrefWidth(72);
                for (int i = 1; i <= itens.size(); i++) {
                    numero.getItems().add(i);
                }
                numero.valueProperty().addListener((obs, oldV, newV) -> atualizarProgressoRodape());

                Label textoAlternativa = new Label(alternativa);
                textoAlternativa.setWrapText(true);
                textoAlternativa.setStyle("-fx-text-fill: #1B1B1B; -fx-font-size: 13px;");
                HBox.setHgrow(textoAlternativa, Priority.ALWAYS);

                respostasCorrespondencia.add(numero);
                linha.getChildren().addAll(numero, textoAlternativa);
                areaCorrespondencia.getChildren().add(linha);
            }

            blocoQuestao.getChildren().add(areaCorrespondencia);
            respostas.add(RespostaQuestao.correspondencia(numeroQuestao, questao.getId(), respostasCorrespondencia, textosAlternativas));
        }
        // --- MÚLTIPLA ESCOLHA ---
        else {
            ToggleGroup grupo = new ToggleGroup();
            VBox listaOpcoes = new VBox(8);
            int quantidadeAlternativas = 0;

            for (String alternativa : questao.getAlternativas()) {
                RadioButton opcao = new RadioButton(alternativa);
                opcao.setToggleGroup(grupo);
                opcao.setWrapText(true);
                opcao.setMaxWidth(Double.MAX_VALUE);
                opcao.setStyle(estiloOpcao());
                listaOpcoes.getChildren().add(opcao);
                quantidadeAlternativas++;
            }

            grupo.selectedToggleProperty().addListener((obs, oldV, newV) -> atualizarProgressoRodape());

            if (quantidadeAlternativas == 0) {
                blocoQuestao.getChildren().add(criarAvisoSemAlternativas());
            } else {
                blocoQuestao.getChildren().add(listaOpcoes);
            }

            respostas.add(RespostaQuestao.multiplaEscolha(numeroQuestao, questao.getId(), grupo, quantidadeAlternativas));
        }

        return blocoQuestao;
    }

    private String estiloOpcao() {
        return "-fx-background-color: #F7FAFF;" +
                "-fx-border-color: #DCE6F5;" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 9 12 9 12;" +
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #1B1B1B;" +
                "-fx-cursor: hand;";
    }

    private void atualizarProgressoRodape() {
        if (lbl_progresso_questoes == null) {
            return;
        }
        int total = respostas.size();
        int respondidas = 0;
        for (RespostaQuestao r : respostas) {
            if (r.foiRespondida()) {
                respondidas++;
            }
        }
        lbl_progresso_questoes.setText("Respondidas: " + respondidas + " / " + total);
    }

    // --- LÓGICA DO CRONÓMETRO TEMPO REAL ---
    private void iniciarCronometro() {
        if (cronometro != null) {
            cronometro.stop();
        }

        if (lblTempoRestante != null) {
            lblTempoRestante.setText("Tempo restante: " + formatarTempo(segundosRestantes));
        }

        cronometro = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            segundosRestantes--;

            if (segundosRestantes <= 0) {
                if (lblTempoRestante != null) {
                    lblTempoRestante.setText("Tempo esgotado!");
                }
                cronometro.stop();
                submeterProvaPorTempoEsgotado();
            } else if (lblTempoRestante != null) {
                lblTempoRestante.setText("Tempo restante: " + formatarTempo(segundosRestantes));
            }
        }));

        cronometro.setCycleCount(Timeline.INDEFINITE);
        cronometro.play();
    }

    private String formatarTempo(int totalSegundos) {
        int horas = totalSegundos / 3600;
        int minutos = (totalSegundos % 3600) / 60;
        int segundos = totalSegundos % 60;

        if (horas > 0) {
            return String.format("%02d:%02d:%02d h", horas, minutos, segundos);
        } else {
            return String.format("%02d:%02d min", minutos, segundos);
        }
    }

    private void submeterProvaPorTempoEsgotado() {
        if (submissaoPorTempoIniciada) {
            return;
        }
        submissaoPorTempoIniciada = true;
        provaEmAndamento = false;

        if (cronometro != null) {
            cronometro.stop();
        }

        // Bloqueia imediatamente os campos da prova para impedir alterações após o tempo zerar
        if (vb_fazerprova_container != null) {
            vb_fazerprova_container.setDisable(true);
        }

        // Executa fora do ciclo de animação do Timeline (evita IllegalStateException no showAndWait)
        Platform.runLater(() -> {
            Long raAlunoLogado = SessaoUsuarioController.getIdUsuarioLogado();

            if (raAlunoLogado != null && provaAtual != null) {
                Map<Long, String> respostasParaSalvar = new LinkedHashMap<>();
                for (RespostaQuestao resposta : respostas) {
                    // Questões não preenchidas retornam "" (ficam zeradas na correção)
                    respostasParaSalvar.put(resposta.idQuestao, resposta.obterRespostaTexto());
                }

                try {
                    new AlunoDAO().salvarRespostasProva(raAlunoLogado, provaAtual.getId(), respostasParaSalvar);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Tempo Esgotado");
            alerta.setHeaderText("O tempo limite para a realização da prova acabou");
            alerta.setContentText("Suas respostas preenchidas foram enviadas automaticamente (questões em branco foram zeradas).");
            alerta.showAndWait();

            try {
                App.setRoot("alunoFluxo/MenuAluno");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Label criarAvisoSemAlternativas() {
        Label aviso = new Label("Nenhuma alternativa foi cadastrada para esta questão.");
        aviso.setWrapText(true);
        aviso.setStyle("-fx-text-fill: #B3261E; -fx-font-size: 12px;");
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
        if ("CORRESPONDENCIA".equals(normalizado)) {
            return Questao.CORRESPONDENCIA;
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
        if (Questao.CORRESPONDENCIA.equals(tipo)) {
            return "Relacione cada alternativa ao número correspondente (sem repetir números)";
        }
        return "Selecione uma alternativa";
    }

    private String valorOuVazio(String valor) {
        return valor == null ? "" : valor;
    }

    private void mostrarCardAviso(String mensagem) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(32, 24, 32, 24));
        card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.96);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: #D4DFEC;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;"
        );

        Label aviso = new Label(mensagem);
        aviso.setWrapText(true);
        aviso.setStyle("-fx-text-fill: #5F6368; -fx-font-size: 14px;");
        card.getChildren().add(aviso);
        vb_fazerprova_container.getChildren().add(card);
    }

    @FXML
    private void expande_barra() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(130), new KeyValue(barra_lateral.prefWidthProperty(), 170.0))
        );
        timeline.setOnFinished(event -> mostrarTextosBotoes());
        timeline.play();
    }

    @FXML
    private void minimiza_barra() {
        ocultarTextosBotoes();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(150), new KeyValue(barra_lateral.prefWidthProperty(), 60.0))
        );
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
        if (submissaoPorTempoIniciada) {
            return;
        }

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

            Map<Long, String> respostasParaSalvar = new LinkedHashMap<>();
            for (RespostaQuestao resposta : respostas) {
                respostasParaSalvar.put(resposta.idQuestao, resposta.obterRespostaTexto());
            }

            try {
                provaEmAndamento = false;
                if (cronometro != null) {
                    cronometro.stop();
                }

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
    void entra_dados_pessoais_alu(ActionEvent event) throws IOException {
        App.abrirDadosPessoais();
    }

    @FXML
    void entra_menuprova_aluno(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/ProvasAluno");
    }

    @FXML
    void entra_meucurso(ActionEvent event) throws IOException {
        App.setRoot("alunoFluxo/MeuCursoAluno");
    }

    private static class RespostaQuestao {
        private final int numeroQuestao;
        private final long idQuestao;
        private final String tipo;
        private final TextArea texto;
        private final ToggleGroup grupo;
        private final List<CheckBox> checklist;
        private final int quantidadeAlternativas;
        private final List<ComboBox<Integer>> correspondencia;
        private final List<String> textosCorrespondencia;

        private RespostaQuestao(int numeroQuestao, long idQuestao, String tipo, TextArea texto,
                                ToggleGroup grupo, List<CheckBox> checklist,
                                int quantidadeAlternativas, List<ComboBox<Integer>> correspondencia,
                                List<String> textosCorrespondencia) {
            this.numeroQuestao = numeroQuestao;
            this.idQuestao = idQuestao;
            this.tipo = tipo;
            this.texto = texto;
            this.grupo = grupo;
            this.checklist = checklist;
            this.quantidadeAlternativas = quantidadeAlternativas;
            this.correspondencia = correspondencia;
            this.textosCorrespondencia = textosCorrespondencia;
        }

        private static RespostaQuestao dissertativa(int numeroQuestao, long idQuestao, TextArea texto) {
            return new RespostaQuestao(numeroQuestao, idQuestao, Questao.DISSERTATIVA, texto,
                    null, null, 0, null, null);
        }

        private static RespostaQuestao multiplaEscolha(int numeroQuestao, long idQuestao,
                                                       ToggleGroup grupo, int quantidadeAlternativas) {
            return new RespostaQuestao(numeroQuestao, idQuestao, Questao.MULTIPLA_ESCOLHA, null,
                    grupo, null, quantidadeAlternativas, null, null);
        }

        private static RespostaQuestao checklist(int numeroQuestao, long idQuestao, List<CheckBox> checklist) {
            return new RespostaQuestao(numeroQuestao, idQuestao, Questao.CHECKLIST, null,
                    null, checklist, checklist.size(), null, null);
        }

        private static RespostaQuestao correspondencia(int numeroQuestao, long idQuestao,
                                                       List<ComboBox<Integer>> correspondencia,
                                                       List<String> textosCorrespondencia) {
            return new RespostaQuestao(numeroQuestao, idQuestao, Questao.CORRESPONDENCIA, null,
                    null, null, correspondencia.size(), correspondencia, textosCorrespondencia);
        }

        private boolean foiRespondida() {
            if (Questao.DISSERTATIVA.equals(tipo)) {
                return texto != null && texto.getText() != null && !texto.getText().trim().isEmpty();
            }

            if (quantidadeAlternativas == 0) {
                return false;
            }

            if (Questao.CORRESPONDENCIA.equals(tipo)) {
                if (correspondencia == null || correspondencia.isEmpty()) {
                    return false;
                }
                List<Integer> numerosUsados = new ArrayList<>();
                for (ComboBox<Integer> combo : correspondencia) {
                    Integer numero = combo.getValue();
                    if (numero == null || numerosUsados.contains(numero)) {
                        return false;
                    }
                    numerosUsados.add(numero);
                }
                return true;
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

        private String obterRespostaTexto() {
            if (Questao.DISSERTATIVA.equals(tipo)) {
                return texto == null ? "" : texto.getText().trim();
            }

            if (Questao.CORRESPONDENCIA.equals(tipo)) {
                List<String> respostas = new ArrayList<>();
                for (int i = 0; i < correspondencia.size(); i++) {
                    Integer numero = correspondencia.get(i).getValue();
                    String textoAlt = textosCorrespondencia.get(i);
                    respostas.add(textoAlt + "=" + (numero == null ? "" : numero));
                }
                return String.join(" | ", respostas);
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