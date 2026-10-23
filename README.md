# SDD Path

Sistema interno para controlar emprestimos de equipamentos. O projeto usa Spring Boot, Next.js/TypeScript e PostgreSQL, conforme [docs/adr/001-stack.md](docs/adr/001-stack.md).

## Executar localmente

Com o Docker Desktop aberto, execute na raiz:

```bash
docker compose up -d --build
docker compose ps
```

- Frontend: `http://localhost:5173`
- Health da API: `http://localhost:8080/health`

No teste manual do frontend, confira a busca de equipamentos, os filtros por status e categoria, o menu em tela pequena, a navegacao por teclado do controle segmentado e a abertura do formulario `Novo emprestimo`. Essas interacoes ainda nao persistem dados.

## Testes e checks

Teste de integracao do backend com PostgreSQL real:

```bash
docker compose run --rm backend-test
```

Build dos dois artefatos:

```bash
docker compose build backend frontend
```

Build isolado do frontend:

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm run build
```

Resultado esperado: Maven informa `BUILD SUCCESS`, sem falhas de teste, e o Next.js conclui `next build` sem erro. Para encerrar os servicos, use `docker compose down`.

## Atualizar o GitHub

O contrato em `rules/operacao.md` determina que o operador revisa, commita e faz o push. Nunca publique direto em `main`:

```bash
git switch -c codex/aulas-7-9
git status --short
git add <arquivos-da-tarefa>
git commit -m "<mensagem-da-tarefa>"
git push -u origin codex/aulas-7-9
```

Depois do push, abra um pull request para `main` no GitHub. Separe mudancas de regras, andar zero e frontend em commits diferentes.
Inclua `.codex/` no commit da Aula 9 para publicar a skill SOLID junto com o repositorio. O frontend usa somente pnpm e `pnpm-lock.yaml`; nao misture lockfiles de outros gerenciadores.

## Testar o agente e a skill SOLID

Abra um PowerShell na raiz do repositorio e inicie o Codex CLI:

```powershell
codex
```

Em uma nova sessao, confira as instrucoes carregadas:

```text
Liste os arquivos de instrucao e as skills disponiveis neste projeto.
```

Depois invoque a skill explicitamente:

```text
$solid Revise o backend deste projeto. Nao altere arquivos. Liste apenas violacoes SOLID concretas, com arquivo, linha, risco e a menor correcao util.
```

A skill fica em `.codex/skills/solid/SKILL.md` e deve citar o codigo real, respeitar specs e ADRs e evitar abstracoes sem necessidade.

## Deploy

A Vercel publica apenas o frontend Next.js. No projeto da Vercel, configure `frontend` como **Root Directory**; o `frontend/vercel.json` fixa o pnpm 10 e deixa o adaptador Next usar o diretorio de saida padrao. O backend Spring Boot precisa de uma hospedagem Java separada; em producao, ele podera usar o PostgreSQL do Supabase por JDBC.

O Supabase ainda nao foi provisionado. Consulte `supabase/README.md` e nao adicione credenciais ao Git.

## Processo

Leia `AGENTS.md` e `rules/restrictions.md` antes de qualquer alteracao. Funcionalidades de produto comecam em `docs/specs/`.
