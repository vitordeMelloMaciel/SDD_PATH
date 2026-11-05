# DDL esperado para login

A Spec 001 reutiliza a tabela `app_user` aprovada pela Spec 002. O contrato esperado e:

- `id` UUID como chave primaria;
- `name` obrigatorio;
- `email` normalizado e unico;
- `password_hash` obrigatorio e nunca retornado ao frontend;
- `role` limitado a `colaborador` ou `operacoes`;
- `active` para bloquear autenticacao sem remover historico;
- timestamps de criacao e atualizacao.

Fonte unica do DDL executavel: `backend/src/main/resources/db/migration/V2__modelo_inicial.sql`.
Requisitos e criterios de banco: `docs/specs/002-modelo-dados/spec.md`.

Nao ha policy RLS exposta ao navegador: o frontend nao recebe chave do Supabase e todo acesso ao PostgreSQL passa pelo backend Spring Boot. Sessao e autorizacao pertencem ao Spring Security conforme ADR-001. Alterar esse limite exige novo ADR.
