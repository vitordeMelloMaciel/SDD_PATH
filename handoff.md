# Handoff

- Ponto atual: aulas 7 a 9 auditadas; regras, specs, ADRs, design system, skill SOLID e subagente de validacao organizados.
- Criterio em andamento: AZ-05 voltou a consultar `/health`; Spec 002 esta implementada; Spec 001 aguarda quatro respostas do operador.
- Branch: `main`; auditoria publicada nos commits `b4d723d` e `9f65414`.
- Alteracoes locais: nenhuma alteracao de produto pendente apos a publicacao desta auditoria.
- Banco local: Flyway V1 e V2 foram validadas anteriormente; `app_user`, `equipment` e `loan` existem; nada aplicado remotamente.
- Supabase: projeto e CLI local ainda nao configurados; Spring Boot continua sendo a unica camada de acesso ao banco.
- Publicado: GitHub atualizado; `https://sdd-path-pi.vercel.app/` recebeu novo gatilho de deploy pela `main`.
- Frontend: Next.js, Tailwind, ADR-002, componentes acessiveis, Phosphor e CSP com nonce.
- Agentes: skill em `.agents/skills/solid/SKILL.md`; unico subagente em `.codex/agents/spec-validator.toml`.
- Verificacao atual: Codex descobriu `$solid`; Docker Desktop estava fechado e impediu repetir testes; instalacao local foi interrompida por demora de rede.
- Nao fazer: nao implementar login antes de aprovar a Spec 001; nao tocar em segredo, Supabase remoto ou deploy sem autorizacao.
- Proximo passo: repetir os checks com Docker aberto e confirmar o novo deploy; depois responder as quatro perguntas da Spec 001.
