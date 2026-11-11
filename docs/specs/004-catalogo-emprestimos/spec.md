# Spec 004 — Catálogo e empréstimos

**Estado** · Em revisão de pré-aprovação. A rodada `pre-approval-05-94a8197`
retornou `BLOQUEADA`; os bloqueios foram tratados nesta versão e aguardam nova
rodada. Decisões do operador incorporadas em 2026-11-11: privacidade do card
emprestado e prazo em dias corridos. Ainda não há `plan.md` nem `tasks.md`.

**Referências** · PRD itens 1–3, ADRs 001 e 002, Specs 001–003 e briefing
de solicitação da prática.

## Objetivo

Permitir que o colaborador autenticado consulte o acervo, solicite um item
disponível e acompanhe os próprios empréstimos abertos, com as regras de
limite, prazo e atraso garantidas pelo banco.

## Pré-requisitos

- Spec 001 implementada: sessão, CSRF e envelope de erro.
- Spec 002: tabelas `app_user`, `equipment` e `loan`, com trigger e índices da V2.
- Spec 003 implementada para CA-01 e CA-06: V3 (`image_object_key`), V4
  (carga dos 12 itens), fotos no Storage e `EQUIPMENT_IMAGE_BASE_URL`.

## Jornada

Após login bem-sucedido, a página de login leva a `/catalogo`. O catálogo
mostra os 12 equipamentos iniciais com foto, patrimônio, nome, categoria e
situação. Itens em manutenção e emprestados aparecem com sua situação e sem
ação de solicitar. O crédito único “Fotos: Pixabay” leva à Pixabay. Meus
empréstimos (`/meus-emprestimos`) mostra somente os próprios empréstimos
abertos e a data prevista de devolução.

Solicitar um item disponível cria um empréstimo imediatamente. A situação do
equipamento muda para emprestado pela trigger
`trg_sync_equipment_status_from_loan` da V2; a API não atualiza `equipment`
diretamente. O limite é de três empréstimos abertos (PRD). O prazo é de
14 dias corridos: o PRD diz apenas “14 dias”, e o operador decidiu pela
contagem corrida, coerente com a Spec 002 e com a V2
(`loaned_at + interval '14 days'`). Quem tem item em atraso recebe uma recusa.
Duas solicitações simultâneas para o mesmo patrimônio deixam exatamente um
empréstimo aberto.

O briefing de solicitação pedia cinco empréstimos e 14 dias úteis. O operador
manteve três empréstimos e 14 dias corridos (ENTREGA.md, seções 1 e 1b).

## Contrato proposto

- `GET /api/equipment` retorna HTTP 200 com uma lista de todo o acervo,
  incluindo os 12 itens da Spec 003, ordenada por patrimônio. Cada item
  contém exatamente os campos `id`, `assetCode`, `name`, `category`, `status`
  e `imageUrl`, para qualquer conta e qualquer situação. A resposta nunca
  contém identificação de quem tomou o item, data de retirada, vencimento,
  ID do empréstimo nem indicação de que o empréstimo pertence à conta da sessão.
  O status usa os valores do banco `disponivel`, `manutencao` e `emprestado`.
  A URL combina `EQUIPMENT_IMAGE_BASE_URL` e a chave da Spec 003. Sem chave ou
  base, `imageUrl` é `null`. O navegador usa o otimizador de imagens do Next.js
  na mesma origem. O Next.js aceita somente a origem e o caminho público do
  bucket da Spec 003.
- `GET /api/loans/me` retorna HTTP 200 com uma lista dos empréstimos abertos
  do principal da sessão, ordenada por vencimento e ID. Cada item tem
  `id`, `assetCode`, `equipmentName`, `dueAt` (ISO 8601 com offset) e
  `overdue` (booleano, verdadeiro quando o vencimento passou).
- `POST /api/loans` recebe o campo obrigatório `equipmentId` como UUID em JSON
  e exige CSRF. Pelo proxy do Next.js, a página solicita o token em
  `GET /api/auth/csrf` e o envia em `X-XSRF-TOKEN`. Retorna 201 com
  a mesma representação de Meus empréstimos. A identidade do solicitante
  vem da sessão. O banco determina a data e garante as regras concorrentes.
- Os GETs retornam 401 `UNAUTHENTICATED` sem sessão e 403 `FORBIDDEN`
  para papel sem permissão. O POST
  retorna 403 `CSRF_INVALID` quando falta um token válido, mesmo se também
  faltar sessão. Com token válido, retorna 401 `UNAUTHENTICATED` sem sessão e
  403 `FORBIDDEN` para papel sem permissão. Esta spec define o código
  `FORBIDDEN` com a mensagem “Acesso negado.”, no envelope `{code, message}`
  da Spec 001.
  Nas três rotas, se o usuário da sessão foi desativado ou excluído, a API
  retorna 401 `UNAUTHENTICATED` e encerra a sessão. No POST a proteção
  CSRF continua sendo avaliada primeiro.
- Erros de negócio usam `{code, message}`. Corpo inválido retorna 400
  `INVALID_REQUEST` (“Informe um equipamento válido.”). Equipamento
  inexistente retorna 404 `EQUIPMENT_NOT_FOUND` (“Equipamento não encontrado.”).
  Os conflitos 409 seguem a ordem da V2:
  `EQUIPMENT_MAINTENANCE` (“Equipamento em manutenção.”),
  `EQUIPMENT_BORROWED` (“Equipamento já emprestado.”),
  `LOAN_LIMIT` (“Você já tem três empréstimos em aberto.”) ou
  `OVERDUE_LOAN` (“Você tem um empréstimo em atraso.”).
