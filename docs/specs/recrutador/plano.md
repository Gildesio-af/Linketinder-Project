# Plano de implementação — Recrutador

Este plano executa a especificação aprovada em
[`spec.md`](./spec.md), respeitando a separação existente entre Flyway,
back-end Groovy/JDBC e front-end TypeScript. Nenhuma tarefa deve reutilizar
DTO completo em contexto pré-match. Em cada tarefa implementada, o
[`CHANGELOG.md`](../../CHANGELOG.md) deve ser atualizado na seção
`[Não lançado]` antes de avançar para a próxima.

## Banco de dados

### Tarefa 1 — Criar o catálogo e a especialização de recrutadores

- [x] **O que será feito:** criar a migração Flyway versionada para as tabelas
  `recruitment_areas` e `recruiters`, incluindo UUID, chaves estrangeiras,
  unicidade de CPF, estados `ACTIVE`/`INACTIVE`, validações estruturais e seed
  inicial de `TI`, `RH` e `Marketing`. Não alterar migrações já aplicadas.
- **Camada afetada:** Banco de Dados.
- **Arquivos envolvidos:** criar
  `backend/src/main/resources/db/migration/V6__add_recruiters_and_recruitment_areas.sql`;
  modificar `CHANGELOG.md` na seção `[Não lançado]`.
- **Como validar:** executar
  `cd backend && ./gradlew flywayValidate` e, contra PostgreSQL limpo,
  `DB_URL=jdbc:postgresql://localhost:5433/linketinder DB_USER=postgres DB_PASSWORD=123456 ./gradlew flywayMigrate`.
  Conferir com SQL que o seed contém as três áreas, que CPF é único e que
  nenhum campo de senha, CPF ou data de nascimento aparece em qualquer
  consulta de projeção de candidato/recrutador.

### Tarefa 2 — Criar convites, vínculos e auditoria de curtidas

- [x] **O que será feito:** adicionar as tabelas
  `recruiter_job_invitations` e `recruiter_job_likes`, seus estados,
  timestamps, chaves estrangeiras com cascata, índices e restrições para
  impedir convite `PENDING`/`ACCEPTED` duplicado e garantir uma curtida por
  recrutador, vaga e candidato. Preservar o histórico via `removed_at` e
  manter a unicidade existente de `matches`.
- **Camada afetada:** Banco de Dados.
- **Arquivos envolvidos:** criar
  `backend/src/main/resources/db/migration/V7__add_recruiter_job_invitations_and_likes.sql`;
  modificar `CHANGELOG.md` na seção `[Não lançado]`.
- **Como validar:** executar
  `cd backend && ./gradlew flywayValidate && ./gradlew test`;
  aplicar a migração em banco de teste e verificar com SQL os índices,
  foreign keys, unicidade de convite ativo e chave primária composta da
  curtida. Confirmar que nenhuma tabela/consulta nova fornece dados
  identificadores em listagens pré-match.

### Tarefa 3 — Verificar integridade relacional e compatibilidade do legado

- [x] **O que será feito:** definir na migração ou no contrato de persistência
  a garantia de que `company_id` do convite corresponde a
  `jobs.publisher_id`, sem apagar nem misturar registros legados de
  `companies_like`; documentar a estratégia adotada para concorrência,
  reativação de curtida e criação/remoção de match.
- **Camada afetada:** Banco de Dados.
- **Arquivos envolvidos:** modificar
  `backend/src/main/resources/db/migration/V7__add_recruiter_job_invitations_and_likes.sql`;
  modificar `backend/src/main/resources/db/testdata/R__mock_data.sql` somente
  se forem necessários dados recorrentes da feature; modificar `CHANGELOG.md`
  na seção `[Não lançado]`.
- **Como validar:** executar
  `cd backend && ./gradlew flywayValidate`;
  carregar o ambiente de desenvolvimento com
  `DB_URL=jdbc:postgresql://localhost:5433/linketinder DB_USER=postgres DB_PASSWORD=123456 ./gradlew -Penv=dev flywayMigrate`
  e tentar inserir convite de vaga pertencente a outra empresa, convite
  duplicado e duas curtidas simultâneas. Todas devem falhar ou ser
  idempotentes sem expor dados pessoais em respostas.

