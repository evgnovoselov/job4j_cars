package ru.job4j.cars.demo;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.repository.PostRepository;
import ru.job4j.cars.repository.hibernate.CrudRepository;
import ru.job4j.cars.repository.hibernate.HibernatePostRepository;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
public class PostDemo {

    public static void main(String[] args) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        try (SessionFactory sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory()) {
            PostRepository postRepository = new HibernatePostRepository(new CrudRepository(sessionFactory));
            log.info("postRepository.findAllByCreatedBetween");
            Collection<Post> postsByCreated = postRepository.findAllByCreatedBetween(
                    LocalDate.of(2024, 4, 22).atStartOfDay(),
                    LocalDate.of(2024, 4, 24).atStartOfDay()
            );
            displayPosts(postsByCreated);
            log.info("postRepository.findAllWherePhotoIsNotNull");
            Collection<Post> postsWithPhoto = postRepository.findAllWherePhotoIsNotNull();
            displayPosts(postsWithPhoto);
            log.info("postRepository.findAllByCarNameLike");
            Collection<Post> postsByCarNameLike = postRepository.findAllByCarNameLike("ВеСт");
            displayPosts(postsByCarNameLike);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static void displayPosts(Collection<Post> posts) {
        for (Post post : posts) {
            String format = """
                    Post: id=%s
                        description=%s
                        created=%s
                        photos=%s
                        user=%s
                        car=%s
                        engine=%s%n""";
            System.out.printf(
                    format,
                    post.getId(),
                    post.getDescription(),
                    post.getCreated(),
                    post.getPhotos().stream().map(postPhoto -> postPhoto.getPhoto().getPath()).toList(),
                    post.getUser().getLogin(),
                    post.getCar().getName(),
                    post.getCar().getEngine().getName()
            );
        }
    }
}