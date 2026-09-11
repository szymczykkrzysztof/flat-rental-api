CREATE TABLE refresh_tokens
(
    id         bigint generated always as identity primary key,
    token      varchar(255)             not null unique,
    user_id    bigint                   not null references users (id),
    revoked    boolean                  not null default false,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at timestamp with time zone not null default now()
);
create index idx_refresh_tokens_user_id on refresh_tokens (user_id);