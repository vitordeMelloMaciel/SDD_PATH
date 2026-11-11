# Rodada pre-aprovacao plan-01 (plan.md e tasks.md)

- Base: `c52ae79` com alteracoes locais em `plan.md` e `tasks.md` da Spec 004
- Executor: instrucoes do `spec-validator` executadas por subagente Claude Code em contexto limpo (Codex CLI indisponivel nesta maquina)
- Sandbox: somente leitura, por instrucao
- Data: 2026-11-11

## Saida do verificador

# Resultado: BLOQUEADA

**Rodada:** pré-aprovação plan-01 (Spec 004, plan.md e tasks.md)
**Estado avaliado:** HEAD `c52ae79` mais as alterações locais não commitadas em `docs/specs/004-catalogo-emprestimos/plan.md` e `tasks.md`. O código local não rastreado da Spec 004 e os diffs locais de `SecurityConfiguration.java`, `login-page.tsx`, `AuthenticationFoundationIntegrationTest.java`, `README.md` e `ENTREGA.md` foram lidos só para conferir o mapeamento.
**Severidade geral:** alta (2 bloqueios)
**Aprovação humana:** ainda necessária

## Bloqueios

| ID | Severidade | Arquivo:linha | Evidência |
| --- | --- | --- | --- |
| B-01 | alta | `docs/specs/004-catalogo-emprestimos/plan.md:15-18`; `docs/adr/001-stack.md:22`, `:39`, `:56` | O plano põe sob o título "ADR-001" o "acesso ao banco pelo padrão já usado (`JdbcTemplate`)". O ADR-001 diz outra coisa: o acesso ao banco é "Spring Data JPA/Hibernate" (linha 22), e "SQL direto em controllers ou services" aparece como alternativa descartada (linha 56). As tarefas T001, T003 e T004 partem de `LoanService`, que escreve SQL direto em um service (`LoanService.java:24-29`, `:34-44`, `:49-80`). Ou seja, o plano cita o ADR de forma errada e não sinaliza o desvio. `rules/restrictions.md:13` manda parar e avisar quando for preciso contrariar um ADR. Há duas saídas, e as duas cabem ao operador: (a) o plano passa a seguir o ADR-001, com JPA, bloqueio pessimista em repositório e query nativa encapsulada; ou (b) o desvio é apresentado como pergunta ao operador e, se for aceito, vira um ADR. O `JdbcTemplate` já existe no código commitado (`UserProvisioning.java:9-19`, `HealthController.java:8-18`), mas isso não altera o ADR. |
| B-02 | alta | `tasks.md:101`, `:90`; `spec.md:77-79`, `:97-99`, `:137`; `AuthController.java:98-101`; `SecurityConfiguration.java:90` (local) | A spec exige duas coisas para conta desativada ou excluída: (1) as três rotas retornam 401, seja qual for o papel; (2) as páginas redirecionam para `/` (CA-05). Na T006, a página consulta `GET /api/auth/session` e redireciona "com 401, inclusive de conta desativada ou excluída". Hoje esse endpoint devolve o principal guardado na sessão sem consultar o banco (`AuthController.java:98-101`), então responde 200 para conta desativada ou excluída. Nenhuma tarefa muda esse endpoint, e mudá-lo mexe no contrato da Spec 001 sem que o plano diga isso. Consequência: uma conta `operacoes` desativada veria "Acesso negado" em vez de ser redirecionada, e na API receberia 403 em vez de 401, porque `hasRole("COLABORADOR")` (`SecurityConfiguration.java:90`) é avaliado antes de `requireActive` (`LoanService.java:83-88`). A T005 (`tasks.md:90`) não testa conta desativada com papel `operacoes`. O plano precisa definir de onde vem a verificação de conta ativa para as páginas e para as rotas antes da checagem de papel, e cobrir os dois papéis nos testes. |

## Cobertura dos critérios

