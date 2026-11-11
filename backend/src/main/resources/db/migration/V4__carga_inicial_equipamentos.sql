-- Spec 003: carga inicial dos 12 equipamentos da prática.
-- Este arquivo não controla transação. O Flyway envolve a primeira aplicação;
-- uma repetição usa o mesmo arquivo com:
--   psql "<url do banco>" --single-transaction -v ON_ERROR_STOP=1 -f <este arquivo>
-- Colisão de nome, categoria ou chave de foto aborta tudo sem escrita parcial.
-- Status e IDs existentes nunca são alterados; só a chave de foto ausente é preenchida.

do $$
declare
    colisao text;
begin
    select string_agg(e.asset_code, ', ' order by e.asset_code)
      into colisao
      from equipment e
      join (values
        ('TI-0001', 'Notebook 14 polegadas', 'notebook', 'equipment/TI-0001.jpg'),
        ('TI-0002', 'Notebook 15 polegadas', 'notebook', 'equipment/TI-0002.jpg'),
        ('TI-0003', 'Notebook 13 polegadas', 'notebook', 'equipment/TI-0003.jpg'),
        ('TI-0004', 'Notebook 15 polegadas', 'notebook', 'equipment/TI-0004.jpg'),
        ('TI-0005', 'Monitor 24 polegadas', 'monitor', 'equipment/TI-0005.jpg'),
        ('TI-0006', 'Monitor 27 polegadas', 'monitor', 'equipment/TI-0006.jpg'),
        ('TI-0007', 'Monitor 22 polegadas', 'monitor', 'equipment/TI-0007.jpg'),
        ('TI-0008', 'Cabo HDMI 2 m', 'cabo', 'equipment/TI-0008.jpg'),
        ('TI-0009', 'Cabo USB-C 1 m', 'cabo', 'equipment/TI-0009.jpg'),
        ('TI-0010', 'Cabo de rede 3 m', 'cabo', 'equipment/TI-0010.jpg'),
        ('TI-0011', 'Webcam Full HD', 'camera', 'equipment/TI-0011.jpg'),
        ('TI-0012', 'Câmera compacta', 'camera', 'equipment/TI-0012.jpg')
      ) as r (asset_code, name, category, image_object_key)
        on r.asset_code = e.asset_code
     where e.name <> r.name
        or e.category <> r.category
        or (e.image_object_key is not null and e.image_object_key <> r.image_object_key);

    if colisao is not null then
        raise exception 'colisao de identidade nos patrimonios: %', colisao;
    end if;
end
$$;

insert into equipment (asset_code, name, category, status, image_object_key)
values
    ('TI-0001', 'Notebook 14 polegadas', 'notebook', 'disponivel', 'equipment/TI-0001.jpg'),
    ('TI-0002', 'Notebook 15 polegadas', 'notebook', 'disponivel', 'equipment/TI-0002.jpg'),
    ('TI-0003', 'Notebook 13 polegadas', 'notebook', 'disponivel', 'equipment/TI-0003.jpg'),
    ('TI-0004', 'Notebook 15 polegadas', 'notebook', 'manutencao', 'equipment/TI-0004.jpg'),
    ('TI-0005', 'Monitor 24 polegadas', 'monitor', 'disponivel', 'equipment/TI-0005.jpg'),
    ('TI-0006', 'Monitor 27 polegadas', 'monitor', 'disponivel', 'equipment/TI-0006.jpg'),
    ('TI-0007', 'Monitor 22 polegadas', 'monitor', 'manutencao', 'equipment/TI-0007.jpg'),
    ('TI-0008', 'Cabo HDMI 2 m', 'cabo', 'disponivel', 'equipment/TI-0008.jpg'),
    ('TI-0009', 'Cabo USB-C 1 m', 'cabo', 'disponivel', 'equipment/TI-0009.jpg'),
    ('TI-0010', 'Cabo de rede 3 m', 'cabo', 'manutencao', 'equipment/TI-0010.jpg'),
    ('TI-0011', 'Webcam Full HD', 'camera', 'disponivel', 'equipment/TI-0011.jpg'),
    ('TI-0012', 'Câmera compacta', 'camera', 'disponivel', 'equipment/TI-0012.jpg')
on conflict (asset_code) do update
   set image_object_key = excluded.image_object_key,
       updated_at = current_timestamp
 where equipment.image_object_key is null;
