USE megabrain_db;

-- Cria a tabela de cursos caso ela ainda não exista.
CREATE TABLE IF NOT EXISTS mg_cursos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_curso VARCHAR(100) NOT NULL UNIQUE
);

-- Adiciona o curso ao cadastro de alunos antigos sem apagar nenhum aluno existente.
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

-- Tabela correta que liga um curso às disciplinas dele.
CREATE TABLE IF NOT EXISTS mg_curso_disciplina (
    id_disciplina BIGINT NOT NULL,
    id_curso BIGINT NOT NULL,
    ordem INT,
    PRIMARY KEY (id_disciplina, id_curso),
    FOREIGN KEY (id_curso) REFERENCES mg_cursos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_disciplina) REFERENCES mg_disciplina(id) ON DELETE CASCADE
);

-- Se a versão antiga com o nome digitado errado existir, copia os vínculos para a tabela nova.
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

-- Salva uma resposta por questão respondida pelo aluno.
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

-- Depois da migração, cadastre os cursos e relacione cada um às disciplinas.
-- Exemplo de uso (troque os valores pelos seus dados reais):
-- INSERT INTO mg_cursos (nome_curso) VALUES ('Nome do curso');
-- INSERT INTO mg_curso_disciplina (id_disciplina, id_curso, ordem) VALUES (1, 1, 1);
