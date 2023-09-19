create table author
(
    id              serial primary key,
    fio             text unique not null,
    creation_date   timestamp not null
);

alter table budget
add author_id int references author(id);

