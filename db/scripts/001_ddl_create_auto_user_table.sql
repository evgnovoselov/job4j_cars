create table auto_user
(
    id       serial primary key,
    login    text unique not null,
    password text        not null
);