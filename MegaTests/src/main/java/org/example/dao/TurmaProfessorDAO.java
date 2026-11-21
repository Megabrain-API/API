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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Consultas usadas pela área "Minhas Turmas" do professor.
 */
public class TurmaProfessorDAO {

    /**
     * DTO com o resumo de cada prova de um aluno na disciplina (nota, peso e status).
     */
    public static class NotaProvaBoletim {
        private final int idProva;
        private final String nomeProva;
        private final double peso;
        private final Double notaTotal;
        private final double pontosMaximos;
        private final String status;

        public NotaProvaBoletim(int idProva,
                                String nomeProva,
                                double peso,
                                Double notaTotal,
                                double pontosMaximos,
                                String status) {
            this.idProva = idProva;
            this.nomeProva = nomeProva;
            this.peso = peso;
            this.notaTotal = notaTotal;
            this.pontosMaximos = pontosMaximos;
            this.status = status;
        }

        public int getIdProva() {
            return idProva;
        }

        public String getNomeProva() {
            return nomeProva;
        }

        public double getPeso() {
            return peso;
        }

        public Double getNotaTotal() {
            return notaTotal;
        }

        public double getPontosMaximos() {
            return pontosMaximos;
        }

        public String getStatus() {
            return status;
        }

        public boolean isCorrigida() {
            return "CORRIGIDA".equalsIgnoreCase(status) && notaTotal != null;
        }
    }

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

