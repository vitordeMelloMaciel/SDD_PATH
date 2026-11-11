# Spec 004 — Catálogo e empréstimos

**Estado** · Decisões confirmadas pelo operador. Verificação final de pré-aprovação pendente.

**Referências** · PRD itens 1–3, ADRs 001 e 002, Specs 001–003 e briefing
de solicitação da prática.

## Jornada

O colaborador autenticado abre o catálogo após o login. O catálogo mostra
os 12 equipamentos iniciais com foto, patrimônio, nome, categoria e situação.
Itens em manutenção e emprestados aparecem com sua situação e sem ação de
solicitar. O crédito único “Fotos: Pixabay” leva à Pixabay. Meus empréstimos
mostra somente os próprios empréstimos abertos e a data prevista de devolução.

Solicitar um item disponível cria um empréstimo imediatamente e muda a
situação para emprestado. O limite é de três empréstimos abertos e o prazo
é de 14 dias corridos, conforme PRD e migration V2. Quem tem item em atraso
recebe uma recusa. Duas solicitações simultâneas para o mesmo patrimônio
deixam exatamente um empréstimo aberto.

## Contrato proposto

- `GET /api/equipment` retorna HTTP 200 com uma lista de todo o acervo,
  incluindo os 12 itens da Spec 003, ordenada por patrimônio. Cada item
  contém `id`, `assetCode`, `name`, `category`, `status` e `imageUrl`.
  O status usa os valores do banco `disponivel`, `manutencao` e `emprestado`.
  A URL combina a base pública e a chave da Spec 003. Sem chave ou base,
  `imageUrl` é `null`. Para URL ausente ou falha de carregamento, o card
  mostra “Foto indisponível” no mesmo espaço da foto. O navegador usa o
  otimizador de imagens do Next.js na mesma origem. O Next.js aceita somente
  a origem e o caminho público do bucket da Spec 003.
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
  403 `FORBIDDEN` para papel sem permissão, conforme o envelope da Spec 001.
  Se o usuário da sessão deixou de estar ativo ou foi excluído, a solicitação
  retorna 401 `UNAUTHENTICATED` e encerra a sessão.
- Erros de negócio usam `{code, message}`. Corpo inválido retorna 400
  `INVALID_REQUEST` (“Informe um equipamento válido.”). Equipamento
  inexistente retorna 404 `EQUIPMENT_NOT_FOUND` (“Equipamento não encontrado.”).
  Os conflitos 409 seguem a ordem da V2:
  `EQUIPMENT_MAINTENANCE` (“Equipamento em manutenção.”),
  `EQUIPMENT_BORROWED` (“Equipamento já emprestado.”),
  `LOAN_LIMIT` (“Você já tem três empréstimos em aberto.”) ou
  `OVERDUE_LOAN` (“Você tem um empréstimo em atraso.”).
- A solicitação verifica o estado sob bloqueios de linha na ordem usuário
  e equipamento, na mesma transação da inserção. A segunda solicitação lê
  o estado após a primeira transação. A trigger e o índice da V2 mantêm
  as restrições no banco. Os motivos da API não dependem de texto de exceção
  do PostgreSQL.
- Esta jornada é exclusiva de colaboradores. O perfil `operacoes` não
  acessa as três rotas nem as páginas da jornada. Exibir datas em
  `America/Sao_Paulo`, com o vencimento baseado no instante registrado
  pelo banco.

## Critérios de aceitação

| ID | Evidência esperada |
| --- | --- |
| CA-01 | Conta de teste entra pelo Next.js e vê os 12 itens com situação correta. As 12 fotos carregam com proporção visual uniforme e o crédito único leva à Pixabay. Foto ausente ou falha mostra o estado alternativo. Somente disponíveis podem ser solicitados. |
| CA-02 | Solicitação válida retorna 201. No PostgreSQL, `due_at` equivale a `loaned_at + interval '14 days'`. O item muda de situação no catálogo e aparece em Meus empréstimos com a data no fuso aprovado. |
| CA-03 | As quatro recusas 409 retornam os códigos acima, mostram o motivo na interface e não inserem empréstimo. JSON inválido ou UUID ausente/inválido retorna 400. UUID válido inexistente retorna 404. |
| CA-04 | Em PostgreSQL real, duas solicitações concorrentes ao mesmo item produzem um 201, um 409 `EQUIPMENT_BORROWED` e um empréstimo aberto. Com dois empréstimos já abertos na mesma conta, duas solicitações concorrentes a itens distintos produzem um 201, um 409 `LOAN_LIMIT` e três empréstimos abertos ao final. |
| CA-05 | Meus empréstimos exclui itens encerrados e de outras contas. Cada rota prova 401 sem sessão e 403 para papel sem permissão. Os testes do POST usam CSRF válido para provar 401 ou 403 de autorização. Token ausente ou inválido retorna 403 `CSRF_INVALID` com e sem sessão. Conta desativada durante a sessão não cria empréstimo. |
| CA-06 | O operador entra em produção com conta de teste, confere as 12 fotos, tenta solicitar item em manutenção, solicita item disponível, confirma status emprestado e data em Meus empréstimos e prova recusa ao atingir três. Uma consulta de leitura confirma zero empréstimos abertos ou encerrados para o professor na entrega. |

Fora de escopo: aprovação por Operações, fila, devolução, reserva,
notificação, renovação e cadastro de equipamentos.
