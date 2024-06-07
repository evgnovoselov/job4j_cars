package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.model.Engine;
import ru.job4j.cars.repository.EngineRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
@Slf4j
public class HibernateEngineRepository implements EngineRepository {
    private final CrudRepository crudRepository;

    @Override
    public Engine create(Engine engine) {
        try {
            crudRepository.run(session -> session.persist(engine));
        } catch (Exception e) {
            log.error("Error create engine", e);
        }
        return engine;
    }

    @Override
    public Collection<Engine> findAll() {
        try {
            return crudRepository.query("from Engine", Engine.class);
        } catch (Exception e) {
            log.error("Error find all engine", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Engine> findById(int id) {
        try {
            return crudRepository.optional(
                    "from Engine where id = :id",
                    Engine.class,
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error find by id engine where id = {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Engine engine) {
        try {
            crudRepository.run(session -> session.merge(engine));
        } catch (Exception e) {
            log.error("Error update engine where id = {}", engine.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        try {
            crudRepository.run(
                    "delete from Engine where id = :id",
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error delete Engine where id = {}", id, e);
        }
    }
}
