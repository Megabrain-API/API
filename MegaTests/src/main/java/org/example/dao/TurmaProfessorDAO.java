package org.example.dao;

import org.example.entity.profEntity.AlunoProva;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.Questao;
import org.example.entity.profEntity.RespostaAlunoProva;
import org.example.entity.profEntity.TurmaItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Consultas usadas pela área "Minhas Turmas" do professor.
 *
 * O banco atual não possui uma tabela professor_disciplina. Por isso, uma
 * disciplina pertence ao professor quando existe pelo menos uma prova criada
 * por ele naquela disciplina.
 */
public class TurmaProfessorDAO {

    /**
     * Lista as disciplinas nas quais o professor já criou alguma prova.
     */
    public List<TurmaItem> listarDisciplinasProfessor(long matriculaProfessor) {
        List<TurmaItem> disciplinas = new ArrayList<>();

        String sql = "SELECT DISTINCT d.id, d.nome " +
                "FROM mg_provas p " +
                "INNER JOIN mg_disciplina d ON d.id = p.id_disciplina " +
                "WHERE p.matricula_professor = ? " +
                "ORDER BY d.nome";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, matriculaProfessor);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    disciplinas.add(new TurmaItem(
                            rs.getLong("id"),
                            rs.getString("nome")
                    ));
                }
            }
        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao carregar disciplinas do professor: " + erro.getMessage(), erro);
        }

        return disciplinas;
    }

    /**
     * Lista todos os cursos que possuem a disciplina escolhida.
     */
    public List<TurmaItem> listarCursosDaDisciplina(long idDisciplina) {
        List<TurmaItem> cursos = new ArrayList<>();

        String sql = "SELECT c.id, c.nome_curso " +
                "FROM mg_curso_disciplina cd " +
                "INNER JOIN mg_cursos c ON c.id = cd.id_curso " +
                "WHERE cd.id_disciplina = ? " +
                "ORDER BY c.nome_curso";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idDisciplina);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cursos.add(new TurmaItem(
                            rs.getLong("id"),
                            rs.getString("nome_curso")
                    ));
                }
            }
        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao carregar cursos da disciplina: " + erro.getMessage(), erro);
        }

        return cursos;
    }

    /**
     * Lista as provas criadas pelo professor para a disciplina escolhida.
     * O EXISTS garante que o curso selecionado realmente possua essa disciplina.
     */
    public List<Prova> listarProvasDaTurma(long matriculaProfessor,
                                           long idDisciplina,
                                           long idCurso) {
        List<Prova> provas = new ArrayList<>();

        String sql = "SELECT p.id, p.nome, d.nome AS disciplina " +
                "FROM mg_provas p " +
                "INNER JOIN mg_disciplina d ON d.id = p.id_disciplina " +
                "WHERE p.matricula_professor = ? " +
                "AND p.id_disciplina = ? " +
                "AND EXISTS (" +
                "    SELECT 1 FROM mg_curso_disciplina cd " +
                "    WHERE cd.id_curso = ? AND cd.id_disciplina = p.id_disciplina" +
                ") " +
                "ORDER BY p.nome";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, matriculaProfessor);
            stmt.setLong(2, idDisciplina);
            stmt.setLong(3, idCurso);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prova prova = new Prova();
                    prova.setId(rs.getInt("id"));
                    prova.setNome(rs.getString("nome"));
                    prova.setDisciplina(rs.getString("disciplina"));
                    provas.add(prova);
                }
            }
        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao carregar provas da turma: " + erro.getMessage(), erro);
        }

        return provas;
    }

    /**
     * Mostra somente alunos do curso escolhido que já enviaram a prova.
     */
    public List<AlunoProva> listarAlunosQueRealizaramProva(int idProva, long idCurso) {
        List<AlunoProva> alunos = new ArrayList<>();

        String sql = "SELECT DISTINCT a.ra, a.nome " +
                "FROM mg_resposta_aluno r " +
                "INNER JOIN mg_aluno a ON a.ra = r.ra_aluno " +
                "WHERE r.id_prova = ? AND a.id_curso = ? " +
                "ORDER BY a.nome";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProva);
            stmt.setLong(2, idCurso);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alunos.add(new AlunoProva(
                            rs.getLong("ra"),
                            rs.getString("nome")
                    ));
                }
            }
        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao carregar alunos que realizaram a prova: " + erro.getMessage(), erro);
        }

        return alunos;
    }

    /**
     * Carrega a prova respondida pelo aluno e compara cada resposta com o gabarito.
     * A matrícula do professor também é validada para impedir que uma prova de
     * outro professor seja aberta por engano.
     */
    public List<RespostaAlunoProva> buscarRespostasDoAluno(int idProva,
                                                            long raAluno,
                                                            long matriculaProfessor) {
        List<RespostaAlunoProva> respostas = new ArrayList<>();

        String sqlQuestoes = "SELECT q.id, pq.ordem, q.tipo, q.enunciado, q.gabarito, " +
                "r.resposta_texto, " +
                "COALESCE(SUM(CASE WHEN alt.correta = TRUE THEN 1 ELSE 0 END), 0) AS quantidade_corretas " +
                "FROM mg_provas p " +
                "INNER JOIN mg_prova_questao pq ON pq.id_prova = p.id " +
                "INNER JOIN mg_questoes q ON q.id = pq.id_questao " +
                "LEFT JOIN mg_resposta_aluno r ON r.id_prova = p.id " +
                "    AND r.id_questao = q.id AND r.ra_aluno = ? " +
                "LEFT JOIN mg_questao_alternativa alt ON alt.id_questao = q.id " +
                "WHERE p.id = ? AND p.matricula_professor = ? " +
                "GROUP BY q.id, pq.ordem, q.tipo, q.enunciado, q.gabarito, r.resposta_texto " +
                "ORDER BY pq.ordem";

        String sqlAlternativas = "SELECT texto, correta " +
                "FROM mg_questao_alternativa " +
                "WHERE id_questao = ? ORDER BY ordem";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmtQuestoes = conn.prepareStatement(sqlQuestoes);
             PreparedStatement stmtAlternativas = conn.prepareStatement(sqlAlternativas)) {

            stmtQuestoes.setLong(1, raAluno);
            stmtQuestoes.setInt(2, idProva);
            stmtQuestoes.setLong(3, matriculaProfessor);

            try (ResultSet rs = stmtQuestoes.executeQuery()) {
                while (rs.next()) {
                    RespostaAlunoProva resposta = new RespostaAlunoProva();
                    long idQuestao = rs.getLong("id");
                    String tipoBanco = rs.getString("tipo");
                    int quantidadeCorretas = rs.getInt("quantidade_corretas");
                    String respostaAluno = valorOuVazio(rs.getString("resposta_texto"));
                    String gabarito = valorOuVazio(rs.getString("gabarito"));

                    resposta.setIdQuestao(idQuestao);
                    resposta.setOrdem(rs.getInt("ordem"));
                    resposta.setEnunciado(rs.getString("enunciado"));
                    resposta.setRespostaAluno(respostaAluno);
                    resposta.setGabarito(gabarito);

                    if ("Dissertativa".equalsIgnoreCase(tipoBanco)) {
                        resposta.setTipo(Questao.DISSERTATIVA);
                    } else if (quantidadeCorretas > 1) {
                        resposta.setTipo(Questao.CHECKLIST);
                    } else {
                        resposta.setTipo(Questao.MULTIPLA_ESCOLHA);
                    }

                    resposta.setCorreta(respostaEstaCorreta(
                            resposta.getTipo(), respostaAluno, gabarito));

                    // Para questões objetivas, carrega todas as alternativas para
                    // mostrar visualmente o que foi marcado e o que era correto.
                    if (!Questao.DISSERTATIVA.equals(resposta.getTipo())) {
                        Set<String> selecionadas = separarRespostas(respostaAluno);

                        stmtAlternativas.setLong(1, idQuestao);
                        try (ResultSet rsAlternativas = stmtAlternativas.executeQuery()) {
                            while (rsAlternativas.next()) {
                                String texto = rsAlternativas.getString("texto");
                                boolean correta = rsAlternativas.getBoolean("correta");
                                boolean selecionada = selecionadas.contains(normalizar(texto));

                                resposta.getAlternativas().add(
                                        new RespostaAlunoProva.AlternativaCorrecao(
                                                texto, correta, selecionada));
                            }
                        }
                    }

                    respostas.add(resposta);
                }
            }
        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao carregar respostas do aluno: " + erro.getMessage(), erro);
        }

        return respostas;
    }

    private boolean respostaEstaCorreta(String tipo, String respostaAluno, String gabarito) {
        if (Questao.CHECKLIST.equals(tipo)) {
            return separarRespostas(respostaAluno).equals(separarRespostas(gabarito));
        }

        return normalizar(respostaAluno).equals(normalizar(gabarito));
    }

    private Set<String> separarRespostas(String valor) {
        Set<String> itens = new HashSet<>();

        if (valor == null || valor.trim().isEmpty()) {
            return itens;
        }

        for (String item : valor.split("\\s*\\|\\s*")) {
            String normalizado = normalizar(item);
            if (!normalizado.isEmpty()) {
                itens.add(normalizado);
            }
        }

        return itens;
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private String valorOuVazio(String valor) {
        return valor == null ? "" : valor;
    }
}
