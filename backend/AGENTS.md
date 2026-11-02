# Linketinder — Instruções para Agentes

Este arquivo é a referência de trabalho para o repositório. O backend descrito
abaixo fica em [`backend/`](./backend); os comandos devem ser executados a
partir da raiz, salvo indicação contrária.

## 1. Visão Geral e Stack Tecnológica

Linketinder conecta candidatos e empresas por competências, vagas, curtidas e
matches. O backend oferece uma interface de console e uma interface HTTP
servida por Tomcat embutido. O repositório também contém o frontend em
[`frontend/`](./frontend), mas este guia se concentra no backend.

- **Linguagem:** Groovy 4.0.21, com classes e records Groovy.
- **Build:** Gradle 9.7 via Gradle Wrapper (`backend/gradlew`); projeto
  configurado pelo [`backend/build.gradle`](./backend/build.gradle).
- **Persistência:** PostgreSQL 15 (imagem Alpine em
  [`backend/docker-compose.yaml`](./backend/docker-compose.yaml)), JDBC,
  `groovy-sql` e pool HikariCP 5.1.0.
- **Migrações:** Flyway 13.7.0, com scripts em
  [`backend/src/main/resources/db/migration/`](./backend/src/main/resources/db/migration/).
- **Servidor HTTP:** Apache Tomcat embutido 11.0.26, com servlets Jakarta.
- **Testes:** Spock 2.3 para Groovy 4, executado pela plataforma JUnit; os
  testes ficam em [`backend/src/test/groovy/`](./backend/src/test/groovy/).
- **Serialização:** `groovy-json`.
- **Arquitetura de acesso:** controladores, serviços, repositórios JDBC,
  DTOs, domínio e utilitários de infraestrutura; não há Spring ou outro
  framework de aplicação configurado.

## 2. Estrutura do Projeto

```text
.
├── AGENTS.md
├── README.md
├── backend/
│   ├── build.gradle                 # Dependências, testes e configuração Flyway
│   ├── settings.gradle              # Nome do projeto Gradle: Linketinder
│   ├── gradlew, gradlew.bat         # Gradle Wrapper
│   ├── docker-compose.yaml           # PostgreSQL local na porta 5433
│   ├── candidates.json               # Dados consumidos pelo menu web
│   ├── src/
│   │   ├── main/groovy/zg/acelera/
│   │   │   ├── Main.groovy          # Entrada da aplicação de console
│   │   │   ├── MainWeb.groovy       # Entrada do servidor HTTP
│   │   │   ├── app/                 # Composição de dependências e menus
│   │   │   ├── controller/          # Controladores de console e servlets web
│   │   │   ├── domain/              # Entidades e contratos do domínio
│   │   │   ├── dto/                 # Objetos de entrada/saída da API
│   │   │   ├── repository/          # Interfaces e implementações JDBC
│   │   │   ├── service/             # Casos de uso e regras de negócio
│   │   │   ├── user_interface/      # Fluxos e impressão da UI de console
│   │   │   └── utils/               # Configuração web, banco, mapeadores e leitura
│   │   └── main/resources/
│   │       └── db/
│   │           ├── migration/       # Migrações versionadas V1...V5
│   │           └── testdata/        # R__mock_data.sql para o ambiente dev
│   ├── src/test/groovy/
│   │   ├── reposistory/              # Testes dos repositórios JDBC
│   │   ├── service/                 # Testes dos serviços
│   │   └── user_interface/          # Testes das UIs de console
│   └── gradle/wrapper/               # Wrapper e versão do Gradle
├── db/                               # Artefatos de banco no nível do repositório
└── frontend/                          # Interface web (fora do backend Groovy)
```

Os diretórios `backend/build/`, `backend/.gradle/`, `backend/.idea/` e
`backend/tomcat.8080/` são artefatos locais/gerados e não são código-fonte.

## 3. Como Executar e Testar

### Preparar o PostgreSQL

Na raiz do repositório:

```bash
docker compose -f backend/docker-compose.yaml up -d postgres
```

O container publica PostgreSQL em `localhost:5433`, com banco
`linketinder`, usuário `postgres` e senha `123456`.

O código e o Flyway usam como padrão a URL `jdbc:postgresql://localhost:5433/postgres`;
para usar exatamente o banco criado pelo Compose, defina explicitamente:

