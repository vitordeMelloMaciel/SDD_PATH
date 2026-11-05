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
| front | Next.js com TypeScript, React, Tailwind CSS e componentes no padrão shadcn/ui |
| como o front é servido | separado do backend; execução local em Node.js e deploy na Vercel |
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
- Next.js com TypeScript fornece componentes React, carregamento local de fonte, suporte a CSP com nonce e integração direta com a Vercel.
- Tailwind CSS e o padrão shadcn/ui permitem mapear os tokens do ADR-002 em componentes acessíveis e reutilizáveis.
- Front e backend separados preservam a sessão HTTP-only no Spring e permitem publicar a interface independentemente.
- Sessão em cookie HTTP-only reduz exposição de token no navegador e atende login local da v1.
- Um `role` no usuário cobre os dois perfis descritos no PRD sem criar um RBAC maior que o problema.
- JPA/Hibernate reduz SQL repetitivo e integra transações com o Spring.
- Flyway torna mudança de schema explícita, revisável e repetível.
- Spring Boot Test com Testcontainers permite provar comportamento usando PostgreSQL real.
- Docker Compose diminui variação entre máquinas sem esconder backend e front do desenvolvedor.
- `.env.example` documenta configuração sem versionar segredo real.

## Alternativas descartadas

| item | alternativa | motivo do descarte |
|---|---|---|
| linguagem / runtime | Python 3 | reduziria o esqueleto inicial, mas deixaria mais convencoes estruturais abertas e fugiria da stack Java definida pelo time |
| framework do backend | FastAPI | acelera um prototipo, mas nao oferece o mesmo conjunto integrado de seguranca, persistencia e testes escolhido para este projeto |
| estilo da API | GraphQL | a v1 possui recursos e consultas simples; uma camada adicional de schema e resolucao nao se paga |
| front | Vite com TypeScript sem React | nao atende a composicao por componentes e o modelo de renderizacao adotado no design system |
| como o front e servido | bundle servido pelo Spring Boot | acoplaria os ciclos de deploy do frontend e backend e reduziria a independencia da Vercel |
| autenticacao | JWT persistido no navegador | aumentaria a exposicao do token a codigo executado no cliente; a sessao HTTP-only reduz essa superficie |
| autorizacao | RBAC com tabelas de papeis e permissoes | os dois perfis fixos do PRD nao justificam um modelo de permissao dinamico na v1 |
| acesso ao banco | SQL direto em controllers ou services | espalharia queries, mapeamento e transacoes pelas regras de aplicacao |
| migrations | schema criado automaticamente pelo ORM | esconderia mudancas de banco e impediria revisao e reproducao deterministica |
| testes | H2 ou mocks como prova de integracao | nao reproduz constraints, indices parciais e triggers do PostgreSQL real |
| execucao local | instalacao manual de Java, Node e PostgreSQL | criaria diferencas entre maquinas e passos nao verificaveis pelo mesmo comando |
| configuracao e segredos | arquivo de configuracao com valores reais versionado | colocaria credenciais no historico Git e misturaria configuracao por ambiente com codigo |
| banco | MySQL | PostgreSQL ja era uma restricao do projeto e oferece os recursos usados nas invariantes do modelo |

## Consequências

- O que fica mais fácil: subir uma API com configuração, migrations, testes e banco real de forma padronizada.
- O que fica mais difícil: a stack tem mais arquivos iniciais que uma versão HTML estática ou uma API minimalista.
- O que essa escolha nos impede de fazer depois sem custo: trocar o framework backend, trocar o modelo de sessão ou abandonar migrations versionadas exigiria refatoração ampla.

## O que este ADR NÃO decide

Estrutura final de domínio, nomes de endpoints de produto, formato de erros da API, telas de negócio, seed de dados, permissões detalhadas e organização definitiva por feature.
Isso vem depois do esqueleto rodar, e vira ADR-002 ou spec quando for uma decisão cara de mudar.
