package org.example.entity.alunoEntity;

import java.util.ArrayList;
import java.util.List;

public class MateriaAluno {

    private long idDisciplina;
    private String nomeDisciplina;
    private Double mediaFinal;
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

    public Double getMediaFinal() {
        return mediaFinal;
    }

    public void setMediaFinal(Double mediaFinal) {
        this.mediaFinal = mediaFinal;
    }

    public List<ProvaCorrigidaAluno> getProvasCorrigidas() {
        return provasCorrigidas;
    }
}