```bash
export DB_URL=jdbc:postgresql://localhost:5433/linketinder
export DB_USER=postgres
export DB_PASSWORD=123456
```

### Migrar o banco

```bash
cd backend
DB_URL=jdbc:postgresql://localhost:5433/linketinder \
DB_USER=postgres DB_PASSWORD=123456 \
./gradlew flywayMigrate
```

Para migrar e carregar também os dados de teste recorrentes:

```bash
cd backend
DB_URL=jdbc:postgresql://localhost:5433/linketinder \
DB_USER=postgres DB_PASSWORD=123456 \
./gradlew -Penv=dev flywayMigrate
```

### Compilar e testar

```bash
cd backend
./gradlew test
```

O build completo (compilação + testes) é:

```bash
cd backend
./gradlew build
```

### Executar a aplicação

O [`backend/build.gradle`](./backend/build.gradle) não aplica o plugin
`application` e, portanto, não define uma task `run`. Compile e execute a
classe principal pela configuração de execução da IDE:

- `zg.acelera.Main` inicia o menu de console.
- `zg.acelera.MainWeb` inicia o Tomcat embutido na porta `8080`, com rotas
  `/candidates/*`, `/companies/*` e `/jobs/*`.

Antes de executar a classe web, configure `DB_URL`, `DB_USER` e
`DB_PASSWORD` como acima e execute as migrações. A classe `MainWeb` é executada
após `./gradlew classes`; o classpath deve ser o classpath `runtimeClasspath`
do Gradle na configuração da IDE. Não documente ou crie `./gradlew run` sem
antes adicionar e configurar formalmente o plugin `application`.

## 4. Domínio do Linketinder

- **`Candidato`**: especialização de `UserBase`, com CPF, sobrenome e data de
  nascimento. Possui competências e endereços; no banco, a identidade base
  está em `users` e os dados específicos em `candidates`.
- **`Empresa`**: especialização de `UserBase`, com CNPJ e candidatos curtidos.
  No banco, a identidade base está em `users` e os dados específicos em
  `companies`.
- **`Vaga` (`Job`)**: publicada por uma empresa, com nome, descrição,
  endereço e competências desejadas. Em SQL, `jobs.publisher_id` referencia
  `companies.user_id`; competências são relacionadas por `jobs_skill`.
- **`Competência` (`Skill`)**: catálogo único em `skills`, associado a
  candidatos/empresas por `users_skill` e a vagas por `jobs_skill`.
- **`Match`**: registro de `candidate_id`, `company_id`, `job_id` e
  `created_at`. A restrição `UNIQUE (candidate_id, job_id)` impede duplicar o
  mesmo match candidato-vaga.

`Address` e `Country` apoiam a localização. `UserBase` concentra os campos
comuns `id`, nome, e-mail, senha, descrição, endereços e competências.

## 5. Regras de Negócio Centrais

### Curtidas

- Um **candidato curte uma vaga** em `jobs_like(candidate_id, job_id)`.
  `JobLikeService.likeJob` registra a curtida de forma idempotente.
- Uma **empresa curte um candidato** em
  `companies_like(company_id, liked_candidate_id)`. A empresa pode curtir
  candidatos independentemente de uma vaga específica; a aplicação avalia
  todas as vagas publicadas por ela.
- Remover a curtida também remove os matches relacionados: a curtida do
  candidato remove o match candidato-vaga; a curtida da empresa remove os
  matches candidato-empresa.

### Quando existe um Match

Um `Match` acontece somente quando há interesse mútuo para a mesma relação:

1. o candidato curtiu a vaga (`jobs_like`); e
2. a empresa que publicou essa vaga curtiu o candidato
   (`companies_like`).

Ao curtir uma vaga, se a empresa já tiver curtido o candidato, é criado um
match para aquela vaga. Ao curtir um candidato, são criados matches para cada
vaga daquela empresa que o candidato já tiver curtido. O serviço não cria um
registro duplicado para o mesmo par candidato-vaga.

## 6. Princípio do Anonimato — RESTRIÇÃO EXPLÍCITA E INEGOCIÁVEL

Esta é uma regra estrita que todo agente deve respeitar em **toda e qualquer
feature, endpoint, DTO ou tela**. Nunca reutilize um DTO completo em uma
listagem pré-match apenas por conveniência.

