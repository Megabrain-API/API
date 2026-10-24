package org.example.controllers.profControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.App;
import org.example.dao.TurmaProfessorDAO;
import org.example.entity.profEntity.AlunoProva;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.Questao;
import org.example.entity.profEntity.RespostaAlunoProva;
import org.example.entity.profEntity.TurmaItem;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Mostra ao professor a prova já respondida pelo aluno.
 * Questões corretas ficam destacadas em verde e incorretas em vermelho.
 */
public class RespostaAlunoProfessorController implements Initializable {

    @FXML
    private Label lbl_titulo_correcao;

    @FXML
    private Label lbl_info_correcao;

    @FXML
    private VBox vb_respostas_professor;

    private final TurmaProfessorDAO dao = new TurmaProfessorDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        carregarRespostas();
    }

    private void carregarRespostas() {
        Prova prova = TurmasProfessorController.getProvaSelecionada();
        AlunoProva aluno = TurmasProfessorController.getAlunoSelecionado();
        TurmaItem curso = TurmasProfessorController.getCursoSelecionado();
        Long matriculaProfessor = SessaoUsuarioController.getIdUsuarioLogado();

        if (prova == null || aluno == null || matriculaProfessor == null) {
            mostrarErro("Não foi possível identificar a prova ou o aluno selecionado.");
            return;
        }

        lbl_titulo_correcao.setText(prova.getNome());

        String info = "Aluno: " + aluno.getNome() + "  •  RA: " + aluno.getRa();
        if (curso != null) {
            info += "  •  Curso: " + curso.getNome();
        }
        if (prova.getDisciplina() != null) {
            info += "  •  Disciplina: " + prova.getDisciplina();
        }
        lbl_info_correcao.setText(info);

        try {
            List<RespostaAlunoProva> respostas = dao.buscarRespostasDoAluno(
                    prova.getId(),
                    aluno.getRa(),
                    matriculaProfessor
            );

            vb_respostas_professor.getChildren().clear();

            if (respostas.isEmpty()) {
                Label vazio = new Label("Nenhuma resposta foi encontrada para este aluno.");
                vazio.setStyle("-fx-text-fill: #1B1B1B; -fx-font-size: 14px;");
                vb_respostas_professor.getChildren().add(vazio);
                return;
            }

            for (RespostaAlunoProva resposta : respostas) {
                vb_respostas_professor.getChildren().add(criarBlocoQuestao(resposta));
            }
        } catch (RuntimeException erro) {
            erro.printStackTrace();
            mostrarErro(erro.getMessage());
        }
    }

    private VBox criarBlocoQuestao(RespostaAlunoProva resposta) {
        VBox bloco = new VBox(9);
        bloco.setPadding(new Insets(15));
        bloco.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(bloco, Priority.NEVER);
        bloco.setStyle(
                "-fx-background-color: rgba(255,255,255,0.96);" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;" +
                "-fx-border-width: 2px;" +
                "-fx-border-color: " + (resposta.isCorreta() ? "#4A7C59;" : "#B3261E;")
        );

        Label enunciado = new Label(
                resposta.getOrdem() + ". " + valorOuVazio(resposta.getEnunciado()));
        enunciado.setFont(Font.font("System", FontWeight.BOLD, 14));
        enunciado.setWrapText(true);
        enunciado.setMaxWidth(Double.MAX_VALUE);
        enunciado.setStyle("-fx-text-fill: #1B1B1B;");
        bloco.getChildren().add(enunciado);

        Label tipo = new Label(textoTipo(resposta.getTipo()));
        tipo.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
        bloco.getChildren().add(tipo);

        if (Questao.DISSERTATIVA.equals(resposta.getTipo())) {
            bloco.getChildren().add(criarTextoResposta(
                    "Resposta do aluno:", resposta.getRespostaAluno()));
            bloco.getChildren().add(criarTextoResposta(
                    "Gabarito:", resposta.getGabarito()));
        } else {
            for (RespostaAlunoProva.AlternativaCorrecao alternativa : resposta.getAlternativas()) {
                bloco.getChildren().add(criarAlternativa(alternativa));
            }
        }

        Label resultado = new Label(
                resposta.isCorreta() ? "✓ RESPOSTA CORRETA" : "✗ RESPOSTA INCORRETA");
        resultado.setFont(Font.font("System", FontWeight.BOLD, 12));
        resultado.setStyle(
                "-fx-text-fill: white;" +
                "-fx-background-color: " + (resposta.isCorreta() ? "#4A7C59;" : "#B3261E;") +
                "-fx-background-radius: 7px;" +
                "-fx-padding: 6px 10px;"
        );
        bloco.getChildren().add(resultado);

        return bloco;
    }

    private VBox criarTextoResposta(String titulo, String texto) {
        VBox caixa = new VBox(3);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblTitulo.setStyle("-fx-text-fill: #1B1B1B;");

        Label lblTexto = new Label(valorOuVazio(texto));
        lblTexto.setWrapText(true);
        lblTexto.setMaxWidth(Double.MAX_VALUE);
        lblTexto.setStyle(
                "-fx-text-fill: #1B1B1B;" +
                "-fx-background-color: #F1F1F1;" +
                "-fx-background-radius: 6px;" +
                "-fx-padding: 8px;"
        );

        caixa.getChildren().addAll(lblTitulo, lblTexto);
        return caixa;
    }

    private Label criarAlternativa(RespostaAlunoProva.AlternativaCorrecao alternativa) {
        String prefixo = "• ";
        String fundo = "transparent";
        String corTexto = "#1B1B1B";

        if (alternativa.isSelecionada() && alternativa.isCorreta()) {
            prefixo = "✓ Selecionada e correta: ";
            fundo = "#DDEEE2";
            corTexto = "#285A36";
        } else if (alternativa.isSelecionada()) {
            prefixo = "✗ Selecionada e incorreta: ";
            fundo = "#F7DDDA";
            corTexto = "#8C1D18";
        } else if (alternativa.isCorreta()) {
            prefixo = "✓ Alternativa correta: ";
            fundo = "#EAF4ED";
            corTexto = "#285A36";
        }

        Label label = new Label(prefixo + valorOuVazio(alternativa.getTexto()));
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle(
                "-fx-text-fill: " + corTexto + ";" +
                "-fx-background-color: " + fundo + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-padding: 7px 9px;"
        );
        return label;
    }

    private String textoTipo(String tipo) {
        if (Questao.DISSERTATIVA.equals(tipo)) {
            return "Questão dissertativa";
        }
        if (Questao.CHECKLIST.equals(tipo)) {
            return "Questão de múltiplas respostas";
        }
        return "Questão de múltipla escolha";
    }

    private String valorOuVazio(String valor) {
        return valor == null || valor.trim().isEmpty() ? "(sem resposta)" : valor;
    }

    private void mostrarErro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro ao abrir respostas");
        alerta.setHeaderText("Não foi possível carregar a prova respondida");
        alerta.setContentText(mensagem == null ? "Erro desconhecido." : mensagem);
        alerta.showAndWait();
    }

    @FXML
    private void voltarParaTurmas() throws IOException {
        TurmasProfessorController.voltarParaAlunosAoAbrir();
        App.setRoot("professorFluxo/turmas_professor");
    }
}
