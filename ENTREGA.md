# Entrega da pratica

Documento progressivo. Itens ainda nao executados permanecem marcados como pendentes; nenhuma afirmacao de producao e feita sem evidencia no link publicado.

## Links

- Repositorio: <https://github.com/vitordeMelloMaciel/SDD_PATH>
- Producao atual: <https://sdd-path-pi.vercel.app/>
- Branch de trabalho: `main` (fluxo direto aprovado pelo operador, com checks e commits separados)

## Estado para a correção

| Requisito do HTML | Estado e evidência |
| --- | --- |
| Subagente verificador | Configuração somente leitura em `.codex/agents/spec-validator.toml`; rodadas de pré-aprovação registradas nas Specs 001 (três), 003 (três) e 004 (rodadas 05 a 07). A verificação final e a prova com erro plantado seguem pendentes. |
| Skill de imagens | API da Pixabay e busca em lotes executadas. Quatro fotos aprovadas, oito pendentes. Commits `ad2d9af` e `9277a10`. |
| Dados iniciais | Os 12 equipamentos conferem com a lista exata da prática. Spec 003 aprovada pelo operador em 2026-11-11 após a rodada `pre-approval-03-8d40630` (`42acd95`), com `plan.md`, `tasks.md` e o DDL da V3. V3 (`65ee264`, T001) e V4 com a carga dos 12 itens (`a46cbd7`, T002) e composição de `EQUIPMENT_IMAGE_BASE_URL` (`d16a3e6`, T006) commitadas; suíte com 31 testes passando. Em produção, em 2026-11-11: V1 a V4 aplicadas pelo backend no Railway, 12 itens (9 disponíveis e 3 em manutenção) e conta de avaliação provisionada com 0 empréstimos (`479d774`). Manifesto completo e upload das fotos pendentes. |
| Solicitação e Meus empréstimos | A migration V2 já protege o limite, atraso e disponibilidade no banco. Spec 004 aprovada pelo operador em 2026-11-11 após a rodada `pre-approval-07-8d40630` (`b0046fa`); plano e tarefas pendentes. Catálogo, API e jornada da prática pendentes. |
| Entrega e link de produção | Tela de login publicada. Em 2026-11-11, o backend no Railway respondeu `/health` com banco `up`, e a Vercel respondeu HTTP 200 em `/api/auth/csrf` (antes, 404). Login em produção confirmado com a conta de teste do operador (`479d774`). Catálogo, Meus empréstimos e a tag final seguem pendentes. |

Os oito testes de autenticação passaram no Docker, incluindo os casos de CSRF
ausente, inválido e antigo (`a932cce`) e a conferência de senha e hash fora
dos logs e respostas (`77dfd30`).

## 1. Conflitos: o que foi pedido contra o que ja existia

| pedido | contrariava o que | decisao do operador | por que | evidencia | commit |
|---|---|---|---|---|---|
| Verificador pode corrigir teste quando for a unica falha | Independencia do subagente e responsabilidade do agente principal | O verificador continua somente leitura e sempre devolve a falha | Quem verifica nao pode alterar a propria evidencia | `.codex/agents/spec-validator.toml`, `rules/checks.md` | `b753715` |
| Usar Pexels | Correcao posterior do operador em 2026-11-10: a fonte da pratica e Pixabay | Usar exclusivamente a Pixabay API | A decisao mais recente do operador corrige o material desatualizado | `.agents/skills/imagens-equipamentos/` | `ad2d9af` |
| Instalar `sharp` diretamente | `rules/restrictions.md` exige aprovacao para nova dependencia | Dependencia aprovada pelo operador, isolada na skill e fixada por lockfile | Garante 800x600 de forma reproduzivel sem contaminar o runtime da aplicacao | `scripts/package.json`, `scripts/pnpm-lock.yaml` | `ad2d9af` |
| Gravar a senha padrao no seed | `rules/secrets.md` e o principio de nao versionar credenciais | A senha chega por variavel de ambiente e e persistida como hash | Evita segredo em codigo e historico Git | `.env.example`, comando `backend-provision` e `UserProvisioningIntegrationTest` | `a5a6f1d` |
| Permitir 5 emprestimos abertos | `docs/PRD.md` limita a 3 | Manter o limite de 3 | O PRD continua sendo a fonte de regra de negocio | Spec 004 e V2 (`open_loan_count >= 3`) | `212f0f8` |
| Esconder todo credito no catalogo | Contrato da API exige indicar a origem | Exibir `Fotos: Pixabay` de forma discreta, sem rodape de card | Cumpre a fonte sem poluir cada item | Spec 004 (jornada) | `212f0f8` |
| Texto dizia dois itens em manutencao, mas a lista marcava tres | O proprio briefing era contraditorio | `TI-0004`, `TI-0007` e `TI-0010` ficam em manutencao | A lista declarada como exata e a decisao explicita do operador prevalecem | `references/equipment.json` | `ad2d9af` |

