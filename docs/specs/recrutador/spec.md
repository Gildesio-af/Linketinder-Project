# Feature: O Recrutador

**Status:** Especificação validada pelo Tutor
**Data:** 2026-10-21
**Etapa:** Especificação
**Implementação:** Backend/BD e frontend serão entregues separadamente

## 1. Contexto e objetivo

O Linketinder atualmente possui candidatos, empresas, vagas, curtidas e
matches. A curtida de candidato é hoje atribuída diretamente à empresa e não
identifica a pessoa que realizou a ação. Uma empresa também não possui um
mecanismo de atribuição de uma vaga a um colaborador.

Esta feature cria o papel de **Recrutador**. O recrutador possui cadastro
próprio na Linketinder e pode atuar em vagas específicas após aceitar um
convite da empresa publicadora. A empresa não cadastra os dados pessoais do
recrutador: ela apenas localiza o cadastro existente pelo e-mail e envia o
convite.

Objetivos:

- permitir o cadastro independente de recrutadores;
- permitir que uma empresa convide um recrutador para uma vaga específica;
- controlar aceite, recusa, cancelamento e revogação do vínculo;
- restringir a análise e a curtida às vagas aceitas pelo recrutador;
- registrar a autoria individual das curtidas;
- manter um único match por candidato e vaga;
- preservar o Princípio do Anonimato e a minimização de dados.

### Escopo da entrega

Esta especificação é única para manter regras e contratos consistentes, mas
separa as entregas:

- **Backend/BD:** domínio, migrações Flyway, repositórios JDBC, serviços,
  validações, autorização e endpoints.
- **Frontend:** telas, estado de sessão, cadastro, convites, atribuição e
  consumo dos endpoints.

As duas partes podem ser implementadas em etapas independentes, desde que
respeitem os contratos e estados definidos aqui.

## 2. Atores e cenários de usuário

### Atores

- **Recrutador:** cadastra e mantém seu próprio perfil, visualiza convites,
  aceita ou recusa convites e trabalha em vagas aceitas.
- **Empresa administradora:** é a conta `Company` existente. Convida
  recrutadores para vagas próprias, cancela convites pendentes, revoga
  vínculos aceitos e consulta a autoria das curtidas.
- **Candidato:** visualiza vagas anonimizadas, curte vagas e acessa dados
  autorizados somente depois de um match.
- **Sistema:** valida vínculos, impede acesso indevido, registra estados e
  calcula a existência do match.

### Cenários principais

1. Um recrutador cria sua conta sem vínculo prévio com uma empresa.
2. Uma empresa localiza o recrutador pelo e-mail e o convida para uma vaga
   que ela publicou.
3. O recrutador aceita o convite no próprio painel e passa a atuar somente
   naquela vaga.
4. O recrutador visualiza candidatos em projeção anônima e curte um candidato
   para a vaga aceita.
5. Quando o candidato também curtiu a mesma vaga, o sistema cria um match.
6. Uma empresa pode convidar o mesmo recrutador para vagas diferentes e o
   recrutador pode aceitar convites de várias empresas.
7. A empresa revoga um vínculo; o recrutador perde imediatamente a autorização
   para novas consultas e curtidas naquela vaga.

## 3. Regras de negócio

### 3.1 Cadastro e identidade

1. O recrutador se cadastra diretamente na Linketinder, sem convite prévio.
2. O cadastro contém nome, área de recrutamento, descrição, e-mail, CPF, data
   de nascimento e senha.
3. O recrutador pode estar vinculado a várias empresas e vagas.
4. E-mail e CPF são normalizados e únicos globalmente entre candidatos,
   empresas e recrutadores.
5. A data de nascimento deve ser válida e representar idade mínima de 18 anos.
6. A senha deve possuir pelo menos 6 caracteres. Nesta etapa ela permanece
   sem hash, conforme decisão do Tutor; isso é uma limitação explícita de
   segurança e deverá ser revista antes de produção.
