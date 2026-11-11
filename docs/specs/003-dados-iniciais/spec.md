# Spec 003 — Dados iniciais do acervo

**Estado** · Aprovada pelo operador em 2026-11-11, após `APROVAVEL` na rodada
`pre-approval-03-8d40630`. O operador também aprovou `plan.md`, `tasks.md` e o
DDL da V3 (`ALTER TABLE equipment ADD COLUMN image_object_key varchar(255)`).
A V4 ainda não foi aprovada. Histórico: a rodada `pre-approval-01-94a8197`
retornou `BLOQUEADA`, e a `pre-approval-02-94a8197`, `APROVAVEL`. Uma versão
anterior deste cabeçalho citava uma rodada `pre-approval-03` com base `e0cbaca`,
que não existe para esta spec: aquele relatório pertence à Spec 001.

**Referências** · PRD itens 1 e 2, briefing de dados iniciais da prática, ADR-001,
Spec 002 (schema), Spec 001 (provisionamento) e skill `imagens-equipamentos`.
A prática chama esta entrega de Spec 002. O repositório já usa 002 para o modelo
de dados, portanto esta entrega recebe 003.

## Objetivo

Disponibilizar os 12 equipamentos da prática com fotos no Supabase Storage e
provisionar a conta de avaliação, que deve chegar à correção com zero empréstimos.

## Regras

- A lista de referência é `.agents/skills/imagens-equipamentos/references/equipment.json`
  na versão do commit `8052d1b`. São quatro notebooks, três monitores, três
  cabos e duas câmeras. TI-0004, TI-0007 e TI-0010 começam em manutenção,
  conforme o campo `status` do arquivo e a decisão registrada em ENTREGA.md.
  Os nove restantes começam disponíveis. Qualquer alteração dessa lista exige
  nova versão desta spec.
- A carga é atômica e usa patrimônio como chave. Repetir a carga preserva
  IDs e a situação operacional atual. Se o patrimônio já existe com nome,
  categoria ou chave de foto divergente, a carga deve recusar a colisão sem
  gravar parcialmente. Foto ainda ausente em um item correspondente recebe
  a chave esperada. Itens fora da lista ficam intactos.
- A chave relativa da foto é `equipment/<patrimônio>.jpg`, gravada em
  `equipment.image_object_key`. Equipamentos anteriores fora da lista podem
  ficar sem foto.
- A skill prepara 12 JPEGs Pixabay de 800 × 600. Cada foto tem ID e hash
  únicos e revisão registrada para ausência de pessoa, marca ou logo legível,
  texto e tela com conteúdo. O manifesto vincula patrimônio, chave, origem,
  autor e licença.
- O operador faz upload usando chaves fixas, aplica as migrations em produção
  e provisiona `marcelo.grilo@institutojef.org.br` como colaborador ativo.
  O nome completo aprovado é `Marcelo Grilo`.
  A senha chega ao comando por variável de ambiente. A carga do acervo
  não cria usuários nem empréstimos. Os testes usam uma conta separada.

## Contrato afetado

### Banco

- `V3__equipment_image_object_key.sql` adiciona a coluna
  `equipment.image_object_key varchar(255)`, anulável, sem valor padrão, sem
  CHECK de formato e sem unicidade. O DDL é o proposto em `plan.md` e só é
  gravado após aprovação específica do operador, conforme `rules/migration.md`.
  Linhas existentes ficam com `null`.
- `V4__carga_inicial_equipamentos.sql` contém somente a carga dos 12 itens,
  separada da alteração de schema. O formato da chave é garantido pela carga.
  O arquivo não contém `BEGIN`, `COMMIT` nem `ROLLBACK`: a transação vem do
  Flyway na primeira aplicação e de quem o reexecuta nas repetições.

### Configuração

- `EQUIPMENT_IMAGE_BASE_URL` é a origem pública do prefixo do bucket no
  Supabase Storage, sem barra final, por exemplo
  `https://<projeto>.supabase.co/storage/v1/object/public/<bucket>`.
  Não é segredo.
- O backend usa a variável para montar a URL da foto:
  `EQUIPMENT_IMAGE_BASE_URL + "/" + image_object_key`. Sem variável ou sem
  chave, a URL é `null`.
- O frontend usa a mesma variável no `next.config.ts` apenas para autorizar
  essa origem e esse caminho no otimizador de imagens. Sem a variável, nenhuma
  origem remota é autorizada.
- Implementar a variável no backend, no `next.config.ts` e em `.env.example`
  (sem valor real, conforme `rules/secrets.md`) é tarefa desta spec, porque é
  ela que define o bucket e as chaves. A Spec 004 só consome a URL pronta.

### Repetição da carga

