# Handoff

- Ponto atual: regras consolidadas; ADR-002 registra o design system; Spec 002 e migration V2 do modelo inicial criadas.
- Criterio em andamento: modelo de dados inicial validado; a Spec 001 de login continua aguardando respostas.
- Branch: `codex/vercel-deploy`.
- Alteracoes locais: ADR-002 implementado no frontend Next.js com Tailwind, componentes acessiveis, Phosphor e CSP com nonce.
- Banco local: Flyway V1 e V2 aplicadas; `app_user`, `equipment` e `loan` confirmadas; nada foi aplicado remotamente.
- Supabase: projeto e CLI local ainda nao configurados.
- Publicado: branch `codex/vercel-deploy` vinculada ao GitHub; `vercel.json` alinhado a Next.js e pnpm 10 para o proximo deploy.
- Verificacao: lockfile congelado com pnpm 10, typecheck, build Next.js, CSP de producao e imagem Docker do frontend passaram.
- Skill: `.codex/skills/solid/SKILL.md` validada e descoberta pelo Codex CLI em modo somente leitura.
- Execucao local: frontend `http://localhost:5173`, backend `http://localhost:8080`.
- Nao fazer: nao implementar endpoints ou telas conectadas antes da spec funcional correspondente.
- Nao fazer: nao publicar, alterar env remoto ou tocar no Supabase remoto sem autorizacao especifica.
- Proximo passo: fechar as quatro perguntas da Spec 001 antes de implementar login e sessao.
