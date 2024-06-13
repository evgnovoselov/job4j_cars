package ru.job4j.cars.repository.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.job4j.cars.config.SessionFactoryConfig;
import ru.job4j.cars.model.File;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HibernateFileRepositoryTest {
    private static SessionFactory sessionFactory;
    private static HibernateFileRepository fileRepository;

    @BeforeAll
    static void beforeAll() {
        sessionFactory = new SessionFactoryConfig().createSessionFactory();
        CrudRepository crudRepository = new CrudRepository(sessionFactory);
        fileRepository = new HibernateFileRepository(crudRepository);
    }

    @BeforeEach
    void setUp() {
        fileRepository.findAll().stream().map(File::getId).forEach(fileRepository::delete);
    }

    @AfterAll
    static void afterAll() {
        sessionFactory.close();
    }

    @Test
    void whenCreateFileThenCreatedInDbAndHaveId() {
        File file = File.builder()
                .name("fileName")
                .path("path/file")
                .build();

        File actualFile = fileRepository.create(file);
        Collection<File> actualFiles = fileRepository.findAll();

        assertThat(actualFile.getId()).isNotZero();
        File expected = new File(actualFile.getId(), "fileName", "path/file");
        assertThat(actualFile).usingRecursiveComparison().isEqualTo(
                expected
        );
        assertThat(actualFiles).hasSize(1);
        assertThat(actualFiles).usingRecursiveComparison().isEqualTo(List.of(expected));
    }

    @Test
    void whenErrorCreateFileThenReturnFileWithoutId() {
        CrudRepository mockCrudRepository = mock(CrudRepository.class);
        HibernateFileRepository hibernateFileRepository = new HibernateFileRepository(mockCrudRepository);
        File file = File.builder()
                .name("fileName")
                .path("path/file")
                .build();
        doThrow(RuntimeException.class).when(mockCrudRepository).run(any());

        File actualFile = hibernateFileRepository.create(file);

        File expected = File.builder()
                .name("fileName")
                .path("path/file")
                .build();
        assertThat(actualFile).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void whenFindByIdThenReturnOptionalWithFile() {
        File file = File.builder()
                .name("fileName").path("path/file")
                .build();
        file = fileRepository.create(file);

        Optional<File> actualFile = fileRepository.findById(file.getId());

        assertThat(actualFile).isPresent();
        File expected = new File(file.getId(), "fileName", "path/file");
        assertThat(actualFile.get()).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void whenFindByIdWrongIdThenReturnOptionalEmpty() {
        File file = File.builder()
                .name("fileName").path("path/file")
                .build();
        file = fileRepository.create(file);

        Optional<File> actualFile = fileRepository.findById(file.getId() + 1);
        Collection<File> actualFiles = fileRepository.findAll();

        assertThat(actualFile).isEmpty();
        assertThat(actualFiles).hasSize(1);
    }

    @Test
    void whenFindByIdThrowExceptionThenReturnOptionalEmpty() {
        CrudRepository mockCrudRepository = mock(CrudRepository.class);
        HibernateFileRepository hibernateFileRepository = new HibernateFileRepository(mockCrudRepository);
        doThrow(RuntimeException.class).when(mockCrudRepository).optional(any(), any(), any());

        Optional<File> actualFile = hibernateFileRepository.findById(1);

        assertThat(actualFile).isEmpty();
    }

    @Test
    void whenFindAllThenReturnCollectionFiles() {
        List<File> files = IntStream.rangeClosed(0, 2).mapToObj(
                value -> fileRepository.create(File.builder()
                        .name("fileName-" + value)
                        .path("path/file-" + value)
                        .build())
        ).toList();

        Collection<File> actualFiles = fileRepository.findAll();

        assertThat(actualFiles).hasSize(3);
        Collection<File> expectedFiles = IntStream.rangeClosed(0, 2).mapToObj(
                value -> new File(files.get(value).getId(), "fileName-" + value, "path/file-" + value)
        ).toList();
        assertThat(actualFiles).usingRecursiveComparison().isEqualTo(expectedFiles);
    }

    @Test
    void whenFindAllThrowExceptionThenReturnEmptyCollection() {
        CrudRepository mockCrudRepository = mock(CrudRepository.class);
        HibernateFileRepository hibernateFileRepository = new HibernateFileRepository(mockCrudRepository);
        doThrow(RuntimeException.class).when(mockCrudRepository).query(any(), any());

        Collection<File> actualFiles = hibernateFileRepository.findAll();

        assertThat(actualFiles).isEmpty();
    }

    @Test
    void whenDeleteFileThenDeletedInDb() {
        List<File> files = IntStream.rangeClosed(0, 2).mapToObj(
                value -> fileRepository.create(File.builder()
                        .name("fileName-" + value)
                        .path("path/file-" + value)
                        .build())
        ).toList();

        fileRepository.delete(files.get(1).getId());
        Collection<File> actualFiles = fileRepository.findAll();

        assertThat(actualFiles).hasSize(2);
        Collection<File> expectedFiles = IntStream.of(0, 2).mapToObj(
                value -> new File(files.get(value).getId(), "fileName-" + value, "path/file-" + value)
        ).toList();
        assertThat(actualFiles).usingRecursiveComparison().isEqualTo(expectedFiles);
    }

    @Test
    void whenDeleteFileWrongIdThenFileNotDeletedInDb() {
        List<File> files = IntStream.rangeClosed(0, 2).mapToObj(
                value -> fileRepository.create(File.builder()
                        .name("fileName-" + value)
                        .path("path/file-" + value)
                        .build())
        ).toList();

        fileRepository.delete(files.get(1).getId() + 1000);
        Collection<File> actualFiles = fileRepository.findAll();

        assertThat(actualFiles).hasSize(3);
        Collection<File> expectedFiles = IntStream.rangeClosed(0, 2).mapToObj(
                value -> new File(files.get(value).getId(), "fileName-" + value, "path/file-" + value)
        ).toList();
        assertThat(actualFiles).usingRecursiveComparison().isEqualTo(expectedFiles);
    }

    @Test
    void whenDeleteFileThrowExceptionThenNotHaveException() {
        CrudRepository mockCrudRepository = mock(CrudRepository.class);
        HibernateFileRepository hibernateFileRepository = new HibernateFileRepository(mockCrudRepository);
        doThrow(RuntimeException.class).when(mockCrudRepository).run(any(), any());

        hibernateFileRepository.delete(1000);

        verify(mockCrudRepository, times(1)).run(any(), any());
    }
}