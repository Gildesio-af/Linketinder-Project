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

### Modificado

- A skill `skills/feature-com-spec/SKILL.md` foi adaptada para armazenar
  especificações em `docs/specs/<nome-da-feature>/`, validar o Princípio do
  Anonimato e exigir atualizações deste changelog na seção `[Não lançado]`.

### Corrigido

- Nenhuma correção registrada.

### Removido

- Nenhuma remoção registrada.
