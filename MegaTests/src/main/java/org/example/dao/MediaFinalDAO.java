package org.example.dao;

import org.example.entity.alunoEntity.MediaFinal;
import org.example.entity.profEntity.MediaFinalAluno;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MediaFinalDAO {

    public List<MediaFinal> listarMediasPorAluno(long raAluno) {
        List<MediaFinal> medias = new ArrayList<>();

        String sql =
                "SELECT d.nome AS nome_disciplina, " +
                        "       COUNT(ap.id) AS qtd_provas, " +
                        "       CASE " +
                        "           WHEN COALESCE(SUM(p.peso), 0) > 0 " +
                        "           THEN ROUND(SUM(ap.nota_total * p.peso) / SUM(p.peso), 2) " +
                        "           ELSE NULL " +
                        "       END AS media_final " +
                        "FROM mg_aluno a " +
                        "INNER JOIN mg_curso_disciplina cd ON cd.id_curso = a.id_curso " +
                        "INNER JOIN mg_disciplina d ON d.id = cd.id_disciplina " +
                        "LEFT JOIN mg_provas p ON p.id_disciplina = d.id " +
                        "LEFT JOIN mg_aluno_prova ap ON ap.id_prova = p.id " +
                        "     AND ap.ra_aluno = a.ra " +
                        "     AND ap.status = 'CORRIGIDA' " +
                        "     AND ap.nota_total IS NOT NULL " +
                        "WHERE a.ra = ? " +
                        "GROUP BY d.id, d.nome, cd.ordem " +
                        "ORDER BY cd.ordem, d.nome";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, raAluno);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MediaFinal item = new MediaFinal();
                    item.setNomeDisciplina(rs.getString("nome_disciplina"));
                    item.setQuantidadeprova(rs.getInt("qtd_provas"));

                    BigDecimal media = rs.getBigDecimal("media_final");
                    item.setMediafinal(media);

                    medias.add(item);
                }
            }
        } catch (SQLException erro) {
            throw new RuntimeException("Erro ao calcular médias do aluno: " + erro.getMessage(), erro);
        }

        return medias;
    }

    /**
     * Lista a média ponderada de todos os alunos nas disciplinas em que
     * o professor logado possui provas cadastradas.
     */
    public List<MediaFinalAluno> listarMediasPorProfessor(long matriculaProfessor) {
        List<MediaFinalAluno> medias = new ArrayList<>();

        String sql =
                "SELECT a.ra AS ra_aluno, " +
                        "       a.nome AS nome_aluno, " +
                        "       d.nome AS nome_disciplina, " +
                        "       CASE " +
                        "           WHEN COALESCE(SUM(p.peso), 0) > 0 " +
                        "           THEN ROUND(SUM(ap.nota_total * p.peso) / SUM(p.peso), 2) " +
                        "           ELSE NULL " +
                        "       END AS media_final " +
                        "FROM mg_disciplina d " +
                        "INNER JOIN mg_curso_disciplina cd ON cd.id_disciplina = d.id " +
                        "INNER JOIN mg_aluno a ON a.id_curso = cd.id_curso " +
                        "LEFT JOIN mg_provas p ON p.id_disciplina = d.id " +
                        "     AND p.matricula_professor = ? " +
                        "LEFT JOIN mg_aluno_prova ap ON ap.id_prova = p.id " +
                        "     AND ap.ra_aluno = a.ra " +
                        "     AND ap.status = 'CORRIGIDA' " +
                        "     AND ap.nota_total IS NOT NULL " +
                        "WHERE EXISTS ( " +
                        "    SELECT 1 FROM mg_provas px " +
                        "    WHERE px.id_disciplina = d.id " +
                        "      AND px.matricula_professor = ? " +
                        ") " +
                        "GROUP BY a.ra, a.nome, d.id, d.nome " +
                        "ORDER BY d.nome, a.nome";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, matriculaProfessor);
            stmt.setLong(2, matriculaProfessor);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MediaFinalAluno item = new MediaFinalAluno();
                    item.setRaAluno(rs.getLong("ra_aluno"));
                    item.setNomeAluno(rs.getString("nome_aluno"));
                    item.setNomeDisciplina(rs.getString("nome_disciplina"));
                    item.setMediaFinal(rs.getBigDecimal("media_final"));

                    medias.add(item);
                }
            }
        } catch (SQLException erro) {
            throw new RuntimeException("Erro ao calcular médias dos alunos do professor: " + erro.getMessage(), erro);
        }

        return medias;
    }
}