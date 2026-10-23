---
description: Como encerrar uma tarefa ou sessao sem perder o estado
globs: []
alwaysApply: false
---

# Handoff
> leitor: agente

## Quando
Ao terminar uma tarefa do plano ou quando o operador disser "vamos fechar".

## Procedimento
Sobrescreva `handoff.md` na raiz com no maximo 25 linhas:
1. tarefa e criterio em andamento;
2. branch e ultimo commit;
3. alteracoes locais ainda nao commitadas;
4. estado do banco local e migrations aplicadas;
5. o que ja foi publicado;
6. o que nao fazer na proxima sessao e o proximo passo.

## Verificacao
Uma nova sessao continua lendo apenas `handoff.md`, a spec e os ADRs citados.

## Nao faca
- Nao omita mudanca local, migration aplicada ou publicacao ainda nao verificada.
- Nao coloque segredo, senha ou chave no handoff.
