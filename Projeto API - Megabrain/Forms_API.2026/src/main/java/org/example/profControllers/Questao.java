
package org.example.profControllers;

import java.util.ArrayList;
import java.util.List;

public class Questao {

    // Tipos de questão
    private String tipo;

    // Enunciado da pergunta
    private String enunciado;

    // Alternativas ou itens da questão
    private List<String> alternativas;

    // Índices das alternativas corretas, começando em zero
    private List<Integer> respostasCorretas;

    // Resposta de referência para questões dissertativas
    private String respostaModelo;

    // Construtor: inicializa as listas
    public Questao() {
        this.alternativas = new ArrayList<>();
        this.respostasCorretas = new ArrayList<>();
    }

    // Getters e Setters

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

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

    public List<Integer> getRespostasCorretas() {
        return respostasCorretas;
    }

    public void setRespostasCorretas(
            List<Integer> respostasCorretas) {
        this.respostasCorretas = respostasCorretas;
    }

    public String getRespostaModelo() {
        return respostaModelo;
    }

    public void setRespostaModelo(String respostaModelo) {
        this.respostaModelo = respostaModelo;
    }
}
