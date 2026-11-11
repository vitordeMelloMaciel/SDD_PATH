# Rodada pre-aprovacao 07

- Base: `8d40630` com alteracoes locais na Spec 004 e no ADR-002
- Executor: instrucoes do `spec-validator` executadas por subagente Claude Code em contexto limpo (Codex CLI indisponivel nesta maquina)
- Sandbox: somente leitura, por instrucao
- Data: 2026-11-11

## Saida do verificador

# Resultado: APROVÁVEL

**Rodada:** pré-aprovação 07 (Spec 004)
**Estado avaliado:** commit base `8d40630` com alterações locais. Diffs da rodada: `docs/specs/004-catalogo-emprestimos/spec.md`, `docs/adr/002-design-system.md` e `docs/specs/003-dados-iniciais/spec.md`/`tasks.md`. Também há alterações rastreadas em `ENTREGA.md`, `README.md`, `frontend/next.config.ts`, `frontend/components/login-page.tsx` e em dois arquivos de auth do backend, além de código não rastreado da 004. Avaliei a spec, não o código. Do código, só consultei `git diff frontend/next.config.ts` e um grep por `EQUIPMENT_IMAGE_BASE_URL`, para registrar um risco.
**Severidade geral:** baixa. As quatro decisões do operador estão refletidas de forma coerente e verificável. As mudanças não introduziram bloqueio. Ficam inconsistências documentais fora da spec e uma lacuna de alocação de evidência no "Contrato afetado".
**Aprovação humana:** ainda necessária

## Bloqueios

Nenhum.

## Critérios não observáveis

- **Status HTTP da página de acesso negado** (`spec.md:99-103`): o status continua sem definição. Não bloqueia, porque a evidência do CA-05 (`spec.md:137`) é textual. Herdado da rodada 06.

Os outros dois pontos da rodada 06 foram resolvidos:
- Rodapé do card em manutenção: agora é fixo, "Em manutenção" (`spec.md:112-113`), e é provado no CA-01 (`spec.md:133`).
- Destino pós-login de `operacoes`: agora é provado no CA-05 (`spec.md:137`).

## Contradições

| Ponto | Evidência | Classificação |
| --- | --- | --- |
| Card com foto: spec × ADR-002 | A spec diz que o contêiner tem proporção 4:3, usa `object-fit: cover` e mostra "Foto indisponível", e que "Essa é a variante com foto do EquipmentCard registrada no ADR-002" (`spec.md:107-110`). O ADR-002 traz a "Variante com foto", que entra no lugar do ícone de 40px com fundo `--gradient-equipment`, datada de 2026-11-11 (`docs/adr/002-design-system.md:247`). Isso atende `:415` ("exceção visual precisa virar variante explícita") e usa o token já previsto em `:112`. | **Resolvida.** Reflete a decisão (a). A spec não repete o gradiente, mas remete ao ADR, então não há divergência. |
| Rodapé de manutenção: spec × ADR-002 | Spec `:112-113` e CA-01 `:133`; ADR-002 `:246` (mesmo texto, sem previsão de retorno, com data da decisão) e `:438` (linha da auditoria atualizada para "Fechado na Spec 004"). | **Resolvida.** Reflete a decisão (b). Fecha também a inconsistência da rodada 06 sobre o ADR `:437` desatualizado. |
| Manutenção no catálogo × PRD | PRD `docs/PRD.md:22` ("não aparece como disponível"); spec `:29-31` e `:112-113` (aparece sem ação de solicitar). | Coerente. |
| `operacoes` pós-login: spec × Spec 001 | A Spec 001 `:78` delega o destino inicial à spec seguinte. A spec 004 `:28` e `:102-103` define `/catalogo` para os dois papéis, e o CA-05 `:137` prova o caso de `operacoes`. | Coerente. Reflete a decisão (c). |
| `EQUIPMENT_IMAGE_BASE_URL`: Spec 004 × Spec 003 | A Spec 003 `:74-76` diz que a implementação no backend, no `next.config.ts` e no `.env.example` é tarefa dela e que "A Spec 004 só consome a URL pronta". O CA-04 da 003 (`:104`) prova a composição, o `null` e o `.env.example`, e `tasks.md:6` (T006) aloca a tarefa. A Spec 004 traz a variável como pré-requisito (`:23-24`), e o "Contrato afetado" (`:121-127`) não lista configuração própria. | **Coerente.** Reflete a decisão (d). A regra de composição (`spec.md:57-60`) repete a Spec 003 `:68-73` sem divergir (ver Duplicações). |
| Card emprestado, 14 dias corridos e limite de 3 | Spec `:38-41`, `:114-117`; ADR-002 `:245`, `:437`; PRD `:19-20`; V2 `:70`, `:99`; ENTREGA `:44-45`. | Coerente, sem mudança desde a rodada 06. |
| ENTREGA × privacidade (pendente na rodada 06) | `ENTREGA.md:45` agora diz "Somente 'Emprestado' para todas as contas, inclusive o titular". | **Resolvida** (commit `8d40630`). |

