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
import java.util.Optional;

@Repository
@AllArgsConstructor
@Slf4j
public class HibernatePostRepository implements PostRepository {
    private final CrudRepository crudRepository;

    @Override
    public Post create(Post post) {
        try {
            crudRepository.run(session -> session.persist(post));
        } catch (Exception e) {
            log.error("Error create post", e);
        }
        return post;
    }

    @Override
    public void update(Post post) {
        try {
            crudRepository.run(session -> session.merge(post));
        } catch (Exception e) {
            log.error("Error update post {}", post.getId(), e);
        }
    }

    @Override
    public Optional<Post> findById(int id) {
        try {
            String jpql = """
                    select p from Post p
                    left join fetch p.user
                    where p.id = :id""";
            return crudRepository.optional(
                    jpql,
                    Post.class,
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error find by id post, id = {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Collection<Post> findAllOrderByCreated() {
        try {
            String jpql = """
                    select p from Post p
                    left join fetch p.user
                    left join fetch p.car c
                    left join fetch c.engine
                    left join fetch p.photos phs
                    left join fetch phs.photo ph
                    order by p.created desc""";
            return crudRepository.query(jpql, Post.class);
        } catch (Exception e) {
            log.error("Error find all order by created", e);
        }
        return Collections.emptyList();
    }

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
            log.error("Error find all by created between from {} to {}", from, to, e);
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<Post> findAllWherePhotoIsNotNull() {
        try {
            return findAllPost("size ( phs ) > 0", Map.of());
        } catch (Exception e) {
            log.error("Error find all where photo is not null", e);
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
            log.error("Error find all by car name", e);
        }
        return Collections.emptyList();
    }

    @Override
    public void delete(int id) {
        try {
            crudRepository.run(
                    "delete from Post where id = :id",
                    Map.of("id", id)
            );
        } catch (Exception e) {
            log.error("Error delete Post where id = {}", id, e);
        }
    }
}