## 1b. Lacunas: o que foi pedido de forma incompleta

| spec | o que faltava | alternativas | decisao | por que | commit |
|---|---|---|---|---|---|
| Skill de imagens | Como impedir duplicidade e provar tamanho | Instrucao manual; validacao automatica | Validar IDs, hashes e dimensoes no script | Requisito verificavel nao deve depender de memoria | `ad2d9af` |
| Skill de imagens | Como repetir o upload sem criar novos objetos | Nome aleatorio; ID da foto; patrimonio | Usar `equipment/<patrimonio>.jpg` | O patrimonio e unico e gera uma chave estavel | `ad2d9af` |
| Skill de imagens | Como eliminar pessoa, marca, texto e tela | Apenas filtro da API; revisao humana | `safesearch` mais revisao visual bloqueante | A API nao prova os quatro requisitos visuais | `ad2d9af` |
| Empréstimo (Spec 004) | O PRD diz "14 dias" sem dizer se são corridos ou úteis; o briefing pedia 14 dias úteis | Dias corridos (a V2 já calcula `loaned_at + interval '14 days'`); dias úteis (exige calendário de feriados, função própria e nova migration substituindo o cálculo da V2) | 14 dias corridos, por decisão do operador | A V2 já calcula assim e o projeto não tem calendário de feriados | `0e6efea` |
| Catálogo (Spec 004) | O ADR-002 previa pessoa e devolução no card emprestado; o PRD diz que cada pessoa vê os próprios empréstimos | Pessoa e data para todos; "Emprestado a você" e data só ao titular; somente "Emprestado" para todos | Somente "Emprestado" para todas as contas, inclusive o titular; exceção registrada no ADR-002 e teste de privacidade CA-07 | Opção mais restrita: a API do catálogo não expõe dono nem datas, e o titular vê o vencimento em Meus empréstimos | `212f0f8` |
| Dados iniciais (Spec 003) | Onde e como a repetição da carga é provada | Só no teste; também em produção via `flyway migrate`; também em produção reexecutando o arquivo versionado | Testcontainers reexecuta a V4 do classpath; em produção o operador roda `psql --single-transaction -f` no mesmo arquivo versionado, sem `BEGIN/COMMIT` no arquivo | Prova a idempotência no banco real sem SQL avulso e sem alterar o checksum do Flyway | `a726f85` |
| Dados iniciais (Spec 003) | Onde fica a evidência de CA-02 a CA-05 | Só no ENTREGA.md; arquivos na pasta da spec | Teste em `backend/src/test/java/`; ações de produção em `verification/producao-<data>.md`; manifesto e aprovações em `verification/manifesto/` | A evidência fica junto da spec e pode ser comparada entre rodadas | `a726f85` |
| Catálogo (Spec 004) | O ADR-002 desenhava o card com ícone e sem foto, e não fixava o texto do card em manutenção | Card com foto como exceção só na spec; variante explícita no ADR-002. Para manutenção: texto da situação com previsão de retorno ou só o estado | Variante com foto registrada no ADR-002 (4:3, `--gradient-equipment`, “Foto indisponível”). Rodapé de manutenção somente “Em manutenção”, sem previsão | Spec e ADR não podem discordar, então registrar no ADR é mais barato. Não há dado de previsão de retorno para mostrar | `b0046fa` |
| Catálogo (Spec 004) | O CA-05 não provava o destino de `operacoes` após o login | Deixar para o plano; incluir no CA-05 | O CA-05 prova que `operacoes` entra, chega a `/catalogo` e vê o acesso negado | Deixa o critério observável | `b0046fa` |
| Dados iniciais (Spec 003) | Se o nome do revisor das fotos pode ir para o Git | Anonimizar; versionar nome e e-mail; versionar só o nome | Versionar só o nome, sem e-mail | É a evidência da revisão visual que o enunciado pede | `42acd95` |
| Dados iniciais (Spec 003) | Qual spec implementa `EQUIPMENT_IMAGE_BASE_URL` | Spec 003; Spec 004 | Spec 003 (T006, CA-04); a 004 só consome a URL | É a 003 que define o bucket e as chaves | `42acd95` |
| Login | Provisionamento, duracao de sessao, usuario inativo e tentativas incorretas | Registradas na Spec 001 | Provisionamento administrativo idempotente; sessao de 8 h por inatividade; inativo indistinguivel de credencial invalida; sem bloqueio na v1; conta de teste separada | Fecha as lacunas sem versionar segredo nem inventar regra de negocio | `052dc17` |
| Dados iniciais | Como repetir a carga e tratar patrimonio existente | Ignorar colisao, sobrescrever ou recusar divergencia | Aprovada na Spec 003. Preservar identidade e estado operacional, preencher chave ausente e recusar divergencia em transacao unica | Evita duplicatas e impede que a carga desfaça emprestimos ou troque fotos ja associadas | `a726f85`, `42acd95`; V4 aprovada pelo operador em 2026-11-11 com a lógica do rascunho e implementada em `a46cbd7` |

