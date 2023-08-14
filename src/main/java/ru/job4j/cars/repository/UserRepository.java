package ru.job4j.cars.repository;

import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.job4j.cars.model.User;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public class UserRepository {
    private final SessionFactory sf;

    /**
     * Сохранить в базе пользователя.
     *
     * @param user пользователь.
     * @return пользователь с id.
     */
    public User create(User user) {
        inTransaction(session -> session.save(user));
        return user;
    }

    /**
     * Обновить пользователя.
     *
     * @param user пользователь.
     */
    public void update(User user) {
        inTransaction(session -> session.createQuery("update User "
                        + "set login = :login, password = :password "
                        + "where id = :id")
                .setParameter("login", user.getLogin())
                .setParameter("password", user.getPassword())
                .setParameter("id", user.getId())
                .executeUpdate());
    }

    /**
     * Удалить пользователя по id.
     *
     * @param userId id пользователя.
     */
    public void delete(int userId) {
        inTransaction(session -> session.createQuery("delete User where id = :id")
                .setParameter("id", userId)
                .executeUpdate());
    }

    /**
     * Список пользователей отсортированных по id.
     *
     * @return список пользователей.
     */
    public List<User> findAllOrderById() {
        return fromTransaction(session -> session.createQuery("from User order by id", User.class).list());
    }

    /**
     * Найти пользователя по id.
     *
     * @param userId id пользователя.
     * @return пользователь.
     */
    public Optional<User> findById(int userId) {
        return fromTransaction(session -> session.createQuery("from User where id = :id", User.class)
                .setParameter("id", userId)
                .uniqueResultOptional());
    }

    /**
     * Список пользователей по login LIKE %key%.
     *
     * @param key key поиска.
     * @return список пользователей.
     */
    public List<User> findByLikeLogin(String key) {
        return fromTransaction(session -> session.createQuery("from User where login like :key", User.class)
                .setParameter("key", "%" + key + "%")
                .list());
    }

    /**
     * Найти пользователя по login.
     *
     * @param login login
     * @return пользователь.
     */
    public Optional<User> findByLogin(String login) {
        return fromTransaction(session -> session.createQuery("from User where login = :login", User.class)
                .setParameter("login", login)
                .uniqueResultOptional());
    }

    private void inTransaction(Consumer<Session> action) {
        try (Session session = sf.openSession()) {
            try {
                session.beginTransaction();
                action.accept(session);
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
            }
        }
    }

    private <R> R fromTransaction(Function<Session, R> action) {
        R result = null;
        try (Session session = sf.openSession()) {
            try {
                session.beginTransaction();
                result = action.apply(session);
                session.getTransaction().commit();
                return result;
            } catch (Exception e) {
                session.getTransaction().rollback();
            }
        }
        return result;
    }
}
