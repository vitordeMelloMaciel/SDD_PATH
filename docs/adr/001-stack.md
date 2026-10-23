# ADR-001 — Stack do projeto

**Status:** aceito
**Contexto do produto:** docs/PRD.md

## Contexto

Sistema web interno de empréstimo de equipamentos. Front, backend e banco.
Time pequeno, prazo curto, nenhum código existente no início do projeto. O banco já estava decidido: PostgreSQL.

## Decisão

| item | escolha |
|---|---|
| linguagem / runtime | Java 21 |
| framework do backend | Spring Boot |
| estilo da API | REST com JSON |
| front | TypeScript com Vite |
| como o front é servido | separado em desenvolvimento; build estático pode ser servido pelo backend em produção |
| autenticação | e-mail e senha, com sessão em cookie HTTP-only gerenciada pelo Spring Security |
| autorização | campo `role` no usuário, com valores `colaborador` e `operacoes` |
| acesso ao banco | Spring Data JPA/Hibernate sobre PostgreSQL |
| migrations | Flyway com scripts SQL versionados |
| testes | JUnit 5, Spring Boot Test e Testcontainers para integração com PostgreSQL |
| execução local | Docker Compose sobe Postgres, backend e front |
| configuração e segredos | `.env.example` versionado; `.env` local ignorado pelo Git |
| banco | PostgreSQL |

## Justificativa

- Java 21 dá tipagem forte e um runtime estável para regras de negócio e APIs internas.
- Spring Boot reduz decisões manuais de API, configuração, validação, segurança, persistência e testes.
- REST com JSON combina com recursos simples como equipamentos, empréstimos e usuários.
- TypeScript com Vite mantém o front pequeno, mas com checagem de contrato melhor que JavaScript puro.
- Front separado em desenvolvimento facilita recarga rápida; servir build estático pelo backend mantém a operação simples em produção.
- Sessão em cookie HTTP-only reduz exposição de token no navegador e atende login local da v1.
- Um `role` no usuário cobre os dois perfis descritos no PRD sem criar um RBAC maior que o problema.
- JPA/Hibernate reduz SQL repetitivo e integra transações com o Spring.
- Flyway torna mudança de schema explícita, revisável e repetível.
- Spring Boot Test com Testcontainers permite provar comportamento usando PostgreSQL real.
- Docker Compose diminui variação entre máquinas sem esconder backend e front do desenvolvedor.
- `.env.example` documenta configuração sem versionar segredo real.

## Alternativas descartadas

- Python com FastAPI — descartada porque acelera protótipo, mas deixaria mais decisões estruturais abertas para a turma neste momento.
- HTML/CSS/JS sem build — descartado porque simplifica o início, mas perde tipagem quando o front começa a depender de várias rotas.
- GraphQL — descartado porque a v1 tem recursos simples e não precisa de uma camada extra de contrato.
- SQL direto — descartado porque espalharia queries e aumentaria o custo de mudança no início.
- PostgreSQL instalado manualmente — descartado porque tornaria a execução local diferente em cada máquina.

## Consequências

- O que fica mais fácil: subir uma API com configuração, migrations, testes e banco real de forma padronizada.
- O que fica mais difícil: a stack tem mais arquivos iniciais que uma versão HTML estática ou uma API minimalista.
- O que essa escolha nos impede de fazer depois sem custo: trocar o framework backend, trocar o modelo de sessão ou abandonar migrations versionadas exigiria refatoração ampla.

## O que este ADR NÃO decide

Estrutura final de domínio, nomes de endpoints de produto, formato de erros da API, telas de negócio, seed de dados, permissões detalhadas e organização definitiva por feature.
Isso vem depois do esqueleto rodar, e vira ADR-002 ou spec quando for uma decisão cara de mudar.
