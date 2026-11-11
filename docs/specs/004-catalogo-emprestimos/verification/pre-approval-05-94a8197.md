# Rodada pre-aprovacao 05

- Base: `94a8197` com worktree sujo (spec 004 sem alteracao local)
- Executor: instrucoes do `spec-validator` executadas por subagente Claude Code em contexto limpo (Codex CLI indisponivel nesta maquina)
- Sandbox: somente leitura, por instrucao
- Data: 2026-11-11

## Saida do verificador

# Resultado: BLOQUEADA

**Rodada:** pré-aprovação 05 (Spec 004). Não há relatório de rodada anterior da Spec 004 em `docs/specs/004-catalogo-emprestimos/`, que só contém `spec.md`. O `handoff.md:9` cita uma "revisão 04", então esta é a 05.
**Estado avaliado:** commit base `94a8197` com worktree sujo. A spec não tem alteração local: `git status --short` não lista `docs/specs/004-*`, e a última mudança nela é o commit `da756ec`.
**Severidade geral:** alta. Há uma discordância aberta entre a spec e o ADR-002, e a regra é parar e avisar.
**Aprovação humana:** ainda necessária

## Bloqueios

| ID | Severidade | Arquivo:linha | Evidência |
| --- | --- | --- | --- |
| B-01 | Alta | `docs/adr/002-design-system.md:245`; `docs/specs/004-catalogo-emprestimos/spec.md:71-77`; `rules/restrictions.md:13,42` | O ADR-002 diz: "Emprestado: sem hover; rodapé informa pessoa e devolução". A spec propõe outra coisa: "Emprestado a você" e a data só para o titular, e "Emprestado" para as demais contas. Ela mesma marca isso como "Decisão em aberto" e diz que a exceção só seria registrada no ADR-002 "se aprovada". Isso é uma spec discordando de um ADR. `restrictions.md:42` manda "pare e avise. Não escolha um dos dois", e `restrictions.md:13` proíbe contrariar o ADR sem parar. Enquanto o operador não decidir e o ADR-002 não for atualizado (ou a spec não for alinhada a ele), a spec não pode ser aprovada. Agravante: `ENTREGA.md:47` registra que a "Implementação local usa a opção mais restrita… A decisão formal do ADR segue pendente". Ou seja, uma das opções já foi escolhida no código. |
| B-02 | Alta | `docs/specs/004-catalogo-emprestimos/spec.md:24-27, 77, 83-88` | Faltam o contrato e o critério de privacidade do card emprestado. A spec diz que vai "fixar os campos da API e o teste de privacidade" depois (`:77`). Hoje `GET /api/equipment` só tem `id`, `assetCode`, `name`, `category`, `status` e `imageUrl` (`:26`). A spec não diz se a posse do empréstimo vem de um campo novo ou do cruzamento com `GET /api/loans/me` por `assetCode`. Nenhum CA (`:83-88`) cobre o rodapé do card emprestado nem prova que a resposta não traz pessoa ou data do empréstimo de outra conta. Seja qual for a decisão de B-01, o comportamento fica sem critério observável. |
| B-03 | Média | `docs/specs/004-catalogo-emprestimos/spec.md:3` × `:71` | Contradição interna de estado. O cabeçalho diz "Decisões confirmadas pelo operador", mas a spec tem uma seção "Decisão em aberto". Pela regra de `rules/spec-flow.md:15` (esperar as respostas antes de fechar a spec), ela não pode se apresentar como fechada. |

## Critérios não observáveis

- **CA-01** (`spec.md:83`): "proporção visual uniforme" não tem medida. Falta dizer o que conferir: proporção 4:3 do contêiner, `object-fit`, dimensões. As fotos são 800 × 600 (`docs/specs/003-dados-iniciais/spec.md:31`), mas a spec não fixa como o card as apresenta.
- **CA-05** (`spec.md:87`) e contrato (`:69`): "operacoes vê acesso negado" não fixa texto nem status HTTP da página. O teste não tem como diferenciar acesso negado de uma página vazia ou de um erro.
- **Jornada** (`spec.md:10`): "abre o catálogo após o login" define o destino depois do login, coisa que a Spec 001 deixou para outra spec (`docs/specs/001-login/spec.md:78`). Nenhum contrato ou CA da 004 prova esse redirecionamento, nem diz para onde vai a conta `operacoes` depois do login.
- **Rodapé do card emprestado**: sem critério nenhum (ver B-02).

## Contradições

