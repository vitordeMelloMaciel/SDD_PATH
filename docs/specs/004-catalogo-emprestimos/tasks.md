# Tarefas da Spec 004

Execute uma tarefa por vez. Cada tarefa vira um commit próprio e termina com:

- diff revisado e `git status --short` sem surpresa;
- os testes, os builds e o `docker compose build backend frontend`, quando
  aplicável, a partir da raiz de um worktree limpo (ver o plano);
- testes com o CA-xx no nome ou num comentário (`rules/tests.md`);
- os CA-xx atendidos citados no commit;
- `ENTREGA.md` atualizado só com as linhas da tarefa, quando algo mudar.

Nenhuma tarefa cria migration. Se uma delas precisar mudar o schema, ela
para e segue `rules/migration.md`: mostra o DDL e espera a aprovação do
operador.

Em cada tarefa, o bloco “Código local” compara a implementação não
rastreada com o que a tarefa pede. Ele não aprova esse código. O acesso ao
banco segue o ADR-001 (JPA/Hibernate), e o `JdbcTemplate` local não é
reaproveitado.

## T001 · CA-07 e CA-01 (API). API de equipamentos

- [x] Entidade `Equipment` e `EquipmentRepository` sobre a tabela existente.
- [x] `GET /api/equipment` devolve todo o acervo, ordenado por patrimônio, com exatamente `id`, `assetCode`, `name`, `category`, `status` e `imageUrl`, sem ler `loan`.
- [x] `imageUrl` vem de `EquipmentImageUrls.urlFor(imageObjectKey)`.
- [x] Teste de privacidade (CA-07): com A titular de um empréstimo aberto e B sem empréstimos, o objeto do item emprestado é idêntico para os dois, tem exatamente os seis campos e nenhum valor contém e-mail, nome, ID de usuário, ID de empréstimo, `loaned_at` ou `due_at` de A.
- [x] Teste de `imageUrl` com e sem `EQUIPMENT_IMAGE_BASE_URL`.

Código local:
- **Cobre:** rota, ordenação e status (`LoanController.catalog`, `LoanService.catalog`).
- **Diverge:**
  - **CA-07:** o objeto inclui `myDueAt`, e a consulta faz `left join loan` pela conta da sessão. O titular recebe a própria data.
  - `imageUrl` é sempre `null`.
  - O código usa SQL com `JdbcTemplate` no service, contra o ADR-001.
  - O teste local exige `myDueAt`.

Commit sugerido: `feat(catalog): list equipment with six public fields [CA-07,CA-01]`

## T002 · Retirada

Por decisão do operador em 2026-11-11, a T002 foi retirada: a spec não
ampliou o “Contrato afetado” para `docker-compose.yml` e `README.md`. O
consumo de `EQUIPMENT_IMAGE_BASE_URL` está na T001. A configuração em
produção é feita pelo operador, com autorização no momento da ação.

## T003 · CA-02 e CA-03. API de empréstimos, com 409 e motivo

- [ ] Entidade `Loan`, `LoanRepository` e busca com `@Lock(PESSIMISTIC_WRITE)` em `AppUserRepository` e `EquipmentRepository`.
- [ ] `POST /api/loans` (`equipmentId` UUID):
  - bloqueia a conta e depois o equipamento na mesma transação;
  - classifica a recusa na ordem da V2: 409 `EQUIPMENT_MAINTENANCE`, `EQUIPMENT_BORROWED`, `LOAN_LIMIT` ou `OVERDUE_LOAN`, com a mensagem da spec;
  - salva o `Loan` sem `dueAt` e lê `loanedAt` e `dueAt` com `refresh`;
  - devolve 201 com a representação de Meus empréstimos.
- [ ] 400 `INVALID_REQUEST` para JSON inválido, UUID ausente ou inválido, ou `Content-Type` diferente de JSON. 404 `EQUIPMENT_NOT_FOUND` para UUID inexistente.
- [ ] `GET /api/loans/me`: empréstimos abertos do principal, ordenados por vencimento e ID, com `dueAt` em ISO 8601 com offset e `overdue`.
- [ ] Testes com PostgreSQL real:
  - cada recusa 409, com código e mensagem, sem inserir empréstimo;
  - os casos 400 (incluindo o `Content-Type` errado) e 404;
  - 201 com `due_at = loaned_at + interval '14 days'` e status `emprestado`, posto pela trigger.

Código local:
- **Cobre:**
  - a ordem de bloqueio e a classificação dos quatro 409 (`LoanService.request`), que são reaproveitadas como lógica;
  - 400 para JSON ou UUID inválido e 404;
  - inserção pela trigger e `GET /api/loans/me`.
  - O teste local já prova parte do CA-02: `due_at`, status `emprestado` e `/me` com um item.
- **Diverge:**
  - SQL com `JdbcTemplate` no service, contra o ADR-001.
  - `Content-Type` errado devolve 415, e não 400.
  - Não há teste por recusa 409 nem para 400 e 404.
  - O teste local fica em `AuthenticationFoundationIntegrationTest` e vai para `LoanApiIntegrationTest`.

