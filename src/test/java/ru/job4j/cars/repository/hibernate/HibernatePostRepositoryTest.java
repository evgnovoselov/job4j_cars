package ru.job4j.cars.repository.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.config.SessionFactoryConfig;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.model.User;
import ru.job4j.cars.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HibernatePostRepositoryTest {
    private static SessionFactory sessionFactory;
    private static HibernatePostRepository postRepository;
    private static UserRepository userRepository;
    private static List<User> testUsers;

    @BeforeAll
    static void beforeAll() {
        sessionFactory = new SessionFactoryConfig().createSessionFactory();
        CrudRepository crudRepository = new CrudRepository(sessionFactory);
        postRepository = new HibernatePostRepository(crudRepository);
        userRepository = new HibernateUserRepository(crudRepository);
        clearDb();
        initTestDb();
    }

    private static void clearPostDb() {
        postRepository.findAllOrderByCreated().stream().map(Post::getId).forEach(postRepository::delete);
    }

    private static void clearDb() {
        clearPostDb();
        userRepository.findAllOrderById().stream().map(User::getId).forEach(userRepository::delete);
    }

    private static void initTestDb() {
        testUsers = Stream.of(
                new User(0, "ivanov", "password"),
                new User(0, "petrov", "password"),
                new User(0, "durov", "password")
        ).map(userRepository::create).toList();
    }

    @BeforeEach
    void setUp() {
        clearPostDb();
    }

    @AfterAll
    static void afterAll() {
        clearDb();
        sessionFactory.close();
    }

    private static Post copyPost(Post post) {
        return Post.builder()
                .id(post.getId())
                .description(post.getDescription())
                .user(post.getUser())
                .created(post.getCreated())
                .build();
    }

    @Test
    void whenSavePostThenReturnPostAndSetIdInPost() {
        LocalDateTime now = LocalDateTime.now();
        Post post = Post.builder()
                .description("Test description")
                .created(now)
                .user(testUsers.get(0))
                .build();

        Post actualPost = postRepository.create(post);

        Post expectedPost = Post.builder()
                .id(actualPost.getId())
                .description("Test description")
                .created(now)
                .user(testUsers.get(0))
                .build();
        assertThat(actualPost.getId()).isNotZero();
        assertThat(actualPost).usingRecursiveComparison().isEqualTo(expectedPost);
    }

    @Test
    void whenFindByIdThenReturnOptionalPost() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Post post = postRepository.create(Post.builder()
                .description("Test description")
                .created(now)
                .user(testUsers.get(0))
                .build());

        Optional<Post> actualOptionPost = postRepository.findById(post.getId());

        Post expectedPost = Post.builder()
                .id(post.getId())
                .description("Test description")
                .created(now)
                .user(testUsers.get(0))
                .build();
        assertThat(actualOptionPost).isPresent();
        Post actualPost = copyPost(actualOptionPost.get());
        assertThat(actualPost.getId()).isNotZero();
        assertThat(actualPost).usingRecursiveComparison().isEqualTo(expectedPost);
    }

    @Test
    void whenClearDbAndFindByIdThenReturnOptionalEmpty() {

        Optional<Post> actualOptionPost = postRepository.findById(1);

        assertThat(actualOptionPost).isEmpty();
    }

    @Test
    void whenUpdatePostThenUpdated() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Post post = postRepository.create(Post.builder()
                .description("Test description")
                .created(now)
                .user(testUsers.get(0))
                .build());
        Post updatePost = Post.builder()
                .id(post.getId())
                .description("New test description")
                .created(now)
                .user(testUsers.get(0))
                .build();

        postRepository.update(updatePost);
        Optional<Post> actualOptionPost = postRepository.findById(post.getId());

        Post expectedPost = Post.builder()
                .id(post.getId())
                .description("New test description")
                .created(now)
                .user(testUsers.get(0))
                .build();
        assertThat(actualOptionPost).isPresent();
        Post actualPost = copyPost(actualOptionPost.get());
        assertThat(actualPost.getId()).isNotZero();
        assertThat(actualPost).usingRecursiveComparison().isEqualTo(expectedPost);
    }

    @Test
    void whenDeletePostThenDeleted() {
        LocalDateTime now = LocalDateTime.now();
        Post post = postRepository.create(Post.builder()
                .description("Test description")
                .created(now)
                .user(testUsers.get(0))
                .build());

        postRepository.delete(post.getId());
        Optional<Post> actualOptionalPost = postRepository.findById(post.getId());
        Collection<Post> actualPosts = postRepository.findAllOrderByCreated();

        assertThat(actualOptionalPost).isEmpty();
        assertThat(actualPosts).isEmpty();
    }

    @Test
    void whenDeletePostWrongIdThenNotDeleted() {
        LocalDateTime now = LocalDateTime.now();
        Post post = postRepository.create(Post.builder()
                .description("Test description")
                .created(now)
                .user(testUsers.get(0))
                .build());

        postRepository.delete(post.getId() + 10);
        Optional<Post> actualOptionalPost = postRepository.findById(post.getId());
        Collection<Post> actualPosts = postRepository.findAllOrderByCreated();

        assertThat(actualOptionalPost).isPresent();
        assertThat(actualPosts).hasSize(1);
    }

    @Test
    void whenFindAllByCreatedBetweenThenReturnCollectionPostCreatedBetween() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<LocalDateTime> times = List.of(
                now,
                now.minusMinutes(10),
                now.minusMinutes(20),
                now.minusMinutes(30)
        );
        List<Post> posts = IntStream.range(0, 4).boxed().map(
                num -> Post.builder()
                        .description("Post description %s".formatted(num))
                        .created(times.get(num))
                        .user(testUsers.get(num % 3))
                        .build()
        ).map(postRepository::create).toList();

        Collection<Post> actualPosts = postRepository.findAllByCreatedBetween(
                now.minusMinutes(25), now.minusMinutes(5)
        ).stream().map(HibernatePostRepositoryTest::copyPost).toList();

        assertThat(actualPosts).hasSize(2);
        List<Post> expectedPosts = IntStream.range(1, 3).boxed().map(
                num -> Post.builder()
                        .id(posts.get(num).getId())
                        .description("Post description %s".formatted(num))
                        .created(times.get(num))
                        .user(testUsers.get(num % 3))
                        .build()
        ).toList();
        assertThat(actualPosts).usingRecursiveComparison().isEqualTo(expectedPosts);
    }

    @Test
    void whenFindAllWherePhotoIsNotNullThenReturnCollectionPostWithPhoto() {

    }

    @Test
    void whenFindAllByCarNameLikeThenReturnCollectionPostWithLikeCarName() {

    }

    @Test
    void whenDeletePostThenPostNotHaveInDb() {
        LocalDateTime now = LocalDateTime.now();
        Post post = postRepository.create(Post.builder()
                .description("Test description")
                .created(now)
                .user(testUsers.get(0))
                .build());

        postRepository.delete(post.getId());
        Optional<Post> actualPost = postRepository.findById(post.getId());
        Collection<Post> actualPosts = postRepository.findAllOrderByCreated();

        assertThat(actualPost).isEmpty();
        assertThat(actualPosts).isEmpty();
    }

    @Test
    void whenDeletePostWithWrongIdThenNotDeletedPostInDb() {
        LocalDateTime now = LocalDateTime.now();
        Post post = postRepository.create(Post.builder()
                .description("Test description")
                .created(now)
                .user(testUsers.get(0))
                .build());

        postRepository.delete(post.getId() + 1);
        Optional<Post> actualPost = postRepository.findById(post.getId());
        Collection<Post> actualPosts = postRepository.findAllOrderByCreated();

        assertThat(actualPost).isPresent();
        assertThat(actualPosts).hasSize(1);
    }
}