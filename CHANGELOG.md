# Histórico de Alterações

Todas as alterações relevantes deste projeto serão documentadas neste arquivo.

O formato deste arquivo é baseado no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/),
e este projeto segue o [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [Não lançado]

### Adicionado

- Especificação da feature `Recrutador` em
  `docs/specs/recrutador/spec.md`, com cadastro independente, convites por
  vaga, autoria de curtidas, regras de match, modelo de dados, anonimato e
  critérios de aceite.
- `AGENTS.md` com o contexto do domínio Linketinder, a arquitetura do projeto,
  as convenções de desenvolvimento e o **Princípio do Anonimato**.
- A skill `skills/feature-com-spec/SKILL.md`, adaptada para o fluxo de
  especificação, planejamento, implementação e validação do Linketinder.
- `CHANGELOG.md` para documentar as alterações relevantes do projeto.
- Contexto inicial da base existente: backend em Groovy com Gradle, persistência
  PostgreSQL via JDBC e Flyway, servidor HTTP com Tomcat embutido, testes Spock
  e frontend separado.
- Migração Flyway V6 para criar o catálogo `recruitment_areas`, com seed inicial
  de `TI`, `RH` e `Marketing`, e a especialização `recruiters`, incluindo
  unicidade e validação estrutural do CPF, estados `ACTIVE`/`INACTIVE`,
  relacionamentos com `users` e áreas de recrutamento e exclusão em cascata.
- Migração Flyway V7 para criar convites e vínculos de recrutadores por vaga,
  com estados do ciclo de vida, timestamps, exclusão em cascata, índices e
  unicidade de convites pendentes ou aceitos.
- Auditoria de curtidas de recrutadores por vaga e candidato, com chave
  primária composta para idempotência, histórico de remoção via `removed_at` e
  índices de consulta, preservando a unicidade existente de `matches`.
- Garantia relacional entre convites e vagas: `company_id` deve corresponder ao
  `publisher_id` da vaga, validada por trigger no PostgreSQL.
- Compatibilidade com `companies_like` legado sem mistura de registros,
  idempotência de convites ativos e curtidas, preservação do histórico de
  remoção e estratégia consistente para criação e remoção de matches.
- Domínio do recrutador, estados de recrutador, convite e curtida, além dos
  DTOs de cadastro, atualização, autenticação e resposta sem senha.
- Validação dos contratos de entrada do recrutador, com normalização e
  validação de e-mail/CPF, idade mínima, senha mínima e área de recrutamento,
  incluindo testes automatizados e proteção contra exposição de dados
  sensíveis nas respostas.

### Modificado

- A skill `skills/feature-com-spec/SKILL.md` foi adaptada para armazenar
  especificações em `docs/specs/<nome-da-feature>/`, validar o Princípio do
  Anonimato e exigir atualizações deste changelog na seção `[Não lançado]`.

### Corrigido

- Nenhuma correção registrada.

### Removido

- Nenhuma remoção registrada.
