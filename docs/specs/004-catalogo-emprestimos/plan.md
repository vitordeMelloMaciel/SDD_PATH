# Plano da Spec 004 — Catálogo e empréstimos

**Estado** · Proposto em 2026-11-11 para aprovação do operador.

## Objetivo do plano

Implementar a Spec 004 aprovada em fatias pequenas: API, autorização,
páginas e card. Cada fatia é testável e cabe em um commit, sobre o schema
das Specs 002 e 003.

## Restrições

- **ADR-001:** Java 21, Spring Boot, Spring Security com sessão HTTP-only,
  PostgreSQL e testes com Testcontainers. O acesso ao banco segue o padrão
  já usado (`JdbcTemplate`). Nenhuma biblioteca nova, no backend ou no
  frontend. Next.js com TypeScript, Tailwind e componentes no padrão shadcn/ui.
- **ADR-002:**
  - O card usa a variante com foto do EquipmentCard: contêiner 4:3, fundo
    `--gradient-equipment`, `object-fit: cover` e “Foto indisponível”.
  - Os rodapés são “Solicitar →”, “Emprestado” e “Em manutenção”.
  - Nenhuma cor nova, estilo inline nem CDN, e a CSP não muda.
  - O card emprestado nunca mostra pessoa nem data.
- **ADR-003:** em produção, o front na Vercel encaminha `/api/*` para o
  backend no Railway por `INTERNAL_API_BASE_URL`. O navegador só chama
  `/api` na mesma origem. O cookie de sessão é `Secure`.
- **Banco:**
  - Nenhuma migration nesta spec. V2 (trigger, índice e ordem de bloqueio)
    e V3/V4 (Spec 003) permanecem inalteradas.
  - A API não atualiza `equipment.status`; a trigger da V2 faz isso.
- **Dependências:**
  - `imageUrl` depende da composição de `EQUIPMENT_IMAGE_BASE_URL`
    implementada pela T006 da Spec 003. A 004 só consome essa composição.
  - O CA-01 e o CA-06 dependem das fotos no Storage (T003 e T004 da Spec 003).
- **Testes de interface:** não há biblioteca de teste de frontend no
  ADR-001. A interface é validada por `pnpm run typecheck`, `pnpm run build`
  e conferência no navegador local, como na Spec 001.

## Código local existente

Há uma implementação local, não rastreada e não aprovada, em:

- `backend/src/main/java/br/org/germinare/equipamentos/loan/`;
- `frontend/app/catalogo/` e `frontend/app/meus-emprestimos/`;
- `frontend/components/loan-journey.tsx` e `frontend/lib/loan-api.ts`;
- alterações locais em `SecurityConfiguration.java`, `login-page.tsx` e
  `AuthenticationFoundationIntegrationTest.java`.

Ela pode servir de ponto de partida. Cada tarefa leva para o commit só o
trecho que atende ao seu CA, já corrigido conforme a spec. O resto continua
fora da `main` até a tarefa correspondente. O `tasks.md` indica, por tarefa,
o que o código local já cobre e o que diverge.

## Sequência

### 1. API do catálogo com privacidade

- `GET /api/equipment` devolve todo o acervo, ordenado por patrimônio, com
  exatamente `id`, `assetCode`, `name`, `category`, `status` e `imageUrl`.
- A consulta não lê `loan`: sem dono, sem datas, sem ID de empréstimo.
- `imageUrl` usa o componente da T006 da Spec 003. É `null` sem base ou
  sem chave.
- Teste HTTP com duas contas (A titular e B sem empréstimo): o objeto do
  item emprestado é idêntico para as duas e não contém nenhum dado de A.

Atende: CA-07 e a parte de API do CA-01.

### 2. Solicitação e Meus empréstimos

- `POST /api/loans`:
  - bloqueia a conta e depois o equipamento na mesma transação da
    inserção, na ordem da trigger da V2;
  - classifica os conflitos na ordem da V2 (manutenção, emprestado,
    limite, atraso) a partir da leitura sob bloqueio, e não do texto da
    exceção;
  - insere sem `due_at`, para a trigger calcular `loaned_at + 14 days`;
  - devolve 201 com a representação de Meus empréstimos.
