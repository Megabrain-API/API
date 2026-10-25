package org.example.dao;

import org.example.controllers.profControllers.SessaoUsuarioController;
import org.example.entity.profEntity.Prova;
import org.example.entity.profEntity.Questao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProvaDAO {

    public void salvaProvaCompleta(Prova prova) {
        // Queries usadas durante o salvamento completo da prova.
        String sqlBuscaDisciplina = "SELECT id FROM mg_disciplina WHERE nome = ?";
        String sqlProva = "INSERT INTO mg_provas (nome, data_inicial, data_limite, id_disciplina, matricula_professor) VALUES (?, ?, ?, ?, ?)";
        String sqlQuestao = "INSERT INTO mg_questoes (tipo, enunciado, gabarito) VALUES (?, ?, ?)";
        String sqlAssociativa = "INSERT INTO mg_prova_questao (id_prova, id_questao, ordem) VALUES (?, ?, ?)";
        String sqlAlternativa = "INSERT INTO mg_questao_alternativa (id_questao, ordem, texto, correta) VALUES (?, ?, ?, ?)";

        try (Connection conn = new ConexaoDAO().conectaBD()) {
            // Tudo é salvo dentro de uma única transação. Se qualquer etapa falhar,
            // o rollback impede que a prova fique pela metade no banco.
            conn.setAutoCommit(false);

            try {
                long idDisciplina;

                // Busca o ID da disciplina escolhida pelo professor.
                try (PreparedStatement stmtDisciplina = conn.prepareStatement(sqlBuscaDisciplina)) {
                    stmtDisciplina.setString(1, prova.getDisciplina());

                    try (ResultSet rs = stmtDisciplina.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Disciplina não encontrada no banco: " + prova.getDisciplina());
                        }
                        idDisciplina = rs.getLong("id");
                    }
                }

                Long matriculaProfessorLogado = SessaoUsuarioController.getIdUsuarioLogado();
                if (matriculaProfessorLogado == null) {
                    throw new SQLException("Erro: Nenhum professor autenticado na sessão atual!");
                }

                long idProva;

                // Salva os dados principais da prova e recupera o ID gerado.
                try (PreparedStatement stmtProva = conn.prepareStatement(sqlProva, Statement.RETURN_GENERATED_KEYS)) {
                    stmtProva.setString(1, prova.getNome());
                    stmtProva.setNull(2, Types.DATE);
                    stmtProva.setNull(3, Types.DATE);
                    stmtProva.setLong(4, idDisciplina);
                    stmtProva.setLong(5, matriculaProfessorLogado);
                    stmtProva.executeUpdate();

                    try (ResultSet rsKeys = stmtProva.getGeneratedKeys()) {
                        if (!rsKeys.next()) {
                            throw new SQLException("Não foi possível obter o ID da prova recém-criada.");
                        }
                        idProva = rsKeys.getLong(1);
                    }
                }

                int ordemQuestao = 1;

                // Salva cada questão da prova.
                for (Questao q : prova.getQuestoes()) {
                    String tipoBanco = Questao.DISSERTATIVA.equalsIgnoreCase(q.getTipo())
                            ? "Dissertativa"
                            : "Objetiva";

                    // O campo gabarito é obrigatório em mg_questoes.
                    // Ele é montado antes do INSERT para garantir que sempre exista um valor.
                    String gabarito = montarGabarito(q);
                    if (gabarito == null) {
                        gabarito = "";
                    }

                    long idQuestao;

                    try (PreparedStatement stmtQuestao = conn.prepareStatement(sqlQuestao, Statement.RETURN_GENERATED_KEYS)) {
                        stmtQuestao.setString(1, tipoBanco);
                        stmtQuestao.setString(2, q.getEnunciado());
                        stmtQuestao.setString(3, gabarito);
                        stmtQuestao.executeUpdate();

                        try (ResultSet rsQuestaoKeys = stmtQuestao.getGeneratedKeys()) {
                            if (!rsQuestaoKeys.next()) {
                                throw new SQLException("Não foi possível obter o ID da questão recém-criada.");
                            }
                            idQuestao = rsQuestaoKeys.getLong(1);
                        }
                    }

                    // Liga a questão à prova e mantém a ordem definida no formulário.
                    try (PreparedStatement stmtAssoc = conn.prepareStatement(sqlAssociativa)) {
                        stmtAssoc.setLong(1, idProva);
                        stmtAssoc.setLong(2, idQuestao);
                        stmtAssoc.setInt(3, ordemQuestao++);
                        stmtAssoc.executeUpdate();
                    }

                    // Salva as alternativas das questões objetivas/checklist.
                    List<String> alternativas = q.getAlternativas();
                    List<Integer> respostasCorretas = q.getRespostasCorretas();

                    for (int i = 0; i < alternativas.size(); i++) {
                        boolean correta = respostasCorretas.contains(i);

                        try (PreparedStatement stmtAlternativa = conn.prepareStatement(sqlAlternativa)) {
                            stmtAlternativa.setLong(1, idQuestao);
                            stmtAlternativa.setInt(2, i + 1);
                            stmtAlternativa.setString(3, alternativas.get(i));
                            stmtAlternativa.setBoolean(4, correta);
                            stmtAlternativa.executeUpdate();
                        }
                    }
                }

                conn.commit();
                System.out.println("Prova, questões e alternativas salvas com sucesso no MySQL!");

            } catch (SQLException | RuntimeException erro) {
                conn.rollback();
                throw erro;
            }

        } catch (SQLException erro) {
            erro.printStackTrace();
            throw new RuntimeException("Erro ao salvar prova no banco: " + erro.getMessage(), erro);
        }
    }

    private String montarGabarito(Questao questao) {
        if (Questao.DISSERTATIVA.equalsIgnoreCase(questao.getTipo())) {
            return questao.getRespostaModelo() == null ? "" : questao.getRespostaModelo();
        }

        List<String> alternativasCorretas = new ArrayList<>();
        List<String> alternativas = questao.getAlternativas();
        for (Integer indice : questao.getRespostasCorretas()) {
            if (indice != null && indice >= 0 && indice < alternativas.size()) {
                alternativasCorretas.add(alternativas.get(indice));
            }
        }
        return String.join(" | ", alternativasCorretas);
    }


    public Prova buscarProvaCompleta(int idProva) {
        String sqlProva = "SELECT p.id, p.nome, p.data_limite, d.nome AS disciplina " +
                "FROM mg_provas p " +
                "INNER JOIN mg_disciplina d ON p.id_disciplina = d.id " +
                "WHERE p.id = ?";

        String sqlQuestoes = "SELECT q.id, q.tipo, q.enunciado, " +
                "COALESCE(SUM(CASE WHEN a.correta = TRUE THEN 1 ELSE 0 END), 0) AS quantidade_corretas " +
                "FROM mg_prova_questao pq " +
                "INNER JOIN mg_questoes q ON pq.id_questao = q.id " +
                "LEFT JOIN mg_questao_alternativa a ON a.id_questao = q.id " +
                "WHERE pq.id_prova = ? " +
                "GROUP BY q.id, q.tipo, q.enunciado, pq.ordem " +
                "ORDER BY pq.ordem";

        String sqlAlternativas = "SELECT texto FROM mg_questao_alternativa " +
                "WHERE id_questao = ? ORDER BY ordem";

        try (Connection conn = new ConexaoDAO().conectaBD()) {
            Prova prova = null;

            try (PreparedStatement stmtProva = conn.prepareStatement(sqlProva)) {
                stmtProva.setInt(1, idProva);

                try (ResultSet rs = stmtProva.executeQuery()) {
                    if (rs.next()) {
                        prova = new Prova();
                        prova.setId(rs.getInt("id"));
                        prova.setNome(rs.getString("nome"));
                        prova.setDisciplina(rs.getString("disciplina"));

                        Date dataLimite = rs.getDate("data_limite");
                        if (dataLimite != null) {
                            prova.setPrazo(dataLimite.toLocalDate().toString());
                        }
                    }
                }
            }

            if (prova == null) {
                return null;
            }

            List<Questao> questoes = new ArrayList<>();

            try (PreparedStatement stmtQuestoes = conn.prepareStatement(sqlQuestoes)) {
                stmtQuestoes.setInt(1, idProva);

                try (ResultSet rsQuestoes = stmtQuestoes.executeQuery()) {
                    while (rsQuestoes.next()) {
                        long idQuestao = rsQuestoes.getLong("id");
                        String tipoBanco = rsQuestoes.getString("tipo");
                        int quantidadeCorretas = rsQuestoes.getInt("quantidade_corretas");

                        Questao questao = new Questao();
                        // Guarda o ID para que a resposta do aluno possa ser ligada à questão correta.
                        questao.setId(idQuestao);
                        questao.setEnunciado(rsQuestoes.getString("enunciado"));

                        if ("Dissertativa".equalsIgnoreCase(tipoBanco)) {
                            questao.setTipo(Questao.DISSERTATIVA);
                        } else if (quantidadeCorretas > 1) {
                            questao.setTipo(Questao.CHECKLIST);
                        } else {
                            questao.setTipo(Questao.MULTIPLA_ESCOLHA);
                        }

                        if (!Questao.DISSERTATIVA.equals(questao.getTipo())) {
                            try (PreparedStatement stmtAlternativas = conn.prepareStatement(sqlAlternativas)) {
                                stmtAlternativas.setLong(1, idQuestao);

                                try (ResultSet rsAlternativas = stmtAlternativas.executeQuery()) {
                                    while (rsAlternativas.next()) {
                                        questao.getAlternativas().add(rsAlternativas.getString("texto"));
                                    }
                                }
                            }
                        }

                        questoes.add(questao);
                    }
                }
            }

            prova.setQuestoes(questoes);
            return prova;

        } catch (SQLException erro) {
            erro.printStackTrace();
            throw new RuntimeException("Erro ao carregar prova completa do banco: " + erro.getMessage(), erro);
        }
    }

    /**
     * Lista apenas as provas das disciplinas pertencentes ao curso do aluno logado.
     * O caminho da consulta é: aluno -> curso -> disciplinas -> provas.
     */
    public List<Prova> listarProvasPorAluno(long raAluno) {
        List<Prova> listaProvas = new ArrayList<>();

        String sql = "SELECT DISTINCT p.id, p.nome, d.nome AS disciplina " +
                "FROM mg_aluno a " +
                "INNER JOIN mg_curso_disciplina cd ON cd.id_curso = a.id_curso " +
                "INNER JOIN mg_disciplina d ON d.id = cd.id_disciplina " +
                "INNER JOIN mg_provas p ON p.id_disciplina = d.id " +
                "WHERE a.ra = ? " +
                "ORDER BY d.nome, p.nome";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, raAluno);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prova prova = new Prova();
                    prova.setId(rs.getInt("id"));
                    prova.setNome(rs.getString("nome"));
                    prova.setDisciplina(rs.getString("disciplina"));
                    listaProvas.add(prova);
                }
            }
        } catch (SQLException erro) {
            erro.printStackTrace();
            throw new RuntimeException(
                    "Erro ao carregar provas do curso do aluno: " + erro.getMessage(), erro);
        }

        System.out.println("Total de provas do curso carregadas: " + listaProvas.size());
        return listaProvas;
    }

    // Funçao para deletar provas pelo remove do ListaProvas.xml
    public boolean deletarProva(long idProva) {
        String sql = "DELETE FROM mg_provas WHERE id = ?";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idProva);
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0; // Retorna true se conseguiu apagar do banco

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Função para carregar uma lista com todas as provas
    public List<Prova> listarNomeProvas() {
        List<Prova> listaProvas = new ArrayList<>();
        String sql = "SELECT * FROM mg_provas"; //Query para puxar todas as provas
        //Query para puxar prova por disciplina
       /*  "SELECT
                provas.nome AS nome_da_prova,
                disciplinas.nome AS nome_da_disciplina
            FROM
                provas
            INNER JOIN
                disciplinas ON provas.id_disciplina = disciplinas.id;"*/
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



