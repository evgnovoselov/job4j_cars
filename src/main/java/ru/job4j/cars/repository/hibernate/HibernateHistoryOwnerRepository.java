package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.model.HistoryOwner;
import ru.job4j.cars.repository.HistoryOwnerRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
@Slf4j
public class HibernateHistoryOwnerRepository implements HistoryOwnerRepository {
    private final CrudRepository crudRepository;

    @Override
    public HistoryOwner create(HistoryOwner historyOwner) {
        try {
            crudRepository.run(session -> session.persist(historyOwner));
        } catch (Exception e) {
            log.error("Error create history owner", e);
        }
        return historyOwner;
    }

    @Override
    public Collection<HistoryOwner> findAll() {
        try {
            return crudRepository.query("from HistoryOwner", HistoryOwner.class);
        } catch (Exception e) {
            log.error("Error find all history owner", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<HistoryOwner> findById(int id) {
        try {
            return crudRepository.optional(
                    "from HistoryOwner where id = :id",
                    HistoryOwner.class,
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error find by id history owner where id = {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public void update(HistoryOwner historyOwner) {
        try {
            crudRepository.run(session -> session.merge(historyOwner));
        } catch (Exception e) {
            log.error("Error update history owner where id = {}", historyOwner.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        try {
            crudRepository.run(
                    "delete from HistoryOwner where id = :id",
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error delete history owner where id = {}", id, e);
        }
    }
}