## Inconsistências não bloqueantes

- **"Contrato afetado" não aloca evidência de navegador ao CA-05.** `spec.md:126-127` aloca o CA-05 só a "integração HTTP com Testcontainers" e reserva o navegador para o CA-01 e o CA-07. Mas o CA-05 (`spec.md:137`) exige quatro provas que uma integração HTTP do backend não cobre: redirecionamento das páginas, texto "Acesso negado", ausência de chamadas à API e, agora, login de `operacoes` → `/catalogo`. A lacuna já existia em parte na rodada 06 e aumentou com a decisão (c). Convém acrescentar o CA-05 à conferência no navegador, ou definir o meio no `plan.md`.
- **O ADR-002 agora fixa mais textos de página.** O ADR diz que não decide "Textos específicos de cada página" (`docs/adr/002-design-system.md:444`), mas passa a fixar "Emprestado" (`:245`), "Em manutenção" (`:246`) e "Foto indisponível" (`:247`), além de "Solicitar →" (`:244`). As exceções estão datadas e atribuídas ao operador. A tensão cresceu, mas continua pequena.
- **O cabeçalho da Spec 004 não lista a decisão (d)** (`spec.md:5-8`), que aparece apenas no pré-requisito `:23-24`.
- **`ENTREGA.md:33-34`** ainda dizem "Spec de emprestimo/catalogo ainda pendente" na coluna de evidência, embora a Spec 004 já registre as decisões (`spec.md:38`, `:31`). Persiste desde a rodada 06.
- **`ENTREGA.md:50`** cita `LoanService.java` como evidência, mas o arquivo não está rastreado e fica fora da main. Persiste.
- **`ENTREGA.md:18` e `:116-120`** dizem que a API e as páginas da 004 "foram implementadas", enquanto a spec ainda está em pré-aprovação e sem `plan.md` (`spec.md:8`).
- **`ENTREGA.md:58-64`** ainda lista o contêiner 4:3 com `object-fit: cover` como detalhe "proposto pelo agente" que aguarda confirmação. A decisão (a) agora o registra como variante no ADR, e a lista não foi atualizada.
- **`handoff.md:9`** ainda fala em "proposta de mostrar a devolução apenas ao titular", contra a decisão já registrada. **`handoff.md:8`** diz "Spec 003 … aprovada pelo operador", mas o cabeçalho da Spec 003 (`docs/specs/003-dados-iniciais/spec.md:3-8`) diz que ela aguarda nova rodada e reaprovação. Persiste.
- **Pré-requisito em estado aberto.** A Spec 003 voltou a "aguarda nova rodada" depois das decisões de 2026-11-11 (`003/spec.md:4-6`), e a T006 entrou em `tasks.md` antes da reaprovação (`003/spec.md:9-10` reconhece isso). A Spec 004 depende dela para o CA-01 e o CA-06 (`spec.md:23-24`).

## Duplicações

- A composição de `imageUrl` e a liberação no Next.js aparecem na Spec 004 (`:57-60`) e na Spec 003 (`:68-73`). A redação é compatível. Como a 003 é dona da implementação, convém que a 004 apenas remeta a ela, para não divergir no futuro.
- Os textos de rodapé ("Emprestado", "Em manutenção") e "Foto indisponível" estão na spec (`:107-117`, `:133`) e no ADR-002 (`:245-247`, `:437-438`), com textos idênticos hoje.
- Limite, prazo e atraso aparecem no PRD `:19-21`, na Spec 002 `:20-22`, na Spec 004 `:38-41` e no ENTREGA `:44`. É aceitável, porque as fontes estão atribuídas.
- O CA-06 da 004 (`spec.md:138`, zero empréstimos para o professor) repete o CA-05 da 003 (`003/spec.md:105`). Convém usar a mesma consulta como evidência.

## Riscos não bloqueantes

