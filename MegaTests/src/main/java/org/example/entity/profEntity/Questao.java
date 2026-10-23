package org.example.entity.profEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de uma questão de prova.
 *
 * O tipo, as alternativas, os índices das respostas corretas e a resposta
 * de referência são serializados em JSON para preservar a definição completa
 * da prova. O DAO existente continua persistindo o tipo e o enunciado nas
 * tabelas atuais do banco.
 */
public class Questao {

    public static final String MULTIPLA_ESCOLHA = "MULTIPLA_ESCOLHA";
    public static final String CHECKLIST = "CHECKLIST";
    public static final String DISSERTATIVA = "DISSERTATIVA";

    private String tipo;
    private String enunciado;
    private List<String> alternativas;
    private List<Integer> respostasCorretas;
    private String respostaModelo;

    public Questao() {
        this.alternativas = new ArrayList<>();
        this.respostasCorretas = new ArrayList<>();
    }

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
        if (alternativas == null) {
            alternativas = new ArrayList<>();
        }
        return alternativas;
    }

    public void setAlternativas(List<String> alternativas) {
        this.alternativas = alternativas == null
                ? new ArrayList<>() : alternativas;
    }

    public List<Integer> getRespostasCorretas() {
        if (respostasCorretas == null) {
            respostasCorretas = new ArrayList<>();
        }
        return respostasCorretas;
    }

    public void setRespostasCorretas(List<Integer> respostasCorretas) {
        this.respostasCorretas = respostasCorretas == null
                ? new ArrayList<>() : respostasCorretas;
    }

    public String getRespostaModelo() {
        return respostaModelo;
    }

    public void setRespostaModelo(String respostaModelo) {
        this.respostaModelo = respostaModelo;
    }
}
