# Rodada pre-aprovacao 04 (mudanca de 2026-11-11)

- Base: `dcecbc9` com alteracao local em `docs/specs/001-login/spec.md`
- Executor: instrucoes do `spec-validator` executadas por subagente Claude Code em contexto limpo (Codex CLI indisponivel nesta maquina)
- Sandbox: somente leitura, por instrucao
- Data: 2026-11-11

## Saida do verificador

# Resultado: APROVÁVEL

**Rodada:** pré-aprovação 04 (Spec 001, mudança de 2026-11-11)
**Estado avaliado:** HEAD `dcecbc9`, com alteração local não commitada em `docs/specs/001-login/spec.md` (linhas 3 e 55; conferida com `git diff`). Último commit da spec: `052dc17`.
**Severidade geral:** BAIXA
**Aprovação humana:** ainda necessária

## Bloqueios

Nenhum.

## Critérios não observáveis

Nenhum. Dá para observar e testar a linha nova (`spec.md:55`):
- **401:** a resposta repete o corpo de `spec.md:54` (`UNAUTHENTICATED`, "Sessao invalida ou expirada.").
- **Cookie expirado:** aparece no `Set-Cookie` da resposta. O mecanismo é o mesmo de `SessionCookieService.java:30-39`, com `Max-Age=0`.
- **Sessão encerrada:** uma consulta posterior com o cookie antigo responde 401. A prova só separa "sessão invalidada" de "conta ainda inativa" se o teste reativar a conta antes da nova consulta. Isso não bloqueia; veja os riscos.

## Contradições

Nenhuma.

- **CA-03 / regra `spec.md:21`:** tratam do `POST /api/auth/login` com `INVALID_CREDENTIALS`. A linha nova trata de sessão já criada e usa outro endpoint e outro código. Não há sobreposição.
- **CA-04 / `spec.md:54`:** a linha reaproveita o mesmo 401 e não muda o 200 da sessão válida.
- **CA-05 / logout idempotente (`spec.md:22,60-65`):** o logout continua público (`spec.md:56`; `SecurityConfiguration.java:85-89`; `AuthenticatedSessionActivityFilter.java:31-37`). O filtro previsto vale só para requisições autenticadas e não públicas (`plan.md:63`). Por isso, o logout de uma conta desativada continua respondendo 204.
- **CA-09 / renovação (`spec.md:24`):** o 401 é uma requisição rejeitada.
  - A renovação só acontece com status < 400 (`AuthenticatedSessionActivityFilter.java:84-90`).
  - Com a sessão invalidada, o `setAttribute` cai no `catch` de `IllegalStateException` (linhas 86-89).
  - Se a sessão já tiver expirado por inatividade, o filtro atual rejeita antes, com o mesmo 401 (linhas 69-81). Não há divergência de corpo.
- **CA-09 / cookie sem `Expires`/`Max-Age` (`spec.md:23`):** a regra vale para a emissão. A remoção já usa `Max-Age=0` no logout e na inatividade (`AuthController.java:111`; `AuthenticatedSessionActivityFilter.java:74`).
- **CA-10 / CSRF:** `GET /api/auth/session` não exige CSRF. A ordem prevista, depois do `CsrfFilter` (`plan.md:58-62`), não afeta login nem logout, que são públicos.
- **CA-01, CA-02, CA-06 a CA-08, CA-11:** a linha não os toca.
- **ADR-001 (`docs/adr/001-stack.md:20,36-37,54`):** continua sendo sessão HTTP-only no Spring Security, sem JWT e sem biblioteca nova.
- **Spec 004:** está coerente com `004/spec.md:77-79` (as três rotas respondem 401 e encerram a sessão) e com `004/spec.md:97-99` e CA-05 (`004/spec.md:137`: as páginas redirecionam para `/`). Também está coerente com `004/plan.md:56-74`, que já previa esse comportamento em `GET /api/auth/session` "sem mudar nenhum CA da Spec 001".
- **PRD:** `docs/PRD.md:12` (item 1) não é contrariado.
- **Regra de negócio nova:** a linha não cria nenhuma. Ela só aplica à Spec 001 o comportamento que a decisão do operador e a Spec 004 já definiram.

