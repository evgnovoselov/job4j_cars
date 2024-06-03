package ru.job4j.cars.repository;

import ru.job4j.cars.model.File;

import java.util.Collection;
import java.util.Optional;

public interface FileRepository {
    File create(File file);

    Optional<File> findById(int id);

    Collection<File> findAll();

    void delete(int id);
}
