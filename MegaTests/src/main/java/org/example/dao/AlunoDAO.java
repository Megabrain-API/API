package org.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AlunoDAO {

    /**
     * Tenta autenticar um aluno no sistema.
     * @return Retorna o 'ra' (Long) se o login for bem-sucedido, ou null se falhar.
     */

    public Long CriaAluno (String nome, String email, String senha) throws SQLException {

        String sql = "INSERT INTO mg_aluno (nome, email, senha) VALUES ( ?, ?, ?)";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, senha);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar o Aluno, nenhuma linha afetada.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()){
                if (generatedKeys.next()){
                    return generatedKeys.getLong(1); // Retorna o RA gerado
                }
                else{
                    throw new SQLException("Falha ao criar Aluno, nenhum RA obtido");
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
        return null;
    }

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

    //TODO fazer o metodo de inserir a resposta do aluno (insert na tabela respostas)

}