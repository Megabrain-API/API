package org.example.controllers.profControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.App;
import org.example.dao.TurmaProfessorDAO;
import org.example.entity.profEntity.AlunoProva;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.TurmaItem;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controla a navegação da tela Minhas Turmas.
 *
 * Fluxo da tela:
 * Disciplina -> Curso -> Prova -> Aluno que realizou a prova.
 */
public class TurmasProfessorController implements Initializable {

    @FXML
    private Label lbl_titulo_turmas;

    @FXML
    private Label lbl_caminho_turmas;

    @FXML
    private VBox vb_turmas_container;

    @FXML
    private Button btn_voltar_turmas;

    private final TurmaProfessorDAO dao = new TurmaProfessorDAO();
    private Etapa etapaAtual = Etapa.DISCIPLINAS;

    private static TurmaItem disciplinaSelecionada;
    private static TurmaItem cursoSelecionado;
    private static Prova provaSelecionada;
    private static AlunoProva alunoSelecionado;
    private static boolean voltarParaListaDeAlunos;

    private Long matriculaProfessor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        matriculaProfessor = SessaoUsuarioController.getIdUsuarioLogado();

        if (matriculaProfessor == null ||
                !"Professor".equalsIgnoreCase(SessaoUsuarioController.getTipoUsuario())) {
            mostrarErro("Professor não identificado", "Faça login novamente para acessar suas turmas.");
            return;
        }

