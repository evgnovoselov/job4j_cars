package ru.job4j.cars.repository.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.config.SessionFactoryConfig;

class HibernateParticipateRepositoryTest {
    private static HibernateParticipateRepository participateRepository;
    private static SessionFactory sessionFactory;

    @BeforeAll
    static void beforeAll() {
        sessionFactory = new SessionFactoryConfig().createSessionFactory();
        CrudRepository crudRepository = new CrudRepository(sessionFactory);
        participateRepository = new HibernateParticipateRepository(crudRepository);
    }

    @AfterAll
    static void afterAll() {
        sessionFactory.close();
    }

    @Test
    void whenCreateParticipateThenCreatedInDbAndHaveId() {

    }

    @Test
    void whenCreateParticipateProcessExceptionThenReturnParticipateWithoutId() {

    }

    @Test
    void whenFindByIdThenReturnOptionalWithParticipate() {

    }

    @Test
    void whenFindByIdWrongIdThenReturnOptionalEmpty() {

    }

    @Test
    void whenFindByIdProcessExceptionThenReturnOptionalEmpty() {

    }

    @Test
    void whenFindAllThenReturnCollectionParticipates() {

    }

    @Test
    void whenFindAllAndEmptyDbThenReturnCollectionEmpty() {

    }

    @Test
    void whenFindAllProcessExceptionThenReturnCollectionEmpty() {

    }

    @Test
    void whenUpdateThenUpdated() {

    }

    @Test
    void whenUpdateProcessExceptionThenNothing() {

    }

    @Test
    void whenDeleteThenDeleted() {

    }

    @Test
    void whenDeleteWrongIdThenParticipateNotDeleted() {

    }

    @Test
    void whenDeleteProcessExceptionThenNothing() {

    }
}