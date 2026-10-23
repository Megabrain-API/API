CREATE DATABASE IF NOT EXISTS megabrain_db;
USE megabrain_db;

-- ============================================================
-- 1. AUTENTICAÇÃO E USUÁRIOS
-- ============================================================

CREATE TABLE IF NOT EXISTS mg_professor (
    matricula BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mg_cursos(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL
);


CREATE TABLE IF NOT EXISTS mg_aluno (
    ra BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_curso BIGINT, 
    email VARCHAR(100) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_curso) REFERENCES mg_curso(id) ON DELETE CASCADE
);

-- ============================================================
-- 2. DISCIPLINAS, CURSO, QUESTÕES E PROVAS
-- ============================================================

CREATE TABLE IF NOT EXISTS mg_disciplinas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL
);


-- Tabela Associativa Curso <-> Disciplina (Relacionamento N:N)
CREATE TABLE IF NOT EXISTS mg_curso_disiciplina (
    id_disciplina BIGINT NOT NULL,
    id_curso BIGINT NOT NULL,
    ordem INT,
    PRIMARY KEY (id_disciplina, id_curso),
    FOREIGN KEY (id_curso) REFERENCES mg_cursos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_disciplina) REFERENCES mg_disciplina(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS mg_questoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('Dissertativa', 'Objetiva') NOT NULL,
    enunciado TEXT NOT NULL,
    gabarito TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS mg_provas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    data_inicial DATE,
    data_limite DATE,
    id_disciplina BIGINT NOT NULL,
    matricula_professor BIGINT NOT NULL,
    FOREIGN KEY (id_disciplina) REFERENCES mg_disciplina(id) ON DELETE CASCADE,
    FOREIGN KEY (matricula_professor) REFERENCES mg_professor(matricula) ON DELETE CASCADE
);

-- Tabela Associativa Prova <-> Questão (Relacionamento N:N)
CREATE TABLE IF NOT EXISTS mg_prova_questao (
    id_prova BIGINT NOT NULL,
    id_questao BIGINT NOT NULL,
    ordem INT,
    PRIMARY KEY (id_prova, id_questao),
    FOREIGN KEY (id_prova) REFERENCES mg_provas(id) ON DELETE CASCADE,
    FOREIGN KEY (id_questao) REFERENCES mg_questoes(id) ON DELETE CASCADE
);

-- ============================================================
-- 3. RESOLUÇÕES E ENTREGAS DOS ALUNOS
-- ============================================================

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

-- ============================================================
-- 4. MIGRAÇÃO DINÂMICA DE COMPATIBILIDADE E INSERTS
-- ============================================================

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

-- Inserts Iniciais
INSERT INTO mg_disciplina (nome) VALUES ('Matemática Discreta');
INSERT INTO mg_disciplina (nome) VALUES ('Modelagem de Banco de Dados');
INSERT INTO mg_disciplina (nome) VALUES ('Algoritmos');