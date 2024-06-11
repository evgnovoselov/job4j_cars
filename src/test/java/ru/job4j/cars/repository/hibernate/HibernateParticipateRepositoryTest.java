package ru.job4j.cars.repository.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.config.SessionFactoryConfig;
import ru.job4j.cars.model.Participate;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.model.User;
import ru.job4j.cars.repository.PostRepository;
import ru.job4j.cars.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HibernateParticipateRepositoryTest {
    private static HibernateParticipateRepository participateRepository;
    private static SessionFactory sessionFactory;
    private static UserRepository userRepository;
    private static List<User> testUsers;
    private static PostRepository postRepository;
    private static List<Post> testPosts;

    @BeforeAll
    static void beforeAll() {
        sessionFactory = new SessionFactoryConfig().createSessionFactory();
        CrudRepository crudRepository = new CrudRepository(sessionFactory);
        participateRepository = new HibernateParticipateRepository(crudRepository);
        userRepository = new HibernateUserRepository(crudRepository);
        postRepository = new HibernatePostRepository(crudRepository);
        clearDataInDb();
        initDataInDb();
    }

    private static void initDataInDb() {
        testUsers = IntStream.rangeClosed(1, 4).mapToObj(
                value -> User.builder()
                        .login("user-" + value)
                        .password("password" + value)
                        .build()
        ).map(userRepository::create).toList();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        testPosts = IntStream.rangeClosed(1, 4).mapToObj(
                value -> Post.builder()
                        .description("Post description #" + value)
                        .created(now)
                        .user(testUsers.get(value - 1))
                        .build()
        ).map(postRepository::create).toList();
    }

    private static void clearDataInDb() {
        clearParticipateDataInDb();
        postRepository.findAllOrderByCreated().stream().map(Post::getId).forEach(postRepository::delete);
        userRepository.findAllOrderById().stream().map(User::getId).forEach(userRepository::delete);

    }

    private static void clearParticipateDataInDb() {
        participateRepository.findAll().stream().map(Participate::getId).forEach(participateRepository::delete);
    }

    @AfterEach
    void tearDown() {
        clearParticipateDataInDb();
    }

    @AfterAll
    static void afterAll() {
        clearDataInDb();
        sessionFactory.close();
    }

    private Participate copyOf(Participate participate) {
        return Participate.builder()
                .id(participate.getId())
                .user(participate.getUser())
                .post(copyOf(participate.getPost()))
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
    void whenCreateParticipateThenCreatedInDbAndHaveId() {
        Participate participate = Participate.builder()
                .user(testUsers.get(0))
                .post(testPosts.get(0))
                .build();

        Participate actualParticipate = participateRepository.create(participate);

        assertThat(actualParticipate.getId()).isNotZero();
        Participate expectedParticipate = Participate.builder()
                .id(actualParticipate.getId())
                .user(testUsers.get(0))
                .post(testPosts.get(0))
                .build();
        assertThat(actualParticipate).usingRecursiveComparison()
                .isEqualTo(expectedParticipate);
    }

    @Test
    void whenCreateParticipateProcessExceptionThenReturnParticipateWithoutId() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        HibernateParticipateRepository participateRepositoryMock = new HibernateParticipateRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any());

        Participate actualParticipate = participateRepositoryMock.create(new Participate());

        verify(crudRepositoryMock, times(1)).run(any());
        assertThat(actualParticipate.getId()).isNull();
    }

    @Test
    void whenFindByIdThenReturnOptionalWithParticipate() {
        Participate participate = participateRepository.create(Participate.builder()
                .user(testUsers.get(1))
                .post(testPosts.get(0))
                .build());

        Optional<Participate> actualOptionalParticipate = participateRepository.findById(participate.getId());

        assertThat(actualOptionalParticipate).isPresent();
        Participate expectedParticipate = Participate.builder()
                .id(participate.getId())
                .user(testUsers.get(1))
                .post(testPosts.get(0))
                .build();
        assertThat(copyOf(actualOptionalParticipate.get())).usingRecursiveComparison()
                .isEqualTo(expectedParticipate);
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