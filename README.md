# 🧠 Megabrain — API 2º Semestre / 2026

<p align="center">
  <b>Plataforma inteligente para gestão, aplicação e correção ágil de avaliações acadêmicas.</b>
</p>

---

## 📋 Sobre o Projeto

O **Megabrain** é o projeto desenvolvido no âmbito da **API (Aprendizagem por Projetos Integrados)** do **2º semestre de 2026**. O sistema nasce para solucionar um dos maiores gargalos no ecossistema acadêmico: **a dificuldade, o tempo gasto e a complexidade na correção e gestão de avaliações pelos professores**, além de proporcionar transparência e acesso organizado para os alunos.

A plataforma centraliza o cadastro de provas, gabaritos, pesos por avaliação, cálculo automatizado de médias e compartilhamento de materiais pedagógicos entre docentes para nivelamento de turmas.

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
| Sprint 1 — Fundação        | 07/09/26 - 27/09/26           | Base técnica               | Estrutura do projeto, modelagem de dados, conexão com banco, funcionalidades básicas |
| Sprint 2 — Autenticação    | 05/10/26 - 25/10/26           | Segurança e agradabilidade | Cadastro/login, telas, funcionalidades essenciais                                      |
| Sprint 3 — Núcleo funcional | 02/11/26 - 27/11/16           | Polimento                   | Validações, tratamento de erros padronizado, conclusão de funcionalidades faltantes |

---

## 🎯 Problemática e Objetivos

### O Problema do Cliente

Professores enfrentam barreiras operacionais significativas na correção e acompanhamento de provas, o que gera sobrecarga de trabalho e dificulta o nivelamento pedagógico entre turmas de uma mesma disciplina. Além disso, falta uma visão centralizada do histórico de avaliações e flexibilidade para lidar com diferentes tipos de questões (múltipla escolha, dissertativas, associação).

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

### 🔄 Fluxo de Uso do Usuário

🧑‍🏫 1. Fluxo do Professor

1. **Autenticação e Seleção de Contexto:**
   * O docente realiza o login no sistema e seleciona o semestre letivo, a turma e a disciplina correspondente.

2. **Criação, Configuração e Agendamento de Provas:**
   * Cadastra as questões (múltipla escolha, dissertativa ou associação), define os pesos das notas e insere o gabarito oficial.
   * Configura os parâmetros de aplicação: data/hora de abertura e fechamento, tempo limite de duração e regras de liberação do gabarito.

3. **Acompanhamento da Aplicação ao Vivo:**
   * Acessa o painel de monitoramento durante o horário do exame para acompanhar em tempo real o status de cada aluno (*Não Iniciou*, *Em Andamento*, *Finalizada*, *Tempo Esgotado*).

4. **Correção e Lançamento de Feedbacks:**
   * Visualiza o resultado das questões objetivas já corrigidas automaticamente pela API.
   * Realiza a correção interativa das questões dissertativas e de associação, insere notas parciais e adiciona observações pedagógicas individuais para cada estudante.

5. **Nivelamento e Gestão de Acervo:**
   * Consulta o acervo de provas anteriores para reutilizar estruturas de testes em novas turmas.
   * Acessa a área de nivelamento institucional para visualizar exames elaborados por outros professores da mesma matéria.

6. **Validação e Homologação Semestral:**
   * O sistema valida se a disciplina cumpriu o requisito mínimo de 2 avaliações aplicadas e corrigidas para liberar o encerramento do semestre letivo.

👨‍🎓 2. Fluxo do Aluno

1. **Autenticação e Painel Acadêmico:**
   * O discente realiza o login e acessa o seu dashboard principal, organizado por semestres e disciplinas.

2. **Acesso à Avaliação Agendada:**
   * Seleciona a prova disponível na agenda da disciplina, lê as instruções do exame e confirma o início da tentativa (disparando o cronômetro do sistema).

3. **Resolução Digital Interativa:**
   * Responde às questões no ambiente virtual (múltipla escolha, caixas de texto estruturadas e mecanismos de associação de itens).
   * O sistema realiza o salvamento automático de rascunhos periodicamente para prevenir perda de dados em caso de instabilidade na conexão.

4. **Submissão e Confirmação de Entrega:**
   * Finaliza o envio da prova manualmente antes do encerramento ou automaticamente ao esgotar o tempo limite, recebendo o comprovante digital de submissão.

5. **Consulta de Notas e Desempenho:**
   * Após a liberação pelo professor, acessa o espelho da prova para conferir suas respostas contra o gabarito oficial, visualizar a nota obtida e ler as anotações pedagógicas recebidas.

---

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

## 📦 Como Executar o Projeto

**EM PROCESSO...**