## 2. Onde o agente tentou decidir pelo operador

Na primeira versao da Spec 001, expressoes como "erro generico", "sessao expirada" e "provisionamento idempotente" deixavam contratos importantes para quem implementasse. O `spec-validator` bloqueou essa delegacao: a spec passou a definir status, corpos, cookies, semantica das 8 horas, variaveis e comportamento de repeticao. A escolha de produto continuou com o operador; o agente apenas transformou as respostas aprovadas em criterios observaveis.

Nas Specs 003 e 004, o agente voltou a avançar sem a decisão do operador. O cabeçalho da Spec 003 afirmava `APROVAVEL` numa rodada `pre-approval-03` com base `e0cbaca`. Essa rodada não existia: o relatório pertencia à Spec 001, e a base era anterior à própria spec. Além disso, `plan.md` e `tasks.md` foram escritos antes de uma rodada válida. A rodada `pre-approval-01` da Spec 003 bloqueou isso, e o cabeçalho foi corrigido (`a726f85`). Na Spec 004, o código local passou a usar uma das opções de privacidade do card antes da decisão. O verificador bloqueou a spec (rodada 05), e o operador escolheu outra opção.

Ao corrigir os bloqueios, o agente propôs detalhes que o operador não tinha pedido. Eles estão nas specs, que o operador aprovou em 2026-11-11. O contêiner 4:3 virou a variante com foto do ADR-002:
- O comando de repetição usa `-v ON_ERROR_STOP=1`, para parar na primeira falha.
- `image_object_key` fica sem CHECK e sem unicidade.
- O prefixo `equipment/` tem exatamente as 12 chaves.
- O contêiner da foto é 4:3 com `object-fit: cover`.
- Para `operacoes`, a página mostra "Acesso negado" e "Esta área é exclusiva de colaboradores.".
- O erro `FORBIDDEN` usa a mensagem "Acesso negado.".

Na implementação da T001 e da T002, o agente fez duas escolhas técnicas que o operador não pediu:
- O `docker compose run --rm backend-test` copia `backend/src` inteiro para a imagem, inclusive o código não rastreado da 004. Para o resultado refletir exatamente cada commit, a suíte rodou num git worktree limpo do commit, com o mesmo comando.
- A V4 usa `set image_object_key = excluded.image_object_key` no lugar de `coalesce(equipment.image_object_key, excluded.image_object_key)`, como estava no rascunho. As duas formas são equivalentes, porque o `where equipment.image_object_key is null` só atualiza linhas sem chave.

No ADR-003, o agente escreveu duas afirmações sobre o Railway sem fonte: que o serviço não é suspenso por inatividade e que o teste não exige cartão. O operador pediu para retirá-las e informou os limites do teste: 1 GB de memória, 30 dias e US$ 5 de crédito (`580dc2b`).

Na T006 da Spec 003 e no plano da Spec 004, o agente fez escolhas que aguardam a revisão do operador:
- `EquipmentImageUrls` ignora a barra final de `EQUIPMENT_IMAGE_BASE_URL`, como o `next.config.ts` já fazia (`d16a3e6`). A spec só diz que a base vem sem barra final.
- O plano da Spec 004 propõe responder 409 `EQUIPMENT_BORROWED` quando o índice `uq_loan_open_equipment` rejeitar uma inserção depois da leitura sob bloqueio, e 500 sem empréstimo para qualquer outra rejeição. A spec não define esse caso, e a proposta depende de aprovação (`fdee1f2`).

