package org.example.entity.profEntity;

import org.example.entity.profEntity.Questao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Prova {

    private String nome;
    private LocalDateTime dataInicial;
    private LocalDateTime dataLimite;
    private Integer duracaoMinutos;
    private String disciplina;
    private String tipoQuestoes;
    private int id;

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

    public LocalDateTime getDataInicial() {
        return dataInicial;
    }

    public void setDataInicial(LocalDateTime dataInicial) {
        this.dataInicial = dataInicial;
    }

    public LocalDateTime getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(LocalDateTime dataLimite) {
        this.dataLimite = dataLimite;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
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

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}