Commit sugerido: `feat(loans): request loans with locked checks and list my loans [CA-02,CA-03]`

## T004 · CA-04. Concorrência

- [ ] Teste: duas solicitações concorrentes ao mesmo item produzem um 201, um 409 `EQUIPMENT_BORROWED` e um empréstimo aberto.
- [ ] Teste: com dois empréstimos abertos na mesma conta, duas solicitações concorrentes a itens distintos produzem um 201, um 409 `LOAN_LIMIT` e três abertos ao final.
- [ ] Só se a proposta 1 do plano for aprovada: tratar a rejeição inesperada do banco.

Código local:
- **Cobre:** o teste do mesmo item (no teste local de autenticação).
- **Diverge:**
  - Falta o teste de concorrência do `LOAN_LIMIT`.
  - Uma violação inesperada vira 500 sem envelope.

Commit sugerido: `test(loans): prove concurrent requests keep one loan and the limit [CA-04]`

## T005 · CA-05 (API). Conta ativa, papel e CSRF

- [ ] `ActiveAccountFilter` depois do `CsrfFilter` e antes da autorização: conta inexistente ou inativa recebe 401 `UNAUTHENTICATED`, com a sessão invalidada e o cookie expirado, nas três rotas e em `GET /api/auth/session`.
- [ ] Restringir as três rotas ao papel `colaborador`: `operacoes` ativo recebe 403 `FORBIDDEN` (“Acesso negado.”).
- [ ] Testes:
  - 401 sem sessão nas três rotas e 403 para `operacoes` ativo;
  - no POST com CSRF válido, 401 sem sessão e 403 para `operacoes`; com token ausente ou inválido, 403 `CSRF_INVALID`, com e sem sessão, inclusive de conta desativada;
  - conta **desativada** e conta **excluída**, com papel **`colaborador`** e com papel **`operacoes`**: 401 nas três rotas e em `GET /api/auth/session`, nenhum empréstimo criado, e o cookie antigo continua com 401;
  - Meus empréstimos exclui os encerrados e os de outras contas;
  - a suíte da Spec 001 continua passando.

Código local:
- **Cobre:**
  - `hasRole("COLABORADOR")` nas três rotas (alteração local em `SecurityConfiguration.java`);
  - o encerramento da sessão de conta inativa, só dentro do controller (`LoanController.rejection`).
  - O teste local prova `/me` vazio para outra conta.
- **Diverge:**
  - A checagem de conta ativa roda depois da autorização, então `operacoes` desativada recebe 403, e não 401.
  - `GET /api/auth/session` não detecta conta desativada ou excluída.
  - O 401 do controller não expira o cookie.
  - Não há testes de 401, 403 e CSRF nem do cenário de conta desativada ou excluída.

Commit sugerido: `feat(loans): reject inactive accounts and restrict routes to collaborators [CA-05]`

## T006 · CA-05 (páginas) e CA-01 (destino). Proteção das páginas

- [ ] `/catalogo` e `/meus-emprestimos` consultam `GET /api/auth/session` no servidor antes de renderizar. Sem sessão, ou com 401, inclusive de conta desativada ou excluída (T005), redirecionam para `/`.
- [ ] Para `operacoes`, mostrar só “Acesso negado” e “Esta área é exclusiva de colaboradores.”, sem lista e sem chamar as três rotas.
- [ ] Para o colaborador, neste commit cada página mostra só o quadro (cabeçalho, navegação e sair) e o título. O conteúdo entra na T007 e na T008. Nenhum trecho do `Catalog` local entra neste commit.
- [ ] A página de login leva colaborador e `operacoes` a `/catalogo` após o login.
- [ ] Conferência no navegador: sem sessão, colaborador, `operacoes`, e conta desativada com sessão aberta.

Código local:
- **Cobre:** redirecionamento sem cookie ou com 401 (`loan-api.ts`) e destino `/catalogo` do colaborador (alteração local em `login-page.tsx`).
- **Diverge:**
  - As páginas chamam a API sem consultar a sessão.
  - `operacoes` vê o “Acesso negado.” do 403 da API, sem o texto “Esta área é exclusiva de colaboradores.”.
  - `operacoes` não é levado a `/catalogo`.

Commit sugerido: `feat(catalog): gate journey pages by session and role [CA-05,CA-01]`

## T007 · CA-01, CA-03 e CA-07 (interface). Catálogo com fotos

- [ ] Variante com foto do `EquipmentCard` (ADR-002) no lugar do ícone: 4:3, `--gradient-equipment`, `object-fit: cover`, e “Foto indisponível” sem URL ou em falha de carregamento.
- [ ] Rodapés: “Solicitar →” nos disponíveis, “Em manutenção”, e “Emprestado” para todas as contas, inclusive a do titular, sem nome nem data.
- [ ] Solicitar com CSRF, mostrar a `message` de qualquer 409 e recarregar o estado da API, sem estado otimista com data.
- [ ] Crédito único “Fotos: Pixabay”, com link para a Pixabay.
- [ ] Conferência no navegador: contas A e B (CA-07), os três itens em manutenção, `EQUIPMENT_BORROWED` e `LOAN_LIMIT` provocados (proposta 2 do plano).

