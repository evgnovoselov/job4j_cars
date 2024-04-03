create table history
(
    id               serial primary key,
    history_owner_id int unique references history_owners (id) on delete cascade not null,
    start_at         timestamp without time zone                                 not null,
    end_at           timestamp without time zone
);