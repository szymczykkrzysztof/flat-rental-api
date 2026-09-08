create table users
(
    id            bigint generated always as identity primary key,
    email         varchar(255)             not null unique,
    password_hash varchar(255)             not null,
    first_name    varchar(100)             not null,
    last_name     varchar(100)             not null,
    role          varchar(20)              not null check (role in ('ADMIN', 'OWNER', 'TENANT')),
    created_at    timestamp with time zone not null default now()
);

create table apartments
(
    id          bigint generated always as identity primary key,
    owner_id    bigint                   not null references users (id),
    title       varchar(100)             not null,
    city        varchar(100)             not null,
    street      varchar(100)             not null,
    postal_code varchar(10)              not null,
    description varchar(255)             not null,
    status      varchar(20)              not null check (status in ('AVAILABLE', 'UNAVAILABLE')),
    created_at  timestamp with time zone not null default now()

);
create index idx_apartments_owner_id on apartments (owner_id);