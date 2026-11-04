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
}