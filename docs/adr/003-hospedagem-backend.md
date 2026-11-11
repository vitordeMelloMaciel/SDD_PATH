# ADR-003 — Hospedagem do backend Spring

**Status:** aceito (decisão do operador em 2026-11-11)
**Contexto do produto:** docs/PRD.md

## Contexto

O ADR-001 colocou o front na Vercel e o banco em PostgreSQL, que em produção é
o Supabase exigido pela prática. Ele não decidiu onde roda o backend Spring Boot. O front encaminha `/api/*` para
`INTERNAL_API_BASE_URL`, para manter as chamadas do navegador na mesma origem.
Sem um host do backend, o login não funciona em produção: hoje
`GET /api/auth/csrf` retorna 404 na Vercel.

O host precisa:

- rodar o `Dockerfile` existente em `backend/`;
- servir HTTPS;
- conectar ao Supabase pelo pooler JDBC com `sslmode=require`;
- aceitar `SESSION_COOKIE_SECURE=true` em produção.

O professor testa o login no link publicado. Por isso, cold start é um critério
de escolha.

## Decisão

| item | escolha |
|---|---|
| host do backend | Railway |
| build | `Dockerfile` existente em `backend/` |
| endereço | domínio HTTPS fornecido pelo Railway, configurado como `INTERNAL_API_BASE_URL` na Vercel |
| banco | Supabase pelo pooler JDBC, com `sslmode=require` |
| sessão | `SESSION_COOKIE_SECURE=true` em produção |
| segredos | variáveis de ambiente do serviço no Railway; nenhuma credencial no repositório |

## Justificativa

- O Railway faz o build do `Dockerfile` existente sem adaptar o backend.
- O serviço recebe um domínio HTTPS, o que satisfaz o cookie `Secure` da Spec 001.
- Entre as opções avaliadas, é a única que roda o `Dockerfile` com HTTPS sem os impedimentos registrados nas alternativas descartadas.
- O teste do Railway oferece 1 GB de memória, 30 dias e US$ 5 de crédito. A imagem final foi validada localmente com limite de 1 GB.

## Alternativas descartadas

| item | alternativa | motivo do descarte |
|---|---|---|
| host do backend | Render gratuito | dorme após 15 minutos sem uso e leva cerca de 1 minuto para acordar, o que prejudica o login do professor |
| host do backend | Fly.io | exige cartão depois do teste de 7 dias |
| host do backend | Google Cloud Run | exige cartão |

## Consequências

- O que fica mais fácil: publicar o backend a partir do mesmo `Dockerfile` usado localmente e ligar a Vercel ao Spring com uma única variável, `INTERNAL_API_BASE_URL`.
- O que fica mais difícil: o teste do Railway é limitado a 1 GB de memória, 30 dias e US$ 5 de crédito; depois, o crédito é de US$ 1 por mês. Quando o crédito ou o prazo acabar, o backend pode parar, e o login em produção volta a falhar até o crédito ser renovado ou o host trocado. O backend precisa caber em 1 GB.
- O que essa escolha nos impede de fazer depois sem custo: trocar de host exige recriar as variáveis de ambiente no novo serviço e atualizar `INTERNAL_API_BASE_URL` na Vercel com um novo deploy do front. O código não muda.
- Nenhuma credencial entra no repositório. Senha do banco, URL JDBC e demais segredos ficam só nas variáveis do Railway e da Vercel. `.env.example` continua sem valores reais.

## O que este ADR NÃO decide

Criação da conta e do serviço no Railway, execução do deploy, região, plano
pago, domínio próprio, monitoramento e alertas. Deploy e gravação de segredos
em serviço remoto exigem autorização explícita do operador no momento da ação
(`rules/restrictions.md`).
