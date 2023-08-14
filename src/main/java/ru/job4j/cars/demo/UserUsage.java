package ru.job4j.cars.demo;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.cars.model.User;
import ru.job4j.cars.repository.UserRepository;

public class UserUsage {
    public static void main(String[] args) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        try (SessionFactory sf = new MetadataSources(registry).buildMetadata().buildSessionFactory()) {
            UserRepository userRepository = new UserRepository(sf);
            userRepository.findByLogin("admin").ifPresent(user -> userRepository.delete(user.getId()));
            User user = new User();
            user.setLogin("admin");
            user.setPassword("admin");
            userRepository.create(user);
            userRepository.findAllOrderById().forEach(System.out::println);
            userRepository.findByLikeLogin("d").forEach(System.out::println);
            userRepository.findById(user.getId()).ifPresent(System.out::println);
            userRepository.findByLogin("admin").ifPresent(System.out::println);
            user.setPassword("password");
            userRepository.update(user);
            userRepository.findById(user.getId()).ifPresent(System.out::println);
            userRepository.delete(user.getId());
            userRepository.findAllOrderById().forEach(System.out::println);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
