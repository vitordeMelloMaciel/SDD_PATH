# Rodada pre-aprovacao 03

- Base: `8d40630` com alteracoes locais na Spec 003 (`spec.md`, `tasks.md`)
- Executor: instrucoes do `spec-validator` executadas por subagente Claude Code em contexto limpo (Codex CLI indisponivel nesta maquina)
- Sandbox: somente leitura, por instrucao
- Data: 2026-11-11

## Saida do verificador

# Resultado: APROVÁVEL

**Rodada:** pré-aprovação 03 (Spec 003)
**Estado avaliado:** commit `8d40630` com alterações locais não commitadas. Em `docs/specs/003-dados-iniciais/`, o `git diff` mostra mudanças em `spec.md` (cabeçalho em `:3-10`, `:74-76` e CA-03/CA-04 em `:103-104`) e em `tasks.md` (nova T006 em `:6`). Desde a base da rodada 02 (`94a8197`), o commit `a726f85` também versionou a spec da rodada 02.
**Severidade geral:** baixa. As duas decisões do operador estão na spec de forma coerente e verificável. Nenhuma das mudanças cria bloqueio. O que sobra são lacunas de cobertura e documentos fora da spec ainda desalinhados.
**Aprovação humana:** ainda necessária

## Bloqueios

Nenhum.

## Critérios não observáveis

Nenhum critério ficou não observável a ponto de bloquear. Há alguns pontos de precisão:

- **CA-04, comparação com a URL pública** (`spec.md:104`): "a URL que o backend monta para cada patrimônio é igual a essa URL pública" tem como evidência só um "teste automatizado em `backend/src/test/java/`". Um teste automatizado só consegue compor a URL com uma base fictícia. A igualdade com a URL pública real de produção não tem evidência de produção definida, e a 003 não expõe endpoint que a mostre. O endpoint é o `GET /api/equipment` da Spec 004 (`004…/spec.md:50-60`). Vale deixar explícito que o teste prova a regra `base + "/" + chave` e que a igualdade em produção vem da 004 (CA-06).
- **CA-03, revisor "sem e-mail"** (`spec.md:103`): o critério pode ser conferido na cópia em `verification/manifesto/`, por exemplo buscando `@` nos campos `reviewer` e `reviewedBy`. Mas nada garante isso na origem. O script exige só que o campo não esteja vazio (`prepare-images.mjs:88,156`), e `SKILL.md:25` diz apenas "o revisor". Como a cópia precisa ser "sem alteração", o formato tem que estar certo já ao preencher `approvals.json`. Hoje o estado local confere: das 12 entradas, 4 estão preenchidas, 8 estão vazias e nenhuma contém `@`.
- **Pontos da rodada 02 que continuam abertos:**
  - **CA-02, produção** (`spec.md:102`): continua sem definir o recorte da "leitura posterior".
  - **CA-05** (`spec.md:105`): a confirmação de login pelo operador continua sem forma de registro.
  - **CA-06** (`spec.md:106`): os termos da "busca no diff" continuam indefinidos.

## Contradições

Nada se contradiz dentro da spec, nem entre ela e o PRD, o ADR-001, a Spec 002, a V2 ou a Spec 004.

- **Decisão (a), nome do revisor:** está refletida em `spec.md:103`, com "somente o nome… sem e-mail" e a cópia sem alteração. É compatível com `rules/secrets.md` e com o CA-06 (`spec.md:106`), porque nome não é senha, chave nem token.
- **Decisão (b), `EQUIPMENT_IMAGE_BASE_URL`:** está refletida em `spec.md:74-76`, no CA-04 (`spec.md:104`) e na T006 (`tasks.md:6`). É coerente com a Spec 004, que só consome a URL (`004…/spec.md:57-60` e `:121`). Também cumpre `rules/secrets.md:16`, que exige a variável em `.env.example` sem valor real.
- **Fora da spec, continua uma divergência:** `handoff.md:8` ainda diz que a Spec 003 "foi aprovada pelo operador", enquanto `spec.md:3-8` diz que ela está em revisão.

