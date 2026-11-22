package org.example.controllers.profControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.App;
import org.example.dao.ProvaDAO;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.SessaoApp;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ListaProvaController {

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    @FXML
    private Label lbl_resumo_provas;

    @FXML
    private VBox vb_provacontainer;

    public void initialize() {
        carregaProvasSalvas();
    }

    private void carregaProvasSalvas() {
        if (vb_provacontainer == null) {
            return;
        }

        ProvaDAO dao = new ProvaDAO();
        List<Prova> listaProvas = dao.listarNomeProvas();

        vb_provacontainer.getChildren().clear();

        if (listaProvas == null || listaProvas.isEmpty()) {
            atualizarResumo(0);
            mostrarEstadoVazio();
            return;
        }

        atualizarResumo(listaProvas.size());

        for (Prova provaRecuperada : listaProvas) {
            vb_provacontainer.getChildren().add(criarCardProva(provaRecuperada));
        }
    }

    private HBox criarCardProva(Prova provaRecuperada) {
        final long idDaProva = provaRecuperada.getId();

        HBox cardLinha = new HBox(14);
        cardLinha.setAlignment(Pos.CENTER_LEFT);
        cardLinha.setPadding(new Insets(16, 20, 16, 20));
        cardLinha.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.96);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: #D4DFD8;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );

        VBox infoProva = new VBox(4);

        Label lblNome = new Label(provaRecuperada.getNome() != null ? provaRecuperada.getNome() : "Prova sem título");
        lblNome.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblNome.setStyle("-fx-text-fill: #1B1B1B;");

        Label lblDetalhes = new Label(montarDetalhesProva(provaRecuperada));
        lblDetalhes.setFont(Font.font("System", 12));
        lblDetalhes.setStyle("-fx-text-fill: #66706A;");

        infoProva.getChildren().addAll(lblNome, lblDetalhes);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        cardLinha.getChildren().addAll(infoProva, espacador);

        if (provaRecuperada.getDisciplina() != null && !provaRecuperada.getDisciplina().isBlank()) {
            Label badgeDisciplina = new Label(provaRecuperada.getDisciplina());
            badgeDisciplina.setPadding(new Insets(5, 12, 5, 12));
            badgeDisciplina.setStyle(
                    "-fx-background-color: #DDEEE2;" +
                            "-fx-text-fill: #285A36;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 12px;" +
                            "-fx-background-radius: 12px;"
            );
            cardLinha.getChildren().add(badgeDisciplina);
        }

        Button btnAcessar = new Button("Acessar");
        btnAcessar.setPadding(new Insets(8, 16, 8, 16));
        String estiloVerdeNormal =
                "-fx-background-color: #4A7C59;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 7px;" +
                        "-fx-cursor: hand;";
        String estiloVerdeHover =
                "-fx-background-color: #3B6347;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 7px;" +
                        "-fx-cursor: hand;";
        btnAcessar.setStyle(estiloVerdeNormal);
        btnAcessar.setOnMouseEntered(e -> btnAcessar.setStyle(estiloVerdeHover));
        btnAcessar.setOnMouseExited(e -> btnAcessar.setStyle(estiloVerdeNormal));
        btnAcessar.setOnAction(evento -> abrirPreviewProva(provaRecuperada));

        Button btnRemove = new Button("Remover");
        btnRemove.setPadding(new Insets(8, 14, 8, 14));
        String estiloVermelhoNormal =
                "-fx-background-color: #FDECEA;" +
                        "-fx-text-fill: #B3261E;" +
                        "-fx-border-color: #F5C2BE;" +
                        "-fx-border-radius: 7px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 7px;" +
                        "-fx-cursor: hand;";
        String estiloVermelhoHover =
                "-fx-background-color: #D93025;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #D93025;" +
                        "-fx-border-radius: 7px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 7px;" +
                        "-fx-cursor: hand;";
        btnRemove.setStyle(estiloVermelhoNormal);
        btnRemove.setOnMouseEntered(e -> btnRemove.setStyle(estiloVermelhoHover));
        btnRemove.setOnMouseExited(e -> btnRemove.setStyle(estiloVermelhoNormal));
        btnRemove.setOnAction(evento -> confirmarERemoverProva(idDaProva, provaRecuperada.getNome(), cardLinha));

        cardLinha.getChildren().addAll(btnAcessar, btnRemove);
        return cardLinha;
    }

    private String montarDetalhesProva(Prova prova) {
        List<String> partes = new ArrayList<>();

        if (prova.getDuracaoMinutos() != null && prova.getDuracaoMinutos() > 0) {
            partes.add("Duração: " + prova.getDuracaoMinutos() + " min");
        }

        if (prova.getDataInicial() != null) {
            partes.add("Início: " + prova.getDataInicial().format(FORMATO_DATA_HORA));
        }

        if (prova.getDataLimite() != null) {
            partes.add("Limite: " + prova.getDataLimite().format(FORMATO_DATA_HORA));
        } else {
            partes.add("Sem prazo limite");
        }

        return String.join("  •  ", partes);
    }

    private void abrirPreviewProva(Prova provaSelecionada) {
        try {
            ProvaDAO provaDAO = new ProvaDAO();
            Prova provaCompleta = provaDAO.buscarProvaCompletaParaProfessor((int) provaSelecionada.getId());

            if (provaCompleta == null) {
                throw new Exception("Prova não encontrada no banco de dados.");
            }

            SessaoApp.provaAtual = provaCompleta;
            App.setRoot("professorFluxo/PreviewFormulario");

        } catch (Exception e) {
            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setTitle("Detalhes da prova indisponíveis");
            erro.setHeaderText("Não foi possível abrir esta prova");
            erro.setContentText("Ocorreu um erro ao carregar os dados da prova.\n\n" + e.getMessage());
            erro.showAndWait();
            e.printStackTrace();
        }
    }

    private void confirmarERemoverProva(long idDaProva, String nomeProva, HBox cardLinha) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Remover prova");
        confirmacao.setHeaderText("Deseja realmente excluir \"" + nomeProva + "\"?");
        confirmacao.setContentText("Esta ação removerá a prova do banco de dados e não poderá ser desfeita.");

        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            ProvaDAO provaDAO = new ProvaDAO();
            boolean deletadoDoBanco = provaDAO.deletarProva(idDaProva);

            if (deletadoDoBanco) {
                vb_provacontainer.getChildren().remove(cardLinha);
                int restantes = vb_provacontainer.getChildren().size();
                atualizarResumo(restantes);
                if (restantes == 0) {
                    mostrarEstadoVazio();
                }
            } else {
                Alert erro = new Alert(Alert.AlertType.ERROR);
                erro.setTitle("Erro ao remover");
                erro.setHeaderText("Não foi possível excluir a prova");
                erro.setContentText("Verifique se existem respostas vinculadas a esta prova no banco.");
                erro.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void atualizarResumo(int total) {
        if (lbl_resumo_provas != null) {
            lbl_resumo_provas.setText(total + " prova(s) postada(s) no sistema");
        }
    }

    private void mostrarEstadoVazio() {
        VBox cardVazio = new VBox(8);
        cardVazio.setAlignment(Pos.CENTER);
        cardVazio.setPadding(new Insets(36, 24, 36, 24));
        cardVazio.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.96);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: #D4DFD8;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;"
        );

        Label lblTitulo = new Label("Nenhuma prova postada ainda");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTitulo.setStyle("-fx-text-fill: #1B1B1B;");

        Label lblSub = new Label("Clique em \"+ Criar Prova\" no canto superior direito para montar sua primeira avaliação.");
        lblSub.setWrapText(true);
        lblSub.setStyle("-fx-text-fill: #66706A; -fx-font-size: 13px;");

        cardVazio.getChildren().addAll(lblTitulo, lblSub);
        vb_provacontainer.getChildren().add(cardVazio);
    }

    @FXML
    void CriarProva(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/CriaProva");
    }

    @FXML
    void VoltarInicio(ActionEvent event) throws IOException {
        App.setRoot("professorFluxo/MenuProf");
    }

    @FXML
    void abrirCriarQuestao(ActionEvent event) {
        var botao = (javafx.scene.Node) event.getSource();
        CriarQuestaoDialog.abrir(botao.getScene().getWindow());
    }

}