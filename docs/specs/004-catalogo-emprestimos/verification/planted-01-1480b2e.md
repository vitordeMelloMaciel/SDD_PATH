# Rodada de verificacao planted-01 (prova do erro plantado)

- Branch descartavel: `erro-plantado-ca07`, criada a partir da `main` em `e06194d`. Nao foi incorporada a `main` nem enviada ao GitHub.
- Erro plantado: commit `1480b2e`. `GET /api/equipment` passa a expor `borrowerName`, o nome de quem tem o item emprestado, para qualquer conta. Isso contraria o CA-07. O comentario antigo, que dizia que a resposta "nao le emprestimos", foi mantido de proposito para nao dar pista ao verificador.
- Suite no commit do erro (`docker compose run --rm backend-test`): 58 testes, 2 falhas, `BUILD FAILURE`:
  - `EquipmentApiIntegrationTest.ca01ListsTheWholeCatalogByAssetCodeWithSixFieldsAndPhotoUrl`;
  - `EquipmentApiIntegrationTest.ca07BorrowedItemIsTheSameObjectForTheHolderAndForAnotherAccount`.

  Nos dois, a chave `borrowerName` aparece como inesperada.
- Correcao: commit `13570f1`, que reverte `1480b2e`. O codigo fica identico ao da `main`, e `git diff e06194d 13570f1` sai vazio. Suite: 58 testes, 0 falhas, `BUILD SUCCESS`.
- Executor da rodada: instrucoes do `spec-validator` executadas por subagente Claude Code em contexto limpo e somente leitura (Codex CLI indisponivel nesta maquina). O verificador nao foi avisado sobre onde estava o erro.
- Data: 2026-11-11

## Saida do verificador

# Resultado geral: REPROVADA

**Rodada:** verificação planted-01 (Spec 004, branch erro-plantado-ca07)
**Commit avaliado:** 1480b2e1ec849a72f16b1eace76d0f753e708aca
**Data:** 2026-11-11

## Tabela por critério

| Critério | Resultado | Evidência arquivo:linha | Lacuna |
| --- | --- | --- | --- |
| CA-02 | APROVADO (backend) | `loan/LoanService.java:44-73` não mudou em relação a `e06194d`. O status muda só pela trigger. `LoanApiIntegrationTest.java:35-58` confere 201, `due_at = loaned_at + 14 days`, status `emprestado` e `/api/loans/me`. | O teste não lê `GET /api/equipment`, então a mudança de situação "no catálogo" só é provada pelo banco. O catálogo do HEAD também viola o contrato de seis campos (ver CA-07). |
| CA-03 | APROVADO | `LoanService.java:52-63` mantém a ordem manutenção → emprestado → limite → atraso. `LoanController.java:128-138` devolve 400 e `LoanService.java:50` devolve 404. Testes em `LoanApiIntegrationTest.java:72-116`. | Nenhuma no backend. O diff não toca nesse caminho. |
| CA-04 | APROVADO | O bloqueio de usuário e depois de equipamento está em `LoanService.java:46-50`, e o tratamento de `uq_loan_open_equipment` em `LoanService.java:81-90`, sem mudança. Testes em `LoanConcurrencyIntegrationTest.java:37-110`. | Nenhuma. |
| CA-05 | APROVADO (backend) | `auth/SecurityConfiguration.java:94` (`hasRole("COLABORADOR")`) e os filtros de conta ativa não mudaram. As respostas 401 e 403 saem antes do controller. Testes em `LoanAuthorizationIntegrationTest.java:33-125`. | Nenhuma no backend. As novas leituras no controller não rodam para sessão rejeitada. |
| CA-07 | **REPROVADO** | `equipment/EquipmentController.java:36-39` lê todos os empréstimos abertos e resolve `AppUser::fullName`. `EquipmentController.java:48` e `:53-54` acrescentam um sétimo campo, `borrowerName`, ao `EquipmentResponse`. | A resposta tem 7 campos em vez de 6 e expõe o nome completo do titular para qualquer conta. Isso viola "exatamente os seis campos" e "nenhum valor contém ... nome". |

## Divergências código × spec

1. **Contrato de `GET /api/equipment` (spec, "Contrato proposto", e CA-07).** A spec exige exatamente `id, assetCode, name, category, status, imageUrl` e diz que a resposta "nunca contém identificação de quem tomou o item". O HEAD serializa `borrowerName` em `EquipmentController.java:53-54`:
   - Para o item emprestado, o campo traz o `full_name` do titular (`:38-39`, `:48`), visível para A, para B e para qualquer colaborador.
   - Para os demais itens, o campo vai como `null`. O `application.yml` não configura `default-property-inclusion`, e não há `@JsonInclude` em `src/main`, então o Jackson inclui a chave nula em todos os 12 itens. O sétimo campo aparece em todo o catálogo, não só no item emprestado.
