# Empréstimo de Equipamentos Internos

Sistema web interno para controlar empréstimos de equipamentos.
O documento de negócio está em docs/PRD.md.

## Onde olhar

- docs/PRD.md — o que o negócio precisa. Não é spec.
- docs/adr/ — decisões técnicas já tomadas e o motivo delas.
- rules/restrictions.md — leia sempre, antes de qualquer tarefa.
- rules/checks.md — o que precisa passar antes de declarar uma tarefa pronta.
- rules/migration.md — procedimento para mudanca de schema.
- rules/secrets.md — limites para variaveis e credenciais.
- rules/tests.md — como os testes rodam neste repositorio.
- rules/handoff.md — como registrar o estado ao encerrar uma tarefa.
- rules/spec-flow.md — ordem dos artefatos de uma funcionalidade.
- rules/operacao.md — quem commita, publica e aprova cada tipo de acao.
- .agents/skills/solid/SKILL.md — revisao e refatoracao Java orientada por SOLID, invocavel com `$solid`.
- .codex/agents/spec-validator.toml — unico subagente do projeto; valida specs em modo somente leitura.
- docs/andar-zero.md — critérios do esqueleto técnico inicial.
- docs/specs/ — uma pasta por funcionalidade, com spec, plano e tarefas. A Spec 001 de login está em elaboração.

## Autoridade

`rules/restrictions.md` e a fonte unica para limites de autoridade e precedencia.
Os demais arquivos em `rules/` descrevem somente procedimentos especializados.
O estado temporario da execucao fica apenas em `handoff.md`.

## Validacao de specs

Depois de redigir ou alterar uma spec, delegue sua validacao ao subagente `spec-validator` antes de pedir aprovacao ao operador ou criar `plan.md`. O subagente apenas aponta bloqueios; a aprovacao continua sendo humana.

## Mapa atual

- `backend/` — aplicação Spring Boot.
- `backend/src/main/java/` — código Java de produção.
- `backend/src/main/resources/db/migration/` — migrations Flyway.
- `backend/src/test/java/` — testes automatizados do backend.
- `frontend/` — aplicação Next.js/TypeScript.
- `frontend/app/` — rotas e tokens globais do front.
- `frontend/components/` — componentes de aplicação e do Design System.
- `docs/` — artefatos de produto, decisão e planejamento.
- `rules/` — limites de autoridade e execução.

## Comandos atuais

- Subir tudo do zero: `docker compose up --build`
- Derrubar os serviços: `docker compose down`
- Derrubar serviços e volume do banco: `docker compose down -v`
- Testar backend: `docker compose run --rm backend-test`
- Conferir health: `curl http://localhost:8080/health`
- Abrir front: `http://localhost:5173`
- Build de deploy do front: `pnpm --dir frontend install --frozen-lockfile` e `pnpm --dir frontend run build`

Consulte `handoff.md` para o estado atual, verificacoes executadas e proximo passo.
