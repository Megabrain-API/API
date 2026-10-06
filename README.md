# 🧠 Megabrain — MegaTests — API 2º Semestre / 2026

<p align="center">
  <b>Plataforma inteligente para gestão, aplicação e correção ágil de avaliações acadêmicas.</b>
</p>

---

## 📋 Sobre o Projeto 

O **MegaTests** é o projeto desenvolvido no âmbito da **API (Aprendizagem por Projetos Integrados)** do **2º semestre de 2026**. O sistema nasce para solucionar um dos maiores gargalos no ecossistema acadêmico: **a dificuldade, o tempo gasto e a complexidade na correção e gestão de avaliações pelos professores**, além de proporcionar transparência e acesso organizado para os alunos.

A plataforma centraliza o cadastro de provas, gabaritos, pesos por avaliação, cálculo automatizado de médias e compartilhamento de materiais pedagógicos entre docentes para nivelamento de turmas.

---

## 🎯 Problemática e Objetivos

### O Problema do Cliente

Professores enfrentam barreiras operacionais significativas na correção e acompanhamento de provas, o que gera sobrecarga de trabalho e dificulta o nivelamento pedagógico entre turmas de uma mesma disciplina. Além disso, falta uma visão centralizada do histórico de avaliações e flexibilidade para lidar com diferentes tipos de questões (múltipla escolha, dissertativas, associação).

---

## 📌 Product Backlog

| # | User Story | Prioridade | Sprint | Estimativa |
| :-: | :-- | :-: | :-: | :-: |
| **1** | Como professor, quero poder aplicar minhas provas com seus gabaritos para os alunos, com o objetivo de facilitar o processo de aplicação de provas. | 🔴 ALTA | 2 | **8** |
| **2** | Como aluno, quero fazer a prova na plataforma, e, em seguida, enviar a prova realizada ao professor para facilitar o processo de realização das avaliações. | 🔴 ALTA | 2 | **5** |
| **3** | Como professor, quero poder enviar quantas provas eu quiser e com diferentes tipos de questão, com o objetivo de facilitar a elaboração do material. | 🔴 ALTA | 2 | **5** |
| **4** | Como professor, quero poder visualizar as provas realizadas pelos alunos, corrigí-las, e retornar ao aluno, visando agilizar o processo de correção das avaliações. | 🟡 MÉDIA | 2 | **5** |
| **5** | Como aluno, quero que seja possível visualizar o meu resultado e o gabarito das provas que fiz, com o objetivo de acompanhar meu desempenho durante o semestre. | 🟡 MÉDIA | 3 | **3** |
| **6** | Como professor, quero que as provas que eu publicar no sistema tenham os pesos e informações da avaliação, com o objetivo de facilitar o cálculo da média de cada aluno. | 🟡 MÉDIA | 3 | **3** |
| **7** | Como professor, quero ter acesso às provas aplicadas anteriormente em um histórico pensando em possíveis reutilizações. | 🟢 BAIXA | 3 | **2** |
| **8** | Como professor, quero poder ter acesso às avaliações que os professores aplicam para as outras turmas com o objetivo de manter as turmas no mesmo nível. | 🟢 BAIXA | 3 | **3** |

---

## 📌 Sprint Backlog

