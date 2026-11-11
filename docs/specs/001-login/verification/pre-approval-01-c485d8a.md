# Rodada pre-aprovacao 01

- Base: `c485d8a` com alteracoes locais na Spec 001
- Executor: `spec-validator` via Codex CLI efemero
- Sandbox: `read-only`
- Data: 2026-11-10

## Saida do verificador

# Resultado: BLOQUEADA

A spec não está apta para aprovação humana nem para originar `plan.md`. Há contratos e critérios que ainda não permitem verificação reproduzível.

## Bloqueios

| ID | Severidade | Arquivo e linha | Evidência |
|---|---|---|---|
| B-01 | CRÍTICA | `docs/specs/001-login/spec.md:30-34,48-54`; `docs/adr/001-stack.md:69-71` | O contrato HTTP não define métodos, rotas, payloads, status nem formatos de resposta para login, consulta de sessão e logout. “Identidade mínima”, “erro genérico” e “rota protegida” não estão definidos. O ADR-001 deixa nomes de endpoints e formato de erros para uma spec posterior; esta spec não fecha essas decisões. CA-02 a CA-06 não têm referência reproduzível suficiente. |
| B-02 | ALTA | `docs/specs/001-login/spec.md:7,31,52,65`; `docs/adr/002-design-system.md:11-12,200-211,296,370-380,442-445` | A spec afeta a tela de login, mas não declara o ADR-002 como aplicável. A mensagem e o tratamento visual do erro permanecem indefinidos. O ADR-002 afirma que a mensagem pertence à spec e que o tratamento visual não pode ser inventado. Isso contradiz a declaração de que não restam perguntas abertas. |
| B-03 | ALTA | `docs/specs/001-login/spec.md:16,23,27-37,60` | Duas regras explícitas não possuem cobertura de aceite: usuário inativo deve receber exatamente a resposta genérica; o cookie deve ser HTTP-only. CA-09 cobre duração e persistência, mas não prova HTTP-only. |
| B-04 | ALTA | `docs/specs/001-login/spec.md:22,37`; `docs/adr/001-stack.md:19-20,36` | CA-09 não define o que conta como atividade, quais requisições renovam a inatividade, nem o comportamento no limite exato de oito horas. “Sessão segura” também não possui critérios observáveis suficientes para o cenário de frontend e backend separados. |
| B-05 | ALTA | `docs/specs/001-login/spec.md:24,36,54,58` | O comando administrativo não tem contrato verificável: nome e forma de invocação, variáveis obrigatórias, equivalência de e-mail, comportamento quando o mesmo e-mail chega com senha ou papel diferentes e resultado esperado de sucesso/falha. “Idempotente” não determina se a segunda execução é no-op, atualização ou erro. |
| B-06 | ALTA | `docs/specs/001-login/spec.md:11,50` | A spec se apresenta como primeira fatia vertical e diz criar o registro de usuário, mas atribui a tabela e a migration à Spec 002. Não está determinado se a Spec 002 é pré-requisito aprovado ou qual spec possui a responsabilidade pelo schema. O plano não pode derivar essa decisão. |

## Critérios não plenamente observáveis

- **CA-01:** observável quanto à presença dos campos, mas incompleto perante o ADR-002: não cobre rótulos associados, teclado, foco, botão e composição aplicável ao login.
- **CA-02:** “identidade mínima” não possui campos definidos; rota, status e corpo também não foram especificados.
- **CA-03:** “erro genérico” não tem status, corpo, texto nem comportamento visual definidos.
- **CA-04:** não identifica qual rota deve estar protegida nem delimita as exceções públicas.
- **CA-05:** não define os contratos de logout e da chamada protegida usada na comprovação.
- **CA-06:** “disponível para verificações” não define em qual identidade/principal ou resposta o papel deve aparecer.
- **CA-07:** “respostas e logs de autenticação” não delimita operações, eventos ou saídas examinadas.
- **CA-08:** as duas propriedades principais são testáveis, mas falta o contrato de execução e a semântica da segunda chamada.
- **CA-09:** a ausência de persistência do cookie é observável; a expiração por inatividade não é determinística sem a semântica temporal.

Também faltam critérios próprios para usuário inativo e para o atributo HTTP-only.

## Contradições e inconsistências

- `spec.md:65` afirma que não restam perguntas abertas, enquanto o tratamento de erro continua explicitamente não decidido pelo ADR-002 (`ADR-002:211,445`).
- `spec.md:7` relaciona somente o ADR-001, embora a entrega inclua uma tela regida pelo ADR-002.
- `spec.md:11` chama esta entrega de primeira fatia vertical, mas `spec.md:50` a torna dependente do schema atribuído à Spec 002.

Não foi encontrada contradição entre o uso de e-mail/senha, cookie HTTP-only e papéis da spec e as decisões do ADR-001.

## Duplicações

- `spec.md:22-25` e `spec.md:58-61` repetem duração da sessão, usuário inativo, provisionamento e ausência de bloqueio temporário. O conteúdo é consistente, mas pode divergir em alterações futuras.
- CA-07 e CA-08 sobrepõem a proibição de exposição de senha; os escopos são diferentes — autenticação versus comando administrativo — e a duplicação não bloqueia.
- As regras de autenticação e papéis repetem o ADR-001 de forma consistente e útil para rastreabilidade.

## Riscos não bloqueantes

- **ALTO:** bloqueio temporário, CAPTCHA e rate limiting foram conscientemente excluídos (`spec.md:25,46,61`), mantendo risco de tentativas automatizadas.
- **MÉDIO:** o vínculo com o item 1 do PRD é parcial. O trecho “cada pessoa vê os próprios empréstimos” (`docs/PRD.md:12`) está excluído desta entrega (`spec.md:44`), sem identificação explícita da spec futura responsável.
- **BAIXO:** as decisões sobre a conta do professor (`spec.md:62-63`) não pertencem ao aceite desta funcionalidade e criam acoplamento documental com uma futura Spec 003.

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
- `docs/specs/001-login/spec.md`

Foram conferidos vínculo com o PRD, objetivo, fora de escopo, regras, todos os CA-xx, contrato afetado, perguntas abertas, aderência aos ADRs, contradições, duplicações e riscos. Nenhum arquivo foi alterado e nenhum comando de escrita foi executado.
