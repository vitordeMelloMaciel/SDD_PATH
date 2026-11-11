# Spec 004 — Catálogo e empréstimos

**Estado** · Rascunho para revisão e aprovação.

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
  `imageUrl` é `null` e o card mostra “Foto indisponível”.
- `GET /api/loans/me` retorna HTTP 200 com uma lista dos empréstimos abertos
  do principal da sessão, ordenada por vencimento e ID. Cada item tem
  `id`, `assetCode`, `equipmentName`, `dueAt` (ISO 8601 com offset) e
  `overdue` (booleano, verdadeiro quando o vencimento passou).
- `POST /api/loans` recebe `equipmentId` e exige CSRF. Retorna 201 com
  a mesma representação de Meus empréstimos. A identidade do solicitante
  vem da sessão. O banco determina a data e garante as regras concorrentes.
- Os GETs retornam 401 sem sessão e 403 para papel sem permissão. O POST
  retorna 403 `CSRF_INVALID` quando falta um token válido, mesmo se também
  faltar sessão. Com token válido, retorna 401 sem sessão e 403 para papel
  sem permissão, conforme o envelope de erros da Spec 001.
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
- Proposta para aprovação: esta jornada é exclusiva de colaboradores.
  Exibir datas em `America/Sao_Paulo`, com horário de vencimento baseado
  no instante registrado pelo banco.

## Critérios de aceitação

| ID | Evidência esperada |
| --- | --- |
| CA-01 | Conta de teste entra pelo Next.js e vê os 12 itens com fotos, crédito único e situação correta. Somente disponíveis podem ser solicitados. |
| CA-02 | Solicitação válida retorna 201. No PostgreSQL, `due_at` equivale a `loaned_at + interval '14 days'`. O item muda de situação no catálogo e aparece em Meus empréstimos com a data no fuso aprovado. |
| CA-03 | Manutenção, item emprestado, limite de três e atraso geram recusas distinguíveis e não criam empréstimo. |
| CA-04 | Duas solicitações concorrentes ao mesmo item em PostgreSQL real produzem um 201, um 409 e um empréstimo aberto. |
| CA-05 | Meus empréstimos exclui itens encerrados e de outras contas. Cada rota prova 401 sem sessão e 403 para papel sem permissão. O POST prova 403 com CSRF ausente e inválido, com e sem sessão. |
| CA-06 | O operador valida a matriz da prática em produção com conta de teste, incluindo atualização do catálogo, Meus empréstimos e recusa no limite. A conta do professor continua com zero empréstimos abertos ou encerrados na entrega. |

Fora de escopo: aprovação por Operações, fila, devolução, reserva,
notificação, renovação e cadastro de equipamentos.