- **Implementação da 003 antes da aprovação.** O `frontend/next.config.ts` rastreado já tem alteração local que lê `EQUIPMENT_IMAGE_BASE_URL` (diff, linhas 3 e 9-11). Isso pertence à T006 da Spec 003, que ainda não foi reaprovada (`rules/restrictions.md:26`). O `.env.example` ainda não declara a variável (o grep não encontrou nada). Também persiste o risco do código não rastreado da 004 antes do plano.
- **Liberação de origem no Next.js sem prova automatizada.** "Sem a variável, nenhuma origem remota é autorizada" (`003/spec.md:72-73`) não é provado explicitamente por nenhum CA da 003 ou da 004. O CA-01 da 004 só prova o caso positivo.
- **Conflito inesperado sem resposta definida.** Se a trigger ou `uq_loan_open_equipment` (`V2__modelo_inicial.sql:48-50`, `:89-111`) rejeitar a inserção mesmo depois da leitura sob bloqueio, a spec não define status nem código (`spec.md:80-92`). Persiste. Convém tratar no plano.
- **Empréstimos permanentes em produção** depois do CA-06 (`spec.md:147-149`). Persiste e está registrado.
- **`loaned_at` é o início da transação.** `current_timestamp` (`V2:35`, `:108`) é anterior à espera pelo bloqueio. O efeito é desprezível.

## Perguntas para o operador

Nenhuma pergunta de negócio nova. Sugestão de redação, sem decisão de produto: incluir o CA-05 entre os critérios conferidos no navegador em `spec.md:126-127`, ou deixar o meio de prova para o `plan.md`.

## Comparação entre rodadas

| Item da rodada 06 | Situação na rodada 07 |
| --- | --- |
| Pergunta 1 (card com foto como variante do ADR-002) | **Respondida e incorporada**: ADR-002 `:247`, spec `:110`. |
| Pergunta 2 (texto do rodapé de manutenção) | **Respondida e incorporada**: "Em manutenção", sem previsão (spec `:112-113`, CA-01 `:133`, ADR `:246`). |
| Pergunta 3 (CA-05 com login de `operacoes`) | **Respondida e incorporada**: CA-05 `:137`. Gerou a lacuna de alocação de evidência em `:126-127`, que não bloqueia. |
| Não observável: destino pós-login de `operacoes` | **Resolvido** (CA-05 `:137`). |
| Não observável: rodapé de manutenção | **Resolvido** (CA-01 `:133`). |
| Não observável: status HTTP da página de acesso negado | **Persiste**, sem bloquear. |
| Foto sem variante no ADR-002 | **Resolvido** (ADR `:247`). |
| ADR-002 `:437` (manutenção "não resolvido") | **Resolvido** (ADR `:438`). |
| Seção "Perguntas em aberto" ausente | **Resolvido** (`spec.md:141-143`). |
| ENTREGA × privacidade | **Resolvido** (`ENTREGA.md:45`). |
| ENTREGA `:33` pendente | **Persiste** (`:33-34`). |
| ENTREGA cita `LoanService.java` | **Persiste** (`:50`). |
| handoff `:9` desatualizado | **Persiste**. |
| Divergência de estado da Spec 003 | **Mudou**: a 003 voltou a aguardar rodada depois da decisão (d). Persiste a divergência com `handoff.md:8`. |
| ADR-002 fixando texto de página | **Aumentou**, sem bloquear (`:245-247`). |
| Risco de conflito inesperado e `loaned_at` | **Persiste**. |
| Novo nesta rodada | `next.config.ts` alterado antes da reaprovação da 003. A decisão (d) introduziu duplicação na composição da URL. |

## Conferido

- `AGENTS.md`, `.codex/agents/spec-validator.toml`
- `rules/restrictions.md`, `rules/spec-flow.md`, `rules/checks.md`
- `docs/PRD.md`
- `docs/specs/004-catalogo-emprestimos/spec.md`, na íntegra e com o `git diff`
- `docs/specs/004-catalogo-emprestimos/verification/pre-approval-06-94a8197.md`
- `docs/adr/001-stack.md`
- `docs/adr/002-design-system.md`: `git diff` e trechos `:1-20`, `:95-130`, `:225-300`, `:400-453`
- `docs/layout.md`: grep por manutenção, foto e retorno, sem ocorrências
- `docs/specs/001-login/spec.md`, `docs/specs/002-modelo-dados/spec.md`
- `docs/specs/003-dados-iniciais/spec.md`, com o `git diff` e o diff de `tasks.md`
- `backend/src/main/resources/db/migration/V2__modelo_inicial.sql`
- `ENTREGA.md` `:1-123`, com o `git diff`; `README.md` (diff); `handoff.md`
- `git status --short`, `git log`, `git diff frontend/next.config.ts` e grep por `EQUIPMENT_IMAGE_BASE_URL` (para o risco)

Nada foi alterado. Só houve leituras e comandos Git de consulta, sem escrita em arquivo, banco, Git ou serviço remoto.