| CA | Tarefas | Meio de prova no plano | Coerência com "Contrato afetado" (spec:126-127) |
| --- | --- | --- | --- |
| CA-01 | T001 (API), T002, T006 (destino), T007, T009 | Teste HTTP de `imageUrl` e conferência no navegador | Coerente (navegador para CA-01). Depende das fotos da Spec 003 (T003/T004), como diz plan.md:47-48. |
| CA-02 | T003, T007, T008 | Teste com PostgreSQL de `due_at = loaned_at + 14 days`, mais navegador para catálogo e Meus empréstimos | O contrato prevê só HTTP para CA-02, mas a parte "aparece em Meus empréstimos no fuso" pede o navegador (T008). Divergência leve, não bloqueia. |
| CA-03 | T003, T007 | Testes HTTP dos quatro 409, de 400 e de 404, mais navegador para o motivo | O teste HTTP é coerente. Nenhuma tarefa descreve como provocar e ver na interface cada um dos quatro motivos (ver perguntas). |
| CA-04 | T004 | Dois testes concorrentes com Testcontainers | Coerente. |
| CA-05 | T005 (API), T006 (páginas), T008 | Testes HTTP mais navegador | API coerente. O caso de conta desativada ou excluída nas páginas e para `operacoes` não fica coberto (B-02). |
| CA-06 | T002, T009 | Roteiro do operador em produção mais consulta de leitura | Coerente. |
| CA-07 | T001, T007 | Teste HTTP com as contas A e B mais navegador | Coerente. |

Todas as tarefas citam pelo menos um CA-xx e cabem, em princípio, em um commit cada.

## Mapeamento do código local

**Confirmações (o "Cobre" e o "Diverge" batem com o código):**
- T001: rota e ordenação estão em `LoanController.java:29-32` e `LoanService.java:34-38` (`order by e.asset_code`). O `myDueAt` com `left join loan` pela sessão está em `LoanService.java:35-37`, `:42`, `:107-108`. `imageUrl` sai sempre `null` em `LoanService.java:41`. O teste local exige `myDueAt` (diff de `AuthenticationFoundationIntegrationTest`, asserções `get("myDueAt")).isNotNull()` e `containsEntry("myDueAt", null)`).
- T002: `docker-compose.yml:25-30` e `:42-43` não repassam `EQUIPMENT_IMAGE_BASE_URL`. O diff local do `README.md` acrescenta instruções do Render, contra `docs/adr/003-hospedagem-backend.md:28` e `:46`. `next.config.ts:3`, `:10` já estão na main.
- T003: bloqueia a conta (`LoanService.java:48`, `:84` `for update`) e depois o equipamento (`:49-50`). Os 409 seguem a ordem da V2 (`:56-72`; `V2__modelo_inicial.sql:73-111`). O 400 está em `LoanController.java:41-43` e `:47-52`, o 404 em `LoanService.java:53-54`, a inserção sem `due_at` em `:74-76` e o `listOpen` em `:22-29`. Não há teste por motivo de 409 nem de 400/404, como o mapeamento diz.
- T004: o teste concorrente do mesmo item existe no diff local. Não há tratamento de `DataAccessException`, então a resposta é 500 sem envelope.
- T005: `SecurityConfiguration.java:90` (local). O controller invalida a sessão em `LoanController.java:54-62`. `FORBIDDEN` "Acesso negado." já existe em `JsonAccessDeniedHandler.java:35-37`.
- T006: o redirecionamento sem cookie ou com 401 está em `frontend/lib/loan-api.ts:29-30` e `:37`. O "Acesso negado." vindo do 403 está em `loan-api.ts:38`. No diff de `login-page.tsx`, só `role === 'colaborador'` vai a `/catalogo`.
- T007: foto 4:3 com `object-cover` e "Foto indisponível" em `loan-journey.tsx:61-65` (`bg-sunken` em `:61`). Crédito em `:136`, CSRF em `:85`, motivo e recarga em `:92-101`. "Emprestado a você" e o estado local estão em `:74`, `:99`, `:117`, `:131`. O botão "Solicitar" está em `:128-130`. Fora do disponível, o rodapé é `null` (`:131`). `equipment-card.tsx:6-12` não tem a chave `cabo`, e a V4 usa `cabo` (`V4__carga_inicial_equipamentos.sql:49-51`). `myDueAt` aparece em `loan-api.ts:11`.
- T008: `MyLoans` em `loan-journey.tsx:141-155`, com fuso em `:12`.

