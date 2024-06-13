package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.model.Owner;
import ru.job4j.cars.repository.OwnerRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
@Slf4j
public class HibernateOwnerRepository implements OwnerRepository {
    private final CrudRepository crudRepository;

    @Override
    public Owner create(Owner owner) {
        try {
            crudRepository.run(session -> session.persist(owner));
        } catch (Exception e) {
            log.error("Error create owner", e);
        }
        return owner;
    }

    @Override
    public Collection<Owner> findAll() {
        try {
            return crudRepository.query("from Owner", Owner.class);
        } catch (Exception e) {
            log.error("Error find all owner", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Owner> findById(int id) {
        try {
            return crudRepository.optional(
                    "from Owner where id = :id",
                    Owner.class,
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error find by id owner where id = {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Owner owner) {
        try {
            crudRepository.run(session -> session.merge(owner));
        } catch (Exception e) {
            log.error("Error update owner where id = {}", owner.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        try {
            crudRepository.run(
                    "delete from Owner where id = :id",
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error delete owner where id = {}", id, e);
        }
    }
}
