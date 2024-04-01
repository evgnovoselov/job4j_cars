create table auto_post
(
    id           serial primary key,
    description  text                          not null,
    created      timestamp without time zone   not null default now(),
    auto_user_id int references auto_user (id) not null
);