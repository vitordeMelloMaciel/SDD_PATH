# Handoff

- Branch `main`. Tela de login e proxy same-origin publicados em `e0cbaca`. API em `f77e5bb`, casos HTTP de erro e cookie seguro em `5f1dc57`, provisionamento em `a5a6f1d`.
- Backend passou com 15 testes via Testcontainers. O comando administrativo foi executado três vezes com PostgreSQL descartável, retornou 0, 0 e 3 e deixou um único usuário.
- Frontend passou em `pnpm run typecheck` e `pnpm run build` dentro do Docker. Teste local pelo Next.js confirmou raiz 200 com CSP, login 200, cookie HttpOnly, logout 204 e sessão encerrada 401.
- Spec 001 ainda precisa de verificação de teclado, foco e alertas no navegador, matriz final e rodada independente pós-implementação. A tentativa de abrir o navegador de teste expirou.
- A raiz pública da Vercel respondeu 200 com a tela de login. `GET /api/auth/csrf` retornou 404. O operador escolheu o Railway para o backend Spring (ADR-003). Falta criar o serviço, configurar `INTERNAL_API_BASE_URL` na Vercel e validar o fluxo publicado; deploy e segredos exigem autorização no momento da ação.
- Spec 002 continua sendo o schema Flyway existente. A Spec 003 recebeu `APROVAVEL` nas rodadas `pre-approval-02` e `pre-approval-03` e foi aprovada pelo operador em 2026-11-11, junto com `plan.md`, `tasks.md` e o DDL da V3. A V4 ainda não foi aprovada. A chave Pixabay funciona e quatro das 12 fotos foram aprovadas localmente. O operador executa as escritas no Supabase de produção.
- A Spec 004 recebeu `APROVAVEL` na rodada `pre-approval-07` e foi aprovada pelo operador em 2026-11-11. O card emprestado mostra somente “Emprestado” para todas as contas (exceção no ADR-002, CA-07); o card em manutenção mostra “Em manutenção”. Faltam `plan.md` e `tasks.md` da 004.
- O código local não rastreado da 004 continua fora da main. Ele ainda mostra “Emprestado a você” e expõe `myDueAt` no catálogo, o que contraria a Spec 004 aprovada. `frontend/next.config.ts` tem alteração local que pertence à T006 da Spec 003.
- Restam catálogo, solicitação e Meus empréstimos. A entrega final precisa da matriz verificada em produção, do `ENTREGA.md` e da tag `entrega-pratica` após autorização específica do operador.
