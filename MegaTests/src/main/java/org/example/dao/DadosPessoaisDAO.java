package org.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Consulta somente os dados públicos do próprio usuário autenticado. */
public class DadosPessoaisDAO {
    public String[] buscar(long id, boolean aluno) throws SQLException {
        String sql = aluno
                ? "SELECT a.nome, a.email, a.ra AS identificador, c.nome_curso AS curso "
                  + "FROM mg_aluno a LEFT JOIN mg_cursos c ON c.id = a.id_curso WHERE a.ra = ?"
                : "SELECT nome, email, matricula AS identificador FROM mg_professor WHERE matricula = ?";
        try (Connection conn = new ConexaoDAO().conectaBD()) {
            if (conn == null) {
                throw new SQLException("Não foi possível conectar ao banco de dados.");
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Cadastro do usuário não encontrado.");
                    }
                    return new String[] {rs.getString("nome"), rs.getString("email"),
                            rs.getString("identificador"), aluno ? rs.getString("curso") : null};
                }
            }
        }
    }
}
