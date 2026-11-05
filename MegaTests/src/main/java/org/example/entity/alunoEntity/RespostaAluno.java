package org.example.entity.alunoEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RespostaAluno {
    private Long id;
    private Long raAluno;
    private Long idProva;
    private Long idQuestao;
    private String respostaTexto;
    private LocalDateTime dataEnvio;
    private BigDecimal nota;

    public RespostaAluno() {}

    public Long getId() { return id;}
    public void setId(Long id) { this.id = id; }
    public Long getRaAluno() { return raAluno;}
    public void setRaAluno(Long raAluno) { this.raAluno = raAluno; }
    public Long getIdProva() { return idProva;}
    public void setIdProva(Long idProva) { this.idProva = idProva; }
    public Long getIdQuestao() { return idQuestao;}
    public void setIdQuestao(Long idQuestao) { this.idQuestao = idQuestao; }
    public String getRespostaTexto() { return respostaTexto;}
    public void setRespostaTexto(String respostaTexto) { this.respostaTexto = respostaTexto; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }
    public BigDecimal getNota() { return nota;}
    public void setNota(BigDecimal nota) { this.nota = nota; }
}
