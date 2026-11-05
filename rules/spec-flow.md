---
description: Ordem dos artefatos ao iniciar uma funcionalidade
globs: ["docs/specs/**"]
alwaysApply: false
---

# Fluxo de spec
> leitor: agente

## Quando
Ao iniciar qualquer funcionalidade nova do PRD.

## Procedimento
1. Crie `docs/specs/<NNN-nome-curto>/spec.md`.
2. Liste lacunas do PRD e espere as respostas antes de fechar a spec.
3. Delegue a leitura ao subagente `spec-validator`; corrija bloqueios e preserve as perguntas que dependem do operador.
4. Depois da aprovacao humana, escreva `plan.md` a partir da spec e cite os ADRs que o restringem.
5. Escreva `tasks.md`; cada tarefa cita um `CA-xx` e cabe em um commit.
6. Apos aprovacao, execute conforme a autoridade de `rules/restrictions.md` e finalize seguindo `rules/checks.md`.

## Verificacao
A spec aprovada tem resultado `APROVAVEL` do `spec-validator`; a pasta da funcionalidade contem `spec.md`, `plan.md` e `tasks.md`; cada tarefa aponta para pelo menos um `CA-xx`.

## Nao faca
- Nao crie pasta fora do padrao `NNN-nome-curto`.
- Nao repita neste arquivo proibicoes que pertencem a `rules/restrictions.md`.
