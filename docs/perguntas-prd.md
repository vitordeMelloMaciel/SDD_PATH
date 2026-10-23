# Perguntas registradas a partir do PRD

Este arquivo preserva as lacunas encontradas na leitura inicial. Respostas aprovadas pertencem as specs e nao sao duplicadas aqui.

1. Como o login sera feito na primeira versao: usuario e senha locais, conta institucional ou outro metodo?
   tipo: tecnica
   status: parcialmente respondida pelo ADR-001; provisionamento inicial segue pendente na Spec 001
   por que importa: define autenticacao, cadastro inicial, sessao e os dados minimos de usuario.

2. Quem pode ser considerado "Operacoes" dentro do sistema e como essa permissao sera concedida?
   tipo: negocio
   status: pendente na Spec 001
   por que importa: sem essa regra, qualquer pessoa pode ganhar acesso indevido a telas administrativas.

3. O que exatamente conta como "item em atraso": depois do fim do 14o dia, no dia seguinte, ou por horario exato?
   tipo: negocio
   status: respondida em `docs/specs/002-modelo-dados/spec.md`
   por que importa: essa decisao muda bloqueio de novos emprestimos, exibicao de status e testes de prazo.

4. O que acontece quando duas pessoas tentam solicitar o mesmo equipamento disponivel quase ao mesmo tempo?
   tipo: tecnica
   status: respondida em `docs/specs/002-modelo-dados/spec.md`
   por que importa: define a regra de concorrencia e evita dois emprestimos ativos para o mesmo item.

5. A importacao da planilha atual sera ignorada completamente ou os equipamentos iniciais serao cadastrados manualmente antes do uso?
   tipo: fora de escopo
   status: importacao fora da v1; provisionamento inicial ainda pendente
   por que importa: deixa claro que a v1 nao precisa migrar historico, mas ainda precisa nascer com catalogo utilizavel.
