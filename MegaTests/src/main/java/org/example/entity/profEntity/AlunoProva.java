package org.example.entity.profEntity;

import java.time.LocalDateTime;

public class AlunoProva {
    private Long id;
    private Long raAluno;
    private Long idProva;
    private LocalDateTime dataInicio;
    private LocalDateTime dataEnvio;
    private String status;
    private String nomeAluno;

    public AlunoProva() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRaAluno() {
        return raAluno;
    }

    public void setRaAluno(Long raAluno) {
        this.raAluno = raAluno;
    }

    public Long getIdProva() {
        return idProva;
    }

    public void setIdProva(Long idProva) {
        this.idProva = idProva;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }

    // Métodos de compatibilidade para a tela do professor:
    public String getNome() {
        return nomeAluno;
    }

    public Long getRa() {
        return getRaAluno();
    }

}