create table file
(
    id   serial primary key,
    name text        not null,
    path text unique not null
);