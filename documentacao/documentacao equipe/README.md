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
| **[Felipe Cafalloni da Costa]**       | Product Owner (PO)          | [GitHub](https://github.com/FelipeCafalloni) |
| **[Sayuri Vidal Nozaki]**             | Scrum Master                | [GitHub](https://github.com/Balinhadmwlango) |
| **[Mateus Daniel Santos]**            | Desenvolvedor               | [GitHub](https://github.com/Teuzor)          |
| **[Rian Fernandes Paes]**             | Desenvolvedor               | [GitHub](https://github.com/rianrz)          |
| **[Yago de Noronha Chaves]**          | Desenvolvedor               | [GitHub](https://github.com/1cafee)          |
| **[Daniel da Silva Carvalho Franco]** | Desenvolvedor               | [GitHub](https://github.com/DanielAWdev)     |

---

## 📅 Cronograma


| Sprint                        | Período (semanas) | Foco principal              | Entregáveis                                                                           |
| :------------------------------ | :------------------- | :---------------------------- | :--------------------------------------------------------------------------------------- |
| Preparação                  |                    | Descoberta e setup          | Visão do produto, backlog inicial priorizado, repositório configurado                |
| Sprint 1 — Fundação        | 30/09/26           | Base técnica               | Estrutura do projeto, modelagem de dados, conexão com banco, funcionalidades básicas |
| Sprint 2 — Autenticação    | 26/10/26           | Segurança e agradabilidade | Cadastro/login, telas, funcionalidades essenciais                                      |
| Sprint 3 — Núcleo funcional | 25/11/26           | Polimento                   | Validações, tratamento de erros padronizado, conclusão de funcionalidades faltantes |

---

## 🎯 Problemática e Objetivos

### O Problema do Cliente

Professores enfrentam barreiras operacionais significativas na correção e acompanhamento de provas, o que gera sobrecarga de trabalho e dificulta o nivelamento pedagógico entre turmas de uma mesma disciplina. Além disso, falta uma visão centralizada do histórico de avaliações e flexibilidade para lidar com diferentes tipos de questões (múltipla escolha, dissertativas, associação).

### Objetivos do Sistema

* **Para o Professor:**

  * Cadastro centralizado de avaliações associadas a gabaritos e pesos específicos.
  * Agilização na correção manual de questões complexas (dissertativas, associação, etc.) por meio de um ambiente digital otimizado.
  * Cálculo automatizado de médias diretamente na aplicação.
  * Acesso a históricos de aplicações passadas para reutilização de conteúdos.
  * Consulta a aplicações de outros professores para assegurar um nivelamento equilibrado entre as turmas.
  * Restrição de segurança/validação acadêmica: exigência de **ao menos 2 avaliações registradas** para o fechamento do semestre.
* **Para o Aluno:**

  * Portal dedicado com autenticação e restrição de acesso estrita às suas próprias provas e respectivos gabaritos.
  * Organização do histórico acadêmico dividido de forma clara por semestres e disciplinas.

---

## 🔄 Fluxo de Uso do Usuário

### 🧑‍🏫 1. Fluxo do Professor

1. **Autenticação:** Realiza o cadastro/login com perfil docente e seleciona a turma e a disciplina lecionada.
2. **Publicação de Prova:** Envia o gabarito oficial das questões e define os pesos de cada avaliação.
3. **Correção:** Realiza a correção interativa (especialmente útil para questões dissertativas e de associação), contando com suporte ágil do sistema.
4. **Validação Semestral:** O sistema verifica se há no mínimo 2 avaliações cadastradas para liberar o fechamento do semestre.
5. **Consulta e Nivelamento:** Navega pelo histórico de aplicações anteriores ou busca avaliações de outros docentes da instituição para nivelamento.

### 👨‍🎓 2. Fluxo do Aluno

1. **Autenticação:** Realiza o cadastro/login com perfil de discente.
2. **Visualização Restrita:** É direcionado diretamente para o seu painel acadêmico, onde visualiza suas provas realizadas, gabaritos liberados e notas, segmentados por semestre e disciplina.

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

## Definition of Ready (DoR)

> 🟢 **Um item só entra na sprint se todos os critérios abaixo estiverem atendidos.** Verificado no Refinement e confirmado no Planning.

### Clareza do item

- [ ]  História escrita no formato "Como `<tipo de usuário>`, quero `<ação>`, para `<benefício>`"
- [ ]  Objetivo de negócio e valor do item estão claros para o time
- [ ]  Critérios de aceite descritos e testáveis
- [ ]  Item não depende de nada bloqueado externamente (como outra user story)
- [ ]  Endpoint(s) envolvidos definidos (método, rota, parâmetros)
- [ ]  Regras de validação e cenários de erro previstos

### Preparação para o Planning

- [ ]  Item priorizado pelo PO no backlog
- [ ]  Item estimado pelo time (story points)
- [ ]  Item pequeno o suficiente para caber em uma sprint (se não, foi quebrado)
- [ ]  Dúvidas levantadas no refinamento foram respondidas pelo PO
- [ ]  Ambiente, acessos e ferramentas necessários já disponíveis

---

## Definition of Done (DoD)

> 🏁 **Um item só é considerado "Pronto" quando todos os critérios abaixo forem cumpridos.** Sem exceções negociadas no meio da sprint.

### Código

- [ ]  Todos os critérios de aceite da história foram atendidos
- [ ]  Código segue o padrão de commits e o padrão de código do projeto
- [ ]  Code review aprovado por ao menos um par
- [ ]  Branch integrada à branch principal sem conflitos
- [ ]  Nenhum código comentado, credencial ou dado sensível versionado

### Qualidade

- [ ]  Testes unitários escritos e passando
- [ ]  Cenários de erro e validações testados
- [ ]  Sem bugs conhecidos de severidade alta em aberto
- [ ]  Interface (Design) aprovada

### Documentação

- [ ]  Documentação e manuais atualizados
- [ ]  README atualizado quando houver mudança de setup ou execução
- [ ]  Mudanças que quebram contrato comunicadas e versionadas

### Entrega e aceite

- [ ]  Deploy realizado no ambiente de homologação
- [ ]  Item validado e aceito pelo PO
- [ ]  Status atualizado no board
- [ ]  Item demonstrado (ou pronto para demonstração) na Sprint Review

---

## 📋 Definições da Permanência

Regras definidas para o grupo **Megabrain**, a partir de **09/09/2026**, para alertar o que pode levar a uma expulsão.

## 📌 Regras

- **Respeito e Profissionalismo:** Manter o respeito e profissionalismo enquanto estamos trabalhando no projeto.
- **Presença nas Quartas-feiras:** Vir todas as quartas-feiras obrigatoriamente. Caso necessite faltar ou chegar atrasado, avisar o motivo.
- **Alinhamento nas Segundas-feiras:** Sempre nas segundas trazer suas dúvidas, dificuldades e atualizações sobre o projeto, para que em grupo possamos discutir.
- **Estimativas de Entrega:** Nas entregas, sempre deve-se antecipadamente estimar mais ou menos quanto tempo irá demorar para realizar a tarefa que lhe foi dada, para não atrasar outras. Porém, se não conseguir entregar no tempo estipulado, avise o motivo.
- **Atraso em Tarefas Médias/Baixas:** Caso você atrase duas entregas de prioridade média/baixa, e não avisa ou solicita apoio, o grupo irá conversar sobre sua permanência.
- **Atraso em Tarefas Altas:** Caso você atrase uma entrega de prioridade alta, e não avisa ou solicita apoio, será motivo de expulsão.
- **Transparência:** É EXTREMAMENTE necessário a transparência e honestidade envolvendo o projeto! Para que tudo ocorra bem!

---

## 📦 Como Executar o Projeto

1. Clone o repositório:
   ```bash
   git clone [https://github.com/seu-usuario/megabrain-api.git](https://github.com/seu-usuario/megabrain-api.git)
   ```
2. Abra o projeto em sua IDE Java de preferência (IntelliJ, Eclipse, VS Code).
3. Configure as credenciais do banco de dados no arquivo de proprieade (application.properties ou equivalente)
4. Execute a aplicação e utilize uma ferramenta de teste de API (como Postman ou Insomnia) ou acesse via interface configurada
