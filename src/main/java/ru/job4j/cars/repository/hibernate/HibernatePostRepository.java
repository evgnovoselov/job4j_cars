package ru.job4j.cars.repository.hibernate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Repository
@AllArgsConstructor
@Slf4j
public class HibernatePostRepository implements PostRepository {
    private final CrudRepository crudRepository;

    @Override
    public Collection<Post> findAllByCreatedBetween(LocalDateTime from, LocalDateTime to) {
        try {
            return crudRepository.tx(session -> {
                String jpql = """
                        select p from Post p
                        left join fetch p.photo
                        left join fetch p.user
                        left join fetch p.car c
                        left join fetch c.engine
                        where p.created between :from and :to""";
                List<Post> posts = session.createQuery(jpql, Post.class)
                        .setParameter("from", from).setParameter("to", to).getResultList();
                jpql = """
                        select p from Post p
                        left join fetch p.car c
                        left join fetch c.historyOwners
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
        } catch (Exception e) {
            log.error("Error find all by created between from {} to {}", from, to);
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<Post> findAllWherePhotoIsNotNull() {
        try {
            return crudRepository.tx(session -> {
                String jpql = """
                        select p from Post p
                        left join fetch p.photo
                        left join fetch p.user
                        left join fetch p.car c
                        left join fetch c.engine
                        where p.photo is not null""";
                List<Post> posts = session.createQuery(jpql, Post.class).getResultList();
                jpql = """
                        select p from Post p
                        left join fetch p.car c
                        left join fetch c.historyOwners
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
        } catch (Exception e) {
            log.error("Error find all by created last day");
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<Post> findAllByCarNameLike(String name) {
        try {

            return crudRepository.tx(session -> {
                String jpql = """
                        select p from Post p
                        left join fetch p.photo
                        left join fetch p.user
                        left join fetch p.car c
                        left join fetch c.engine
                        where lower(c.name) like lower(:name)""";
                List<Post> posts = session.createQuery(jpql, Post.class)
                        .setParameter("name", "%" + name + "%").getResultList();
                jpql = """
                        select p from Post p
                        left join fetch p.car c
                        left join fetch c.historyOwners
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
        } catch (Exception e) {
            log.error("Error find all by car name");
        }
        return Collections.emptyList();
    }
}
