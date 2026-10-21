package org.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

    public List<String> listarNomesDisciplinas() {
        List<String> disciplinas = new ArrayList<>();
        String sql = "SELECT nome FROM mg_disciplina"; // Query para puxar todas as disciplinas

        try(Connection conn = new ConexaoDAO().conectaBD();
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet rs = statement.executeQuery()) {
            //Roda todos os resultados do SELECT
            while (rs.next()){
                disciplinas.add(rs.getString("nome"));
            }
        }
        catch (SQLException erro){
            erro.printStackTrace();
        }
        System.out.println("Total de disciplinas carregadas: " + disciplinas.size());
        return  disciplinas;
    }
}
