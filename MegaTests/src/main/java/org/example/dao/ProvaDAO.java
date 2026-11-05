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
        String sqlProva = "INSERT INTO mg_provas (nome, data_inicial, data_limite, duracao_minutos, id_disciplina, matricula_professor) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlQuestao = "INSERT INTO mg_questoes (tipo, enunciado, gabarito) VALUES (?, ?, ?)";
        String sqlAssociativa = "INSERT INTO mg_prova_questao (id_prova, id_questao, ordem) VALUES (?, ?, ?)";
        String sqlAlternativa = "INSERT INTO mg_questao_alternativa (id_questao, ordem, texto, correta) VALUES (?, ?, ?, ?)";
        String sqlItemCorrespondencia = "INSERT INTO mg_questao_correspondencia_item (id_questao, ordem, texto) VALUES (?, ?, ?)";
        String sqlOpcaoCorrespondencia = "INSERT INTO mg_questao_correspondencia_alternativa (id_questao, texto, numero_correto) VALUES (?, ?, ?)";

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

                    if (prova.getDataInicial() != null) {
                        stmtProva.setTimestamp(2, Timestamp.valueOf(prova.getDataInicial()));
                    } else {
                        stmtProva.setNull(2, Types.TIMESTAMP);
                    }

                    if (prova.getDataLimite() != null) {
                        stmtProva.setTimestamp(3, Timestamp.valueOf(prova.getDataLimite()));
                    } else {
                        stmtProva.setNull(3, Types.TIMESTAMP);
                    }

                    if (prova.getDuracaoMinutos() != null) {
                        stmtProva.setInt(4, prova.getDuracaoMinutos());
                    } else {
                        stmtProva.setNull(4, Types.INTEGER);
                    }

                    stmtProva.setLong(5, idDisciplina);
                    stmtProva.setLong(6, matriculaProfessorLogado);
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
                    // Tratamento isolado por tipo de questão (Evita duplicação e usa as queries certas)
                    if (Questao.CORRESPONDENCIA.equals(q.getTipo())) {
                        // Salva os itens numerados da correspondência
                        for (int i = 0; i < q.getItensCorrespondencia().size(); i++) {
                            try (PreparedStatement stmt = conn.prepareStatement(sqlItemCorrespondencia)) {
                                stmt.setLong(1, idQuestao);
                                stmt.setInt(2, i + 1);
                                stmt.setString(3, q.getItensCorrespondencia().get(i));
                                stmt.executeUpdate();
                            }
                        }

                        // Salva as opções e o número correto da correspondência
                        for (int i = 0; i < q.getAlternativasCorrespondencia().size(); i++) {
                            try (PreparedStatement stmt = conn.prepareStatement(sqlOpcaoCorrespondencia)) {
                                stmt.setLong(1, idQuestao);
                                stmt.setString(2, q.getAlternativasCorrespondencia().get(i));
                                stmt.setInt(3, q.getGabaritoCorrespondencia().get(i));
                                stmt.executeUpdate();
                            }
                        }
                    }
                    else if (!Questao.DISSERTATIVA.equals(q.getTipo())) {
                        // Salva as alternativas comuns APENAS UMA VEZ para Múltipla Escolha e Checklist
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
        if (Questao.CORRESPONDENCIA.equals(questao.getTipo())) {
            List<String> partes = new ArrayList<>();

            for (int i = 0; i < questao.getAlternativasCorrespondencia().size(); i++) {

                String texto = questao.getAlternativasCorrespondencia().get(i);

                String numero = questao.getItensCorrespondencia().get(i);

                partes.add(texto + "=" + numero);
            }
            return String.join(" | ", partes);
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

    public Prova buscarProvaCompletaParaProfessor(int idProva) {
        // Reutiliza a lógica base de buscar a prova
        Prova prova = buscarProvaCompleta(idProva);
        if (prova == null) {
            return null;
        }

        String sqlAlternativasComGabarito = "SELECT texto, correta FROM mg_questao_alternativa " +
                "WHERE id_questao = ? ORDER BY ordem";

        String sqlItensCorrespondencia = "SELECT ordem, texto FROM mg_questao_correspondencia_item " +
                "WHERE id_questao = ? ORDER BY ordem";

        String sqlAlternativasCorrespondencia = "SELECT texto, numero_correto FROM mg_questao_correspondencia_alternativa " +
                "WHERE id_questao = ? ORDER BY id";

        try (Connection conn = new ConexaoDAO().conectaBD()) {
            for (Questao questao : prova.getQuestoes()) {

                // Se for Múltipla Escolha ou Checklist, carrega as corretas
                if (Questao.DISSERTATIVA.equals(questao.getTipo())) {
                    String gabaritoDissertativa = "";
                    // Tem um select proprio pra referencia da dissertativa
                    try (PreparedStatement stmtGab = conn.prepareStatement("SELECT gabarito FROM mg_questoes WHERE id = ?")) {
                        stmtGab.setLong(1, questao.getId());

                        try (ResultSet rsGab = stmtGab.executeQuery()) {
                            if (rsGab.next()) {
                                gabaritoDissertativa = rsGab.getString("gabarito");
                            }
                        }
                    }
                    questao.setRespostaModelo(gabaritoDissertativa);
                }
                //Se for Correspondência, garante que os itens e o gabarito vêm preenchidos
                else if (Questao.CORRESPONDENCIA.equals(questao.getTipo())) {
                    questao.getItensCorrespondencia().clear();
                    questao.getAlternativasCorrespondencia().clear();
                    questao.getGabaritoCorrespondencia().clear();

                    try (PreparedStatement stmtItens = conn.prepareStatement(sqlItensCorrespondencia)) {
                        stmtItens.setLong(1, questao.getId());
                        try (ResultSet rs = stmtItens.executeQuery()) {
                            while (rs.next()) {
                                questao.getItensCorrespondencia().add(rs.getString("texto"));
                            }
                        }
                    }

                    try (PreparedStatement stmtOpcoes = conn.prepareStatement(sqlAlternativasCorrespondencia)) {
                        stmtOpcoes.setLong(1, questao.getId());
                        try (ResultSet rs = stmtOpcoes.executeQuery()) {
                            while (rs.next()) {
                                questao.getAlternativasCorrespondencia().add(rs.getString("texto"));
                                questao.getGabaritoCorrespondencia().add(rs.getInt("numero_correto"));
                            }
                        }
                    }
                }
                // Se for Múltipla Escolha ou Checklist, AGORA EXECUTA O SELECT E CARREGA AS CORRETAS aaaaaaaa
                else {
                    questao.getRespostasCorretas().clear();

                    try (PreparedStatement stmt = conn.prepareStatement(sqlAlternativasComGabarito)) {
                        stmt.setLong(1, questao.getId());

                        try (ResultSet rs = stmt.executeQuery()) {
                            int indice = 0;
                            while (rs.next()) {
                                boolean correta = rs.getBoolean("correta");
                                if (correta) {
                                    questao.getRespostasCorretas().add(indice);
                                }
                                indice++;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prova;
    }

    public Prova buscarProvaCompleta(int idProva) {
        String sqlProva = "SELECT p.id, p.nome, p.data_inicial, p.data_limite, p.duracao_minutos, d.nome AS disciplina " +
                "FROM mg_provas p " +
                "INNER JOIN mg_disciplina d ON p.id_disciplina = d.id " +
                "WHERE p.id = ?";

        String sqlQuestoes =
                "SELECT q.id, q.tipo, q.enunciado, " +
                        "COALESCE(SUM(CASE WHEN a.correta = TRUE THEN 1 ELSE 0 END), 0) AS quantidade_corretas, " +
                        "COUNT(DISTINCT ci.id) AS quantidade_itens_correspondencia " +
                        "FROM mg_prova_questao pq " +
                        "INNER JOIN mg_questoes q ON pq.id_questao = q.id " +
                        "LEFT JOIN mg_questao_alternativa a ON a.id_questao = q.id " +
                        "LEFT JOIN mg_questao_correspondencia_item ci ON ci.id_questao = q.id " +
                        "WHERE pq.id_prova = ? " +
                        "GROUP BY q.id, q.tipo, q.enunciado, pq.ordem " +
                        "ORDER BY pq.ordem";
        String sqlItensCorrespondencia =
                "SELECT ordem, texto " +
                        "FROM mg_questao_correspondencia_item " +
                        "WHERE id_questao = ? " +
                        "ORDER BY ordem";

        String sqlAlternativasCorrespondencia =
                "SELECT texto, numero_correto " +
                        "FROM mg_questao_correspondencia_alternativa " +
                        "WHERE id_questao = ? " +
                        "ORDER BY id";

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
                        prova.setDuracaoMinutos(rs.getInt("duracao_minutos"));

                        Timestamp dataInicial = rs.getTimestamp("data_inicial");
                        if (dataInicial != null) {
                            prova.setDataInicial(dataInicial.toLocalDateTime());
                        }

                        Timestamp dataLimite = rs.getTimestamp("data_limite");
                        if (dataLimite != null) {
                            prova.setDataLimite(dataLimite.toLocalDateTime());
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

                        int quantidadeItensCorrespondencia =
                                rsQuestoes.getInt("quantidade_itens_correspondencia");

                        if ("Dissertativa".equalsIgnoreCase(tipoBanco)) {

                            questao.setTipo(Questao.DISSERTATIVA);

                        } else if (quantidadeItensCorrespondencia > 0) {

                            questao.setTipo(Questao.CORRESPONDENCIA);

                        } else if (quantidadeCorretas > 1) {

                            questao.setTipo(Questao.CHECKLIST);

                        } else {

                            questao.setTipo(Questao.MULTIPLA_ESCOLHA);
                        }if (Questao.CORRESPONDENCIA.equals(questao.getTipo())) {

                            try (PreparedStatement stmt =
                                         conn.prepareStatement(sqlItensCorrespondencia)) {

                                stmt.setLong(1, idQuestao);

                                try (ResultSet rs = stmt.executeQuery()) {

                                    while (rs.next()) {
                                        questao.getItensCorrespondencia()
                                                .add(rs.getString("texto"));
                                    }
                                }
                            }

                            try (PreparedStatement stmt =
                                         conn.prepareStatement(sqlAlternativasCorrespondencia)) {

                                stmt.setLong(1, idQuestao);

                                try (ResultSet rs = stmt.executeQuery()) {

                                    while (rs.next()) {

                                        questao.getAlternativasCorrespondencia()
                                                .add(rs.getString("texto"));

                                        questao.getGabaritoCorrespondencia()
                                                .add(rs.getInt("numero_correto"));
                                    }
                                }
                            }

                        } else if (!Questao.DISSERTATIVA.equals(questao.getTipo())) {

                            try (PreparedStatement stmtAlternativas =
                                         conn.prepareStatement(sqlAlternativas)) {

                                stmtAlternativas.setLong(1, idQuestao);

                                try (ResultSet rsAlternativas =
                                             stmtAlternativas.executeQuery()) {

                                    while (rsAlternativas.next()) {
                                        questao.getAlternativas()
                                                .add(rsAlternativas.getString("texto"));
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

        String sql = "SELECT DISTINCT p.id, p.nome, p.data_inicial, p.data_limite, p.duracao_minutos, d.nome AS disciplina " +
                "FROM mg_aluno a " +
                "INNER JOIN mg_curso_disciplina cd ON cd.id_curso = a.id_curso " +
                "INNER JOIN mg_disciplina d ON d.id = cd.id_disciplina " +
                "INNER JOIN mg_provas p ON p.id_disciplina = d.id " +
                "WHERE a.ra = ? " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM mg_resposta_aluno r " +
                "WHERE r.ra_aluno = a.ra " +
                "AND r.id_prova = p.id" +
                ") " +
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
                    prova.setDuracaoMinutos(rs.getInt("duracao_minutos"));

                    Timestamp dataInicial = rs.getTimestamp("data_inicial");
                    if (dataInicial != null) {
                        prova.setDataInicial(dataInicial.toLocalDateTime());
                    }

                    Timestamp dataLimite = rs.getTimestamp("data_limite");
                    if (dataLimite != null) {
                        prova.setDataLimite(dataLimite.toLocalDateTime());
                    }

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
                prova.setDuracaoMinutos(rs.getInt("duracao_minutos"));

                Timestamp dataInicial = rs.getTimestamp("data_inicial");
                if (dataInicial != null) {
                    prova.setDataInicial(dataInicial.toLocalDateTime());
                }

                Timestamp dataLimite = rs.getTimestamp("data_limite");
                if (dataLimite != null) {
                    prova.setDataLimite(dataLimite.toLocalDateTime());
                }

                listaProvas.add(prova);
            }

        } catch (SQLException erro) {
            erro.printStackTrace();
        }
        System.out.println("Total de provas carregadas: " + listaProvas.size());
        return listaProvas;
    } //Fecha listaNomeProvas

}
