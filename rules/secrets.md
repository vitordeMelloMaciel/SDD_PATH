---
description: Onde cada variavel de ambiente vive e quais valores podem ir ao navegador
globs: ["**/.env*", "frontend/app/**", "frontend/components/**", "frontend/lib/**", "backend/src/main/resources/**", "docker-compose.yml", "vercel.json"]
alwaysApply: false
---

# Variaveis de ambiente e segredos
> leitor: agente

## Quando
Ao criar ou usar variavel de ambiente ou configuracao de servico externo.

## Procedimento
1. Codigo do navegador recebe apenas valores publicos com prefixo `NEXT_PUBLIC_`.
2. URL JDBC, usuario, senha e chaves privilegiadas ficam somente no backend ou no provedor de hospedagem.
3. Variavel nova deve aparecer em `.env.example` sem valor real e no ambiente que a executa.
4. Declare o que foi configurado localmente e o que ainda depende do operador no painel remoto.

## Verificacao
`.env.example` documenta os nomes sem segredos e `git status --ignored` confirma que `.env` e `.env.local` estao ignorados.

## Nao faca
- Nao escreva segredo em resposta, commit, log ou comentario.
- Nao use chave de servico em variavel `NEXT_PUBLIC_`.
- Nao contorne autorizacao trocando por uma chave mais privilegiada.