7. A área de recrutamento é obrigatória e deve existir no catálogo de áreas.
8. A conta do recrutador possui estado ativo/inativo. Uma conta inativa não
   pode autenticar nem executar ações.

### 3.2 Convite e vínculo com vaga

1. A empresa administra somente vagas cujo `publisher_id` seja seu próprio
   identificador.
2. O convite é enviado a um recrutador já cadastrado, localizado pelo e-mail.
3. O convite sempre referencia uma vaga específica; não existe convite amplo
   para toda a empresa nesta feature.
4. Somente o administrador da empresa pode enviar, cancelar ou revogar
   convites/vínculos.
5. O recrutador visualiza convites no próprio painel e pode aceitar ou recusar.
6. O aceite transforma o convite em vínculo ativo para aquela vaga.
7. Enquanto o convite estiver pendente, o recrutador não pode analisar
   candidatos nem curtir.
8. Um convite não pode ser duplicado para o mesmo recrutador e vaga enquanto
   houver convite pendente ou vínculo ativo.
9. Após recusa ou cancelamento, a empresa pode enviar novo convite.
10. A empresa pode revogar um vínculo aceito. A revogação bloqueia
    imediatamente novas ações do recrutador naquela vaga.
11. O histórico de convites e ações não deve ser apagado por cancelamento ou
    revogação.
12. Um recrutador pode aceitar convites de várias empresas, desde que cada
    vínculo seja avaliado separadamente.

### 3.3 Vagas, candidatos e curtidas

1. O recrutador só pode analisar candidatos de vagas para as quais tenha
   vínculo ativo.
2. A curtida do recrutador é contextual à vaga e registra recrutador, vaga e
   candidato.
3. O recrutador não pode curtir candidato em vaga de outra empresa, vaga não
   atribuída, convite pendente, vínculo recusado/cancelado/revogado ou vaga
   inexistente.
4. A curtida do candidato continua sendo registrada para a vaga em
   `jobs_like(candidate_id, job_id)`.
5. Curtidas repetidas do mesmo recrutador para o mesmo candidato e vaga são
   idempotentes.
6. A descurtida do recrutador remove sua curtida efetiva, mas preserva o
   registro de auditoria.
7. Se vários recrutadores curtirem o mesmo candidato na mesma vaga, cada
   autoria será mantida, mas haverá no máximo um match para o par
   candidato-vaga.
8. O match existe quando:
   - o candidato curtiu a vaga; e
   - pelo menos um recrutador com vínculo ativo naquela vaga curtiu aquele
     candidato.
9. O match permanece enquanto houver pelo menos uma curtida ativa de
   recrutador. A remoção da última curtida ativa desfaz o match.
10. A criação e a remoção de match devem ser consistentes com as curtidas e
    respeitar a unicidade existente por candidato e vaga.

### 3.4 Validação e autorização

1. Todas as regras de autorização devem ser aplicadas no serviço/controlador,
   nunca somente na interface.
2. IDs devem ser UUID válidos e entradas textuais devem respeitar limites
   definidos pelos DTOs e pelo banco.
3. CPF deve conter exatamente 11 dígitos após normalização.
4. E-mail deve ser validado, normalizado e comparado sem distinção de caixa.
5. A área deve ser uma opção existente em `recruitment_areas`.
6. A empresa só pode operar sobre seus próprios convites e vagas.
7. O recrutador só pode aceitar/recusar convites destinados a ele.
8. Uma conta ou vínculo revogado não pode executar ações novas, mesmo que a
   interface ainda mantenha dados antigos em cache.

## 4. Modelo de dados

Os nomes abaixo são o contrato relacional proposto para uma nova migração
Flyway. Migrações já aplicadas não devem ser editadas.

### 4.1 Entidades novas

#### `recruitment_areas`

Catálogo inicial de áreas de recrutamento.

| Campo | Tipo | Constraints |
|---|---|---|
| `id` | `UUID` | `PRIMARY KEY`, default `gen_random_uuid()` |
| `name` | `VARCHAR(50)` | `NOT NULL`, `UNIQUE` |