- `GET /api/loans/me` devolve os empréstimos abertos do principal,
  ordenados por vencimento e ID, com `dueAt` em ISO 8601 com offset e
  `overdue`.
- Erros 400 `INVALID_REQUEST` e 404 `EQUIPMENT_NOT_FOUND`.
- **Proposta para aprovação.** Se o banco ainda assim rejeitar a inserção
  (trigger ou `uq_loan_open_equipment`), a transação é revertida e a API
  responde 409 `EQUIPMENT_BORROWED` só quando a violação for do índice
  `uq_loan_open_equipment`. Qualquer outra rejeição vira 500, sem
  empréstimo. A spec não define esse caso.
- Testes com PostgreSQL real:
  - as quatro recusas 409 sem inserção, mais os casos 400 e 404;
  - duas solicitações concorrentes ao mesmo item (um 201, um 409
    `EQUIPMENT_BORROWED`);
  - duas solicitações concorrentes a itens distintos, com dois empréstimos
    já abertos (um 201, um 409 `LOAN_LIMIT`, três abertos ao final);
  - `due_at = loaned_at + interval '14 days'`.

Atende: CA-02, CA-03 e CA-04.

### 3. Autorização, CSRF e sessão inválida nas três rotas

- As três rotas exigem o papel `colaborador`. `operacoes` recebe 403
  `FORBIDDEN` (“Acesso negado.”).
- Sem sessão, a resposta é 401 `UNAUTHENTICATED`.
- No POST, o CSRF é avaliado primeiro: 403 `CSRF_INVALID` com e sem sessão.
- Conta desativada ou excluída: 401, sessão encerrada, nenhum empréstimo
  criado, e o cookie antigo continua recebendo 401.
- Teste negativo: outra conta não vê empréstimos alheios em Meus
  empréstimos.

Atende: a parte de API do CA-05.

### 4. Portão das páginas e destino após o login

- `/catalogo` e `/meus-emprestimos` consultam `GET /api/auth/session` no
  servidor antes de renderizar.
  - Sem sessão, ou com 401, redirecionam para `/`.
  - Para `operacoes`, mostram só “Acesso negado” e “Esta área é exclusiva
    de colaboradores.”, sem chamar as três rotas.
- A página de login leva as duas contas a `/catalogo` após o login.
- Conferência no navegador: acesso direto sem sessão, colaborador e
  `operacoes`.

Atende: a parte de páginas do CA-05 e o redirecionamento do CA-01.

### 5. Catálogo e card

- O card segue a variante com foto do `EquipmentCard` (ADR-002), no lugar
  do ícone. Os rodapés são “Solicitar →”, “Em manutenção” e “Emprestado”.
- O card exibe só o estado recebido da API. Depois de uma solicitação, a
  página recarrega os dados.
- O motivo das recusas 409 aparece na interface. Há um único crédito,
  “Fotos: Pixabay”, com link para a Pixabay.
- Typecheck, build e conferência no navegador local com as 12 fotos (ou
  com o estado “Foto indisponível”, enquanto não houver fotos).

Atende: CA-01, a parte de interface do CA-03 e a do CA-07.

### 6. Meus empréstimos

- A lista mostra patrimônio, nome e data de devolução em
  `America/Sao_Paulo`, com destaque para atraso, e mensagem quando está vazia.

Atende: a parte de interface do CA-02 e a do CA-05.

### 7. Verificação final

- Rodar a suíte do backend em worktree limpo e
  `docker compose build backend frontend`.
- Executar a matriz no navegador local, sem usar a conta do professor.
- Roteiro do CA-06 em produção, executado pelo operador com conta de
  teste, e consulta de leitura com zero empréstimos para o professor.
- `spec-validator` em modo verificação, com a tabela em `verification/`.
- Atualizar `ENTREGA.md` e `handoff.md`.

Atende: CA-01 a CA-07.
