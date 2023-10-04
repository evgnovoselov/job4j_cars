create table participates
(
    id      serial primary key,
    user_id bigint not null references auto_user (id),
    post_id bigint not null references auto_post (id),
    unique (user_id, post_id)
);