O seed inicial deve conter pelo menos `TI`, `RH` e `Marketing`. CRUD do
catálogo fica fora desta feature.

#### `recruiters`

Especialização de `users` para o perfil recrutador.

| Campo | Tipo | Constraints |
|---|---|---|
| `user_id` | `UUID` | `PRIMARY KEY`, FK `users(id)`, `ON DELETE CASCADE` |
| `recruitment_area_id` | `UUID` | `NOT NULL`, FK `recruitment_areas(id)` |
| `cpf` | `CHAR(11)` | `NOT NULL`, `UNIQUE`, exatamente 11 dígitos |
| `birth_date` | `DATE` | `NOT NULL`, idade mínima de 18 anos validada na borda |
| `status` | `VARCHAR(20)` | `NOT NULL`, default `ACTIVE`, valores `ACTIVE`/`INACTIVE` |

Os campos comuns ficam em `users`: `id`, `name`, `email`, `password` e
`description`. A coluna `users.email` existente deve continuar sendo a
identidade de login e precisa suportar unicidade global entre os perfis.

#### `recruiter_job_invitations`

Convite e ciclo de vida do vínculo entre recrutador, empresa e vaga.

| Campo | Tipo | Constraints |
|---|---|---|
| `id` | `UUID` | `PRIMARY KEY`, default `gen_random_uuid()` |
| `recruiter_id` | `UUID` | `NOT NULL`, FK `recruiters(user_id)`, `ON DELETE CASCADE` |
| `company_id` | `UUID` | `NOT NULL`, FK `companies(user_id)`, `ON DELETE CASCADE` |
| `job_id` | `UUID` | `NOT NULL`, FK `jobs(id)`, `ON DELETE CASCADE` |
| `status` | `VARCHAR(20)` | `NOT NULL`, valores `PENDING`, `ACCEPTED`, `DECLINED`, `CANCELLED`, `REVOKED` |
| `created_at` | `TIMESTAMP` | `NOT NULL`, default `CURRENT_TIMESTAMP` |
| `responded_at` | `TIMESTAMP` | nullable |
| `revoked_at` | `TIMESTAMP` | nullable |

Relacionamentos e constraints adicionais:

- `company_id` deve ser igual a `jobs.publisher_id`; essa regra deve ser
  garantida pelo serviço e, se necessário, por chave/trigger compatível com o
  PostgreSQL usado pelo projeto.
- Índice para `(recruiter_id, status)` e `(job_id, status)`.
- Não pode existir mais de um convite `PENDING` ou `ACCEPTED` para o mesmo
  `(recruiter_id, job_id)`.
- O estado `ACCEPTED` representa o vínculo ativo; `REVOKED` encerra a
  autorização sem apagar o histórico.

#### `recruiter_job_likes`

Autoria das curtidas feitas por recrutadores.

| Campo | Tipo | Constraints |
|---|---|---|
| `recruiter_id` | `UUID` | `NOT NULL`, FK `recruiters(user_id)`, `ON DELETE CASCADE` |
| `job_id` | `UUID` | `NOT NULL`, FK `jobs(id)`, `ON DELETE CASCADE` |
| `candidate_id` | `UUID` | `NOT NULL`, FK `candidates(user_id)`, `ON DELETE CASCADE` |
| `created_at` | `TIMESTAMP` | `NOT NULL`, default `CURRENT_TIMESTAMP` |
| `removed_at` | `TIMESTAMP` | nullable |

Chave e regras:

- `PRIMARY KEY (recruiter_id, job_id, candidate_id)` para idempotência;
- uma curtida é ativa quando `removed_at IS NULL`;
- o serviço só cria/reativa a curtida se existir convite `ACCEPTED` para o
  recrutador e a vaga;
- o histórico não é apagado fisicamente pela descurtida;
- índices para `(job_id, candidate_id)` e `(candidate_id, job_id)`.

### 4.2 Entidades existentes afetadas

