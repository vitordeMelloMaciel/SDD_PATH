# Layout do frontend

Este documento registra a composicao das paginas. Tokens, variantes, acessibilidade e comportamento dos componentes pertencem ao [ADR-002](adr/002-design-system.md).

## Operacoes

A pagina inicial autenticada usa o `AppShell` com navegacao lateral, cabecalho de contexto e acao `Novo emprestimo`.

Ordem do conteudo:

1. titulo e contexto operacional;
2. aviso com as regras vigentes;
3. resumo do acervo;
4. catalogo com busca, filtro de situacao e filtro de categoria;
5. cards de equipamentos;
6. tabela de emprestimos em aberto e prazos;
7. dialogo de registro de emprestimo.

Os dados exibidos sao demonstrativos ate as specs funcionais autorizarem a integracao com a API. Componentes visuais recebem estado e conteudo por propriedades e nao decidem regras de emprestimo.

## Login

O login nao usa `AppShell`. Sua composicao sera detalhada quando a Spec 001 sair do estado de elaboracao. O gradiente `--gradient-login` permanece reservado para o painel de marca dessa pagina.
