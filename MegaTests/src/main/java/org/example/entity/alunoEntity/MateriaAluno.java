package org.example.entity.alunoEntity;

import java.util.ArrayList;
import java.util.List;

public class MateriaAluno {

    private long idDisciplina;
    private String nomeDisciplina;
    private final List<ProvaCorrigidaAluno> provasCorrigidas = new ArrayList<>();

    public MateriaAluno(long idDisciplina, String nomeDisciplina) {
        this.idDisciplina = idDisciplina;
        this.nomeDisciplina = nomeDisciplina;
    }

    public long getIdDisciplina() {
        return idDisciplina;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public List<ProvaCorrigidaAluno> getProvasCorrigidas() {
        return provasCorrigidas;
    }
}