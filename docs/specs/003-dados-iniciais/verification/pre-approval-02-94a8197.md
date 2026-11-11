# Rodada pre-aprovacao 02

- Base: `94a8197` com alteracoes locais na Spec 003
- Executor: instrucoes do `spec-validator` executadas por subagente Claude Code em contexto limpo (Codex CLI indisponivel nesta maquina)
- Sandbox: somente leitura, por instrucao
- Data: 2026-11-11

## Saida do verificador

# Resultado: APROVÁVEL

**Rodada:** pré-aprovação 02 (Spec 003)
**Estado avaliado:** commit `94a8197` com alterações locais não commitadas. Em `docs/specs/003-dados-iniciais/`, `spec.md` aparece modificado (`git diff`: cabeçalho, Regras, novas seções "Contrato afetado" e "Perguntas em aberto", e CA com coluna "Onde fica a evidência") e `verification/` aparece como não rastreada (contém só `pre-approval-01-94a8197.md`).
**Severidade geral:** baixa. Os dois bloqueios da rodada 01 foram tratados na spec. O que sobra é divergência em documentos fora da spec e tarefas de implementação que a spec já registra.
**Aprovação humana:** ainda necessária

## Bloqueios

Nenhum.

## Critérios não observáveis

Nenhum critério ficou não observável a ponto de bloquear. Há três pontos de precisão menor:

- **CA-05** (`spec.md:103`): o trecho "O operador confirma o login no deploy" depende de uma declaração do operador. A coluna de evidência só lista "Consulta SQL de leitura, resultado e data e hora" e não diz como registrar essa confirmação (por exemplo, status HTTP e horário) em `producao-<AAAA-MM-DD>.md`.
- **CA-02, parte de produção** (`spec.md:100`): "a leitura posterior é igual à anterior" não diz o recorte da leitura (os 12 patrimônios ou a tabela `equipment` inteira) nem as colunas. No teste, o recorte está definido: "a tabela `equipment` fica igual à leitura anterior".
- **CA-06** (`spec.md:104`): "Busca no diff" não define os termos nem os padrões da busca. A verificação final é que vai fixá-los.

## Contradições

Nada se contradiz dentro da spec, nem entre ela e o PRD, o ADR-001, a Spec 002, a V2 ou a Spec 004. Fora da spec, persistem estas divergências:

- **Estado da Spec 003:**
  - O cabeçalho corrigido diz "Em revisão de pré-aprovação" e que a aprovação humana "deve ser renovada" (`spec.md:3-9`).
  - `handoff.md:8` ainda diz "A Spec 003 recebeu `APROVAVEL` e foi aprovada pelo operador".
  - `ENTREGA.md:17` ainda diz "Spec 003 aprovada e plano versionado".
  - `ENTREGA.md:46` diz "Proposta… Rascunho".

  Esses arquivos não são a spec, então não bloqueiam a aprovação. Eles precisam ser alinhados antes de registrar a aprovação.
- **Restrições de produção:** `spec.md:84-86` diz que reexecutar o V4 versionado "não é SQL avulso nem altera schema". Isso é coerente com `rules/restrictions.md:34` e `rules/migration.md:25`, que proíbem SQL avulso como fonte do *schema*: V4 só carrega dados, e o DDL fica na V3 (`spec.md:51-59`). Quem executa é o operador, o que respeita `rules/restrictions.md:33` e `rules/migration.md:27`. A decisão (a) está refletida.

## Inconsistências não bloqueantes

- **A configuração do backend não tem CA nem tarefa na 003.**
  - `spec.md:67-69` define a composição `EQUIPMENT_IMAGE_BASE_URL + "/" + image_object_key` e o retorno `null`. `spec.md:70-72` define a autorização no `next.config.ts`.
  - Nenhum CA da 003 cobre esse comportamento: o CA-04 (`spec.md:102`) testa a URL pública direto no Storage. O comportamento é verificado pela Spec 004 (`004-catalogo-emprestimos/spec.md:55-58`, CA-01 em `:129`).
  - `spec.md:73-74` manda incluir a variável em `.env.example` "na tarefa que a implementar", mas nenhuma tarefa de `tasks.md:3-7` menciona a variável.
  - O backend ainda não a lê (busca em `backend/src/main/java` não acha `EQUIPMENT_IMAGE`). `.env.example:1-29` também não a contém.

  Isso depende da reconfirmação do plano (`spec.md:8-9`), então não bloqueia. Falta escolher se a tarefa fica na 003 ou na 004.
