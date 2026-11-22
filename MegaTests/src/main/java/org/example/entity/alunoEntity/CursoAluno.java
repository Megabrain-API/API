package org.example.entity.alunoEntity;

import java.util.ArrayList;
import java.util.List;

public class CursoAluno {

    private long idCurso;
    private String nomeCurso;
    private final List<MateriaAluno> materias = new ArrayList<>();

    public CursoAluno(long idCurso, String nomeCurso) {
        this.idCurso = idCurso;
        this.nomeCurso = nomeCurso;
    }

    public long getIdCurso() {
        return idCurso;
    }

    public String getNomeCurso() {
        return nomeCurso;
    }

    public List<MateriaAluno> getMaterias() {
        return materias;
    }
}