### Antes do Match

Dados identificadores e de contato são estritamente ocultos.

- **Empresa vendo candidatos:** exibir somente competências, formação/
  experiência anônima ou descrição genérica. **NUNCA** exibir nome, e-mail,
  CPF, telefone, LinkedIn ou endereço antes do match.
- **Candidato vendo vagas/empresas:** exibir somente título/descrição da vaga,
  requisitos, competências exigidas e localização genérica. **NUNCA** exibir
  razão social/nome da empresa, CNPJ ou e-mail de contato antes do match.

Crie DTOs/consultas de projeção anônima específicos quando necessário; não
envie `CandidateResponseDTO`, `CompanyResponseDTO` ou
`JobMatchResponseDTO` sem revisar seus campos e o contexto de autorização.

### Depois do Match

Somente os envolvidos no match podem visualizar mutuamente os dados de
identificação e contato liberados pela regra de negócio. A autorização deve
ser verificada no serviço/controlador, não apenas na tela.

## 7. Banco de Dados

As migrações reais ficam em
[`backend/src/main/resources/db/migration/`](./backend/src/main/resources/db/migration/):

- `V1__create_core_entities.sql`: países, competências, endereços, usuários,
  candidatos, empresas, vagas e tabelas de associação.
- `V2__fixing_address_and_user_relationship.sql`: move a relação de endereço
  para `addresses.user_id`.
- `V3__add_delete_cascade_on_user_relationships.sql`: cascatas de exclusão
  de usuários, endereços e competências.
- `V4__add_delete_cascade_on_job_relatioships.sql`: cascatas de vagas.
- `V5__add_like_and_match_tables.sql`: `companies_like`, `jobs_like` e
  `matches`.

Os dados de desenvolvimento ficam em
[`backend/src/main/resources/db/testdata/R__mock_data.sql`](./backend/src/main/resources/db/testdata/R__mock_data.sql).
O procedimento padrão é iniciar o PostgreSQL, definir a URL do banco
`linketinder` e executar:

```bash
cd backend
DB_URL=jdbc:postgresql://localhost:5433/linketinder \
DB_USER=postgres DB_PASSWORD=123456 ./gradlew flywayMigrate
```

Use `./gradlew -Penv=dev flywayMigrate` para incluir `db/testdata`; sem
`-Penv=dev`, o `build.gradle` limita o Flyway às migrações versionadas.
Para inspecionar o estado, use `./gradlew flywayInfo`; para validar, use
`./gradlew flywayValidate`.

## 8. Convenções de Código e Restrições

- Preserve a separação `controller -> service -> repository`; regras de
  negócio ficam nos serviços, SQL nas implementações `*RepositoryJDBC` e
  transformação de dados nos DTOs/mapeadores.
- Para persistência nova, adicione primeiro o contrato do repositório e depois
  a implementação JDBC, usando parâmetros (`?`) nas queries; não concatene
  entrada do usuário em SQL.
- Use Groovy 4 e os padrões já presentes: `@Canonical`, `@Builder`,
  `@EqualsAndHashCode`, records para DTOs e `UUID` para identificadores.
- Mantenha os nomes existentes: classes e métodos em `PascalCase`/`camelCase`,
  tabelas e colunas SQL em `snake_case` (respeitando nomes legados como
  `CEP`), interfaces com sufixo de contrato e implementações JDBC com
  `JDBC`.
- Migrações novas devem ser versionadas no padrão Flyway
  `V<versão>__<descricao>.sql`; dados repetíveis ficam em
  `R__<descricao>.sql`. Nunca edite uma migração já aplicada; crie outra.
- Escreva testes Spock próximos da camada alterada: `service` para regras de
  negócio, `reposistory` para JDBC e `user_interface` para fluxos de console.
  Inclua cenários de reciprocidade, remoção de curtida, duplicidade de match e
  anonimização quando tocar essas áreas.
- Feche recursos de banco pelos mecanismos existentes (`DatabaseFactory` e
  `close`) e preserve o pool HikariCP.
- Não adicione identificadores a respostas pré-match. Qualquer mudança em
  domínio, consulta, DTO, servlet ou UI deve ser revisada contra a seção de
  anonimato acima.
