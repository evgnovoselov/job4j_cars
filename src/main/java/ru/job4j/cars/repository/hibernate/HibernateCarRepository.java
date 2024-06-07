package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.model.Car;
import ru.job4j.cars.repository.CarRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
@Slf4j
public class HibernateCarRepository implements CarRepository {
    private final CrudRepository crudRepository;

    @Override
    public Car create(Car car) {
        try {
            crudRepository.run(session -> session.persist(car));
        } catch (Exception e) {
            log.error("Error create car", e);
        }
        return car;
    }

    @Override
    public Collection<Car> findAll() {
        try {
            return crudRepository.query("from Car", Car.class);
        } catch (Exception e) {
            log.error("Error find all car", e);
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Car> findById(int id) {
        try {
            return crudRepository.optional(
                    "from Car where id = :id",
                    Car.class,
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error find by id car, id = {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Car car) {
        try {
            crudRepository.run(session -> session.merge(car));
        } catch (Exception e) {
            log.error("Error update car, id = {}", car.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        try {
            crudRepository.run(
                    "delete from Car where id = :id",
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error delete Car where id = {}", id, e);
        }
    }
}
