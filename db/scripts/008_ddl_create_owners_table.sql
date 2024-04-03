create table owners
(
    id      serial primary key,
    name    text                                 not null,
    user_id int unique references auto_user (id) not null
);