- **Plano anterior à spec:** `plan.md:22-23` fala em "Reexecutar o SQL de V4 na mesma base". Não menciona o `psql --single-transaction` de produção (`spec.md:81-86`) nem a pasta `verification/manifesto/` (`spec.md:101`). Como `rules/restrictions.md:20` proíbe pasta nova fora de plano aprovado, essa pasta tem que entrar no `plan.md` reconfirmado.
- **Pasta `verification/` já existe:** CA-01 e CA-03 apontam para dentro dela (`spec.md:99,101`), o que é coerente com `rules/spec-flow.md:20`.
- **Caminho no frontend:** `spec.md:71` diz "essa origem e esse caminho" e não explicita o padrão `/equipment/**`, que é o que `frontend/next.config.ts:10` implementa. É coerente com `README.md:113-114` ("sem o sufixo `/equipment`"), mas o caminho fica implícito na spec.
- **Lista fixada:** a fixação em `8052d1b` (`spec.md:23-28`) confere. `git log` de `references/equipment.json` mostra `8052d1b` como o último commit, e não há diff local. Os status de manutenção estão em `equipment.json:5,8,11`.

## Duplicações

- O CA-05 da 003 (`spec.md:103`) e o CA-06 da 004 (`004-catalogo-emprestimos/spec.md:134`) exigem os dois a consulta de zero empréstimos da conta de avaliação. Isso já vinha da rodada 01.
- A regra de composição da URL e de autorização no Next.js aparece em `spec.md:67-72` e em `004-catalogo-emprestimos/spec.md:55-58`. O texto é coerente, mas está em dois lugares.
- A data de aprovação da Spec 003 aparece em `handoff.md:8` e `ENTREGA.md:17` com um valor diferente do da spec (veja Contradições).

## Riscos não bloqueantes

- **Nome do revisor no Git:** CA-03 manda copiar `approvals.json` e `final-manifest.json` sem alteração (`spec.md:101`). O script grava o campo `reviewer` (`prepare-images.mjs:135,156`), e o manifesto final traz `reviewedBy` (`prepare-images.mjs:193`). Ou seja, o nome do revisor vai ser versionado. Convém confirmar que isso é aceitável.
- **Colisão na primeira aplicação:** se a produção já tiver um TI-00xx divergente, a V4 aborta o `flyway migrate` dessa versão (`spec.md:29-33`; `plan.md:19-20`). A transação é revertida, mas a carga fica travada até uma decisão manual.
- **Variável lida no build:** `next.config.ts` lê `EQUIPMENT_IMAGE_BASE_URL` durante o build (`frontend/next.config.ts:3`). Mudar o valor exige novo deploy do front, como já acontece com `INTERNAL_API_BASE_URL` (`README.md:117-120`).
- **Briefing fora do repositório:** o "briefing de dados iniciais da prática" (`spec.md:11`) continua sem versão no repositório.
- **Nomes repetidos:** TI-0002 e TI-0004 têm o mesmo nome (`equipment.json:3,5`).
- **Dependências do CA-05:** o CA-05 depende de a API pública funcionar. Hoje ela retorna 404 (`ENTREGA.md:19`).
- **Fotos pendentes:** só 4 das 12 fotos estão aprovadas (`ENTREGA.md:16`).

## Perguntas para o operador

Nenhuma pergunta de negócio pendente, o que confere com `spec.md:106-108`. Há duas confirmações de procedimento para a reconfirmação do plano:

1. A tarefa da `EQUIPMENT_IMAGE_BASE_URL` no backend e em `.env.example` fica na Spec 003 ou na 004?
2. O nome do revisor pode ir ao Git na cópia de `approvals.json` e `final-manifest.json`?