        String sql = "SELECT p.id, p.nome, p.peso, d.nome AS disciplina " +
                "FROM mg_provas p " +
                "INNER JOIN mg_disciplina d ON d.id = p.id_disciplina " +
                "WHERE p.matricula_professor = ? " +
                "AND p.id_disciplina = ? " +
                "AND EXISTS (" +
                "    SELECT 1 FROM mg_curso_disciplina cd " +
                "    WHERE cd.id_curso = ? AND cd.id_disciplina = p.id_disciplina" +
                ") " +
                "ORDER BY p.id";

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
                    prova.setPeso(rs.getDouble("peso"));
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
     * Lista todos os alunos matriculados no curso selecionado (usado no fluxo "Ver Notas").
     */
    public List<AlunoProva> listarAlunosDoCurso(long idCurso) {
        List<AlunoProva> alunos = new ArrayList<>();

        String sql = "SELECT ra, nome " +
                "FROM mg_aluno " +
                "WHERE id_curso = ? " +
                "ORDER BY nome";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idCurso);

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
                    "Erro ao carregar alunos do curso: " + erro.getMessage(), erro);
        }

        return alunos;
    }

    /**
     * Busca todas as provas lançadas pelo professor naquela disciplina, trazendo
     * o peso de cada prova e a nota obtida pelo aluno (caso já corrigida).
     */
    public List<NotaProvaBoletim> listarBoletimAlunoNaDisciplina(long matriculaProfessor,
                                                                 long idDisciplina,
                                                                 long raAluno) {
        List<NotaProvaBoletim> boletim = new ArrayList<>();

        String sql = "SELECT p.id, p.nome, COALESCE(p.peso, 1.0) AS peso, " +
                "ap.nota_total, ap.status, " +
                "COALESCE((SELECT SUM(q.pontos) FROM mg_prova_questao pq " +
                "          INNER JOIN mg_questoes q ON q.id = pq.id_questao " +
                "          WHERE pq.id_prova = p.id), 0) AS pontos_maximos " +
                "FROM mg_provas p " +
                "LEFT JOIN mg_aluno_prova ap ON ap.id_prova = p.id AND ap.ra_aluno = ? " +
                "WHERE p.matricula_professor = ? " +
                "AND p.id_disciplina = ? " +
                "ORDER BY p.id";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, raAluno);
            stmt.setLong(2, matriculaProfessor);
            stmt.setLong(3, idDisciplina);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal notaBD = rs.getBigDecimal("nota_total");
                    Double notaTotal = notaBD != null ? notaBD.doubleValue() : null;

                    boletim.add(new NotaProvaBoletim(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getDouble("peso"),
                            notaTotal,
                            rs.getDouble("pontos_maximos"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao carregar notas do aluno na disciplina: " + erro.getMessage(), erro);
        }

        return boletim;
    }

    /**
     * Garante que todas as avaliações já corrigidas do aluno nesta disciplina estejam
     * com status = 'CORRIGIDA' e nota_total consolidada em mg_aluno_prova,
     * além de gravar a média final em mg_media_final para exibição na área "Ver Curso" do aluno.
     */
    public int fecharNotasEEnviarParaAluno(long matriculaProfessor,
                                           long idDisciplina,
                                           long raAluno) {
        List<NotaProvaBoletim> boletim = listarBoletimAlunoNaDisciplina(
                matriculaProfessor, idDisciplina, raAluno
        );

        String sqlConsolidar = "UPDATE mg_aluno_prova " +
                "SET status = 'CORRIGIDA', nota_total = ? " +
                "WHERE ra_aluno = ? AND id_prova = ?";

        String sqlUpsertMedia = "INSERT INTO mg_media_final (ra_aluno, id_disciplina, media_final, data_fechamento) " +
                "VALUES (?, ?, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE media_final = VALUES(media_final), data_fechamento = NOW()";

        int provasEnviadas = 0;
        double somaNotasPonderadas = 0.0;
        double somaPesos = 0.0;

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sqlConsolidar);
             PreparedStatement stmtMedia = conn.prepareStatement(sqlUpsertMedia)) {

            conn.setAutoCommit(false);

            try {
                for (NotaProvaBoletim item : boletim) {
                    if (item.isCorrigida()) {
                        double pesoEfetivo = item.getPeso() > 0 ? item.getPeso() : 1.0;
                        somaNotasPonderadas += item.getNotaTotal() * pesoEfetivo;
                        somaPesos += pesoEfetivo;

                        BigDecimal notaConsolidada = BigDecimal.valueOf(item.getNotaTotal())
                                .setScale(2, RoundingMode.HALF_UP);

                        stmt.setBigDecimal(1, notaConsolidada);
                        stmt.setLong(2, raAluno);
                        stmt.setInt(3, item.getIdProva());

                        provasEnviadas += stmt.executeUpdate();
                    }
                }

                if (provasEnviadas > 0 && somaPesos > 0) {
                    BigDecimal mediaFinal = BigDecimal.valueOf(somaNotasPonderadas / somaPesos)
                            .setScale(2, RoundingMode.HALF_UP);

                    stmtMedia.setLong(1, raAluno);
                    stmtMedia.setLong(2, idDisciplina);
                    stmtMedia.setBigDecimal(3, mediaFinal);
                    stmtMedia.executeUpdate();
                }

                conn.commit();
                return provasEnviadas;
            } catch (SQLException erro) {
                conn.rollback();
                throw erro;
            }
        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao enviar notas consolidadas para o aluno: " + erro.getMessage(), erro);
        }
    }

    public List<AlunoProva> listarAlunosQueRealizaramProva(int idProva, long idCurso) {
        List<AlunoProva> alunos = new ArrayList<>();

        String sql = "SELECT DISTINCT a.ra, a.nome, ap.status, ap.nota_total " +
                "FROM mg_resposta_aluno r " +
                "INNER JOIN mg_aluno a ON a.ra = r.ra_aluno " +
                "LEFT JOIN mg_aluno_prova ap ON ap.ra_aluno = a.ra AND ap.id_prova = r.id_prova " +
                "WHERE r.id_prova = ? AND a.id_curso = ? " +
                "ORDER BY CASE WHEN ap.status = 'CORRIGIDA' THEN 1 ELSE 0 END, a.nome";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProva);
            stmt.setLong(2, idCurso);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AlunoProva aluno = new AlunoProva();
                    aluno.setRaAluno(rs.getLong("ra"));
                    aluno.setNomeAluno(rs.getString("nome"));
                    aluno.setStatus(rs.getString("status"));

                    BigDecimal notaBD = rs.getBigDecimal("nota_total");
                    aluno.setNotaTotal(notaBD != null ? notaBD.doubleValue() : null);

                    alunos.add(aluno);
                }
            }
        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao carregar alunos que realizaram a prova: " + erro.getMessage(), erro);
        }

        return alunos;
    }

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

        String sqlItensCorrespondencia =
                "SELECT ordem, texto " +
                        "FROM mg_questao_correspondencia_item " +
                        "WHERE id_questao = ? " +
                        "ORDER BY ordem";

        String sqlOpcoesCorrespondencia =
                "SELECT texto, numero_correto " +
                        "FROM mg_questao_correspondencia_alternativa " +
                        "WHERE id_questao = ? " +
                        "ORDER BY id";

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

                    if (quantidadeItensCorrespondencia > 0) {
                        resposta.setTipo(Questao.CORRESPONDENCIA);
                    } else if ("Dissertativa".equalsIgnoreCase(tipoBanco)) {
                        resposta.setTipo(Questao.DISSERTATIVA);
                    } else if (quantidadeCorretas > 1) {
                        resposta.setTipo(Questao.CHECKLIST);
                    } else {
                        resposta.setTipo(Questao.MULTIPLA_ESCOLHA);
                    }

                    if (Questao.CORRESPONDENCIA.equals(resposta.getTipo())) {
                        Map<Integer, String> itensPorOrdem = new LinkedHashMap<>();

                        try (PreparedStatement stmtItens = conn.prepareStatement(sqlItensCorrespondencia)) {
                            stmtItens.setLong(1, idQuestao);
                            try (ResultSet rsItens = stmtItens.executeQuery()) {
                                while (rsItens.next()) {
                                    int ordemItem = rsItens.getInt("ordem");
                                    String textoItem = rsItens.getString("texto");
                                    itensPorOrdem.put(ordemItem, textoItem);
                                    resposta.getItensCorrespondencia().add(textoItem);
                                }
                            }
                        }

                        Map<String, Integer> numeroCorretoPorOpcao = new LinkedHashMap<>();
                        try (PreparedStatement stmtOpcoes = conn.prepareStatement(sqlOpcoesCorrespondencia)) {
                            stmtOpcoes.setLong(1, idQuestao);
                            try (ResultSet rsOpcoes = stmtOpcoes.executeQuery()) {
                                while (rsOpcoes.next()) {
                                    String opcao = rsOpcoes.getString("texto");
                                    int numCorreto = rsOpcoes.getInt("numero_correto");
                                    numeroCorretoPorOpcao.put(opcao, numCorreto);
                                }
                            }
                        }

                        resposta.setCorrigida(true);
                        avaliarCorrespondencia(
                                resposta,
                                respostaAluno,
                                gabarito,
                                itensPorOrdem,
                                numeroCorretoPorOpcao,
                                pontosQuestao
                        );
                    } else if (Questao.DISSERTATIVA.equals(resposta.getTipo())) {
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

    private void avaliarCorrespondencia(RespostaAlunoProva resposta,
                                        String respostaAluno,
                                        String gabaritoBruto,
                                        Map<Integer, String> itensPorOrdem,
                                        Map<String, Integer> numeroCorretoPorOpcao,
                                        double pontosQuestao) {
        Map<String, String> numeroEsperadoPorOpcao = new LinkedHashMap<>();
        Map<String, String> textoEsperadoPorOpcao = new LinkedHashMap<>();
        List<String> gabaritoLegivel = new ArrayList<>();

        if (!numeroCorretoPorOpcao.isEmpty()) {
            for (Map.Entry<String, Integer> entry : numeroCorretoPorOpcao.entrySet()) {
                String opcao = entry.getKey();
                int numCorreto = entry.getValue();
                String nomeItem = itensPorOrdem.getOrDefault(numCorreto, String.valueOf(numCorreto));

                String chaveOpcao = normalizar(opcao);
                numeroEsperadoPorOpcao.put(chaveOpcao, String.valueOf(numCorreto));
                textoEsperadoPorOpcao.put(chaveOpcao, normalizar(nomeItem));
                gabaritoLegivel.add(opcao + " = " + numCorreto + " (" + nomeItem + ")");
            }
        } else if (!gabaritoBruto.isEmpty()) {
            Map<String, Integer> ordemPorTextoItem = new LinkedHashMap<>();
            for (Map.Entry<Integer, String> item : itensPorOrdem.entrySet()) {
                ordemPorTextoItem.put(normalizar(item.getValue()), item.getKey());
            }

            for (String par : gabaritoBruto.split("\\s*\\|\\s*|\\r?\\n")) {
                String[] partes = par.split("=", 2);
                if (partes.length == 2) {
                    String opcao = partes[0].trim();
                    String valorGab = partes[1].trim();
                    String chaveOpcao = normalizar(opcao);

                    Integer numEncontrado = ordemPorTextoItem.get(normalizar(valorGab));
                    if (numEncontrado == null) {
                        try {
                            numEncontrado = Integer.parseInt(valorGab);
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    if (numEncontrado != null) {
                        String nomeItem = itensPorOrdem.getOrDefault(numEncontrado, valorGab);
                        numeroEsperadoPorOpcao.put(chaveOpcao, String.valueOf(numEncontrado));
                        textoEsperadoPorOpcao.put(chaveOpcao, normalizar(nomeItem));
                        gabaritoLegivel.add(opcao + " = " + numEncontrado + " (" + nomeItem + ")");
                    } else {
                        textoEsperadoPorOpcao.put(chaveOpcao, normalizar(valorGab));
                        gabaritoLegivel.add(opcao + " = " + valorGab);
                    }
                }
            }
        }

        List<String> alunoLegivel = new ArrayList<>();
        int totalPares = Math.max(numeroEsperadoPorOpcao.size(), textoEsperadoPorOpcao.size());
        int paresCorretos = 0;

        if (!respostaAluno.isEmpty()) {
            for (String par : respostaAluno.split("\\s*\\|\\s*|\\r?\\n")) {
                String[] partes = par.split("=", 2);
                if (partes.length == 2) {
                    String opcao = partes[0].trim();
                    String valorAluno = partes[1].trim();
                    String chaveOpcao = normalizar(opcao);
                    String valorAlunoNorm = normalizar(valorAluno);

                    String numEsperado = numeroEsperadoPorOpcao.get(chaveOpcao);
                    String textoEsperado = textoEsperadoPorOpcao.get(chaveOpcao);

                    boolean acertouPar = (numEsperado != null && valorAlunoNorm.equals(numEsperado))
                            || (textoEsperado != null && valorAlunoNorm.equals(textoEsperado));

                    if (acertouPar) {
                        paresCorretos++;
                    }

                    try {
                        int numEscolhido = Integer.parseInt(valorAluno);
                        String nomeItem = itensPorOrdem.get(numEscolhido);
                        if (nomeItem != null) {
                            alunoLegivel.add(opcao + " = " + numEscolhido + " (" + nomeItem + ")");
                        } else {
                            alunoLegivel.add(opcao + " = " + valorAluno);
                        }
                    } catch (NumberFormatException e) {
                        alunoLegivel.add(opcao + " = " + valorAluno);
                    }
                }
            }
        }

        if (!gabaritoLegivel.isEmpty()) {
            resposta.setGabarito(String.join(" | ", gabaritoLegivel));
        }
        if (!alunoLegivel.isEmpty()) {
            resposta.setRespostaAluno(String.join(" | ", alunoLegivel));
        }

        double notaCalculada = totalPares > 0
                ? arredondarDuasCasas(((double) paresCorretos / totalPares) * pontosQuestao)
                : 0.0;

        resposta.setNota(notaCalculada);
        resposta.setCorreta(notaCalculada > 0.0);
        resposta.setDetalheCorrecao(
                paresCorretos + " de " + totalPares + " associações corretas"
        );
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
                double notaIgual = igual ? pontosQuestao : 0.0;
                resposta.setCorreta(igual);
                resposta.setNota(notaIgual);
                resposta.setNotaAutomatica(notaIgual);
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
            resposta.setNotaAutomatica(notaCalculada);
            resposta.setCorreta(notaCalculada > 0.0);
            resposta.setDetalheCorrecao(
                    corretasMarcadas + " de " + gabaritoSet.size() + " corretas marcadas"
                            + (incorretasMarcadas > 0 ? " (" + incorretasMarcadas + " incorreta(s))" : "")
            );
            return;
        }

        boolean acertou = normalizar(respostaAluno).equals(normalizar(gabarito));
        resposta.setCorreta(acertou);
        resposta.setNota(acertou ? arredondarDuasCasas(pontosQuestao) : 0.0);
    }

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