        // Quando o professor volta da correção, retornamos para a lista de alunos
        // da mesma prova em vez de reiniciar toda a navegação.
        if (voltarParaListaDeAlunos && disciplinaSelecionada != null &&
                cursoSelecionado != null && provaSelecionada != null) {
            voltarParaListaDeAlunos = false;
            mostrarAlunos();
        } else {
            mostrarDisciplinas();
        }
    }

    /**
     * Limpa a navegação anterior quando o professor entra em Minhas Turmas pelo menu.
     */
    public static void limparContexto() {
        disciplinaSelecionada = null;
        cursoSelecionado = null;
        provaSelecionada = null;
        alunoSelecionado = null;
        voltarParaListaDeAlunos = false;
    }

    /**
     * Usado pela tela de correção para voltar exatamente à lista de alunos.
     */
    public static void voltarParaAlunosAoAbrir() {
        voltarParaListaDeAlunos = true;
    }

    public static Prova getProvaSelecionada() {
        return provaSelecionada;
    }

    public static AlunoProva getAlunoSelecionado() {
        return alunoSelecionado;
    }

    public static TurmaItem getCursoSelecionado() {
        return cursoSelecionado;
    }

    private void mostrarDisciplinas() {
        etapaAtual = Etapa.DISCIPLINAS;
        disciplinaSelecionada = null;
        cursoSelecionado = null;
        provaSelecionada = null;
        alunoSelecionado = null;

        configurarCabecalho(
                "Minhas Turmas",
                "Escolha uma disciplina para ver os cursos vinculados a ela."
        );

        try {
            List<TurmaItem> disciplinas = dao.listarDisciplinasProfessor(matriculaProfessor);
            limparLista();

            if (disciplinas.isEmpty()) {
                mostrarMensagemVazia("Você ainda não possui provas cadastradas em nenhuma disciplina.");
                return;
            }

            for (TurmaItem disciplina : disciplinas) {
                adicionarBotaoLista(disciplina.getNome(), () -> {
                    disciplinaSelecionada = disciplina;
                    mostrarCursos();
                });
            }
        } catch (RuntimeException erro) {
            tratarErroCarregamento("Não foi possível carregar as disciplinas.", erro);
        }
    }

    private void mostrarCursos() {
        if (disciplinaSelecionada == null) {
            mostrarDisciplinas();
            return;
        }

        etapaAtual = Etapa.CURSOS;
        cursoSelecionado = null;
        provaSelecionada = null;
        alunoSelecionado = null;

        configurarCabecalho(
                "Cursos",
                "Disciplina: " + disciplinaSelecionada.getNome()
        );

        try {
            List<TurmaItem> cursos = dao.listarCursosDaDisciplina(disciplinaSelecionada.getId());
            limparLista();

            if (cursos.isEmpty()) {
                mostrarMensagemVazia("Nenhum curso está vinculado a esta disciplina.");
                return;
            }

            for (TurmaItem curso : cursos) {
                adicionarBotaoLista(curso.getNome(), () -> {
                    cursoSelecionado = curso;
                    mostrarProvas();
                });
            }
        } catch (RuntimeException erro) {
            tratarErroCarregamento("Não foi possível carregar os cursos.", erro);
        }
    }

    private void mostrarProvas() {
        if (disciplinaSelecionada == null || cursoSelecionado == null) {
            mostrarCursos();
            return;
        }

        etapaAtual = Etapa.PROVAS;
        provaSelecionada = null;
        alunoSelecionado = null;

        configurarCabecalho(
                "Provas",
                cursoSelecionado.getNome() + "  •  " + disciplinaSelecionada.getNome()
        );

        try {
            List<Prova> provas = dao.listarProvasDaTurma(
                    matriculaProfessor,
                    disciplinaSelecionada.getId(),
                    cursoSelecionado.getId()
            );

            limparLista();

            if (provas.isEmpty()) {
                mostrarMensagemVazia("Nenhuma prova foi encontrada para esta disciplina e curso.");
                return;
            }

            for (Prova prova : provas) {
                adicionarBotaoLista(prova.getNome(), () -> {
                    provaSelecionada = prova;
                    mostrarAlunos();
                });
            }
        } catch (RuntimeException erro) {
            tratarErroCarregamento("Não foi possível carregar as provas.", erro);
        }
    }

    private void mostrarAlunos() {
        if (cursoSelecionado == null || provaSelecionada == null) {
            mostrarProvas();
            return;
        }

        etapaAtual = Etapa.ALUNOS;
        alunoSelecionado = null;

        configurarCabecalho(
                "Alunos que realizaram a prova",
                provaSelecionada.getNome() + "  •  " + cursoSelecionado.getNome()
        );

        try {
            List<AlunoProva> alunos = dao.listarAlunosQueRealizaramProva(
                    provaSelecionada.getId(),
                    cursoSelecionado.getId()
            );

            limparLista();

            if (alunos.isEmpty()) {
                mostrarMensagemVazia("Nenhum aluno deste curso enviou esta prova ainda.");
                return;
            }

            for (AlunoProva aluno : alunos) {
                adicionarBotaoLista(
                        aluno.getNome() + "  -  RA: " + aluno.getRa(),
                        () -> abrirRespostaDoAluno(aluno)
                );
            }
        } catch (RuntimeException erro) {
            tratarErroCarregamento("Não foi possível carregar os alunos.", erro);
        }
    }

    private void abrirRespostaDoAluno(AlunoProva aluno) {
        alunoSelecionado = aluno;

        try {
            App.setRoot("professorFluxo/resposta_aluno_professor");
        } catch (IOException erro) {
            tratarErroCarregamento("Não foi possível abrir a prova respondida.", erro);
        }
    }

    /**
     * Cria os botões das listas usando o mesmo estilo simples das listas de provas.
     */
    private void adicionarBotaoLista(String texto, Runnable acao) {
        Button botao = new Button(texto);
        botao.setMaxWidth(Double.MAX_VALUE);
        botao.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-padding: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 9px;" +
                "-fx-alignment: CENTER_LEFT;"
        );
        botao.setOnAction(event -> acao.run());

        HBox linha = new HBox(botao);
        linha.setPadding(new Insets(5, 15, 5, 15));
        HBox.setHgrow(botao, Priority.ALWAYS);

        vb_turmas_container.getChildren().add(linha);
    }

    private void configurarCabecalho(String titulo, String caminho) {
        if (lbl_titulo_turmas != null) {
            lbl_titulo_turmas.setText(titulo);
        }
        if (lbl_caminho_turmas != null) {
            lbl_caminho_turmas.setText(caminho);
        }
    }

    private void limparLista() {
        if (vb_turmas_container != null) {
            vb_turmas_container.getChildren().clear();
        }
    }

    private void mostrarMensagemVazia(String texto) {
        Label mensagem = new Label(texto);
        mensagem.setWrapText(true);
        mensagem.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px; -fx-padding: 20px;");
        vb_turmas_container.getChildren().add(mensagem);
    }

    private void tratarErroCarregamento(String cabecalho, Exception erro) {
        erro.printStackTrace();
        mostrarErro(cabecalho, erro.getMessage());
    }

    private void mostrarErro(String cabecalho, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro");
        alerta.setHeaderText(cabecalho);
        alerta.setContentText(mensagem == null ? "Erro desconhecido." : mensagem);
        alerta.showAndWait();
    }

    @FXML
    private void voltar() throws IOException {
        switch (etapaAtual) {
            case CURSOS:
                mostrarDisciplinas();
                break;
            case PROVAS:
                mostrarCursos();
                break;
            case ALUNOS:
                mostrarProvas();
                break;
            case DISCIPLINAS:
            default:
                App.setRoot("professorFluxo/MenuProf");
                break;
        }
    }

    private enum Etapa {
        DISCIPLINAS,
        CURSOS,
        PROVAS,
        ALUNOS
    }
}
