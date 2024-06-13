package ru.job4j.cars.repository.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.config.SessionFactoryConfig;
import ru.job4j.cars.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HibernateUserRepositoryTest {
    private static SessionFactory sessionFactory;
    private static HibernateUserRepository userRepository;

    @BeforeAll
    static void beforeAll() {
        sessionFactory = new SessionFactoryConfig().createSessionFactory();
        userRepository = new HibernateUserRepository(new CrudRepository(sessionFactory));
        clearUsersInDb();
    }

    private static void clearUsersInDb() {
        userRepository.findAllOrderById().stream().map(User::getId).forEach(userRepository::delete);
    }

    @AfterEach
    void tearDown() {
        clearUsersInDb();
    }

    @AfterAll
    static void afterAll() {
        sessionFactory.close();
    }

    @Test
    void whenSaveUserThenReturnUserAndSetIdInUser() {
        User user = new User(0, "ivan", "password");

        User actualUser = userRepository.create(user);

        User expectedUser = new User(user.getId(), "ivan", "password");
        assertThat(user.getId()).isNotZero();
        assertThat(actualUser.getId()).isNotZero();
        assertThat(actualUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    void whenUpdateUserThenUpdated() {
        User saveUser = new User(0, "ivan", "password");
        userRepository.create(saveUser);
        User updateUser = new User(saveUser.getId(), "new_ivan", "new_password");

        userRepository.update(updateUser);
        User actualUser = userRepository.findById(updateUser.getId()).orElseThrow();
        List<User> actualUsers = userRepository.findAllOrderById();

        assertThat(actualUsers).hasSize(1);
        User expectedUser = new User(updateUser.getId(), "new_ivan", "new_password");
        assertThat(actualUser.getId()).isNotZero();
        assertThat(actualUser).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    void whenDeleteByIdThenDeleted() {
        User user = new User(0, "ivan", "password");
        userRepository.create(user);

        userRepository.delete(user.getId());
        Optional<User> actualOptionalUser = userRepository.findById(user.getId());
        List<User> actualUsers = userRepository.findAllOrderById();

        assertThat(actualOptionalUser).isEmpty();
        assertThat(actualUsers).isEmpty();
    }

    @Test
    void whenDeleteUserByWrongIdThenUserNotDeleted() {
        User user = new User(0, "ivan", "password");
        userRepository.create(user);

        userRepository.delete(user.getId() + 1);
        User actualUser = userRepository.findById(user.getId()).orElseThrow();
        List<User> actualUsers = userRepository.findAllOrderById();

        assertThat(actualUser).usingRecursiveComparison()
                .isEqualTo(new User(user.getId(), "ivan", "password"));
        assertThat(actualUsers).hasSize(1);
    }

    @Test
    void whenFindAllOrderByIdThenReturnSortListOrderById() {
        List<User> savedUsers = IntStream.range(1, 5).boxed()
                .map(num -> new User(0, "ivan_" + num, "password_" + num))
                .map(userRepository::create).toList();

        List<User> actualUsers = userRepository.findAllOrderById();

        assertThat(actualUsers).hasSize(4);
        List<User> expectedUsers = IntStream.range(1, 5).boxed()
                .map(num -> new User(
                                savedUsers.get(num - 1).getId(),
                                "ivan_" + num,
                                "password_" + num
                        )
                ).toList();
        assertThat(actualUsers).usingRecursiveComparison().isEqualTo(expectedUsers);
    }

    @Test
    void whenClearDbAndFindAllOrderByIdThenEmptyList() {

        List<User> actualUsers = userRepository.findAllOrderById();

        assertThat(actualUsers).isEmpty();
    }

    @Test
    void whenFindByIdThenReturnOptionalWithUser() {
        User savedUser = userRepository.create(new User(0, "ivan", "password"));

        User actualUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(actualUser).usingRecursiveComparison().isEqualTo(
                new User(savedUser.getId(), "ivan", "password")
        );
    }

    @Test
    void whenFindByIdWrongIdReturnOptionalEmpty() {
        User savedUser = userRepository.create(new User(0, "ivan", "password"));

        Optional<User> actualOptionalUser = userRepository.findById(savedUser.getId() + 1);

        assertThat(actualOptionalUser).isEmpty();
    }

    @Test
    void whenEmptyDbAndFindByIdThenOptionalEmpty() {

        Optional<User> actualOptionalUser = userRepository.findById(1);

        assertThat(actualOptionalUser).isEmpty();
    }

    @Test
    void whenFindByLikeLoginThenReturnUsers() {
        List<User> users = Stream.of(
                new User(0, "ivan", "pass"),
                new User(0, "alex", "pass"),
                new User(0, "PiVanO", "pass"),
                new User(0, "anna", "pass"),
                new User(0, "OivAnA", "pass"),
                new User(0, "julia", "pass"),
                new User(0, "DiVaNO", "pass")
        ).map(userRepository::create).toList();

        List<User> actualUsers = userRepository.findByLikeLogin("van");

        List<User> expectedUsers = List.of(
                new User(users.get(0).getId(), "ivan", "pass"),
                new User(users.get(2).getId(), "PiVanO", "pass"),
                new User(users.get(4).getId(), "OivAnA", "pass"),
                new User(users.get(6).getId(), "DiVaNO", "pass")
        );
        assertThat(actualUsers).hasSize(expectedUsers.size());
        assertThat(actualUsers).usingRecursiveComparison().isEqualTo(expectedUsers);
    }

    @Test
    void whenFindByLikeLoginAndDbNotHaveLikeLoginThenReturnEmptyList() {
        List<User> users = Stream.of(
                new User(0, "alex", "pass"),
                new User(0, "anna", "pass"),
                new User(0, "julia", "pass")
        ).map(userRepository::create).toList();

        List<User> actualUsers = userRepository.findByLikeLogin("van");
        List<User> actualUsersInDb = userRepository.findAllOrderById();

        assertThat(actualUsers).isEmpty();
        assertThat(actualUsers).usingRecursiveComparison().isEqualTo(List.of());
        List<User> expectedUsersInDb = List.of(
                new User(users.get(0).getId(), "alex", "pass"),
                new User(users.get(1).getId(), "anna", "pass"),
                new User(users.get(2).getId(), "julia", "pass")
        );
        assertThat(actualUsersInDb).hasSize(expectedUsersInDb.size());
        assertThat(actualUsersInDb).usingRecursiveComparison().isEqualTo(expectedUsersInDb);
    }

    @Test
    void whenDbClearAndFindByLikeLoginThenReturnEmptyList() {

        List<User> actualUsers = userRepository.findByLikeLogin("van");

        List<User> expectedUsers = List.of();
        assertThat(actualUsers).isEmpty();
        assertThat(actualUsers).usingRecursiveComparison().isEqualTo(expectedUsers);
    }

    @Test
    void whenFindByLoginUserThenReturnUser() {
        User user = new User(0, "ivan", "password");
        user = userRepository.create(user);

        Optional<User> actualOptionalUser = userRepository.findByLogin("ivan");
        List<User> actualUsers = userRepository.findAllOrderById();

        assertThat(actualUsers).hasSize(1);
        assertThat(actualOptionalUser).isPresent();
        User expectedUser = new User(user.getId(), "ivan", "password");
        assertThat(actualOptionalUser.get()).usingRecursiveComparison().isEqualTo(expectedUser);
    }

    @Test
    void whenFindByWrongLoginUserThenReturnOptionalEmpty() {
        User user = new User(0, "ivan", "password");
        userRepository.create(user);

        Optional<User> actualOptionalUser = userRepository.findByLogin("julia");
        List<User> actualUsers = userRepository.findAllOrderById();

        assertThat(actualUsers).hasSize(1);
        assertThat(actualOptionalUser).isEmpty();
    }

    @Test
    void whenCleanDbAndFindByLoginThenReturnOptionalEmpty() {

        Optional<User> actualOptionalUser = userRepository.findByLogin("julia");
        List<User> actualUsers = userRepository.findAllOrderById();

        assertThat(actualUsers).isEmpty();
        assertThat(actualOptionalUser).isEmpty();
    }
}
