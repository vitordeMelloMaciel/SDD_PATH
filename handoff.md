# Handoff

- Ponto atual: regras consolidadas; ADR-002 registra o design system; Spec 002 e migration V2 do modelo inicial criadas.
- Criterio em andamento: modelo de dados inicial validado; a Spec 001 de login continua aguardando respostas.
- Branch: `main`; ultimo commit `6cc5d51 Initial commit`.
- Alteracoes locais: andar zero, regras, frontend visual, modelo de dados e documentos ainda nao commitados.
- Banco local: Flyway V1 e V2 aplicadas; `app_user`, `equipment` e `loan` confirmadas; nada foi aplicado remotamente.
- Supabase: projeto e CLI local ainda nao configurados.
- Publicado: repositorio remoto tem apenas o commit inicial; Vercel vinculada, deploy anterior com erro.
- Verificacao: build Vite passou; imagens Docker passaram; 6 testes backend passaram; `/health` retornou banco `up`.
- Skill: `.codex/skills/solid/SKILL.md` validada e descoberta pelo Codex CLI em modo somente leitura.
- Execucao local: frontend `http://localhost:5173`, backend `http://localhost:8080`.
- Nao fazer: nao implementar endpoints ou telas conectadas antes da spec funcional correspondente.
- Nao fazer: nao publicar, alterar env remoto ou tocar no Supabase remoto sem autorizacao especifica.
- Proximo passo: fechar as quatro perguntas da Spec 001 antes de implementar login e sessao.
