# Tarefas da Spec 004

Execute uma tarefa por vez. Cada tarefa termina com diff revisado, checks
proporcionais (`rules/checks.md`) e um commit próprio. Pré-requisito de
T001: a T006 da Spec 003 (composição de `EQUIPMENT_IMAGE_BASE_URL`).

A coluna “Código local” compara a implementação não rastreada com o que a
tarefa pede. Ela não é aprovação desse código.

## T001 · CA-07 e CA-01 (API). Catálogo com seis campos e foto

- [ ] Implementar `GET /api/equipment` com os seis campos, ordenado por patrimônio, sem ler `loan`.
- [ ] Preencher `imageUrl` com o componente da T006 da Spec 003.
- [ ] Teste HTTP de privacidade com as contas A e B (CA-07).

Código local:
- **Cobre:** rota, ordenação e status (`LoanController.java`, `LoanService.catalog`).
- **Diverge:**
  - Expõe `myDueAt` com o vencimento do titular, o que contraria o CA-07.
  - Faz `left join loan` pela conta da sessão.
  - `imageUrl` é sempre `null`.
  - O teste local (`AuthenticationFoundationIntegrationTest`) exige `myDueAt`.

Commit sugerido: `feat(catalog): list equipment with six public fields [CA-07,CA-01]`

## T002 · CA-02, CA-03 e CA-04. Solicitação e Meus empréstimos

- [ ] Implementar `POST /api/loans` com bloqueio conta → equipamento, os quatro 409 na ordem da V2, 400 e 404.
- [ ] Implementar `GET /api/loans/me` com `dueAt` ISO 8601 e `overdue`.
- [ ] Tratar a rejeição inesperada do banco, conforme a proposta do plano (depende de aprovação).
- [ ] Testes com PostgreSQL real: recusas sem inserção, prazo de 14 dias e as duas concorrências do CA-04.

Código local:
- **Cobre:** bloqueios na ordem certa, classificação dos conflitos, 400 e 404, inserção pela trigger e `GET /api/loans/me` (`LoanService.request`, `LoanService.listOpen`).
- **Diverge:**
  - Uma rejeição inesperada da trigger ou do índice vira erro 500 sem envelope.
  - O teste local cobre só a concorrência ao mesmo item. Falta a concorrência de `LOAN_LIMIT` e cada uma das quatro recusas sem inserção.
  - O teste está dentro de `AuthenticationFoundationIntegrationTest`. Ele vai para uma classe própria da 004.

Commit sugerido: `feat(loans): request loans with locked checks and list my loans [CA-02,CA-03,CA-04]`

## T003 · CA-05 (API). Papel, CSRF e sessão inválida

- [ ] Restringir as três rotas ao papel `colaborador` (403 `FORBIDDEN`, “Acesso negado.”).
- [ ] Testar: 401 sem sessão; 403 para `operacoes`; CSRF ausente ou inválido com e sem sessão; conta desativada ou excluída (401, sessão encerrada, sem empréstimo, cookie antigo com 401); outra conta não vê empréstimos alheios.

Código local:
- **Cobre:** a regra `hasRole("COLABORADOR")` (alteração local em `SecurityConfiguration.java`) e o encerramento da sessão de conta inativa (`LoanController.rejection`).
- **Diverge:** não há testes da matriz de 401, 403 e CSRF nem do cenário de conta desativada ou excluída.

Commit sugerido: `feat(loans): restrict loan routes to collaborators [CA-05]`

## T004 · CA-05 (páginas) e CA-01 (destino). Portão das páginas e login

- [ ] `/catalogo` e `/meus-emprestimos` consultam `GET /api/auth/session` no servidor; sem sessão ou com 401, redirecionam para `/`.
- [ ] Para `operacoes`, mostrar só “Acesso negado” e “Esta área é exclusiva de colaboradores.”, sem chamar as três rotas.
- [ ] Após o login, levar colaborador e `operacoes` a `/catalogo`.
- [ ] Typecheck, build e conferência no navegador.

Código local:
- **Cobre:** redirecionamento para `/` sem cookie ou com 401 (`frontend/lib/loan-api.ts`) e destino `/catalogo` do colaborador (alteração local em `login-page.tsx`).
- **Diverge:**
  - As páginas chamam a rota da API sem consultar a sessão antes.
  - Para `operacoes`, mostram “Acesso negado.” a partir do 403 da API, e a spec proíbe chamar a API nesse caso. Também falta o texto “Esta área é exclusiva de colaboradores.”.
  - `operacoes` não é levado a `/catalogo` após o login.

Commit sugerido: `feat(catalog): gate journey pages by session and role [CA-05,CA-01]`

## T005 · CA-01, CA-03 e CA-07 (interface). Catálogo e card com foto

- [ ] Aplicar a variante com foto do `EquipmentCard` (ADR-002): 4:3, `--gradient-equipment`, `object-fit: cover`, “Foto indisponível”.
- [ ] Rodapés: “Solicitar →”, “Em manutenção” e “Emprestado”, este para todas as contas.
- [ ] Solicitar com CSRF, mostrar o motivo das recusas 409 e recarregar o estado da API.
- [ ] Crédito único “Fotos: Pixabay”.
- [ ] Typecheck, build e conferência no navegador com duas contas (CA-07).

Código local:
- **Cobre:** grade de cards, foto 4:3 com `object-fit: cover`, “Foto indisponível”, crédito Pixabay, CSRF no POST e mensagens 409 (`frontend/components/loan-journey.tsx`).
- **Diverge:**
  - Mostra “Emprestado a você · Devolução …” a partir de `myDueAt` e de estado local. Isso contraria o CA-07 e a exceção do ADR-002.
  - Não há rodapé “Em manutenção” nem “Emprestado”.
  - Usa fundo `bg-sunken` no lugar de `--gradient-equipment`.
  - Usa um botão “Solicitar” no lugar do rodapé “Solicitar →”.
  - Não reaproveita o `EquipmentCard` do Design System. O componente commitado não tem ícone para a categoria `cabo`.

Commit sugerido: `feat(catalog): render equipment cards with photo variant [CA-01,CA-03,CA-07]`

## T006 · CA-02 e CA-05 (interface). Meus empréstimos

- [ ] Listar os empréstimos abertos com patrimônio, nome e devolução em `America/Sao_Paulo`, com destaque para atraso e mensagem para a lista vazia.
- [ ] Typecheck, build e conferência no navegador.

Código local:
- **Cobre:** lista, fuso, destaque de atraso e mensagem para a lista vazia (`MyLoans` em `loan-journey.tsx`, `frontend/app/meus-emprestimos/page.tsx`).
- **Diverge:** depende do portão de páginas da T004, pelo mesmo motivo já descrito nela.

Commit sugerido: `feat(loans): show my open loans page [CA-02,CA-05]`

## T007 · CA-01 a CA-07. Verificação final

- [ ] Suíte do backend em worktree limpo e `docker compose build backend frontend`.
- [ ] Matriz no navegador local, sem a conta do professor.
- [ ] Roteiro do CA-06 em produção, executado pelo operador, e consulta de leitura com zero empréstimos para o professor.
- [ ] `spec-validator` em modo verificação, com a tabela em `verification/`.
- [ ] Atualizar `ENTREGA.md` e `handoff.md`.

Código local: não se aplica.

Commit sugerido: `test(catalog): verify Spec 004 acceptance criteria [CA-01..CA-07]`
