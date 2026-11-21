package org.example.controllers.profControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import org.example.dao.TurmaProfessorDAO.NotaProvaBoletim;
import org.example.entity.profEntity.AlunoProva;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.TurmaItem;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controla a navegação da tela Minhas Turmas.
 *
 * Fluxo 1 (Ver Provas):
 * 1. Disciplinas e Cursos -> 2. Provas do Curso -> 3. Alunos que realizaram -> TelaCorrecao
 *
 * Fluxo 2 (Ver Notas):
 * 1. Disciplinas e Cursos -> 2. Alunos matriculados no Curso (Nome e RA)
 * -> 3. Notas individuais + Estimativa de Média + Botão "Fechar Nota e Enviar ao Aluno"
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
    private final DecimalFormat formatoDecimal = new DecimalFormat(
            "0.00",
            DecimalFormatSymbols.getInstance(new Locale("pt", "BR"))
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        matriculaProfessor = SessaoUsuarioController.getIdUsuarioLogado();

        if (matriculaProfessor == null ||
                !"Professor".equalsIgnoreCase(SessaoUsuarioController.getTipoUsuario())) {
            mostrarErro("Professor não identificado", "Faça login novamente para acessar suas turmas.");
            return;
        }

        if (voltarParaListaDeAlunos && disciplinaSelecionada != null &&
                cursoSelecionado != null && provaSelecionada != null) {
            voltarParaListaDeAlunos = false;
            mostrarAlunos();
        } else {
            mostrarDisciplinasECursos();
        }
    }

    public static void limparContexto() {
        disciplinaSelecionada = null;
        cursoSelecionado = null;
        provaSelecionada = null;
        alunoSelecionado = null;
        voltarParaListaDeAlunos = false;
    }

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
     * os cursos vinculados com os botões "Ver Provas" e "Ver Notas".
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
                                criarLinhaCursoComProvasENotas(disciplina, curso)
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
     * Linha de curso na tela inicial contendo os botões "Ver Provas" e "Ver Notas".
     */
    private HBox criarLinhaCursoComProvasENotas(TurmaItem disciplina, TurmaItem curso) {
        HBox linha = new HBox(10);
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
        Label lblTitulo = new Label(curso.getNome());
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTitulo.setStyle("-fx-text-fill: #1B1B1B;");

        Label lblSub = new Label("Acompanhe as provas aplicadas ou consulte as notas e médias dos alunos");
        lblSub.setFont(Font.font("System", 12));
        lblSub.setStyle("-fx-text-fill: #66706A;");

        colunaTexto.getChildren().addAll(lblTitulo, lblSub);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        Button btnVerProvas = criarBotaoPrimario("Ver Provas", () -> {
            disciplinaSelecionada = disciplina;
            cursoSelecionado = curso;
            mostrarProvas();
        });

        Button btnVerNotas = criarBotaoSecundario("Ver Notas", () -> {
            disciplinaSelecionada = disciplina;
            cursoSelecionado = curso;
            mostrarAlunosParaNotas();
        });

        linha.getChildren().addAll(colunaTexto, espacador, btnVerProvas, btnVerNotas);
        return linha;
    }

    /**
     * Fluxo 2 - Etapa 1: Lista todos os alunos (Nome e RA) matriculados no curso selecionado.
     */
    private void mostrarAlunosParaNotas() {
        if (disciplinaSelecionada == null || cursoSelecionado == null) {
            mostrarDisciplinasECursos();
            return;
        }

        etapaAtual = Etapa.ALUNOS_NOTAS;
        alunoSelecionado = null;

        try {
            List<AlunoProva> alunos = dao.listarAlunosDoCurso(cursoSelecionado.getId());
            limparLista();

            configurarCabecalho(
                    "BOLETIM • " + disciplinaSelecionada.getNome().toUpperCase(),
                    cursoSelecionado.getNome(),
                    alunos.size() + " aluno(s) matriculado(s) neste curso"
            );

            VBox cardAlunos = criarCardBase();

            HBox topoCard = new HBox(12);
            topoCard.setAlignment(Pos.CENTER_LEFT);

            Label lblTituloSecao = new Label("Alunos Matriculados");
            lblTituloSecao.setFont(Font.font("System", FontWeight.BOLD, 17));
            lblTituloSecao.setStyle("-fx-text-fill: #4A7C59;");

            Region espacador = new Region();
            HBox.setHgrow(espacador, Priority.ALWAYS);

            Label badgeAlunos = criarBadge(
                    alunos.size() + (alunos.size() == 1 ? " aluno" : " alunos"),
                    !alunos.isEmpty()
            );

            topoCard.getChildren().addAll(lblTituloSecao, espacador, badgeAlunos);
            cardAlunos.getChildren().addAll(topoCard, new Separator());

            if (alunos.isEmpty()) {
                Label lblVazio = new Label("Nenhum aluno matriculado neste curso.");
                lblVazio.setStyle("-fx-text-fill: #70757A; -fx-font-size: 13px;");
                lblVazio.setPadding(new Insets(4, 0, 4, 0));
                cardAlunos.getChildren().add(lblVazio);
            } else {
                VBox listaAlunos = new VBox(10);
                for (AlunoProva aluno : alunos) {
                    listaAlunos.getChildren().add(
                            criarLinhaItem(
                                    aluno.getNome(),
                                    "Clique para visualizar as notas das provas e a estimativa de média",
                                    "RA: " + aluno.getRa(),
                                    "Ver Notas",
                                    () -> {
                                        alunoSelecionado = aluno;
                                        mostrarBoletimDoAluno();
                                    }
                            )
                    );
                }
                cardAlunos.getChildren().add(listaAlunos);
            }

            vb_turmas_container.getChildren().add(cardAlunos);
        } catch (RuntimeException erro) {
            tratarErroCarregamento("Não foi possível carregar os alunos do curso.", erro);
        }
    }

    /**
     * Fluxo 2 - Etapa 2: Exibe as notas individuais de cada prova da matéria (com seu peso),
     * o card de Estimativa de Média baseado nas provas corrigidas e o botão para
     * fechar a nota do semestre e enviar ao aluno.
     */
    private void mostrarBoletimDoAluno() {
        if (disciplinaSelecionada == null || cursoSelecionado == null || alunoSelecionado == null) {
            mostrarAlunosParaNotas();
            return;
        }

        etapaAtual = Etapa.BOLETIM_ALUNO;

        try {
            List<NotaProvaBoletim> notas = dao.listarBoletimAlunoNaDisciplina(
                    matriculaProfessor,
                    disciplinaSelecionada.getId(),
                    alunoSelecionado.getRa()
            );

            limparLista();

            configurarCabecalho(
                    disciplinaSelecionada.getNome().toUpperCase() + " • " + cursoSelecionado.getNome().toUpperCase(),
                    alunoSelecionado.getNome() + " (RA: " + alunoSelecionado.getRa() + ")",
                    "Desempenho individual por avaliação e estimativa de média parcial"
            );

            VBox cardBoletim = criarCardBase();

            HBox topoCard = new HBox(12);
            topoCard.setAlignment(Pos.CENTER_LEFT);

            Label lblTituloSecao = new Label("Notas por Avaliação");
            lblTituloSecao.setFont(Font.font("System", FontWeight.BOLD, 17));
            lblTituloSecao.setStyle("-fx-text-fill: #4A7C59;");

            Region espacador = new Region();
            HBox.setHgrow(espacador, Priority.ALWAYS);

            Label badgeTotalProvas = criarBadge(
                    notas.size() + (notas.size() == 1 ? " prova lançada" : " provas lançadas"),
                    !notas.isEmpty()
            );

            topoCard.getChildren().addAll(lblTituloSecao, espacador, badgeTotalProvas);
            cardBoletim.getChildren().addAll(topoCard, new Separator());

            double somaNotasPonderadas = 0.0;
            double somaPesosCorrigidas = 0.0;
            int qtdCorrigidas = 0;

            if (notas.isEmpty()) {
                Label lblVazio = new Label("Nenhuma prova foi lançada nesta disciplina ainda.");
                lblVazio.setStyle("-fx-text-fill: #70757A; -fx-font-size: 13px;");
                lblVazio.setPadding(new Insets(4, 0, 4, 0));
                cardBoletim.getChildren().add(lblVazio);
            } else {
                VBox listaProvas = new VBox(10);

                for (NotaProvaBoletim item : notas) {
                    String textoPeso = "Peso: " + formatarPeso(item.getPeso());
                    String subtitulo;
                    String textoNotaBadge;
                    boolean temNota = item.isCorrigida();

                    if (temNota) {
                        double pesoEfetivo = item.getPeso() > 0 ? item.getPeso() : 1.0;
                        somaNotasPonderadas += item.getNotaTotal() * pesoEfetivo;
                        somaPesosCorrigidas += pesoEfetivo;
                        qtdCorrigidas++;

                        subtitulo = "Prova corrigida • Valor máximo: "
                                + formatoDecimal.format(item.getPontosMaximos()) + " pts";
                        textoNotaBadge = "Nota: " + formatoDecimal.format(item.getNotaTotal())
                                + " / " + formatoDecimal.format(item.getPontosMaximos());
                    } else if ("FINALIZADA".equalsIgnoreCase(item.getStatus())
                            || "EXPIRADA".equalsIgnoreCase(item.getStatus())) {
                        subtitulo = "Enviada pelo aluno • Aguardando correção do professor";
                        textoNotaBadge = "Aguardando correção";
                    } else if ("EM_ANDAMENTO".equalsIgnoreCase(item.getStatus())) {
                        subtitulo = "Aluno realizando a prova no momento";
                        textoNotaBadge = "Em andamento";
                    } else {
                        subtitulo = "Avaliação ainda não realizada pelo aluno";
                        textoNotaBadge = "Sem envio";
                    }

                    listaProvas.getChildren().add(
                            criarLinhaNotaProva(item.getNomeProva(), subtitulo, textoPeso, textoNotaBadge, temNota)
                    );
                }

                cardBoletim.getChildren().add(listaProvas);
            }

            cardBoletim.getChildren().add(new Separator());

            // Bloco inferior: Estimativa de Média Semestral (baseada apenas nas provas já corrigidas)
            HBox boxEstimativa = new HBox(16);
            boxEstimativa.setAlignment(Pos.CENTER_LEFT);
            boxEstimativa.setPadding(new Insets(16, 20, 16, 20));
            boxEstimativa.setStyle(
                    "-fx-background-color: #EEF5F0;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-border-color: #BFD6C6;" +
                            "-fx-border-radius: 10px;" +
                            "-fx-border-width: 1px;"
            );

            VBox colunaDescricaoMedia = new VBox(4);
            Label lblTituloMedia = new Label("Estimativa de Média da Disciplina");
            lblTituloMedia.setFont(Font.font("System", FontWeight.BOLD, 15));
            lblTituloMedia.setStyle("-fx-text-fill: #285A36;");

            String explicacaoMedia = qtdCorrigidas > 0
                    ? "Calculada dinamicamente sobre " + qtdCorrigidas
                    + (qtdCorrigidas == 1 ? " prova corrigida" : " provas corrigidas")
                    + " (soma dos pesos aplicados: " + formatarPeso(somaPesosCorrigidas) + ")."
                    : "Nenhuma prova corrigida ainda para compor a estimativa de média.";

            Label lblDetalheMedia = new Label(explicacaoMedia);
            lblDetalheMedia.setWrapText(true);
            lblDetalheMedia.setStyle("-fx-text-fill: #466350; -fx-font-size: 12px;");

            colunaDescricaoMedia.getChildren().addAll(lblTituloMedia, lblDetalheMedia);

            Region espacadorMedia = new Region();
            HBox.setHgrow(espacadorMedia, Priority.ALWAYS);

            String valorMediaTexto = (qtdCorrigidas > 0 && somaPesosCorrigidas > 0)
                    ? formatoDecimal.format(somaNotasPonderadas / somaPesosCorrigidas)
                    : "—";

            Label lblValorMedia = new Label(valorMediaTexto);
            lblValorMedia.setPadding(new Insets(8, 18, 8, 18));
            lblValorMedia.setFont(Font.font("System", FontWeight.BOLD, 20));
            lblValorMedia.setStyle(
                    "-fx-background-color: #4A7C59;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 8px;"
            );

            boxEstimativa.getChildren().addAll(colunaDescricaoMedia, espacadorMedia, lblValorMedia);
            cardBoletim.getChildren().add(boxEstimativa);

            // Barra de ação para fechar nota do semestre e enviar ao aluno
            HBox barraEnvioAluno = new HBox(12);
            barraEnvioAluno.setAlignment(Pos.CENTER_RIGHT);
            barraEnvioAluno.setPadding(new Insets(4, 0, 0, 0));

            final int totalCorrigidasFinal = qtdCorrigidas;
            final String mediaFinalTexto = valorMediaTexto;

            Button btnEnviarAluno = criarBotaoPrimario("Fechar Nota e Enviar ao Aluno", () -> {
                confirmarEEnviarNotasAluno(totalCorrigidasFinal, mediaFinalTexto);
            });
            btnEnviarAluno.setDisable(qtdCorrigidas == 0);

            barraEnvioAluno.getChildren().add(btnEnviarAluno);
            cardBoletim.getChildren().add(barraEnvioAluno);

            vb_turmas_container.getChildren().add(cardBoletim);
        } catch (RuntimeException erro) {
            tratarErroCarregamento("Não foi possível carregar o boletim do aluno.", erro);
        }
    }

    private void confirmarEEnviarNotasAluno(int qtdProvasCorrigidas, String mediaCalculada) {
        if (qtdProvasCorrigidas <= 0) {
            mostrarErro("Nenhuma prova corrigida", "Corrija ao menos uma avaliação antes de enviar ao aluno.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Fechar Nota e Enviar ao Aluno");
        confirmacao.setHeaderText("Confirmar envio das notas de " + alunoSelecionado.getNome() + "?");
        confirmacao.setContentText(
                "Disciplina: " + disciplinaSelecionada.getNome() + "\n" +
                        "Provas corrigidas consolidadas: " + qtdProvasCorrigidas + "\n" +
                        "Média calculada: " + mediaCalculada
        );

        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            try {
                int enviadas = dao.fecharNotasEEnviarParaAluno(
                        matriculaProfessor,
                        disciplinaSelecionada.getId(),
                        alunoSelecionado.getRa()
                );

                Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                sucesso.setTitle("Notas Enviadas");
                sucesso.setHeaderText("Notas enviadas com sucesso ao aluno!");
                sucesso.setContentText(enviadas + " avaliação(ões) consolidada(s) com sucesso.");
                sucesso.showAndWait();

                mostrarBoletimDoAluno();
            } catch (RuntimeException erro) {
                tratarErroCarregamento("Não foi possível enviar as notas ao aluno.", erro);
            }
        }
    }

    private HBox criarLinhaNotaProva(String nomeProva,
                                     String subtitulo,
                                     String textoPeso,
                                     String textoNota,
                                     boolean temNota) {
        HBox linha = new HBox(12);
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
        Label lblTitulo = new Label(nomeProva);
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTitulo.setStyle("-fx-text-fill: #1B1B1B;");

        Label lblSub = new Label(subtitulo);
        lblSub.setFont(Font.font("System", 12));
        lblSub.setStyle("-fx-text-fill: #66706A;");

        colunaTexto.getChildren().addAll(lblTitulo, lblSub);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        Label badgePeso = new Label(textoPeso);
        badgePeso.setPadding(new Insets(5, 11, 5, 11));
        badgePeso.setStyle(
                "-fx-background-color: #E8ECEA;" +
                        "-fx-text-fill: #37413B;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 6px;"
        );

        Label badgeNota = new Label(textoNota);
        badgeNota.setPadding(new Insets(5, 12, 5, 12));
        badgeNota.setStyle(
                temNota
                        ? "-fx-background-color: #DDEEE2; -fx-text-fill: #285A36; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 6px;"
                        : "-fx-background-color: #FFF4E5; -fx-text-fill: #8A5300; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 6px;"
        );

        linha.getChildren().addAll(colunaTexto, espacador, badgePeso, badgeNota);
        return linha;
    }

    private String formatarPeso(double peso) {
        if (peso == Math.floor(peso)) {
            return String.valueOf((int) peso);
        }
        return formatoDecimal.format(peso);
    }

    /**
     * Fluxo 1 - Tela 2: Exibe as provas (ex: P1, P2) da disciplina e curso selecionados.
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
                    String pesoInfo = prova.getPeso() != null
                            ? "Peso: " + formatarPeso(prova.getPeso())
                            : null;
                    listaProvas.getChildren().add(
                            criarLinhaItem(
                                    prova.getNome(),
                                    "Disciplina: " + disciplinaSelecionada.getNome(),
                                    pesoInfo,
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
     * Fluxo 1 - Tela 3: Exibe os alunos que realizaram a prova selecionada no curso.
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

    // ==================== COMPONENTES VISUAIS ====================

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

        linha.getChildren().add(criarBotaoPrimario(textoBotao, acao));
        return linha;
    }

    private Button criarBotaoPrimario(String texto, Runnable acao) {
        Button btnAcao = new Button(texto);
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
        return btnAcao;
    }

    private Button criarBotaoSecundario(String texto, Runnable acao) {
        Button btnAcao = new Button(texto);
        btnAcao.setPadding(new Insets(7, 16, 7, 16));
        String estiloNormal =
                "-fx-background-color: #EEF5F0;" +
                        "-fx-text-fill: #285A36;" +
                        "-fx-border-color: #BFD6C6;" +
                        "-fx-border-radius: 7px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 7px;" +
                        "-fx-cursor: hand;";
        String estiloHover =
                "-fx-background-color: #DCECE1;" +
                        "-fx-text-fill: #1F472A;" +
                        "-fx-border-color: #9EC2A8;" +
                        "-fx-border-radius: 7px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 7px;" +
                        "-fx-cursor: hand;";

        btnAcao.setStyle(estiloNormal);
        btnAcao.setOnMouseEntered(e -> btnAcao.setStyle(estiloHover));
        btnAcao.setOnMouseExited(e -> btnAcao.setStyle(estiloNormal));
        btnAcao.setOnAction(event -> acao.run());
        return btnAcao;
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
            case ALUNOS_NOTAS:
                mostrarDisciplinasECursos();
                break;
            case ALUNOS:
                mostrarProvas();
                break;
            case BOLETIM_ALUNO:
                mostrarAlunosParaNotas();
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
        ALUNOS,
        ALUNOS_NOTAS,
        BOLETIM_ALUNO
    }
}