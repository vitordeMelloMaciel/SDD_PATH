---
description: Procedimento para qualquer mudanca de schema PostgreSQL
globs: ["backend/src/main/resources/db/migration/**"]
alwaysApply: false
---

# Mudanca de schema
> leitor: agente

## Quando
Ao criar, alterar ou remover tabela, coluna, indice, constraint ou regra de acesso.

## Procedimento
1. Antes de gerar qualquer arquivo, apresente o DDL pretendido, os criterios atendidos e o efeito sobre dados existentes; pare para aprovacao do operador.
2. Depois da aprovacao, crie uma unica migration Flyway `V<numero>__<nome>.sql` por tarefa.
3. Explique como a mudanca evolui dados existentes e como seria revertida por uma nova migration.
4. Garanta que acesso a tabelas novas continue exclusivo ao backend; autenticacao e autorizacao pertencem ao Spring Security conforme ADR-001. RLS do Supabase nao substitui esse contrato.
5. Rode `docker compose run --rm backend-test`; Testcontainers deve aplicar todas as migrations em PostgreSQL vazio e descartavel.
6. Se houver ambiente local ativo, suba-o com `docker compose up -d --build` e confirme `/health`, sem remover volumes sem autorizacao.

## Verificacao
Um banco vazio aplica todas as migrations e o endpoint `/health` retorna banco `up`.

## Nao faca
- Nao altere schema por ferramenta visual ou SQL avulso.
- Nao edite migration que ja entrou no historico remoto; crie a seguinte.
- Nao aplique migration em Supabase ou outro banco remoto nesta etapa; acao remota segue `rules/restrictions.md`.
