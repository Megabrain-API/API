package org.example.entity.profEntity;

/**
 * Representa um aluno que já enviou uma determinada prova.
 */
public class AlunoProva {

    private final long ra;
    private final String nome;

    public AlunoProva(long ra, String nome) {
        this.ra = ra;
        this.nome = nome;
    }

    public long getRa() {
        return ra;
    }

    public String getNome() {
        return nome;
    }
}