**Divergências que o mapeamento não lista:**
1. Conta `operacoes` desativada recebe 403 nas três rotas, e não 401 (`SecurityConfiguration.java:90` antes de `LoanService.java:83-88`), contra `spec.md:77-78`. Faz parte de B-02.
2. `GET /api/auth/session` não detecta conta desativada ou excluída (`AuthController.java:98-101`). Faz parte de B-02.
3. O 401 vindo do controller invalida a sessão, mas não expira o cookie (`LoanController.java:56-60`), ao contrário do filtro (`AuthenticatedSessionActivityFilter.java:149-153`). Não contraria a spec, mas o comportamento fica diferente.
4. O POST sem `Content-Type: application/json` cai no tratamento padrão do Spring (415), e não em 400 `INVALID_REQUEST`. `spec.md:80-81` diz "Corpo inválido retorna 400".
5. O teste local já cobre parte do CA-02 (`due_at = loaned_at + 14 days` e status `emprestado`) e parte do CA-05 (`/me` vazio para outra conta). A T003 e a T005 não registram essa cobertura parcial.
6. `ENTREGA.md` tem diff local não listado em plan.md:64-71. Ele diz que "a V3 e a V4 não foram gravadas" e que "a carga inicial continua pendente", o que contradiz o estado commitado (`ENTREGA.md:17`; `a46cbd7`). Cada tarefa manda atualizar o `ENTREGA.md` (`tasks.md:11`), então esse texto desatualizado pode acabar commitado.

## Contradições

- Plano × ADR-001 sobre acesso ao banco (B-01).
- T006 × spec:97-99 e CA-05 sobre conta desativada ou excluída nas páginas; T005 × spec:77-78 para `operacoes` desativada (B-02).

## Inconsistências não bloqueantes

- O plano não lista a estrutura de pastas e arquivos que as tarefas vão criar (pacote `loan`, `frontend/app/catalogo/`, `frontend/app/meus-emprestimos/`, `frontend/lib/loan-api.ts`, nova classe de teste da Spec 004 citada em `tasks.md:66`). Hoje só aparecem como "código local existente" (plan.md:64-71). `rules/restrictions.md:20` exige que a estrutura nova esteja num plano aprovado.
- A T006 cria as páginas antes da T007 e da T008, mas não diz o que as páginas renderizam nesse commit intermediário. Se o `Catalog` local entrar inteiro, o código com "Emprestado a você" (contra CA-07) chega à main, mesmo que por pouco tempo. Isso contraria plan.md:77-79.
- A tabela de divergências (`tasks.md:166`) põe "CA-04 (proposta do plano)" como critério para o 500 sem envelope, mas a proposta (plan.md:84-90) ainda não foi aprovada e não define o corpo do 500.
- `rules/tests.md:16` pede que o nome ou um comentário do teste cite o CA-xx. As tarefas não lembram disso.
- `rules/checks.md:15` pede `docker compose build backend frontend` por tarefa quando ela afeta artefatos. O fechamento das tarefas (`tasks.md:3-11`) só prevê isso na T009.
- A T002 mexe em `docker-compose.yml` e no README, arquivos que não aparecem em "Contrato afetado" (spec:119-127). A Spec 003 (`spec.md:73-75`) diz que a 004 "só consome a URL". A extensão faz sentido para a conferência local, mas não foi declarada como desvio de escopo.
- O commit sugerido da T009 é `test(...)`, mas a tarefa é de verificação e documentação.

## Duplicações

