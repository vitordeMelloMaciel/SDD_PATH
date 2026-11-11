# Rodada pre-aprovacao 06

- Base: `94a8197` com alteracoes locais na Spec 004 e no ADR-002
- Executor: instrucoes do `spec-validator` executadas por subagente Claude Code em contexto limpo (Codex CLI indisponivel nesta maquina)
- Sandbox: somente leitura, por instrucao
- Data: 2026-11-11

## Saida do verificador

# Resultado: APROVÁVEL

**Rodada:** pré-aprovação 06 (Spec 004)
**Estado avaliado:** commit base `94a8197` com worktree sujo. A Spec 004 e o ADR-002 têm alterações locais não commitadas (`git diff docs/specs/004-catalogo-emprestimos/spec.md docs/adr/002-design-system.md`). Também há alterações em `ENTREGA.md` e na Spec 003. Os arquivos de implementação não rastreados não foram avaliados como entrega; só fiz um grep para registrar um risco.
**Severidade geral:** baixa. Nenhum bloqueio. Ficam inconsistências documentais fora da spec (ENTREGA.md e handoff.md) e uma lacuna de variante visual no ADR-002, que precisa ser resolvida antes de implementar o card.
**Aprovação humana:** ainda necessária

## Bloqueios

Nenhum.

## Critérios não observáveis

- **Destino pós-login de `operacoes`** (`docs/specs/004-catalogo-emprestimos/spec.md:100-101`): o contrato diz que `operacoes` é levado a `/catalogo` após o login. O CA-01 (`spec.md:129`) prova o redirecionamento só para a "conta de teste" (colaborador). O CA-05 (`spec.md:133`) prova o estado "Acesso negado" só por acesso direto às páginas. Nenhum CA prova o caminho login → `/catalogo` → acesso negado para `operacoes`. Não bloqueia: basta acrescentar essa frase ao CA-05.
- **Rodapé do card em manutenção** (`spec.md:109`: "rodapé informa a situação"; `docs/adr/002-design-system.md:246`: "situação da assistência"): não há texto fixado. O CA-01 (`spec.md:129`) confere "situação correta" e a ausência de ação, mas não diz qual texto aparece. A ausência de ação é observável; o texto não é. Não bloqueia.
- **Status HTTP da página de acesso negado** (`spec.md:97-99`): o texto agora está fixado, o que basta para distinguir de uma página vazia ou de um erro. O status HTTP da página continua sem definição. Isso é aceitável, porque a evidência do CA-05 é textual.

Os três critérios apontados na rodada 05 foram resolvidos (ver Comparação entre rodadas).

## Contradições

| Ponto | Evidência | Classificação |
| --- | --- | --- |
| Card emprestado: spec × ADR-002 | Spec `:110-113` ("somente 'Emprestado', para todas as contas, inclusive a do titular"); ADR-002 `:245` (mesmo texto, com a exceção datada de 2026-11-11) e `:436` (linha na tabela de contradições auditadas, citando PRD `:12` e Spec 004 CA-07) | **Resolvida.** Spec e ADR estão alinhados e refletem a decisão (a) do operador. |
| Prazo de 14 dias: spec × PRD | PRD `:20` ("14 dias", sem qualificar); spec `:36-39` diz que "o PRD diz apenas '14 dias', e o operador decidiu pela contagem corrida, coerente com a Spec 002 e com a V2"; V2 `:70` (`loaned_at + interval '14 days'`); Spec 002 `:22`; ENTREGA `:44` (seção 1b, com alternativas e motivo) | **Resolvida.** A atribuição agora é à decisão do operador, não ao PRD, conforme a decisão (b). A linha de ENTREGA seção 1 que atribuía "corridos" ao PRD foi removida (ver `git diff ENTREGA.md`). |
| Limite de 3: spec × PRD × V2 | Spec `:36`, PRD `:19`, Spec 002 `:20`, V2 `:99`, ENTREGA `:33` | Coerente. Reflete a decisão (c). |
| ENTREGA.md × spec e ADR-002 (privacidade) | ENTREGA `:47` diz: "Implementação local usa a opção mais restrita. O titular vê 'Emprestado a você' e a própria data… A decisão formal do ADR segue pendente." Isso contraria a spec `:110-113`, o ADR-002 `:245` e `:436`. | **Inconsistência fora da spec**, não bloqueia a spec (`rules/restrictions.md:41`: a spec prevalece). Precisa ser corrigida no ENTREGA.md antes da entrega. |

## Inconsistências não bloqueantes

