# Handoff

- Branch `main`. Tela de login e proxy same-origin publicados em `e0cbaca`. API em `f77e5bb`, casos HTTP de erro e cookie seguro em `5f1dc57`, provisionamento em `a5a6f1d`.
- Backend passou com 15 testes via Testcontainers. O comando administrativo foi executado três vezes com PostgreSQL descartável, retornou 0, 0 e 3 e deixou um único usuário.
- Frontend passou em `pnpm run typecheck` e `pnpm run build` dentro do Docker. Teste local pelo Next.js confirmou raiz 200 com CSP, login 200, cookie HttpOnly, logout 204 e sessão encerrada 401.
- Spec 001 ainda precisa de verificação de teclado, foco e alertas no navegador, matriz final e rodada independente pós-implementação. A tentativa de abrir o navegador de teste expirou.
- A raiz pública da Vercel respondeu 200 com a tela de login. `GET /api/auth/csrf` retornou 404. Falta conectar um host HTTPS do backend Spring via `INTERNAL_API_BASE_URL` e validar o fluxo publicado.
- Spec 002 continua sendo o schema Flyway existente. A Spec 003 recebeu `APROVAVEL` e foi aprovada pelo operador. A V3 aguarda autorização específica do DDL. A chave Pixabay funciona e quatro das 12 fotos foram aprovadas localmente. O operador executa as escritas no Supabase de produção.
- O operador confirmou acesso e fuso da Spec 004. A revisão 04 pediu proteção das páginas, invalidação verificável e compatibilidade do rodapé do card com ADR-002. A spec registra a proposta de mostrar a devolução apenas ao titular do empréstimo.
- Restam catálogo, solicitação e Meus empréstimos. A entrega final precisa da matriz verificada em produção, do `ENTREGA.md` e da tag `entrega-pratica` após autorização específica do operador.
