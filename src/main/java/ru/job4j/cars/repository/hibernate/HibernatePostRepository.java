package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Repository
@AllArgsConstructor
@Slf4j
public class HibernatePostRepository implements PostRepository {
    private final CrudRepository crudRepository;

    private Collection<Post> findAllPost(String whereJpql, Map<String, Object> parameters) {
        String jpql = """
                select p from Post p
                left join fetch p.user
                left join fetch p.car c
                left join fetch c.engine
                left join fetch p.photos phs
                left join fetch phs.photo ph
                where %s
                order by p.created desc""".formatted(whereJpql);
        return crudRepository.query(jpql, Post.class, parameters);
    }

    @Override
    public Collection<Post> findAllByCreatedBetween(LocalDateTime from, LocalDateTime to) {
        try {
            return findAllPost(
                    "p.created between :from and :to",
                    Map.of(
                            "from", from,
                            "to", to
                    )
            );
        } catch (Exception e) {
            log.error("Error find all by created between from {} to {}", from, to);
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<Post> findAllWherePhotoIsNotNull() {
        try {
            return findAllPost("size ( phs ) > 0", Map.of());
        } catch (Exception e) {
            log.error("Error find all where photo is not null");
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<Post> findAllByCarNameLike(String name) {
        try {
            return findAllPost(
                    "lower(c.name) like lower(:name)",
                    Map.of("name", "%" + name + "%")
            );
        } catch (Exception e) {
            log.error("Error find all by car name");
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }
}
