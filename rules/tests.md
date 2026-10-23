---
description: Como executar e escrever testes neste projeto
globs: ["backend/src/test/**", "frontend/src/**/*.test.*"]
alwaysApply: false
---

# Testes
> leitor: agente

## Quando
Ao escrever, alterar ou remover teste.

## Procedimento
1. Backend roda com `docker compose run --rm backend-test` e PostgreSQL isolado por Testcontainers.
2. Teste de endpoint verifica status e corpo.
3. Quando existir spec, o nome ou comentario do teste cita o `CA-xx` provado.
4. Regra de autorizacao precisa de teste negativo: outro usuario nao ve nem altera o recurso.

## Verificacao
O comando de teste passa sem depender de banco remoto ou dado manual.

## Nao faca
- Nao use mock para provar autorizacao ou comportamento que depende do PostgreSQL.
- Nao rode teste contra banco remoto.