Código local:
- **Cobre:** grade de cards, foto 4:3 com `object-fit: cover`, “Foto indisponível”, crédito Pixabay, CSRF no POST, motivo das recusas 409 e recarga.
- **Diverge:**
  - **CA-07:** “Emprestado a você · Devolução …” e a data só para o titular, a partir de `myDueAt` e de um estado local após o 201.
  - Não há rodapés “Em manutenção” nem “Emprestado”.
  - O fundo é `bg-sunken`, e não `--gradient-equipment`.
  - Há um botão “Solicitar”, e não o rodapé “Solicitar →”.
  - Não usa o `EquipmentCard`, que não tem ícone para `cabo`.
  - O tipo `Equipment` em `loan-api.ts` declara `myDueAt`.

Commit sugerido: `feat(catalog): render equipment cards with photo variant [CA-01,CA-03,CA-07]`

## T008 · CA-02 e CA-05 (interface). Meus empréstimos

- [ ] Listar os empréstimos abertos com patrimônio, nome e devolução em `America/Sao_Paulo`, com destaque para atraso e mensagem para a lista vazia.
- [ ] Conferência no navegador: depois de uma solicitação, o item aparece com a data esperada, e outra conta não o vê.

Código local:
- **Cobre:** lista, fuso, destaque de atraso e mensagem para a lista vazia (`MyLoans`).
- **Diverge:** depende do portão de páginas da T006.

Commit sugerido: `feat(loans): show my open loans page [CA-02,CA-05]`

## T009 · CA-01 a CA-07. Verificação final

- [ ] Suíte completa do backend, typecheck e build do front e `docker compose build backend frontend`, a partir da raiz de um worktree limpo do último commit.
- [ ] Matriz no navegador local, sem a conta do professor.
- [ ] CA-06 em produção: roteiro do operador com a conta de teste e consulta de leitura com zero empréstimos para o professor. Deploy e variáveis remotas só com autorização no momento da ação.
- [ ] `spec-validator` em modo verificação, com a tabela em `verification/`.
- [ ] `ENTREGA.md` e `handoff.md` com só as linhas da tarefa.

Código local: não se aplica.

Commit sugerido: `docs(catalog): record Spec 004 verification [CA-01..CA-07]`

## Divergências do código local em relação à spec e aos ADRs

| Onde | Divergência | Critério | Tarefa |
| --- | --- | --- | --- |
| `LoanService.catalog`, `EquipmentView` | `myDueAt` e `left join loan` pela conta da sessão | CA-07 | T001 |
| `loan-journey.tsx` (`Catalog`) | “Emprestado a você · Devolução …” e a data só para o titular, mais estado local com data | CA-07, ADR-002 | T007 |
| `frontend/lib/loan-api.ts` | Tipo `Equipment` com `myDueAt` | CA-07 | T007 |
| `LoanService` | SQL com `JdbcTemplate` no service | ADR-001 | T001, T003 |
| `LoanService.catalog` | `imageUrl` sempre `null` | CA-01 | T001 |
| `LoanController` | `Content-Type` errado devolve 415, e não 400 | CA-03 | T003 |
| `LoanService.request` | Rejeição inesperada do banco vira 500 sem envelope | Proposta 1 do plano, pendente | T004 |
| `SecurityConfiguration` (local), `LoanService.requireActive` | Conta ativa checada depois do papel: `operacoes` desativada recebe 403 | CA-05 | T005 |
| `AuthController.session` | Não detecta conta desativada ou excluída | CA-05 | T005 |
| `LoanController.rejection` | O 401 não expira o cookie | CA-05 | T005 |
| `frontend/app/*/page.tsx`, `loan-api.ts` | Páginas chamam a API sem consultar a sessão; `operacoes` recebe o 403 da API | CA-05 | T006 |
| `login-page.tsx` (local) | Só o colaborador vai a `/catalogo` | CA-05 | T006 |
| `loan-journey.tsx` | Sem rodapés “Em manutenção” e “Emprestado”, fundo `bg-sunken`, botão “Solicitar” | CA-01, ADR-002 | T007 |
| `AuthenticationFoundationIntegrationTest` (local) | Exige `myDueAt` e mistura a Spec 004 com os testes de login | CA-07 | T001, T003 |
| `README.md` (local) | Instruções de deploy no Render e texto sobre o catálogo local | ADR-003 | Nunca entra; o plano não altera o README |
| `ENTREGA.md` (local) | Texto desatualizado sobre a V3, a V4 e a carga | — | Nunca entra inteiro |
| `frontend/next.config.ts` | Nenhuma: o trecho já está na `main` (`d16a3e6`) | — | — |
