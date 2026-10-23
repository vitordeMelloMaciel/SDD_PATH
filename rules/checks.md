---
description: O que precisa passar antes de declarar uma tarefa pronta
globs: []
alwaysApply: true
---

# Verificacao de fim de tarefa
> leitor: agente

## Quando
Antes de dizer "pronto", "implementado" ou "funcionando".

## Procedimento
1. Rode os testes do componente alterado conforme `rules/tests.md`.
2. Rode `docker compose build backend frontend` quando a tarefa puder afetar os artefatos.
3. Se houver migration Flyway, siga integralmente `rules/migration.md`.
4. Rode `git status --short` e confira o diff dos arquivos da tarefa.
5. Diga qual criterio de aceitacao a tarefa atende, quando houver uma spec.

## Verificacao
Pronto significa comandos sem falha e `git status` sem surpresa.

## Nao faca
- Nao relate sucesso parcial.
- Nao repita a mesma tentativa de correcao sem examinar e mostrar o erro.
