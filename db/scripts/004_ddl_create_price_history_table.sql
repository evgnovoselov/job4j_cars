create table price_history
(
    id           serial primary key,
    before       bigint not null,
    after        bigint not null,
    created      timestamp without time zone default now(),
    auto_post_id int references auto_post (id)
);