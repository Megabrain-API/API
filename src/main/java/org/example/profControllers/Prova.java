package org.example;

import org.example.Questao;

import java.util.ArrayList;
import java.util.List;

public class Prova {

    private String nome;
    private String prazo;
    private String disciplina;
    private String tipoQuestoes;

    // Lista que vai guardar todas as questões desta prova
    private List<Questao> questoes;

    // Construtor: sempre que uma Prova for criada, inicializamos a lista vazia
    public Prova() {
        this.questoes = new ArrayList<>();
    }

    // --- Getters e Setters ---

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPrazo() {
        return prazo;
    }

    public void setPrazo(String prazo) {
        this.prazo = prazo;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

    public String getTipoQuestoes() {
        return tipoQuestoes;
    }

    public void setTipoQuestoes(String tipoQuestoes) {
        this.tipoQuestoes = tipoQuestoes;
    }

    public List<Questao> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(List<Questao> questoes) {
        this.questoes = questoes;
    }
}