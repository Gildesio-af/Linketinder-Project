---
name: feature-com-spec
description: "Use when implementing a new feature through a complete spec → plan → implementation workflow, with repository context, acceptance criteria, automated tests, and manual regression validation."
argument-hint: "Describe the feature to specify and implement"
user-invocable: true
---

# Feature com especificação

## Objetivo

Conduza uma feature do requisito até a implementação usando o fluxo:

```text
contexto do repositório → spec → plano → tarefas → implementação → validação
```

Use os artefatos existentes em `docs/` como fonte de contexto e decisão. As
especificações e seus artefatos devem ficar em
`docs/specs/<nome-da-feature>/`. Não avance para a implementação enquanto a
especificação, o plano de testes e as tarefas executáveis não estiverem
definidos.

Em cada tarefa ou etapa executada, atualize obrigatoriamente o
`CHANGELOG.md` na raiz do projeto, na seção `[Não lançado]`, descrevendo a
alteração realizada. A atualização do changelog faz parte da própria tarefa e
deve ser verificada antes de avançar para a etapa seguinte.

## 1. Preparar o contexto do repositório

1. Identifique a raiz do projeto e explore a árvore, os arquivos de
   configuração, os módulos principais, os testes e a documentação em `docs/`.
2. Leia o `AGENTS.md` existente na raiz ou na pasta indicada pelas instruções
   do projeto. Se ele não existir, crie um `AGENTS.md` na raiz do projeto
   depois de explorar o repositório.
3. O `AGENTS.md` deve documentar:
   - **Visão geral:** o que a aplicação faz e quais funcionalidades já
     existem.
   - **Stack e estrutura:** linguagens, ferramentas, dependências relevantes e
     organização das pastas.
   - **Como executar e testar:** comandos para iniciar a aplicação, executar
     testes, lint, build e validações relevantes.
   - **Convenções:** nomes, organização de classes/módulos, fronteiras de
     camadas, estilo e padrões de testes.
   - **Restrições:** alterações que não devem ser feitas sem autorização,
     incluindo mudanças fora do escopo, dependências, persistência, contratos,
     UX e arquitetura.
4. Se já existir um `AGENTS.md`, atualize somente informações verificadas e
   preserve orientações válidas. Não substitua instruções específicas por
   generalidades.
5. Registre as funcionalidades existentes que podem sofrer regressão. Para
   uma aplicação de tarefas, inclua explicitamente listagem, filtros, mudança
   de status e alarmes quando essas superfícies existirem.

## 2. Especificação (`spec`)

1. Leia os documentos atuais em `docs/` antes de escrever. Procure decisões,
   modelo de dados, contratos, quickstart, tarefas anteriores e critérios de
   aceite.
2. Crie ou atualize `docs/specs/<nome-da-feature>/spec.md` com:
   - contexto, objetivo e escopo;
   - atores e cenários de usuário;
   - regras de negócio e casos de borda;
   - requisitos funcionais testáveis;
   - critérios de aceite em formato **Dado / Quando / Então**;
   - impacto nas funcionalidades existentes;
   - itens explicitamente fora do escopo;
   - suposições e dúvidas que realmente bloqueiam a implementação.
3. Cada critério de aceite deve ser observável e mapear para pelo menos um
   teste automatizado ou uma verificação manual claramente identificada.
4. Revise a especificação pelo **Princípio do Anonimato** definido no
   `AGENTS.md`. Confirme explicitamente que nenhum DTO, endpoint, query ou
   tela da feature expõe dados sensíveis antes do match, incluindo nome,
   e-mail, CPF, telefone, LinkedIn, endereço, razão social ou CNPJ, conforme
   o contexto. Registre os DTOs/projeções anônimas e as verificações de
   autorização necessárias.
5. Atualize o `CHANGELOG.md` na raiz, na seção `[Não lançado]`, registrando a
   criação ou alteração da spec.
6. Não invente comportamento incompatível com os contratos existentes. Quando
   houver mais de uma interpretação com impacto significativo em escopo,
   persistência, UX ou segurança, peça esclarecimento antes de decidir.

## 3. Plano técnico (`plan`)

1. Crie ou atualize `docs/specs/<nome-da-feature>/plan.md` somente depois de a
   spec estar coerente.
2. Descreva stack, integrações, persistência, estrutura de arquivos,
   fronteiras de pacote/módulo e estratégia de testes.
3. Faça uma verificação de constituição/conformidade:
   - a solução reutiliza a arquitetura existente;
   - entradas são validadas nas bordas;
   - erros preservam causas e são apresentados pelo fluxo padrão;
   - não há dependências novas ou mudanças de formato sem autorização;
   - o plano é compatível com o ambiente e os comandos documentados.
4. Verifique no plano o **Princípio do Anonimato** do `AGENTS.md`: DTOs,
   endpoints, queries e telas devem impedir o vazamento de dados sensíveis
   antes do match, com projeções anônimas e autorização no serviço/controlador
   quando aplicável.
5. Registre decisões e alternativas descartadas quando houver risco ou
   trade-off relevante.
6. Inclua no plano os testes para cenários válidos, inválidos, limites,
   persistência, erros e integração com as superfícies afetadas.
7. Crie ou atualize `docs/specs/<nome-da-feature>/quickstart.md` com comandos
   reproduzíveis e um
   roteiro de validação ponta a ponta.
