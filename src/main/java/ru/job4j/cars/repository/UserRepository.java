package ru.job4j.cars.repository;

import lombok.AllArgsConstructor;
import ru.job4j.cars.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class UserRepository {
    private CrudRepository crudRepository;

    /**
     * Сохранить в базе пользователя.
     *
     * @param user пользователь.
     * @return пользователь с id.
     */
    public User create(User user) {
        crudRepository.run(session -> session.persist(user));
        return user;
    }

    /**
     * Обновить пользователя.
     *
     * @param user пользователь.
     */
    public void update(User user) {
        crudRepository.run(session -> session.merge(user));
    }

    /**
     * Удалить пользователя по id.
     *
     * @param userId id пользователя.
     */
    public void delete(int userId) {
        crudRepository.run(
                "delete from User where id = :id",
                Map.of("id", userId)
        );
    }

    /**
     * Список пользователей отсортированных по id.
     *
     * @return список пользователей.
     */
    public List<User> findAllOrderById() {
        return crudRepository.query("from User order by id asc", User.class);
    }

    /**
     * Найти пользователя по id.
     *
     * @param userId id пользователя.
     * @return пользователь.
     */
    public Optional<User> findById(int userId) {
        return crudRepository.optional(
                "from User where id = :id",
                User.class,
                Map.of("id", userId)
        );
    }

    /**
     * Список пользователей по login LIKE %key%.
     *
     * @param key key поиска.
     * @return список пользователей.
     */
    public List<User> findByLikeLogin(String key) {
        return crudRepository.query(
                "from User where login like :key",
                User.class,
                Map.of("key", "%" + key + "%")
        );
    }

    /**
     * Найти пользователя по login.
     *
     * @param login login
     * @return пользователь.
     */
    public Optional<User> findByLogin(String login) {
        return crudRepository.optional(
                "from User where login = :login",
                User.class,
                Map.of("login", login)
        );
    }
}
