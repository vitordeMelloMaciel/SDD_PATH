# Rodada pre-aprovacao 01

- Base: `94a8197` com worktree sujo (spec 003 sem alteracao local)
- Executor: instrucoes do `spec-validator` executadas por subagente Claude Code em contexto limpo (Codex CLI indisponivel nesta maquina)
- Sandbox: somente leitura, por instrucao
- Data: 2026-11-11

## Saida do verificador

# Resultado: BLOQUEADA

**Rodada:** pré-aprovação 01 (Spec 003)
**Severidade geral:** alta. O estado declarado na spec não tem relatório que o comprove, e o CA de repetição da carga deixa o ambiente e o procedimento em aberto.
**Aprovação humana:** ainda necessária
**Estado avaliado:** commit `94a8197` com alterações locais não commitadas (`git status` do início da sessão). Nenhum arquivo dentro de `docs/specs/003-dados-iniciais/` aparece como modificado.

## Bloqueios

| ID | Severidade | Arquivo:linha | Evidência |
| --- | --- | --- | --- |
| B-01 | Alta | `docs/specs/003-dados-iniciais/spec.md:3-4`; `handoff.md:8`; `ENTREGA.md:17` | A spec diz "`APROVAVEL` na rodada `pre-approval-03`, base `e0cbaca`". Não há relatório para isso: a pasta `docs/specs/003-dados-iniciais/` contém só `spec.md`, `plan.md` e `tasks.md`, sem `verification/`. O único `pre-approval-03` do repositório é da Spec 001 (`docs/specs/001-login/verification/pre-approval-03-c485d8a.md`; `ENTREGA.md:86-92`). A base também não pode estar certa: `e0cbaca` é ancestral de `8052d1b`, o commit que criou a spec. O `git log` de `docs/specs/003-dados-iniciais` mostra só `8052d1b` (A spec.md) e `62e5768` (A plan.md, A tasks.md). Ou seja, a spec não existia na base citada. O fluxo exige `APROVAVEL` antes do `plan.md` (`rules/spec-flow.md:16-17,24`; `AGENTS.md:30`), mas `plan.md` e `tasks.md` já foram escritos (`62e5768`). |
| B-02 | Média | `docs/specs/003-dados-iniciais/spec.md:46,54-57`; `plan.md:22-23` | O CA-02 pede "Repetir o SQL da carga em PostgreSQL real", e a spec diz que "o teste e o operador executam esse mesmo arquivo em transação única". Três pontos ficam em aberto. (a) Qual ambiente conta como "PostgreSQL real": Testcontainers, que o plano cita como "mesma base" descartável (`plan.md:22`), ou produção. (b) Como o arquivo é reexecutado fora do Flyway: comando, transação externa ou proibição de `BEGIN/COMMIT` no próprio arquivo. O Flyway já envolve cada migration PostgreSQL numa transação, e um `BEGIN/COMMIT` explícito dentro de V4 conflitaria com isso. (c) Se o operador vai reexecutar SQL fora do Flyway em produção. Esse último caso esbarra em `rules/restrictions.md:34` ("nunca use SQL avulso") e `rules/migration.md:25-27`, e o `README.md:108-110` desliga o Flyway no serviço. Reexecutar o mesmo arquivo sem editá-lo não mexe no checksum do `flyway_schema_history`, então a ideia é compatível com o Flyway. Falta a spec dizer o ambiente, o mecanismo e quem executa, para o critério poder ser reproduzido. |

## Critérios não observáveis

- **CA-02** (`spec.md:46`): veja B-02. A spec não diz em que ambiente a repetição acontece nem como o arquivo é invocado. "Colisões de nome, categoria ou chave" também não define os casos mínimos: um por atributo, em item novo ou preexistente.
- **CA-04** (`spec.md:48`): "nenhum objeto adicional da carga" não diz como separar um objeto "da carga" de outro objeto no mesmo prefixo. Não fica claro se o prefixo `equipment/` deve ter exatamente 12 objetos.
- **CA-03** (`spec.md:47`): "aprovação visual registrada" não diz onde o registro fica versionado. A skill grava aprovações em `.local/pixabay-equipment/approvals.json` (`.agents/skills/imagens-equipamentos/SKILL.md:24`), um diretório local, e a spec não diz se o manifesto final vai para o Git.
- **CA-05** (`spec.md:49`): "No momento da entrega para correção" não diz onde fica a evidência da consulta (arquivo, `ENTREGA.md` ou `verification/`) nem como ela é registrada com data e hora.

## Contradições

- **`EQUIPMENT_IMAGE_BASE_URL`:** a spec diz que "O backend usará `EQUIPMENT_IMAGE_BASE_URL`" (`spec.md:27-29`). O `README.md:113-114` manda configurar a variável "no backend e no frontend", e o worktree já a lê no front (`frontend/next.config.ts:3,10`, arquivo modificado e não commitado). A variável também não está em `.env.example` (linhas 1-29), embora `rules/secrets.md:16` exija isso.
- **Estado da Spec 003 entre documentos:** `ENTREGA.md:46` ainda chama a decisão de carga de "Proposta na Spec 003" e "Rascunho". `ENTREGA.md:17` e `handoff.md:8` dizem que a spec foi aprovada. As duas versões não batem, e nenhuma tem relatório (B-01).
- Não achei contradição com o PRD, o ADR-001, a Spec 002 ou as migrations V1/V2. A carga só insere `disponivel` ou `manutencao`, valores aceitos pelo `ck_equipment_status` (`V2__modelo_inicial.sql:27-28`). A unicidade de `asset_code` (`V2__modelo_inicial.sql:26`) sustenta "patrimônio como chave" (`spec.md:22`). A Spec 002 tirou seed do escopo (`002-modelo-dados/spec.md:38`) e o ADR-001 não decide seed (`001-stack.md:71`), então a 003 pode tratar disso. A Spec 001 entrega a conta do professor à 003 (`001-login/spec.md:112`).

