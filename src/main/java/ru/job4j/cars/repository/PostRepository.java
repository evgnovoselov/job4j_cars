package ru.job4j.cars.repository;

import ru.job4j.cars.model.Post;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface PostRepository {
    Post create(Post post);

    void update(Post post);

    Optional<Post> findById(int id);

    Collection<Post> findAllOrderByCreated();

    Collection<Post> findAllByCreatedBetween(LocalDateTime from, LocalDateTime to);

    Collection<Post> findAllWherePhotoIsNotNull();

    Collection<Post> findAllByCarNameLike(String name);

    void delete(int id);
}
