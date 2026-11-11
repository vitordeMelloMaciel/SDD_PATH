# Plano da Spec 003 — Dados iniciais

O ADR-001 exige PostgreSQL e migrations Flyway. A carga deve preservar
dados operacionais anteriores e rodar sem segredos.

## DDL proposto para revisão

```sql
ALTER TABLE equipment ADD COLUMN image_object_key varchar(255);
```

V3 adiciona uma coluna anulável e não altera linhas existentes. A URL
pública permanece na configuração do ambiente. Uma reversão posterior
exigiria outra migration e removeria apenas as referências de foto.

## Sequência

1. Após a autorização do DDL, criar V3 e testar uma base vazia e a evolução
   da V2. Criar V4 separada com a carga de 12 itens. Uma colisão de identidade
   aborta a transação. Atualizar apenas a chave ausente quando a identidade
   coincidir e manter o estado dos itens existentes.
2. Aplicar V3 e V4 em PostgreSQL descartável. Reexecutar o SQL de V4 na mesma
   base para verificar IDs, estado após empréstimo e recusa de colisões.
3. Preparar os 12 JPEGs pela skill após configuração local da chave.
   Conferir créditos, revisão visual, dimensões, IDs e hashes no manifesto.
4. Entregar ao operador a sequência de aplicação em produção. Verificar
   por leitura o inventário e as imagens após suas ações. O operador
   provisiona a conta de avaliação usando o comando da Spec 001.
