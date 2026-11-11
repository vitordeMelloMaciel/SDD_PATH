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

Para testar a jornada da Spec 004, crie uma conta local (próxima seção) e entre em `http://localhost:5173`:

1. Com o papel `colaborador`, o login leva a `/catalogo`, com os 12 equipamentos.
   Os 9 disponíveis têm “Solicitar →”, e os 3 em manutenção mostram “Em manutenção”.
2. Clique em “Solicitar →”. O card passa a “Emprestado” e aparece a mensagem de empréstimo registrado.
3. Abra **Meus empréstimos** no topo para ver o item e a data de devolução em `America/Sao_Paulo`.
4. Com o papel `operacoes`, as duas páginas mostram só “Acesso negado”.

Sem `EQUIPMENT_IMAGE_BASE_URL` no ambiente, os cards mostram “Foto indisponível”.
Para apagar os empréstimos de teste locais, use `docker compose down -v`, que também apaga o banco local.

## Criar um usuário de teste local

Defina as quatro variáveis `PROVISION_USER_*` do `.env.example` no ambiente local.
Use `colaborador` ou `operacoes` como papel e uma senha de pelo menos 12 caracteres,
limitada a 72 bytes em UTF-8 pelo BCrypt. Execute na raiz do repositório:

```powershell
docker compose run --build --rm backend-provision
```

A segunda execução com os mesmos dados retorna sucesso. Se a conta existente
tiver dados diferentes, o comando retorna código 3 e preserva o registro.
Para a conta da avaliação em produção, o operador deve executar o provisionamento
com a configuração do banco de produção e os segredos fora do Git.

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

O contrato em `rules/operacao.md` vale para os commits e o push. Use um commit por tarefa, citando a tarefa e o CA-xx, e `git add` com caminhos explícitos. O push direto na `main` só acontece com autorização explícita do operador, sem `--force` e sem tag de entrega:

```bash
git status --short
git add <arquivos-da-tarefa>
git commit -m "<mensagem-da-tarefa> [Txxx, CA-xx]"
git push origin main
```

Separe mudanças de regras, andar zero e frontend em commits diferentes.
Inclua `.agents/` e `.codex/` no commit para publicar a skill SOLID e o subagente validador junto com o repositorio. O frontend usa somente pnpm e `pnpm-lock.yaml`; nao misture lockfiles de outros gerenciadores.

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

A skill fica em `.agents/skills/solid/SKILL.md` e deve citar o codigo real, respeitar specs e ADRs e evitar abstracoes sem necessidade. O unico subagente fica em `.codex/agents/spec-validator.toml` e valida specs sem editar arquivos nem substituir a aprovacao humana.

## Deploy

A Vercel publica apenas o frontend Next.js. No projeto da Vercel, configure `frontend` como **Root Directory**; o `frontend/vercel.json` fixa o pnpm 10 e deixa o adaptador Next usar o diretorio de saida padrao.

O backend Spring Boot roda no Railway, conforme o [ADR-003](docs/adr/003-hospedagem-backend.md):

- **Serviço:** **Root Directory** `backend`, construído pelo `Dockerfile`. O domínio público aponta para a porta 8080.
- **Variáveis no Railway, sem valores no Git:**
  - `SPRING_DATASOURCE_URL`, o JDBC do Session pooler do Supabase com `?sslmode=require`;
  - `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`;
  - `SESSION_COOKIE_SECURE=true` e `SERVER_PORT=8080`;
  - `EQUIPMENT_IMAGE_BASE_URL`.
- **Migrations:** o primeiro deploy aplica as migrations Flyway pendentes.

As fotos dos 12 equipamentos ficam num bucket público do Supabase Storage, com as chaves `equipment/TI-0001.jpg` até `equipment/TI-0012.jpg`. `EQUIPMENT_IMAGE_BASE_URL` é a origem pública do bucket, `https://<projeto>.supabase.co/storage/v1/object/public/<bucket>`, sem barra final e sem `/equipment`. Configure a mesma variável no Railway e no ambiente **Production** da Vercel, e faça um novo deploy do front, porque o Next.js a lê no build.

Para ligar o login publicado, abra primeiro `https://<host-do-backend>/health`
e confirme HTTP 200 com o banco ativo. No projeto Vercel, configure
`INTERNAL_API_BASE_URL` no ambiente **Production** com a origem HTTPS do
Spring, sem o sufixo `/api`. Faça um novo deploy de produção. O Next.js lê
essa variável durante o build.

Confira `/api/auth/csrf` no domínio da Vercel. O esperado é HTTP 200 com um
token JSON. Um 404 indica que o encaminhamento ou o endpoint do Spring ainda
está ausente. No backend de produção, configure `SESSION_COOKIE_SECURE=true`.
As credenciais JDBC ficam no host do Spring. Consulte `supabase/README.md`
para preparar o banco.

## Processo

Leia `AGENTS.md` e `rules/restrictions.md` antes de qualquer alteracao. Funcionalidades de produto comecam em `docs/specs/`.
