package org.example;

import java.util.ArrayList;
import java.util.List;

public class Questao {

    private String enunciado;
    private List<String> alternativas;

    // Construtor: inicializa a lista de alternativas
    public Questao() {
        this.alternativas = new ArrayList<>();
    }

    // --- Getters e Setters ---

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public List<String> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<String> alternativas) {
        this.alternativas = alternativas;
    }
}