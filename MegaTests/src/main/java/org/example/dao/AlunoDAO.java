package org.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * Salva todas as respostas de uma prova dentro da mesma transação.
     * A chave do Map é o ID da questão e o valor é a resposta digitada/selecionada.
     */
    public void salvarRespostasProva(Long raAluno, int idProva, Map<Long, String> respostas) {
        String sqlLimpaRespostas =
                "DELETE FROM mg_resposta_aluno WHERE ra_aluno = ? AND id_prova = ?";

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
                // Remove uma tentativa anterior da mesma prova para evitar respostas duplicadas.
                // Assim, se o aluno reenviar durante o desenvolvimento, fica apenas o envio mais recente.
                try (PreparedStatement stmtLimpa = conn.prepareStatement(sqlLimpaRespostas)) {
                    stmtLimpa.setLong(1, raAluno);
                    stmtLimpa.setInt(2, idProva);
                    stmtLimpa.executeUpdate();
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
}
