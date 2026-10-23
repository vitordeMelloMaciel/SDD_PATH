# DDL da Spec 001

A proposta inicial deste arquivo foi substituida pela fonte aprovada do modelo de dados:

- requisitos e criterios: `docs/specs/002-modelo-dados/spec.md`;
- DDL executavel: `backend/src/main/resources/db/migration/V2__modelo_inicial.sql`.

A Spec 001 continua responsavel pelos comportamentos de login e sessao. Ela nao deve duplicar o DDL mantido pela Spec 002 e pelo Flyway.