- **Foto no EquipmentCard sem variante no ADR-002.** Já apontada na rodada 05 e não tratada. O ADR-002 descreve o card com "Ícone de 40px" (`docs/adr/002-design-system.md:242`). Só o token `--gradient-equipment` prevê uma "Área de imagem do equipamento" (`:112`). A spec cria o contêiner 4:3, o `object-fit: cover` e o estado "Foto indisponível" (`spec.md:105-107`). O ADR-002 `:414` exige que "Uma exceção visual precisa virar variante explícita antes de entrar em uma página". A spec não contraria o ADR, porque não remove o ícone e o token já antecipa uma área de imagem. Mas a variante precisa ser registrada no ADR-002 antes da tarefa que implementar o card.
- **ADR-002 `:437` desatualizado.** O ADR diz que "Manutenção 'fora do catálogo'" é "Não resolvido aqui… precisa ser fechado na spec do catálogo". A Spec 004 fecha esse ponto (`spec.md:27-28`, `:109`; coerente com PRD `:22`), mas a linha do ADR não aponta para a Spec 004.
- **ADR-002 passa a fixar texto de página.** O ADR-002 `:443` diz que não decide "Textos específicos de cada página", mas `:245` agora fixa "Emprestado". O ADR já fazia isso com "Solicitar →" (`:244`), então a tensão é pequena, mas existe.
- **Seção "Perguntas em aberto" ausente.** A spec não tem essa seção, nem com "Nenhuma", ao contrário da Spec 003 (`docs/specs/003-dados-iniciais/spec.md:161-163`) e da Spec 001 (`:133`). O cabeçalho (`spec.md:3-6`) e a ausência de seção de decisão aberta indicam que não há pendência, mas convém declarar isso.
- **ENTREGA.md `:33`.** A coluna de evidência do limite de 3 ainda diz "Spec de emprestimo ainda pendente" / "pendente". A spec já registra a decisão (`spec.md:36`, `:43-44`).
- **ENTREGA.md `:48`** cita `LoanService.java` como evidência dos bloqueios. Esse arquivo não está rastreado e, por decisão do operador, fica fora da main.
- **handoff.md `:9`** ainda diz que "A spec registra a proposta de mostrar a devolução apenas ao titular". Isso está desatualizado frente à decisão (a).
- **Estado da Spec 003.** A Spec 004 exige a Spec 003 implementada para CA-01 e CA-06 (`spec.md:21-22`). Mas o cabeçalho da Spec 003 (`docs/specs/003-dados-iniciais/spec.md:3-9`) diz "Em revisão de pré-aprovação… retornou `BLOQUEADA`", enquanto `ENTREGA.md:17` e `handoff.md:8` dizem "Spec 003 aprovada". O pré-requisito está declarado; a divergência é de estado entre documentos.

## Duplicações

- A regra de limite, prazo e atraso aparece no PRD (`:19-21`), na Spec 002 (`:20-22`) e na Spec 004 (`:36-39`). É aceitável, porque a 004 atribui as fontes de forma explícita.
- A decisão de privacidade está em três lugares: na spec (`:110-113`), no ADR-002 (`:245` e `:436`) e no ENTREGA (`:47`). O ENTREGA já diverge (ver Contradições), que é exatamente o risco apontado na rodada 05.
- O CA-06 (`spec.md:134`: "zero empréstimos abertos ou encerrados para o professor") repete o CA-05 da Spec 003 (`docs/specs/003-dados-iniciais/spec.md:158`). É aceitável como conferência final, mas a evidência deve ser a mesma consulta, para não divergir.
- O prazo corrido aparece na spec (`:36-39`, `:43-44`) e no ENTREGA (`:44`), com redação compatível.

## Riscos não bloqueantes

- **Código local diverge da spec.** `frontend/components/loan-journey.tsx:131` renderiza "Emprestado a você · Devolução …". Isso contraria a spec `:110-113` e vai reprovar o CA-07 na verificação. O arquivo não está rastreado e fica fora da main até o plano ser aprovado (decisão do operador). Continua valendo o risco de processo da rodada 05: código antes de spec e plano aprovados (`rules/restrictions.md:22,26`).
- **Conflito inesperado sem resposta definida.** Se a trigger ou `uq_loan_open_equipment` (`V2__modelo_inicial.sql:48-50`, `:89-111`) rejeitar a inserção mesmo após a leitura sob bloqueio, a spec não define status nem código (`spec.md:78-90`). Convém o plano tratar isso, sem depender de texto de exceção (`spec.md:89-90`).
- **Empréstimos permanentes em produção.** O risco já está registrado em `spec.md:139-141`. Com 3 itens em manutenção (`docs/specs/003-dados-iniciais/spec.md:80-81`) e até 3 emprestados pela conta de teste no CA-06, restam pelo menos 6 disponíveis na correção. Não há reversão sem devolução (fora de escopo, `spec.md:143`) nem SQL avulso (`rules/restrictions.md:34`).
- **`loaned_at` é o início da transação.** `current_timestamp` (`V2:35`, `:108`) reflete o momento antes da espera pelo bloqueio. O efeito é desprezível, mas convém lembrar no plano.

## Perguntas para o operador

