package ru.job4j.cars.repository.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.config.SessionFactoryConfig;
import ru.job4j.cars.model.Engine;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HibernateEngineRepositoryTest {
    private static SessionFactory sessionFactory;
    private static HibernateEngineRepository engineRepository;

    @BeforeAll
    static void beforeAll() {
        sessionFactory = new SessionFactoryConfig().createSessionFactory();
        CrudRepository crudRepository = new CrudRepository(sessionFactory);
        engineRepository = new HibernateEngineRepository(crudRepository);
    }

    @AfterEach
    void tearDown() {
        engineRepository.findAll().stream().map(Engine::getId).forEach(engineRepository::delete);
    }

    @AfterAll
    static void afterAll() {
        sessionFactory.close();
    }

    @Test
    void whenCreateEngineThenCreatedInDbAndHaveId() {
        Engine engine = new Engine(null, "Test engine");

        Engine actualEngine = engineRepository.create(engine);

        assertThat(actualEngine).usingRecursiveComparison()
                .isEqualTo(new Engine(engine.getId(), "Test engine"));
    }

    @Test
    void whenCreateEngineProcessExceptionThenReturnEngineWithoutId() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateEngineRepository engineRepositoryMock = new HibernateEngineRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any());

        engineRepositoryMock.create(new Engine(null, "Test"));

        verify(crudRepositoryMock, times(1)).run(any());
    }

    @Test
    void whenFindByIdThenReturnOptionalWithEngine() {
        Engine engine = engineRepository.create(new Engine(null, "Test engine"));

        Optional<Engine> actualEngine = engineRepository.findById(engine.getId());

        assertThat(actualEngine).isPresent();
        assertThat(actualEngine.get()).usingRecursiveComparison()
                .isEqualTo(new Engine(engine.getId(), "Test engine"));
    }

    @Test
    void whenFindByIdWrongIdThenReturnOptionalEmpty() {
        Engine engine = engineRepository.create(new Engine(null, "Test engine"));

        Optional<Engine> actualEngine = engineRepository.findById(engine.getId() + 10);

        assertThat(actualEngine).isEmpty();
    }

    @Test
    void whenFindByIdProcessExceptionThenReturnOptionalEmpty() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateEngineRepository engineRepositoryMock = new HibernateEngineRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).optional(any(), any(), any());

        Optional<Engine> actualEngine = engineRepositoryMock.findById(1);

        verify(crudRepositoryMock, times(1)).optional(any(), any(), any());
        assertThat(actualEngine).isEmpty();
    }

    @Test
    void whenFindAllThenReturnCollectionEngines() {
        List<Engine> engines = IntStream.rangeClosed(1, 3)
                .mapToObj(value -> new Engine(null, "Engine-" + value))
                .map(engineRepository::create)
                .toList();

        Collection<Engine> actualEngines = engineRepository.findAll();

        assertThat(actualEngines).hasSize(3);
        List<Engine> expectedEngines = IntStream.rangeClosed(1, 3)
                .mapToObj(value -> new Engine(engines.get(value - 1).getId(), "Engine-" + value))
                .toList();
        assertThat(actualEngines).usingRecursiveComparison().isEqualTo(expectedEngines);
    }

    @Test
    void whenFindAllProcessExceptionThenReturnCollectionEmpty() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateEngineRepository engineRepositoryMock = new HibernateEngineRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).query(any(), any());

        Collection<Engine> actualEngines = engineRepositoryMock.findAll();

        verify(crudRepositoryMock, times(1)).query(any(), any());
        assertThat(actualEngines).isEmpty();
    }

    @Test
    void whenUpdateThenUpdated() {
        Engine engine = engineRepository.create(new Engine(null, "Test engine"));
        Engine updateEngine = new Engine(engine.getId(), "Update engine");

        engineRepository.update(updateEngine);
        Optional<Engine> actualEngine = engineRepository.findById(engine.getId());
        Collection<Engine> actualEngines = engineRepository.findAll();

        assertThat(actualEngines).hasSize(1);
        assertThat(actualEngine).isPresent();
        assertThat(actualEngine.get()).usingRecursiveComparison()
                .isEqualTo(new Engine(engine.getId(), "Update engine"));
    }

    @Test
    void whenUpdateProcessExceptionThenNothing() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateEngineRepository engineRepositoryMock = new HibernateEngineRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any());

        engineRepositoryMock.update(new Engine());

        verify(crudRepositoryMock, times(1)).run(any());
    }

    @Test
    void whenDeleteThenDeleted() {
        Engine engine = engineRepository.create(new Engine(null, "Test engine"));

        engineRepository.delete(engine.getId());
        Optional<Engine> actualEngine = engineRepository.findById(engine.getId());
        Collection<Engine> actualEngines = engineRepository.findAll();

        assertThat(actualEngine).isEmpty();
        assertThat(actualEngines).isEmpty();
    }

    @Test
    void whenDeleteWrongIdThenEngineNotDeleted() {
        Engine engine = engineRepository.create(new Engine(null, "Test engine"));

        engineRepository.delete(engine.getId() + 10);
        Optional<Engine> actualEngine = engineRepository.findById(engine.getId());
        Collection<Engine> actualEngines = engineRepository.findAll();

        assertThat(actualEngines).hasSize(1);
        assertThat(actualEngine).isPresent();
        assertThat(actualEngine.get()).usingRecursiveComparison()
                .isEqualTo(new Engine(engine.getId(), "Test engine"));
    }

    @Test
    void whenDeleteProcessExceptionThenNothing() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateEngineRepository engineRepositoryMock = new HibernateEngineRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any(), any());

        engineRepositoryMock.delete(1);

        verify(crudRepositoryMock, times(1)).run(any(), any());
    }
}