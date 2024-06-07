package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.model.File;
import ru.job4j.cars.repository.FileRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
@Slf4j
public class HibernateFileRepository implements FileRepository {
    private final CrudRepository crudRepository;

    @Override
    public File create(File file) {
        try {
            crudRepository.run(session -> session.persist(file));
        } catch (Exception e) {
            log.error("Error create File", e);
        }
        return file;
    }

    @Override
    public Optional<File> findById(int id) {
        try {
            return crudRepository.optional(
                    "select f from File f where id = :id",
                    File.class,
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error find File by id = {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Collection<File> findAll() {
        try {
            return crudRepository.query(
                    "select f from File f",
                    File.class
            );
        } catch (Exception e) {
            log.error("Error find all File", e);
        }
        return Collections.emptyList();
    }

    @Override
    public void delete(int id) {
        try {
            crudRepository.run(
                    "delete from File where id = :id",
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error delete File where id = {}", id, e);
        }
    }
}
