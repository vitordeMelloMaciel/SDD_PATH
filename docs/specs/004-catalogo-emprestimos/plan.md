# Plano da Spec 004 — Catálogo e empréstimos

**Estado** · Proposto para aprovação do operador. A rodada `plan-01-c52ae79`
bloqueou a versão anterior (B-01: acesso ao banco contra o ADR-001; B-02:
conta desativada ou excluída). Esta revisão corrige os dois bloqueios e
aguarda nova rodada.

## Objetivo do plano

Implementar a Spec 004 aprovada em fatias pequenas: API, concorrência,
autorização, configuração das fotos, páginas e card. Cada fatia é
testável, cita seus CA-xx e cabe em um commit, sobre o schema das
Specs 002 e 003.

## Restrições

- **ADR-001:**
  - Backend em Java 21, Spring Boot e Spring Security, com sessão HTTP-only.
  - Acesso ao banco por **Spring Data JPA/Hibernate**, sem SQL escrito em
    controllers ou services, que o ADR-001 descarta.
  - Front em Next.js com TypeScript, Tailwind e componentes no padrão shadcn/ui.
  - Testes com Testcontainers e PostgreSQL real.
  - Nenhuma biblioteca nova.
- **ADR-002:**
  - O card usa a variante com foto do EquipmentCard: contêiner 4:3, fundo
    `--gradient-equipment`, `object-fit: cover` e “Foto indisponível”.
  - Os rodapés são “Solicitar →”, “Emprestado” e “Em manutenção”.
  - Nenhuma cor nova, estilo inline nem CDN, e a CSP não muda.
  - O card emprestado nunca mostra pessoa nem data.
- **ADR-003:** em produção, o front na Vercel encaminha `/api/*` para o
  backend no Railway por `INTERNAL_API_BASE_URL`. O navegador só chama
  `/api` na mesma origem. O cookie de sessão é `Secure`. Deploy e
  variáveis em serviço remoto exigem autorização do operador no momento
  da ação.

## Acesso ao banco (correção do B-01)

- Entidades JPA `Equipment` e `Loan` mapeiam as tabelas existentes, sem
  alterar o schema (`ddl-auto: validate` continua valendo). `AppUser` já
  existe.
- Os repositórios Spring Data concentram as consultas, em JPQL:
  - `findAllByOrderByAssetCode` para o catálogo, sem tocar em `loan`;
  - os empréstimos abertos do principal, ordenados por `dueAt` e `id`;
  - a contagem de abertos e a existência de atraso de uma conta.
- **Bloqueio pessimista:** métodos de repositório com
  `@Lock(PESSIMISTIC_WRITE)`, que viram `select ... for update`, para a
  conta e depois para o equipamento, na mesma transação `@Transactional`
  da inserção. A ordem é a da trigger da V2.
- **Inserção:** o `Loan` é salvo sem `dueAt`, para a trigger calcular
  `loaned_at + 14 days`. Depois do `flush`, um `refresh` lê `loanedAt` e
  `dueAt` do banco. O status do equipamento continua mudando só pela
  trigger.
- O `JdbcTemplate` já usado em código commitado (`HealthController`,
  `UserProvisioning`) não é ampliado nesta spec.

## Conta desativada ou excluída (correção do B-02)

- Um novo filtro, `ActiveAccountFilter`, entra na cadeia do Spring Security
  **depois do `CsrfFilter` e antes da autorização**. Assim:
  - no POST, o CSRF continua sendo avaliado primeiro (403 `CSRF_INVALID`);
  - a conta inativa recebe 401 antes da checagem de papel, inclusive com
    `operacoes`.
- Em cada requisição autenticada e não pública, o filtro recarrega a conta
  pelo `id` do principal (`AppUserRepository`). Se ela não existe ou está
  inativa, o filtro invalida a sessão, expira o cookie e responde 401
  `UNAUTHENTICATED`. O caminho é o mesmo do filtro de inatividade atual.
- O filtro vale para as três rotas da 004 e para `GET /api/auth/session`.
  As páginas consultam esse endpoint no servidor e redirecionam para `/`
  quando recebem 401.
- **Ponto para a aprovação do operador.** A Spec 001 não define o que
  `GET /api/auth/session` faz quando a conta foi desativada ou excluída
  depois do login. A Spec 004 exige que as páginas redirecionem nesse caso
  (`spec.md:97-99`, CA-05). O filtro passa a responder 401 nesse caso, sem
  mudar nenhum CA da Spec 001. Os testes da Spec 001 continuam passando.

## Banco

- Nenhuma migration. V2 (trigger, índice `uq_loan_open_equipment` e ordem
  de bloqueio) e V3/V4 (Spec 003) permanecem inalteradas.
- Se alguma tarefa precisar mudar o schema, ela para e segue
  `rules/migration.md`: apresenta o DDL, os critérios atendidos e o efeito
  sobre os dados existentes, e espera a aprovação do operador antes de criar
  qualquer arquivo de migration.

## Arquivos que as tarefas criam ou alteram

Esta lista cumpre `rules/restrictions.md`, que exige plano aprovado para
estrutura nova.

- **Backend, novos:**
  - pacote `br.org.germinare.equipamentos.equipment`:
    - `Equipment` e `EquipmentRepository`;
    - `EquipmentController`, com `GET /api/equipment`, e `EquipmentResponse`;
  - pacote `br.org.germinare.equipamentos.loan`:
    - `Loan` e `LoanRepository`;
    - `LoanService` e `LoanController`, com `POST /api/loans` e `GET /api/loans/me`;
    - `LoanResponse` e o tratamento de erros da rota;
  - `auth/ActiveAccountFilter`.
