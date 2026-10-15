package org.example.dao;

import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.Questao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProvaDAO {

    public void salvaProvaCompleta(Prova prova){
        //Strings de query com recepção de valores (?)
        String sqlBuscaDisciplina = "SELECT id FROM mg_disciplina WHERE nome = ?";
        String sqlProva = "INSERT INTO mg_provas (nome, data_inicial, data_limite, id_disciplina) VALUES (?, ?, ?, ?)";
        String sqlQuestao = "INSERT INTO mg_questoes (tipo, enunciado) VALUES (?, ?)";
        String sqlAssociativa = "INSERT INTO mg_prova_questao (id_prova, id_questao, ordem) VALUES (?, ?, ?)";


        try (Connection conn = new ConexaoDAO().conectaBD()) {
            // Desativa o auto-commit para garantir que ou salva tudo, ou não salva nada
            conn.setAutoCommit(false);
        // busca as disciplinas para se ligar a prova
            long idDisciplina  = 0;
            try(PreparedStatement stmtDisciplina = conn.prepareStatement(sqlBuscaDisciplina)) {
                stmtDisciplina.setString(1,prova.getDisciplina());
                try( ResultSet rs  = stmtDisciplina.executeQuery()) {
                    if (rs.next()){
                        idDisciplina = rs.getLong("id");
                    } else {
                        throw new SQLException("Disciplina não encontrada no banco: " + prova.getDisciplina());
                    }
                }
            }

            // Produzir o query para inserir a prova na tabela mg_provas
            try(PreparedStatement stmtProva = conn.prepareStatement(sqlProva /* Coluna */, Statement.RETURN_GENERATED_KEYS /* Valor */)){
                stmtProva.setString(1, prova.getNome());
                stmtProva.setNull(2, Types.DATE); // No momento sem o sistema de tempo de prova por isso SETNULL
                stmtProva.setNull(3, Types.DATE); // Também sem o sistema de tempo de prova por isso SETNULL
                stmtProva.setLong(4, idDisciplina);
                // stmtProva.setString(4, prova.getDisciplina());
                stmtProva.executeUpdate();


                // Pega o ID gerado automaticamente pela prova recém-inserida
                ResultSet rsKeys = stmtProva.getGeneratedKeys();
                long idProva = 0;
                if (rsKeys.next()) {
                    idProva = rsKeys.getLong(1);
            }

                // Percorre cada questão gerada no formulário
                int ordem = 1;
                for (Questao q : prova.getQuestoes()){
                    long idQuestao = 0;

                    // Insere a questão objetiva na tabela mg_questoes
                    try (PreparedStatement stmtQuestao = conn.prepareStatement(sqlQuestao, Statement.RETURN_GENERATED_KEYS)) {
                        stmtQuestao.setString(1, "Objetiva"); // Ou "Dissertativa" dependendo do tipo
                        stmtQuestao.setString(2, q.getEnunciado());
                        stmtQuestao.executeUpdate();

                        ResultSet rsQuestaoKeys = stmtQuestao.getGeneratedKeys();
                        if (rsQuestaoKeys.next()) {
                            idQuestao = rsQuestaoKeys.getLong(1);
                        }
                    }

                    // Faz a associação na tabela mg_prova_questao
                    try (PreparedStatement stmtAssoc = conn.prepareStatement(sqlAssociativa)) {
                        stmtAssoc.setLong(1, idProva);
                        stmtAssoc.setLong(2, idQuestao);
                        stmtAssoc.setInt(3, ordem++);
                        stmtAssoc.executeUpdate();
                    }

                }

                // Confirma todas as operações no banco de dados
                conn.commit();
                System.out.println("Prova e questões salvas com sucesso no MySQL!");

            }
            catch(SQLException erro){
                conn.rollback(); // cancela se der erro ou na questão ou na prova
                throw erro;
            }

        } catch (SQLException erro) {
            erro.printStackTrace();
            throw new RuntimeException("Erro ao salvar prova no banco: " + erro.getMessage());
        }
    }

    // Função para carregar uma lista com todas as provas
    public List<Prova> listarNomeProvas() {
        List<Prova> listaProvas = new ArrayList<>();
        String sql = "SELECT * FROM mg_provas"; //Query para puxar todas as provas
        //Query para puxar prova por disciplina seria
        /* "SELECT
                provas.nome AS nome_da_prova,
                disciplinas.nome AS nome_da_disciplina
            FROM
                provas
            INNER JOIN
                disciplinas ON provas.id_disciplina = disciplinas.id;" */
        //Implantar essa função futuramente.

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            //Percorre todos os resultados do SELECT
            while (rs.next()) {
                Prova prova = new Prova();
                prova.setId(rs.getInt("id"));
                prova.setNome(rs.getString("Nome"));
                listaProvas.add(prova);
            }

        } catch (SQLException erro) {
            erro.printStackTrace();
        }
        System.out.println("Total de provas carregadas: " + listaProvas.size());
        return listaProvas;
    } //Fecha listaNomeProvas

}



