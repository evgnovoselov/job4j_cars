create table history_owners
(
    id       serial primary key,
    car_id   int references car (id) on delete cascade    not null,
    owner_id int references owners (id) on delete cascade not null,
    start_at timestamp without time zone                  not null,
    end_at   timestamp without time zone
);