package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.model.PriceHistory;
import ru.job4j.cars.repository.PriceHistoryRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
@Slf4j
public class HibernatePriceHistoryRepository implements PriceHistoryRepository {
    private final CrudRepository crudRepository;

    @Override
    public PriceHistory create(PriceHistory priceHistory) {
        try {
            crudRepository.run(session -> session.persist(priceHistory));
        } catch (Exception e) {
            log.error("Error create price history", e);
        }
        return priceHistory;
    }

    @Override
    public Collection<PriceHistory> findAll() {
        try {
            return crudRepository.query("from PriceHistory", PriceHistory.class);
        } catch (Exception e) {
            log.error("Error find all price history", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<PriceHistory> findById(int id) {
        try {
            return crudRepository.optional(
                    "from PriceHistory where id = :id",
                    PriceHistory.class,
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error find by id price history where id = {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public void update(PriceHistory priceHistory) {
        try {
            crudRepository.run(session -> session.merge(priceHistory));
        } catch (Exception e) {
            log.error("Error update price history where id = {}", priceHistory.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        try {
            crudRepository.run(
                    "delete from PriceHistory where id = :id",
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error delete price history where id = {}", id, e);
        }
    }
}
