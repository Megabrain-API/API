package org.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.example.entity.alunoEntity.CursoAluno;
import org.example.entity.alunoEntity.MateriaAluno;
import org.example.entity.alunoEntity.ProvaCorrigidaAluno;
import org.example.entity.profEntity.Questao;
import org.example.entity.profEntity.RespostaAlunoProva;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;

public class AlunoDAO {

    /**
     * Cria o aluno e associa o cadastro ao curso escolhido no ComboBox.
     * O curso é buscado pelo nome para que a tela não precise conhecer o ID do banco.
     */
    public Long CriaAluno(String nome, String email, String senha, String curso) throws SQLException {
        String sqlBuscaCurso = "SELECT id FROM mg_cursos WHERE nome_curso = ?";
        String sqlAluno = "INSERT INTO mg_aluno (nome, email, senha, id_curso) VALUES (?, ?, ?, ?)";

        try (Connection conn = new ConexaoDAO().conectaBD()) {
            long idCurso;

            // Primeiro procura o ID do curso selecionado pelo aluno no cadastro.
            try (PreparedStatement stmtCurso = conn.prepareStatement(sqlBuscaCurso)) {
                stmtCurso.setString(1, curso);

                try (ResultSet rs = stmtCurso.executeQuery()) {
                    if (rs.next()) {
                        idCurso = rs.getLong("id");
                    } else {
                        throw new SQLException("Curso não encontrado no banco: " + curso);
                    }
                }
            }

            // Depois cria o aluno já ligado ao curso encontrado acima.
            try (PreparedStatement stmtAluno = conn.prepareStatement(
                    sqlAluno, PreparedStatement.RETURN_GENERATED_KEYS)) {

                stmtAluno.setString(1, nome);
                stmtAluno.setString(2, email);
                stmtAluno.setString(3, senha);
                stmtAluno.setLong(4, idCurso);

                int affectedRows = stmtAluno.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Falha ao criar o Aluno, nenhuma linha afetada.");
                }

                try (ResultSet generatedKeys = stmtAluno.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1); // Retorna o RA gerado pelo banco.
                    }
                    throw new SQLException("Falha ao criar Aluno, nenhum RA obtido.");
                }
            }
        }
    }

    /**
     * Busca os cursos cadastrados para preencher o ComboBox da tela de cadastro.
     */
    public List<String> listarCursos() {
        List<String> cursos = new ArrayList<>();
        String sql = "SELECT nome_curso FROM mg_cursos ORDER BY nome_curso";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cursos.add(rs.getString("nome_curso"));
            }
        } catch (SQLException erro) {
            erro.printStackTrace();
        }

        return cursos;
    }

    /**
     * Tenta autenticar um aluno no sistema.
     * Retorna o RA se o login for bem-sucedido ou null se falhar.
     */
    public Long autenticarAluno(String email, String senha) {
        String sql = "SELECT ra FROM mg_aluno WHERE email = ? AND senha = ?";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("ra");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Busca a data de início da prova para um determinado aluno na tabela mg_aluno_prova.
     * Se o aluno já tiver iniciado a prova anteriormente, retorna a data/hora registrada.
     */
    public LocalDateTime buscarDataInicioProva(long raAluno, int idProva) {
        String sql = "SELECT data_inicio FROM mg_aluno_prova WHERE ra_aluno = ? AND id_prova = ?";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, raAluno);
            stmt.setInt(2, idProva);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getTimestamp("data_inicio") != null) {
                    return rs.getTimestamp("data_inicio").toLocalDateTime();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Registra ou mantém a data de início da prova pelo aluno.
     * Garante a persistência do cronômetro entre trocas de tela ou reinícios.
     */
    public void registrarInicioProva(long raAluno, int idProva, LocalDateTime dataInicio) {
        String sql = "INSERT INTO mg_aluno_prova (ra_aluno, id_prova, data_inicio, status) " +
                "VALUES (?, ?, ?, 'EM_ANDAMENTO') " +
                "ON DUPLICATE KEY UPDATE data_inicio = COALESCE(data_inicio, VALUES(data_inicio))";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, raAluno);
            stmt.setInt(2, idProva);
            stmt.setTimestamp(3, Timestamp.valueOf(dataInicio));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Salva todas as respostas de uma prova dentro da mesma transação.
     * A chave do Map é o ID da questão e o valor é a resposta digitada/selecionada.
     */
    public void salvarRespostasProva(Long raAluno, int idProva, Map<Long, String> respostas) {

        // Verifica se o aluno já respondeu essa prova.
        String sqlVerificaResposta =
                "SELECT COUNT(*) FROM mg_resposta_aluno " +
                        "WHERE ra_aluno = ? AND id_prova = ?";


        String sqlResposta =
                "INSERT INTO mg_resposta_aluno " +
                        "(ra_aluno, id_prova, id_questao, resposta_texto) VALUES (?, ?, ?, ?)";

        if (raAluno == null) {
            throw new RuntimeException("Nenhum aluno autenticado para salvar as respostas.");
        }

        if (respostas == null || respostas.isEmpty()) {
            throw new RuntimeException("Nenhuma resposta foi informada para salvar.");
        }

        try (Connection conn = new ConexaoDAO().conectaBD()) {
            conn.setAutoCommit(false);

            try {
                // Antes de salvar, verifica se esse aluno já respondeu essa prova.
                try (PreparedStatement stmtVerifica =
                             conn.prepareStatement(sqlVerificaResposta)) {

                    stmtVerifica.setLong(1, raAluno);
                    stmtVerifica.setInt(2, idProva);

                    try (ResultSet rs = stmtVerifica.executeQuery()) {

                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new RuntimeException(
                                    "Esta prova já foi respondida."
                            );
                        }
                    }
                }

                // Insere uma linha para cada questão respondida pelo aluno.
                try (PreparedStatement stmtResposta = conn.prepareStatement(sqlResposta)) {
                    for (Map.Entry<Long, String> resposta : respostas.entrySet()) {
                        stmtResposta.setLong(1, raAluno);
                        stmtResposta.setInt(2, idProva);
                        stmtResposta.setLong(3, resposta.getKey());
                        stmtResposta.setString(4, resposta.getValue());
                        stmtResposta.addBatch();
                    }

                    stmtResposta.executeBatch();
                }

                conn.commit();
                System.out.println("Respostas do aluno salvas com sucesso!");
            } catch (SQLException erro) {
                conn.rollback();
                throw erro;
            }
        } catch (SQLException erro) {
            erro.printStackTrace();
            throw new RuntimeException("Erro ao salvar respostas do aluno: " + erro.getMessage(), erro);
        }
    }

    public CursoAluno buscarCursoComMateriasEProvasCorrigidas(long raAluno) {
        String sqlCurso = "SELECT c.id, c.nome_curso " +
                "FROM mg_aluno a " +
                "INNER JOIN mg_cursos c ON c.id = a.id_curso " +
                "WHERE a.ra = ?";

        String sqlMaterias = "SELECT d.id, d.nome " +
                "FROM mg_curso_disciplina cd " +
                "INNER JOIN mg_disciplina d ON d.id = cd.id_disciplina " +
                "WHERE cd.id_curso = ? " +
                "ORDER BY d.nome";

        String sqlProvasCorrigidas = "SELECT p.id AS id_prova, p.nome AS nome_prova, " +
                "d.id AS id_disciplina, d.nome AS nome_disciplina, " +
                "ap.nota_total, ap.data_envio, " +
                "COALESCE(SUM(q.pontos), 0) AS pontos_maximos " +
                "FROM mg_aluno_prova ap " +
                "INNER JOIN mg_provas p ON p.id = ap.id_prova " +
                "INNER JOIN mg_disciplina d ON d.id = p.id_disciplina " +
                "LEFT JOIN mg_prova_questao pq ON pq.id_prova = p.id " +
                "LEFT JOIN mg_questoes q ON q.id = pq.id_questao " +
                "WHERE ap.ra_aluno = ? AND ap.status = 'CORRIGIDA' " +
                "GROUP BY p.id, p.nome, d.id, d.nome, ap.nota_total, ap.data_envio " +
                "ORDER BY d.nome, ap.data_envio DESC, p.nome";

        try (Connection conn = new ConexaoDAO().conectaBD()) {
            CursoAluno cursoAluno;

            try (PreparedStatement stmtCurso = conn.prepareStatement(sqlCurso)) {
                stmtCurso.setLong(1, raAluno);
                try (ResultSet rs = stmtCurso.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    cursoAluno = new CursoAluno(
                            rs.getLong("id"),
                            rs.getString("nome_curso")
                    );
                }
            }

            Map<Long, MateriaAluno> mapaMaterias = new LinkedHashMap<>();

            try (PreparedStatement stmtMaterias = conn.prepareStatement(sqlMaterias)) {
                stmtMaterias.setLong(1, cursoAluno.getIdCurso());
                try (ResultSet rs = stmtMaterias.executeQuery()) {
                    while (rs.next()) {
                        long idDisc = rs.getLong("id");
                        MateriaAluno materia = new MateriaAluno(idDisc, rs.getString("nome"));
                        mapaMaterias.put(idDisc, materia);
                        cursoAluno.getMaterias().add(materia);
                    }
                }
            }

            try (PreparedStatement stmtProvas = conn.prepareStatement(sqlProvasCorrigidas)) {
                stmtProvas.setLong(1, raAluno);
                try (ResultSet rs = stmtProvas.executeQuery()) {
                    while (rs.next()) {
                        long idDisc = rs.getLong("id_disciplina");
                        String nomeDisc = rs.getString("nome_disciplina");

                        MateriaAluno materia = mapaMaterias.get(idDisc);
                        if (materia == null) {
                            materia = new MateriaAluno(idDisc, nomeDisc);
                            mapaMaterias.put(idDisc, materia);
                            cursoAluno.getMaterias().add(materia);
                        }

                        ProvaCorrigidaAluno prova = new ProvaCorrigidaAluno();
                        prova.setIdProva(rs.getInt("id_prova"));
                        prova.setNomeProva(rs.getString("nome_prova"));
                        prova.setIdDisciplina(idDisc);
                        prova.setNomeDisciplina(nomeDisc);

                        BigDecimal notaBD = rs.getBigDecimal("nota_total");
                        BigDecimal maxBD = rs.getBigDecimal("pontos_maximos");
                        prova.setNotaTotal(notaBD != null ? notaBD.doubleValue() : 0.0);
                        prova.setPontosMaximos(maxBD != null ? maxBD.doubleValue() : 0.0);

                        Timestamp tsEnvio = rs.getTimestamp("data_envio");
                        if (tsEnvio != null) {
                            prova.setDataEnvio(tsEnvio.toLocalDateTime());
                        }

                        materia.getProvasCorrigidas().add(prova);
                    }
                }
            }

            return cursoAluno;
        } catch (SQLException erro) {
            throw new RuntimeException(
                    "Erro ao carregar curso e provas corrigidas do aluno: " + erro.getMessage(), erro);
        }
    }

    /**
     * Busca todas as questões, respostas, gabarito e notas salvas pelo professor
     * para exibição na tela de prova corrigida do aluno.
     */
    public List<RespostaAlunoProva> buscarCorrecaoProvaDoAluno(int idProva, long raAluno) {
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
                "WHERE p.id = ? " +
                "GROUP BY q.id, pq.ordem, q.tipo, q.pontos, q.enunciado, q.gabarito, r.resposta_texto, r.correta, r.nota " +
                "ORDER BY pq.ordem";

        String sqlAlternativas = "SELECT texto, correta " +
                "FROM mg_questao_alternativa " +
                "WHERE id_questao = ? ORDER BY ordem";

        String sqlItensCorrespondencia = "SELECT texto " +
                "FROM mg_questao_correspondencia_item " +
                "WHERE id_questao = ? ORDER BY ordem";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmtQuestoes = conn.prepareStatement(sqlQuestoes);
             PreparedStatement stmtAlternativas = conn.prepareStatement(sqlAlternativas);
             PreparedStatement stmtItens = conn.prepareStatement(sqlItensCorrespondencia)) {

            stmtQuestoes.setLong(1, raAluno);
            stmtQuestoes.setInt(2, idProva);

            try (ResultSet rs = stmtQuestoes.executeQuery()) {
                while (rs.next()) {
                    RespostaAlunoProva resposta = new RespostaAlunoProva();
                    long idQuestao = rs.getLong("id");
                    String tipoBanco = rs.getString("tipo");
                    BigDecimal pontosBD = rs.getBigDecimal("pontos");
                    double pontosQuestao = pontosBD != null ? pontosBD.doubleValue() : 1.0;

                    int quantidadeCorretas = rs.getInt("quantidade_corretas");
                    int quantidadeItensCorrespondencia = rs.getInt("quantidade_itens_correspondencia");
                    String respostaAluno = rs.getString("resposta_texto") == null ? "" : rs.getString("resposta_texto");
                    String gabarito = rs.getString("gabarito") == null ? "" : rs.getString("gabarito");

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

                    BigDecimal notaSalva = rs.getBigDecimal("nota_salva");
                    double nota = notaSalva != null
                            ? notaSalva.setScale(2, RoundingMode.HALF_UP).doubleValue()
                            : (rs.getBoolean("correta_salva") ? pontosQuestao : 0.0);

                    resposta.setCorrigida(true);
                    resposta.setNota(nota);
                    resposta.setCorreta(nota > 0.0);

                    if (Questao.CORRESPONDENCIA.equals(resposta.getTipo())) {
                        stmtItens.setLong(1, idQuestao);
                        try (ResultSet rsItens = stmtItens.executeQuery()) {
                            while (rsItens.next()) {
                                resposta.getItensCorrespondencia().add(rsItens.getString("texto"));
                            }
                        }
                    } else if (!Questao.DISSERTATIVA.equals(resposta.getTipo())) {
                        Set<String> selecionadas = new HashSet<>();
                        for (String item : respostaAluno.split("\\s*\\|\\s*|\\r?\\n")) {
                            String norm = item.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
                            if (!norm.isEmpty()) {
                                selecionadas.add(norm);
                            }
                        }

                        stmtAlternativas.setLong(1, idQuestao);
                        try (ResultSet rsAlt = stmtAlternativas.executeQuery()) {
                            while (rsAlt.next()) {
                                String texto = rsAlt.getString("texto");
                                boolean correta = rsAlt.getBoolean("correta");
                                String normTexto = texto == null
                                        ? ""
                                        : texto.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
                                boolean selecionada = selecionadas.contains(normTexto);

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
                    "Erro ao carregar correção da prova: " + erro.getMessage(), erro);
        }

        return respostas;
    }
}