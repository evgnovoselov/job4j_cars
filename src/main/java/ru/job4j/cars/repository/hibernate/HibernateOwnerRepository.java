package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.repository.OwnerRepository;

@Repository
@AllArgsConstructor
public class HibernateOwnerRepository implements OwnerRepository {
    private final CrudRepository crudRepository;
}
