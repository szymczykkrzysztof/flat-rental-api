create table health_check
(
    id         bigint generated always as identity primary key,
    status     varchar(32)              not null,
    checked_at timestamp with time zone not null default now()
);