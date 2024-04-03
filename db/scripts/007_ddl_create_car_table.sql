create table car
(
    id        serial primary key,
    name      text                       not null,
    engine_id int references engine (id) not null
);