## 3. Uma decisao que merece ou nao um ADR

O operador escolheu o Railway para hospedar o backend Spring, registrado no
[ADR-003](docs/adr/003-hospedagem-backend.md) (`aa8e0b2`). O Railway faz o build do `Dockerfile` de
`backend/`, serve HTTPS e vira `INTERNAL_API_BASE_URL` na Vercel. Render gratuito
(dorme e demora a acordar), Fly.io e Cloud Run (exigem cartão) foram descartados.
O crédito limitado do Railway pode parar o backend depois do prazo. Nenhuma
credencial fica no repositório. A criação do serviço e o deploy seguem pendentes
e exigem autorização do operador no momento da ação.

## 4. A skill imagens-equipamentos

- `SKILL.md` guarda o fluxo e os limites de autoridade.
- `references/api-contract.md` guarda apenas o contrato da Pixabay que muda decisoes da skill.
- `references/equipment.json` guarda a lista uniforme da pratica.
- `scripts/prepare-images.mjs` garante cache, candidatos unicos, aprovacao visual explicita, 800x600, hashes unicos e manifesto de creditos.
- A skill nao possui cliente nem credencial do Supabase e nao consegue publicar. O operador recebe arquivos e chaves deterministicas para executar o upload.
- A consulta autenticada a Pixabay foi executada. Quatro fotos passaram na
  revisao visual local, para TI-0002, TI-0008, TI-0009 e TI-0010. Outros
  candidatos foram recusados por pessoa, marca, texto, tela com conteudo
  ou por mostrar um objeto diferente. O commit `9277a10` adiciona a busca
  em lotes, preservando as aprovacoes. O manifesto final e o upload aguardam
  as oito fotos restantes.

## 5. O subagente verificador

- O mesmo `spec-validator` atua antes da aprovacao e depois da implementacao; nao foi criado um segundo subagente.
- Cada rodada nasce em contexto limpo e le PRD, ADRs, rules, spec e, na verificacao, plano, tarefas, codigo, testes e resultados.
- O sandbox e `read-only`; as instrucoes proíbem corrigir codigo ou teste e qualquer acao de producao.
- A saida final usa uma tabela fixa por `CA-xx`, identificador da rodada e commit, permitindo comparacao.

## 6. O verificador em acao

A Spec 001 passou por tres contextos independentes antes de ser aprovada:

1. `pre-approval-01-c485d8a.md`: bloqueada por seis lacunas, incluindo contratos HTTP indefinidos, sessao ambigua e provisionamento incompleto.
2. `pre-approval-02-c485d8a.md`: bloqueada por tres lacunas residuais no envelope de sessao, rota de logout e repeticao do ciclo CSRF.
3. `pre-approval-03-c485d8a.md`: resultado `APROVAVEL`, sem bloqueio e sem criterio nao observavel.

Os relatorios estao em `docs/specs/001-login/verification/`. A spec aprovada foi publicada no commit `052dc17`. A verificacao pos-implementacao e a prova com erro plantado permanecem pendentes.

A Spec 003 foi bloqueada na rodada `pre-approval-01-94a8197`. O estado citava uma rodada inexistente, e o CA-02 não definia o ambiente nem o mecanismo da repetição. A rodada `pre-approval-02-94a8197` retornou `APROVAVEL`. A Spec 004 foi bloqueada na rodada `pre-approval-05-94a8197` por três motivos: spec divergente do ADR-002 no card emprestado, falta de contrato e de CA de privacidade, e estado contraditório. A rodada `pre-approval-06-94a8197` retornou `APROVAVEL`. Os relatórios estão nas pastas `verification/` de cada spec. Essas rodadas foram feitas por subagente Claude Code em contexto limpo, com as instruções do `spec-validator`, porque o Codex CLI não estava disponível.

Depois das respostas do operador às perguntas das rodadas, as duas specs mudaram e passaram por nova rodada: `pre-approval-03-8d40630` na Spec 003 e `pre-approval-07-8d40630` na Spec 004, ambas `APROVAVEL`. O operador aprovou as duas specs em 2026-11-11. Na 003, aprovou também `plan.md`, `tasks.md` e o DDL da V3 (`42acd95`, `b0046fa`).

