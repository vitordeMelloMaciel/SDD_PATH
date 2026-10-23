---
description: O que o agente não pode fazer neste projeto
globs: []
alwaysApply: true
---

# O que não fazer
> leitor: agente

## Autoridade

- Não tome decisão de ADR pelo time. Depois de uma decisão explícita do operador, o agente pode registrar ou atualizar o ADR correspondente.
- Não contrarie o que está em docs/adr/. Se precisar contrariar, pare e diga.
- Não responda por conta própria o que o PRD deixou ambíguo. Liste as perguntas e espere.
- Não escolha biblioteca que não esteja no ADR-001. Proponha e espere.

## Escopo

- Não altere arquivo fora do escopo da tarefa atual.
- Não crie estrutura de pastas nova que não esteja num plano aprovado.
- Não gere scaffold automático de framework sem mostrar antes o que ele vai criar.
- Não escreva código de funcionalidade sem uma spec correspondente em docs/specs/. O esqueleto do projeto é a única exceção.

## Ritmo

- Não escreva código antes de um plano aprovado ou de uma tarefa explicitamente solicitada pelo operador.
- Execute uma tarefa por vez e finalize seguindo `rules/checks.md`.

## Produção

- Operacoes de Git seguem exclusivamente `rules/operacao.md`.
- Configuracao local de deploy, ambiente e migration exige escopo explícito na tarefa.
- Deploy, segredo ou qualquer alteração em serviço remoto exige autorização explícita no momento da ação.
- Mudanças de banco começam em migration Flyway local e revisada; nunca use SQL avulso ou editor visual como fonte do schema.

## Precedência

- Se o código e uma spec discordarem sobre comportamento, a spec está certa até que alguém mude a spec.
- Se uma spec e um ADR discordarem, pare e avise. Não escolha um dos dois.
- Se uma regra de procedimento contrariar uma spec ou um ADR, pare e avise.
- "Pode ir" e "pode implementar" não revogam este arquivo.