- **Backend, alterados:** `auth/SecurityConfiguration` (papel e registro do
  filtro) e `user/AppUserRepository` (busca com bloqueio).
- **Testes, novos:** `equipment/EquipmentApiIntegrationTest`,
  `loan/LoanApiIntegrationTest`, `loan/LoanConcurrencyIntegrationTest` e
  `loan/LoanAuthorizationIntegrationTest`. Os nomes dos testes citam o CA
  (`rules/tests.md`).
- **Frontend, novos:**
  - `frontend/app/catalogo/page.tsx` e `frontend/app/meus-emprestimos/page.tsx`;
  - `frontend/lib/loan-api.ts`;
  - `frontend/components/loan-journey.tsx`, com o quadro das páginas, o
    catálogo e Meus empréstimos.
- **Frontend, alterados:** `frontend/components/login-page.tsx` (destino
  após o login) e `frontend/components/equipment-card.tsx` (variante com foto).
- **Configuração:** `docker-compose.yml` e `README.md` (T002).

## Dependências

- `imageUrl` usa `EquipmentImageUrls` (Spec 003, T006, `d16a3e6`). A 004 só
  consome a URL.
- O CA-01 e o CA-06 dependem das 12 fotos no Storage (Spec 003, T003 e T004).
  Até lá, o catálogo mostra “Foto indisponível”.
- A interface não tem biblioteca de teste no ADR-001. Ela é validada por
  `pnpm run typecheck`, `pnpm run build` e conferência no navegador local.

## Testes e builds em worktree limpo

- O `docker compose run --rm backend-test` copia `backend/src` inteiro para
  a imagem, inclusive o código local não rastreado.
- Para o resultado valer para o commit, cada tarefa roda os comandos a
  partir da **raiz de um `git worktree` limpo**, acrescido só dos arquivos
  da tarefa, e não a partir da raiz principal. Os comandos são:
  - a suíte do backend;
  - o typecheck e o build do front, quando a tarefa mexe no frontend;
  - `docker compose build backend frontend`, quando a tarefa afeta os
    artefatos (`rules/checks.md`).

## Código local existente

Há uma implementação local, não rastreada e não aprovada, em:

- `backend/.../loan/` (`LoanController.java` e `LoanService.java`, que usam
  `JdbcTemplate` com SQL no service);
- `frontend/app/catalogo/` e `frontend/app/meus-emprestimos/`;
- `frontend/components/loan-journey.tsx` e `frontend/lib/loan-api.ts`;
- alterações locais em `SecurityConfiguration.java`, `login-page.tsx`,
  `AuthenticationFoundationIntegrationTest.java`, `README.md` e `ENTREGA.md`.

A alteração local do `frontend/next.config.ts` já entrou na `main` pela
T006 da Spec 003 (`d16a3e6`).

Por decisão do operador, esse código não é commitado como está nem é
apagado. Cada tarefa reescreve o seu trecho pela spec. A lógica de negócio
local (ordem de bloqueio e classificação dos 409) é reaproveitada sobre
repositórios JPA. Os diffs locais de `README.md` e `ENTREGA.md` nunca entram
inteiros: cada commit leva só as linhas da tarefa.

## Propostas para aprovação

1. **Rejeição inesperada do banco.** Se o banco ainda rejeitar a inserção
   depois da leitura sob bloqueio:
   - violação de `uq_loan_open_equipment`, identificada por SQLState
     `23505` e pelo nome da constraint, e não pelo texto da mensagem: a
     transação é revertida e a API responde 409 `EQUIPMENT_BORROWED`;
   - qualquer outra rejeição: a transação é revertida e a API responde 500
     `{code: "INTERNAL_ERROR", message: "Não foi possível registrar o empréstimo."}`,
     sem criar empréstimo.
2. **Evidência do CA-03 na interface.** A interface mostra a `message` de
   qualquer 409 pelo mesmo caminho. No navegador, provoca-se
   `EQUIPMENT_BORROWED` (duas sessões no mesmo item) e `LOAN_LIMIT` (quarta
   solicitação). `EQUIPMENT_MAINTENANCE` não pode ser provocado pela
   interface, porque o card não tem ação. `OVERDUE_LOAN` exige um empréstimo
   vencido. Esses dois ficam provados pelos testes HTTP, que verificam
   código e mensagem.
3. **Escopo da T002.** A T002 repassa a variável no `docker-compose.yml` e
   a documenta no README para a conferência local. Esses dois arquivos não
   estão no “Contrato afetado” da spec.

## Sequência

1. API de equipamentos (CA-07, CA-01).
2. `EQUIPMENT_IMAGE_BASE_URL` no ambiente local (CA-01, CA-06).
3. API de empréstimos, com os 409 e seus motivos (CA-02, CA-03).
4. Concorrência (CA-04).
5. Conta ativa, papel e CSRF nas rotas e na sessão (CA-05).
6. Proteção das páginas e destino após o login (CA-05, CA-01).
7. Catálogo com fotos (CA-01, CA-03, CA-07).
8. Meus empréstimos (CA-02, CA-05).
9. Verificação final (CA-01 a CA-07).

O detalhe de cada passo está no `tasks.md`.
