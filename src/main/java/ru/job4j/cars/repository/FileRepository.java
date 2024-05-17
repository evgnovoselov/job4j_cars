package ru.job4j.cars.repository;

import ru.job4j.cars.model.File;

import java.util.Optional;

public interface FileRepository {
    File create(File file);

    Optional<File> findById(int id);

    void delete(int id);
}
