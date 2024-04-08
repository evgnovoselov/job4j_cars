package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.repository.PostRepository;

import java.util.Collection;
import java.util.Collections;

@Repository
@AllArgsConstructor
public class HibernatePostRepository implements PostRepository {
    private final CrudRepository crudRepository;

    @Override
    public Collection<Post> findAllByCreatedLastDay() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Post> findAllWherePhotoIs(boolean have) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Post> findAllByCarNameLike(String name) {
        return Collections.emptyList();
    }
}