- `users`: deve representar também recrutadores e manter e-mail globalmente
  único. Os campos relevantes já existentes são:

  | Campo | Tipo | Constraints atuais/relevantes |
  |---|---|---|
  | `id` | `UUID` | `PRIMARY KEY`, default `gen_random_uuid()` |
  | `name` | `VARCHAR(100)` | `NOT NULL` |
  | `email` | `VARCHAR(100)` | `NOT NULL`, `UNIQUE`; normalizado na borda |
  | `password` | `VARCHAR(100)` | mínimo de 6 caracteres; sem hash nesta etapa |
  | `description` | `TEXT` | `NOT NULL` |

  O recrutador será uma especialização por `recruiters.user_id`. A senha
  continua com a regra atual de mínimo de 6 caracteres nesta etapa.
- `companies`: continua sendo a empresa publicadora e administradora das
  próprias vagas.
- `jobs`: permanece com `publisher_id -> companies(user_id)`.
- `jobs_like`: continua representando a curtida do candidato na vaga.
- `matches`: continua com `candidate_id`, `company_id`, `job_id`,
  `created_at` e `UNIQUE(candidate_id, job_id)`. `company_id` será derivado
  de `jobs.publisher_id`; não deve ser substituído pelo recrutador.
- `companies_like`: a regra nova de curtida contextual deve ser implementada
  em `recruiter_job_likes`. O uso legado de `companies_like` precisa ser
  mantido ou descontinuado em etapa própria, sem misturar registros antigos
  com curtidas de recrutadores.

### 4.3 Projeções e contratos

Devem ser criados DTOs específicos, sem reutilizar respostas completas em
listagens pré-match:

- cadastro/atualização do recrutador, sem retornar senha;
- resumo anônimo de candidato para recrutador;
- resumo anônimo de vaga para recrutador;
- convite para recrutador, com estado e vaga sem dados indevidos;
- autorização/resultado de curtida;
- match autorizado, com dados liberados somente ao candidato, empresa ou
  recrutador participante.

## 5. Anonimato e LGPD

O Princípio do Anonimato de [AGENTS.md](/home/gildesio/Documentos/aczg/Linketinder/backend/AGENTS.md)
é obrigatório no serviço, query, DTO, endpoint e tela. A UI não pode ser a
única barreira.

### 5.1 Visibilidade por perfil

| Perfil e contexto | Antes do match | Depois do match |
|---|---|---|
| **Candidato vendo vaga/empresa** | Título, descrição, requisitos/competências e localização genérica da vaga. Não exibir nome, razão social, CNPJ, e-mail, telefone, LinkedIn ou endereço identificável da empresa. | Dados autorizados da empresa e da relação correspondente ao match, conforme contrato de apresentação. |
| **Empresa vendo candidato** | Competências, formação/experiência anônima e descrição genérica. Não exibir nome, e-mail, CPF, telefone, LinkedIn ou endereço do candidato. | Dados de contato/identificação liberados pela regra de negócio para os envolvidos no match. |
| **Recrutador vendo candidato em vaga aceita** | A mesma projeção anônima destinada à empresa: competências, formação/experiência anônima e descrição genérica. Não exibir nome, e-mail, CPF, telefone, LinkedIn ou endereço. | Dados autorizados da relação de match, somente quando o recrutador estiver vinculado à vaga e for um dos envolvidos autorizados. |
| **Candidato vendo recrutador** | Nenhum dado individual do recrutador deve ser exibido em listagens ou antes do match. | Somente `area` e `description` do recrutador, conforme decisão do Tutor. O e-mail não foi autorizado para liberação. |
| **Empresa administradora vendo recrutador** | Pode localizar pelo e-mail exclusivamente para enviar convite, sem transformar a busca em diretório público. Deve receber apenas os campos necessários ao convite e à gestão. | Pode consultar a área, descrição e estado do vínculo; dados pessoais adicionais dependem de autorização administrativa explícita. |
| **Recrutador vendo seus próprios dados** | Pode ver e editar o próprio perfil permitido. | Igual ao próprio perfil, sem expor a senha em respostas ou telas. |

