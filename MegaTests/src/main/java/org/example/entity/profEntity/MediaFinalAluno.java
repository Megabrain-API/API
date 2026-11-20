
package org.example.entity.profEntity;

import java.math.BigDecimal;

public class MediaFinalAluno {

    private Long raAluno;
    private String nomeAluno;
    private String nomeDisciplina;
    private BigDecimal mediaFinal;


    public Long getRaAluno() { return raAluno; }
    public void setRaAluno(Long raAluno) { this.raAluno = raAluno; }


    public String getNomeAluno() { return nomeAluno; }
    public void setNomeAluno(String nomeAluno) { this.nomeAluno = nomeAluno; }


    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }


    public BigDecimal getMediaFinal() { return mediaFinal; }
    public void setMediaFinal(BigDecimal mediaFinal) { this.mediaFinal = mediaFinal; }
}