| Ponto | Evidência | Classificação |
| --- | --- | --- |
| Rodapé do card emprestado | ADR-002 `:245` × spec `:71-77` | Bloqueio (B-01) |
| Estado "confirmado" × decisão aberta | spec `:3` × `:71` | Bloqueio (B-03) |
| Origem de "14 dias corridos" | spec `:17-18` diz "conforme PRD e migration V2". O PRD só diz "O prazo padrao de devolucao e de 14 dias" (`docs/PRD.md:20`), sem dizer se são corridos ou úteis. A Spec 002 diz "14 dias a partir da retirada" (`docs/specs/002-modelo-dados/spec.md:22`). A V2 faz `new.loaned_at + interval '14 days'` (`V2__modelo_inicial.sql:70`), que na prática é contagem corrida. `ENTREGA.md:34` diz que o "PRD e a migration V2 usam 14 dias corridos", o que é falso quanto ao PRD. | **Lacuna, não conflito.** O PRD é omisso, e há decisão do operador registrada ("Manter 14 dias corridos", `ENTREGA.md:34`) diante do briefing que pedia dias úteis. A spec e o ENTREGA.md devem atribuir "corridos" à decisão do operador, à Spec 002 e à V2, não ao PRD. Não bloqueia porque a decisão humana existe e a regra bate com a V2. |

## Inconsistências não bloqueantes

- **Limite de 3: coerente.** O PRD limita a 3 (`docs/PRD.md:19`), assim como a Spec 002 (`spec.md:20`), a spec 004 (`:17`, `:56`) e a V2 (`open_loan_count >= 3`, `V2__modelo_inicial.sql:99`). O briefing pedia 5, e o operador manteve 3 (`ENTREGA.md:33`). A spec cita o "briefing de solicitação" (`:5-6`) mas não registra os dois conflitos resolvidos (5 itens e dias úteis). A coluna de evidência do ENTREGA ainda diz "Spec de emprestimo ainda pendente" (`ENTREGA.md:33-34`).
- **Ordem dos bloqueios e trigger: coerentes.** A V2 bloqueia `app_user ... for update` (`:73-77`) e depois `equipment ... for update` (`:83-87`), e checa disponibilidade → limite → atraso (`:89-111`). A spec exige bloqueio usuário → equipamento na mesma transação (`:58-60`) e conflitos nessa ordem (`:53-57`). A ordem de aquisição é a mesma, o que evita deadlock. A V2 junta manutenção, emprestado e inexistente numa mensagem só ("equipment must be available", `:90`). A distinção vem da leitura sob bloqueio, e a spec já proíbe depender do texto da exceção (`:61-62`). A mudança de status é feita pela trigger `trg_sync_equipment_status_from_loan` (`V2:146-167`). A spec não exige UPDATE redundante, mas também não diz que a mudança "para emprestado" (`:16-17`) vem da trigger. Convém explicitar para o plano não duplicar o UPDATE.
- **Falta de seções obrigatórias.** O modo pré-aprovação pede objetivo, contrato afetado e perguntas em aberto (`.codex/agents/spec-validator.toml`). A spec não tem seção "Objetivo" (a "Jornada" faz esse papel) nem "Contrato afetado" com banco, backend, frontend e testes, como têm as Specs 001 (`:116-122`) e 002 (`:43-48`). Por exemplo, ela não diz se precisa de migration.
- **Pré-requisito não declarado.** `imageUrl` depende da chave da Spec 003 (`spec.md:28`), que pede uma extensão de schema. Hoje só existem `V1__andar_zero.sql` e `V2__modelo_inicial.sql`, e `ENTREGA.md:104-106` registra que a V3 e a V4 não foram gravadas. CA-01 e CA-06 ("12 fotos") dependem da Spec 003 implementada, e a spec não declara isso como pré-requisito.
- **Foto no EquipmentCard.** O ADR-002 descreve o card com um ícone de 40px (`:242`), sem área de foto nem estado "Foto indisponível". Só o token `--gradient-equipment` cita "Área de imagem do equipamento" (`:112`). `ADR-002:414` exige que "Uma exceção visual precisa virar variante explícita". A spec (`:29-31`) cria esse estado visual sem variante no ADR.
- **Fora de escopo incompleto.** Os itens excluídos estão em `spec.md:90-91`. Faltam a tela de Operações (PRD item 5, `docs/PRD.md:16`) e a busca e os filtros previstos em `docs/layout.md:14`.
- **Erro `FORBIDDEN`.** A spec usa o código `FORBIDDEN` (`:43,46`) "conforme o envelope da Spec 001", mas a Spec 001 não define esse código nem a mensagem (`docs/specs/001-login/spec.md:45-54`).
- **Manutenção no catálogo.** A spec decide que os itens em manutenção aparecem sem ação (`:12-13`). Isso fecha o ponto que o ADR-002 deixou para a spec (`:436`, `:444`) e respeita o PRD (`:22`). Está coerente.

## Duplicações

- As regras de limite, prazo e atraso estão no PRD (`:19-21`), na Spec 002 (`:20-22`) e na Spec 004 (`:17-19`). Isso é aceitável se a 004 só referenciar.
- A decisão de privacidade e a de bloqueios aparecem tanto na spec (`:58-62`, `:71-77`) quanto em `ENTREGA.md:47-48`, e o ENTREGA aponta `LoanService.java` como evidência. Há risco de as duas versões divergirem.

## Riscos não bloqueantes