## Inconsistências não bloqueantes

- **A parte de frontend da T006 não tem critério na 003.** A T006 cita só o CA-04 (`tasks.md:6`), que cobre o backend e o `.env.example`. A regra do `next.config.ts` ("apenas essa origem e esse caminho; sem a variável, nenhuma origem remota", `spec.md:71-73`) agora é tarefa desta spec, mas nenhum CA da 003 a verifica. A prova fica indireta, pelo CA-01 e pelo CA-06 da 004 (`004…/spec.md:133,138`). O caminho `/equipment/**` continua implícito na spec, mas explícito em `frontend/next.config.ts:10`.
- **O `plan.md` não acompanha a spec.** `plan.md:16-28` não tem o passo da configuração da URL (T006), o `psql --single-transaction` de produção nem a pasta `verification/manifesto/`. A pasta precisa entrar no plano reconfirmado (`rules/restrictions.md:20`). `spec.md:9-10` já prevê essa reconfirmação.
- **`tasks.md` mudou antes da aprovação.** A T006 foi adicionada antes da aprovação humana da spec (`rules/spec-flow.md:5`). Os números também saem de ordem (T006 antes de T004). O cabeçalho já exige reconfirmação, e em `spec.md:9` "dessa rodada" ficou ambíguo depois da reescrita.
- **Pré-requisito da 004 ambíguo.** `004…/spec.md:23-24` diz "Spec 003 implementada para CA-01 e CA-06" e inclui ali `EQUIPMENT_IMAGE_BASE_URL`. Se esses CAs forem os da 003, a lista não bate: na 003, a variável e as fotos agora são o CA-04, e o CA-06 é a busca de segredos. Se forem os CAs da própria 004, está correto, mas o texto não diz isso.
- **As decisões (a) e (b) não estão registradas fora da spec.** `spec.md:5` cita "decisões do operador de 2026-11-11", mas nem `ENTREGA.md` nem `handoff.md` registram o nome do revisor ou a atribuição da variável (busca por `revisor` e `EQUIPMENT_IMAGE_BASE_URL` sem resultado). `ENTREGA.md:15` ainda conta "003 (duas)" rodadas, e `ENTREGA.md:49` ainda diz "Proposta… Rascunho".

## Duplicações

- A regra de composição da URL e do otimizador está em `spec.md:68-73` e em `004…/spec.md:57-60`. O texto é coerente, e agora a responsabilidade pela implementação está definida na 003 (`spec.md:74-76`).
- O CA-05 da 003 (`spec.md:105`) e o CA-06 da 004 (`004…/spec.md:138`) pedem a mesma consulta de zero empréstimos. Isso já vinha das rodadas anteriores.

## Riscos não bloqueantes

- **Implementação adiantada.** `frontend/next.config.ts:3,10` já implementa localmente parte da T006 (diff não commitado), antes da aprovação da spec e do plano (`rules/restrictions.md:26`). O código da Spec 004 também já existe sem rastreio no Git (`backend/.../loan/`, `frontend/lib/loan-api.ts`). Hoje `imageUrl` é fixado em `null` (`LoanService.java:41`). Fora do contrato de imagem, o `EquipmentView` tem um campo `myDueAt` (`LoanService.java:107-108`) que não está entre os seis campos de `004…/spec.md:52-53`. Isso é assunto da verificação da 004.
- **Barra final na base.** A spec define a base "sem barra final" (`spec.md:64-65`). O front remove a barra final (`next.config.ts:3`), mas a spec não diz o que o backend faz com uma base que tenha barra. Pode surgir `//` na URL e falhar o `remotePatterns`.
- **Nome do revisor versionado.** É aceito pela decisão (a). Fica no histórico Git em definitivo.
- **Riscos que continuam da rodada 02:**
  - A V4 pode abortar na primeira aplicação se houver colisão.
  - A variável é lida no build do Next.js.
  - O briefing não está versionado.
  - TI-0002 e TI-0004 têm o mesmo nome (`equipment.json:3,5`).
  - A API pública ainda retorna 404.
  - Só 4 das 12 fotos estão aprovadas.

