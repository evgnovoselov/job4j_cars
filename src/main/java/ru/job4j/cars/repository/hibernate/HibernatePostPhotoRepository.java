package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.model.PostPhoto;
import ru.job4j.cars.repository.PostPhotoRepository;

import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
@Slf4j
public class HibernatePostPhotoRepository implements PostPhotoRepository {
    private final CrudRepository crudRepository;

    @Override
    public PostPhoto create(PostPhoto postPhoto) {
        try {
            crudRepository.run(session -> session.persist(postPhoto));
        } catch (Exception e) {
            log.error("Error create PostPhoto");
            log.error(e.getMessage());
        }
        return postPhoto;
    }

    @Override
    public Optional<PostPhoto> findById(int id) {
        try {
            return crudRepository.optional(
                    "select pp from PostPhoto pp where id = :id",
                    PostPhoto.class,
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error find PostPhoto by id where id = {}", id);
            log.error(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void delete(int id) {
        try {
            crudRepository.run(
                    "delete from PostPhoto where id = :id",
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error delete PostPhoto where id = {}", id);
            log.error(e.getMessage());
        }
    }
}