| SPRINT | RANK | ÉPICO | USER STORY | DESCRIÇÃO | CRITÉRIO DE ACEITAÇÃO | PRIORIDADE | ESTIMATIVA | STATUS |
| :---: | :--- | :--- | :--- | :--- | :---: | :---: | :---: |
| 1 | 1 | Criação de provas | Como professor, quero poder aplicar minhas provas com seus gabaritos para os alunos, com o objetivo de facilitar o processo de aplicação de provas. | Deve ser possível criar uma prova básica, pensando em ter uma base sólida para próximas sprints, o usuário professor irá criar uma prova, onde ele pode editar as perguntas e as opções de resposta, e, ao finalizar, ele pressiona o botão de conclusão e a prova é gerada no sistema, futuramente, ela será direcionada para os alunos específicos, mas para essa User Story, a prova será inserida em uma página genérica. | Professor consegue criar uma prova, adicionar perguntas e publicar ela. | ALTA | 8 | EM PROCESSO |
| 2 | 2 | Encaminhamento de resoluções | Como aluno, quero fazer a prova na plataforma, e, em seguida, enviar a prova realizada ao professor para facilitar o processo de realização das avaliações. | O usuário aluno deve receber as provas das disciplinas que estiver cadastrado, ele irá realizar a prova pelo sistema, ao enviar a prova, o professor responsável pela matéria irá receber a prova do aluno com suas respostas. | Aluno consegue realizar a prova e professor visualiza as provas em seu usuário. | ALTA | 5 | AGUARDANDO |
| 2 | 3 | Acessibilidade | Como professor, quero poder enviar quantas provas eu quiser e com diferentes tipos de questão, com o objetivo de facilitar a elaboração do material. | O sistema deve permitir que diferentes tipos de questões sejam armazenadas(múltipla escolha, dissertativa, seleção etc.) | Deve ser possível adicionar diferentes avaliações. | ALTA | 5 | AGUARDANDO |
| 2 | 4 | Facilidade de correção | Como professor, quero poder visualizar as provas realizadas pelos alunos, corrigi-las, e retornar ao aluno, visando agilizar o processo de correção das avaliações. | O professor deve conseguir corrigir a prova digitalmente pelo sistema, ele poderá visualizar as provas dos alunos matriculados, fazer a correção e retornar para o aluno o resultado, o aluno receberá também um gabarito para saber a resposta correta para as questões incorretas. | O professor deve conseguir corrigir as provas dos alunos, e retornar o resultado para eles. | MÉDIA | 5 | AGUARDANDO |
| 2 | 5 | Acesso do aluno | Como aluno, quero que seja possível visualizar o meu resultado e o gabarito das provas que fiz, com o objetivo de acompanhar meu desempenho durante o semestre. | O aluno, em sua página inicial, poderá ver as provas que ele realizou e já foram corrigidas, ele terá acesso à nota da prova e à um gabarito com as respostas corretas. | O aluno recebe um retorno após o professor concluir a correção. | MÉDIA | 3 | AGUARDANDO |
| 3 | 6 | Aplicação de notas | Como professor, quero que as provas que eu publicar no sistema tenham os pesos e informações da avaliação, com o objetivo de facilitar o cálculo da média de cada aluno. | O sistema permitirá que o professor aplique o peso da avaliação e questões na nota do aluno, gerando uma nota específica para a prova aplicada. | O usuário professor pode aplicar diferentes pesos para as provas e questões. | MÉDIA | 3 | AGUARDANDO |
| 3 | 7 | Armazenamento de informações | Como professor, quero ter acesso às provas aplicadas anteriormente em um histórico pensando em possíveis reutilizações. | O sistema não pode apagar as provas aplicadas pelos professores, a não ser que seja da vontade dos mesmos. | O usuário professor pode ter acesso às suas antigas publicações. | MÉDIA | 2 | AGUARDANDO |
| 3 | 8 | Interatividade de usuários | Como professor, quero poder ter acesso às avaliações que os professores aplicam para as outras turmas com o objetivo de manter as turmas no mesmo nível. | O sistema terá um meio de interação de usuários, onde será possível o compartilhamento de avaliações por parte dos professores. | O usuário professor pode observar a atividade de outros usuários professores da mesma instituição. | BAIXA | 3 | AGUARDANDO |

---

## 👥 Equipe


