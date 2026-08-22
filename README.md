# Linketinder

Name: Gildésio Araújo Félix

Sistema em **Groovy**, executado em console, para cadastro de candidatos e empresas, busca por habilidades e criação de matches entre perfis com interesse mútuo.

## Funcionalidades

### Candidatos

* Listar todos os candidatos
* Buscar candidato por CPF
* Filtrar candidatos por skill
* Cadastrar candidato
* Atualizar candidato
* Remover candidato

### Empresas

* Listar todas as empresas
* Buscar empresa por CNPJ
* Filtrar empresas por skill
* Cadastrar empresa
* Atualizar empresa
* Remover empresa

### Sistema de Matches

* Login com CPF ou CNPJ
* Visualizar perfis compatíveis
* Curtir perfis
* Ver matches recíprocos

## Tecnologias utilizadas

* **Groovy**
* **JsonSlurper**
* **JsonBuilder**
* **Gradle**
* **Interface de console**
* **JSON** para persistência dos dados

## Estrutura do projeto

```text
src/
└── main/
    └── groovy/
        └── zg/
            └── acelera/
                ├── app/
                │   └── Menu principal
                │
                ├── service/
                │   └── Regras de negócio
                │
                ├── repository/
                │   └── Persistência em JSON
                │
                ├── user_interface/
                │   └── Menus de interação
                │
                ├── domain/
                │   └── Entidades do sistema
                │
                ├── candidates.json
                │   └── Dados iniciais de candidatos
                │
                ├── companies.json
                │   └── Dados iniciais de empresas
                │
                └── Main.groovy
```

## Dados iniciais em JSON

Os arquivos `.json` utilizados pelo projeto podem ser mantidos como **dados de exemplo**. Isso facilita a utilização do sistema por outras pessoas, pois o projeto já pode ser executado e testado sem a necessidade de realizar cadastros manualmente.

Os arquivos utilizados são:

```text
candidates.json
companies.json
```

O código atual realiza a leitura desses arquivos utilizando **caminhos relativos**. Portanto, para que a aplicação funcione sem alterações no código, os arquivos devem estar disponíveis na pasta de execução do programa.

Uma alternativa é copiar os arquivos para a raiz do projeto antes de iniciar a aplicação:

```bash
cp src/main/groovy/zg/acelera/candidates.json .
cp src/main/groovy/zg/acelera/companies.json .
```

## Requisitos

Antes de executar o projeto, certifique-se de possuir:

* **Java 21 ou superior**
* **Gradle Wrapper**
* **Groovy** instalado para executar diretamente a aplicação

O projeto utiliza o **Gradle Wrapper**, portanto não é necessário ter uma instalação global do Gradle.

## Como executar

### Opção 1 — Executar pela IDE (recomendado)

A maneira recomendada de executar o projeto durante o desenvolvimento é utilizar uma **IDE com suporte a Groovy**, como o **IntelliJ IDEA**.

Isso facilita a execução, depuração e desenvolvimento da aplicação.

#### 1. Abrir o projeto

Abra a pasta `Linketinder` no IntelliJ IDEA.

A IDE deverá reconhecer automaticamente o projeto Gradle através do arquivo:

```text
build.gradle
```

#### 2. Configurar o SDK

Certifique-se de que o projeto está utilizando **Java 21 ou superior**.

No IntelliJ:

```text
File
└── Project Structure
    └── Project
        └── SDK → Java 21+
```

Também é importante verificar se o Gradle está utilizando o mesmo JDK:

```text
Settings
└── Build, Execution, Deployment
    └── Build Tools
        └── Gradle
            └── Gradle JVM → Java 21+
```

#### 3. Configurar os arquivos JSON

Como a aplicação utiliza caminhos relativos para acessar os arquivos JSON, copie-os para a raiz do projeto:

```bash
cp src/main/groovy/zg/acelera/candidates.json .
cp src/main/groovy/zg/acelera/companies.json .
```

A estrutura ficará semelhante a:

```text
Linketinder/
├── build.gradle
├── gradlew
├── candidates.json
├── companies.json
└── src/
    └── main/
        └── groovy/
            └── zg/
                └── acelera/
                    └── Main.groovy
```

#### 4. Executar o `Main.groovy`

No IntelliJ, abra:

```text
src/main/groovy/zg/acelera/Main.groovy
```

Clique com o botão direito sobre o arquivo `Main.groovy` e selecione:

```text
Run 'Main'
```

A aplicação será iniciada diretamente no **console da IDE**.

### Opção 2 — Executar pelo terminal

#### 1. Clonar o repositório

```bash
git clone https://github.com/Gildesio-af/Linketinder-Project.git
cd Linketinder
```

#### 2. Copiar os arquivos JSON para a raiz

```bash
cp src/main/groovy/zg/acelera/candidates.json .
cp src/main/groovy/zg/acelera/companies.json .
```

#### 3. Compilar o projeto

```bash
./gradlew clean classes
```

#### 4. Executar a aplicação

```bash
groovy -cp build/classes/groovy/main src/main/groovy/zg/acelera/Main.groovy
```

## Como usar

Ao iniciar a aplicação, o sistema apresenta um **menu principal** com acesso às seguintes áreas:

* **Candidatos**
* **Empresas**
* **Login e Matches**

### Área de candidatos

Permite:

* Listar candidatos
* Buscar por CPF
* Filtrar por skill
* Cadastrar candidatos
* Atualizar candidatos
* Remover candidatos

### Área de empresas

Permite:

* Listar empresas
* Buscar por CNPJ
* Filtrar por skill
* Cadastrar empresas
* Atualizar empresas
* Remover empresas

### Sistema de login e matches

Após realizar o login utilizando **CPF ou CNPJ**, o usuário pode:

1. Visualizar perfis compatíveis.
2. Curtir perfis de interesse.
3. Verificar os matches recíprocos.

Um **match** ocorre quando existe interesse mútuo entre os perfis.

## Skills suportadas

O sistema possui suporte às seguintes habilidades:

```text
JAVA
PYTHON
JAVASCRIPT
CSHARP
RUBY
PHP
SWIFT
KOTLIN
GO
RUST
```

## Arquitetura

O projeto organiza suas responsabilidades em diferentes camadas:

```text
┌─────────────────────────────┐
│       User Interface        │
│     Menus / Interação       │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│           Service           │
│       Regras de negócio     │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│         Repository          │
│      Persistência JSON      │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│          JSON Files         │
│ candidates.json / companies │
└─────────────────────────────┘
```

Essa separação permite manter a **interface**, as **regras de negócio**, a **persistência** e as **entidades** organizadas em responsabilidades distintas.

## Objetivo

O **Linketinder** tem como objetivo simular uma plataforma de conexão entre **profissionais e empresas**, permitindo o gerenciamento dos perfis, a busca por habilidades e a identificação de interesses mútuos por meio do sistema de matches.
