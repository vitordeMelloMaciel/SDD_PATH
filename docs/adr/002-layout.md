# ADR-002 - Layout da area de Operacoes

**Status:** aceito

**Decisao do operador:** a segunda decisao arquitetural registra o layout do produto

**Contexto:** `docs/PRD.md`, tela de Operacoes

## Contexto

A equipe de Operacoes precisa consultar inventario, emprestimos em aberto e itens que exigem atencao durante o trabalho diario. A interface deve favorecer leitura rapida, uso repetido e acesso em telas menores, sem assumir que as funcionalidades do backend ja estejam prontas.

## Decisao

- A area autenticada usa navegacao lateral persistente em desktop e recolhivel em telas pequenas.
- A visao geral prioriza resumo do inventario, lista pesquisavel de equipamentos, pendencias e movimentacao recente.
- Estados de equipamento usam texto e cor: `disponivel`, `emprestado` e `manutencao`.
- A acao primaria da tela e `Novo emprestimo`, apresentada no cabecalho e aberta em dialogo.
- O visual e operacional e compacto: hierarquia tipografica contida, cantos discretos e densidade adequada para consulta frequente.
- Enquanto nao existir spec funcional aprovada, dados, filtros, dialogos e confirmacoes dessa tela sao apenas prototipo local, sem persistencia nem chamada de API de produto.
- A interface deve manter navegacao por teclado, rotulos acessiveis e reorganizacao responsiva sem sobreposicao.

## Alternativas descartadas

- Landing page de apresentacao: nao atende o fluxo diario de Operacoes.
- Navegacao composta apenas por cards: reduz a densidade e torna a consulta repetida mais lenta.
- Layout exclusivo para desktop: impede verificacao e uso eventual em telas menores.
- Conectar o prototipo diretamente ao banco: viola a separacao definida no ADR-001 e antecipa contratos ainda nao especificados.

## Consequencias

- O frontend pode evoluir visualmente sem representar funcionalidade concluida.
- Integracao com login, catalogo e emprestimos continua dependente das respectivas specs, planos e testes.
- Mudancas amplas na navegacao ou na hierarquia principal exigem atualizar este ADR depois de nova decisao do time.