Em todos os contextos:

- CPF, data de nascimento e senha do recrutador **nunca devem ser exibidos**,
  inclusive depois do match, para candidato, empresa, outro recrutador ou
  qualquer resposta de API.
- Senhas não devem aparecer em DTOs, logs, mensagens de erro, auditoria ou
  armazenamento de estado do frontend.
- O e-mail do recrutador não está entre os dados liberados ao candidato.
- Endpoints de match devem verificar a participação/autorização do solicitante
  antes de buscar ou serializar dados identificadores.
- Queries pré-match devem usar projeções anônimas; não devem carregar DTOs
  completos por conveniência.

## 6. Requisitos funcionais testáveis

- **RF-01:** cadastrar recrutador independente com dados válidos.
- **RF-02:** rejeitar CPF inválido/duplicado, e-mail inválido/duplicado, área
  inexistente, data incompatível com idade mínima ou senha menor que 6.
- **RF-03:** autenticar recrutador ativo sem expor a senha.
- **RF-04:** permitir que empresa convide recrutador existente para vaga própria.
- **RF-05:** impedir convite de recrutador inexistente, vaga de outra empresa ou
  convite pendente/aceito duplicado.
- **RF-06:** listar convites do recrutador autenticado e aceitar ou recusar
  somente os próprios convites.
- **RF-07:** cancelar convite pendente e revogar vínculo aceito somente pela
  empresa proprietária da vaga.
- **RF-08:** listar para o recrutador apenas vagas com vínculo `ACCEPTED`.
- **RF-09:** exibir candidatos em projeção anônima antes do match.
- **RF-10:** registrar curtida contextual com autoria e idempotência.
- **RF-11:** criar um match quando houver curtida recíproca na mesma vaga.
- **RF-12:** preservar um único match mesmo com vários recrutadores.
- **RF-13:** remover o match somente após a última curtida ativa do recrutador.
- **RF-14:** bloquear novas ações após revogação.
- **RF-15:** liberar somente os campos pós-match autorizados e manter CPF, data
  de nascimento e senha ocultos.

## 7. Critérios de aceite

Cada critério deve mapear para teste automatizado de serviço/repositório ou
verificação manual de endpoint/UI indicada entre parênteses.

### Cadastro e autenticação

- **CA-01 (teste de serviço):** **Dado** um visitante com dados válidos de
  recrutador, **Quando** concluir o cadastro, **Então** deve existir um
  `users` e um `recruiters` relacionados, com área válida e status `ACTIVE`,
  sem que a resposta contenha a senha.
- **CA-02 (teste de validação):** **Dado** CPF, e-mail, área, data ou senha
  inválidos, **Quando** enviar o cadastro, **Então** a operação deve falhar
  explicitamente antes da persistência.
- **CA-03 (teste de autenticação):** **Dado** recrutador inativo ou senha
  incorreta, **Quando** tentar autenticar, **Então** o sistema deve rejeitar a
  autenticação e não retornar dados sensíveis.

### Convites e vínculos

- **CA-04 (teste de serviço):** **Dado** empresa dona da vaga e recrutador
  cadastrado, **Quando** a empresa enviar convite, **Então** deve ser criado
  um convite `PENDING` para aquela vaga.
- **CA-05 (teste de autorização):** **Dado** empresa que não publica a vaga,
  **Quando** tentar convidar ou revogar, **Então** deve receber erro de
  autorização e nenhuma alteração deve ocorrer.
- **CA-06 (teste de idempotência):** **Dado** convite pendente ou aceito para
  o mesmo recrutador e vaga, **Quando** a empresa repetir o convite, **Então**
  não deve ser criado registro duplicado.
- **CA-07 (teste de serviço):** **Dado** convite `PENDING`, **Quando** o
  recrutador aceitar, **Então** o estado deve ser `ACCEPTED` e ele deve poder
  acessar somente aquela vaga.
- **CA-08 (teste de autorização):** **Dado** convite destinado a outro
  recrutador, **Quando** o usuário tentar aceitá-lo, **Então** a operação deve
  ser recusada.
