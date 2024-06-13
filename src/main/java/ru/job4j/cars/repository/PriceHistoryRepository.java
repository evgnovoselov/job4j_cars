package ru.job4j.cars.repository;

import ru.job4j.cars.model.PriceHistory;

import java.util.Collection;
import java.util.Optional;

public interface PriceHistoryRepository {
    PriceHistory create(PriceHistory priceHistory);

    Collection<PriceHistory> findAll();

    Optional<PriceHistory> findById(int id);

    void update(PriceHistory priceHistory);

    void delete(int id);
}
