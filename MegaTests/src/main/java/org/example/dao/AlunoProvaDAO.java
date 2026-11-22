package org.example.dao;

import org.example.entity.profEntity.AlunoProva;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class AlunoProvaDAO {

    /**
     * Procura uma tentativa em andamento do aluno para a prova.
     * Se não existir, registra o momento exato em que o aluno clicou para iniciar a prova.
     */
    public AlunoProva registrarOuBuscarInicioProva(long raAluno, long idProva) {
        String sqlSelect = "SELECT * FROM mg_aluno_prova WHERE ra_aluno = ? AND id_prova = ?";
        String sqlInsert = "INSERT INTO mg_aluno_prova (ra_aluno, id_prova, data_inicio, status) VALUES (?, ?, NOW(), 'EM_ANDAMENTO')";

        try (Connection conn = new ConexaoDAO().conectaBD()) {
            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelect)) {
                stmtSelect.setLong(1, raAluno);
                stmtSelect.setLong(2, idProva);

                try (ResultSet rs = stmtSelect.executeQuery()) {
                    if (rs.next()) {
                        AlunoProva tentativa = new AlunoProva();
                        tentativa.setId(rs.getLong("id"));
                        tentativa.setRaAluno(rs.getLong("ra_aluno"));
                        tentativa.setIdProva(rs.getLong("id_prova"));

                        Timestamp dataInicio = rs.getTimestamp("data_inicio");
                        if (dataInicio != null) {
                            tentativa.setDataInicio(dataInicio.toLocalDateTime());
                        }

                        Timestamp dataEnvio = rs.getTimestamp("data_envio");
                        if (dataEnvio != null) {
                            tentativa.setDataEnvio(dataEnvio.toLocalDateTime());
                        }

                        tentativa.setStatus(rs.getString("status"));
                        return tentativa;
                    }
                }
            }

            // Registra a nova tentativa caso o aluno esteja abrindo a prova pela primeira vez
            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                stmtInsert.setLong(1, raAluno);
                stmtInsert.setLong(2, idProva);
                stmtInsert.executeUpdate();
                return registrarOuBuscarInicioProva(raAluno, idProva);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao controlar tempo de prova do aluno: " + e.getMessage(), e);
        }
    }

    /**
     * Calcula em segundos quanto tempo o aluno ainda tem para responder a prova.
     * Considera a duracao_minutos definida pelo professor a partir do momento em que o aluno abriu.
     */
    public long calcularTempoRestanteSegundos(long raAluno, long idProva, int duracaoMinutos, LocalDateTime dataLimite) {
        AlunoProva tentativa = registrarOuBuscarInicioProva(raAluno, idProva);

        if ("FINALIZADA".equals(tentativa.getStatus()) || "EXPIRADA".equals(tentativa.getStatus())) {
            return 0;
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limiteIndividual = tentativa.getDataInicio().plusMinutes(duracaoMinutos);

        LocalDateTime limiteEfetivo = limiteIndividual;
        if (dataLimite != null && dataLimite.isBefore(limiteIndividual)) {
            limiteEfetivo = dataLimite;
        }

        if (agora.isAfter(limiteEfetivo)) {
            return 0;
        }

        return Duration.between(agora, limiteEfetivo).getSeconds();
    }

    /**
     * Atualiza o status da prova para FINALIZADA e grava o momento da entrega.
     */
    public void finalizarTentativa(long raAluno, long idProva) {
        String sql = "UPDATE mg_aluno_prova SET status = 'FINALIZADA', data_envio = NOW() WHERE ra_aluno = ? AND id_prova = ?";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, raAluno);
            stmt.setLong(2, idProva);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}