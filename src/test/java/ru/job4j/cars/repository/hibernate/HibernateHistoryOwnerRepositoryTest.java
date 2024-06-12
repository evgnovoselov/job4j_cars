package ru.job4j.cars.repository.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.config.SessionFactoryConfig;
import ru.job4j.cars.model.*;
import ru.job4j.cars.repository.CarRepository;
import ru.job4j.cars.repository.EngineRepository;
import ru.job4j.cars.repository.OwnerRepository;
import ru.job4j.cars.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HibernateHistoryOwnerRepositoryTest {
    private static SessionFactory sessionFactory;
    private static HibernateHistoryOwnerRepository historyOwnerRepository;
    private static CarRepository carRepository;
    private static List<Car> testCars;
    private static EngineRepository engineRepository;
    private static OwnerRepository ownerRepository;
    private static List<Owner> testOwners;
    private static UserRepository userRepository;

    @BeforeAll
    static void beforeAll() {
        sessionFactory = new SessionFactoryConfig().createSessionFactory();
        CrudRepository crudRepository = new CrudRepository(sessionFactory);
        historyOwnerRepository = new HibernateHistoryOwnerRepository(crudRepository);
        carRepository = new HibernateCarRepository(crudRepository);
        engineRepository = new HibernateEngineRepository(crudRepository);
        ownerRepository = new HibernateOwnerRepository(crudRepository);
        userRepository = new HibernateUserRepository(crudRepository);
        clearDataInDb();
        initDataInDb();
    }

    private static void initDataInDb() {
        List<Engine> testEngines = IntStream.rangeClosed(0, 3)
                .mapToObj(value -> new Engine(null, "Engine-" + value))
                .map(engineRepository::create)
                .toList();
        testCars = IntStream.rangeClosed(0, 3)
                .mapToObj(
                        value -> Car.builder()
                                .name("Car-" + value)
                                .engine(testEngines.get(value))
                                .build()
                )
                .map(carRepository::create)
                .toList();
        List<User> testUsers = IntStream.rangeClosed(0, 3)
                .mapToObj(
                        value -> User.builder()
                                .login("user-" + value)
                                .password("password-" + value)
                                .build()
                )
                .map(userRepository::create)
                .toList();
        testOwners = IntStream.rangeClosed(0, 3)
                .mapToObj(
                        value -> Owner.builder()
                                .name("Name-" + value)
                                .user(testUsers.get(value))
                                .build()
                )
                .map(ownerRepository::create)
                .toList();
    }

    private static void clearDataInDb() {
        clearHistoryOwnerDataInDb();
        carRepository.findAll().stream().map(Car::getId).forEach(carRepository::delete);
        engineRepository.findAll().stream().map(Engine::getId).forEach(engineRepository::delete);
        ownerRepository.findAll().stream().map(Owner::getId).forEach(ownerRepository::delete);
        userRepository.findAllOrderById().stream().map(User::getId).forEach(userRepository::delete);
    }

    private static void clearHistoryOwnerDataInDb() {
        historyOwnerRepository.findAll().stream().map(HistoryOwner::getId).forEach(historyOwnerRepository::delete);
    }

    @AfterEach
    void tearDown() {
        clearHistoryOwnerDataInDb();
    }

    @AfterAll
    static void afterAll() {
        clearDataInDb();
        sessionFactory.close();
    }

    private HistoryOwner copyOf(HistoryOwner historyOwner) {
        return HistoryOwner.builder()
                .id(historyOwner.getId())
                .car(copyOf(historyOwner.getCar()))
                .owner(copyOf(historyOwner.getOwner()))
                .startAt(historyOwner.getStartAt())
                .endAt(historyOwner.getEndAt())
                .build();
    }

    private Owner copyOf(Owner owner) {
        return Owner.builder()
                .id(owner.getId())
                .name(owner.getName())
                .user(owner.getUser())
                .build();
    }

    private Car copyOf(Car car) {
        return Car.builder()
                .id(car.getId())
                .name(car.getName())
                .engine(car.getEngine())
                .build();
    }

    @Test
    void whenCreateHistoryOwnerThenCreatedInDbAndHaveId() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        HistoryOwner historyOwner = HistoryOwner.builder()
                .car(testCars.get(0))
                .owner(testOwners.get(0))
                .startAt(now.minusDays(2))
                .endAt(now)
                .build();

        HistoryOwner actualHistoryOwner = historyOwnerRepository.create(historyOwner);

        assertThat(actualHistoryOwner.getId()).isNotNull();
        HistoryOwner expectedHistoryOwner = HistoryOwner.builder()
                .id(actualHistoryOwner.getId())
                .car(testCars.get(0))
                .owner(testOwners.get(0))
                .startAt(now.minusDays(2))
                .endAt(now)
                .build();
        assertThat(actualHistoryOwner).usingRecursiveComparison()
                .isEqualTo(expectedHistoryOwner);
    }

    @Test
    void whenCreateHistoryOwnerProcessExceptionThenReturnHistoryOwnerWithoutId() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateHistoryOwnerRepository historyOwnerRepositoryMock = new HibernateHistoryOwnerRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any());

        HistoryOwner actualHistoryOwner = historyOwnerRepositoryMock.create(new HistoryOwner());

        verify(crudRepositoryMock, times(1)).run(any());
        assertThat(actualHistoryOwner.getId()).isNull();
    }

    @Test
    void whenFindByIdThenReturnOptionalWithHistoryOwner() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        HistoryOwner historyOwner = historyOwnerRepository.create(HistoryOwner.builder()
                .car(testCars.get(0))
                .owner(testOwners.get(0))
                .startAt(now.minusDays(2))
                .endAt(now)
                .build());

        Optional<HistoryOwner> actualOptionalHistoryOwner = historyOwnerRepository.findById(historyOwner.getId());

        assertThat(actualOptionalHistoryOwner).isPresent();
        HistoryOwner expectedHistoryOwner = HistoryOwner.builder()
                .id(historyOwner.getId())
                .car(testCars.get(0))
                .owner(testOwners.get(0))
                .startAt(now.minusDays(2))
                .endAt(now)
                .build();
        assertThat(copyOf(actualOptionalHistoryOwner.get())).usingRecursiveComparison()
                .isEqualTo(expectedHistoryOwner);
    }

    @Test
    void whenFindByIdWrongIdThenReturnOptionalEmpty() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        HistoryOwner historyOwner = historyOwnerRepository.create(HistoryOwner.builder()
                .car(testCars.get(0))
                .owner(testOwners.get(0))
                .startAt(now.minusDays(2))
                .endAt(now)
                .build());

        Optional<HistoryOwner> actualOptionalHistoryOwner = historyOwnerRepository.findById(historyOwner.getId() + 1);
        Collection<HistoryOwner> actualHistoryOwners = historyOwnerRepository.findAll();

        assertThat(actualOptionalHistoryOwner).isEmpty();
        assertThat(actualHistoryOwners).hasSize(1);
    }

    @Test
    void whenFindByIdProcessExceptionThenReturnOptionalEmpty() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateHistoryOwnerRepository historyOwnerRepositoryMock = new HibernateHistoryOwnerRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).optional(any(), any(), any());

        Optional<HistoryOwner> actualOptionalHistoryOwner = historyOwnerRepositoryMock.findById(1);

        verify(crudRepositoryMock, times(1)).optional(any(), any(), any());
        assertThat(actualOptionalHistoryOwner).isEmpty();
    }

    @Test
    void whenFindAllThenReturnCollectionHistoryOwners() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<HistoryOwner> historyOwners = IntStream.rangeClosed(0, 3)
                .mapToObj(
                        value -> HistoryOwner.builder()
                                .car(testCars.get(value))
                                .owner(testOwners.get(value))
                                .startAt(now.minusDays(2))
                                .endAt(now)
                                .build()
                )
                .map(historyOwnerRepository::create)
                .toList();

        Collection<HistoryOwner> actualHistoryOwners = historyOwnerRepository.findAll();

        assertThat(actualHistoryOwners).hasSize(4);
        List<HistoryOwner> expectedHistoryOwners = IntStream.rangeClosed(0, 3).mapToObj(
                value -> HistoryOwner.builder()
                        .id(historyOwners.get(value).getId())
                        .car(historyOwners.get(value).getCar())
                        .owner(historyOwners.get(value).getOwner())
                        .startAt(now.minusDays(2))
                        .endAt(now)
                        .build()
        ).toList();
        assertThat(actualHistoryOwners.stream().map(this::copyOf).toList()).usingRecursiveComparison()
                .isEqualTo(expectedHistoryOwners);
    }

    @Test
    void whenFindAllAndEmptyDbThenReturnCollectionEmpty() {

        Collection<HistoryOwner> actualHistoryOwners = historyOwnerRepository.findAll();

        assertThat(actualHistoryOwners).isEmpty();
    }

    @Test
    void whenFindAllProcessExceptionThenReturnCollectionEmpty() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateHistoryOwnerRepository historyOwnerRepositoryMock = new HibernateHistoryOwnerRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).query(any(), any());

        Collection<HistoryOwner> actualHistoryOwners = historyOwnerRepositoryMock.findAll();

        verify(crudRepositoryMock, times(1)).query(any(), any());
        assertThat(actualHistoryOwners).isEmpty();
    }

    @Test
    void whenUpdateThenUpdated() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        HistoryOwner historyOwner = historyOwnerRepository.create(HistoryOwner.builder()
                .car(testCars.get(0))
                .owner(testOwners.get(0))
                .startAt(now.minusDays(2))
                .endAt(now)
                .build());
        HistoryOwner updateHistoryOwner = HistoryOwner.builder()
                .id(historyOwner.getId())
                .car(testCars.get(1))
                .owner(testOwners.get(0))
                .startAt(now.minusDays(10))
                .endAt(now.minusDays(1))
                .build();

        historyOwnerRepository.update(updateHistoryOwner);
        Optional<HistoryOwner> actualOptionalHistoryOwner = historyOwnerRepository.findById(historyOwner.getId());
        Collection<HistoryOwner> actualHistoryOwners = historyOwnerRepository.findAll();

        assertThat(actualHistoryOwners).hasSize(1);
        assertThat(actualOptionalHistoryOwner).isPresent();
        HistoryOwner expectedHistoryOwner = HistoryOwner.builder()
                .id(historyOwner.getId())
                .car(testCars.get(1))
                .owner(testOwners.get(0))
                .startAt(now.minusDays(10))
                .endAt(now.minusDays(1))
                .build();
        assertThat(copyOf(actualOptionalHistoryOwner.get())).usingRecursiveComparison()
                .isEqualTo(expectedHistoryOwner);
    }

    @Test
    void whenUpdateProcessExceptionThenNothing() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateHistoryOwnerRepository historyOwnerRepositoryMock = new HibernateHistoryOwnerRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any());

        historyOwnerRepositoryMock.update(new HistoryOwner());

        verify(crudRepositoryMock, times(1)).run(any());
    }

    @Test
    void whenDeleteThenDeleted() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        HistoryOwner historyOwner = historyOwnerRepository.create(HistoryOwner.builder()
                .car(testCars.get(0))
                .owner(testOwners.get(0))
                .startAt(now.minusDays(2))
                .endAt(now)
                .build());

        historyOwnerRepository.delete(historyOwner.getId());
        Optional<HistoryOwner> actualOptionalHistoryOwner = historyOwnerRepository.findById(historyOwner.getId());
        Collection<HistoryOwner> actualHistoryOwners = historyOwnerRepository.findAll();

        assertThat(actualOptionalHistoryOwner).isEmpty();
        assertThat(actualHistoryOwners).isEmpty();
    }

    @Test
    void whenDeleteWrongIdThenHistoryOwnerNotDeleted() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        HistoryOwner historyOwner = historyOwnerRepository.create(HistoryOwner.builder()
                .car(testCars.get(0))
                .owner(testOwners.get(0))
                .startAt(now.minusDays(2))
                .endAt(now)
                .build());

        historyOwnerRepository.delete(historyOwner.getId() + 1);
        Optional<HistoryOwner> actualOptionalHistoryOwner = historyOwnerRepository.findById(historyOwner.getId());
        Collection<HistoryOwner> actualHistoryOwners = historyOwnerRepository.findAll();

        assertThat(actualOptionalHistoryOwner).isPresent();
        assertThat(actualHistoryOwners).hasSize(1);
    }

    @Test
    void whenDeleteProcessExceptionThenNothing() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateHistoryOwnerRepository historyOwnerRepositoryMock = new HibernateHistoryOwnerRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any(), any());

        historyOwnerRepositoryMock.delete(1);

        verify(crudRepositoryMock, times(1)).run(any(), any());
    }
}