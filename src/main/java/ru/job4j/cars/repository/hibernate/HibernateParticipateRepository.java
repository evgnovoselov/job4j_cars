package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.model.Participate;
import ru.job4j.cars.repository.ParticipateRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
@Slf4j
public class HibernateParticipateRepository implements ParticipateRepository {
    private final CrudRepository crudRepository;

    @Override
    public Participate create(Participate participate) {
        try {
            crudRepository.run(session -> session.persist(participate));
        } catch (Exception e) {
            log.error("Error create participate", e);
        }
        return participate;
    }

    @Override
    public Collection<Participate> findAll() {
        try {
            return crudRepository.query("from Participate", Participate.class);
        } catch (Exception e) {
            log.error("Error find all participate", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Participate> findById(int id) {
        try {
            return crudRepository.optional(
                    "from Participate where id = :id",
                    Participate.class,
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error find by id participate where id = {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Participate participate) {
        try {
            crudRepository.run(session -> session.merge(participate));
        } catch (Exception e) {
            log.error("Error update participate where id = {}", participate.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        try {
            crudRepository.run(
                    "delete from Participate where id = :id",
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error delete Participate where id = {}", id, e);
        }
    }
}