## Back-end em Groovy

### Tarefa 4 — Modelar recrutador, estados e contratos de entrada

- [x] **O que será feito:** criar o domínio do recrutador e tipos para status
  de recrutador, convite e curtida, além dos DTOs de cadastro, atualização,
  autenticação e respostas próprias sem senha. Implementar validação de
  e-mail/CPF normalizados, idade mínima de 18 anos, senha mínima de seis
  caracteres e área existente.
- **Camada afetada:** Back-end.
- **Arquivos envolvidos:** criar
  `backend/src/main/groovy/zg/acelera/domain/Recruiter.groovy`,
  `backend/src/main/groovy/zg/acelera/domain/RecruiterStatus.groovy`,
  `backend/src/main/groovy/zg/acelera/domain/RecruiterJobInvitation.groovy`,
  `backend/src/main/groovy/zg/acelera/domain/RecruiterJobLike.groovy`,
  `backend/src/main/groovy/zg/acelera/dto/recruiter/RecruiterCreateDTO.groovy`,
  `backend/src/main/groovy/zg/acelera/dto/recruiter/RecruiterUpdateDTO.groovy`,
  `backend/src/main/groovy/zg/acelera/dto/recruiter/RecruiterResponseDTO.groovy`,
  `backend/src/main/groovy/zg/acelera/dto/recruiter/RecruiterAuthResponseDTO.groovy`;
  modificar `CHANGELOG.md` na seção `[Não lançado]`.
- **Como validar:** criar
  `backend/src/test/groovy/service/RecruiterValidationSpec.groovy` e
  executar `cd backend && ./gradlew test --tests '*RecruiterValidationSpec'`.
  Verificar que CPF, data de nascimento, senha e e-mail inválidos são
  rejeitados antes da persistência e que nenhuma resposta serializada contém
  senha, CPF ou data de nascimento.

### Tarefa 5 — Implementar contratos e JDBC de recrutador e áreas

- [x] **O que será feito:** criar interfaces e implementações JDBC para
  consultar áreas, localizar recrutador por e-mail, criar/atualizar perfil,
  alterar status e obter os dados do próprio recrutador usando parâmetros
  JDBC. Reutilizar `users` para a identidade global e preservar a separação
  repository/service.
- **Camada afetada:** Back-end.
- **Arquivos envolvidos:** criar
  `backend/src/main/groovy/zg/acelera/repository/recruiter/RecruitmentAreaRepository.groovy`,
  `backend/src/main/groovy/zg/acelera/repository/recruiter/RecruitmentAreaRepositoryJDBC.groovy`,
  `backend/src/main/groovy/zg/acelera/repository/recruiter/RecruiterRepository.groovy`,
  `backend/src/main/groovy/zg/acelera/repository/recruiter/RecruiterRepositoryJDBC.groovy`,
  `backend/src/main/groovy/zg/acelera/utils/mapper/RecruiterRowMapper.groovy`;
  criar `backend/src/test/groovy/reposistory/RecruiterRepositoryJDBCSpec.groovy`;
  modificar `CHANGELOG.md` na seção `[Não lançado]`.
- **Como validar:** executar
  `cd backend && ./gradlew test --tests '*RecruiterRepositoryJDBCSpec'`;
  verificar criação e leitura em banco de teste, unicidade global de e-mail e
  CPF, área inexistente, status inativo e ausência de senha nas projeções.

### Tarefa 6 — Implementar cadastro, atualização e autenticação

- [x] **O que foi feito:** criado o serviço de recrutador e integrada a
  `AuthService` para cadastro independente, atualização do próprio perfil e
  autenticação apenas de conta ativa. Propagar erros de validação e
  autorização sem capturas amplas e sem retornar credenciais.