## Inconsistências não bloqueantes

| Severidade | Arquivo e linha | Inconsistência |
|---|---|---|
| BAIXA | `docs/specs/001-login/spec.md:3` | O Status já diz "validada na rodada 04" antes do resultado desta rodada. A frase só fica verdadeira se este resultado for registrado em `verification/` e confirmado pelo operador. Até lá, é uma afirmação antecipada. |
| BAIXA | `docs/specs/001-login/spec.md:120,122` | "Contrato afetado" não cita o recarregamento da conta pelo backend, nem o teste de conta desativada ou excluída com sessão ativa. |
| BAIXA | `docs/specs/001-login/spec.md:5,112` | O texto ainda chama a Spec 004 de "futura", mas ela já existe e tem plano aprovado (`004/plan.md:3`). O problema vem de antes desta mudança. |
| INFORMATIVA | `AuthController.java:98-101` | O código commitado ainda responde 200 para conta desativada, porque a sessão não recarrega a conta. Pela precedência de `rules/restrictions.md:41`, a spec prevalece. A implementação está prevista em `004/plan.md:58-69` e ainda não existe (`ActiveAccountFilter` não aparece em `backend/src/main/java`). |

## Riscos não bloqueantes

| Severidade | Arquivo e linha | Risco |
|---|---|---|
| MÉDIA | `docs/specs/001-login/spec.md:55,93-105` | Nenhum CA da Spec 001 cobre a linha nova. A verificação final da Spec 001, feita por CA, não vai cobri-la. A prova fica com o CA-05 da Spec 004 (`004/spec.md:137`), que não cita `GET /api/auth/session` de forma explícita. O plano da 004 precisa garantir esse teste (`004/plan.md:67-69`). |
| BAIXA | `docs/specs/001-login/spec.md:55` | Para provar "sessão encerrada" separada de "conta inativa", o teste precisa reativar a conta (ou recriar o registro) e mostrar que o cookie antigo continua com 401. |
| BAIXA | `docs/specs/001-login/spec.md:55` | Em "excluída", a exclusão física de `app_user` pode ser impedida por chave estrangeira de `loan` (Spec 002). Os testes talvez precisem de uma conta sem empréstimos. |

## Perguntas para o operador

Nenhuma. A decisão de 2026-11-11 é humana e não foi reavaliada.

## Comparação com a rodada 03

| Item | Situação na rodada 04 |
|---|---|
| Bloqueios das rodadas 01 a 03 | Continuam resolvidos. A mudança não reabre nenhum. |
| Bloqueios novos | Nenhum. |
| Riscos da rodada 03 | Continuam iguais. O risco BAIXO sobre a remoção do cookie com expiração explícita agora vale também para `spec.md:55`. |
| Mudança avaliada | Uma linha nova no contrato (`spec.md:55`) e a nota no Status (`spec.md:3`). As linhas seguintes se deslocaram em +1 em relação às citações da rodada 03. |

## Conferido

Arquivos lidos em modo somente leitura:
- `AGENTS.md`, `.codex/agents/spec-validator.toml`, `rules/restrictions.md`, `rules/spec-flow.md`.
- Trechos relevantes de `docs/PRD.md` e de `docs/adr/001-stack.md`.
- `docs/specs/001-login/spec.md` inteira, com o `git diff`.
- Trecho final de `docs/specs/001-login/verification/pre-approval-03-c485d8a.md`.
- `docs/specs/004-catalogo-emprestimos/spec.md:66-102,137`.
- `docs/specs/004-catalogo-emprestimos/plan.md:1-6,56-74`.
- `AuthController.java`, `AuthenticatedSessionActivityFilter.java`, `SecurityConfiguration.java` e `SessionCookieService.java`.
- Busca por `active()`/`ActiveAccount` em `backend/src/main/java`.

Nenhum arquivo foi alterado. Nenhum comando de escrita foi executado.
