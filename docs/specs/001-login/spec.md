# Spec 001 - Login e sessao

**Status:** rascunho; aguardando decisoes do operador

**Vinculo:** `docs/PRD.md`, item 1

**ADRs aplicaveis:** `docs/adr/001-stack.md`

## Objetivo

Permitir que uma pessoa cadastrada entre com e-mail e senha, mantenha uma sessao segura e saia do sistema. Esta e a primeira fatia vertical: cria o registro de usuario no PostgreSQL, o contrato de autenticacao no backend Spring Boot e a tela de login no frontend Next.js.

## Regras

- A autenticacao usa e-mail e senha.
- A sessao e gerenciada pelo Spring Security em cookie HTTP-only, conforme ADR-001.
- Cada usuario possui um papel: `colaborador` ou `operacoes`.
- Senha e armazenada somente como hash e nunca aparece em resposta, log ou codigo do frontend.
- Credenciais invalidas nao criam sessao e retornam uma mensagem que nao revela se o e-mail existe.
- Recurso protegido sem sessao valida retorna HTTP 401.
- Logout invalida a sessao atual.

## Criterios de aceitacao

- **CA-01:** pessoa sem sessao visualiza a tela de login com campos de e-mail e senha.
- **CA-02:** credenciais validas criam uma sessao e retornam a identidade minima do usuario autenticado.
- **CA-03:** credenciais invalidas nao criam sessao e exibem erro generico.
- **CA-04:** uma rota protegida responde HTTP 401 quando chamada sem sessao valida.
- **CA-05:** logout invalida a sessao e uma chamada protegida posterior responde HTTP 401.
- **CA-06:** o papel `colaborador` ou `operacoes` fica disponivel para as verificacoes de autorizacao do backend.
- **CA-07:** respostas e logs de autenticacao nao expoem hash nem senha.

## Fora de escopo

- Cadastro publico, convite e administracao de usuarios.
- Recuperacao ou troca de senha.
- Login social, SSO e autenticacao multifator.
- Catalogo, emprestimo, devolucao e tela operacional funcional.
- Configuracao do projeto Supabase ou deploy da Vercel.

## Contrato afetado

- Banco: usa `app_user`, criada pela Spec 002 e pela migration Flyway V2.
- Backend: endpoints de login, consulta de sessao e logout; protecao das demais rotas.
- Frontend: tela de login e tratamento dos estados carregando, invalido e autenticado.
- Testes: integracao com PostgreSQL real e teste negativo de acesso sem sessao.

## Perguntas em aberto

1. Quem cria os primeiros usuarios: seed local, cadastro feito por Operacoes ou insercao administrativa?
2. Qual e a duracao da sessao e ela deve sobreviver ao fechamento do navegador?
3. Usuario inativo deve receber a mesma mensagem generica de credencial invalida?
4. Havera bloqueio temporario depois de varias tentativas incorretas na v1?

Esta spec nao esta aprovada enquanto as quatro perguntas acima nao forem respondidas. Conforme `rules/spec-flow.md`, `plan.md` e `tasks.md` ainda nao devem ser criados.

## Regras acionadas quando a spec avancar

- `spec-validator` confere PRD, ADRs, criterios e perguntas antes da aprovacao humana; depois da aprovacao, `rules/spec-flow.md` libera plano e tarefas.
- Mudanca de schema aciona `rules/migration.md`; credenciais e sessao acionam `rules/secrets.md`; implementacao e encerramento acionam `rules/tests.md`, `rules/checks.md` e `rules/handoff.md`.
