package org.example.dao;

import org.example.entity.profEntity.AlunoProva;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.Questao;
import org.example.entity.profEntity.RespostaAlunoProva;
import org.example.entity.profEntity.TurmaItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
 */
public class TurmaProfessorDAO {

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
                    AlunoProva aluno = new AlunoProva();
                    aluno.setRaAluno(rs.getLong("ra"));
                    aluno.setNomeAluno(rs.getString("nome"));
                    alunos.add(aluno);
                }
            }
        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao carregar alunos que realizaram a prova: " + erro.getMessage(), erro);
        }

        return alunos;
    }

    /**
     * Carrega a prova respondida pelo aluno e calcula a pontuação por questão:
     * - Múltipla escolha: tudo ou nada
     * - Checklist: proporcional aos acertos líquidos
     * - Correspondência: proporcional aos pares corretos
     * - Dissertativa: carrega a nota manual salva ou deixa pendente para avaliação
     */
    public List<RespostaAlunoProva> buscarRespostasDoAluno(int idProva,
                                                           long raAluno,
                                                           long matriculaProfessor) {
        List<RespostaAlunoProva> respostas = new ArrayList<>();

        String sqlQuestoes = "SELECT q.id, pq.ordem, q.tipo, q.pontos, q.enunciado, q.gabarito, " +
                "r.resposta_texto, r.correta AS correta_salva, r.nota AS nota_salva, " +
                "COALESCE(SUM(CASE WHEN alt.correta = TRUE THEN 1 ELSE 0 END), 0) AS quantidade_corretas, " +
                "COUNT(DISTINCT ci.id) AS quantidade_itens_correspondencia " +
                "FROM mg_provas p " +
                "INNER JOIN mg_prova_questao pq ON pq.id_prova = p.id " +
                "INNER JOIN mg_questoes q ON q.id = pq.id_questao " +
                "LEFT JOIN mg_resposta_aluno r ON r.id_prova = p.id " +
                "    AND r.id_questao = q.id AND r.ra_aluno = ? " +
                "LEFT JOIN mg_questao_alternativa alt ON alt.id_questao = q.id " +
                "LEFT JOIN mg_questao_correspondencia_item ci ON ci.id_questao = q.id " +
                "WHERE p.id = ? AND p.matricula_professor = ? " +
                "GROUP BY q.id, pq.ordem, q.tipo, q.pontos, q.enunciado, q.gabarito, r.resposta_texto, r.correta, r.nota " +
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
                    BigDecimal pontosBD = rs.getBigDecimal("pontos");
                    double pontosQuestao = pontosBD != null ? pontosBD.doubleValue() : 1.0;

                    int quantidadeCorretas = rs.getInt("quantidade_corretas");
                    int quantidadeItensCorrespondencia = rs.getInt("quantidade_itens_correspondencia");
                    String respostaAluno = valorOuVazio(rs.getString("resposta_texto"));
                    String gabarito = valorOuVazio(rs.getString("gabarito"));

                    resposta.setIdQuestao(idQuestao);
                    resposta.setOrdem(rs.getInt("ordem"));
                    resposta.setPontosQuestao(pontosQuestao);
                    resposta.setEnunciado(rs.getString("enunciado"));
                    resposta.setRespostaAluno(respostaAluno);
                    resposta.setGabarito(gabarito);

                    if ("Dissertativa".equalsIgnoreCase(tipoBanco)) {
                        resposta.setTipo(Questao.DISSERTATIVA);
                    } else if (quantidadeItensCorrespondencia > 0) {
                        resposta.setTipo(Questao.CORRESPONDENCIA);
                    } else if (quantidadeCorretas > 1) {
                        resposta.setTipo(Questao.CHECKLIST);
                    } else {
                        resposta.setTipo(Questao.MULTIPLA_ESCOLHA);
                    }

                    if (Questao.DISSERTATIVA.equals(resposta.getTipo())) {
                        Object corretaSalvaObj = rs.getObject("correta_salva");
                        BigDecimal notaSalva = rs.getBigDecimal("nota_salva");

                        if (corretaSalvaObj != null || notaSalva != null) {
                            double nota = notaSalva != null
                                    ? notaSalva.doubleValue()
                                    : (rs.getBoolean("correta_salva") ? pontosQuestao : 0.0);
                            resposta.setCorrigida(true);
                            resposta.setNota(arredondarDuasCasas(nota));
                            resposta.setCorreta(nota > 0.0);
                        } else {
                            resposta.setCorrigida(false);
                            resposta.setNota(0.0);
                            resposta.setCorreta(false);
                        }
                    } else {
                        resposta.setCorrigida(true);
                        avaliarQuestaoAutomatica(resposta, respostaAluno, gabarito, pontosQuestao);
                    }

                    if (!Questao.DISSERTATIVA.equals(resposta.getTipo())
                            && !Questao.CORRESPONDENCIA.equals(resposta.getTipo())) {
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

    private void avaliarQuestaoAutomatica(RespostaAlunoProva resposta,
                                          String respostaAluno,
                                          String gabarito,
                                          double pontosQuestao) {
        String tipo = resposta.getTipo();

        if (Questao.CHECKLIST.equals(tipo)) {
            Set<String> gabaritoSet = separarRespostas(gabarito);
            Set<String> alunoSet = separarRespostas(respostaAluno);

            if (gabaritoSet.isEmpty()) {
                boolean igual = normalizar(respostaAluno).equals(normalizar(gabarito));
                resposta.setCorreta(igual);
                resposta.setNota(igual ? pontosQuestao : 0.0);
                return;
            }

            int corretasMarcadas = 0;
            int incorretasMarcadas = 0;
            for (String item : alunoSet) {
                if (gabaritoSet.contains(item)) {
                    corretasMarcadas++;
                } else {
                    incorretasMarcadas++;
                }
            }

            int acertosLiquidos = Math.max(0, corretasMarcadas - incorretasMarcadas);
            double notaCalculada = ((double) acertosLiquidos / gabaritoSet.size()) * pontosQuestao;
            notaCalculada = arredondarDuasCasas(notaCalculada);

            resposta.setNota(notaCalculada);
            resposta.setCorreta(notaCalculada > 0.0);
            resposta.setDetalheCorrecao(
                    corretasMarcadas + " de " + gabaritoSet.size() + " corretas marcadas"
                            + (incorretasMarcadas > 0 ? " (" + incorretasMarcadas + " incorreta(s))" : "")
            );
            return;
        }

        if (Questao.CORRESPONDENCIA.equals(tipo)) {
            List<String> paresGabarito = separarItensOrdenados(gabarito);
            List<String> paresAluno = separarItensOrdenados(respostaAluno);

            if (paresGabarito.isEmpty()) {
                boolean igual = normalizar(respostaAluno).equals(normalizar(gabarito));
                resposta.setCorreta(igual);
                resposta.setNota(igual ? pontosQuestao : 0.0);
                return;
            }

            Set<String> gabaritoSet = new HashSet<>(paresGabarito);
            Set<String> alunoSet = new HashSet<>(paresAluno);

            int paresCorretos = 0;
            for (String par : alunoSet) {
                if (gabaritoSet.contains(par)) {
                    paresCorretos++;
                }
            }

            double notaCalculada = ((double) paresCorretos / paresGabarito.size()) * pontosQuestao;
            notaCalculada = arredondarDuasCasas(notaCalculada);

            resposta.setNota(notaCalculada);
            resposta.setCorreta(notaCalculada > 0.0);
            resposta.setDetalheCorrecao(
                    paresCorretos + " de " + paresGabarito.size() + " associações corretas"
            );
            return;
        }

        // Múltipla escolha: tudo ou nada
        boolean acertou = normalizar(respostaAluno).equals(normalizar(gabarito));
        resposta.setCorreta(acertou);
        resposta.setNota(acertou ? arredondarDuasCasas(pontosQuestao) : 0.0);
    }

    /**
     * Salva a nota individual de cada questão e atualiza mg_aluno_prova com a soma total
     * e status = 'CORRIGIDA', devolvendo a prova corrigida ao aluno.
     */
    public double salvarCorrecaoProva(int idProva,
                                      long raAluno,
                                      List<RespostaAlunoProva> respostas) {
        if (respostas == null || respostas.isEmpty()) {
            throw new RuntimeException("Não há questões para corrigir.");
        }

        BigDecimal notaTotal = BigDecimal.ZERO;
        for (RespostaAlunoProva r : respostas) {
            BigDecimal notaQuestao = BigDecimal.valueOf(r.getNota())
                    .setScale(2, RoundingMode.HALF_UP);
            notaTotal = notaTotal.add(notaQuestao);
        }
        notaTotal = notaTotal.setScale(2, RoundingMode.HALF_UP);

        String sqlUpdateResposta = "UPDATE mg_resposta_aluno " +
                "SET correta = ?, nota = ? " +
                "WHERE ra_aluno = ? AND id_prova = ? AND id_questao = ?";

        String sqlInsertRespostaVazia = "INSERT INTO mg_resposta_aluno " +
                "(ra_aluno, id_prova, id_questao, resposta_texto, correta, nota) " +
                "VALUES (?, ?, ?, '', ?, ?)";

        String sqlUpsertAlunoProva = "INSERT INTO mg_aluno_prova " +
                "(ra_aluno, id_prova, data_inicio, data_envio, status, nota_total) " +
                "VALUES (?, ?, NOW(), NOW(), 'CORRIGIDA', ?) " +
                "ON DUPLICATE KEY UPDATE status = 'CORRIGIDA', nota_total = VALUES(nota_total)";

        try (Connection conn = new ConexaoDAO().conectaBD()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateResposta);
                 PreparedStatement stmtInsert = conn.prepareStatement(sqlInsertRespostaVazia);
                 PreparedStatement stmtAlunoProva = conn.prepareStatement(sqlUpsertAlunoProva)) {

                for (RespostaAlunoProva r : respostas) {
                    BigDecimal notaQuestao = BigDecimal.valueOf(r.getNota())
                            .setScale(2, RoundingMode.HALF_UP);
                    boolean acertouAlgo = notaQuestao.compareTo(BigDecimal.ZERO) > 0;
                    r.setCorreta(acertouAlgo);
                    r.setCorrigida(true);

                    stmtUpdate.setBoolean(1, acertouAlgo);
                    stmtUpdate.setBigDecimal(2, notaQuestao);
                    stmtUpdate.setLong(3, raAluno);
                    stmtUpdate.setInt(4, idProva);
                    stmtUpdate.setLong(5, r.getIdQuestao());

                    int linhasAfetadas = stmtUpdate.executeUpdate();
                    if (linhasAfetadas == 0) {
                        stmtInsert.setLong(1, raAluno);
                        stmtInsert.setInt(2, idProva);
                        stmtInsert.setLong(3, r.getIdQuestao());
                        stmtInsert.setBoolean(4, acertouAlgo);
                        stmtInsert.setBigDecimal(5, notaQuestao);
                        stmtInsert.executeUpdate();
                    }
                }

                stmtAlunoProva.setLong(1, raAluno);
                stmtAlunoProva.setInt(2, idProva);
                stmtAlunoProva.setBigDecimal(3, notaTotal);
                stmtAlunoProva.executeUpdate();

                conn.commit();
                return notaTotal.doubleValue();
            } catch (SQLException erro) {
                conn.rollback();
                throw erro;
            }
        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao salvar correção da prova: " + erro.getMessage(), erro);
        }
    }

    private double arredondarDuasCasas(double valor) {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Set<String> separarRespostas(String valor) {
        Set<String> itens = new HashSet<>();

        if (valor == null || valor.trim().isEmpty()) {
            return itens;
        }

        for (String item : valor.split("\\s*\\|\\s*|\\r?\\n")) {
            String normalizado = normalizar(item);
            if (!normalizado.isEmpty()) {
                itens.add(normalizado);
            }
        }

        return itens;
    }

    private List<String> separarItensOrdenados(String valor) {
        List<String> itens = new ArrayList<>();

        if (valor == null || valor.trim().isEmpty()) {
            return itens;
        }

        for (String item : valor.split("\\s*\\|\\s*|\\r?\\n")) {
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