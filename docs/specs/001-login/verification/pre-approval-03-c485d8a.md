# Rodada pre-aprovacao 03

- Base: `c485d8a` com alteracoes locais na Spec 001
- Executor: `spec-validator` via Codex CLI efemero
- Sandbox: `read-only`
- Data: 2026-11-10

## Saida do verificador

# Resultado: APROVÁVEL

**Rodada:** pré-aprovação 03
**Severidade geral:** nenhuma bloqueante
**Aprovação humana:** ainda necessária

A Spec 001 possui vínculo com o PRD, contratos suficientemente determinados e critérios CA-01 a CA-11 observáveis. Os três bloqueios da rodada 02 foram resolvidos.

## Bloqueios

Nenhum.

## Critérios não observáveis

Nenhum. Todos os critérios possuem resultado esperado verificável:

- CA-01: página, acessibilidade, foco e submissão única.
- CA-02 a CA-06: contratos HTTP, sessão, logout e papel.
- CA-07 e CA-08: sigilo e provisionamento idempotente.
- CA-09 e CA-10: cookie, expiração e CSRF.
- CA-11: integração same-origin pelo encaminhamento do frontend.

## Contradições

Nenhuma contradição bloqueante entre a Spec 001, o PRD, os ADRs 001 e 002 ou a Spec 002.

A dependência da tabela `app_user` está coerente com `docs/specs/002-modelo-dados/spec.md:9-24`, e a Spec 001 não atribui a si uma migration nova (`docs/specs/001-login/spec.md:9,118`).

## Inconsistência não bloqueante

| Severidade | Arquivo e linha | Evidência |
|---|---|---|
| BAIXA | `docs/specs/001-login/spec.md:3` | O status menciona apenas correção após a rodada 01, embora a rodada 02 já esteja registrada. Isso não altera o contrato funcional nem os critérios de aceite. |

## Duplicações

Não bloqueantes e consistentes:

- Atributos do cookie: `spec.md:23,44,95,102`.
- Sigilo de senha e hash: `spec.md:20,89,100`.
- Idempotência do logout: `spec.md:22,59-64,98`.
- Papel do usuário: `spec.md:19,44,53,84,99`.
- Provisionamento: `spec.md:80-90,101,118-122,126`.
- Exclusão de bloqueio temporário, CAPTCHA e rate limiting: `spec.md:27,109,129`.

## Riscos não bloqueantes

| Severidade | Arquivo e linha | Risco |
|---|---|---|
| ALTA | `docs/specs/001-login/spec.md:27,109,129` | A exclusão consciente de bloqueio temporário, CAPTCHA e rate limiting mantém exposição a tentativas automatizadas. |
| MÉDIA | `docs/specs/001-login/spec.md:78,110-112` | A entrega autentica e consulta a sessão, mas não entrega uma jornada de negócio autenticada completa. |
| MÉDIA | `docs/specs/001-login/spec.md:102,113,121` | `Secure` em produção deverá ser comprovado por configuração ou teste, pois deploy e host estão fora do escopo. |
| BAIXA | `docs/specs/001-login/spec.md:23,63,102` | A emissão do cookie sem `Expires`/`Max-Age` e sua remoção no logout deverão ser distinguidas na verificação, pois a instrução HTTP de remoção pode usar expiração explícita. |
| BAIXA | `docs/specs/001-login/spec.md:112,130-131` | As decisões sobre a conta concreta do professor mantêm dependência documental com a futura Spec 003. |

## Comparação entre rodadas

### Rodada 01

| Bloqueio | Situação na rodada 03 |
|---|---|
| B-01 — contrato HTTP indefinido | **Resolvido.** Métodos, rotas, status, payloads e erros estão em `spec.md:29-64`. |
| B-02 — ADR-002 e erros visuais ausentes | **Resolvido.** ADR declarado em `spec.md:7`; página e erros definidos em `spec.md:66-78`. |
| B-03 — usuário inativo e HTTP-only sem aceite | **Resolvido.** Cobertos por CA-03 e CA-09 em `spec.md:96,102`. |
| B-04 — expiração não determinística | **Resolvido.** Limite inclusivo e regras de renovação estão em `spec.md:24,102`. |
| B-05 — provisionamento indefinido | **Resolvido.** Invocação, entradas e resultados estão em `spec.md:80-90,101`. |
| B-06 — responsabilidade pelo schema | **Resolvido.** A Spec 002 é pré-requisito explícito em `spec.md:9,118`. |

### Rodada 02

| Bloqueio | Situação na rodada 03 |
|---|---|
| B-01 — envelope da sessão ambíguo | **Resolvido.** `spec.md:53` exige o mesmo envelope e os mesmos quatro campos do login. |
| B-02 — logout ausente das rotas públicas | **Resolvido.** `spec.md:55` inclui expressamente `POST /api/auth/logout`. |
| B-03 — repetição do logout sem sequência CSRF | **Resolvido.** `spec.md:64,98` exige obter novo token antes da repetição. |

Nenhum bloqueio novo foi identificado.

## Conferido

Foram lidos integralmente, em modo somente leitura:

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
- `docs/specs/001-login/verification/pre-approval-02-c485d8a.md`

Também foram conferidos vínculo com o PRD, objetivo, fora de escopo, regras, CA-01 a CA-11, contratos HTTP/UI/operação, perguntas abertas, aderência aos ADRs, dependência da Spec 002, contradições, duplicações, riscos e evolução entre as três rodadas. Nenhum arquivo foi alterado e nenhum comando de escrita foi executado.
