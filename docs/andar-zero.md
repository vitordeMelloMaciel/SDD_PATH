# Andar zero — prova do ADR-001

Isto não é uma spec: não há comportamento de produto aqui.
É a verificação de que a stack decidida em docs/adr/001-stack.md funciona junta, numa máquina limpa.

## Critérios de aceitação

- AZ-01 · um comando único sobe a aplicação e o Postgres do zero
- AZ-02 · a aplicação conecta no banco; sem banco, erro claro e imediato
- AZ-03 · existe uma migration versionada e aplicada
- AZ-04 · GET /health devolve 200 e o status da conexão com o banco
- AZ-05 · uma página servida pelo front carrega e mostra o resultado de AZ-04
- AZ-06 · um comando roda os testes; existe pelo menos um teste sobre AZ-04
- AZ-07 · configuração via variável de ambiente, com .env.example versionado
- AZ-08 · .env no .gitignore, nenhum segredo no repositório

## Fora de escopo

Qualquer funcionalidade do PRD. Nada de login, catálogo, empréstimo ou devolução.
Nenhuma tabela de domínio além da mínima para AZ-03.

## Não decidido ainda

Regras de negócio, endpoints de produto, telas de produto, seed de dados e formato de erros de domínio ainda não foram decididos.