- A solicitação verifica o estado sob bloqueios de linha na ordem usuário
  e equipamento, na mesma transação da inserção, igual à ordem da trigger
  da V2. A segunda solicitação lê o estado após a primeira transação.
  A trigger e o índice da V2 mantêm as restrições no banco. Os motivos da
  API não dependem de texto de exceção do PostgreSQL.
- Esta jornada é exclusiva de colaboradores. O perfil `operacoes` não
  acessa as três rotas nem as páginas da jornada. Exibir datas em
  `America/Sao_Paulo`, com o vencimento baseado no instante registrado
  pelo banco.
- Catálogo e Meus empréstimos consultam a sessão no servidor antes de
  renderizar. Sem sessão, inclusive se a conta foi desativada ou excluída,
  redirecionam para `/`. Para `operacoes`, mostram somente o título
  “Acesso negado” e o texto “Esta área é exclusiva de colaboradores.”, sem
  lista de equipamentos ou empréstimos e sem chamar as três rotas da API.
  Após o login, `operacoes` também é levado a `/catalogo` e vê esse estado;
  a tela de Operações está fora desta versão.

## Card de equipamento

- O contêiner da foto tem proporção 4:3 em todos os cards. A foto preenche
  o contêiner sem distorção (`object-fit: cover`). Para `imageUrl` nulo ou
  falha de carregamento, o mesmo contêiner mostra “Foto indisponível”.
- Disponível: rodapé “Solicitar →”, conforme ADR-002.
- Manutenção: sem ação; rodapé informa a situação.
- Emprestado: sem ação; rodapé mostra somente “Emprestado”, para todas as
  contas, inclusive a do titular. O card não mostra pessoa nem data de
  devolução. O titular consulta o vencimento em Meus empréstimos. Esta é a
  exceção de privacidade registrada no ADR-002 por decisão do operador.

## Contrato afetado

- Banco: nenhuma migration nova nesta spec. Usa V2 e as V3/V4 da Spec 003.
- Backend: rotas `GET /api/equipment`, `GET /api/loans/me` e `POST /api/loans`,
  autorização por papel e tratamento de conflitos.
- Frontend: páginas `/catalogo` e `/meus-emprestimos`, redirecionamento após
  o login e card de equipamento conforme a seção acima.
- Testes: integração HTTP com Testcontainers para CA-02 a CA-05 e CA-07;
  conferência no navegador para CA-01 e CA-07; roteiro do operador para CA-06.

## Critérios de aceitação

| ID | Evidência esperada |
| --- | --- |
| CA-01 | Conta de teste entra pelo Next.js, é levada a `/catalogo` e vê os 12 itens com situação correta. As 12 fotos carregam em contêiner 4:3 com `object-fit: cover`, e o crédito único leva à Pixabay. Foto ausente ou falha mostra “Foto indisponível” no mesmo contêiner. Somente disponíveis têm ação de solicitar. |
| CA-02 | Solicitação válida retorna 201. No PostgreSQL, `due_at` equivale a `loaned_at + interval '14 days'`. O item muda de situação no catálogo e aparece em Meus empréstimos com a data no fuso aprovado. |
| CA-03 | As quatro recusas 409 retornam os códigos acima, mostram o motivo na interface e não inserem empréstimo. JSON inválido ou UUID ausente/inválido retorna 400. UUID válido inexistente retorna 404. |
| CA-04 | Em PostgreSQL real, duas solicitações concorrentes ao mesmo item produzem um 201, um 409 `EQUIPMENT_BORROWED` e um empréstimo aberto. Com dois empréstimos já abertos na mesma conta, duas solicitações concorrentes a itens distintos produzem um 201, um 409 `LOAN_LIMIT` e três empréstimos abertos ao final. |
| CA-05 | Meus empréstimos exclui itens encerrados e de outras contas. Cada rota prova 401 sem sessão e 403 `FORBIDDEN` (“Acesso negado.”) para `operacoes`. Acesso direto às duas páginas sem sessão redireciona a `/`. Para `operacoes`, as páginas mostram “Acesso negado” e “Esta área é exclusiva de colaboradores.”, sem lista e sem chamadas às rotas da API. Os testes do POST usam CSRF válido para provar 401 ou 403 de autorização. Token ausente ou inválido retorna 403 `CSRF_INVALID` com e sem sessão. Para conta desativada ou excluída, cada rota rejeita a sessão, as páginas redirecionam, não é criado empréstimo e uma consulta posterior com o cookie antigo retorna 401. |
| CA-06 | O operador entra em produção com conta de teste, confere as 12 fotos e a ausência da ação de solicitar em itens em manutenção, solicita item disponível, confirma status emprestado e data em Meus empréstimos e prova recusa ao atingir três. Uma consulta de leitura confirma zero empréstimos abertos ou encerrados para o professor na entrega. |
| CA-07 | Com a conta A titular de um empréstimo aberto e a conta B sem empréstimos, `GET /api/equipment` devolve para A e para B o mesmo objeto para o item emprestado. O objeto tem exatamente os seis campos do contrato, com `status` `emprestado`. Nenhum valor da resposta contém e-mail, nome, ID de usuário, ID de empréstimo, `loaned_at` ou `due_at` de A. No navegador, o card desse item mostra apenas “Emprestado” para A e para B, sem nome e sem data. |

## Riscos conhecidos

- Como a devolução está fora de escopo, os empréstimos criados pela conta de
  teste no CA-06 continuam abertos em produção. A conta do professor não é
  usada para isso.

Fora de escopo: aprovação por Operações, tela de Operações, fila, devolução,
reserva, notificação, renovação, busca e filtros do catálogo e cadastro de
equipamentos.
