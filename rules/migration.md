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
1. Confirme que a tarefa ou o plano aprovado descreve o DDL e seus criterios.
2. Crie uma unica migration Flyway `V<numero>__<nome>.sql` por tarefa.
3. Explique o efeito sobre dados existentes e como desfazer a mudanca.
4. Rode `docker compose run --rm backend-test`; Testcontainers deve aplicar todas as migrations em PostgreSQL vazio e descartavel.
5. Se houver ambiente local ativo, suba-o com `docker compose up -d --build` e confirme `/health`, sem remover volumes sem autorizacao.

## Verificacao
Um banco vazio aplica todas as migrations e o endpoint `/health` retorna banco `up`.

## Nao faca
- Nao altere schema por ferramenta visual ou SQL avulso.
- Nao edite migration que ja entrou no historico remoto; crie a seguinte.
- Nao aplique migration em Supabase ou outro banco remoto nesta etapa; acao remota segue `rules/restrictions.md`.
