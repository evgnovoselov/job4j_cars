package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.repository.ParticipateRepository;

@Repository
@AllArgsConstructor
public class HibernateParticipateRepository implements ParticipateRepository {
    private final CrudRepository crudRepository;
}