- **CA-09 (teste de serviço):** **Dado** vínculo aceito, **Quando** a empresa
  revogá-lo, **Então** o estado deve ser `REVOKED` e novas consultas/curtidas
  do recrutador naquela vaga devem ser bloqueadas imediatamente.

### Curtidas e matches

- **CA-10 (teste de autorização):** **Dado** recrutador sem vínculo aceito na
  vaga, **Quando** tentar listar candidatos ou curtir, **Então** a operação
  deve ser recusada.
- **CA-11 (teste de integração):** **Dado** vínculo aceito e candidato
  visualizado anonimamente, **Quando** o recrutador curtir o candidato,
  **Então** deve ser registrada uma curtida com recrutador, vaga, candidato e
  timestamp, sem match se o candidato não tiver curtido a vaga.
- **CA-12 (teste de integração):** **Dado** candidato que curtiu a vaga e
  recrutador autorizado que curtiu o mesmo candidato naquela vaga, **Quando**
  processar a reciprocidade, **Então** deve existir um match único por
  candidato e vaga.
- **CA-13 (teste de persistência):** **Dado** dois recrutadores autorizados
  que curtiram o mesmo candidato na mesma vaga, **Quando** as duas ações forem
  processadas, **Então** deve existir um match e duas autorias de curtida, sem
  duplicar o match.
- **CA-14 (teste de serviço):** **Dado** duas curtidas ativas e um match,
  **Quando** um recrutador descurtir, **Então** o match deve permanecer; quando
  a última curtida ativa for removida, **Então** o match deve ser removido e o
  histórico deve permanecer.

### Anonimato

- **CA-15 (teste de DTO/query):** **Dado** recrutador ou empresa consultando
  candidatos antes do match, **Quando** a resposta for serializada, **Então**
  não deve conter nome, e-mail, CPF, telefone, LinkedIn ou endereço
  identificável.
- **CA-16 (teste de DTO/query):** **Dado** candidato consultando vagas antes
  do match, **Quando** a resposta for serializada, **Então** não deve conter
  nome/razão social, CNPJ ou contato da empresa.
- **CA-17 (teste de autorização):** **Dado** match não pertencente ao
  solicitante, **Quando** ele tentar consultar seus detalhes, **Então** a
  operação deve ser recusada.
- **CA-18 (teste de contrato):** **Dado** qualquer resposta, log ou tela do
  recrutador, **Então** CPF, data de nascimento e senha nunca devem aparecer;
  após match, apenas área e descrição podem ser liberadas ao candidato.

## 8. Impacto no que já existe

### Backend e banco

- Adicionar domínio, DTOs, repositórios JDBC, serviços e controladores para
  recrutadores, convites e curtidas contextuais.
- Adicionar migração versionada e seed do catálogo de áreas.
- Ajustar autenticação para reconhecer recrutador sem quebrar login de
  candidato/empresa.
- Adicionar autorização de empresa proprietária e recrutador vinculado.
- Rever `CompanyLikeService` e `JobLikeService`: o fluxo novo deve usar
  curtida contextual de recrutador, sem atribuir autoria automaticamente à
  empresa.
- Preservar a unicidade e os relacionamentos existentes de `matches`.
- Integrar os controladores necessários no [MainWebMenu.groovy](/home/gildesio/Documentos/aczg/Linketinder/backend/src/main/groovy/zg/acelera/app/MainWebMenu.groovy).
- Corrigir ou substituir projeções que atualmente retornam
  `CompanyResponseDTO` completo em contexto pré-match.

### Frontend

- Adicionar cadastro e login de recrutador.
- Adicionar painel de convites com aceitar/recusar.
- Adicionar gestão de convites/vínculos e atribuição de vagas para empresa.
- Adicionar visão de vagas atribuídas e candidatos anonimizados para
  recrutador.
- Substituir o fluxo de curtida/match em `localStorage` por chamadas aos
  contratos backend quando a integração for implementada.
