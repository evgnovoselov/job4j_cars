package ru.job4j.cars.repository.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import ru.job4j.cars.config.SessionFactoryConfig;
import ru.job4j.cars.model.User;
import ru.job4j.cars.repository.UserRepository;

import java.util.List;
import java.util.stream.Stream;

class HibernatePostRepositoryTest {
    private static SessionFactory sessionFactory;
    private static HibernatePostRepository postRepository;
    private static UserRepository userRepository;
    private List<User> testUsers;

    @BeforeAll
    static void beforeAll() {
        sessionFactory = new SessionFactoryConfig().createSessionFactory();
        CrudRepository crudRepository = new CrudRepository(sessionFactory);
        postRepository = new HibernatePostRepository(crudRepository);

        userRepository = new HibernateUserRepository(crudRepository);
        userRepository.findAllOrderById().stream().map(User::getId).forEach(userRepository::delete);
    }

    @BeforeEach
    void setUp() {
        testUsers = Stream.of(
                new User(0, "ivanov", "password"),
                new User(0, "petrov", "password"),
                new User(0, "durov", "password")
        ).map(userRepository::create).toList();
    }

    @AfterEach
    void tearDown() {
        userRepository.findAllOrderById().stream().map(User::getId).forEach(userRepository::delete);
    }

    @AfterAll
    static void afterAll() {
        sessionFactory.close();
    }

    @Test
    void whenSavePostThenReturnPostAndSetIdInPost() {

    }

    @Test
    void whenUpdatePostThenUpdated() {

    }

    @Test
    void whenDeletePostThenDeleted() {

    }

    @Test
    void whenDeletePostWrongIdThenNotDeleted() {

    }
}