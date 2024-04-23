package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
@Slf4j
public class HibernatePostRepository implements PostRepository {
    private final CrudRepository crudRepository;

    private Collection<Post> findAllPost(String whereJpql, Map<String, Object> parameters) {
        return crudRepository.tx(session -> {
            String jpql;
            List<Post> posts;
            jpql = """
                    select p from Post p
                    left join fetch p.user
                    left join fetch p.car c
                    left join fetch c.engine
                    left join fetch c.historyOwners ho
                    left join fetch ho.owner o
                    left join fetch o.user
                    where %s""".formatted(whereJpql);
            Query<Post> query = session.createQuery(jpql, Post.class);
            for (Map.Entry<String, Object> parameterEntry : parameters.entrySet()) {
                query.setParameter(parameterEntry.getKey(), parameterEntry.getValue());
            }
            posts = query.getResultList();
            jpql = """
                    select p from Post p
                    left join fetch p.photos ph
                    left join fetch ph.photo
                    where p in :posts""";
            posts = session.createQuery(jpql, Post.class).setParameter("posts", posts)
                    .getResultList();
            jpql = """
                    select p from Post p
                    left join fetch p.priceHistories
                    where p in :posts""";
            posts = session.createQuery(jpql, Post.class).setParameter("posts", posts)
                    .getResultList();
            jpql = """
                    select p from Post p
                    left join fetch p.participates
                    where p in :posts
                    order by p.created desc""";
            posts = session.createQuery(jpql, Post.class).setParameter("posts", posts)
                    .getResultList();
            return posts;
        });
    }

    @Override
    public Collection<Post> findAllByCreatedBetween(LocalDateTime from, LocalDateTime to) {
        try {
            return findAllPost("p.created between :from and :to", Map.of("from", from, "to", to));
        } catch (Exception e) {
            log.error("Error find all by created between from {} to {}", from, to);
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<Post> findAllWherePhotoIsNotNull() {
        try {
            return findAllPost("p.photo is not null", Map.of());
        } catch (Exception e) {
            log.error("Error find all by created last day");
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<Post> findAllByCarNameLike(String name) {
        try {
            return findAllPost("lower(c.name) like lower(:name)", Map.of("name", "%" + name + "%"));
        } catch (Exception e) {
            log.error("Error find all by car name");
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }
}
