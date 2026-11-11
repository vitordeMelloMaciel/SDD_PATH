# Rodada pre-aprovacao 02

- Base: `c485d8a` com alteracoes locais na Spec 001
- Executor: `spec-validator` via Codex CLI efemero
- Sandbox: `read-only`
- Data: 2026-11-10

## Saida do verificador

# Resultado: BLOQUEADA

**Rodada:** pré-aprovação 02
**Base:** `c485d8a`, com alteração local em `docs/specs/001-login/spec.md`
**Severidade geral:** ALTA

A Spec 001 corrigiu a maior parte dos bloqueios da rodada 01, mas ainda não permite derivar e verificar de forma inequívoca o contrato de sessão e a idempotência do logout.

## Bloqueios

| ID | Severidade | Arquivo e linha | Evidência |
|---|---|---|---|
| B-01 | ALTA | `docs/specs/001-login/spec.md:44,51-54,96` | O sucesso de `GET /api/auth/session` não possui formato JSON inequívoco. “O mesmo objeto `user` retornado no login” pode significar o envelope `{ "user": {...} }` ou somente o objeto interno `{ "id": ... }`. O CA-04 também exige apenas “identidade autenticada”. Assim, duas implementações incompatíveis poderiam alegar atendimento. |
| B-02 | ALTA | `docs/specs/001-login/spec.md:55,59-63,97` | A lista de rotas públicas menciona saúde, login e CSRF, mas não logout. Ao mesmo tempo, o contrato exige que logout sem sessão autenticada responda 204. A autorização da rota fica contraditória ou, no mínimo, não determinística para o plano e os testes. |
| B-03 | ALTA | `docs/specs/001-login/spec.md:36,59-61,97,102` | O CA-05 exige que repetir logout continue respondendo 204, enquanto o CA-10 exige 403 para token CSRF inválido. Não está definido se a repetição reutiliza o token anterior ou obtém novo token depois que a primeira chamada invalida a sessão. O resultado esperado da segunda requisição não é reproduzível sem essa informação. |

## Critérios não plenamente observáveis

- **CA-04:** o status e os cenários estão definidos, mas o corpo da resposta 200 permanece ambíguo.
- **CA-05:** invalidação e 204 estão definidos; a repetição não possui sequência CSRF determinística.
- **CA-10:** o comportamento isolado de CSRF é observável, mas entra em ambiguidade com a repetição exigida pelo CA-05.

Os demais critérios, CA-01 a CA-03, CA-06 a CA-09 e CA-11, possuem resultados observáveis suficientes nesta etapa documental.

## Contradições e inconsistências

- `spec.md:55` omite logout das rotas públicas, embora `spec.md:61,97` exija sucesso sem sessão.
- `spec.md:97` exige 204 na repetição do logout, enquanto `spec.md:102` exige 403 se o token reutilizado já não for válido; o ciclo do token não fecha qual cenário se aplica.
- Não foi encontrada contradição com o PRD, ADR-001, ADR-002 ou Spec 002.
- O uso das cores de erro em `spec.md:73` fecha a decisão que o ADR-002 deixava para a spec, sem criar cor nova.

## Duplicações

Não bloqueantes e atualmente consistentes:

- Atributos do cookie aparecem nas regras, CA-02 e CA-09 (`spec.md:23,94,101`).
- Sigilo de senha e hash aparece nas regras, provisionamento e CA-07 (`spec.md:20,88,99`).
- Idempotência do logout aparece nas regras, contrato e CA-05 (`spec.md:22,61-63,97`).
- Papel do usuário aparece nas regras, contrato HTTP e CA-06 (`spec.md:19,44,98`).
- Provisionamento aparece no contrato, CA-08, contrato afetado e decisões encerradas (`spec.md:79-89,100,117-125`).

## Riscos não bloqueantes

| Severidade | Arquivo e linha | Risco |
|---|---|---|
| ALTA | `docs/specs/001-login/spec.md:27,108,128` | Bloqueio temporário, CAPTCHA e rate limiting foram conscientemente excluídos, mantendo exposição a tentativas automatizadas. |
| MÉDIA | `docs/specs/001-login/spec.md:77,109-110` | O login cria e consulta a sessão, mas o destino autenticado e a tela de negócio ficam para specs futuras; a entrega não forma sozinha uma jornada navegável completa. |
| MÉDIA | `docs/specs/001-login/spec.md:101,112,120` | `Secure` em produção é exigido, mas host e deploy estão fora do escopo. A evidência precisará ser obtida por configuração/teste, não por implantação real. |
| BAIXA | `docs/specs/001-login/spec.md:111,129-130` | Decisões sobre a conta concreta do professor e contas de teste criam dependência documental com a futura Spec 003. |

## Comparação com a rodada 01

| Bloqueio anterior | Situação na rodada 02 |
|---|---|
| B-01 — contrato HTTP indefinido | **Parcialmente resolvido.** Métodos, rotas, status e erros foram definidos. Permanecem a ambiguidade do corpo 200 de sessão e o contrato de acesso ao logout. |
| B-02 — ADR-002 e tratamento visual ausentes | **Resolvido.** ADR-002 foi declarado e a página, acessibilidade e mensagens de erro foram especificadas. |
| B-03 — usuário inativo e HTTP-only sem aceite | **Resolvido.** Usuário inativo entrou no CA-03 e HTTP-only no CA-09. |
| B-04 — semântica de expiração indefinida | **Resolvido.** Limite inclusivo, renovação por requisição autenticada aceita e ausência de renovação por chamadas públicas ou rejeitadas foram definidos. |
| B-05 — provisionamento não verificável | **Resolvido.** Invocação, variáveis, normalização, idempotência, divergência e códigos de processo foram definidos. |
| B-06 — responsabilidade pelo schema indefinida | **Resolvido.** Spec 002 foi declarada pré-requisito implementado e responsável por `app_user` e V2. |
| Risco — parte “próprios empréstimos” do PRD sem destino | **Resolvido documentalmente.** A futura Spec 004 foi identificada. |

## Conferido

Foram lidos integralmente, somente em modo de leitura:

- `AGENTS.md`
- `.codex/agents/spec-validator.toml`
- `rules/restrictions.md`
- `rules/spec-flow.md`
- `rules/checks.md`
- `docs/PRD.md`
- `docs/adr/001-stack.md`
- `docs/adr/002-design-system.md`
- `docs/specs/002-modelo-dados/spec.md`
- `docs/specs/001-login/spec.md`
- `docs/specs/001-login/verification/pre-approval-01-c485d8a.md`

Também foram conferidos o vínculo com o PRD, objetivo, fora de escopo, regras, CA-01 a CA-11, contratos HTTP/UI/operação, perguntas abertas, aderência aos ADRs, dependência da Spec 002, contradições, duplicações, riscos e comparação com a rodada 01. Nenhum arquivo foi alterado e nenhum comando de escrita foi executado.
