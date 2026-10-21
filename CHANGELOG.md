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
- Contratos e implementações JDBC para áreas de recrutamento e perfis de
  recrutador, incluindo busca parametrizada por e-mail, criação e atualização
  transacional, alteração de status e leitura do próprio perfil sem expor
  senha.
- `RecruiterRowMapper` e testes Spock do repositório de recrutador, cobrindo
  criação, leitura, ausência de recrutador, status e projeções sem credenciais.
- Serviço de recrutador para cadastro independente, validação de área e
  e-mail, atualização exclusiva do próprio perfil e bloqueio de contas
  inativas, com respostas sem senha, CPF ou data de nascimento.
- Autenticação de candidatos, empresas e recrutadores por estratégias
  específicas para CPF, CNPJ e e-mail, permitindo somente contas de
  recrutador ativas e preservando mensagens de erro sem exposição de
  credenciais.
- Tratamento explícito de falhas de autorização no controlador web, com
  resposta HTTP 403, além de testes Spock para cadastro, duplicidade,
  inatividade, senha incorreta e atualização indevida.
- Persistência JDBC e serviço do ciclo de convites de recrutadores por vaga,
  cobrindo criação idempotente, listagem por recrutador ou empresa, aceite,
  recusa, cancelamento e revogação com transições atômicas e autorização por
  proprietário da vaga ou destinatário correto.
- DTO específico de convite e testes Spock de repositório e serviço, sem
  exposição de senha, CPF, data de nascimento ou outros dados pessoais não
  necessários.
- Projeções anônimas para recrutadores, com listagem exclusiva de vagas que
  possuem vínculo `ACCEPTED` e candidatos associados à vaga sem carregar ou
  serializar nome, e-mail, CPF, telefone, LinkedIn ou endereço identificável.
- Autorização no serviço antes da consulta de candidatos, bloqueando
  recrutadores sem vínculo aceito, com DTOs específicos e testes Spock para
  acesso autorizado, convites pendentes ou revogados e campos sensíveis.
- Curtidas contextuais de recrutadores por vaga e candidato, com autorização
  de vínculo aceito, idempotência, reativação e descurtida auditável via
  `removed_at`.
- Integração das curtidas de recrutadores com a reciprocidade de candidatos,
  garantindo no máximo um match por candidato e vaga, preservando o match
  enquanto houver uma autoria ativa e removendo-o após a última remoção.
- Operação atômica de remoção condicional de match e DTO de resultado sem
  dados identificadores ou credenciais, com cobertura Spock de autorização,
  múltiplos recrutadores, reciprocidade e idempotência.

### Modificado

- A skill `skills/feature-com-spec/SKILL.md` foi adaptada para armazenar
  especificações em `docs/specs/<nome-da-feature>/`, validar o Princípio do
  Anonimato e exigir atualizações deste changelog na seção `[Não lançado]`.

### Corrigido

- Nenhuma correção registrada.

### Removido

- Nenhuma remoção registrada.