- Manter telas atuais de candidato e empresa funcionando durante a migração.

### Regressões a verificar

- cadastro, login, atualização e exclusão de candidatos e empresas;
- listagem e atualização de vagas;
- filtros e recomendações de vagas;
- curtidas de candidatos em vagas;
- criação, consulta e remoção de matches;
- ocultação de dados antes do match;
- encerramento e tratamento de erros do servidor web.

## 9. Fora do escopo

- Envio real de e-mail, SMS ou notificações push para convites.
- Convite para a empresa inteira; o convite é sempre por vaga.
- Diretório público de recrutadores.
- CRUD do catálogo de áreas após o seed inicial.
- Um recrutador criar, editar, publicar ou excluir vagas.
- Match separado por recrutador.
- Liberação de CPF, data de nascimento, senha ou e-mail pessoal do recrutador
  ao candidato.
- Alteração do modelo de candidato ou empresa além do necessário para
  identidade global, autenticação e autorização.
- Hash de senha nesta etapa, embora seja requisito obrigatório para uma versão
  de produção.
- Notificações, workflow de aprovação adicional ou múltiplos níveis de
  administrador.
- Migração destrutiva ou remoção imediata do legado de `companies_like`.
- Implementação simultânea obrigatória de backend e frontend.

## 10. Decisões tomadas e validadas com o Tutor

### Vínculo

- O recrutador se cadastra independentemente na Linketinder.
- A empresa encontra o recrutador pelo e-mail já cadastrado.
- A empresa envia convite para uma vaga específica.
- O convite é aceito ou recusado no painel do recrutador.
- O recrutador pode aceitar convites de várias empresas.
- Somente a empresa proprietária da vaga administra o convite.
- A empresa pode cancelar convite pendente e revogar vínculo aceito.
- O recrutador atua somente em vagas com vínculo aceito.

### Curtidas e match

- A curtida do recrutador é contextual à vaga.
- A autoria individual da curtida é armazenada.
- O candidato precisa ter curtido a mesma vaga.
- Existe um único match por candidato e vaga.
- Curtidas de vários recrutadores não duplicam o match.
- O match permanece enquanto houver uma curtida ativa.
- A remoção da última curtida ativa desfaz o match.

### Escopo de atuação

- O recrutador pode analisar candidatos somente nas vagas aceitas.
- Apenas o administrador da empresa envia convites, cancela convites e revoga
  vínculos.
- O recrutador não recebe autorização automática para todas as vagas da
  empresa.

### Anonimato

- Antes do match, candidatos, empresas e recrutadores usam projeções anônimas.
- Nome, e-mail, CPF, telefone, LinkedIn, endereço identificável, razão social
  e CNPJ não devem vazar em contexto pré-match.
- Depois do match, somente dados expressamente autorizados são liberados aos
  envolvidos.
- Para o candidato, a decisão validada libera área e descrição do recrutador.
- CPF, data de nascimento e senha do recrutador nunca são exibidos.

### Validações

- Cadastro independente de recrutador.
- CPF normalizado com 11 dígitos e unicidade global.
- E-mail normalizado, validado e globalmente único.
- Data de nascimento válida e idade mínima de 18 anos.
- Área obrigatória pertencente ao catálogo seedado.
- Senha com mínimo de 6 caracteres, sem hash nesta etapa.
- Vínculos e ações revogados devem ser bloqueados imediatamente no backend.

## 11. Suposições e dúvidas bloqueadoras

As decisões necessárias para esta especificação foram validadas pelo Tutor.
Não há dúvida de negócio bloqueadora para iniciar o plano técnico. Permanecem
como decisões de implementação a serem detalhadas no `plan.md`:

- formato exato dos endpoints e representação dos estados;
- estratégia de compatibilidade com `companies_like`;
- mecanismo PostgreSQL para garantir a consistência entre `company_id` e
  `jobs.publisher_id`;
- mecanismo de sessão/autenticação necessário para aplicar autorização no
  backend;
- formato final das projeções anônimas e dos dados liberados após o match.
