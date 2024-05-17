package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.repository.PostPhotoRepository;

@Repository
@AllArgsConstructor
public class HibernatePostPhotoRepository implements PostPhotoRepository {
    private final CrudRepository crudRepository;
}
