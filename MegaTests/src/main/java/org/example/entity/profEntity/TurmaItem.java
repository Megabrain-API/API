package org.example.entity.profEntity;

/**
 * Item simples usado nas listas de disciplinas e cursos da tela Minhas Turmas.
 * Guarda o ID do banco e o nome que será mostrado ao professor.
 */
public class TurmaItem {

    private final long id;
    private final String nome;

    public TurmaItem(long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
