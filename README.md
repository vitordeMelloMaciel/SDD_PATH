# SDD Path

Sistema interno para controlar emprestimos de equipamentos. O projeto usa Spring Boot, Vite/TypeScript e PostgreSQL, conforme [docs/adr/001-stack.md](docs/adr/001-stack.md).

## Executar localmente

```bash
docker compose up --build
```

- Frontend: `http://localhost:5173`
- Health da API: `http://localhost:8080/health`

Testes do backend:

```bash
docker compose run --rm backend-test
```

## Deploy

A Vercel publica apenas o frontend Vite usando `vercel.json`. O backend Spring Boot precisa de uma hospedagem Java separada; em producao, ele podera usar o PostgreSQL do Supabase por JDBC.

O Supabase ainda nao foi provisionado. Consulte `supabase/README.md` e nao adicione credenciais ao Git.

## Processo

Leia `AGENTS.md` e `rules/restrictions.md` antes de qualquer alteracao. Funcionalidades de produto comecam em `docs/specs/`.