1. O card com foto 4:3, `object-fit: cover` e o estado "Foto indisponível" (`spec.md:105-107`) deve virar variante explícita do EquipmentCard no ADR-002 (`:242`, `:414`) antes da tarefa de implementação? Esta pergunta já estava na rodada 05 e segue sem resposta.
2. Qual é o texto do rodapé do card em manutenção (`spec.md:109`; ADR-002 `:246`)? Ou ele pode ficar a cargo de `docs/layout.md`?
3. Deve-se acrescentar ao CA-05 a prova de que `operacoes`, após o login, chega a `/catalogo` e vê o estado "Acesso negado" (`spec.md:100-101`)?

## Comparação entre rodadas

| Item da rodada 05 | Situação na rodada 06 |
| --- | --- |
| **B-01**: card emprestado na spec × ADR-002 `:245` | **Resolvido.** O ADR-002 `:245` e `:436` registram a exceção decidida pelo operador em 2026-11-11. A spec `:110-113` traz o mesmo comportamento e remete ao ADR. A seção "Decisão em aberto" foi removida. |
| **B-02**: sem contrato nem CA de privacidade | **Resolvido.** `spec.md:48-54` fixa "exatamente" os seis campos para qualquer conta e situação e lista os dados proibidos. O CA-07 (`spec.md:135`) compara as contas A e B, exige os seis campos e a ausência de e-mail, nome, IDs, `loaned_at` e `due_at`, e confere o card no navegador. O Contrato afetado (`spec.md:122-123`) aloca o CA-07 a testes HTTP e ao navegador. |
| **B-03**: cabeçalho "confirmado" × decisão aberta | **Resolvido.** O cabeçalho `spec.md:3-6` declara "Em revisão de pré-aprovação" e aponta a rodada 05. Não há mais seção aberta. |
| Não observável: "proporção visual uniforme" (CA-01) | **Resolvido.** `spec.md:105-107` e CA-01 `:129` definem contêiner 4:3, `object-fit: cover` e o texto "Foto indisponível". |
| Não observável: acesso negado de `operacoes` | **Resolvido.** `spec.md:97-99` e CA-05 `:133` definem o título, o texto, a ausência de lista e a ausência de chamadas à API. O status HTTP da página segue indefinido, o que não bloqueia. |
| Não observável: destino pós-login | **Resolvido para o colaborador** (`spec.md:26`, CA-01 `:129`). O destino de `operacoes` está definido (`:100-101`), mas nenhum CA o prova. |
| Não observável: rodapé do card emprestado | **Resolvido** pelo CA-07. |
| Origem de "14 dias corridos" | **Resolvido.** Spec `:36-39` e ENTREGA `:44` (seção 1b) atribuem a contagem à decisão do operador. |
| `FORBIDDEN` sem definição | **Resolvido** (`spec.md:72-74`, CA-05 `:133`). |
| Seções Objetivo e Contrato afetado ausentes | **Resolvido** (`spec.md:11-15`, `:115-123`). "Perguntas em aberto" continua ausente. |
| Pré-requisito Spec 003 | **Resolvido** (`spec.md:17-22`). |
| Fora de escopo incompleto | **Resolvido** (`spec.md:143-145`: tela de Operações, busca e filtros). |
| Trigger como origem da mudança de status | **Resolvido** (`spec.md:33-36`). |
| Briefing (5 empréstimos e dias úteis) não registrado | **Resolvido** (`spec.md:43-44`). |
| Foto sem variante no ADR-002 | **Persiste** (pergunta 1). |
| ENTREGA × decisão de privacidade | **Persiste e agora contradiz o ADR**: `ENTREGA.md:47`. |
| Conflito inesperado do índice ou trigger | **Persiste** como risco. |
| Rodadas 01–04 sem registro | Sem mudança. A rodada 05 está versionada em `verification/pre-approval-05-94a8197.md`. |

## Conferido

Arquivos lidos:

- `AGENTS.md`
- `.codex/agents/spec-validator.toml`
- `rules/restrictions.md`, `rules/spec-flow.md`, `rules/checks.md`
- `docs/PRD.md`
- `docs/specs/004-catalogo-emprestimos/spec.md`, com o `git diff`
- `docs/specs/004-catalogo-emprestimos/verification/pre-approval-05-94a8197.md`
- `docs/adr/001-stack.md`
- `docs/adr/002-design-system.md`, com o `git diff`, os trechos `:1-12`, `:95-125`, `:225-300` e `:400-452`
- `docs/specs/001-login/spec.md`, `docs/specs/002-modelo-dados/spec.md`, `docs/specs/003-dados-iniciais/spec.md`
- `backend/src/main/resources/db/migration/V2__modelo_inicial.sql`, mais a listagem do diretório de migrations (só V1 e V2)
- `ENTREGA.md` `:1-80`, com o `git diff`
- `handoff.md`

O código de implementação não rastreado não foi avaliado. Só houve um grep por "Emprestado a você" para registrar o risco. Nada foi alterado: só houve leituras e comandos Git de consulta (`diff`, `diff --stat`), sem escrita em arquivo, banco, Git ou serviço remoto.
