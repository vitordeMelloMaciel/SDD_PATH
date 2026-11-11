# Entrega da pratica

Documento progressivo. Itens ainda nao executados permanecem marcados como pendentes; nenhuma afirmacao de producao e feita sem evidencia no link publicado.

## Links

- Repositorio: <https://github.com/vitordeMelloMaciel/SDD_PATH>
- Producao atual: <https://sdd-path-pi.vercel.app/>
- Branch de trabalho: `main` (fluxo direto aprovado pelo operador, com checks e commits separados)

## Estado para a correção

| Requisito do HTML | Estado e evidência |
| --- | --- |
| Subagente verificador | Configuração somente leitura em `.codex/agents/spec-validator.toml`; três rodadas de pré-aprovação registradas na Spec 001. A verificação final e a prova com erro plantado seguem pendentes. |
| Skill de imagens | API da Pixabay e busca em lotes executadas. Quatro fotos aprovadas, oito pendentes. Commits `ad2d9af` e `9277a10`. |
| Dados iniciais | Os 12 equipamentos conferem com a lista exata da prática. Spec 003 aprovada e plano versionado. Migration, manifesto completo, upload e conta de avaliação em produção pendentes. |
| Solicitação e Meus empréstimos | A migration V2 já protege o limite, atraso e disponibilidade no banco. A Spec 004 aguarda a decisão sobre privacidade do card. Catálogo, API e jornada da prática pendentes. |
| Entrega e link de produção | Tela de login publicada. Fluxo local validado com conta descartável. A API pública ainda retorna 404, por isso a jornada de produção e a tag final seguem pendentes. |

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
| Permitir 5 emprestimos abertos | `docs/PRD.md` limita a 3 | Manter o limite de 3 | O PRD continua sendo a fonte de regra de negocio | Spec de emprestimo ainda pendente | pendente |
| Esconder todo credito no catalogo | Contrato da API exige indicar a origem | Exibir `Fotos: Pixabay` de forma discreta, sem rodape de card | Cumpre a fonte sem poluir cada item | Spec de catalogo ainda pendente | pendente |
| Texto dizia dois itens em manutencao, mas a lista marcava tres | O proprio briefing era contraditorio | `TI-0004`, `TI-0007` e `TI-0010` ficam em manutencao | A lista declarada como exata e a decisao explicita do operador prevalecem | `references/equipment.json` | `ad2d9af` |

## 1b. Lacunas: o que foi pedido de forma incompleta

| spec | o que faltava | alternativas | decisao | por que | commit |
|---|---|---|---|---|---|
| Skill de imagens | Como impedir duplicidade e provar tamanho | Instrucao manual; validacao automatica | Validar IDs, hashes e dimensoes no script | Requisito verificavel nao deve depender de memoria | `ad2d9af` |
| Skill de imagens | Como repetir o upload sem criar novos objetos | Nome aleatorio; ID da foto; patrimonio | Usar `equipment/<patrimonio>.jpg` | O patrimonio e unico e gera uma chave estavel | `ad2d9af` |
| Skill de imagens | Como eliminar pessoa, marca, texto e tela | Apenas filtro da API; revisao humana | `safesearch` mais revisao visual bloqueante | A API nao prova os quatro requisitos visuais | `ad2d9af` |
| Empréstimo (Spec 004) | O PRD diz "14 dias" sem dizer se são corridos ou úteis; o briefing pedia 14 dias úteis | Dias corridos (a V2 já calcula `loaned_at + interval '14 days'`); dias úteis (exige calendário de feriados, função própria e nova migration substituindo o cálculo da V2) | 14 dias corridos, por decisão do operador | A V2 já calcula assim e o projeto não tem calendário de feriados | Spec 004, CA-02 |
| Login | Provisionamento, duracao de sessao, usuario inativo e tentativas incorretas | Registradas na Spec 001 | Provisionamento administrativo idempotente; sessao de 8 h por inatividade; inativo indistinguivel de credencial invalida; sem bloqueio na v1; conta de teste separada | Fecha as lacunas sem versionar segredo nem inventar regra de negocio | `052dc17` |
| Dados iniciais | Como repetir a carga e tratar patrimonio existente | Ignorar colisao, sobrescrever ou recusar divergencia | Proposta na Spec 003. Preservar identidade e estado operacional, preencher chave ausente e recusar divergencia em transacao unica | Evita duplicatas e impede que a carga desfaça emprestimos ou troque fotos ja associadas | Rascunho em `docs/specs/003-dados-iniciais/spec.md` |

## 2. Onde o agente tentou decidir pelo operador

Na primeira versao da Spec 001, expressoes como "erro generico", "sessao expirada" e "provisionamento idempotente" deixavam contratos importantes para quem implementasse. O `spec-validator` bloqueou essa delegacao: a spec passou a definir status, corpos, cookies, semantica das 8 horas, variaveis e comportamento de repeticao. A escolha de produto continuou com o operador; o agente apenas transformou as respostas aprovadas em criterios observaveis.

## 3. Uma decisao que merece ou nao um ADR

Pendente. A adocao do Supabase Storage veio como requisito da pratica; a hospedagem do backend Spring em producao ainda e uma decisao arquitetural aberta e sera avaliada antes do deploy.

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

## 7. Uma regra que a pratica pos a prova

`rules/spec-flow.md` exigia o verificador apenas antes da aprovacao da spec. A pratica mostrou que isso nao garantia uma revisao independente da implementacao. O fluxo agora exige uma segunda rodada, registrada e somente leitura (`b753715`).

## 8. O que ficou de fora e o que faria diferente

- O login, a sessao e o logout passaram localmente via Next.js, Spring e PostgreSQL descartavel. O commit `e0cbaca` publica a tela e o encaminhamento same-origin. O fluxo ainda precisa da revisao interativa e da verificacao independente final.
- A Vercel serve a tela de login com HTTP 200. A rota CSRF retorna HTTP 404 enquanto o frontend de producao aguarda configuracao do backend Spring.
- A carga inicial, o catalogo, a solicitacao e Meus emprestimos ainda estao pendentes.
- A chave da Pixabay esta configurada localmente. O arquivo `.env` esta ignorado pelo Git.
- Nenhuma migration, imagem ou conta foi aplicada ao Supabase de producao.
- A suite do backend passou com 15 testes em Docker e PostgreSQL descartavel. O frontend passou em `pnpm run typecheck` e `pnpm run build`. A verificacao final da pratica precisa da matriz no link de producao.
