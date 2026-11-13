package org.example.entity.profEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda tudo o que o professor precisa para corrigir visualmente uma questão:
 * enunciado, resposta do aluno, gabarito, alternativas, valor máximo e nota obtida.
 */
public class RespostaAlunoProva {

    private long idQuestao;
    private int ordem;
    private String tipo;
    private String enunciado;
    private String respostaAluno;
    private String gabarito;
    private double pontosQuestao = 1.0;
    private double nota;
    private boolean correta;
    private boolean corrigida;
    private String detalheCorrecao;
    private final List<AlternativaCorrecao> alternativas = new ArrayList<>();

    public long getIdQuestao() {
        return idQuestao;
    }

    public void setIdQuestao(long idQuestao) {
        this.idQuestao = idQuestao;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
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

    public String getRespostaAluno() {
        return respostaAluno;
    }

    public void setRespostaAluno(String respostaAluno) {
        this.respostaAluno = respostaAluno;
    }

    public String getGabarito() {
        return gabarito;
    }

    public void setGabarito(String gabarito) {
        this.gabarito = gabarito;
    }

    public double getPontosQuestao() {
        return pontosQuestao;
    }

    public void setPontosQuestao(double pontosQuestao) {
        this.pontosQuestao = pontosQuestao;
    }

    public double getNota() {
        return nota;
    }

    public void setNota(double nota) {
        this.nota = nota;
    }

    public boolean isCorreta() {
        return correta;
    }

    public void setCorreta(boolean correta) {
        this.correta = correta;
    }

    public boolean isCorrigida() {
        return corrigida;
    }

    public void setCorrigida(boolean corrigida) {
        this.corrigida = corrigida;
    }

    public String getDetalheCorrecao() {
        return detalheCorrecao;
    }

    public void setDetalheCorrecao(String detalheCorrecao) {
        this.detalheCorrecao = detalheCorrecao;
    }

    public boolean isParcial() {
        return corrigida && nota > 0.0001 && nota < (pontosQuestao - 0.0001);
    }

    public List<AlternativaCorrecao> getAlternativas() {
        return alternativas;
    }

    /**
     * Uma alternativa da questão, com informação de gabarito e de seleção do aluno.
     */
    public static class AlternativaCorrecao {
        private final String texto;
        private final boolean correta;
        private final boolean selecionada;

        public AlternativaCorrecao(String texto, boolean correta, boolean selecionada) {
            this.texto = texto;
            this.correta = correta;
            this.selecionada = selecionada;
        }

        public String getTexto() {
            return texto;
        }

        public boolean isCorreta() {
            return correta;
        }

        public boolean isSelecionada() {
            return selecionada;
        }
    }
}