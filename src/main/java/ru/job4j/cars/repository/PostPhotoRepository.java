package ru.job4j.cars.repository;

import ru.job4j.cars.model.PostPhoto;

import java.util.Optional;

public interface PostPhotoRepository {
    PostPhoto create(PostPhoto postPhoto);

    Optional<PostPhoto> findById(int id);

    void delete(int id);
}