- **Camada afetada:** Back-end.
- **Arquivos envolvidos:** criar
  `backend/src/main/groovy/zg/acelera/service/RecruiterService.groovy`;
  modificar `backend/src/main/groovy/zg/acelera/service/AuthService.groovy`,
  `backend/src/main/groovy/zg/acelera/dto/auth/AuthResponseDTO.groovy`,
  `backend/src/main/groovy/zg/acelera/controller/BaseWebController.groovy`;
  criar `backend/src/test/groovy/service/RecruiterServiceSpec.groovy`;
  modificar `backend/src/test/groovy/service/AuthServiceSpec.groovy` e
  `CHANGELOG.md` na seção `[Não lançado]`.
- **Como validar:** executar
  `cd backend && ./gradlew test --tests '*RecruiterServiceSpec' --tests '*AuthServiceSpec'`.
  Cobrir cadastro válido, duplicidades, conta inativa, senha incorreta e
  atualização de outro usuário; inspecionar as respostas para confirmar que
  não contêm senha, CPF ou data de nascimento.

### Tarefa 7 — Implementar persistência e serviço do ciclo de convites

- [x] **O que será feito:** criar contratos e JDBC para criar, listar,
  aceitar, recusar, cancelar e revogar convites; criar o serviço que valida
  empresa proprietária da vaga, recrutador destinatário, estados permitidos,
  duplicidade e transições atômicas. Listagens devem retornar somente campos
  necessários ao destinatário ou administrador autorizado.
- **Camada afetada:** Back-end.
- **Arquivos envolvidos:** criar
  `backend/src/main/groovy/zg/acelera/repository/recruiter/RecruiterJobInvitationRepository.groovy`,
  `backend/src/main/groovy/zg/acelera/repository/recruiter/RecruiterJobInvitationRepositoryJDBC.groovy`,
  `backend/src/main/groovy/zg/acelera/service/RecruiterInvitationService.groovy`,
  `backend/src/main/groovy/zg/acelera/dto/recruiter/RecruiterInvitationDTO.groovy`;
  criar `backend/src/test/groovy/reposistory/RecruiterJobInvitationRepositoryJDBCSpec.groovy`
  e `backend/src/test/groovy/service/RecruiterInvitationServiceSpec.groovy`;
  modificar `CHANGELOG.md` na seção `[Não lançado]`.
- **Como validar:** executar
  `cd backend && ./gradlew test --tests '*RecruiterJobInvitationRepositoryJDBCSpec' --tests '*RecruiterInvitationServiceSpec'`.
  Verificar todas as transições válidas e inválidas, autorização da empresa,
  aceite pelo recrutador correto, convite duplicado e bloqueio imediato após
  revogação; confirmar que a resposta não cria diretório público nem expõe
  CPF, senha ou dados não necessários.

### Tarefa 8 — Implementar projeções anônimas e autorização de candidatos

- [x] **O que foi feito:** criada consulta/DTO específicos para o recrutador
  listar apenas vagas com vínculo `ACCEPTED` e candidatos anonimizados da
  vaga. Aplicar a autorização no serviço antes da consulta e garantir que
  nome, e-mail, CPF, telefone, LinkedIn e endereço identificável não sejam
  carregados nem serializados antes do match.
- **Camada afetada:** Back-end.
- **Arquivos envolvidos:** criar
  `backend/src/main/groovy/zg/acelera/dto/candidate/AnonymousCandidateResponseDTO.groovy`,
  `backend/src/main/groovy/zg/acelera/dto/job/RecruiterJobResponseDTO.groovy`;
  modificar `backend/src/main/groovy/zg/acelera/repository/candidate/CandidateRepository.groovy`,
  `backend/src/main/groovy/zg/acelera/repository/candidate/CandidateRepositoryJDBC.groovy`,
  `backend/src/main/groovy/zg/acelera/service/CandidateService.groovy`;
  criar `backend/src/test/groovy/service/RecruiterCandidateVisibilitySpec.groovy`;
  modificar `CHANGELOG.md` na seção `[Não lançado]`.
