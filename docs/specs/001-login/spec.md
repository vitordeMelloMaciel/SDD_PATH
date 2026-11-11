# Spec 001 - Login e sessao

**Status:** aprovada pelo operador em 2026-11-10; resultado `APROVAVEL` na rodada 03 do `spec-validator`. Em 2026-11-11, o operador aprovou uma linha sobre conta desativada ou excluida em `GET /api/auth/session`, validada na rodada 04.

**Vinculo:** `docs/PRD.md`, item 1. A visualizacao dos proprios emprestimos sera coberta pela futura Spec 004.

**ADRs aplicaveis:** `docs/adr/001-stack.md` e `docs/adr/002-design-system.md`

**Pre-requisito:** Spec 002 implementada; a tabela `app_user` e a migration Flyway V2 ja existem e nao pertencem a esta spec.

## Objetivo

Permitir que uma pessoa previamente cadastrada entre com e-mail e senha, consulte a propria sessao e saia do sistema. A entrega conecta a tabela `app_user` existente ao Spring Security e a uma pagina de login Next.js, sem criar cadastro publico nem tela de negocio autenticada.

## Regras

- A autenticacao usa e-mail normalizado e senha.
- A sessao e gerenciada pelo Spring Security por identificador opaco em cookie HTTP-only; nao usa JWT no navegador.
- Cada usuario possui um papel `colaborador` ou `operacoes`, exposto na identidade autenticada.
- Senha e armazenada somente como hash e nunca aparece em resposta, cookie, log ou codigo do frontend.
- E-mail inexistente, senha incorreta e usuario inativo retornam exatamente o mesmo status, codigo e mensagem.
- Logout e idempotente: com ou sem sessao valida, invalida o que existir, remove o cookie e responde 204.
- O cookie de sessao nao possui `Expires` nem `Max-Age`, usa `HttpOnly`, `Path=/`, `SameSite=Lax` e, em producao, `Secure`.
- A sessao expira quando uma requisicao autenticada chega em instante maior ou igual ao ultimo acesso aceito mais 8 horas. Cada requisicao autenticada aceita atualiza o ultimo acesso; requisicoes publicas ou rejeitadas nao renovam a sessao.
- O navegador consome caminhos relativos em `/api`; o frontend encaminha esses caminhos ao backend para manter o contrato same-origin e evitar cookie de terceiro.
- Operacoes que alteram estado usam protecao CSRF do Spring Security. O token CSRF nao e credencial de sessao e pode ser lido pelo frontend apenas para ser reenviado no cabecalho exigido.
- A v1 nao bloqueia temporariamente a conta e nao adiciona CAPTCHA nem rate limiting. Esse risco e conhecido e permanece fora do escopo.

## Contrato HTTP

### Obter token CSRF

`GET /api/auth/csrf`

- Resposta 200: `{ "token": "<valor-opaco>" }`.
- O token retornado deve ser enviado em `X-XSRF-TOKEN` nos `POST` de login e logout.
- A resposta nunca cria uma sessao autenticada.

### Entrar

`POST /api/auth/login`

- Requisicao JSON: `{ "email": "pessoa@exemplo.org", "password": "<senha>" }`.
- Sucesso 200: `{ "user": { "id": "<uuid>", "fullName": "Nome", "email": "pessoa@exemplo.org", "role": "colaborador" } }` e cookie de sessao com os atributos definidos nesta spec.
- Credencial invalida ou usuario inativo 401: `{ "code": "INVALID_CREDENTIALS", "message": "E-mail ou senha invalidos." }`, sem criar cookie de sessao.
- JSON invalido ou campo ausente 400: `{ "code": "INVALID_REQUEST", "message": "Informe e-mail e senha." }`.
- CSRF ausente ou invalido 403: `{ "code": "CSRF_INVALID", "message": "Atualize a pagina e tente novamente." }`.

### Consultar sessao

`GET /api/auth/session`

