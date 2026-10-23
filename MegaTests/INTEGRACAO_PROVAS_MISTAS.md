# Integração de Provas Mistas

## O que foi integrado

- Editor dinâmico no fluxo original do professor, mantendo a barra lateral, os caminhos FXML e a navegação do projeto `teuzo`.
- Tipos de questão: múltipla escolha, checklist numerado e dissertativa.
- Validação de enunciado, alternativas preenchidas, ao menos uma resposta correta e exatamente uma resposta correta em múltipla escolha.
- Armazenamento do índice de cada alternativa correta e da resposta-modelo dissertativa.
- Pré-visualização diferenciada por tipo de questão.
- Ao selecionar uma prova na lista do professor, os metadados continuam vindo do banco, e o JSON local fornece os detalhes completos das questões.

## Persistência e compatibilidade com o banco

O fluxo de conexão e as queries existentes do `ProvaDAO` foram mantidos. O arquivo continua usando as mesmas cinco strings SQL e as mesmas cinco chamadas `prepareStatement`. A vinculação do parâmetro `tipo` da questão passou a enviar `Dissertativa` ao enum do banco para questões dissertativas, e `Objetiva` para múltipla escolha/checklist.

O esquema atual de `mg_questoes` só possui as colunas `id`, `tipo` e `enunciado`; não há tabela/campos para alternativas, índices corretos ou resposta-modelo. Para não alterar nem substituir as queries existentes, a definição completa de cada prova mista (alternativas, respostas corretas e resposta de referência) continua no JSON em `provas_salvas/<nome-da-prova>.json`. O banco mantém os registros da prova, a ordem e os enunciados/tipos que o esquema atual suporta.

Também foi corrigida a divergência entre o `ProvaDAO` original — que já insere a coluna `matricula_professor` — e o `ddl.sql` fornecido, que não a declarava. O DDL atualizado inclui essa coluna e uma migração condicional para bases existentes. Em uma instalação Docker antiga, scripts de `docker-entrypoint-initdb.d` não são reexecutados automaticamente; execute o `ddl.sql` atualizado manualmente no banco existente para aplicar a migração. A coluna é adicionada como nullable na migração para não invalidar registros antigos, enquanto novas instalações a criam como `NOT NULL`.

## Verificações realizadas

- As cinco strings SQL originais de `ProvaDAO` permanecem idênticas.
- Contagem de chamadas `prepareStatement`: 5 antes e 5 depois.
- Os 15 arquivos FXML passaram na validação XML.
- `Questao` e `Prova` compilam isoladamente com `javac`.

Não foi possível executar a compilação Maven completa nem um teste de conexão real: o ambiente atual não tem Maven nem as dependências JavaFX/Gson/MySQL instaladas, e não houve acesso à rede para obtê-las. Portanto, a compilação final e o teste integrado precisam ser feitos no ambiente do projeto com suas dependências e seu banco.
