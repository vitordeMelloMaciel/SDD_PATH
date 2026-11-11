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
6. Quando a tarefa concluir uma spec, rode o `spec-validator` em contexto limpo no modo `verificacao` e registre a tabela conforme `rules/spec-flow.md`.

## Verificacao
Pronto significa comandos sem falha, `git status` sem surpresa e, quando houver spec concluida, resultado final `APROVADA` do verificador.

## Nao faca
- Nao relate sucesso parcial.
- Nao repita a mesma tentativa de correcao sem examinar e mostrar o erro.
- Nao permita que o verificador altere codigo ou teste, mesmo quando essa pareca ser a unica falha.