### Quem executou as rodadas

As três rodadas da Spec 001 rodaram pelo Codex CLI, como registra o cabeçalho de cada relatório. O Codex CLI não estava instalado nesta máquina quando as Specs 003 e 004 foram validadas. Por isso, todas as rodadas dessas duas specs, inclusive a do `plan.md` e do `tasks.md` da 004, rodaram como subagentes Claude Code, com as instruções de `.codex/agents/spec-validator.toml`, em contexto limpo e somente leitura. O cabeçalho de cada relatório registra o executor.

### Rodadas que reprovaram algo

| Spec | Rodada | Resultado | CA | Causa | Commit |
| --- | --- | --- | --- | --- | --- |
| 001 | `pre-approval-01-c485d8a` | BLOQUEADA | CA-09; o resto foi de contrato, sem CA | Seis lacunas: contrato HTTP indefinido, ADR-002 não declarado, usuário inativo e HTTP-only sem aceite, expiração de sessão indefinida, provisionamento sem contrato e responsabilidade pelo schema | `052dc17` |
| 001 | `pre-approval-02-c485d8a` | BLOQUEADA | CA-05 e CA-10; o resto foi de contrato, sem CA | Envelope ambíguo de `GET /api/auth/session`, logout fora das rotas públicas e repetição do logout sem sequência CSRF | `052dc17` |
| 003 | `pre-approval-01-94a8197` | BLOQUEADA | CA-02; estado da spec, sem CA | O cabeçalho citava uma rodada `pre-approval-03` inexistente, e o CA-02 não definia ambiente, mecanismo nem executor da repetição da carga | `a726f85` |
| 004 | `pre-approval-05-94a8197` | BLOQUEADA | Card emprestado (originou o CA-07); estado da spec, sem CA | Spec divergente do ADR-002 no rodapé do card emprestado, falta de contrato e de CA de privacidade, e cabeçalho “confirmado” com uma decisão em aberto | `212f0f8` |
| 004 | `plan-01-c52ae79` (`plan.md` e `tasks.md`) | BLOQUEADA | CA-02, CA-03, CA-04 e CA-07 (tarefas de API); CA-05 | O plano atribuía ao ADR-001 o acesso por `JdbcTemplate` com SQL no service, mas o ADR-001 escolhe Spring Data JPA e descarta SQL em services. Além disso, conta desativada ou excluída não era detectada por `GET /api/auth/session` nem antes da checagem de papel nas rotas | `f8ef24b` |

A rodada `plan-02-ad56229` conferiu o `plan.md` e o `tasks.md` corrigidos e retornou `APROVAVEL`, sem bloqueios (`9be0306`). O acesso ao banco passou a seguir o ADR-001 (JPA/Hibernate). Um filtro de conta ativa, depois do CSRF e antes da autorização, passou a cobrir o CA-05. Sete perguntas do verificador aguardam a decisão do operador antes da implementação.

## 7. Uma regra que a pratica pos a prova

`rules/spec-flow.md` exigia o verificador apenas antes da aprovacao da spec. A pratica mostrou que isso nao garantia uma revisao independente da implementacao. O fluxo agora exige uma segunda rodada, registrada e somente leitura (`b753715`).

O código da Spec 004 foi escrito antes de a spec ser aprovada. São eles: `LoanService.java` e `LoanController.java` no backend, e `loan-journey.tsx`, `loan-api.ts` e as páginas `/catalogo` e `/meus-emprestimos` no frontend. Isso contrariou `rules/restrictions.md`, que proíbe escrever código de funcionalidade sem spec e antes de um plano aprovado. Comparado com a spec aprovada, o código diverge em três pontos:
- no CA-07, mostra “Emprestado a você” e a data só ao titular, a partir de `myDueAt`;
- no CA-05, a página de `operacoes` chama a API em vez de mostrar o acesso negado;
- nos rodapés do card exigidos pelo ADR-002.

Decisão do operador: não commitar nem apagar esse código, mantê-lo fora da `main` e reescrevê-lo pela spec aprovada, uma tarefa por commit. Cada tarefa aproveita só o trecho que atende ao seu CA, já corrigido. Motivo: pela precedência de `rules/restrictions.md`, quando código e spec discordam, a spec prevalece. Commitar o código como estava levaria para a `main` um comportamento que a spec proíbe. O mapeamento por tarefa está no plano (`fdee1f2`), e a rodada que bloqueou o plano está em `f8ef24b`.

