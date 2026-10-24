CREATE DATABASE IF NOT EXISTS megabrain_db;
USE megabrain_db;

CREATE TABLE IF NOT EXISTS mg_professor (
    matricula BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    nome VARCHAR(50) NOT NULL,
    senha VARCHAR(50) NOT NULL,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Cursos precisam existir antes do cadastro do aluno e da associação curso/disciplina.
CREATE TABLE IF NOT EXISTS mg_cursos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_curso VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS mg_aluno (
    ra BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    nome VARCHAR(50) NOT NULL,
    senha VARCHAR(50) NOT NULL,
    id_curso BIGINT,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_curso) REFERENCES mg_cursos(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS mg_disciplina (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL
);

-- Liga cada curso às disciplinas que fazem parte dele.
CREATE TABLE IF NOT EXISTS mg_curso_disciplina (
    id_disciplina BIGINT NOT NULL,
    id_curso BIGINT NOT NULL,
    ordem INT,
    PRIMARY KEY (id_disciplina, id_curso),
    FOREIGN KEY (id_curso) REFERENCES mg_cursos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_disciplina) REFERENCES mg_disciplina(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS mg_provas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    data_inicial DATE,
    data_limite DATE,
    id_disciplina BIGINT NOT NULL,
    matricula_professor BIGINT NOT NULL,
    FOREIGN KEY (id_disciplina) REFERENCES mg_disciplina(id) ON DELETE CASCADE,
    FOREIGN KEY (matricula_professor) REFERENCES mg_professor(matricula) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS mg_questoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM("Dissertativa", "Objetiva") NOT NULL,
    enunciado TEXT NOT NULL,
    gabarito TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS mg_prova_questao (
    id_prova BIGINT NOT NULL,
    id_questao BIGINT NOT NULL,
    ordem INT,
    PRIMARY KEY (id_prova, id_questao),
    FOREIGN KEY (id_prova) REFERENCES mg_provas(id) ON DELETE CASCADE,
    FOREIGN KEY (id_questao) REFERENCES mg_questoes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS mg_questao_alternativa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_questao BIGINT NOT NULL,
    ordem INT NOT NULL,
    texto TEXT NOT NULL,
    correta BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_questao) REFERENCES mg_questoes(id) ON DELETE CASCADE
);

-- Cada linha representa a resposta de um aluno para uma questão de uma prova.
CREATE TABLE IF NOT EXISTS mg_resposta_aluno (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ra_aluno BIGINT NOT NULL,
    id_prova BIGINT NOT NULL,
    id_questao BIGINT NOT NULL,
    resposta_texto TEXT,
    data_envio DATETIME DEFAULT CURRENT_TIMESTAMP,
    nota DECIMAL(4,2),
    FOREIGN KEY (ra_aluno) REFERENCES mg_aluno(ra) ON DELETE CASCADE,
    FOREIGN KEY (id_prova) REFERENCES mg_provas(id) ON DELETE CASCADE,
    FOREIGN KEY (id_questao) REFERENCES mg_questoes(id) ON DELETE CASCADE
);

-- Compatibilidade para bancos antigos onde mg_aluno foi criado sem id_curso.
SET @mg_aluno_tem_id_curso = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mg_aluno'
      AND COLUMN_NAME = 'id_curso'
);
SET @sql_mg_aluno_curso = IF(
    @mg_aluno_tem_id_curso = 0,
    'ALTER TABLE mg_aluno ADD COLUMN id_curso BIGINT NULL',
    'SELECT 1'
);
PREPARE stmt_mg_aluno_curso FROM @sql_mg_aluno_curso;
EXECUTE stmt_mg_aluno_curso;
DEALLOCATE PREPARE stmt_mg_aluno_curso;


-- Migração para versões antigas do projeto que usavam o nome mg_curso_disiciplina.
SET @tem_tabela_curso_disiciplina = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mg_curso_disiciplina'
);
SET @sql_migrar_curso_disciplina = IF(
    @tem_tabela_curso_disiciplina > 0,
    'INSERT IGNORE INTO mg_curso_disciplina (id_disciplina, id_curso, ordem) SELECT id_disciplina, id_curso, ordem FROM mg_curso_disiciplina',
    'SELECT 1'
);
PREPARE stmt_migrar_curso_disciplina FROM @sql_migrar_curso_disciplina;
EXECUTE stmt_migrar_curso_disciplina;
DEALLOCATE PREPARE stmt_migrar_curso_disciplina;

-- Dados de teste de disciplinas. INSERT IGNORE evita duplicação quando o script for executado novamente.
INSERT INTO mg_disciplina (nome)
SELECT 'Matematica Discreta'
WHERE NOT EXISTS (SELECT 1 FROM mg_disciplina WHERE nome = 'Matematica Discreta');

INSERT INTO mg_disciplina (nome)
SELECT 'Modelagem de Banco de Dados'
WHERE NOT EXISTS (SELECT 1 FROM mg_disciplina WHERE nome = 'Modelagem de Banco de Dados');

INSERT INTO mg_disciplina (nome)
SELECT 'Algoritmo'
WHERE NOT EXISTS (SELECT 1 FROM mg_disciplina WHERE nome = 'Algoritmo');
