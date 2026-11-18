[Voltar para o README Principal](../README.md)

#Manual de Instalação e Execução — MegaTests

Este documento apresenta as instruções necessárias para configurar o ambiente local, inicializar o banco de dados via Docker e executar a aplicação.

---

##Pré-requisitos

Certifique-se de ter as seguintes ferramentas instaladas na sua máquina:

* **Java Development Kit (JDK):** Versão 21.
* **Docker e Docker Compose:** Para execução do container do banco de dados.
* **Apache Maven:** Gerenciador de dependências e build (ou uso integrado via IDE).
* **IDE recomendada:** IntelliJ IDEA.
* **Git:** Para clonar o repositório.

---

##Estrutura Relevante para Configuração

Os arquivos de configuração e estrutura de dados estão organizados no projeto da seguinte forma:

```text
API/
├── src/
│   └── main/
│       ├── java/
│       │   └── .../
│       │       ├── dao/          # Classes de acesso e conexão com o banco de dados
│       │       └── entity/       # Classes de entidades do sistema
│       └── resources/
│           ├── compose.yml       # Configuração do container Docker do banco de dados
│           ├── *.sql             # Script DDL do MySQL para criação das tabelas
│           └── .gitignore        # Regras de arquivos ignorados no versionamento
└── pom.xml                       # Arquivo de dependências e build do Maven
