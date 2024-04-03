package ru.job4j.cars.demo;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.cars.model.User;
import ru.job4j.cars.repository.hibernate.CrudRepository;
import ru.job4j.cars.repository.hibernate.HibernateUserRepository;

public class UserUsage {
    public static void main(String[] args) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        try (SessionFactory sf = new MetadataSources(registry).buildMetadata().buildSessionFactory()) {
            HibernateUserRepository hibernateUserRepository = new HibernateUserRepository(new CrudRepository(sf));
            hibernateUserRepository.findByLogin("admin").ifPresent(user -> hibernateUserRepository.delete(user.getId()));
            User user = new User();
            user.setLogin("admin");
            user.setPassword("admin");
            hibernateUserRepository.create(user);
            hibernateUserRepository.findAllOrderById().forEach(System.out::println);
            hibernateUserRepository.findByLikeLogin("d").forEach(System.out::println);
            hibernateUserRepository.findById(user.getId()).ifPresent(System.out::println);
            hibernateUserRepository.findByLogin("admin").ifPresent(System.out::println);
            user.setPassword("password");
            hibernateUserRepository.update(user);
            hibernateUserRepository.findById(user.getId()).ifPresent(System.out::println);
            hibernateUserRepository.delete(user.getId());
            hibernateUserRepository.findAllOrderById().forEach(System.out::println);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