- **Como validar:** executar
  `cd backend && ./gradlew test --tests '*RecruiterCandidateVisibilitySpec'`;
  consultar vaga sem vínculo, convite pendente e vínculo revogado para
  confirmar erro de autorização, e vaga aceita para confirmar que apenas a
  projeção anônima é retornada. Verificar explicitamente os nomes dos campos
  serializados.

### Tarefa 9 — Implementar curtidas contextuais e regras de match

- [ ] **O que será feito:** criar repositório, DTO e serviço para curtida de
  recrutador por vaga/candidato, com idempotência, reativação, descurtida
  auditável e autorização de vínculo ativo. Integrar a curtida do candidato
  com a criação de um único match por candidato e vaga, mantendo o match
  enquanto existir ao menos uma curtida ativa de recrutador e removendo-o após
  a última remoção.
- **Camada afetada:** Back-end.
- **Arquivos envolvidos:** criar
  `backend/src/main/groovy/zg/acelera/repository/recruiter/RecruiterJobLikeRepository.groovy`,
  `backend/src/main/groovy/zg/acelera/repository/recruiter/RecruiterJobLikeRepositoryJDBC.groovy`,
  `backend/src/main/groovy/zg/acelera/service/RecruiterJobLikeService.groovy`,
  `backend/src/main/groovy/zg/acelera/dto/recruiter/RecruiterLikeResultDTO.groovy`;
  modificar `backend/src/main/groovy/zg/acelera/service/JobLikeService.groovy`,
  `backend/src/main/groovy/zg/acelera/service/MatchService.groovy`,
  `backend/src/main/groovy/zg/acelera/repository/match/MatchRepository.groovy`,
  `backend/src/main/groovy/zg/acelera/repository/match/MatchRepositoryJDBC.groovy`;
  criar `backend/src/test/groovy/reposistory/RecruiterJobLikeRepositoryJDBCSpec.groovy`
  e `backend/src/test/groovy/service/RecruiterJobLikeServiceSpec.groovy`;
  modificar os testes de `JobLikeService`, `MatchService` e `CHANGELOG.md`.
- **Como validar:** executar
  `cd backend && ./gradlew test --tests '*RecruiterJobLikeRepositoryJDBCSpec' --tests '*RecruiterJobLikeServiceSpec' --tests '*JobLikeServiceSpec' --tests '*MatchServiceSpec'`.
  Cobrir vínculo ausente/revogado, curtida repetida, dois recrutadores,
  reciprocidade, descurtida da primeira e da última autoria. Confirmar por
  consulta que há duas autorias, no máximo um match e nenhum dado
  identificador em respostas pré-match.

### Tarefa 10 — Criar contratos pós-match e proteger a autorização

- [ ] **O que será feito:** criar a projeção de match autorizado e ajustar a
  consulta de detalhes para permitir acesso apenas ao candidato, à empresa
  proprietária ou ao recrutador ainda vinculado à vaga. Liberar somente os
  campos aprovados por contexto; nunca retornar CPF, data de nascimento ou
  senha do recrutador, nem e-mail do recrutador ao candidato.
- **Camada afetada:** Back-end.
- **Arquivos envolvidos:** criar
  `backend/src/main/groovy/zg/acelera/dto/match/AuthorizedMatchResponseDTO.groovy`;
  modificar `backend/src/main/groovy/zg/acelera/service/MatchService.groovy`,
  `backend/src/main/groovy/zg/acelera/controller/MatchController.groovy`;
  criar `backend/src/test/groovy/service/AuthorizedMatchVisibilitySpec.groovy`;
  modificar `CHANGELOG.md` na seção `[Não lançado]`.
- **Como validar:** executar
  `cd backend && ./gradlew test --tests '*AuthorizedMatchVisibilitySpec'`;
  solicitar match pertencente e não pertencente ao usuário, antes e depois
  da revogação, verificando autorização, área/descrição liberadas ao
  candidato quando aplicável e ausência permanente de CPF, data de nascimento,
  senha e e-mail pessoal do recrutador.

### Tarefa 11 — Expor endpoints e integrar a composição web

