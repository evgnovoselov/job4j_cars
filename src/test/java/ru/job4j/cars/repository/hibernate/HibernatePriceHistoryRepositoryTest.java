package ru.job4j.cars.repository.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.config.SessionFactoryConfig;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.model.PriceHistory;
import ru.job4j.cars.model.User;
import ru.job4j.cars.repository.PostRepository;
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

class HibernatePriceHistoryRepositoryTest {
    private static SessionFactory sessionFactory;
    private static HibernatePriceHistoryRepository priceHistoryRepository;
    private static PostRepository postRepository;
    private static List<Post> testPosts;
    private static UserRepository userRepository;
    private static List<User> testUsers;

    @BeforeAll
    static void beforeAll() {
        sessionFactory = new SessionFactoryConfig().createSessionFactory();
        CrudRepository crudRepository = new CrudRepository(sessionFactory);
        priceHistoryRepository = new HibernatePriceHistoryRepository(crudRepository);
        postRepository = new HibernatePostRepository(crudRepository);
        userRepository = new HibernateUserRepository(crudRepository);
        clearDataInDb();
        initDataInDb();
    }

    private static void initDataInDb() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        testUsers = IntStream.rangeClosed(0, 3).mapToObj(
                        value -> User.builder()
                                .login("user-" + value)
                                .password("password-" + value)
                                .build()
                )
                .map(userRepository::create)
                .toList();
        testPosts = IntStream.rangeClosed(0, 3).mapToObj(
                        value -> Post.builder()
                                .description("Post description #" + value)
                                .created(now)
                                .user(testUsers.get(value))
                                .build()
                )
                .map(postRepository::create)
                .toList();
    }

    private static void clearDataInDb() {
        clearPriceHistoryDataInDb();
        postRepository.findAllOrderByCreated().stream().map(Post::getId).forEach(postRepository::delete);
        userRepository.findAllOrderById().stream().map(User::getId).forEach(userRepository::delete);
    }

    private static void clearPriceHistoryDataInDb() {
        priceHistoryRepository.findAll().stream().map(PriceHistory::getId).forEach(priceHistoryRepository::delete);
    }

    @AfterEach
    void tearDown() {
        clearPriceHistoryDataInDb();
    }

    @AfterAll
    static void afterAll() {
        clearDataInDb();
        sessionFactory.close();
    }

    private PriceHistory copyOf(PriceHistory priceHistory) {
        return PriceHistory.builder()
                .id(priceHistory.getId())
                .before(priceHistory.getBefore())
                .after(priceHistory.getAfter())
                .created(priceHistory.getCreated())
                .post(copyOf(priceHistory.getPost()))
                .build();
    }

    private Post copyOf(Post post) {
        return Post.builder()
                .id(post.getId())
                .description(post.getDescription())
                .user(post.getUser())
                .created(post.getCreated())
                .build();
    }

    @Test
    void whenCreatePriceHistoryThenCreatedInDbAndHaveId() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        PriceHistory priceHistory = PriceHistory.builder()
                .before(100)
                .after(150)
                .created(now)
                .post(testPosts.get(0))
                .build();

        PriceHistory actualPriceHistory = priceHistoryRepository.create(priceHistory);

        assertThat(actualPriceHistory.getId()).isNotZero();
        PriceHistory expectedPriceHistory = PriceHistory.builder()
                .id(actualPriceHistory.getId())
                .before(100)
                .after(150)
                .created(now)
                .post(testPosts.get(0))
                .build();
        assertThat(actualPriceHistory).usingRecursiveComparison()
                .isEqualTo(expectedPriceHistory);
    }

    @Test
    void whenCreatePriceHistoryProcessExceptionThenReturnPriceHistoryWithoutId() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernatePriceHistoryRepository priceHistoryRepositoryMock = new HibernatePriceHistoryRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any());

        priceHistoryRepositoryMock.create(new PriceHistory());

        verify(crudRepositoryMock, times(1)).run(any());
    }

    @Test
    void whenFindByIdThenReturnOptionalWithPriceHistory() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        PriceHistory priceHistory = priceHistoryRepository.create(PriceHistory.builder()
                .before(100)
                .after(150)
                .created(now)
                .post(testPosts.get(0))
                .build());

        Optional<PriceHistory> actualOptionalPriceHistory = priceHistoryRepository.findById(priceHistory.getId());

        assertThat(actualOptionalPriceHistory).isPresent();
        PriceHistory expectedPriceHistory = PriceHistory.builder()
                .id(priceHistory.getId())
                .before(100)
                .after(150)
                .created(now)
                .post(testPosts.get(0))
                .build();
        assertThat(copyOf(actualOptionalPriceHistory.get())).usingRecursiveComparison()
                .isEqualTo(expectedPriceHistory);
    }

    @Test
    void whenFindByIdWrongIdThenReturnOptionalEmpty() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        PriceHistory priceHistory = priceHistoryRepository.create(PriceHistory.builder()
                .before(100)
                .after(150)
                .created(now)
                .post(testPosts.get(0))
                .build());

        Optional<PriceHistory> actualOptionalPriceHistory = priceHistoryRepository.findById(priceHistory.getId() + 1);

        assertThat(actualOptionalPriceHistory).isEmpty();
    }

    @Test
    void whenFindByIdProcessExceptionThenReturnOptionalEmpty() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernatePriceHistoryRepository priceHistoryRepositoryMock = new HibernatePriceHistoryRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).optional(any(), any(), any());

        Optional<PriceHistory> actualOptionalPriceHistory = priceHistoryRepositoryMock.findById(1);

        verify(crudRepositoryMock, times(1)).optional(any(), any(), any());
        assertThat(actualOptionalPriceHistory).isEmpty();
    }

    @Test
    void whenFindAllThenReturnCollectionPriceHistories() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<PriceHistory> priceHistories = IntStream.rangeClosed(0, 3).mapToObj(
                        value -> PriceHistory.builder()
                                .before(value + 100)
                                .after(value + 200)
                                .created(now)
                                .post(testPosts.get(0))
                                .build()
                )
                .map(priceHistoryRepository::create)
                .toList();

        Collection<PriceHistory> actualPriceHistories = priceHistoryRepository.findAll();

        assertThat(actualPriceHistories).hasSize(4);
        List<PriceHistory> expectedPriceHistories = IntStream.rangeClosed(0, 3).mapToObj(
                value -> PriceHistory.builder()
                        .id(priceHistories.get(value).getId())
                        .before(value + 100)
                        .after(value + 200)
                        .created(now)
                        .post(testPosts.get(0))
                        .build()
        ).toList();
        assertThat(actualPriceHistories.stream().map(this::copyOf).toList()).usingRecursiveComparison()
                .isEqualTo(expectedPriceHistories);
    }

    @Test
    void whenFindAllAndEmptyDbThenReturnCollectionEmpty() {
        Collection<PriceHistory> actualPriceHistories = priceHistoryRepository.findAll();

        assertThat(actualPriceHistories).hasSize(0);
    }

    @Test
    void whenFindAllProcessExceptionThenReturnCollectionEmpty() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernatePriceHistoryRepository priceHistoryRepositoryMock = new HibernatePriceHistoryRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).query(any(), any());

        Collection<PriceHistory> actualPriceHistories = priceHistoryRepositoryMock.findAll();

        verify(crudRepositoryMock, times(1)).query(any(), any());
        assertThat(actualPriceHistories).isEmpty();
    }

    @Test
    void whenUpdateThenUpdated() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        PriceHistory priceHistory = priceHistoryRepository.create(PriceHistory.builder()
                .before(100)
                .after(150)
                .created(now)
                .post(testPosts.get(0))
                .build());
        PriceHistory updatePriceHistory = PriceHistory.builder()
                .id(priceHistory.getId())
                .before(400)
                .after(550)
                .created(now)
                .post(testPosts.get(0))
                .build();

        priceHistoryRepository.update(updatePriceHistory);
        Optional<PriceHistory> actualOptionalPriceHistory = priceHistoryRepository.findById(priceHistory.getId());
        Collection<PriceHistory> actualPriceHistories = priceHistoryRepository.findAll();

        assertThat(actualPriceHistories).hasSize(1);
        assertThat(actualOptionalPriceHistory).isPresent();
        PriceHistory expectedPriceHistory = PriceHistory.builder()
                .id(priceHistory.getId())
                .before(400)
                .after(550)
                .created(now)
                .post(testPosts.get(0))
                .build();
        assertThat(copyOf(actualOptionalPriceHistory.get())).usingRecursiveComparison()
                .isEqualTo(expectedPriceHistory);
    }

    @Test
    void whenUpdateProcessExceptionThenNothing() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernatePriceHistoryRepository priceHistoryRepositoryMock = new HibernatePriceHistoryRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any());

        priceHistoryRepositoryMock.update(new PriceHistory());

        verify(crudRepositoryMock, times(1)).run(any());
    }

    @Test
    void whenDeleteThenDeleted() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        PriceHistory priceHistory = priceHistoryRepository.create(PriceHistory.builder()
                .before(100)
                .after(150)
                .created(now)
                .post(testPosts.get(0))
                .build());

        priceHistoryRepository.delete(priceHistory.getId());
        Optional<PriceHistory> actualOptionalPriceHistory = priceHistoryRepository.findById(priceHistory.getId());
        Collection<PriceHistory> actualPriceHistories = priceHistoryRepository.findAll();

        assertThat(actualOptionalPriceHistory).isEmpty();
        assertThat(actualPriceHistories).isEmpty();
    }

    @Test
    void whenDeleteWrongIdThenPriceHistoryNotDeleted() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        PriceHistory priceHistory = priceHistoryRepository.create(PriceHistory.builder()
                .before(100)
                .after(150)
                .created(now)
                .post(testPosts.get(0))
                .build());

        priceHistoryRepository.delete(priceHistory.getId() + 1);
        Optional<PriceHistory> actualOptionalPriceHistory = priceHistoryRepository.findById(priceHistory.getId());
        Collection<PriceHistory> actualPriceHistories = priceHistoryRepository.findAll();

        assertThat(actualPriceHistories).hasSize(1);
        assertThat(actualOptionalPriceHistory).isPresent();
        PriceHistory expectedPriceHistory = PriceHistory.builder()
                .id(priceHistory.getId())
                .before(100)
                .after(150)
                .created(now)
                .post(testPosts.get(0))
                .build();
        assertThat(copyOf(actualOptionalPriceHistory.get())).usingRecursiveComparison()
                .isEqualTo(expectedPriceHistory);
    }

    @Test
    void whenDeleteProcessExceptionThenNothing() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernatePriceHistoryRepository priceHistoryRepositoryMock = new HibernatePriceHistoryRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any(), any());

        priceHistoryRepositoryMock.delete(1);

        verify(crudRepositoryMock, times(1)).run(any(), any());
    }
}