| Integrante                            | Função / Papel no Projeto | GitHub / Contato                             |
| :-------------------------------------- | :---------------------------- | :--------------------------------------------- |
| **Felipe Cafalloni da Costa**       | Product Owner (PO)          | [GitHub](https://github.com/FelipeCafalloni) |
| **Sayuri Vidal Nozaki**             | Scrum Master                | [GitHub](https://github.com/Balinhadmwlango) |
| **Mateus Daniel Santos**            | Desenvolvedor               | [GitHub](https://github.com/Teuzor)          |
| **Rian Fernandes Paes**             | Desenvolvedor               | [GitHub](https://github.com/rianrz)          |
| **Yago de Noronha Chaves**          | Desenvolvedor               | [GitHub](https://github.com/1cafee)          |
| **Daniel da Silva Carvalho Franco** | Desenvolvedor               | [GitHub](https://github.com/DanielAWdev)     |

---

## 📅 Cronograma


| Sprint                        | Período (semanas) | Foco principal              | Entregáveis                                                                           |
| :------------------------------ | :------------------- | :---------------------------- | :--------------------------------------------------------------------------------------- |
| Preparação                  |                    | Descoberta e setup          | Visão do produto, backlog inicial priorizado, repositório configurado                |
| Sprint 1 — Fundação        | 07/09/26 - 27/09/26           | Base técnica               | Estrutura do projeto, telas principais, funcionalidade de criação de provas |
| Sprint 2 — Autenticação    | 05/10/26 - 25/10/26           | Acesso e utilidade  | Cadastro/login, , funcionalidades essenciais de realização de provas pelos alunos e correção dos professores                                      |
| Sprint 3 — Núcleo funcional | 02/11/26 - 27/11/16           | Polimento e Interação                   | Validações, tratamento de erros, conclusão de funcionalidades faltantes, refinamento de funções de interação entre professores |

---

### Objetivos do Sistema

Para o Professor:
* **Criação e Configuração Flexível de Provas:** Permite cadastrar avaliações com diferentes formatos de questões (múltipla escolha, dissertativas e associação), definindo gabaritos oficiais, pesos de nota, janelas de agendamento e limites de tempo.
* **Monitoramento e Aplicação em Tempo Real:** Painel para acompanhar a execução das provas pelos alunos ao vivo, com indicadores de progresso, status de entrega e controle automatizado de tempo limite.
* **Correção Interativa e Automatizada:** Agilização no processo de avaliação com correção instantânea das questões objetivas pela API e ambiente otimizado para a correção manual de questões dissertativas e de associação, incluindo campo para anotações pedagógicas personalizadas por aluno.
* **Cálculo Automatizado de Médias:** Processamento imediato das notas parciais e cálculo automatizado do resultado final da disciplina, garantindo precisão e eliminando tarefas repetitivas.
* **Reutilização e Histórico de Avaliações:** Acesso completo ao histórico de provas aplicadas em semestres anteriores para clonagem, edição e reutilização rápida de exames.
* **Nivelamento Institucional:** Repositório compartilhado que permite consultar exames aplicados por outros docentes da mesma disciplina na instituição, assegurando o alinhamento pedagógico entre turmas.
* **Validação e Governança Acadêmica:** Regra de negócio integrada que exige o registro e aplicação de, no mínimo, 2 avaliações por disciplina antes de liberar o fechamento do semestre letivo.

Para o Aluno:
* **Ambiente Digital de Resolução de Provas:** Interface dedicada para realizar provas diretamente pelo sistema em tempo real, com salvamento automático de rascunhos, cronômetro regressivo e componentes interativos para múltiplos tipos de questão.
* **Portal Restrito e Seguro:** Acesso autenticado estrito, garantindo que o estudante visualize apenas suas próprias avaliações, respostas submetidas, gabaritos liberados e notas.
* **Transparência e Feedback Pedagógico:** Visualização detalhada do espelho da prova corrigida, permitindo consultar o gabarito oficial e ler as observações individuais deixadas pelos professores.
* **Organização do Histórico Acadêmico:** Estruturação limpa do desempenho escolar categorizada por semestres e disciplinas, facilitando o acompanhamento da evolução acadêmica.


## 🛠️ Metodologia e Tecnologias

### Metodologia Ágil (Scrum)

O desenvolvimento é conduzido sob a metodologia **Scrum**, garantindo entregas iterativas por meio de:

* **Sprint Backlogs:** Organização de tarefas e entregas cíclicas.
* **Reuniões Periódicas:** Alinhamentos de equipe para distribuição de demandas e acompanhamento contínuo do progresso técnico.

### Stack Tecnológica

* **Backend:** Linguagem **Java** (desenvolvido com conceitos avançados de POO e arquitetura limpa).
* **Banco de Dados:** Modelagem relacional estruturada através de **MER** (Modelo Entidade-Relacionamento) e **DER** (Diagrama Entidade-Relacionamento).
* **Controle de Versão:** Git e GitHub.

---

# Checklist de Definição de Pronto para Começar (DoR)

> 💡 **Regra:** Um item só entra na sprint se todos os critérios abaixo estiverem atendidos. Verificado no *Refinement* e confirmado no *Planning*.

---

## Clareza do Item

- [ ] **História formatada:** Escrita no padrão `"Como <tipo de usuário>, quero <ação>, para <benefício>"`.
- [ ] **Valor de negócio:** Objetivo e valor do item claros para todo o time.
- [ ] **Critérios de aceite:** Detalhados e testáveis.
- [ ] **Independência:** Item não depende de bloqueios externos (ex: dependência de uma User Story futura).
- [ ] **Regras de negócio:** Regras de validação e cenários de erro previstos.

---

## Preparação para o Planning

- [ ] **Priorização:** Item priorizado pelo PO no backlog.
- [ ] **Estimativa:** Item estimado pelo time de desenvolvimento (*story points*).
- [ ] **Tamanho adequado:** Item fatiado o suficiente para ser concluído dentro de uma única sprint.
- [ ] **Dúvidas sanadas:** Questões levantadas no refinamento devidamente respondidas pelo PO.
- [ ] **Infraestrutura:** Ambiente, acessos e ferramentas necessárias já estão disponíveis.

---

# Checklist de Definição de Pronto (DoD)

> 🏁 **Regra:** Um item só é considerado "Pronto" quando todos os critérios abaixo forem cumpridos. Sem exceções negociadas no meio da sprint.

---

## Código

- [ ] **Critérios de aceite:** Todos os critérios de aceite da história foram atendidos.
- [ ] **Padrões:** Código segue o padrão de commits e o padrão de código do projeto.
- [ ] **Code Review:** Aprovado por ao menos um par.
- [ ] **Integração:** Branch integrada à branch principal sem conflitos.
- [ ] **Segurança e Limpeza:** Nenhum código comentado, credencial ou dado sensível versionado.

---

## Qualidade

- [ ] **Testes unitários:** Escritos e passando.
- [ ] **Validações:** Cenários de erro e validações testados.
- [ ] **Bugs:** Sem bugs conhecidos de severidade alta em aberto.
- [ ] **Interface:** Design/Interface aprovada.

---

## Documentação

- [ ] **Manuais:** Documentação e manuais atualizados.
- [ ] **README:** Atualizado quando houver mudança de setup ou execução.

---

## Entrega e Aceite

- [ ] **Deploy:** Funcionalidade lançada na main.
- [ ] **Aprovação do PO:** Item validado e aceito pelo PO.
- [ ] **Gestão:** Status atualizado no board.
- [ ] **Sprint Review:** Item demonstrado (ou pronto para demonstração) na Sprint Review.

## 📦 Como Executar o Projeto

**EM PROCESSO...**
