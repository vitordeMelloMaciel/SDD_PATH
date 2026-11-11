# Tarefas - Spec 001 Login e sessao

Execute uma tarefa por vez. Cada item termina com diff revisado, checks proporcionais e um commit proprio.

## T001 - Fundacao de identidade e Spring Security

- [x] Adicionar dependencias autorizadas de Security e JPA.
- [x] Mapear `app_user` existente e busca por e-mail normalizado.
- [x] Criar principal com `id`, `fullName`, `email` e `role`.
- [x] Configurar encoder de senha, regras publicas/protegidas e erros JSON.
- [x] Testar usuario valido, inexistente, senha incorreta e inativo.

Atende: CA-02, CA-03, CA-06, CA-07 e parte de CA-09.

Commit sugerido: `feat(login): add authenticated user foundation [CA-02,CA-03,CA-06]`

## T002 - Endpoints, sessao, logout e CSRF

- [x] Implementar os quatro endpoints e corpos definidos na spec.
- [x] Configurar cookie de sessao e timeout de inatividade.
- [x] Implementar ciclo CSRF e logout publico/idempotente.
- [ ] Testar status, payloads, cookies, expiracao e logout repetido.

O fluxo HTTP de login, sessao e logout repetido e a expiracao em oito horas passaram no Docker.
Ainda faltam os casos HTTP de JSON invalido e credenciais invalidas, e a verificacao do atributo `Secure`.

Atende: CA-02, CA-04, CA-05, CA-06, CA-07, CA-09 e CA-10.

Commit sugerido: `feat(login): implement session and CSRF contracts [CA-02,CA-04,CA-05,CA-10]`

## T003 - Provisionamento administrativo idempotente

- [ ] Implementar servico e modo de comando sem servidor web.
- [ ] Criar servico Compose `backend-provision`.
- [ ] Documentar variaveis sem valores reais.
- [ ] Testar primeira criacao, repeticao, divergencia, validacoes e saida sem segredo.

Atende: CA-07 e CA-08.

Commit sugerido: `feat(login): add safe user provisioning command [CA-07,CA-08]`

## T004 - Pagina de login Nocturne

- [ ] Encaminhar `/api/*` ao backend sem expor URL interna ao navegador.
- [ ] Implementar a composicao desktop e os textos aprovados.
- [ ] Implementar CSRF, loading, 400, 401, 403 e falha inesperada.
- [ ] Conferir teclado, foco, rotulos, `aria-invalid`, `role=alert` e CSP.
- [ ] Rodar typecheck e build do frontend.

Atende: CA-01, CA-03, CA-10 e CA-11.

Commit sugerido: `feat(login): build accessible Nocturne login page [CA-01,CA-03,CA-11]`

## T005 - Verificacao final da Spec 001

- [ ] Rodar testes completos do backend e builds dos containers.
- [ ] Subir o ambiente e executar a matriz de login sem usar a conta do professor.
- [ ] Conferir que nenhuma saida contem senha ou hash.
- [ ] Rodar `spec-validator` em modo verificacao e salvar a tabela final.
- [ ] Atualizar `ENTREGA.md` e `handoff.md` com evidencias reais.

Atende: CA-01 a CA-11.

Commit sugerido: `test(login): verify Spec 001 acceptance criteria [CA-01..CA-11]`