## Inconsistências não bloqueantes

- **Coluna da imagem:** a spec não nomeia a coluna, o tipo, a nulidade nem as versões V3/V4. Ela só diz "Flyway estende o modelo de equipamento" (`spec.md:54`) e "chave relativa no banco" (`spec.md:27`). Quem define `image_object_key varchar(255)` anulável é o plano (`plan.md:9,12`). O conteúdo é coerente: o plano deixa a coluna anulável e a spec permite equipamento anterior sem foto (`spec.md:29-30`). Mesmo assim, a Spec 004 consome esse contrato (`004-catalogo-emprestimos/spec.md:26-29`, `imageUrl`). Nenhum dos dois documentos define CHECK de formato (`equipment/<patrimônio>.jpg`) nem unicidade da chave.
- **Seções do modelo:** "Fora de escopo" e "Contrato afetado" estão juntos em "Contratos e escopo" (`spec.md:52-60`), e "Perguntas em aberto" virou "Pendências" (`spec.md:62-66`), que só lista ações operacionais. A spec não afirma que não há pergunta de negócio pendente.
- **Status e patrimônios estão consistentes com a fonte:** `spec.md:18-21` diz 4 notebooks, 3 monitores, 3 cabos e 2 câmeras, com TI-0004, TI-0007 e TI-0010 em manutenção e os outros nove disponíveis. Isso confere com `.agents/skills/imagens-equipamentos/references/equipment.json:2-13`: manutenção nas linhas 5, 8 e 11, e categorias `notebook`, `monitor`, `cabo` e `camera`. A decisão está registrada em `ENTREGA.md:36`. A justificativa de status aponta para `ENTREGA.md` (`spec.md:20`), mas o próprio JSON já traz o campo `status`.

## Duplicações

- O CA-05 da 003 (zero empréstimos da conta de avaliação, `spec.md:49`) se repete no CA-06 da Spec 004 (`004-catalogo-emprestimos/spec.md:88`).
- A afirmação de aprovação da 003 aparece em três lugares (`spec.md:3-4`, `handoff.md:8`, `ENTREGA.md:17`), e nenhum aponta um relatório.

## Riscos não bloqueantes

- TI-0002 e TI-0004 têm o mesmo nome, "Notebook 15 polegadas" (`equipment.json:3,5`). O catálogo vai mostrar dois itens iguais, e só o patrimônio os diferencia.
- Se a produção já tiver um TI-00xx divergente, a V4 aborta (`plan.md:19-20`). Isso trava o `flyway migrate` daquela versão até uma decisão manual. No PostgreSQL a transação volta atrás, mas a aplicação da carga fica bloqueada.
- A fonte da lista (`equipment.json`) é um arquivo mutável da skill. A spec não fixa um commit nem um hash dela, então o CA-01 pode mudar se o JSON for editado.
- O "briefing de dados iniciais da prática" (`spec.md:6`) não está versionado no repositório.
- O CA-05 depende de login no deploy, mas a API pública ainda responde 404 (`handoff.md:7`; `ENTREGA.md:101`).
- Nada foi aplicado no Supabase até agora (`ENTREGA.md:108`), e só 4 das 12 fotos estão aprovadas (`ENTREGA.md:16,70-75`).

## Perguntas para o operador

1. Houve de fato uma rodada de pré-aprovação da Spec 003? Se não houve, o estado em `spec.md:3` precisa ser corrigido, e a aprovação humana tem que ser renovada depois desta rodada.
2. A repetição do CA-02 é exigida só em PostgreSQL descartável (Testcontainers) ou também em produção pelo operador? Se for em produção, qual mecanismo é autorizado, sabendo que `rules/restrictions.md:34` proíbe SQL avulso?
3. No CA-04, o prefixo `equipment/` deve ter exatamente 12 objetos, ou outros objetos são permitidos?
4. Onde ficam versionados o manifesto final e o registro de aprovação visual do CA-03?
5. `EQUIPMENT_IMAGE_BASE_URL` também deve ser contrato do frontend, como dizem o README e o `next.config.ts`? Ela vai entrar em `.env.example`?
6. A chave de imagem deve ter CHECK de formato ou unicidade no banco? Isso define o DDL da V3, que ainda aguarda autorização (`handoff.md:8`).

## Conferido

- `AGENTS.md`, `.codex/agents/spec-validator.toml`, `rules/restrictions.md`, `rules/spec-flow.md`, `rules/checks.md`, `rules/migration.md`, `rules/secrets.md`
- `docs/PRD.md`, `docs/adr/001-stack.md`
- `docs/specs/003-dados-iniciais/spec.md`, `plan.md`, `tasks.md`
- `docs/specs/002-modelo-dados/spec.md`, `docs/specs/001-login/spec.md` (trechos de provisionamento), `docs/specs/004-catalogo-emprestimos/spec.md` (trechos de imagem e CA-06)
- `backend/src/main/resources/db/migration/V1__andar_zero.sql`, `V2__modelo_inicial.sql`
- `.agents/skills/imagens-equipamentos/SKILL.md`, `.agents/skills/imagens-equipamentos/references/equipment.json`
- `ENTREGA.md`, `handoff.md`, `README.md:105-120`, `.env.example`, `frontend/next.config.ts`
- Histórico Git de `docs/specs/003-dados-iniciais/` (commits `8052d1b`, `62e5768`) e ancestralidade de `e0cbaca`; listagem de `docs/specs/*/` e de `docs/specs/001-login/verification/`

Trabalhei somente em leitura. Nenhum arquivo, banco, estado do Git ou serviço remoto foi alterado.
