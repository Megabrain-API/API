package org.example.dao;

import org.example.controllers.profControllers.SessaoUsuarioController;
import org.example.entity.profEntity.Questao;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuestaoDAO {

    public long salvarQuestaoAvulsa(Questao q, String nomeDisciplina) {
        String sqlBuscaDisciplina = "SELECT id FROM mg_disciplina WHERE nome = ?";
        String sqlQuestao = "INSERT INTO mg_questoes (tipo, enunciado, gabarito, pontos, id_disciplina, matricula_professor) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlAlternativa = "INSERT INTO mg_questao_alternativa (id_questao, ordem, texto, correta) VALUES (?, ?, ?, ?)";
        String sqlItemCorrespondencia = "INSERT INTO mg_questao_correspondencia_item (id_questao, ordem, texto) VALUES (?, ?, ?)";
        String sqlOpcaoCorrespondencia = "INSERT INTO mg_questao_correspondencia_alternativa (id_questao, texto, numero_correto) VALUES (?, ?, ?)";

        try (Connection conn = new ConexaoDAO().conectaBD()) {
            conn.setAutoCommit(false);

            try {
                long idDisciplina;
                try (PreparedStatement stmtDisc = conn.prepareStatement(sqlBuscaDisciplina)) {
                    stmtDisc.setString(1, nomeDisciplina);
                    try (ResultSet rs = stmtDisc.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Disciplina não encontrada no banco: " + nomeDisciplina);
                        }
                        idDisciplina = rs.getLong("id");
                    }
                }

                Long matriculaProf = SessaoUsuarioController.getIdUsuarioLogado();
                if (matriculaProf == null) {
                    throw new SQLException("Nenhum professor autenticado na sessão atual.");
                }

                String tipoBanco = Questao.DISSERTATIVA.equalsIgnoreCase(q.getTipo())
                        ? "Dissertativa"
                        : "Objetiva";

                String gabarito = montarGabarito(q);
                if (gabarito == null) {
                    gabarito = "";
                }

                long idQuestao;
                try (PreparedStatement stmtQuestao = conn.prepareStatement(sqlQuestao, Statement.RETURN_GENERATED_KEYS)) {
                    stmtQuestao.setString(1, tipoBanco);
                    stmtQuestao.setString(2, q.getEnunciado());
                    stmtQuestao.setString(3, gabarito);
                    stmtQuestao.setDouble(4, q.getPontos() > 0 ? q.getPontos() : 1.0);
                    stmtQuestao.setLong(5, idDisciplina);
                    stmtQuestao.setLong(6, matriculaProf);
                    stmtQuestao.executeUpdate();

                    try (ResultSet rsKeys = stmtQuestao.getGeneratedKeys()) {
                        if (!rsKeys.next()) {
                            throw new SQLException("Não foi possível obter o ID da questão criada.");
                        }
                        idQuestao = rsKeys.getLong(1);
                    }
                }

                if (Questao.CORRESPONDENCIA.equals(q.getTipo())) {
                    for (int i = 0; i < q.getItensCorrespondencia().size(); i++) {
                        try (PreparedStatement stmt = conn.prepareStatement(sqlItemCorrespondencia)) {
                            stmt.setLong(1, idQuestao);
                            stmt.setInt(2, i + 1);
                            stmt.setString(3, q.getItensCorrespondencia().get(i));
                            stmt.executeUpdate();
                        }
                    }

                    for (int i = 0; i < q.getAlternativasCorrespondencia().size(); i++) {
                        try (PreparedStatement stmt = conn.prepareStatement(sqlOpcaoCorrespondencia)) {
                            stmt.setLong(1, idQuestao);
                            stmt.setString(2, q.getAlternativasCorrespondencia().get(i));
                            stmt.setInt(3, q.getGabaritoCorrespondencia().get(i));
                            stmt.executeUpdate();
                        }
                    }
                } else if (!Questao.DISSERTATIVA.equals(q.getTipo())) {
                    List<String> alternativas = q.getAlternativas();
                    List<Integer> respostasCorretas = q.getRespostasCorretas();

                    for (int i = 0; i < alternativas.size(); i++) {
                        boolean correta = respostasCorretas.contains(i);
                        try (PreparedStatement stmtAlt = conn.prepareStatement(sqlAlternativa)) {
                            stmtAlt.setLong(1, idQuestao);
                            stmtAlt.setInt(2, i + 1);
                            stmtAlt.setString(3, alternativas.get(i));
                            stmtAlt.setBoolean(4, correta);
                            stmtAlt.executeUpdate();
                        }
                    }
                }

                conn.commit();
                return idQuestao;

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao salvar questão no banco: " + e.getMessage(), e);
        }
    }

    private String montarGabarito(Questao questao) {
        if (Questao.DISSERTATIVA.equalsIgnoreCase(questao.getTipo())) {
            return questao.getRespostaModelo() == null ? "" : questao.getRespostaModelo();
        }
        if (Questao.CORRESPONDENCIA.equals(questao.getTipo())) {
            List<String> partes = new ArrayList<>();
            for (int i = 0; i < questao.getAlternativasCorrespondencia().size(); i++) {
                String texto = questao.getAlternativasCorrespondencia().get(i);
                Integer numero = questao.getGabaritoCorrespondencia().get(i);
                partes.add(texto + "=" + numero);
            }
            return String.join(" | ", partes);
        }
        List<String> alternativasCorretas = new ArrayList<>();
        List<String> alternativas = questao.getAlternativas();
        for (Integer indice : questao.getRespostasCorretas()) {
            if (indice != null && indice >= 0 && indice < alternativas.size()) {
                alternativasCorretas.add(alternativas.get(indice));
            }
        }
        return String.join(" | ", alternativasCorretas);
    }

    public List<Questao> listarQuestoes() {
        Map<Long, Questao> mapaQuestoes = new LinkedHashMap<>();
        Long matriculaProf = SessaoUsuarioController.getIdUsuarioLogado();

        String sqlBase =
                "SELECT " +
                        "    q.id, " +
                        "    q.tipo, " +
                        "    q.pontos, " +
                        "    q.enunciado, " +
                        "    q.gabarito, " +
                        "    COALESCE(d_q.nome, d_p.nome, 'Sem disciplina') AS nome_disciplina, " +
                        "    p.nome AS nome_prova, " +
                        "    COALESCE(( " +
                        "        SELECT COUNT(*) " +
                        "        FROM mg_questao_alternativa a " +
                        "        WHERE a.id_questao = q.id AND a.correta = TRUE " +
                        "    ), 0) AS qtd_corretas, " +
                        "    COALESCE(( " +
                        "        SELECT COUNT(*) " +
                        "        FROM mg_questao_correspondencia_item ci " +
                        "        WHERE ci.id_questao = q.id " +
                        "    ), 0) AS qtd_itens_corr " +
                        "FROM mg_questoes q " +
                        "LEFT JOIN mg_disciplina d_q ON d_q.id = q.id_disciplina " +
                        "LEFT JOIN mg_prova_questao pq ON pq.id_questao = q.id " +
                        "LEFT JOIN mg_provas p ON p.id = pq.id_prova " +
                        "LEFT JOIN mg_disciplina d_p ON d_p.id = p.id_disciplina " +
                        (matriculaProf != null
                                ? "WHERE COALESCE(q.matricula_professor, p.matricula_professor) = ? "
                                : "") +
                        "ORDER BY q.id DESC";

        String sqlAlternativas =
                "SELECT texto, correta " +
                        "FROM mg_questao_alternativa " +
                        "WHERE id_questao = ? " +
                        "ORDER BY ordem ASC, id ASC";

        String sqlItensCorr =
                "SELECT texto " +
                        "FROM mg_questao_correspondencia_item " +
                        "WHERE id_questao = ? " +
                        "ORDER BY ordem ASC, id ASC";

        String sqlAltCorr =
                "SELECT texto, numero_correto " +
                        "FROM mg_questao_correspondencia_alternativa " +
                        "WHERE id_questao = ? " +
                        "ORDER BY id ASC";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sqlBase)) {

            if (matriculaProf != null) {
                stmt.setLong(1, matriculaProf);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long idQuestao = rs.getLong("id");

                    if (!mapaQuestoes.containsKey(idQuestao)) {
                        Questao q = new Questao();
                        q.setIdOrigemBanco(idQuestao);
                        q.setReutilizadaDoBanco(true);
                        q.setId(idQuestao);
                        q.setEnunciado(rs.getString("enunciado"));
                        q.setPontos(rs.getDouble("pontos"));
                        q.setDisciplina(rs.getString("nome_disciplina"));
                        q.setProva(rs.getString("nome_prova"));

                        String tipoBanco = rs.getString("tipo");
                        int qtdCorretas = rs.getInt("qtd_corretas");
                        int qtdItensCorr = rs.getInt("qtd_itens_corr");

                        if ("Dissertativa".equalsIgnoreCase(tipoBanco)) {
                            q.setTipo(Questao.DISSERTATIVA);
                            q.setRespostaModelo(rs.getString("gabarito"));
                        } else if (qtdItensCorr > 0) {
                            q.setTipo(Questao.CORRESPONDENCIA);
                        } else if (qtdCorretas > 1) {
                            q.setTipo(Questao.CHECKLIST);
                        } else {
                            q.setTipo(Questao.MULTIPLA_ESCOLHA);
                        }

                        mapaQuestoes.put(idQuestao, q);
                    } else {
                        Questao existente = mapaQuestoes.get(idQuestao);
                        if (existente.getProva() == null && rs.getString("nome_prova") != null) {
                            existente.setProva(rs.getString("nome_prova"));
                        }
                    }
                }
            }

            for (Questao q : mapaQuestoes.values()) {
                if (Questao.CORRESPONDENCIA.equals(q.getTipo())) {
                    try (PreparedStatement stmtItens = conn.prepareStatement(sqlItensCorr)) {
                        stmtItens.setLong(1, q.getId());
                        try (ResultSet rsItens = stmtItens.executeQuery()) {
                            while (rsItens.next()) {
                                q.getItensCorrespondencia().add(rsItens.getString("texto"));
                            }
                        }
                    }

                    try (PreparedStatement stmtAltCorr = conn.prepareStatement(sqlAltCorr)) {
                        stmtAltCorr.setLong(1, q.getId());
                        try (ResultSet rsAltCorr = stmtAltCorr.executeQuery()) {
                            while (rsAltCorr.next()) {
                                q.getAlternativasCorrespondencia().add(rsAltCorr.getString("texto"));
                                q.getGabaritoCorrespondencia().add(rsAltCorr.getInt("numero_correto"));
                            }
                        }
                    }
                } else if (!Questao.DISSERTATIVA.equals(q.getTipo())) {
                    try (PreparedStatement stmtAlt = conn.prepareStatement(sqlAlternativas)) {
                        stmtAlt.setLong(1, q.getId());
                        try (ResultSet rsAlt = stmtAlt.executeQuery()) {
                            int indice = 0;
                            while (rsAlt.next()) {
                                q.getAlternativas().add(rsAlt.getString("texto"));
                                if (rsAlt.getBoolean("correta")) {
                                    q.getRespostasCorretas().add(indice);
                                }
                                indice++;
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar questões: " + e.getMessage(), e);
        }

        return new ArrayList<>(mapaQuestoes.values());
    }
}