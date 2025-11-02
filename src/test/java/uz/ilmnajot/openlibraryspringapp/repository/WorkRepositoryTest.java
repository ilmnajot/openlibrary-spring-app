package uz.ilmnajot.openlibraryspringapp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import uz.ilmnajot.openlibraryspringapp.entity.Author;
import uz.ilmnajot.openlibraryspringapp.entity.Work;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for WorkRepository
 * Uses in-memory H2 database
 */
@DataJpaTest
@DisplayName("Work Repository Integration Tests")
class WorkRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Author testAuthor1;
    private Author testAuthor2;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        workRepository.deleteAll();
        authorRepository.deleteAll();

        // Create test authors
        testAuthor1 = new Author();
        testAuthor1.setAuthorId("/authors/OL123A");
        testAuthor1.setAuthorName("Test Author 1");
        testAuthor1 = authorRepository.save(testAuthor1);

        testAuthor2 = new Author();
        testAuthor2.setAuthorId("/authors/OL456A");
        testAuthor2.setAuthorName("Test Author 2");
        testAuthor2 = authorRepository.save(testAuthor2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should save work successfully")
    void shouldSaveWork() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Test Work");
        work.setDescription("Test Description");
        work.getAuthors().add(testAuthor1);

        // When
        Work savedWork = workRepository.save(work);

        // Then
        assertNotNull(savedWork);
        assertNotNull(savedWork.getId());
        assertEquals("/works/OL1W", savedWork.getWorkId());
        assertEquals("Test Work", savedWork.getTitle());
        assertEquals("Test Description", savedWork.getDescription());
        assertEquals(1, savedWork.getAuthors().size());
    }

    @Test
    @DisplayName("Should find work by workId")
    void shouldFindByWorkId() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Findable Work");
        work.getAuthors().add(testAuthor1);
        workRepository.save(work);
        entityManager.flush();

        // When
        Optional<Work> found = workRepository.findByWorkId("/works/OL1W");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Findable Work", found.get().getTitle());
        assertEquals("/works/OL1W", found.get().getWorkId());
    }

    @Test
    @DisplayName("Should return empty when work not found by workId")
    void shouldReturnEmptyWhenWorkNotFound() {
        // When
        Optional<Work> found = workRepository.findByWorkId("/works/NONEXISTENT");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find all works by author ID")
    void shouldFindAllByAuthorId() {
        // Given
        Work work1 = new Work();
        work1.setWorkId("/works/OL1W");
        work1.setTitle("Work 1");
        work1.getAuthors().add(testAuthor1);
        workRepository.save(work1);

        Work work2 = new Work();
        work2.setWorkId("/works/OL2W");
        work2.setTitle("Work 2");
        work2.getAuthors().add(testAuthor1);
        workRepository.save(work2);

        Work work3 = new Work();
        work3.setWorkId("/works/OL3W");
        work3.setTitle("Work 3 by different author");
        work3.getAuthors().add(testAuthor2);
        workRepository.save(work3);

        entityManager.flush();

        // When
        List<Work> works = workRepository.findAllByAuthors_AuthorId("/authors/OL123A");

        // Then
        assertEquals(2, works.size());
        assertThat(works)
                .extracting(Work::getTitle)
                .containsExactlyInAnyOrder("Work 1", "Work 2");
    }

    @Test
    @DisplayName("Should return empty list when no works found for author")
    void shouldReturnEmptyListWhenNoWorksForAuthor() {
        // When
        List<Work> works = workRepository.findAllByAuthors_AuthorId("/authors/NONEXISTENT");

        // Then
        assertTrue(works.isEmpty());
    }

    @Test
    @DisplayName("Should handle work with multiple authors")
    void shouldHandleWorkWithMultipleAuthors() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Collaborative Work");
        work.getAuthors().add(testAuthor1);
        work.getAuthors().add(testAuthor2);
        workRepository.save(work);
        entityManager.flush();

        // When
        List<Work> worksAuthor1 = workRepository.findAllByAuthors_AuthorId("/authors/OL123A");
        List<Work> worksAuthor2 = workRepository.findAllByAuthors_AuthorId("/authors/OL456A");

        // Then
        assertEquals(1, worksAuthor1.size());
        assertEquals(1, worksAuthor2.size());
        assertEquals("Collaborative Work", worksAuthor1.get(0).getTitle());
        assertEquals("Collaborative Work", worksAuthor2.get(0).getTitle());
        assertEquals(2, worksAuthor1.get(0).getAuthors().size());
    }

    @Test
    @DisplayName("Should save work with subjects")
    void shouldSaveWorkWithSubjects() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Work with Subjects");
        work.setSubjects(Arrays.asList("Fiction", "Adventure", "Science Fiction"));
        work.getAuthors().add(testAuthor1);

        // When
        Work savedWork = workRepository.save(work);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Work> found = workRepository.findByWorkId("/works/OL1W");
        assertTrue(found.isPresent());
        assertEquals(3, found.get().getSubjects().size());
        assertThat(found.get().getSubjects())
                .containsExactly("Fiction", "Adventure", "Science Fiction");
    }

    @Test
    @DisplayName("Should save work with covers")
    void shouldSaveWorkWithCovers() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Work with Covers");
        work.setCovers(Arrays.asList(12345L, 67890L, 11111L));
        work.getAuthors().add(testAuthor1);

        // When
        Work savedWork = workRepository.save(work);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Work> found = workRepository.findByWorkId("/works/OL1W");
        assertTrue(found.isPresent());
        assertEquals(3, found.get().getCovers().size());
        assertThat(found.get().getCovers())
                .containsExactly(12345L, 67890L, 11111L);
    }

    @Test
    @DisplayName("Should save work with null description")
    void shouldSaveWorkWithNullDescription() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Work Without Description");
        work.setDescription(null);
        work.getAuthors().add(testAuthor1);

        // When
        Work savedWork = workRepository.save(work);

        // Then
        assertNotNull(savedWork);
        assertNull(savedWork.getDescription());
    }

    @Test
    @DisplayName("Should update existing work")
    void shouldUpdateExistingWork() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Original Title");
        work.setDescription("Original Description");
        work.getAuthors().add(testAuthor1);
        Work savedWork = workRepository.save(work);
        entityManager.flush();

        // When
        savedWork.setTitle("Updated Title");
        savedWork.setDescription("Updated Description");
        Work updatedWork = workRepository.save(savedWork);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Work> found = workRepository.findByWorkId("/works/OL1W");
        assertTrue(found.isPresent());
        assertEquals("Updated Title", found.get().getTitle());
        assertEquals("Updated Description", found.get().getDescription());
    }

    @Test
    @DisplayName("Should delete work by ID")
    void shouldDeleteWorkById() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Work to Delete");
        work.getAuthors().add(testAuthor1);
        Work savedWork = workRepository.save(work);
        entityManager.flush();

        // When
        workRepository.deleteById(savedWork.getId());
        entityManager.flush();

        // Then
        Optional<Work> found = workRepository.findByWorkId("/works/OL1W");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should handle cascade operations with authors")
    void shouldHandleCascadeWithAuthors() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Work with Author");
        work.getAuthors().add(testAuthor1);
        workRepository.save(work);
        entityManager.flush();

        // When - Delete work
        workRepository.deleteAll();
        entityManager.flush();

        // Then - Author should still exist
        Optional<Author> author = authorRepository.findByAuthorId("/authors/OL123A");
        assertTrue(author.isPresent(), "Author should not be deleted when work is deleted");
    }

    @Test
    @DisplayName("Should find works with empty subjects list")
    void shouldFindWorksWithEmptySubjects() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Work Without Subjects");
        work.getAuthors().add(testAuthor1);
        workRepository.save(work);
        entityManager.flush();

        // When
        Optional<Work> found = workRepository.findByWorkId("/works/OL1W");

        // Then
        assertTrue(found.isPresent());
        assertNotNull(found.get().getSubjects());
        assertTrue(found.get().getSubjects().isEmpty());
    }

    @Test
    @DisplayName("Should count works by author")
    void shouldCountWorksByAuthor() {
        // Given
        for (int i = 1; i <= 5; i++) {
            Work work = new Work();
            work.setWorkId("/works/OL" + i + "W");
            work.setTitle("Work " + i);
            work.getAuthors().add(testAuthor1);
            workRepository.save(work);
        }
        entityManager.flush();

        // When
        List<Work> works = workRepository.findAllByAuthors_AuthorId("/authors/OL123A");

        // Then
        assertEquals(5, works.size());
    }

    @Test
    @DisplayName("Should handle long descriptions")
    void shouldHandleLongDescriptions() {
        // Given
        String longDescription = "A".repeat(1000);
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Work with Long Description");
        work.setDescription(longDescription);
        work.getAuthors().add(testAuthor1);

        // When
        Work savedWork = workRepository.save(work);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Work> found = workRepository.findByWorkId("/works/OL1W");
        assertTrue(found.isPresent());
        assertEquals(1000, found.get().getDescription().length());
    }

    @Test
    @DisplayName("Should handle special characters in titles")
    void shouldHandleSpecialCharactersInTitles() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Work: The Story of O'Brien & Friends (2024)");
        work.getAuthors().add(testAuthor1);

        // When
        Work savedWork = workRepository.save(work);
        entityManager.flush();

        // Then
        Optional<Work> found = workRepository.findByWorkId("/works/OL1W");
        assertTrue(found.isPresent());
        assertEquals("Work: The Story of O'Brien & Friends (2024)", found.get().getTitle());
    }

    @Test
    @DisplayName("Should maintain order of subjects")
    void shouldMaintainOrderOfSubjects() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL1W");
        work.setTitle("Ordered Work");
        work.setSubjects(Arrays.asList("First", "Second", "Third"));
        work.getAuthors().add(testAuthor1);

        // When
        workRepository.save(work);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Work> found = workRepository.findByWorkId("/works/OL1W");
        assertTrue(found.isPresent());
        assertThat(found.get().getSubjects())
                .containsExactly("First", "Second", "Third");
    }
}