package ru.job4j.cars.repository;

import ru.job4j.cars.model.Post;

import java.util.Collection;

public interface PostRepository {
    Collection<Post> findAllByCreatedLastDay();

    Collection<Post> findAllWherePhotoIs(boolean have);

    Collection<Post> findAllByCarNameLike(String name);
}