## Perguntas para o operador

Não há pergunta de negócio pendente, o que confere com `spec.md:108-110`. Há confirmações de procedimento para a reconfirmação do plano:

1. A parte da T006 no `next.config.ts` deve ter um critério verificável na 003, ou basta a prova indireta pelo CA-01 e pelo CA-06 da 004?
2. A edição local em `frontend/next.config.ts` deve ser considerada trabalho da T006 já iniciado, a ser revisado depois da aprovação?

## Comparação entre rodadas

| Item da rodada 02 | Situação na rodada 03 | Evidência |
| --- | --- | --- |
| Configuração do backend sem CA nem tarefa na 003 | **Resolvido em parte.** Há tarefa (T006) e o CA-04 cobre a composição, o `null` e o `.env.example`. O `next.config.ts` continua sem CA na 003. | `spec.md:74-76,104`; `tasks.md:6` |
| Pergunta 1 da rodada 02 (variável fica na 003 ou na 004) | **Resolvido pela decisão (b):** fica na 003, e a 004 só consome. | `spec.md:74-76`; `004…/spec.md:57-60` |
| Pergunta 2 e risco da rodada 02 (nome do revisor no Git) | **Resolvido pela decisão (a):** somente o nome, sem e-mail. | `spec.md:103` |
| Estado divergente em `handoff.md:8` e `ENTREGA.md:17,46` | `ENTREGA.md:17` foi alinhado. `handoff.md:8` e `ENTREGA.md:49` continuam divergentes. | `ENTREGA.md:17`; `handoff.md:8`; `ENTREGA.md:49` |
| `plan.md` anterior à spec | **Persiste e aumentou:** agora também falta a T006 no plano. | `plan.md:16-28` |
| Precisão de CA-02, CA-05 e CA-06 | Persiste, sem bloqueio. | `spec.md:102,105,106` |
| Duplicações (URL; CA-05 da 003 com CA-06 da 004) | Persistem, coerentes. | `spec.md:68-73,105`; `004…/spec.md:57-60,138` |
| Novos bloqueios introduzidos pelas mudanças | **Nenhum.** | `git diff docs/specs/003-dados-iniciais/` |

## Conferido

- `AGENTS.md`, `.codex/agents/spec-validator.toml`, `rules/restrictions.md`, `rules/spec-flow.md`, `rules/checks.md`, `rules/migration.md` e `rules/secrets.md`.
- `docs/PRD.md`, `docs/adr/001-stack.md:55-72` e `docs/specs/002-modelo-dados/spec.md`.
- Spec 003:
  - `spec.md` (completo) e `git diff` local.
  - `plan.md` e `tasks.md`.
  - `verification/pre-approval-02-94a8197.md`.
  - `git log` e `git diff --stat 94a8197 HEAD` da pasta.
- Migrations: `V1__andar_zero.sql` e `V2__modelo_inicial.sql`, com a definição de `equipment` em `:18-30`. A V3 e a V4 ainda não existem.
- `.agents/skills/imagens-equipamentos/SKILL.md`, `references/equipment.json` e `scripts/prepare-images.mjs`, nos campos `reviewer` (`:88,135,156`) e `reviewedBy` (`:193`).
- `.local/pixabay-equipment/approvals.json`: contei só as entradas e as ocorrências de `@`. O arquivo é ignorado por `.gitignore:15`.
- Spec 004 no contrato de imagem: `spec.md:20-24,50-60,105-121,133,138`, com o `git diff` local.
- `frontend/next.config.ts` com o diff local, `README.md:105-120`, `ENTREGA.md` com o diff, `handoff.md:8` e `LoanService.java:41,95-108`.
- Busca por `EQUIPMENT_IMAGE`, `image_object_key` e `imageUrl` em `backend/src`, `frontend/lib`, `frontend/components`, `.env.example`, `README.md`, `ENTREGA.md` e `handoff.md`.

Trabalhei somente em leitura. Nenhum arquivo, banco, estado do Git ou serviço remoto foi alterado.