- Sessao valida 200: `{ "user": { "id": "<uuid>", "fullName": "Nome", "email": "pessoa@exemplo.org", "role": "colaborador" } }`, com o mesmo envelope e os mesmos quatro campos do sucesso de login.
- Sessao ausente ou expirada 401: `{ "code": "UNAUTHENTICATED", "message": "Sessao invalida ou expirada." }`.
- Conta desativada ou excluida depois do login: o mesmo 401, com a sessao encerrada e o cookie expirado. Acrescentado em 2026-11-11 por decisao do operador, porque a Spec 004 exige que as paginas redirecionem nesse caso. Nenhum CA desta spec muda.
- Este endpoint e a rota protegida usada para provar autenticacao. `GET /health`, `GET /api/auth/csrf`, `POST /api/auth/login` e `POST /api/auth/logout` nao exigem sessao; login e logout continuam exigindo CSRF valido.

### Sair

`POST /api/auth/logout`

- Com CSRF valido, responde 204 tanto para sessao valida quanto ausente.
- Quando houver sessao, ela e invalidada no servidor.
- A resposta remove o cookie de sessao.
- Para repetir o logout, o cliente primeiro executa novo `GET /api/auth/csrf` e envia o token retornado. Reutilizar token invalido ou omitir o token continua respondendo 403.

## Contrato da pagina de login

- A pagina nao usa `AppShell`.
- No desktop, ocupa a altura da viewport e usa duas areas: painel de marca com `--gradient-login` e painel de formulario sobre `--color-bg`.
- Painel de marca: marca `EmpresTI`, titulo `Equipamentos certos, nas maos certas.` e descricao `Consulte o acervo, acompanhe seus emprestimos e solicite itens disponiveis.`
- Formulario: titulo `Entrar`, apoio `Use seu e-mail institucional.`, campos rotulados `E-mail` e `Senha`, e botao primario `Entrar`.
- Todos os controles sao acessiveis por teclado, possuem rotulo associado e usam o foco definido no ADR-002.
- Durante a requisicao, o botao fica desabilitado e mostra `Entrando...`; submissao duplicada nao e enviada.
- Em 401, os dois campos recebem `aria-invalid=true` e aparece abaixo deles um bloco `role=alert` com `E-mail ou senha invalidos.`. O bloco usa o texto semantico `#F0A0A5` e borda `rgba(224,91,98,0.4)` ja definidos no ADR-002, sem criar nova cor.
- Em 400, o bloco de alerta mostra `Informe e-mail e senha.`.
- Em 403, o bloco de alerta mostra `Atualize a pagina e tente novamente.` e a pagina solicita novo token CSRF.
- Em falha de rede ou resposta inesperada, mostra `Nao foi possivel entrar. Tente novamente.` sem afirmar que a credencial esta errada.
- Apos login, a aplicacao consulta `/api/auth/session`; o destino autenticado inicial sera definido pela spec da pagina inicial e nao e inventado nesta entrega.

## Provisionamento administrativo

- A invocacao externa e `docker compose run --rm backend-provision`.
- Variaveis obrigatorias: `PROVISION_USER_EMAIL`, `PROVISION_USER_PASSWORD`, `PROVISION_USER_FULL_NAME` e `PROVISION_USER_ROLE`.
- E-mail e normalizado com trim e minusculas; papel aceita somente `colaborador` ou `operacoes`; senha deve possuir no minimo 12 caracteres.
- Se o e-mail nao existe, cria usuario ativo com senha em hash e retorna codigo de processo 0.
- Se o e-mail ja existe e nome, papel, estado ativo e senha correspondem, nao altera o registro, retorna codigo 0 e informa apenas que o usuario ja estava provisionado.
- Se o e-mail ja existe mas algum desses dados difere, nao altera o registro, retorna codigo diferente de zero e orienta usar um futuro fluxo administrativo. O comando nunca redefine credencial implicitamente.
- Nenhuma execucao cria emprestimo.
- O comando pode informar e-mail normalizado e resultado, mas nunca imprime senha, hash, cookie, token CSRF ou variaveis completas.
- Execucao em banco de producao pertence ao operador; os testes usam PostgreSQL descartavel.

## Criterios de aceitacao