- [ ] **O que será feito:** criar controladores HTTP para cadastro, perfil,
  autenticação, convites, vagas aceitas, candidatos anônimos, curtidas e
  matches; padronizar parsing de UUID, erros e sessão/autorização. Registrar
  as rotas no menu web sem quebrar candidatos, empresas e vagas existentes.
- **Camada afetada:** Back-end.
- **Arquivos envolvidos:** criar
  `backend/src/main/groovy/zg/acelera/controller/RecruiterWebController.groovy`,
  `backend/src/main/groovy/zg/acelera/controller/RecruiterInvitationWebController.groovy`,
  `backend/src/main/groovy/zg/acelera/controller/RecruiterLikeWebController.groovy`;
  modificar `backend/src/main/groovy/zg/acelera/app/MainWebMenu.groovy`,
  `backend/src/main/groovy/zg/acelera/controller/AuthController.groovy`,
  `backend/src/main/groovy/zg/acelera/controller/LikeController.groovy`;
  criar `backend/src/test/groovy/controller/RecruiterWebControllerSpec.groovy`;
  modificar `CHANGELOG.md` na seção `[Não lançado]`.
- **Como validar:** executar
  `cd backend && ./gradlew test --tests '*RecruiterWebControllerSpec'`;
  iniciar o servidor pela configuração `zg.acelera.MainWeb` após
  `./gradlew classes` e testar manualmente `POST/GET/PATCH` dos endpoints
  com recrutador, empresa e usuário não autorizado. Confirmar respostas
  anônimas pré-match e bloqueio no backend, mesmo sem a UI.

### Tarefa 12 — Executar regressão completa do back-end

- [ ] **O que será feito:** revisar os testes de cadastro/login de candidato e
  empresa, vagas, filtros/recomendações, curtidas legadas, matches e
  encerramento/tratamento de erro; adicionar apenas ajustes diretamente
  necessários para compatibilidade com recrutadores.
- **Camada afetada:** Back-end.
- **Arquivos envolvidos:** modificar somente os testes existentes nas pastas
  `backend/src/test/groovy/service/`,
  `backend/src/test/groovy/reposistory/` e os arquivos de produção diretamente
  envolvidos; modificar `CHANGELOG.md` na seção `[Não lançado]`.
- **Como validar:** executar `cd backend && ./gradlew test && ./gradlew build`.
  Repetir manualmente os fluxos existentes de candidato e empresa e revisar
  as respostas pré-match para garantir que a adição de recrutadores não
  reintroduziu nome, e-mail, CPF, CNPJ, telefone ou endereço identificável.

## Front-end em TypeScript

### Tarefa 13 — Criar modelos, estado de sessão e cliente HTTP do recrutador

- [ ] **O que será feito:** adicionar tipos TypeScript para recrutador,
  convite, vaga atribuída, candidato anônimo, curtida e match; criar serviço
  HTTP com tratamento explícito de erros e estado de sessão por perfil, sem
  persistir senha nem dados sensíveis. Manter os modelos existentes de
  candidato, empresa e vaga compatíveis.
- **Camada afetada:** Front-end.
- **Arquivos envolvidos:** modificar
  `frontend/ts/models/Domain.ts`,
  `frontend/ts/services/storage/StorageService.ts`;
  criar `frontend/ts/services/recruiter/RecruiterService.ts`,
  `frontend/ts/services/recruiter/RecruiterSessionService.ts`;
  modificar `frontend/ts/tsconfig.json` e `CHANGELOG.md` na seção
  `[Não lançado]`.
- **Como validar:** executar
  `cd frontend/ts && npx tsc --noEmit -p tsconfig.json` (ou o comando
  equivalente documentado pelo projeto se o compilador estiver instalado
  localmente). Inspecionar o estado persistido para confirmar ausência de
  senha, CPF e data de nascimento e simular resposta de erro HTTP sem
  fallback de sucesso.

### Tarefa 14 — Implementar cadastro e login de recrutador

