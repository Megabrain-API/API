package org.example.dao;

import org.example.entity.profEntity.Questao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuestaoDAO {

    public List<Questao> listarQuestoes() {
        Map<Long, Questao> mapaQuestoes = new LinkedHashMap<>();

        String sqlBase =
                "SELECT " +
                        "    q.id, " +
                        "    q.tipo, " +
                        "    q.pontos, " +
                        "    q.enunciado, " +
                        "    q.gabarito, " +
                        "    COALESCE(d.nome, 'Sem disciplina') AS nome_disciplina, " +
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
                        "LEFT JOIN mg_prova_questao pq ON pq.id_questao = q.id " +
                        "LEFT JOIN mg_provas p ON p.id = pq.id_prova " +
                        "LEFT JOIN mg_disciplina d ON d.id = p.id_disciplina " +
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
             PreparedStatement stmt = conn.prepareStatement(sqlBase);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                long idQuestao = rs.getLong("id");

                if (!mapaQuestoes.containsKey(idQuestao)) {
                    Questao q = new Questao();
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
