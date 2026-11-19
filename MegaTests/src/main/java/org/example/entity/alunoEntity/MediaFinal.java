package org.example.entity.alunoEntity;

import java.math.BigDecimal;

public class MediaFinal {
    private Long idDisciplina;
    private String nomeDisciplinas;
    private int quantidadeprovas;
    private BigDecimal mediafinal;

    public MediaFinal() {}

    public Long getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(Long idDisciplina) { this.idDisciplina = idDisciplina; }

    public String getNomeDisciplinas() { return nomeDisciplinas; }
    public void setNomeDisciplinas(String nomeDisciplinas) {this.nomeDisciplinas = nomeDisciplinas; }

    public int getQuantidadeprovas() { return quantidadeprovas;}
    public void setQuantidadeprovas(int quantidadeprovas) { this.quantidadeprovas = quantidadeprovas; }

    public BigDecimal getMediafinal() { return mediafinal; }
    public void setMediafinal (BigDecimal mediafinal) { this.mediafinal = mediafinal; }
}
