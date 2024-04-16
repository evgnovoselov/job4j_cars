package ru.job4j.cars.repository;

import ru.job4j.cars.model.Post;

import java.time.LocalDateTime;
import java.util.Collection;

public interface PostRepository {

    Collection<Post> findAllByCreatedBetween(LocalDateTime from, LocalDateTime to);

    Collection<Post> findAllWherePhotoIsNotNull();

    Collection<Post> findAllByCarNameLike(String name);
}
