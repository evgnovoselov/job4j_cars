package ru.job4j.cars.repository.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.config.SessionFactoryConfig;
import ru.job4j.cars.model.Car;
import ru.job4j.cars.model.Engine;
import ru.job4j.cars.repository.EngineRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HibernateCarRepositoryTest {
    private static HibernateCarRepository carRepository;
    private static SessionFactory sessionFactory;
    private static EngineRepository engineRepository;
    private static List<Engine> testEngines;

    @BeforeAll
    static void beforeAll() {
        sessionFactory = new SessionFactoryConfig().createSessionFactory();
        CrudRepository crudRepository = new CrudRepository(sessionFactory);
        carRepository = new HibernateCarRepository(crudRepository);
        engineRepository = new HibernateEngineRepository(crudRepository);
        clearDataInDb();
        initialTestDataInDb();
    }

    private static void clearDataInDb() {
        clearEngineDataInDb();
        engineRepository.findAll().stream().map(Engine::getId).forEach(engineRepository::delete);
    }

    private static void clearEngineDataInDb() {
        carRepository.findAll().stream().map(Car::getId).forEach(carRepository::delete);
    }

    private static void initialTestDataInDb() {
        testEngines = IntStream.rangeClosed(1, 4)
                .mapToObj(value -> new Engine(null, "Engine-" + value))
                .map(engineRepository::create)
                .toList();
    }

    @AfterEach
    void tearDown() {
        clearEngineDataInDb();
    }

    @AfterAll
    static void afterAll() {
        clearDataInDb();
        sessionFactory.close();
    }

    private Car copyOf(Car car) {
        return Car.builder()
                .id(car.getId())
                .name(car.getName())
                .engine(car.getEngine())
                .build();
    }

    @Test
    void whenCreateCarThenCreatedInDbAndHaveId() {
        Car car = Car.builder()
                .name("Car")
                .engine(testEngines.get(0))
                .build();

        Car actualCar = carRepository.create(car);

        Car expectedCar = Car.builder()
                .id(actualCar.getId())
                .name("Car")
                .engine(testEngines.get(0))
                .build();
        assertThat(actualCar).usingRecursiveComparison()
                .isEqualTo(expectedCar);
    }

    @Test
    void whenCreateCarProcessExceptionThenReturnCarWithoutId() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateCarRepository carRepositoryMock = new HibernateCarRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any());

        carRepositoryMock.create(new Car());

        verify(crudRepositoryMock, times(1)).run(any());
    }

    @Test
    void whenFindByIdThenReturnOptionalWithCar() {
        Car car = carRepository.create(Car.builder()
                .name("Car")
                .engine(testEngines.get(0))
                .build());

        Optional<Car> actualCar = carRepository.findById(car.getId());

        assertThat(actualCar).isPresent();
        Car expectedCar = Car.builder()
                .id(car.getId())
                .name("Car")
                .engine(testEngines.get(0))
                .build();
        assertThat(copyOf(actualCar.get())).usingRecursiveComparison()
                .isEqualTo(expectedCar);
    }

    @Test
    void whenFindByIdWrongIdThenReturnOptionalEmpty() {
        Car car = carRepository.create(Car.builder()
                .name("Car")
                .engine(testEngines.get(0))
                .build());

        Optional<Car> actualCar = carRepository.findById(car.getId() + 1);

        assertThat(actualCar).isEmpty();
    }

    @Test
    void whenFindByIdProcessExceptionThenReturnOptionalEmpty() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateCarRepository carRepositoryMock = new HibernateCarRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).optional(any(), any(), any());

        Optional<Car> actualCar = carRepositoryMock.findById(1);

        verify(crudRepositoryMock, times(1)).optional(any(), any(), any());
        assertThat(actualCar).isEmpty();
    }

    @Test
    void whenFindAllThenReturnCollectionCars() {
        List<Car> cars = IntStream.rangeClosed(1, 4)
                .mapToObj(value -> Car.builder()
                        .name("Car-" + value)
                        .engine(testEngines.get(value - 1))
                        .build())
                .map(carRepository::create)
                .toList();

        Collection<Car> actualCars = carRepository.findAll();

        assertThat(actualCars).hasSize(4);
        List<Car> expectedCars = IntStream.rangeClosed(1, 4)
                .mapToObj(value -> Car.builder()
                        .id(cars.get(value - 1).getId())
                        .name("Car-" + value)
                        .engine(testEngines.get(value - 1))
                        .build())
                .toList();
        assertThat(actualCars.stream().map(this::copyOf).toList()).usingRecursiveComparison()
                .isEqualTo(expectedCars);
    }

    @Test
    void whenFindAllAndEmptyDbThenReturnCollectionEmpty() {

        Collection<Car> actualCars = carRepository.findAll();

        assertThat(actualCars).isEmpty();
    }

    @Test
    void whenFindAllProcessExceptionThenReturnCollectionEmpty() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateCarRepository carRepositoryMock = new HibernateCarRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).query(any(), any());

        Collection<Car> actualCars = carRepositoryMock.findAll();

        verify(crudRepositoryMock, times(1)).query(any(), any());
        assertThat(actualCars).isEmpty();
    }

    @Test
    void whenUpdateThenUpdated() {
        Car car = carRepository.create(Car.builder()
                .name("Car")
                .engine(testEngines.get(0))
                .build());
        Car updateCar = Car.builder()
                .id(car.getId())
                .name("Updated Car")
                .engine(testEngines.get(1))
                .build();

        carRepository.update(updateCar);
        Optional<Car> actualOptionalCar = carRepository.findById(car.getId());
        Collection<Car> actualCars = carRepository.findAll();

        assertThat(actualOptionalCar).isPresent();
        Car expectedCar = Car.builder()
                .id(car.getId())
                .name("Updated Car")
                .engine(testEngines.get(1))
                .build();
        assertThat(copyOf(actualOptionalCar.get())).usingRecursiveComparison()
                .isEqualTo(expectedCar);
        assertThat(actualCars).hasSize(1);
    }

    @Test
    void whenUpdateProcessExceptionThenNothing() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateCarRepository carRepositoryMock = new HibernateCarRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any());

        carRepositoryMock.update(new Car());

        verify(crudRepositoryMock, times(1)).run(any());
    }

    @Test
    void whenDeleteThenDeleted() {
        Car car = carRepository.create(Car.builder()
                .name("Car")
                .engine(testEngines.get(0))
                .build());

        carRepository.delete(car.getId());
        Optional<Car> actualOptionalCar = carRepository.findById(car.getId());
        Collection<Car> actualCars = carRepository.findAll();

        assertThat(actualOptionalCar).isEmpty();
        assertThat(actualCars).isEmpty();
    }

    @Test
    void whenDeleteWrongIdThenCarNotDeleted() {
        Car car = carRepository.create(Car.builder()
                .name("Car")
                .engine(testEngines.get(0))
                .build());

        carRepository.delete(car.getId() + 1);
        Optional<Car> actualOptionalCar = carRepository.findById(car.getId());
        Collection<Car> actualCars = carRepository.findAll();

        assertThat(actualOptionalCar).isPresent();
        assertThat(actualCars).hasSize(1);
    }

    @Test
    void whenDeleteProcessExceptionThenNothing() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateCarRepository carRepositoryMock = new HibernateCarRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any(), any());

        carRepositoryMock.delete(1);

        verify(crudRepositoryMock, times(1)).run(any(), any());
    }
}