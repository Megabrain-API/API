package org.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfessorDAO {

    /**
     * Tenta autenticar um professor no sistema.
     * @return Retorna a 'matricula' (Long) se o login for bem-sucedido, ou null se falhar.
     */
    public Long criaProfessor(String nome, String email, String senha) {
        String sql = "INSERT INTO mg_professor (nome, email, senha) VALUES (?, ?, ?)";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, senha);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar o professor, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1); // Retorna a matrícula gerada
                } else {
                    throw new SQLException("Falha ao criar o professor, nenhuma matrícula obtida.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Long autenticarProfessor(String email, String senha) {
        String sql = "SELECT matricula FROM mg_professor WHERE email = ? AND senha = ?";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    return rs.getLong("matricula");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }
}