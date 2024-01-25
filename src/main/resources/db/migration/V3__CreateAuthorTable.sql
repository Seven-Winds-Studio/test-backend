CREATE TABLE author
(
    id         serial primary key,
    full_name  text not null,
    created_at timestamptz default now()
);