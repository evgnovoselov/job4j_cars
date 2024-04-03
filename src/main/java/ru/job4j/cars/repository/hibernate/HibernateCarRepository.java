package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.repository.CarRepository;

@Repository
@AllArgsConstructor
public class HibernateCarRepository implements CarRepository {
    private final CrudRepository crudRepository;
}
