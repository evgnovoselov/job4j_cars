package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.repository.HistoryRepository;

@Repository
@AllArgsConstructor
public class HibernateHistoryRepository implements HistoryRepository {
    private final CrudRepository crudRepository;
}
