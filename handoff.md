# Handoff

- Ponto atual: Spec 001 de login aprovada pelo operador apos tres rodadas independentes; plano e tarefas redigidos, ainda sem implementacao de produto.
- Criterio em andamento: Spec 002 esta implementada; Spec 001 aguarda aprovacao explicita de `plan.md` e `tasks.md` antes da T001.
- Branch: `main`; fluxo direto aprovado, preservando checks e commits separados.
- Alteracoes locais: plano, tarefas e registros desta aprovacao aguardam publicacao; nenhuma alteracao de produto foi iniciada.
- Banco local: Flyway V1 e V2 foram validadas anteriormente; `app_user`, `equipment` e `loan` existem; nada aplicado remotamente.
- Supabase: projeto e CLI local ainda nao configurados; Spring Boot continua sendo a unica camada de acesso ao banco.
- Publicado: a spec e as tres rodadas de pre-validacao estao no commit local `052dc17`; o envio para o GitHub sera confirmado ao encerrar esta atualizacao.
- Frontend: Next.js, Tailwind, ADR-002, componentes acessiveis, Phosphor e CSP com nonce.
- Agentes: skill em `.agents/skills/solid/SKILL.md`; unico subagente em `.codex/agents/spec-validator.toml`.
- Verificacao atual: rodadas 01 e 02 bloqueadas e corrigidas; rodada 03 `APROVAVEL`. Os relatorios estao em `docs/specs/001-login/verification/`.
- Nao fazer: nao iniciar a T001 antes de aprovar o plano; nao tocar em segredo, Supabase remoto ou dados de producao sem autorizacao.
- Proximo passo: aprovar `docs/specs/001-login/plan.md` e `tasks.md`; depois executar T001 a T005, uma por vez, com verificacao final independente.
