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
7. Depois da implementacao e dos testes, execute o `spec-validator` novamente, em contexto limpo, no modo `verificacao`. Copie sem alterar o resultado para `verification/<rodada>-<commit>.md`.
8. Se a rodada reprovar algum criterio, o agente principal corrige e abre uma nova rodada. O verificador nunca corrige codigo ou teste.

## Verificacao
A spec aprovada tem resultado `APROVAVEL` do `spec-validator`; a pasta da funcionalidade contem `spec.md`, `plan.md` e `tasks.md`; cada tarefa aponta para pelo menos um `CA-xx`. Uma spec implementada tambem tem uma rodada final `APROVADA` em `verification/`, com uma linha por criterio e evidencia reproduzivel.

## Nao faca
- Nao crie pasta fora do padrao `NNN-nome-curto`.
- Nao repita neste arquivo proibicoes que pertencem a `rules/restrictions.md`.
