# Spec 002 - Modelo de dados inicial

**Status:** implementada e validada por migration e testes de integracao

**Vinculo:** `docs/PRD.md`, itens 1 a 5

**ADR aplicavel:** `docs/adr/001-stack.md`

## Objetivo

Criar por migration Flyway o modelo relacional minimo para usuarios, equipamentos e emprestimos da primeira versao.

## Regras

- Usuario possui nome, e-mail normalizado, hash de senha, papel e estado ativo.
- Papel aceita somente `colaborador` ou `operacoes`.
- Equipamento possui identificador patrimonial, nome, categoria e situacao.
- Situacao aceita somente `disponivel`, `emprestado` ou `manutencao`.
- Um equipamento possui no maximo um emprestimo em aberto.
- Uma pessoa possui no maximo tres emprestimos em aberto.
- Pessoa com emprestimo em aberto cujo prazo ja passou nao inicia outro emprestimo.
- O prazo padrao e 14 dias a partir da retirada.
- A devolucao registra quando ocorreu e qual usuario a registrou.
- Abrir e devolver um emprestimo atualiza a situacao do equipamento.

## Criterios de aceitacao

- **CA-01:** banco vazio aplica V1 e V2 e cria `app_user`, `equipment` e `loan`.
- **CA-02:** e-mail e identificador patrimonial sao unicos.
- **CA-03:** papeis e situacoes fora dos valores definidos sao rejeitados.
- **CA-04:** o banco rejeita um quarto emprestimo aberto para a mesma pessoa.
- **CA-05:** o banco rejeita novo emprestimo para pessoa com item em atraso.
- **CA-06:** o banco impede dois emprestimos abertos do mesmo equipamento.
- **CA-07:** retirada e devolucao mantem a situacao do equipamento consistente.

## Fora de escopo

- Endpoints, entidades JPA, seed e telas conectadas.
- Sessao HTTP e politica de provisionamento dos primeiros usuarios.
- Reserva futura, notificacao e importacao da planilha.
- Aplicacao da migration em banco remoto.

## Contrato afetado

- Banco: migrations Flyway V1 e V2 criam e protegem as invariantes de `app_user`, `equipment` e `loan`.
- Backend: passa a poder mapear o modelo, mas endpoints e entidades JPA continuam fora desta spec.
- Frontend: nenhum contrato funcional; os dados exibidos permanecem demonstrativos.
- Testes: PostgreSQL descartavel deve aplicar todas as migrations e rejeitar violacoes de CA-02 a CA-07.

## Decisoes de modelagem

- Identificadores usam UUID gerado pelo PostgreSQL.
- E-mail e salvo em minusculas e sem espacos nas pontas; a constraint evita duplicidade por caixa.
- Um item esta atrasado quando `due_at` e anterior ao instante atual e `returned_at` continua nulo.
- As invariantes concorrentes ficam no PostgreSQL por indice parcial e triggers; a API ainda devera traduzir os erros para respostas de dominio.
