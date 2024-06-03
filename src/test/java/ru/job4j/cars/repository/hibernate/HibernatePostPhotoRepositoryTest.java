package ru.job4j.cars.repository.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.config.SessionFactoryConfig;
import ru.job4j.cars.model.File;
import ru.job4j.cars.model.Post;
import ru.job4j.cars.model.PostPhoto;
import ru.job4j.cars.model.User;
import ru.job4j.cars.repository.FileRepository;
import ru.job4j.cars.repository.PostPhotoRepository;
import ru.job4j.cars.repository.PostRepository;
import ru.job4j.cars.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HibernatePostPhotoRepositoryTest {
    private static SessionFactory sessionFactory;
    private static HibernatePostPhotoRepository postPhotoRepository;
    private static PostRepository postRepository;
    private static FileRepository fileRepository;
    private static UserRepository userRepository;
    private static List<User> testUsers;
    private static List<Post> testPosts;
    private static List<File> testFiles;

    @BeforeAll
    static void beforeAll() {
        sessionFactory = new SessionFactoryConfig().createSessionFactory();
        CrudRepository crudRepository = new CrudRepository(sessionFactory);
        postPhotoRepository = new HibernatePostPhotoRepository(crudRepository);
        clearPostPhotoInDb();
        postRepository = new HibernatePostRepository(crudRepository);
        fileRepository = new HibernateFileRepository(crudRepository);
        userRepository = new HibernateUserRepository(crudRepository);
        clearPostsAndFilesAndUsersInDb();
        initTestDataInDb();
    }

    private static void initTestDataInDb() {
        testUsers = Stream.of(
                new User(0, "ivanov", "password"),
                new User(0, "petrov", "password"),
                new User(0, "durov", "password")
        ).map(userRepository::create).toList();
        testPosts = IntStream.rangeClosed(0, 2).mapToObj(value -> postRepository.create(Post.builder()
                .description("Post-" + value)
                .user(testUsers.get(value))
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build())).toList();
        testFiles = IntStream.rangeClosed(0, 2).mapToObj(value -> fileRepository.create(File.builder()
                .name("fileName-" + value)
                .path("path/file-" + value)
                .build())).toList();
    }

    private static void clearPostsAndFilesAndUsersInDb() {
        postRepository.findAllOrderByCreated().stream().map(Post::getId).forEach(postRepository::delete);
        fileRepository.findAll().stream().map(File::getId).forEach(fileRepository::delete);
        userRepository.findAllOrderById().stream().map(User::getId).forEach(userRepository::delete);
    }

    private static void clearPostPhotoInDb() {
        postPhotoRepository.findAll().stream().map(PostPhoto::getId).forEach(postPhotoRepository::delete);
    }

    @AfterEach
    void tearDown() {
        clearPostPhotoInDb();
    }

    @AfterAll
    static void afterAll() {
        clearPostsAndFilesAndUsersInDb();
        sessionFactory.close();
    }

    private static PostPhoto copyPostPhoto(PostPhoto postPhoto) {
        return PostPhoto.builder()
                .id(postPhoto.getId())
                .post(copyPost(postPhoto.getPost()))
                .photo(postPhoto.getPhoto())
                .sort(postPhoto.getSort())
                .build();
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
    void whenCreateThenReturnPostPhotoWithId() {
        PostPhoto postPhoto = PostPhoto.builder()
                .post(testPosts.get(0))
                .photo(testFiles.get(0))
                .sort(1000)
                .build();

        PostPhoto actualPostPhoto = postPhotoRepository.create(postPhoto);

        PostPhoto expected = PostPhoto.builder()
                .id(actualPostPhoto.getId())
                .sort(1000)
                .post(testPosts.get(0))
                .photo(testFiles.get(0))
                .build();
        assertThat(actualPostPhoto).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void whenCreateThrowExceptionThenReturnPostPhotoWithoutId() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        PostPhotoRepository postPhotoRepositoryMock = new HibernatePostPhotoRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any());
        PostPhoto postPhoto = PostPhoto.builder()
                .post(testPosts.get(0))
                .photo(testFiles.get(0))
                .sort(1000)
                .build();

        PostPhoto actualPostPhoto = postPhotoRepositoryMock.create(postPhoto);

        PostPhoto expected = PostPhoto.builder()
                .post(testPosts.get(0))
                .photo(testFiles.get(0))
                .sort(1000)
                .build();
        assertThat(actualPostPhoto).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void whenFindByIdThenReturnOptionalWithPostPhoto() {
        PostPhoto postPhoto = PostPhoto.builder()
                .post(testPosts.get(0))
                .photo(testFiles.get(0))
                .sort(1000)
                .build();
        postPhoto = postPhotoRepository.create(postPhoto);

        Optional<PostPhoto> actualPostPhoto = postPhotoRepository.findById(postPhoto.getId());

        assertThat(actualPostPhoto).isPresent();
        PostPhoto expected = PostPhoto.builder()
                .id(actualPostPhoto.get().getId())
                .sort(1000)
                .post(testPosts.get(0))
                .photo(testFiles.get(0))
                .build();
        assertThat(copyPostPhoto(actualPostPhoto.get())).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void whenFindByIdWrongIdThenReturnOptionalEmpty() {
        PostPhoto postPhoto = PostPhoto.builder()
                .post(testPosts.get(0))
                .photo(testFiles.get(0))
                .sort(1000)
                .build();
        postPhoto = postPhotoRepository.create(postPhoto);

        Optional<PostPhoto> actualPostPhoto = postPhotoRepository.findById(postPhoto.getId() + 1000);

        assertThat(actualPostPhoto).isEmpty();
    }

    @Test
    void whenFindByIdThrowExceptionThenReturnOptionalEmpty() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        PostPhotoRepository postPhotoRepositoryMock = new HibernatePostPhotoRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).optional(any(), any(), any());

        Optional<PostPhoto> actualPostPhoto = postPhotoRepositoryMock.findById(1000);

        assertThat(actualPostPhoto).isEmpty();
    }

    @Test
    void whenFindAllThenReturnCollectionWithPostPhoto() {
        List<PostPhoto> postPhotos = IntStream.rangeClosed(0, 2).mapToObj(
                value -> postPhotoRepository.create(
                        PostPhoto.builder()
                                .post(testPosts.get(value % testPosts.size()))
                                .photo(testFiles.get((value + 1) % testFiles.size()))
                                .sort(1000)
                                .build()
                )
        ).toList();

        Collection<PostPhoto> actualPostPhotos = postPhotoRepository.findAll();

        assertThat(actualPostPhotos).hasSize(3);
        Collection<PostPhoto> expectedPostPhotos = IntStream.rangeClosed(0, 2).mapToObj(
                value -> PostPhoto.builder()
                        .id(postPhotos.get(value).getId())
                        .post(testPosts.get(value % testPosts.size()))
                        .photo(testFiles.get((value + 1) % testFiles.size()))
                        .sort(1000)
                        .build()
        ).toList();
        assertThat(actualPostPhotos.stream().map(HibernatePostPhotoRepositoryTest::copyPostPhoto))
                .usingRecursiveComparison().isEqualTo(expectedPostPhotos);
    }

    @Test
    void whenFindAllThrowExceptionThenReturnCollectionEmpty() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        PostPhotoRepository postPhotoRepositoryMock = new HibernatePostPhotoRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).query(any(), any());

        Collection<PostPhoto> actualPostPhotos = postPhotoRepositoryMock.findAll();

        assertThat(actualPostPhotos).isEmpty();
    }

    @Test
    void whenDeleteThenDeletedInDb() {
        List<PostPhoto> postPhotos = IntStream.rangeClosed(0, 2).mapToObj(
                value -> postPhotoRepository.create(
                        PostPhoto.builder()
                                .post(testPosts.get(value % testPosts.size()))
                                .photo(testFiles.get((value + 1) % testFiles.size()))
                                .sort(1000)
                                .build()
                )
        ).toList();

        postPhotoRepository.delete(postPhotos.get(1).getId());
        Collection<PostPhoto> actualPostPhotos = postPhotoRepository.findAll();

        assertThat(actualPostPhotos).hasSize(2);
        Collection<PostPhoto> expectedPostPhotos = IntStream.of(0, 2).mapToObj(
                value -> PostPhoto.builder()
                        .id(postPhotos.get(value).getId())
                        .post(testPosts.get(value % testPosts.size()))
                        .photo(testFiles.get((value + 1) % testFiles.size()))
                        .sort(1000)
                        .build()
        ).toList();
        assertThat(actualPostPhotos.stream().map(HibernatePostPhotoRepositoryTest::copyPostPhoto))
                .usingRecursiveComparison().isEqualTo(expectedPostPhotos);
    }

    @Test
    void whenDeleteWrongIdThenNotDeletedInDb() {
        List<PostPhoto> postPhotos = IntStream.rangeClosed(0, 2).mapToObj(
                value -> postPhotoRepository.create(
                        PostPhoto.builder()
                                .post(testPosts.get(value % testPosts.size()))
                                .photo(testFiles.get((value + 1) % testFiles.size()))
                                .sort(1000)
                                .build()
                )
        ).toList();

        postPhotoRepository.delete(postPhotos.get(2).getId() + 1000);
        Collection<PostPhoto> actualPostPhotos = postPhotoRepository.findAll();

        assertThat(actualPostPhotos).hasSize(3);
        Collection<PostPhoto> expectedPostPhotos = IntStream.rangeClosed(0, 2).mapToObj(
                value -> PostPhoto.builder()
                        .id(postPhotos.get(value).getId())
                        .post(testPosts.get(value % testPosts.size()))
                        .photo(testFiles.get((value + 1) % testFiles.size()))
                        .sort(1000)
                        .build()
        ).toList();
        assertThat(actualPostPhotos.stream().map(HibernatePostPhotoRepositoryTest::copyPostPhoto))
                .usingRecursiveComparison().isEqualTo(expectedPostPhotos);
    }

    @Test
    void whenDeleteThrowExceptionThenNotException() {
        CrudRepository crudRepositoryMock = mock(CrudRepository.class);
        PostPhotoRepository postPhotoRepositoryMock = new HibernatePostPhotoRepository(crudRepositoryMock);
        doThrow(RuntimeException.class).when(crudRepositoryMock).run(any(), any());

        postPhotoRepositoryMock.delete(1);

        verify(crudRepositoryMock, times(1)).run(any(), any());
    }
}