[⬅️ Voltar para o README Principal](../../README.md)

# 🛠️ Manual de Instalação e Execução — MegaTests

<p align="center">
  <b>Guia técnico completo para preparação do ambiente, orquestração do banco de dados e execução local da plataforma.</b>
</p>

---

## 📋 1. Pré-requisitos do Ambiente

Antes de iniciar a configuração do projeto, certifique-se de que as ferramentas abaixo estejam instaladas e configuradas corretamente em sua máquina:

| Ferramenta | Versão / Especificação | Finalidade no Projeto |
| :--- | :---: | :--- |
| **Java Development Kit (JDK)** | `21` | Compilação e execução do backend. |
| **Docker & Docker Compose** | Versão recente | Orquestração e execução do container do banco de dados. |
| **Apache Maven** | Integrado ou local | Gerenciamento de dependências e *build* (`pom.xml`). |
| **IDE de Desenvolvimento** | `IntelliJ IDEA` | Ambiente recomendado para desenvolvimento e execução. |
| **Git** | Versão recente | Clonagem e versionamento do código-fonte. |

---

## 📂 2. Arquitetura e Arquivos de Configuração

Para facilitar a localização dos arquivos mencionados neste guia, observe como os recursos estão organizados no repositório:

```text
API/
├── docs/                         # Documentação técnica e manuais do projeto
├── src/
│   └── main/
│       ├── java/
│       │   └── .../
│       │       ├── dao/          # Classes DAO responsáveis pela conexão e operações no banco
│       │       └── entity/       # Classes de entidades (mapeamento das tabelas)
│       └── resources/
│           ├── compose.yml       # Arquivo de configuração do container Docker (MySQL)
│           ├── *.sql             # Script DDL com a estrutura do banco de dados
│           └── .gitignore        # Regras de arquivos ignorados no controle de versão
└── pom.xml                       # Arquivo de configuração de dependências e build do Maven
```

---

## 🚀 3. Passo a Passo de Instalação

### Etapa 1: Clonar o Repositório
Abra o terminal de sua preferência, navegue até o diretório onde deseja salvar o projeto e execute:

```bash
git clone https://github.com/Megabrain-API/API.git
cd API
```

---

### Etapa 2: Inicializar o Banco de Dados (MySQL via Docker)
O banco de dados do sistema roda isolado dentro de um container Docker configurado via Docker Compose.

1. No terminal, acesse a pasta `resources` onde o arquivo `compose.yml` está armazenado:
   ```bash
   cd src/main/resources
   ```

2. Execute o comando para subir o container do MySQL em segundo plano (*detached mode*):
   ```bash
   docker compose up -d
   ```

3. Verifique se o container está ativo e rodando corretamente:
   ```bash
   docker ps
   ```

> 💡 **Nota:** Caso utilize uma versão mais antiga do Docker, substitua o comando por `docker-compose up -d`.

---

### Etapa 3: Aplicar a Estrutura do Banco de Dados (DDL)
Com o container do MySQL em execução, é necessário criar as tabelas do sistema:

1. Localize o arquivo de script **DDL** (`.sql`) dentro da pasta `src/main/resources`.
2. Conecte-se à instância do MySQL que está rodando no Docker utilizando o cliente de banco de dados de sua preferência (como a aba *Database* do próprio **IntelliJ IDEA**, **MySQL Workbench** ou **DBeaver**), usando as credenciais definidas no arquivo `compose.yml`.
3. Execute o script DDL completo para criar o banco de dados e todas as tabelas correspondentes às classes da pasta `entity`.

---

### Etapa 4: Importar e Configurar o Projeto no IntelliJ IDEA
1. Abra o **IntelliJ IDEA** e clique em **File > Open...** (ou **Open** na tela inicial).
2. Selecione a pasta raiz do repositório clonado (`API/`), onde se encontra o arquivo **`pom.xml`**.
3. O IntelliJ reconhecerá automaticamente que se trata de um projeto **Maven**. Aguarde até que o download e a indexação de todas as dependências do `pom.xml` sejam concluídos na barra inferior da IDE.
4. Confirme se o projeto está utilizando o **Java 21**:
   * Acesse **File > Project Structure...** (ou pressione `Ctrl + Alt + Shift + S`).
   * Na aba **Project**, verifique se o campo **SDK** está definido para a versão **21** e o **Language level** está compatível.

---

### Etapa 5: Validar a Conexão com o Banco de Dados (Pacote DAO)
A camada de persistência da aplicação utiliza o padrão **DAO (Data Access Object)**:

1. Navegue até o diretório de código-fonte (`src/main/java/`) e abra a pasta dedicada às classes **DAO**.
2. Localize a classe responsável por estabelecer a conexão JDBC com o MySQL.
3. Certifique-se de que os parâmetros de conexão (URL do banco, porta exposta pelo container, usuário e senha) correspondem exatamente aos valores definidos no seu arquivo `compose.yml`.

---

## ▶️ 4. Execução e Testes do Projeto

### Executando pela IDE (IntelliJ IDEA)
1. Na árvore de projetos (`src/main/java/`), localize a classe principal da aplicação (`Main`).
2. Clique com o botão direito sobre o arquivo da classe e selecione **Run 'Main.main()'** (ou clique no ícone verde de **Play** ▶️ ao lado da assinatura do método `main`).
3. Acompanhe o console da IDE para confirmar que a aplicação iniciou sem erros de conexão.

### Compilando e Validando via Maven (Terminal)
Caso queira limpar builds anteriores, compilar o projeto e executar os testes automatizados configurados no Maven via linha de comando na raiz do repositório:

```bash
mvn clean test
```

Para gerar o pacote completo da aplicação:

```bash
mvn clean package
```

---

## 🔧 5. Resolução de Problemas Comuns (Troubleshooting)

* **Erro de conexão recusada (`Communications link failure`):** Verifique com `docker ps` se o container do MySQL está realmente em execução e se a porta mapeada no `compose.yml` não está sendo ocupada por outro serviço MySQL local na sua máquina.
* **Erro de versão do Java (`Unsupported class file major version`):** Confirme nas configurações do IntelliJ (**Project Structure** e **Settings > Build, Execution, Deployment > Compiler > Java Compiler**) se o target bytecode está apontando para o **Java 21**.
* **Dependências não reconhecidas:** Clique com o botão direito sobre o arquivo `pom.xml` no IntelliJ e selecione **Maven > Reload Project**.

---

<p align="center">
  <b>Equipe Megabrain — API 2º Semestre / 2026</b>
</p>
