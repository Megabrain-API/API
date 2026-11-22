package org.example.entity.alunoEntity;

import java.math.BigDecimal;

public class MediaFinal {
    private String nomeDisciplina;
    private int quantidadeprova;
    private BigDecimal mediafinal;

    public MediaFinal() {}

    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplinas) {this.nomeDisciplina = nomeDisciplinas; }

    public int getQuantidadeprova() { return quantidadeprova;}
    public void setQuantidadeprova(int quantidadeprovas) { this.quantidadeprova = quantidadeprovas; }

    public BigDecimal getMediafinal() { return mediafinal; }
    public void setMediafinal (BigDecimal mediafinal) { this.mediafinal = mediafinal; }
}
