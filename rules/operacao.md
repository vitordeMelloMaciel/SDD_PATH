---
description: Contrato de operacao entre operador e agente neste projeto
globs: []
alwaysApply: true
---

# Contrato de operacao

## Git
> leitor: agente

- O agente pode criar branch com prefixo `codex/`, commitar e publicar essa branch depois de rodar os checks e mostrar o resumo do diff ao operador.
- `main` recebe mudancas somente por pull request. O merge continua sendo do operador, depois de revisar o diff e os checks.
- O agente nunca faz push direto em `main`, nunca reescreve historico remoto e nunca cria tag de entrega sem autorizacao especifica.
- A mensagem de commit cita a tarefa e o criterio de aceitacao, quando existir.
- Um commit por tarefa. Nunca junte codigo e mudanca de regra no mesmo commit.

## Autorizacao por tipo de acao
> leitor: agente

| acao | nivel |
|---|---|
| Editar arquivo existente dentro do escopo da tarefa | livre |
| Criar arquivo novo previsto no plano ou no pedido explícito | avisar depois |
| Deletar ou renomear arquivo explicitamente indicado na tarefa | avisar depois |
| Deletar ou renomear arquivo fora do pedido explícito | pedir antes |
| Instalar ou atualizar dependencia | pedir antes |
| Criar migration prevista na tarefa | avisar depois |
| Alterar configuracao local de ambiente ou deploy prevista na tarefa | avisar depois |
| Executar deploy ou alterar projeto remoto | pedir antes |

O que cada nivel significa:

- livre: faca e siga em frente.
- avisar depois: faca e diga na resposta o que foi criado.
- pedir antes: descreva o efeito externo e obtenha autorizacao antes de executar.

As proibicoes permanentes e a precedencia pertencem somente a `rules/restrictions.md`.

## Configuracao do operador
> leitor: humano - isto nao e instrucao para o agente

| fase | modelo / raciocinio |
|---|---|
| Escrever a spec | raciocinio alto |
| Planejar e quebrar em tarefas | raciocinio alto |
| Executar uma tarefa ja decidida | rapido |
| Debugar algo que quebrou | raciocinio alto |
| Revisar o diff antes do commit | padrao |

- A revisao do diff acontece em sessao limpa, de preferencia com modelo diferente do que escreveu o codigo.
- Sessao longa nao se corrige com regra mais enfatica: encerre seguindo `rules/handoff.md` e abra outra.
