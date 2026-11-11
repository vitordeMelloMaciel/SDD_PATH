---
name: imagens-equipamentos
description: Buscar, revisar e preparar fotos de equipamentos pela Pixabay API para o catalogo deste repositorio. Use ao criar ou substituir imagens do acervo; nao use para publicar no Supabase nem para imagens de marca, landing page ou ilustracoes.
---

# Imagens de equipamentos

Prepare imagens rastreaveis e repetiveis sem escrever em producao.

## Antes de executar

1. Leia `AGENTS.md`, `rules/restrictions.md`, `rules/secrets.md` e a spec ativa.
2. Leia [references/api-contract.md](references/api-contract.md) antes de consultar a API.
3. Use somente os equipamentos e buscas de [references/equipment.json](references/equipment.json), salvo decisao registrada na spec.
4. Exija `PIXABAY_API_KEY` no ambiente. Nunca mostre, copie para argumento de linha de comando ou grave a chave.

## Fluxo

1. Em `.agents/skills/imagens-equipamentos/scripts`, instale as dependencias pelo lockfile com `pnpm install --frozen-lockfile`.
2. Na raiz do repositorio, rode `node .agents/skills/imagens-equipamentos/scripts/prepare-images.mjs search`.
3. Revise visualmente todos os candidatos em `.local/pixabay-equipment/candidates/`.
4. Reprove qualquer foto com pessoa, marca ou logo legivel, texto, ou tela com conteudo. Nao presuma que a busca ou `safesearch` prova esses requisitos.
5. Preencha `.local/pixabay-equipment/approvals.json` com um candidato por patrimonio, o revisor e os quatro controles visuais.
6. Rode `node .agents/skills/imagens-equipamentos/scripts/prepare-images.mjs finalize`.
7. Confira o manifesto final, dimensoes, hashes e ausencia de IDs repetidos antes de propor seed ou upload.

## Garantias

- A busca usa somente `https://pixabay.com/api/`, com cache local de 24 horas.
- O resultado final possui exatamente 800x600, JPEG sem metadados herdados e nome deterministico por patrimonio.
- IDs da Pixabay e hashes finais nao se repetem.
- O manifesto registra pagina de origem, autor, perfil do autor e Pixabay Content License.
- A interface que usar as fotos exibe `Fotos: Pixabay` com link para `https://pixabay.com/`.
- A skill nao possui comando, chave ou cliente para Supabase. O manifesto apenas sugere chaves deterministicas `equipment/<patrimonio>.jpg`, permitindo upload idempotente pelo operador.

## Pare e devolva ao operador

- chave ausente ou API indisponivel;
- nenhum candidato que passe na revisao visual;
- pedido para usar outra fonte, hotlink permanente ou imagem repetida;
- pedido para enviar arquivos ao banco, Storage ou deploy;
- necessidade de instalar dependencia diferente da registrada no lockfile.
