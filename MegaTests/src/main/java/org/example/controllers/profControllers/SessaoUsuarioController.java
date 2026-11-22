package org.example.controllers.profControllers;

import org.example.dao.ConexaoDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SessaoUsuarioController {

    private static Long idUsuarioLogado;
    private static String tipoUsuario;
    private static String nomeUsuarioLogado;

    public static void iniciarSessao(Long id, String tipo) {
        idUsuarioLogado = id;
        tipoUsuario = tipo;
        nomeUsuarioLogado = null;
    }

    public static void iniciarSessao(Long id, String tipo, String nome) {
        idUsuarioLogado = id;
        tipoUsuario = tipo;
        nomeUsuarioLogado = nome;
    }

    public static Long getIdUsuarioLogado() {
        return idUsuarioLogado;
    }

    public static String getTipoUsuario() {
        return tipoUsuario;
    }

    public static String getNomeUsuarioLogado() {
        if (nomeUsuarioLogado != null && !nomeUsuarioLogado.isBlank()) {
            return nomeUsuarioLogado;
        }
        if (idUsuarioLogado == null || tipoUsuario == null) {
            return null;
        }

        String sql = "Aluno".equalsIgnoreCase(tipoUsuario)
                ? "SELECT nome FROM mg_aluno WHERE ra = ?"
                : "SELECT nome FROM mg_professor WHERE matricula = ?";

        try (Connection conn = new ConexaoDAO().conectaBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idUsuarioLogado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nomeUsuarioLogado = rs.getString("nome");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nomeUsuarioLogado;
    }

    public static void encerrarSessao() {
        idUsuarioLogado = null;
        tipoUsuario = null;
        nomeUsuarioLogado = null;
    }
}