- **CA-01:** pessoa sem sessao visualiza a pagina descrita nesta spec, com rotulos associados, ordem de teclado, foco visivel e submissao unica durante carregamento.
- **CA-02:** `POST /api/auth/login` com credencial valida responde 200, cria cookie com os atributos definidos e retorna somente `id`, `fullName`, `email` e `role` dentro de `user`.
- **CA-03:** e-mail inexistente, senha incorreta e usuario inativo respondem o mesmo 401 e o frontend mostra o mesmo alerta generico, sem indicar qual condicao ocorreu.
- **CA-04:** `GET /api/auth/session` sem sessao ou depois da expiracao responde o 401 definido; com sessao valida responde 200 com a identidade autenticada.
- **CA-05:** `POST /api/auth/logout` com CSRF valido responde 204, invalida a sessao e faz uma consulta posterior a `/api/auth/session` responder 401; obter novo token em `/api/auth/csrf` e repetir logout sem sessao continua respondendo 204.
- **CA-06:** o `role` retornado e o mesmo persistido em `app_user` e fica disponivel no principal do Spring Security.
- **CA-07:** respostas, cookies e logs de login, sessao, logout e provisionamento nao contem senha nem `password_hash`.
- **CA-08:** duas execucoes de `backend-provision` com os mesmos valores deixam um unico usuario e retornam 0; valores divergentes retornam erro sem alterar o registro.
- **CA-09:** o cookie de sessao e HTTP-only, sem `Expires` ou `Max-Age`, usa `SameSite=Lax` e recebe `Secure` em producao; sessao com inatividade maior ou igual a 8 horas e rejeitada sem renovacao por requisicao publica ou invalida.
- **CA-10:** login e logout sem token CSRF valido, inclusive com token reutilizado que ja nao seja aceito, respondem 403 no formato definido; cada tentativa com token novo segue o contrato de sucesso ou erro correspondente.
- **CA-11:** o navegador chama somente caminhos relativos `/api/auth/*`, e a integracao local prova que cookies de sessao atravessam o encaminhamento do frontend sem expor o identificador ao JavaScript.

## Fora de escopo

- Cadastro publico, convite, administracao, recuperacao ou troca de senha.
- Login social, SSO, autenticacao multifator, bloqueio temporario, CAPTCHA e rate limiting.
- Catalogo, emprestimo, devolucao e tela operacional funcional.
- Conteudo de `Meus emprestimos`, que pertence a futura Spec 004.
- Provisionar a conta concreta do professor, que pertence a futura Spec 003.
- Escolha e configuracao do host de producao do backend, Supabase e deploy da Vercel.
- Layout para celular ou tablet, ainda nao decidido pelo ADR-002.

## Contrato afetado

- Banco: le e cria registros em `app_user` existente; nenhuma migration nova e prevista.
- Backend: Spring Security, endpoints definidos, sessao, CSRF, principal com papel e comando administrativo.
- Frontend: pagina de login, proxy same-origin de `/api` e estados vazio, carregando, invalido, CSRF invalido e indisponibilidade.
- Testes: integracao com PostgreSQL real, teste negativo sem sessao, usuario inativo, CSRF, expiracao configurada com relogio ou timeout de teste e idempotencia do provisionamento.
- Operacao: o operador fornece variaveis no ambiente e executa `backend-provision`; nenhuma credencial e versionada.

## Decisoes encerradas pelo operador

1. Provisionamento idempotente por comando e ambiente, sem senha em migration, seed ou Git.
2. Cookie de navegador sem persistencia e expiracao no servidor por 8 horas de inatividade.
3. Usuario inativo indistinguivel de credencial invalida.
4. Bloqueio temporario e rate limiting fora da v1, registrados como risco conhecido.
5. A conta do professor sera `colaborador`, tera zero emprestimos e sera criada na Spec 003.
6. Testes de fluxo usarao outra conta e nunca alterarao a conta do professor.

Nao restam perguntas abertas nesta spec. `plan.md` e `tasks.md` so podem ser criados depois de resultado `APROVAVEL` do `spec-validator` e confirmacao humana da versao final.

## Regras acionadas quando a spec avancar

- `rules/spec-flow.md` para validacao, plano, tarefas e verificacao final independente.
- `rules/secrets.md` para credenciais, cookies e variaveis de provisionamento.
- `rules/tests.md` para integracao real com PostgreSQL e testes negativos.
- `rules/checks.md` e `rules/handoff.md` para encerramento de cada tarefa.
