# Spec 003 — Dados iniciais do acervo

**Estado** · `APROVAVEL` na rodada `pre-approval-03`, base `e0cbaca`.
Aprovada pelo operador para implementação.

**Referências** · PRD itens 1 e 2, briefing de dados iniciais da prática, ADR-001,
Spec 002 (schema), Spec 001 (provisionamento) e skill `imagens-equipamentos`.
A prática chama esta entrega de Spec 002. O repositório já usa 002 para o modelo
de dados, portanto esta entrega recebe 003.

## Objetivo

Disponibilizar os 12 equipamentos da prática com fotos no Supabase Storage e
provisionar a conta de avaliação, que deve chegar à correção com zero empréstimos.

## Regras

- A lista de referência é `.agents/skills/imagens-equipamentos/references/equipment.json`.
  São quatro notebooks, três monitores, três cabos e duas câmeras. TI-0004,
  TI-0007 e TI-0010 começam em manutenção, conforme registrado em ENTREGA.md.
  Os nove restantes começam disponíveis.
- A carga é atômica e usa patrimônio como chave. Repetir a carga preserva
  IDs e a situação operacional atual. Se o patrimônio já existe com nome,
  categoria ou chave de foto divergente, a carga deve recusar a colisão sem
  gravar parcialmente. Foto ainda ausente em um item correspondente recebe
  a chave esperada. Itens fora da lista ficam intactos.
- A chave relativa no banco é `equipment/<patrimônio>.jpg`. O backend
  usará `EQUIPMENT_IMAGE_BASE_URL` para resolver a origem pública do bucket
  e fornecer a URL ao catálogo. Equipamentos anteriores
  fora da lista podem ficar sem foto.
- A skill prepara 12 JPEGs Pixabay de 800 × 600. Cada foto tem ID e hash
  únicos e revisão registrada para ausência de pessoa, marca ou logo legível,
  texto e tela com conteúdo. O manifesto vincula patrimônio, chave, origem,
  autor e licença.
- O operador faz upload usando chaves fixas, aplica as migrations em produção
  e provisiona `marcelo.grilo@institutojef.org.br` como colaborador ativo.
  O nome completo aprovado é `Marcelo Grilo`.
  A senha chega ao comando por variável de ambiente. A carga do acervo
  não cria usuários nem empréstimos. Os testes usam uma conta separada.

## Critérios de aceitação

| ID | Evidência esperada |
| --- | --- |
| CA-01 | PostgreSQL descartável migrado contém os 12 patrimônios com nome, categoria e chave da foto esperados. Há três itens em manutenção e nove disponíveis inicialmente. Na primeira carga em produção, uma leitura antes e outra após confirmam os dados dos 12 patrimônios. Para cada item inserido, a situação confere com a referência. Para cada item preexistente, a situação confere com a leitura anterior. |
| CA-02 | Repetir o SQL da carga em PostgreSQL real preserva IDs, registros externos à lista e empréstimos, incluindo o status emprestado. Uma foto ausente é preenchida. Colisões de nome, categoria ou chave são recusadas sem escrita parcial. |
| CA-03 | O manifesto cobre 12 patrimônios com IDs e hashes únicos. Cada JPEG tem 800 × 600, crédito e aprovação visual registrados. |
| CA-04 | Cada URL pública retorna 200, JPEG com dimensões 800 × 600 e SHA-256 igual ao manifesto. A listagem do prefixo após repetir o upload tem as mesmas 12 chaves e nenhum objeto adicional da carga. |
| CA-05 | No momento da entrega para correção, a conta de avaliação está ativa como colaborador e possui o nome completo aprovado. Uma consulta conta zero empréstimos abertos ou encerrados para a conta. O operador verifica o login no deploy sem solicitar item nessa conta. |
| CA-06 | O diff, a migration, o manifesto e a documentação versionada não contêm senha, chave, token ou hash de senha da conta de avaliação. |

## Contratos e escopo

- Flyway estende o modelo de equipamento e aplica a carga inicial por um
  script SQL versionado separado da alteração de schema. Para repetir a carga,
  o teste e o operador executam esse mesmo arquivo em transação única. Rodar
  somente `flyway migrate` após aplicar a versão não conta como repetição.
- O catálogo, o crédito visível `Fotos: Pixabay`, a solicitação e Meus
  empréstimos ficam na próxima spec. Cadastro pela interface, importação
  da planilha, devolução e tela de Operações ficam fora desta entrega.

## Pendências

- Configurar `PIXABAY_API_KEY` no ambiente local e preparar as fotos.
- Identificar a URL pública do bucket e obter evidência das ações do operador
  no banco e no Storage no momento da publicação.
