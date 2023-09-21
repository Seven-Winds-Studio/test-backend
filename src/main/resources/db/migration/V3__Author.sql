create table author
(
    id   serial primary key,
    fio  varchar,
    date timestamp default now()
);

alter table budget
add column author_id int default null,
add constraint fk_budget_author foreign key (author_id) references author(id);