## Comparação entre rodadas

| Item da rodada 01 | Situação na rodada 02 | Evidência |
| --- | --- | --- |
| B-01 (estado citava `pre-approval-03`/`e0cbaca` inexistente) | **Resolvido na spec.** O cabeçalho agora diz que a spec está em revisão, reconhece o erro anterior, exige renovar a aprovação humana e reconfirmar `plan.md` e `tasks.md`. Continua em aberto fora da spec: `handoff.md:8` e `ENTREGA.md:17,46`. | `spec.md:3-9` |
| B-02 (ambiente e mecanismo da repetição do CA-02) | **Resolvido.** O teste reexecuta a V4 lida do classpath, em transação única, no Testcontainers. Em produção, o operador roda `psql --single-transaction -v ON_ERROR_STOP=1 -f` sobre o arquivo versionado, que não tem `BEGIN/COMMIT/ROLLBACK`. O checksum fica preservado e `flyway migrate` sozinho não conta como repetição. Há três colisões mínimas e o resultado esperado é definido. Reflete a decisão (a). | `spec.md:56-59,76-87,100` |
| Q3 (prefixo com exatamente 12 objetos) | Resolvido: "exatamente as 12 chaves… sem outro objeto". | `spec.md:102` |
| Q4 (onde fica o manifesto e a aprovação visual) | Resolvido: `verification/manifesto/`, sem chave de API. | `spec.md:101` |
| Q5 (`EQUIPMENT_IMAGE_BASE_URL` no front e em `.env.example`) | Resolvido na spec: a variável é contrato do backend e do front, e a spec exige sua inclusão em `.env.example`. A implementação está pendente. Reflete a decisão (d). | `spec.md:61-74` |
| Q6 (CHECK e unicidade da chave) | Resolvido: sem CHECK, sem unicidade, coluna anulável e `varchar(255)`. O DDL ainda depende de aprovação específica. Reflete a decisão (d). | `spec.md:51-55`; `plan.md:9` |
| Onde fica a evidência de CA-01 a CA-05 | Resolvido com a nova coluna da tabela de CA. Reflete a decisão (c). | `spec.md:97-104` |
| Seções do modelo e "Perguntas em aberto" | Resolvido: "Contrato afetado", "Escopo" e "Perguntas em aberto" agora existem. | `spec.md:47,89-93,106-108` |
| Lista mutável | Resolvido: lista fixada em `8052d1b`, e qualquer mudança exige nova versão da spec. | `spec.md:23-28` |
| Duplicação CA-05 (003) e CA-06 (004) | Persiste. Não bloqueia. | `spec.md:103`; `004…/spec.md:134` |

## Conferido

- `AGENTS.md`, `.codex/agents/spec-validator.toml`, `rules/restrictions.md`, `rules/spec-flow.md`, `rules/checks.md`, `rules/migration.md` e `rules/secrets.md`.
- `docs/PRD.md` e `docs/adr/001-stack.md:71`.
- Spec 003: `spec.md` com o `git diff` local, `plan.md`, `tasks.md` e `verification/pre-approval-01-94a8197.md`.
- `docs/specs/002-modelo-dados/spec.md:38`, `docs/specs/001-login/spec.md:82-131` (provisionamento) e `docs/specs/004-catalogo-emprestimos/spec.md:15-60,105-134`.
- Migrations `V1__andar_zero.sql` e `V2__modelo_inicial.sql`. A V3 e a V4 ainda não existem.
- `.agents/skills/imagens-equipamentos/SKILL.md`, `references/equipment.json` (histórico Git e diff contra `8052d1b`) e `scripts/prepare-images.mjs` (nomes de arquivo e campo `reviewer`).
- `.env.example`, `README.md:105-120`, `frontend/next.config.ts`, `ENTREGA.md:10-46` e `handoff.md:8`.
- Busca por `EQUIPMENT_IMAGE`, `image_object_key` e `imageUrl` em `backend/src`, `frontend/lib` e `frontend/components`.

Trabalhei somente em leitura. Nenhum arquivo, banco, estado do Git ou serviço remoto foi alterado.