- [ ] **O que será feito:** criar tela de cadastro com área, descrição,
  e-mail, CPF, data de nascimento e senha, além do fluxo de login e logout;
  validar campos no formulário e consumir os endpoints sem duplicar a regra
  de autorização do back-end. Não exibir nem manter senha após o envio.
- **Camada afetada:** Front-end.
- **Arquivos envolvidos:** criar
  `frontend/pages/recruiter_registration.html`,
  `frontend/pages/recruiter_login.html`,
  `frontend/ts/pages/recruiter/RecruiterRegistration.ts`,
  `frontend/ts/pages/recruiter/RecruiterLogin.ts`,
  `frontend/css/recruiter_registration.css`,
  `frontend/css/recruiter_login.css`;
  gerar os artefatos `frontend/ts/pages/recruiter/RecruiterRegistration.js`,
  `frontend/ts/pages/recruiter/RecruiterLogin.js` conforme o fluxo atual de
  compilação; modificar `frontend/index.html`, `frontend/pages/login.html` e
  `CHANGELOG.md` na seção `[Não lançado]`.
- **Como validar:** executar
  `cd frontend/ts && npx tsc --noEmit -p tsconfig.json`; iniciar o backend e
  verificar manualmente cadastro válido e inválido, login ativo/inativo e
  logout. Confirmar no DOM, `localStorage` e rede que senha, CPF e data de
  nascimento não aparecem em respostas/telas fora do formulário próprio.

### Tarefa 15 — Implementar painel de convites do recrutador

- [ ] **O que será feito:** criar fluxo para listar convites destinados ao
  recrutador, aceitar/recusar convite próprio, atualizar estados e exibir
  somente vaga, empresa e dados permitidos pelo contrato. Tratar estados
  cancelado/revogado e erros de autorização sem deixar ações antigas
  disponíveis em cache.
- **Camada afetada:** Front-end.
- **Arquivos envolvidos:** criar
  `frontend/pages/recruiter_invitations.html`,
  `frontend/ts/pages/recruiter/RecruiterInvitations.ts`,
  `frontend/css/recruiter_invitations.css`;
  modificar `frontend/ts/services/recruiter/RecruiterService.ts`,
  `frontend/ts/services/storage/StorageService.ts` e
  `CHANGELOG.md` na seção `[Não lançado]`; gerar
  `frontend/ts/pages/recruiter/RecruiterInvitations.js`.
- **Como validar:** executar
  `cd frontend/ts && npx tsc --noEmit -p tsconfig.json`; com o backend ativo,
  aceitar e recusar convite próprio, tentar agir em convite de outro usuário e
  atualizar após revogação. Conferir manualmente que a tela não exibe CPF,
  senha, data de nascimento ou diretório de recrutadores.

### Tarefa 16 — Implementar gestão de convites pela empresa

- [ ] **O que será feito:** adicionar à área da empresa busca pontual por
  e-mail, envio de convite para vaga própria, listagem de convites, cancelamento
  pendente e revogação aceita. Exibir apenas os dados administrativos mínimos
  autorizados, sem transformar a busca em diretório público.
- **Camada afetada:** Front-end.
- **Arquivos envolvidos:** modificar
  `frontend/ts/pages/company_menu/CompanyMenu.ts`,
  `frontend/pages/company_menu.html`,
  `frontend/css/global.css`;
  criar `frontend/ts/pages/company_menu/RecruiterInvitationManagement.ts`;
  modificar `frontend/ts/services/recruiter/RecruiterService.ts` e
  `CHANGELOG.md` na seção `[Não lançado]`; gerar
  `frontend/ts/pages/company_menu/RecruiterInvitationManagement.js`.
- **Como validar:** executar
  `cd frontend/ts && npx tsc --noEmit -p tsconfig.json`; testar manualmente
  convite para recrutador inexistente, vaga de outra empresa, duplicidade,
  cancelamento e revogação. Confirmar que a UI não oferece nome, CPF, data de
  nascimento ou outros campos além dos necessários ao convite/gestão.

### Tarefa 17 — Implementar vagas aceitas, candidatos anônimos e curtidas

