# Handoff

- Ponto atual: Spec 001 de login e seu plano foram aprovados pelo operador apos tres rodadas independentes; implementacao ainda nao iniciada.
- Criterio em andamento: Spec 002 esta implementada; a proxima unidade autorizada e a T001 da Spec 001.
- Branch: `main`; fluxo direto aprovado, preservando checks e commits separados.
- Alteracoes locais: nenhuma alteracao de produto foi iniciada; esta atualizacao documental remove estados antigos de elaboracao.
- Banco local: Flyway V1 e V2 foram validadas anteriormente; `app_user`, `equipment` e `loan` existem; nada aplicado remotamente.
- Supabase: projeto e CLI local ainda nao configurados; Spring Boot continua sendo a unica camada de acesso ao banco.
- Publicado: `main` no GitHub aponta para `ec101bd`; a spec, o plano, as tarefas e as tres rodadas de pre-validacao estao publicados.
- Frontend: Next.js, Tailwind, ADR-002, componentes acessiveis, Phosphor e CSP com nonce.
- Agentes: skill em `.agents/skills/solid/SKILL.md`; unico subagente em `.codex/agents/spec-validator.toml`.
- Verificacao atual: rodadas 01 e 02 bloqueadas e corrigidas; rodada 03 `APROVAVEL`. Os relatorios estao em `docs/specs/001-login/verification/`.
- Nao fazer: nao executar mais de uma tarefa da Spec 001 por autorizacao; nao tocar em segredo, Supabase remoto ou dados de producao sem autorizacao.
- Proximo passo: executar somente a T001, rodar os checks e parar para revisao.
