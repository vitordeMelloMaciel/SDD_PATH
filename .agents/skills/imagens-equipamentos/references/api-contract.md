# Contrato da Pixabay API

Fonte oficial: <https://pixabay.com/api/docs/>

## Endpoint e credencial

- Buscar imagens com `GET https://pixabay.com/api/`.
- Enviar a chave somente no parametro `key`, construida pelo script a partir de `PIXABAY_API_KEY`.
- Nunca registrar a URL completa da requisicao porque ela contem a chave.

## Parametros desta skill

- `q`: busca definida para o equipamento.
- `image_type=photo`.
- `orientation=horizontal`.
- `safesearch=true`.
- `min_width=800` e `min_height=600`.
- `per_page=20`; somente tres candidatos unicos sao baixados para revisao.

## Limites que alteram o fluxo

- Respostas de busca ficam em cache local por pelo menos 24 horas.
- URLs retornadas podem expirar e nao podem ser usadas como hotlink permanente.
- A imagem escolhida e baixada, processada e depois armazenada pelo operador.
- O credito `Fotos: Pixabay` com link deve aparecer na tela que exibe as fotos.
- `safesearch` nao verifica pessoa, marca, logo, texto ou tela; esses itens exigem revisao visual.

## Campos preservados no manifesto

- `id` como identificador unico da foto;
- `pageURL` como origem;
- `user` como fotografo/contribuidor;
- perfil derivado de `https://pixabay.com/users/<user>-<user_id>/`;
- licenca `Pixabay Content License`, com <https://pixabay.com/service/license-summary/>.
