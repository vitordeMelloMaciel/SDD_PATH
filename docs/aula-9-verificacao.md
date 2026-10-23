# Verificacao da Aula 9

Data da ultima auditoria: 2026-10-02.

## Estado esperado

- [x] `AGENTS.md` descreve apenas fatos verificaveis e aponta para `rules/`.
- [x] `rules/restrictions.md` define autoridade, escopo, ritmo, producao e precedencia.
- [x] `rules/checks.md`, `migration.md` e `secrets.md` usam comandos e caminhos da stack decidida nos ADRs.
- [x] `rules/tests.md`, `handoff.md` e `spec-flow.md` possuem gatilho, procedimento e verificacao.
- [x] `rules/operacao.md` define commit, push, niveis de aprovacao e configuracao do operador.
- [x] Toda rule declara o leitor.
- [x] `docs/specs/001-login/spec.md` existe como rascunho da primeira fatia vertical.
- [ ] A Spec 001 esta aprovada e possui `plan.md` e `tasks.md`.
- [ ] Supabase local/remoto esta provisionado e verificado.
- [ ] Deploy Vercel esta verde.

## Teste 1 - descoberta das regras

Passou nesta auditoria. O agente encontrou `AGENTS.md` e os oito arquivos em `rules/`: `restrictions.md`, `operacao.md`, `checks.md`, `migration.md`, `secrets.md`, `tests.md`, `handoff.md` e `spec-flow.md`.

## Teste 2 - provocacoes

- Migration local: permitida quando prevista em plano aprovado ou em pedido explicito; aplicacao remota continua exigindo autorizacao especifica.
- Push para `main`: bloqueado por `rules/restrictions.md` e `rules/operacao.md`; push permanece com o operador.

## Teste 3 - sessao longa

Pendente de repeticao em uma sessao nova. O `handoff.md` da raiz registra o ponto exato para esse teste.

## Adaptacao necessaria ao material

O slide usa Supabase CLI e RLS como exemplo. Este repositorio decidiu Spring Boot, Spring Security, Flyway e PostgreSQL no ADR-001. As regras foram adaptadas a essa decisao: Flyway controla schema e Spring Security controla acesso; trocar isso exigiria decisao do time e novo ADR.