- O fechamento padrão (`tasks.md:3-11`) e a seção do plano sobre worktree limpo (plan.md:53-60) repetem o mesmo procedimento. Não há conflito, mas há redundância.
- As divergências aparecem duas vezes: no bloco "Código local" de cada tarefa e na tabela final (`tasks.md:158-172`). Elas estão consistentes entre si, mas o custo de manter as duas é alto.

## Riscos não bloqueantes

- Com a conta e o equipamento bloqueados com `FOR UPDATE` em READ COMMITTED, a violação de `uq_loan_open_equipment` prevista na proposta (plan.md:87-88) só deve acontecer com escrita fora da API. Se a proposta for aprovada, identifique a violação por SQLState e nome da constraint, nunca pelo texto da mensagem (spec:91-92).
- O CA-01 e o CA-06 dependem das oito fotos e do upload que faltam na Spec 003 (T003/T004 em aberto, `003-dados-iniciais/tasks.md:5`, `:7`).
- O worktree limpo fica no OneDrive, e `docker compose run --rm backend-test` copia `backend/src` inteiro. É preciso garantir que o comando rode a partir da raiz do worktree, e não da raiz principal.
- Os empréstimos da conta de teste ficam abertos em produção depois do CA-06 (spec:147-149).

## Perguntas para o operador

1. Acesso ao banco na Spec 004: segue o ADR-001 (JPA/Hibernate) ou o operador aceita `JdbcTemplate` com SQL no service? No segundo caso, o desvio precisa ser registrado em ADR (B-01).
2. Como as páginas e as rotas devem detectar conta desativada ou excluída antes de checar o papel? É aceitável mudar `GET /api/auth/session` (contrato da Spec 001) para consultar o banco (B-02)?
3. A proposta para a rejeição inesperada do banco (plan.md:84-90) está aprovada? O 500 deve usar o envelope `{code, message}`?
4. Para o CA-03 na interface, qual é a evidência esperada para cada um dos quatro motivos? `EQUIPMENT_MAINTENANCE` não pode ser provocado pela interface, porque o card não tem ação.

## Conferido

AGENTS.md; `.codex/agents/spec-validator.toml`; `rules/restrictions.md`, `spec-flow.md`, `checks.md`, `tests.md`, `migration.md`, `operacao.md`; `docs/PRD.md`; `docs/specs/004-catalogo-emprestimos/spec.md`, `plan.md`, `tasks.md`; `docs/adr/001-stack.md`, `002-design-system.md`, `003-hospedagem-backend.md`; `docs/specs/003-dados-iniciais/spec.md` e `tasks.md`; `EquipmentImageUrls.java`; `docker-compose.yml`; `frontend/next.config.ts`; `frontend/Dockerfile`; `frontend/proxy.ts:11` (CSP com `img-src 'self'`); `V2__modelo_inicial.sql`; `V4__carga_inicial_equipamentos.sql` (categorias); `globals.css:54` (`--gradient-equipment`); `LoanController.java`; `LoanService.java`; `loan-journey.tsx`; `loan-api.ts`; `app/catalogo/page.tsx`; `app/meus-emprestimos/page.tsx`; `equipment-card.tsx`; `AuthController.java`; `AuthenticatedUser.java`; `JsonAccessDeniedHandler.java`; `JsonAuthenticationEntryPoint.java`; `AuthenticatedSessionActivityFilter.java`; `SecurityConfiguration.java`; diffs locais de `SecurityConfiguration.java`, `login-page.tsx`, `AuthenticationFoundationIntegrationTest.java`, `README.md` e `ENTREGA.md`. O plano não cria migration nem introduz biblioteca nova, cita os ADRs 001, 002 e 003 e trata mudança de schema conforme `rules/migration.md` (plan.md:38-41; tasks.md:13-15). Os itens marcados como proposta estão claramente pendentes de decisão (plan.md:82-90; tasks.md:74). Nenhum arquivo foi alterado nesta rodada.
