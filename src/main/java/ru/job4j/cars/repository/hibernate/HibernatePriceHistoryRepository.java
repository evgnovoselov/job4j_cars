package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.repository.PriceHistoryRepository;

@Repository
@AllArgsConstructor
public class HibernatePriceHistoryRepository implements PriceHistoryRepository {
    private final CrudRepository crudRepository;
}
