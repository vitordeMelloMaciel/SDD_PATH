create extension if not exists pgcrypto;

create table app_user (
    id uuid primary key default gen_random_uuid(),
    email varchar(254) not null,
    password_hash varchar(255) not null,
    full_name varchar(160) not null,
    role varchar(20) not null,
    active boolean not null default true,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint ck_app_user_email_normalized
        check (email = lower(btrim(email)) and length(email) > 3),
    constraint uq_app_user_email unique (email),
    constraint ck_app_user_role check (role in ('colaborador', 'operacoes'))
);

create table equipment (
    id uuid primary key default gen_random_uuid(),
    asset_code varchar(80) not null,
    name varchar(160) not null,
    category varchar(80) not null,
    status varchar(20) not null default 'disponivel',
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint uq_equipment_asset_code unique (asset_code),
    constraint ck_equipment_status
        check (status in ('disponivel', 'emprestado', 'manutencao'))
);

create table loan (
    id uuid primary key default gen_random_uuid(),
    borrower_id uuid not null references app_user (id),
    equipment_id uuid not null references equipment (id),
    loaned_at timestamp with time zone not null default current_timestamp,
    due_at timestamp with time zone not null,
    returned_at timestamp with time zone,
    return_registered_by_id uuid references app_user (id),
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint ck_loan_due_after_start check (due_at > loaned_at),
    constraint ck_loan_return_after_start
        check (returned_at is null or returned_at >= loaned_at),
    constraint ck_loan_return_operator
        check (returned_at is null or return_registered_by_id is not null)
);

create unique index uq_loan_open_equipment
    on loan (equipment_id)
    where returned_at is null;

create index ix_loan_open_borrower
    on loan (borrower_id, due_at)
    where returned_at is null;

create function validate_loan_opening()
returns trigger
language plpgsql
as $$
declare
    borrower_active boolean;
    equipment_status varchar(20);
    open_loan_count integer;
begin
    if new.returned_at is not null then
        raise exception 'loan must be opened before it can be returned';
    end if;

    if new.due_at is null then
        new.due_at := new.loaned_at + interval '14 days';
    end if;

    select active
      into borrower_active
      from app_user
     where id = new.borrower_id
       for update;

    if not found or not borrower_active then
        raise exception 'borrower must exist and be active';
    end if;

    select status
      into equipment_status
      from equipment
     where id = new.equipment_id
       for update;

    if not found or equipment_status <> 'disponivel' then
        raise exception 'equipment must be available';
    end if;

    select count(*)
      into open_loan_count
      from loan
     where borrower_id = new.borrower_id
       and returned_at is null;

    if open_loan_count >= 3 then
        raise exception 'borrower already has three open loans';
    end if;

    if exists (
        select 1
          from loan
         where borrower_id = new.borrower_id
           and returned_at is null
           and due_at < current_timestamp
    ) then
        raise exception 'borrower has an overdue loan';
    end if;

    return new;
end;
$$;

create trigger trg_validate_loan_opening
before insert on loan
for each row execute function validate_loan_opening();

create function validate_loan_update()
returns trigger
language plpgsql
as $$
begin
    if new.borrower_id <> old.borrower_id
       or new.equipment_id <> old.equipment_id
       or new.loaned_at <> old.loaned_at
       or new.due_at <> old.due_at then
        raise exception 'loan identity and dates cannot be changed after opening';
    end if;

    if old.returned_at is not null and new.returned_at is distinct from old.returned_at then
        raise exception 'returned loan cannot be reopened or have its return date changed';
    end if;

    new.updated_at := current_timestamp;
    return new;
end;
$$;

create trigger trg_validate_loan_update
before update on loan
for each row execute function validate_loan_update();

create function sync_equipment_status_from_loan()
returns trigger
language plpgsql
as $$
begin
    if tg_op = 'INSERT' then
        update equipment
           set status = 'emprestado', updated_at = current_timestamp
         where id = new.equipment_id;
    elsif old.returned_at is null and new.returned_at is not null then
        update equipment
           set status = 'disponivel', updated_at = current_timestamp
         where id = new.equipment_id;
    end if;

    return new;
end;
$$;

create trigger trg_sync_equipment_status_from_loan
after insert or update of returned_at on loan
for each row execute function sync_equipment_status_from_loan();
