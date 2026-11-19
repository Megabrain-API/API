package org.example.controllers.profControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
 * 1. Disciplinas e Cursos (unificados em cards)
 * 2. Provas do Curso selecionado (ex: P1, P2)
 * 3. Alunos que realizaram a prova selecionada -> TelaCorrecao
 */
public class TurmasProfessorController implements Initializable {

    @FXML
    private Label lbl_subtitulo_topo;

    @FXML
    private Label lbl_titulo_turmas;

    @FXML
    private Label lbl_caminho_turmas;

    @FXML
    private VBox vb_turmas_container;

    @FXML
    private Button btn_voltar_turmas;

    private final TurmaProfessorDAO dao = new TurmaProfessorDAO();
    private Etapa etapaAtual = Etapa.DISCIPLINAS_E_CURSOS;

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

        // Quando o professor volta da correção, retorna diretamente para a lista de alunos da prova.
        if (voltarParaListaDeAlunos && disciplinaSelecionada != null &&
                cursoSelecionado != null && provaSelecionada != null) {
            voltarParaListaDeAlunos = false;
            mostrarAlunos();
        } else {
            mostrarDisciplinasECursos();
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

    /**
     * Tela 1: Exibe todas as disciplinas do professor e, dentro de cada card,
     * os cursos vinculados a ela (padrão visual MeuCursoAluno).
     */
    private void mostrarDisciplinasECursos() {
        etapaAtual = Etapa.DISCIPLINAS_E_CURSOS;
        disciplinaSelecionada = null;
        cursoSelecionado = null;
        provaSelecionada = null;
        alunoSelecionado = null;

        try {
            List<TurmaItem> disciplinas = dao.listarDisciplinasProfessor(matriculaProfessor);
            limparLista();

            if (disciplinas.isEmpty()) {
                configurarCabecalho(
                        "MINHAS TURMAS",
                        "Disciplinas e Cursos",
                        "0 disciplina(s) • 0 curso(s) vinculado(s)"
                );
                mostrarCardVazio("Você ainda não possui provas cadastradas em nenhuma disciplina.");
                return;
            }

            int totalCursos = 0;

            for (TurmaItem disciplina : disciplinas) {
                List<TurmaItem> cursos = dao.listarCursosDaDisciplina(disciplina.getId());
                totalCursos += cursos.size();

                VBox cardDisciplina = criarCardBase();

                // Cabeçalho do card da disciplina
                HBox topoCard = new HBox(12);
                topoCard.setAlignment(Pos.CENTER_LEFT);

                Label lblNomeDisciplina = new Label(disciplina.getNome());
                lblNomeDisciplina.setFont(Font.font("System", FontWeight.BOLD, 17));
                lblNomeDisciplina.setStyle("-fx-text-fill: #4A7C59;");

                Region espacador = new Region();
                HBox.setHgrow(espacador, Priority.ALWAYS);

                Label badgeCursos = criarBadge(
                        cursos.size() + (cursos.size() == 1 ? " curso" : " cursos"),
                        !cursos.isEmpty()
                );

                topoCard.getChildren().addAll(lblNomeDisciplina, espacador, badgeCursos);
                cardDisciplina.getChildren().addAll(topoCard, new Separator());

                if (cursos.isEmpty()) {
                    Label lblSemCursos = new Label("Nenhum curso vinculado a esta disciplina.");
                    lblSemCursos.setStyle("-fx-text-fill: #70757A; -fx-font-size: 13px;");
                    lblSemCursos.setPadding(new Insets(4, 0, 4, 0));
                    cardDisciplina.getChildren().add(lblSemCursos);
                } else {
                    VBox listaCursos = new VBox(10);
                    for (TurmaItem curso : cursos) {
                        listaCursos.getChildren().add(
                                criarLinhaItem(
                                        curso.getNome(),
                                        "Clique para visualizar as provas aplicadas neste curso",
                                        null,
                                        "Ver Provas",
                                        () -> {
                                            disciplinaSelecionada = disciplina;
                                            cursoSelecionado = curso;
                                            mostrarProvas();
                                        }
                                )
                        );
                    }
                    cardDisciplina.getChildren().add(listaCursos);
                }

                vb_turmas_container.getChildren().add(cardDisciplina);
            }

            configurarCabecalho(
                    "MINHAS TURMAS",
                    "Disciplinas e Cursos",
                    disciplinas.size() + " disciplina(s) • " + totalCursos + " curso(s) vinculado(s)"
            );
        } catch (RuntimeException erro) {
            tratarErroCarregamento("Não foi possível carregar as disciplinas e cursos.", erro);
        }
    }

    /**
     * Tela 2: Exibe as provas (ex: P1, P2) da disciplina e curso selecionados.
     */
    private void mostrarProvas() {
        if (disciplinaSelecionada == null || cursoSelecionado == null) {
            mostrarDisciplinasECursos();
            return;
        }

        etapaAtual = Etapa.PROVAS;
        provaSelecionada = null;
        alunoSelecionado = null;

        try {
            List<Prova> provas = dao.listarProvasDaTurma(
                    matriculaProfessor,
                    disciplinaSelecionada.getId(),
                    cursoSelecionado.getId()
            );

            limparLista();

            configurarCabecalho(
                    "DISCIPLINA: " + disciplinaSelecionada.getNome().toUpperCase(),
                    cursoSelecionado.getNome(),
                    provas.size() + " prova(s) encontrada(s) neste curso"
            );

            VBox cardProvas = criarCardBase();

            HBox topoCard = new HBox(12);
            topoCard.setAlignment(Pos.CENTER_LEFT);

            Label lblTituloSecao = new Label("Provas Aplicadas");
            lblTituloSecao.setFont(Font.font("System", FontWeight.BOLD, 17));
            lblTituloSecao.setStyle("-fx-text-fill: #4A7C59;");

            Region espacador = new Region();
            HBox.setHgrow(espacador, Priority.ALWAYS);

            Label badgeProvas = criarBadge(
                    provas.size() + (provas.size() == 1 ? " prova" : " provas"),
                    !provas.isEmpty()
            );

            topoCard.getChildren().addAll(lblTituloSecao, espacador, badgeProvas);
            cardProvas.getChildren().addAll(topoCard, new Separator());

            if (provas.isEmpty()) {
                Label lblVazio = new Label("Nenhuma prova foi encontrada para esta disciplina e curso.");
                lblVazio.setStyle("-fx-text-fill: #70757A; -fx-font-size: 13px;");
                lblVazio.setPadding(new Insets(4, 0, 4, 0));
                cardProvas.getChildren().add(lblVazio);
            } else {
                VBox listaProvas = new VBox(10);
                for (Prova prova : provas) {
                    listaProvas.getChildren().add(
                            criarLinhaItem(
                                    prova.getNome(),
                                    "Disciplina: " + disciplinaSelecionada.getNome(),
                                    null,
                                    "Ver Alunos",
                                    () -> {
                                        provaSelecionada = prova;
                                        mostrarAlunos();
                                    }
                            )
                    );
                }
                cardProvas.getChildren().add(listaProvas);
            }

            vb_turmas_container.getChildren().add(cardProvas);
        } catch (RuntimeException erro) {
            tratarErroCarregamento("Não foi possível carregar as provas.", erro);
        }
    }

    /**
     * Tela 3: Exibe os alunos que realizaram a prova selecionada no curso.
     */
    private void mostrarAlunos() {
        if (cursoSelecionado == null || provaSelecionada == null) {
            mostrarProvas();
            return;
        }

        etapaAtual = Etapa.ALUNOS;
        alunoSelecionado = null;

        try {
            List<AlunoProva> alunos = dao.listarAlunosQueRealizaramProva(
                    provaSelecionada.getId(),
                    cursoSelecionado.getId()
            );

            limparLista();

            String subtitulo = (disciplinaSelecionada != null ? disciplinaSelecionada.getNome().toUpperCase() + " • " : "")
                    + cursoSelecionado.getNome().toUpperCase();

            configurarCabecalho(
                    subtitulo,
                    provaSelecionada.getNome(),
                    alunos.size() + " aluno(s) realizaram esta prova"
            );

            VBox cardAlunos = criarCardBase();

            HBox topoCard = new HBox(12);
            topoCard.setAlignment(Pos.CENTER_LEFT);

            Label lblTituloSecao = new Label("Alunos que realizaram a prova");
            lblTituloSecao.setFont(Font.font("System", FontWeight.BOLD, 17));
            lblTituloSecao.setStyle("-fx-text-fill: #4A7C59;");

            Region espacador = new Region();
            HBox.setHgrow(espacador, Priority.ALWAYS);

            Label badgeAlunos = criarBadge(
                    alunos.size() + (alunos.size() == 1 ? " envio" : " envios"),
                    !alunos.isEmpty()
            );

            topoCard.getChildren().addAll(lblTituloSecao, espacador, badgeAlunos);
            cardAlunos.getChildren().addAll(topoCard, new Separator());

            if (alunos.isEmpty()) {
                Label lblVazio = new Label("Nenhum aluno deste curso enviou esta prova ainda.");
                lblVazio.setStyle("-fx-text-fill: #70757A; -fx-font-size: 13px;");
                lblVazio.setPadding(new Insets(4, 0, 4, 0));
                cardAlunos.getChildren().add(lblVazio);
            } else {
                VBox listaAlunos = new VBox(10);
                for (AlunoProva aluno : alunos) {
                    listaAlunos.getChildren().add(
                            criarLinhaItem(
                                    aluno.getNome(),
                                    "Prova enviada • Pronto para visualização e correção",
                                    "RA: " + aluno.getRa(),
                                    "Corrigir Prova",
                                    () -> abrirRespostaDoAluno(aluno)
                            )
                    );
                }
                cardAlunos.getChildren().add(listaAlunos);
            }

            vb_turmas_container.getChildren().add(cardAlunos);
        } catch (RuntimeException erro) {
            tratarErroCarregamento("Não foi possível carregar os alunos.", erro);
        }
    }

    private void abrirRespostaDoAluno(AlunoProva aluno) {
        alunoSelecionado = aluno;

        try {
            App.setRoot("professorFluxo/TelaCorrecao");
        } catch (IOException erro) {
            tratarErroCarregamento("Não foi possível abrir a prova respondida.", erro);
        }
    }

    // ==================== COMPONENTES VISUAIS (PADRÃO MEU CURSO ALUNO EM VERDE) ====================

    private VBox criarCardBase() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.96);" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: #D4DFD8;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);"
        );
        return card;
    }

    private HBox criarLinhaItem(String tituloPrincipal,
                                String subtitulo,
                                String textoBadgeDireita,
                                String textoBotao,
                                Runnable acao) {
        HBox linha = new HBox(14);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setPadding(new Insets(12, 16, 12, 16));
        linha.setStyle(
                "-fx-background-color: #F7FAF8;" +
                        "-fx-background-radius: 9px;" +
                        "-fx-border-color: #DCE7E0;" +
                        "-fx-border-radius: 9px;" +
                        "-fx-border-width: 1px;"
        );

        VBox colunaTexto = new VBox(3);
        Label lblTitulo = new Label(tituloPrincipal);
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTitulo.setStyle("-fx-text-fill: #1B1B1B;");

        Label lblSub = new Label(subtitulo);
        lblSub.setFont(Font.font("System", 12));
        lblSub.setStyle("-fx-text-fill: #66706A;");

        colunaTexto.getChildren().addAll(lblTitulo, lblSub);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        linha.getChildren().addAll(colunaTexto, espacador);

        if (textoBadgeDireita != null && !textoBadgeDireita.isBlank()) {
            Label badgeInfo = new Label(textoBadgeDireita);
            badgeInfo.setPadding(new Insets(5, 11, 5, 11));
            badgeInfo.setStyle(
                    "-fx-background-color: #E3EFE7;" +
                            "-fx-text-fill: #285A36;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 12px;" +
                            "-fx-background-radius: 6px;"
            );
            linha.getChildren().add(badgeInfo);
        }

        Button btnAcao = new Button(textoBotao);
        btnAcao.setPadding(new Insets(7, 16, 7, 16));
        String estiloNormal =
                "-fx-background-color: #4A7C59;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 7px;" +
                        "-fx-cursor: hand;";
        String estiloHover =
                "-fx-background-color: #3B6347;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 7px;" +
                        "-fx-cursor: hand;";

        btnAcao.setStyle(estiloNormal);
        btnAcao.setOnMouseEntered(e -> btnAcao.setStyle(estiloHover));
        btnAcao.setOnMouseExited(e -> btnAcao.setStyle(estiloNormal));
        btnAcao.setOnAction(event -> acao.run());

        linha.getChildren().add(btnAcao);
        return linha;
    }

    private Label criarBadge(String texto, boolean ativo) {
        Label badge = new Label(texto);
        badge.setPadding(new Insets(4, 12, 4, 12));
        if (ativo) {
            badge.setStyle(
                    "-fx-background-color: #DDEEE2;" +
                            "-fx-text-fill: #285A36;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 12px;" +
                            "-fx-background-radius: 12px;"
            );
        } else {
            badge.setStyle(
                    "-fx-background-color: #ECEFED;" +
                            "-fx-text-fill: #66706A;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 12px;" +
                            "-fx-background-radius: 12px;"
            );
        }
        return badge;
    }

    private void mostrarCardVazio(String texto) {
        VBox card = criarCardBase();
        Label mensagem = new Label(texto);
        mensagem.setWrapText(true);
        mensagem.setStyle("-fx-text-fill: #66706A; -fx-font-size: 14px;");
        card.getChildren().add(mensagem);
        vb_turmas_container.getChildren().add(card);
    }

    private void configurarCabecalho(String subtituloTopo, String titulo, String caminho) {
        if (lbl_subtitulo_topo != null) {
            lbl_subtitulo_topo.setText(subtituloTopo);
        }
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
            case PROVAS:
                mostrarDisciplinasECursos();
                break;
            case ALUNOS:
                mostrarProvas();
                break;
            case DISCIPLINAS_E_CURSOS:
            default:
                App.setRoot("professorFluxo/MenuProf");
                break;
        }
    }

    private enum Etapa {
        DISCIPLINAS_E_CURSOS,
        PROVAS,
        ALUNOS
    }
}