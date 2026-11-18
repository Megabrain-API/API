package org.example.entity.alunoEntity;

import java.time.LocalDateTime;

public class ProvaCorrigidaAluno {

    private int idProva;
    private String nomeProva;
    private long idDisciplina;
    private String nomeDisciplina;
    private double notaTotal;
    private double pontosMaximos;
    private LocalDateTime dataEnvio;

    public int getIdProva() {
        return idProva;
    }

    public void setIdProva(int idProva) {
        this.idProva = idProva;
    }

    public String getNomeProva() {
        return nomeProva;
    }

    public void setNomeProva(String nomeProva) {
        this.nomeProva = nomeProva;
    }

    public long getIdDisciplina() {
        return idDisciplina;
    }

    public void setIdDisciplina(long idDisciplina) {
        this.idDisciplina = idDisciplina;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public void setNomeDisciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }

    public double getNotaTotal() {
        return notaTotal;
    }

    public void setNotaTotal(double notaTotal) {
        this.notaTotal = notaTotal;
    }

    public double getPontosMaximos() {
        return pontosMaximos;
    }

    public void setPontosMaximos(double pontosMaximos) {
        this.pontosMaximos = pontosMaximos;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }
}