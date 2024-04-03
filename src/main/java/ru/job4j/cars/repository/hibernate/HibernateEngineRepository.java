package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.repository.EngineRepository;

@Repository
@AllArgsConstructor
public class HibernateEngineRepository implements EngineRepository {
    private final CrudRepository crudRepository;
}
