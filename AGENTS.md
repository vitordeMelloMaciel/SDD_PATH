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
- docs/andar-zero.md — critérios do esqueleto técnico inicial.
- docs/specs/ — uma pasta por funcionalidade, com spec, plano e tarefas. Ainda não existe porque nenhuma funcionalidade de produto começou.

## Precedência

Se dois artefatos discordarem sobre comportamento, a spec vence o código.
Se uma spec discordar de um ADR, pare e avise.
Se algo não estiver em lugar nenhum, pergunte — não decida.

## Processo

- Leia docs/adr/ antes de propor qualquer coisa estrutural.
- Antes de implementar, liste os requisitos ambíguos que encontrar.
- Decisão estrutural nova precisa de ADR antes do código. Você não escreve o ADR: descreve a decisão e as alternativas, e para.
- Uma tarefa por vez.
- Não implemente funcionalidade do PRD sem spec em docs/specs/.

## Mapa atual

- `backend/` — aplicação Spring Boot.
- `backend/src/main/java/` — código Java de produção.
- `backend/src/main/resources/db/migration/` — migrations Flyway.
- `backend/src/test/java/` — testes automatizados do backend.
- `frontend/` — aplicação Vite/TypeScript.
- `frontend/src/` — código TypeScript e CSS do front.
- `docs/` — artefatos de produto, decisão e planejamento.
- `rules/` — limites de autoridade e execução.

## Comandos atuais

- Subir tudo do zero: `docker compose up --build`
- Derrubar os serviços: `docker compose down`
- Derrubar serviços e volume do banco: `docker compose down -v`
- Testar backend: `docker compose run --rm backend-test`
- Conferir health: `curl http://localhost:8080/health`
- Abrir front: `http://localhost:5173`
- Build de deploy do front: `npm --prefix frontend install` e `npm --prefix frontend run build`

## Estado atual do projeto

O andar zero está implementado. O projeto sobe Postgres, backend e front; aplica uma migration técnica; expõe `GET /health`; e tem teste de integração para esse endpoint.
Ainda não existe login, catálogo, empréstimo, devolução, tela de Operações ou tabela de domínio do PRD.
O repositório local agora acompanha `origin/main`, mas as mudancas locais ainda nao foram publicadas.
O projeto Vercel existe e esta ligado ao GitHub; o deploy atual falha porque estava configurado como Python. `vercel.json` define o build Vite correto, mas so tera efeito depois de entrar no remoto.
O projeto Supabase e a CLI local ainda nao foram criados. A pasta `supabase/` registra essa pendencia sem fingir que a infraestrutura existe.