## 8. O que ficou de fora e o que faria diferente

- O login, a sessao e o logout passaram localmente via Next.js, Spring e PostgreSQL descartavel. O commit `e0cbaca` publica a tela e o encaminhamento same-origin. O fluxo ainda precisa da revisao interativa e da verificacao independente final.
- A Vercel serve a tela de login com HTTP 200. Em 2026-11-11, `/api/auth/csrf` passou a responder HTTP 200, encaminhado ao backend no Railway; antes respondia 404 (`479d774`).
- A carga inicial dos 12 itens está em produção desde 2026-11-11. O catálogo, a solicitação e Meus empréstimos ainda não estão no link (ver abaixo).
- A chave da Pixabay esta configurada localmente. O arquivo `.env` esta ignorado pelo Git.
- Em 2026-11-11, o primeiro deploy do backend aplicou V1 a V4 no Supabase de produção. O operador provisionou a conta de avaliação e a sua conta de teste, as duas com 0 empréstimos. Nenhuma imagem foi enviada ao Storage. A evidência está em `docs/specs/003-dados-iniciais/verification/producao-2026-11-11.md` (`479d774`).
- A imagem final do `backend/Dockerfile`, construída a partir de `7c4a10b`, foi validada localmente em 2026-11-11, com limite de 1 GB e um PostgreSQL descartável. O Flyway aplicou V1 a V4 e carregou os 12 itens. O Spring subiu em 11,6 s, e o `/health` respondeu HTTP 200 com banco `up` em 14,8 s após o `docker run`. O uso de memória foi de cerca de 270 MiB, e o cookie saiu com `Secure` com `SESSION_COOKIE_SECURE=true`.
- A suíte do backend passou com 31 testes em Docker e PostgreSQL descartável (`d16a3e6`). O frontend passou em `pnpm run typecheck` e `pnpm run build`. A verificacao final da pratica precisa da matriz no link de producao.

### O que não foi feito

| Item | Motivo | Próximo passo |
| --- | --- | --- |
| Catálogo e Meus empréstimos no link de produção | O código local da Spec 004 foi escrito antes da spec aprovada e diverge dela, no CA-07 (`myDueAt` e “Emprestado a você” só para o titular) e no CA-05 (portão das páginas para `operacoes`). O `plan.md` e o `tasks.md` da 004 foram bloqueados na rodada `plan-01-c52ae79` (`f8ef24b`). Depois da correção, a rodada `plan-02-ad56229` retornou `APROVAVEL` (`9be0306`). | O operador responde às sete perguntas da `plan-02` e aprova o plano. Depois, implementar T001 a T009, uma tarefa por commit, e publicar com autorização do operador. |
| 8 das 12 fotos e nada no Storage | Só 4 candidatos passaram na revisão visual (TI-0002, TI-0008, TI-0009 e TI-0010). Os outros mostravam pessoa, marca, texto, tela com conteúdo ou outro objeto. Sem as 12 fotos não há manifesto final, e sem manifesto o operador não faz o upload. | Revisar novos lotes com `search-next`, aprovar as 8 fotos restantes, rodar `finalize` e copiar o manifesto para `verification/manifesto/` (Spec 003, T003). O operador faz o upload, e a evidência do CA-03 e do CA-04 entra em `producao-<data>.md` (T004). |
| Rodadas pós-implementação do verificador | Nenhuma spec tem rodada em modo verificação. A Spec 001 ainda aguarda a conferência de teclado, foco e alertas no navegador. A Spec 003 tem T003, T004 e T005 em aberto. A Spec 004 não foi implementada. | Rodar o `spec-validator` em modo verificação ao concluir cada spec e registrar a tabela em `verification/`, como manda `rules/spec-flow.md`. |
| Login com a conta do professor | O login em produção foi confirmado com a conta de teste do operador, para não arriscar um empréstimo na conta do professor, que precisa chegar à correção com zero empréstimos. O CA-05 da Spec 003 fala em confirmar o login “sem solicitar item nessa conta”, e isso pode exigir a conta do professor. | O operador decide se o login com a conta do professor, sem solicitar nada, é exigido. No dia da entrega, refazer a consulta de leitura com zero empréstimos e registrar em `verification/`. |
