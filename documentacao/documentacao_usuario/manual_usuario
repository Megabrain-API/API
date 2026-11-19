[⬅️ Voltar para o README Principal](../README.md)

# 📖 Manual do Usuário — MegaTests

## 📋 1. Visão Geral dos Perfis de Acesso

A plataforma **MegaTests** possui dois perfis distintos de usuários, cada um com permissões e funcionalidades específicas dentro do ciclo avaliativo:

| Perfil | Responsabilidades Principais | Funcionalidades Disponíveis |
| :--- | :--- | :--- |
| ** Professor** | Criação, gestão e correção de avaliações. | Cadastro/Login, Dados Pessoais, Minhas Provas, Minhas Turmas e Sair. |
| ** Aluno** | Resolução de provas e acompanhamento de desempenho. | Cadastro/Login, Dados Pessoais, Minhas Provas (dentro do prazo) e Consulta de Notas/Gabarito. |

---

## 🧭 2. Mapa de Navegação do Sistema

Para facilitar a localização das telas e recursos mencionados neste guia, observe a estrutura de navegação após a autenticação:

```text
MegaTests/
├──  Tela Inicial (Cadastro / Login)
│   ├──  Painel do Professor
│   │   ├──  Dados Pessoais (Consulta de informações cadastrais)
│   │   ├──  Minhas Provas (Listagem, Exclusão e Criação com pesos = 10)
│   │   ├──  Minhas Turmas (Filtro por Disciplina/Curso/Semestre e Correção)
│   │   └──  Sair
│   └──  Painel do Aluno
│       ├──  Dados Pessoais (Consulta de informações cadastrais)
│       ├──  Minhas Provas (Provas do seu curso/semestre, Notas e Gabarito)
│       └──  Sair
```

---

## 🚀 3. Passo a Passo de Utilização

### Etapa 1: Cadastro e Autenticação

O acesso ao ambiente é protegido e segmentado de acordo com o vínculo acadêmico:

1. **Cadastro:** Novos usuários (Professores ou Alunos) devem preencher o formulário de cadastro inicial com seus dados pessoais e acadêmicos.
2. **Login:** Na tela de acesso, insira as credenciais cadastradas para ser redirecionado automaticamente ao painel correspondente ao seu perfil.
3. **Sair:** Utilize a opção **Sair** no menu sempre que desejar encerrar a sessão com segurança.

---

### Etapa 2: Fluxo do Professor (`👨‍🏫`)

Após realizar o login, o docente tem acesso aos seguintes módulos:

#### 2.1. Dados Pessoais
* Permite visualizar as informações cadastrais registradas pelo professor no momento da criação da conta.

#### 2.2. Minhas Provas (Criação e Gestão)
Nesta área, o professor visualiza todas as avaliações já publicadas, podendo **excluí-las** ou **criar novas provas**:

1. **Configuração da Avaliação:** Ao iniciar a criação de uma nova prova, o professor informa o **nome da avaliação**, a **disciplina vinculada**, o **prazo limite** de entrega e demais dados necessários.
2. **Seleção e Montagem de Questões:** O docente adiciona as questões desejadas à prova, podendo combinar quatro formatos distintos:
   * **Múltipla escolha**;
   * **Dissertativa**;
   * **Seleção**;
   * **Checklist**.
3. **Atribuição de Pesos:** Para cada questão inserida, o professor deve definir um peso individual.

> ⚠️ **Regra de Negócio:** A soma dos pesos de todas as questões adicionadas a uma prova deve totalizar **sempre 10** para que a avaliação seja concluída e publicada.

#### 2.3. Minhas Turmas (Acompanhamento e Correção)
Ambiente dedicado à visualização das entregas e avaliação dos alunos:

1. **Filtro de Turmas:** As turmas são organizadas e separadas por **Disciplina**, **Curso** e **Semestre**.
2. **Seleção do Aluno:** Ao acessar uma turma e uma prova específica, o professor visualiza a lista de alunos que enviaram suas respostas. Ao clicar sobre um aluno, suas respostas são exibidas na tela.
3. **Processo de Correção:**
   * **Correção Automática:** Questões objetivas (como alternativas e *checklist*) são corrigidas automaticamente pelo sistema.
   * **Correção Manual:** Nas questões que exigem análise docente (como as dissertativas), o professor atribui a nota manualmente e pode preencher uma **justificativa** explicando a pontuação concedida.
4. **Envio da Devolutiva:** Ao concluir a revisão, o professor confirma e envia a correção para o aluno.

---

### Etapa 3: Fluxo do Aluno (`🎓`)

Após efetuar o login, o estudante acessa seu ambiente acadêmico:

#### 3.1. Dados Pessoais
* Permite consultar os dados pessoais e acadêmicos informados no momento do cadastro.

#### 3.2. Minhas Provas (Realização e Resultados)
1. **Visualização Filtrada:** O aluno visualiza apenas as provas disponíveis que pertencem às disciplinas do seu **curso** e **semestre**.
2. **Resolução da Avaliação:**
   * Ao abrir uma prova disponível, o estudante responde às questões dentro do **prazo limite** e do **tempo de duração** estipulados pelo professor.
   * Ao finalizar, o aluno submete a prova para envio ao professor.
3. **Consulta de Nota e Gabarito:** Assim que o professor finaliza e envia a correção da avaliação, a **nota obtida** e o **gabarito** ficam liberados para consulta pelo aluno.

---

## 💡 4. Boas Práticas e Regras Importantes

* **Atenção aos Prazos e Cronômetro (Aluno):** Certifique-se de enviar a prova antes do término do tempo estipulado e dentro da data limite definida pelo professor.
* **Conferência de Pontuação (Professor):** Antes de publicar uma nova avaliação, verifique se a distribuição de pesos entre as questões fecha exatamente em **10 pontos**.
* **Feedback Pedagógico (Professor):** Utilize o campo de justificativa nas questões corrigidas manualmente para orientar o aluno sobre os critérios avaliados.