- **Processo: código antes de spec e plano aprovados.** Existem arquivos não rastreados de implementação: `backend/src/main/java/br/org/germinare/equipamentos/loan/`, `frontend/app/catalogo/`, `frontend/app/meus-emprestimos/`, `frontend/components/loan-journey.tsx` e `frontend/lib/loan-api.ts`. Também há mudanças em `SecurityConfiguration.java`, `login-page.tsx` e `next.config.ts`. Não existe `plan.md` nem `tasks.md` na pasta da 004. Isso contraria `rules/restrictions.md:22,26` e `rules/spec-flow.md:17-18`. O próprio `ENTREGA.md:18,47-48,102-103` declara que a implementação existe. O código não foi avaliado nesta rodada.
- **Rodadas anteriores sem registro.** As revisões 01 a 04 da Spec 004 não estão versionadas. A Spec 001 versionou as suas em `docs/specs/001-login/verification/`. Sem esse registro, a comparação entre rodadas não tem como ser reproduzida.
- **CA-06 deixa a conta de teste com 3 itens emprestados em produção.** CA-06 (`spec.md:88`) pede provar a recusa ao atingir três empréstimos, mas a devolução está fora de escopo (`:90`). A conta de teste fica com 3 itens emprestados em produção. Com 3 itens em manutenção (`docs/specs/003-dados-iniciais/spec.md:19-21`), sobram 6 disponíveis para a correção, e não há como reverter sem SQL avulso (`rules/restrictions.md:34`).
- **Conflito inesperado.** A spec não diz como responder se a trigger ou o índice `uq_loan_open_equipment` (`V2:48-50`) rejeitar a inserção mesmo após a leitura sob bloqueio. Hoje esse caso não tem resposta prevista.
- **`loaned_at` e espera pelo bloqueio.** `loaned_at` e a checagem de atraso usam `current_timestamp` (`V2:35,108`), que é o início da transação. Se a transação esperar no bloqueio, `loaned_at` reflete o momento antes da espera. O efeito é desprezível, mas convém lembrar no plano.

## Perguntas para o operador

1. No card emprestado, vale o ADR-002 (`:245`: pessoa e devolução para todos) ou a proposta da Spec 004 (`:73-76`: só o titular vê "Emprestado a você" e a data)? Se for a proposta, o ADR-002 deve ser atualizado com essa exceção antes da aprovação?
2. Se a proposta for aceita, a posse vem de um campo novo em `GET /api/equipment` ou do cruzamento com `GET /api/loans/me`? Qual teste prova que dados de outra conta não aparecem?
3. O card com foto e o estado "Foto indisponível" precisam de variante explícita no ADR-002 (`:242`, `:414`)?
4. Qual é o texto (e o status) da página de acesso negado para `operacoes`, e para onde essa conta vai depois do login?
5. A conta de teste pode ficar com 3 itens emprestados em produção na correção (CA-06), já que não há devolução nesta versão?
6. Confirma que "14 dias corridos" deve ser atribuído à sua decisão (`ENTREGA.md:34`), à Spec 002 e à V2, e não ao PRD?

## Comparação entre rodadas

Não há relatórios das rodadas 01 a 04 da Spec 004. A comparação usa `handoff.md:9` e o diff do commit `da756ec`:

| Pedido da revisão 04 (`handoff.md:9`) | Situação na rodada 05 |
| --- | --- |
| Proteção das páginas | **Resolvido**: `spec.md:67-69` e CA-05 (`:87`) definem o redirecionamento para `/` sem sessão. Resta definir o texto e o status do acesso negado. |
| Invalidação verificável | **Resolvido**: CA-05 (`:87`) pede "consulta posterior com o cookie antigo retorna 401" nas três rotas, e `:47-49` cobre as três rotas com CSRF avaliado primeiro. |
| Rodapé do card compatível com o ADR-002 | **Persiste**: virou "Decisão em aberto" (`:71-77`), sem atualizar o ADR e sem CA (B-01, B-02). Surgiu ainda a contradição de estado B-03. |

## Conferido

Arquivos lidos:

- `AGENTS.md`
- `.codex/agents/spec-validator.toml`
- `rules/restrictions.md`, `rules/spec-flow.md`, `rules/checks.md`
- `docs/PRD.md`
- `docs/specs/004-catalogo-emprestimos/spec.md`
- `docs/adr/001-stack.md`, `docs/adr/002-design-system.md`
- `docs/specs/001-login/spec.md`, `docs/specs/002-modelo-dados/spec.md`, `docs/specs/003-dados-iniciais/spec.md`
- `backend/src/main/resources/db/migration/V2__modelo_inicial.sql`; `V1__andar_zero.sql` só foi listado, não lido
- `ENTREGA.md` (com `git diff`) e `handoff.md`
- `docs/perguntas-prd.md`, `docs/layout.md` (trechos)
- Histórico Git da spec 004 (`git log`, `git show da756ec`) e listagem de `docs/specs/*/verification/`

O código de implementação não rastreado não foi avaliado. Nada foi alterado: só houve leituras e comandos Git de consulta (`status`, `diff`, `log`, `show`), sem escrita em arquivo, banco, Git ou serviço remoto.