8. Atualize o `CHANGELOG.md` na raiz, na seção `[Não lançado]`, registrando o
   plano e o quickstart.

## 4. Tarefas executáveis (`tasks`)

1. Crie ou atualize `docs/specs/<nome-da-feature>/tasks.md` a partir da spec e
   do plano.
2. Ordene as tarefas por dependência: setup, fundação, testes primeiro,
   implementação, integração, documentação e validação.
3. Cada tarefa deve ter:
   - identificador único;
   - checkbox;
   - caminho de arquivo ou diretório;
   - resultado verificável;
   - dependências explícitas quando existirem.
4. Separe tarefas por história/capacidade para que cada incremento tenha um
   critério de teste independente.
5. Inclua tarefas para atualizar documentação relacionada e para revisar cada
   critério de aceite, sem marcar como concluída uma tarefa não verificada.
6. Inclua em cada tarefa a atualização obrigatória do `CHANGELOG.md` na raiz,
   na seção `[Não lançado]`, e só marque a tarefa como concluída depois de
   registrar essa atualização.

## 5. Implementação orientada a testes

Execute as tarefas na ordem definida, seguindo TDD:

1. escreva ou atualize o teste que expressa o comportamento esperado;
2. execute o teste e confirme que ele falha pelo motivo correto;
3. implemente a menor solução suficiente;
4. refatore mantendo os testes verdes;
5. avance apenas quando a tarefa e seus critérios estiverem verificáveis.
6. Em cada tarefa, atualize o `CHANGELOG.md` na raiz, na seção `[Não lançado]`,
   antes de avançar para a próxima tarefa.

Garanta que existam testes automatizados para a feature:

- em Java, use Spock/Groovy e a organização de testes do projeto;
- em JavaScript/TypeScript, use o framework já adotado pelo projeto;
- cubra sucesso, validação, limites, caminhos de erro, persistência e
  integração nas bordas alteradas;
- teste explicitamente o **Princípio do Anonimato** do `AGENTS.md`: confirme
  que nenhum DTO, endpoint, query ou tela vaza dados sensíveis antes do match
  e que somente os envolvidos no match recebem os dados autorizados;
- não adicione framework ou dependência de teste sem autorização.

Preserve as fronteiras existentes (`app`, `controller`, `service`,
`repository`, `domain`, `dto`, `util`, ou equivalentes do projeto) e não altere
funcionalidades fora do escopo. Exponha erros explicitamente; não use capturas
amplas, falhas silenciosas ou retornos de sucesso falsos.

## 6. Validação final

### Testes automatizados

1. Execute primeiro os testes focados da feature.
2. Execute a suíte completa de testes.
3. Execute o build/release check documentado pelo projeto. Para o back-end
   Java deste repositório, isso inclui:

   ```bash
   ./gradlew test
   ./gradlew build
   ```

4. Registre falhas, comandos e resultado; corrija regressões antes de concluir.
5. Confirme que o `CHANGELOG.md` na raiz foi atualizado na seção
   `[Não lançado]` para cada tarefa/etapa executada.

### Critérios de aceite

Confira **um a um** todos os critérios de aceite da spec. Mantenha uma tabela
ou checklist de rastreabilidade com:

| Critério | Evidência | Teste/verificação | Resultado |
|---|---|---|---|
| CA-01 | comportamento esperado | nome do teste ou passo manual | Passou/Falhou |

Um critério só pode ser marcado como atendido quando houver evidência
reproduzível. Se não puder ser automatizado, documente a razão e a verificação
manual executada.

### Regressão manual

Teste manualmente as funcionalidades existentes relacionadas à mudança. Em uma
aplicação de tarefas, execute pelo menos:

1. listagem de tarefas;
2. filtros existentes, incluindo categoria e status quando disponíveis;
3. mudança de status;
4. alarmes, incluindo criação/listagem/remoção e o comportamento de disparo,
   quando alarmes existirem ou forem afetados pela feature.

Use os comandos e o cenário documentados em
`docs/specs/<nome-da-feature>/quickstart.md`. Verifique
também que a persistência existente, mensagens de erro e encerramento da
aplicação continuam funcionando. Não altere arquivos de dados reais sem
preservar/restaurar o estado ou usar um ambiente temporário.

### Princípio do Anonimato

Na revisão final, confirme explicitamente que o **Princípio do Anonimato**
definido no `AGENTS.md` foi respeitado em toda a implementação e nos testes:
nenhum DTO, endpoint, query ou tela expõe dados sensíveis antes do match, e a
autorização é aplicada no serviço/controlador, não apenas na UI.

## 7. Encerramento

Antes de reportar conclusão:

- confirme que spec, plano, tarefas e documentação estão atualizados em
  `docs/specs/<nome-da-feature>/`;
- confirme que cada tarefa/etapa atualizou o `CHANGELOG.md` na raiz, na seção
  `[Não lançado]`;
- confirme novamente o **Princípio do Anonimato** em DTOs, endpoints, queries e
  telas, antes e depois do match;
- confirme que todos os testes automatizados relevantes passaram;
- confirme que `build` passou quando houver essa verificação no projeto;
- confirme cada critério de aceite e as regressões manuais;
- liste arquivos alterados, comandos executados e qualquer limitação;
- não faça commit, upgrade de dependência, migração destrutiva ou mudança
  fora do escopo sem solicitação explícita.
