CREATE DATABASE IF NOT EXISTS megabrain_db;
USE megabrain_db;

CREATE TABLE IF NOT EXISTS mg_professor (
matricula BIGINT AUTO_INCREMENT PRIMARY KEY,
email VARCHAR(100) NOT NULL UNIQUE,
nome VARCHAR(50) NOT NULL,
senha VARCHAR (50) NOT NULL,
data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mg_aluno (
ra BIGINT AUTO_INCREMENT PRIMARY KEY,
email VARCHAR(100) NOT NULL UNIQUE,
nome VARCHAR(50) NOT NULL,
senha VARCHAR (50) NOT NULL,
data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mg_disciplina(
id BIGINT AUTO_INCREMENT PRIMARY KEY,
nome VARCHAR (50) NOT NULL
);

CREATE TABLE IF NOT EXISTS mg_provas (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
nome VARCHAR (50) NOT NULL,
data_inicial DATE,
data_limite	DATE,
id_disciplina BIGINT NOT NULL,
matricula_professor BIGINT NOT NULL,
FOREIGN KEY (id_disciplina) REFERENCES mg_disciplina(id) ON DELETE CASCADE,
FOREIGN KEY (matricula_professor) REFERENCES mg_professor(matricula) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS mg_respostas (
id_aluno BIGINT
);
CREATE TABLE IF NOT EXISTS mg_questoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM("Dissertativa", "Objetiva") NOT NULL,
    enunciado TEXT NOT NULL,
    gabarito TEXT NOT NULL
);


-- Tabela Auxiliar para Prova e Questão
CREATE TABLE IF NOT EXISTS mg_prova_questao (
    id_prova BIGINT NOT NULL,
    id_questao BIGINT NOT NULL,
    ordem INT,
    PRIMARY KEY (id_prova, id_questao),
    FOREIGN KEY (id_prova) REFERENCES mg_provas(id) ON DELETE CASCADE,
    FOREIGN KEY (id_questao) REFERENCES mg_questoes(id) ON DELETE CASCADE
);

-- Compatibilidade: adiciona a coluna necessária à query já utilizada pelo ProvaDAO
-- em instalações existentes que tenham criado mg_provas antes desta versão.
SET @mg_provas_tem_matricula_professor = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mg_provas'
      AND COLUMN_NAME = 'matricula_professor'
);
SET @sql_mg_provas_matricula = IF(
    @mg_provas_tem_matricula_professor = 0,
    'ALTER TABLE mg_provas ADD COLUMN matricula_professor BIGINT NULL',
    'SELECT 1'
);
PREPARE stmt_mg_provas_matricula FROM @sql_mg_provas_matricula;
EXECUTE stmt_mg_provas_matricula;
DEALLOCATE PREPARE stmt_mg_provas_matricula;

-- INSERT de test para as disciplinas
USE megabrain_db;

INSERT INTO mg_disciplina (nome) VALUES ('Matematica Discreta');
INSERT INTO mg_disciplina (nome) VALUES ('Modelagem de Banco de Dados');
INSERT INTO mg_disciplina (nome) VALUES ('Algoritmo');