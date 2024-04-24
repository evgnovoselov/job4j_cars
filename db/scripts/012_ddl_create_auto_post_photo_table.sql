create table auto_post_photo
(
    id           serial primary key,
    auto_post_id int references auto_post (id) on delete cascade not null,
    file_id      int references file (id) on delete cascade      not null,
    sort         int                                             not null default 1000,
    unique (auto_post_id, file_id)
)