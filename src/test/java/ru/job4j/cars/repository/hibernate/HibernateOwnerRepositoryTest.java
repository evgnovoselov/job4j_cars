package ru.job4j.cars.repository.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.config.SessionFactoryConfig;
import ru.job4j.cars.model.Owner;
import ru.job4j.cars.model.User;
import ru.job4j.cars.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HibernateOwnerRepositoryTest {
    private static HibernateOwnerRepository ownerRepository;
    private static SessionFactory sessionFactory;
    private static UserRepository userRepository;
    private static List<User> testUsers;

    @BeforeAll
    static void beforeAll() {
        sessionFactory = new SessionFactoryConfig().createSessionFactory();
        CrudRepository crudRepository = new CrudRepository(sessionFactory);
        ownerRepository = new HibernateOwnerRepository(crudRepository);
        userRepository = new HibernateUserRepository(crudRepository);
        clearDataInDb();
        initialDataInDb();
    }

    private static void initialDataInDb() {
        testUsers = IntStream.rangeClosed(1, 4)
                .mapToObj(value -> User.builder()
                        .login("user-" + value)
                        .password("password")
                        .build())
                .map(userRepository::create)
                .toList();
    }

    private static void clearDataInDb() {
        clearOwnerDataInDb();
        userRepository.findAllOrderById().stream().map(User::getId).forEach(userRepository::delete);
    }

    private static void clearOwnerDataInDb() {
        ownerRepository.findAll().stream().map(Owner::getId).forEach(ownerRepository::delete);
    }

    @AfterEach
    void tearDown() {
        clearOwnerDataInDb();
    }

    @AfterAll
    static void afterAll() {
        clearDataInDb();
        sessionFactory.close();
    }

    private Owner copyOf(Owner owner) {
        return Owner.builder()
                .id(owner.getId())
                .name(owner.getName())
                .user(owner.getUser())
                .build();
    }

    @Test
    void whenCreateOwnerThenCreatedInDbAndHaveId() {
        Owner owner = Owner.builder()
                .name("owner")
                .user(testUsers.get(0))
                .build();

        Owner actualOwner = ownerRepository.create(owner);

        assertThat(actualOwner.getId()).isNotZero();
        Owner expectedOwner = Owner.builder()
                .id(actualOwner.getId())
                .name("owner")
                .user(testUsers.get(0))
                .build();
        assertThat(actualOwner).usingRecursiveComparison()
                .isEqualTo(expectedOwner);
    }

    @Test
    void whenCreateOwnerProcessExceptionThenReturnOwnerWithoutId() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateOwnerRepository ownerRepositoryMock = new HibernateOwnerRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any());

        ownerRepositoryMock.create(new Owner());

        verify(crudRepositoryMock, times(1)).run(any());
    }

    @Test
    void whenFindByIdThenReturnOptionalWithOwner() {
        Owner owner = ownerRepository.create(Owner.builder()
                .name("owner")
                .user(testUsers.get(0))
                .build());

        Optional<Owner> actualOwner = ownerRepository.findById(owner.getId());

        assertThat(actualOwner).isPresent();
        Owner expectedOwner = Owner.builder()
                .id(owner.getId())
                .name("owner")
                .user(testUsers.get(0))
                .build();
        assertThat(copyOf(actualOwner.get())).usingRecursiveComparison()
                .isEqualTo(expectedOwner);
    }

    @Test
    void whenFindByIdWrongIdThenReturnOptionalEmpty() {
        Owner owner = ownerRepository.create(Owner.builder()
                .name("owner")
                .user(testUsers.get(0))
                .build());

        Optional<Owner> actualOwner = ownerRepository.findById(owner.getId() + 1);

        assertThat(actualOwner).isEmpty();
    }

    @Test
    void whenFindByIdProcessExceptionThenReturnOptionalEmpty() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateOwnerRepository ownerRepositoryMock = new HibernateOwnerRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).optional(any(), any(), any());

        Optional<Owner> actualOptionalOwner = ownerRepositoryMock.findById(1);

        verify(crudRepositoryMock, times(1)).optional(any(), any(), any());
        assertThat(actualOptionalOwner).isEmpty();
    }

    @Test
    void whenFindAllThenReturnCollectionOwners() {
        List<Owner> owners = IntStream.rangeClosed(1, 4)
                .mapToObj(value -> Owner.builder()
                        .name("owner-" + value)
                        .user(testUsers.get((value - 1) % testUsers.size()))
                        .build())
                .map(ownerRepository::create)
                .toList();

        Collection<Owner> actualOwners = ownerRepository.findAll();

        assertThat(actualOwners).hasSize(4);
        List<Owner> expectedOwners = IntStream.rangeClosed(1, 4)
                .mapToObj(value -> Owner.builder()
                        .id(owners.get(value - 1).getId())
                        .name("owner-" + value)
                        .user(testUsers.get((value - 1) % testUsers.size()))
                        .build())
                .toList();
        assertThat(actualOwners.stream().map(this::copyOf).toList()).usingRecursiveComparison()
                .isEqualTo(expectedOwners);
    }

    @Test
    void whenFindAllAndEmptyDbThenReturnCollectionEmpty() {

        Collection<Owner> actualOwners = ownerRepository.findAll();

        assertThat(actualOwners).isEmpty();
    }

    @Test
    void whenFindAllProcessExceptionThenReturnCollectionEmpty() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateOwnerRepository ownerRepositoryMock = new HibernateOwnerRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).query(any(), any());

        Collection<Owner> actualOwners = ownerRepositoryMock.findAll();

        verify(crudRepositoryMock, times(1)).query(any(), any());
        assertThat(actualOwners).isEmpty();
    }

    @Test
    void whenUpdateThenUpdated() {
        Owner owner = ownerRepository.create(Owner.builder()
                .name("owner")
                .user(testUsers.get(0))
                .build());
        Owner updateOwner = Owner.builder()
                .id(owner.getId())
                .name("update-owner")
                .user(testUsers.get(0))
                .build();

        ownerRepository.update(updateOwner);
        Optional<Owner> actualOptionalOwner = ownerRepository.findById(owner.getId());
        Collection<Owner> actualOwners = ownerRepository.findAll();

        assertThat(actualOptionalOwner).isPresent();
        Owner expectedOwner = Owner.builder()
                .id(owner.getId())
                .name("update-owner")
                .user(testUsers.get(0))
                .build();
        assertThat(copyOf(actualOptionalOwner.get())).usingRecursiveComparison()
                .isEqualTo(expectedOwner);
        assertThat(actualOwners).hasSize(1);
    }

    @Test
    void whenUpdateProcessExceptionThenNothing() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateOwnerRepository ownerRepositoryMock = new HibernateOwnerRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any());

        ownerRepositoryMock.update(new Owner());

        verify(crudRepositoryMock, times(1)).run(any());
    }

    @Test
    void whenDeleteThenDeleted() {
        Owner owner = ownerRepository.create(Owner.builder()
                .name("owner")
                .user(testUsers.get(0))
                .build());

        ownerRepository.delete(owner.getId());
        Optional<Owner> actualOptionalOwner = ownerRepository.findById(owner.getId());
        Collection<Owner> actualOwners = ownerRepository.findAll();

        assertThat(actualOptionalOwner).isEmpty();
        assertThat(actualOwners).isEmpty();
    }

    @Test
    void whenDeleteWrongIdThenOwnerNotDeleted() {
        Owner owner = ownerRepository.create(Owner.builder()
                .name("owner")
                .user(testUsers.get(0))
                .build());

        ownerRepository.delete(owner.getId() + 1);
        Optional<Owner> actualOptionalOwner = ownerRepository.findById(owner.getId());
        Collection<Owner> actualOwners = ownerRepository.findAll();

        assertThat(actualOptionalOwner).isPresent();
        assertThat(actualOwners).hasSize(1);
    }

    @Test
    void whenDeleteProcessExceptionThenNothing() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateOwnerRepository ownerRepositoryMock = new HibernateOwnerRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any(), any());

        ownerRepositoryMock.delete(1);

        verify(crudRepositoryMock, times(1)).run(any(), any());
    }
}