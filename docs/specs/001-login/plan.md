# Plano - Spec 001 Login e sessao

## Objetivo do plano

Implementar a Spec 001 aprovada como uma fatia autenticavel e testavel, preservando o schema existente da Spec 002 e os contratos do ADR-001 e ADR-002.

## Restricoes

- Stack e dependencias de autenticacao seguem `docs/adr/001-stack.md`: Java 21, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL e sessao HTTP.
- A pagina segue `docs/adr/002-design-system.md`; nao usa `AppShell`, CDN, estilo inline, cor arbitraria ou novo tema.
- Nao existe migration nesta spec. `app_user` e V2 permanecem inalteradas.
- O navegador chama somente `/api`; a URL interna do backend nao e exposta ao cliente.
- Nenhuma credencial concreta entra em codigo, teste, log, resposta ou Git.
- Nao sera adicionada biblioteca de teste frontend fora do ADR. A pagina sera validada por typecheck, build, teste de integracao do contrato e verificacao no navegador local.

## Sequencia

### 1. Fundacao de identidade e seguranca

- Adicionar apenas dependencias ja autorizadas pelo ADR-001: Spring Security e Spring Data JPA.
- Mapear `app_user` sem alterar o schema.
- Implementar repositorio por e-mail normalizado, principal autenticado e `PasswordEncoder`.
- Configurar regras publicas e protegidas, cookie de sessao e formato JSON dos erros de seguranca.
- Criar testes negativos para usuario inexistente, senha incorreta e usuario inativo.

Atende: CA-02, CA-03, CA-06, CA-07 e parte de CA-09.

### 2. Contrato HTTP, sessao, logout e CSRF

- Implementar `GET /api/auth/csrf`, `POST /api/auth/login`, `GET /api/auth/session` e `POST /api/auth/logout` exatamente como a spec.
- Configurar expiracao por 8 horas de inatividade e diferenciar cookie de sessao do cookie/token CSRF.
- Tornar logout publico quanto a autenticacao, protegido por CSRF e idempotente com token novo.
- Cobrir status, corpos, cookie, principal, expiracao e ciclo de logout em testes Spring Boot com PostgreSQL descartavel.

Atende: CA-02 a CA-07, CA-09 e CA-10.

### 3. Provisionamento administrativo

- Criar servico reutilizavel de provisionamento e modo de execucao sem servidor web.
- Adicionar `backend-provision` ao Docker Compose, recebendo somente as quatro variaveis definidas na spec.
- Validar normalizacao, papel, tamanho minimo de senha, no-op idempotente e erro sem mutacao para dados divergentes.
- Testar criacao, repeticao, divergencia, ausencia de emprestimo e ausencia de segredo na saida.
- Documentar nomes das variaveis em `.env.example` sem valores reais.

Atende: CA-07 e CA-08.

### 4. Pagina de login e encaminhamento same-origin

- Configurar o Next.js para encaminhar `/api/*` ao backend pela variavel interna do servidor.
- Substituir a raiz demonstrativa pela pagina de login da spec sem apagar componentes do Design System que serao reutilizados nas paginas seguintes.
- Implementar obtencao/renovacao do CSRF, submissao unica, estados de carregamento e mensagens por status.
- Garantir rotulos, teclado, foco, `aria-invalid`, `role=alert`, CSP e ausencia do identificador de sessao no JavaScript.
- Rodar typecheck e build do frontend e conferir a pagina no navegador local em largura desktop.

Atende: CA-01, CA-03, CA-10 e CA-11.

### 5. Integracao e verificacao independente

- Rodar todos os testes de backend com Testcontainers.
- Rodar build dos containers backend e frontend.
- Subir o ambiente local e provar login valido, tres casos indistinguiveis de 401, sessao, logout repetido e ausencia de segredo.
- Executar o `spec-validator` em contexto limpo no modo verificacao e salvar a tabela por CA em `verification/`.
- Corrigir qualquer reprova antes de atualizar `handoff.md`, `ENTREGA.md` e publicar a tarefa.

Atende: CA-01 a CA-11.

## Arquivos e areas esperadas

- `backend/pom.xml`
- `backend/src/main/java/br/org/germinare/equipamentos/auth/**`
- `backend/src/main/java/br/org/germinare/equipamentos/user/**`
- `backend/src/main/resources/application.yml`
- `backend/src/test/java/br/org/germinare/equipamentos/auth/**`
- `docker-compose.yml`
- `.env.example`
- `frontend/next.config.ts`
- `frontend/app/page.tsx`
- `frontend/components/login-page.tsx`

Os nomes internos podem ser ajustados para manter coesao, mas nao podem criar uma camada ou biblioteca nao prevista neste plano.

## Estrategia de teste

- Teste de integracao usa PostgreSQL real por Testcontainers e todas as migrations existentes.
- Cada teste cita o CA correspondente.
- Autorizacao, usuario inativo, CSRF e acesso sem sessao possuem casos negativos.
- Timeout de teste pode ser reduzido por configuracao para provar a mesma semantica de expiracao sem esperar 8 horas.
- Frontend passa por `pnpm --dir frontend run typecheck` e `pnpm --dir frontend run build`.
- Verificacao manual local confere layout, foco, teclado, mensagens e cookies; nao usa conta do professor.

## Reversao

Como nao ha migration, a reversao consiste em reverter os commits da Spec 001 e remover as variaveis de provisionamento do ambiente. Nenhuma tabela ou dado existente deve ser apagado.

## Riscos acompanhados

- Sem rate limiting na v1: risco aceito e registrado na spec.
- Sessao em memoria exige uma unica instancia ou afinidade enquanto armazenamento compartilhado nao for decidido.
- `Secure` e o encaminhamento same-origin de producao dependem do futuro host do backend; esta spec prova configuracao e comportamento local, nao escolhe o provedor.
- Apos login ainda nao existe jornada de negocio completa; dados iniciais e catalogo entram nas proximas specs.
