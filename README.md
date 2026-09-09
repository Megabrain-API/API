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

| Integrante                                  | Função / Papel no Projeto        | GitHub / Contato                            |
| :------------------------------------------ | :--------------------------------- | :------------------------------------------ |
| **[Felipe Cafalloni da Costa]**       | Product Owner (PO) / Desenvolvedor | [GitHub](https://github.com/FelipeCafalloni) |
| **[Sayuri Vidal Nozaki]**             | Scrum Master / Desenvolvedor       | [GitHub](https://github.com/Balinhadmwlango) |
| **[Mateus Daniel Santos]**            | Desenvolvedor                      | [GitHub](https://github.com/Teuzor)          |
| **[Rian Fernandes Paes]**             | Desenvolvedor                      | [GitHub](https://github.com/rianrz)          |
| **[Yago de Noronha Chaves]**          | Desenvolvedor                      | [GitHub](https://github.com/1cafee)          |
| **[Daniel da Silva Carvalho Franco]** | Desenvolvedor                      | [GitHub](https://github.com/DanielAWdev)     |

---

## 📅 Cronograma

| Sprint                        | Período (semanas) | Foco principal              | Entregáveis                                                                           |
| :---------------------------- | :----------------- | :-------------------------- | :------------------------------------------------------------------------------------- |
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

## 📦 Como Executar o Projeto

1. Clone o repositório:
   ```bash
   git clone [https://github.com/seu-usuario/megabrain-api.git](https://github.com/seu-usuario/megabrain-api.git)
   ```
2. Abra o projeto em sua IDE Java de preferência (IntelliJ, Eclipse, VS Code).
3. Configure as credenciais do banco de dados no arquivo de proprieade (application.properties ou equivalente)
4. Execute a aplicação e utilize uma ferramenta de teste de API (como Postman ou Insomnia) ou acesse via interface configurada