2. **Privacidade do card (spec, "Card de equipamento", e exceção do ADR-002).** O backend passa a fornecer a pessoa titular ao front. `frontend/lib/loan-api.ts:13-20` ignora o campo no tipo, mas o dado vaza na rede e no navegador de qualquer conta.
3. **Comentário enganoso.** `EquipmentController.java:16-19` ainda afirma que "a resposta não lê empréstimos, então nunca expõe dono", o que contradiz `:36-39`. Avaliei pelo código, não pelo comentário.
4. **Mensagem de commit.** `1480b2e` se identifica como `test(...)`, mas altera apenas código de produção (`git diff e06194d HEAD --stat`: 1 arquivo, `EquipmentController.java`). Nenhum teste foi alterado.

## Testes que falhariam

Estimativa feita pela leitura do código e dos testes. Nenhum teste foi executado nesta rodada.

| Teste | Linha da falha | Motivo |
| --- | --- | --- |
| `EquipmentApiIntegrationTest.ca01ListsTheWholeCatalogByAssetCodeWithSixFieldsAndPhotoUrl` | `EquipmentApiIntegrationTest.java:42` | `containsExactlyInAnyOrderElementsOf(FIELDS)` recebe 7 chaves (`borrowerName: null`) contra as 6 de `:23`. |
| `EquipmentApiIntegrationTest.ca07BorrowedItemIsTheSameObjectForTheHolderAndForAnotherAccount` | `EquipmentApiIntegrationTest.java:83` | Mesma razão: `keySet()` tem `borrowerName`. A linha `:82` passa, porque os dois objetos trazem o mesmo nome "Pessoa Titular". A linha `:85` também passa. Se `:83` passasse, `:87-89` falharia, porque o corpo contém "Pessoa Titular" (`:88`). |

Testes que passariam:
- `EquipmentApiIntegrationTest.ca01ImageUrlIsNullWhenTheItemHasNoPhotoKey`: `:59-60` só conferem `assetCode` e `imageUrl`.
- Todos os de `LoanApiIntegrationTest` (CA-02, CA-03).
- Todos os de `LoanConcurrencyIntegrationTest` (CA-04).
- Todos os de `LoanAuthorizationIntegrationTest` (CA-05). Em `:33-52` e `:76-97`, as chamadas a `/api/equipment` são barradas por 401 ou 403 antes do controller.
- `EquipmentImageUrlsTest`.

No item emprestado, `Collectors.toMap` (`EquipmentController.java:38`) não colide, porque `uq_loan_open_equipment` impede dois empréstimos abertos para o mesmo equipamento. Os testes que falham não quebram por exceção, e sim por asserção.

## Conferido

- `git -C wt-erro log --oneline -3`: HEAD `1480b2e` sobre `e06194d` e `9839b43`.
- `git -C wt-erro diff e06194d HEAD`: mudança única em `backend/src/main/java/br/org/germinare/equipamentos/equipment/EquipmentController.java` (+20/−3).
- Spec `docs/specs/004-catalogo-emprestimos/spec.md`: contrato de `GET /api/equipment`, seção do card e CA-02 a CA-07.
- Código lido:
  - `EquipmentController.java`
  - `loan/Loan.java`, `LoanRepository.java`, `LoanService.java`, `LoanController.java`
  - `user/AppUser.java`, `AppUserRepository.java`
  - `auth/SecurityConfiguration.java:94`
  - `src/main/resources/application.yml`, onde não há configuração de inclusão do Jackson
- Testes lidos: `EquipmentApiIntegrationTest.java` (inteiro), `LoanApiIntegrationTest.java:28-60` e a lista de métodos, `LoanAuthorizationIntegrationTest.java:26-125`, e a lista de métodos de `LoanConcurrencyIntegrationTest.java`.
- Front: `frontend/lib/loan-api.ts:13-20` (o tipo `Equipment` tem 6 campos) e `frontend/components/loan-journey.tsx` (sem uso de `borrowerName`).
- Não foram reavaliadas a evidência de navegador e a de produção (CA-01, CA-06 e a parte visual do CA-07), conforme o escopo desta rodada. Não alterei arquivos, git, contêineres nem banco.

RESULTADO GERAL: REPROVADA
