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