create table if not exists az_marker (
    id integer primary key,
    applied_at timestamp with time zone not null default now()
);

insert into az_marker (id)
values (1)
on conflict (id) do nothing;
