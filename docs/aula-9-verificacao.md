# Verificacao da Aula 9

Data da ultima auditoria: 2026-11-10.

## Estado esperado

- [x] `AGENTS.md` descreve apenas fatos verificaveis e aponta para `rules/`.
- [x] `rules/restrictions.md` define autoridade, escopo, ritmo, producao e precedencia.
- [x] `rules/checks.md`, `migration.md` e `secrets.md` usam comandos e caminhos da stack decidida nos ADRs.
- [x] `rules/tests.md`, `handoff.md` e `spec-flow.md` possuem gatilho, procedimento e verificacao.
- [x] `rules/operacao.md` define commit, push, niveis de aprovacao e configuracao do operador.
- [x] Toda rule declara o leitor.
- [x] `docs/specs/001-login/spec.md` foi aprovada apos tres rodadas independentes do `spec-validator`.
- [x] A Spec 001 possui `plan.md` e `tasks.md`, aprovados pelo operador em 2026-11-10.
- [ ] Supabase local/remoto esta provisionado e verificado.
- [x] Deploy Vercel esta verde em `https://sdd-path-pi.vercel.app/`.
- [x] Skill SOLID esta no caminho de projeto `.agents/skills/solid/SKILL.md` e foi descoberta pelo Codex.
- [x] O unico subagente do projeto e `spec-validator`, em modo somente leitura.

## Teste 1 - descoberta das regras

Passou nesta auditoria. O agente encontrou `AGENTS.md` e os oito arquivos em `rules/`: `restrictions.md`, `operacao.md`, `checks.md`, `migration.md`, `secrets.md`, `tests.md`, `handoff.md` e `spec-flow.md`.

## Teste 2 - provocacoes

- Migration local: permitida quando prevista em plano aprovado ou em pedido explicito; aplicacao remota continua exigindo autorizacao especifica.
- Push para `main`: permitido somente no fluxo direto explicitamente aprovado pelo operador, ainda sujeito a diff, checks e commits separados por tarefa.

## Teste 3 - sessao longa

Pendente de repeticao em uma sessao nova. O `handoff.md` da raiz registra o ponto exato para esse teste.

## Adaptacao necessaria ao material

O slide usa Supabase CLI e RLS como exemplo. Este repositorio decidiu Spring Boot, Spring Security, Flyway e PostgreSQL no ADR-001. As regras foram adaptadas a essa decisao: Flyway controla schema e Spring Security controla acesso; trocar isso exigiria decisao do time e novo ADR.