- [ ] **O que será feito:** criar painel do recrutador com somente vagas de
  vínculo `ACCEPTED`, listagem de candidatos anônimos, ação idempotente de
  curtir/descurtir por vaga e atualização visual após erro de autorização ou
  revogação. Não exibir identificadores antes do match.
- **Camada afetada:** Front-end.
- **Arquivos envolvidos:** criar
  `frontend/pages/recruiter_dashboard.html`,
  `frontend/ts/pages/recruiter/RecruiterDashboard.ts`,
  `frontend/css/recruiter_dashboard.css`;
  modificar `frontend/ts/services/recruiter/RecruiterService.ts`,
  `frontend/ts/models/Domain.ts` e `CHANGELOG.md` na seção `[Não lançado]`;
  gerar `frontend/ts/pages/recruiter/RecruiterDashboard.js`.
- **Como validar:** executar
  `cd frontend/ts && npx tsc --noEmit -p tsconfig.json`; com dados de teste,
  verificar que convite pendente, recusado, cancelado ou revogado não permite
  consulta/curtida, que duas curtidas não duplicam a ação e que a tela não
  mostra nome, e-mail, CPF, telefone, LinkedIn ou endereço identificável.

### Tarefa 18 — Integrar matches autorizados e preservar telas existentes

- [ ] **O que será feito:** exibir no painel do recrutador apenas matches dos
  quais ele participa e apenas campos liberados; adaptar o fluxo de match do
  candidato/empresa às respostas do backend sem expor dados pré-match.
  Substituir somente chamadas de `localStorage` relacionadas ao fluxo novo e
  manter cadastro, login, vagas, curtidas e matches legados funcionando.
- **Camada afetada:** Front-end.
- **Arquivos envolvidos:** modificar
  `frontend/ts/services/match/MatchService.ts`,
  `frontend/ts/pages/jobs/Jobs.ts`,
  `frontend/ts/pages/company_menu/CompanyMenu.ts`,
  `frontend/ts/models/Domain.ts`, as páginas HTML/CSS diretamente afetadas e
  `frontend/ts/services/storage/StorageService.ts`; modificar
  `CHANGELOG.md` na seção `[Não lançado]`.
- **Como validar:** executar
  `cd frontend/ts && npx tsc --noEmit -p tsconfig.json`; iniciar backend e
  front-end, percorrer cadastro/login, vagas, convite, aceite, curtida,
  reciprocidade e match com candidato, empresa e recrutador. Tentar abrir um
  match alheio e revogar vínculo durante a sessão; confirmar bloqueio e que
  CPF, data de nascimento, senha e e-mail pessoal do recrutador nunca são
  renderizados.

### Tarefa 19 — Validar a entrega ponta a ponta e a matriz de aceite

- [ ] **O que será feito:** executar a validação final de todos os critérios
  CA-01 a CA-18, registrar evidências dos testes de banco, serviços,
  repositórios, controladores e fluxos manuais, revisar regressões de
  candidato/empresa e confirmar documentação/changelog de cada tarefa.
- **Camada afetada:** Banco de Dados, Back-end e Front-end.
- **Arquivos envolvidos:** revisar
  `docs/specs/recrutador/spec.md`,
  `docs/specs/recrutador/plano.md`,
  `CHANGELOG.md`, todos os testes criados em
  `backend/src/test/groovy/` e os arquivos TypeScript/HTML/CSS da feature;
  criar ou atualizar apenas a documentação de evidências solicitada durante
  a implementação.
- **Como validar:** executar `cd backend && ./gradlew test && ./gradlew build`;
  executar `cd frontend/ts && npx tsc --noEmit -p tsconfig.json`; repetir
  `flywayValidate` e a migração em banco limpo. Fazer o roteiro manual
  cadastro → convite → aceite → vaga → candidato anônimo → curtida →
  reciprocidade → match → descurtida/revogação e verificar, em cada resposta
  e tela, autorização no backend e preservação integral do Princípio do
  Anonimato.
