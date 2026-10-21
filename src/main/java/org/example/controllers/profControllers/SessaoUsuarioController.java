package org.example.controllers.profControllers;

public class SessaoUsuarioController {

    private static Long idUsuarioLogado;
    private static String tipoUsuario;

    public static void iniciarSessao(Long id, String tipo) {
        idUsuarioLogado = id;
        tipoUsuario = tipo;
    }

    public static Long getIdUsuarioLogado() {
        return idUsuarioLogado;
    }

    public static String getTipoUsuario() {
        return tipoUsuario;
    }

    public static void encerrarSessao() {
        idUsuarioLogado = null;
        tipoUsuario = null;
    }
}
