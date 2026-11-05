# Plano - Modelo de dados inicial

**Restricao arquitetural:** `docs/adr/001-stack.md` define PostgreSQL, Flyway e testes de integracao com banco real.

1. Criar uma migration Flyway V2 com as tres tabelas, constraints, indices e triggers descritos na spec.
2. Adicionar testes de integracao com PostgreSQL real para existencia do schema e invariantes criticas.
3. Recriar o banco local, executar testes e validar o health check.

Este plano foi aprovado pelo pedido explicito para criar as tabelas do PRD via migration.
