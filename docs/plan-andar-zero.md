# Plano — andar zero

## Organização de pastas proposta

- `backend/` — aplicação Spring Boot, com código Java, recursos, migrations e testes.
- `frontend/` — aplicação Next.js/TypeScript, com página operacional baseada no ADR-002.
- `docs/` — PRD, ADRs, critérios e planos.
- `rules/` — limites de autoridade e ritmo do agente.

Por que essa e não outra: o projeto tem dois runtimes no ADR-001, Java e TypeScript. Separar backend e frontend deixa claro qual ferramenta executa cada parte e evita misturar build, testes e dependências.

## Tarefas executadas

- [x] T1 · Completar orquestração local com Postgres, backend e front
      atende: AZ-01, AZ-07
      verifico assim: `docker compose up --build` sobe os três serviços usando variáveis documentadas.

- [x] T2 · Criar backend Spring Boot mínimo, sem funcionalidade do PRD
      atende: pré-requisito de AZ-02, AZ-04 e AZ-06
      verifico assim: o backend inicia sem rotas de produto.

- [x] T3 · Configurar conexão com PostgreSQL por variável de ambiente
      atende: AZ-02, AZ-07
      verifico assim: `SPRING_DATASOURCE_URL`, usuário e senha vêm do ambiente; sem banco, a aplicação falha ao conectar.

- [x] T4 · Criar primeira migration versionada
      atende: AZ-03
      verifico assim: Flyway aplica `V1__andar_zero.sql` e cria apenas a tabela técnica `az_marker`.

- [x] T5 · Criar endpoint `GET /health` com status do banco
      atende: AZ-04
      verifico assim: `curl http://localhost:8080/health` devolve 200 quando o banco está acessível.

- [x] T6 · Criar frontend Next.js e validar sua execução local
      atende: AZ-05
      verifico assim: abrir `http://localhost:5173` mostra o estado retornado pelo backend.

- [x] T7 · Criar teste de integração do `/health`
      atende: AZ-06
      verifico assim: `docker compose run --rm backend-test` executa teste com PostgreSQL real via Testcontainers.

- [x] T8 · Proteger segredos locais
      atende: AZ-08
      verifico assim: `.env` está no `.gitignore` e só `.env.example` é versionável.
