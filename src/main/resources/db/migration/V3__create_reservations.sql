CREATE TABLE reservations
(
    id           bigint generated always as identity primary key,
    apartment_id bigint                   not null references apartments (id),
    tenant_id    bigint                   not null references users (id),
    start_date   DATE                     NOT NULL,
    end_date     DATE                     NOT NULL,
    created_at   timestamp with time zone not null default now(),
    status       VARCHAR(20)              not null check (status in ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'))
);
create index idx_reservations_apartment_id on reservations (apartment_id);
create index idx_reservations_tenant_id on reservations (tenant_id);