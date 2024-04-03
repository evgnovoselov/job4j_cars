package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.repository.HistoryOwnerRepository;

@Repository
@AllArgsConstructor
public class HibernateHistoryOwnerRepository implements HistoryOwnerRepository {
    private final CrudRepository crudRepository;
}