- No teste, Testcontainers aplica todas as migrations em PostgreSQL descartável
  e executa de novo o conteúdo de `V4__carga_inicial_equipamentos.sql`, lido
  do classpath, numa única transação da mesma base.
- Em produção, o operador repete a carga com
  `psql "<url do banco>" --single-transaction -v ON_ERROR_STOP=1 -f backend/src/main/resources/db/migration/V4__carga_inicial_equipamentos.sql`,
  usando o mesmo arquivo versionado, sem edição. O operador fornece a URL do
  banco fora do Git. Por ser o arquivo da migration versionada, e não SQL
  escrito à parte, essa execução não é SQL avulso nem altera schema. O arquivo
  inalterado mantém o checksum registrado no `flyway_schema_history`.
- Rodar somente `flyway migrate` após aplicar a versão não conta como repetição.

### Escopo

- O catálogo, o crédito visível `Fotos: Pixabay`, a solicitação e Meus
  empréstimos ficam na Spec 004. Cadastro pela interface, importação
  da planilha, devolução e tela de Operações ficam fora desta entrega.

## Critérios de aceitação

| ID | Evidência esperada | Onde fica a evidência |
| --- | --- | --- |
| CA-01 | PostgreSQL descartável migrado contém os 12 patrimônios com nome, categoria e chave da foto esperados. Há três itens em manutenção e nove disponíveis inicialmente. Na primeira carga em produção, uma leitura antes e outra após confirmam os dados dos 12 patrimônios. Para cada item inserido, a situação confere com a referência. Para cada item preexistente, a situação confere com a leitura anterior. | Teste de integração em `backend/src/test/java/`; leituras de produção em `docs/specs/003-dados-iniciais/verification/producao-<AAAA-MM-DD>.md`. |
| CA-02 | O mesmo arquivo `V4__carga_inicial_equipamentos.sql` é executado de novo conforme “Repetição da carga”, no teste e em produção. A repetição preserva IDs, registros fora da lista e empréstimos, incluindo o status emprestado. Uma foto ausente é preenchida. O teste cobre ao menos três colisões em itens preexistentes: nome divergente, categoria divergente e chave de foto divergente. Cada colisão encerra com erro, e a tabela `equipment` fica igual à leitura anterior. Em produção, o `psql` retorna código 0 e a leitura posterior é igual à anterior. | Teste de integração em `backend/src/test/java/`; em produção, comando, código de saída e leituras antes e depois em `verification/producao-<AAAA-MM-DD>.md`. |
| CA-03 | O manifesto cobre 12 patrimônios com IDs e hashes únicos. Cada JPEG tem 800 × 600, crédito e aprovação visual registrados. | `final-manifest.json` e `approvals.json` da skill, copiados sem alteração para `docs/specs/003-dados-iniciais/verification/manifesto/`. O campo do revisor (`reviewer` e `reviewedBy`) contém somente o nome de quem revisou, sem e-mail; esse nome é a evidência da revisão visual. A cópia não contém chave de API. |
| CA-04 | Cada URL pública `EQUIPMENT_IMAGE_BASE_URL/equipment/<patrimônio>.jpg` retorna 200, JPEG com 800 × 600 e SHA-256 igual ao manifesto. A listagem do prefixo `equipment/` após repetir o upload tem exatamente as 12 chaves `equipment/TI-0001.jpg` a `equipment/TI-0012.jpg`, sem outro objeto. Com `EQUIPMENT_IMAGE_BASE_URL` configurada, a URL que o backend monta para cada patrimônio é igual a essa URL pública. Sem a variável ou sem chave, a URL montada é `null`. O `.env.example` declara a variável sem valor real. | Requisições e listagem, com data e hora em `America/Sao_Paulo`, em `verification/producao-<AAAA-MM-DD>.md`. Composição da URL em teste automatizado em `backend/src/test/java/`. |
| CA-05 | No dia da entrega para correção, a conta de avaliação está ativa como colaborador e tem o nome completo aprovado. Uma consulta conta zero empréstimos abertos ou encerrados para a conta. O operador confirma o login no deploy sem solicitar item nessa conta. | Consulta SQL de leitura, resultado e data e hora em `America/Sao_Paulo` em `verification/producao-<AAAA-MM-DD>.md`, sem senha nem hash. |
| CA-06 | O diff, a migration, o manifesto e a documentação versionada não contêm senha, chave, token ou hash de senha da conta de avaliação. | Busca no diff registrada na rodada final de verificação. |

## Perguntas em aberto

Nenhuma pergunta de negócio pendente.

## Pendências operacionais

- Configurar `PIXABAY_API_KEY` no ambiente local e preparar as fotos que faltam.
- Identificar a URL pública do bucket para `EQUIPMENT_IMAGE_BASE_URL`.
- Obter evidência das ações do operador no banco e no Storage no momento da
  publicação.
