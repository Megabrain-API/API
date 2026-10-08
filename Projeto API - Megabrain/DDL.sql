CREATE DATABASE IF NOT EXISTS megabrain_db;
USE megabrain_db;

CREATE TABLE mg_professor (
matricula BIGINT AUTO_INCREMENT PRIMARY KEY,
email VARCHAR(100) NOT NULL UNIQUE,
nome VARCHAR(50) NOT NULL,
senha VARCHAR (50) NOT NULL,
data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mg_aluno (
ra BIGINT AUTO_INCREMENT PRIMARY KEY,
email VARCHAR(100) NOT NULL UNIQUE,
nome VARCHAR(50) NOT NULL,
senha VARCHAR (50) NOT NULL,
data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mg_provas (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
nome VARCHAR (50) NOT NULL,
data_inicial DATE,
data_limite	DATE,
disciplina VARCHAR(50)
);

CREATE TABLE mg_questoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM("Dissertativa", "Objetiva") NOT NULL,
    enunciado TEXT NOT NULL
);

-- Tabela Auxiliar (Associativa)
CREATE TABLE mg_prova_questao (
    id_prova INT NOT NULL,
    id_questao INT NOT NULL,
    ordem INT,
    valor_pontos DECIMAL(4,2),
    PRIMARY KEY (id_prova, id_questao),
    FOREIGN KEY (id_prova) REFERENCES provas(id) ON DELETE CASCADE,
    FOREIGN KEY (id_questao) REFERENCES questoes(id) ON DELETE CASCADE
);
