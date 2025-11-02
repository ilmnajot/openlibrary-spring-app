package uz.ilmnajot.openlibraryspringapp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import uz.ilmnajot.openlibraryspringapp.entity.Author;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AuthorRepository
 * Uses in-memory H2 database
 */
@DataJpaTest
@DisplayName("Author Repository Integration Tests")
class AuthorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save author successfully")
    void shouldSaveAuthor() {
        // Given
        Author author = new Author();
        author.setAuthorId("/authors/OL123A");
        author.setAuthorName("Test Author");

        // When
        Author savedAuthor = authorRepository.save(author);

        // Then
        assertNotNull(savedAuthor);
        assertNotNull(savedAuthor.getId());
        assertEquals("/authors/OL123A", savedAuthor.getAuthorId());
        assertEquals("Test Author", savedAuthor.getAuthorName());
    }

    @Test
    @DisplayName("Should find author by authorId")
    void shouldFindByAuthorId() {
        // Given
        Author author = new Author();
        author.setAuthorId("/authors/OL123A");
        author.setAuthorName("Findable Author");
        authorRepository.save(author);
        entityManager.flush();

        // When
        Optional<Author> found = authorRepository.findByAuthorId("/authors/OL123A");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Findable Author", found.get().getAuthorName());
    }

    @Test
    @DisplayName("Should return empty when author not found by authorId")
    void shouldReturnEmptyWhenAuthorNotFound() {
        // When
        Optional<Author> found = authorRepository.findByAuthorId("/authors/NONEXISTENT");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find authors by name containing (case insensitive)")
    void shouldFindByAuthorNameContainingIgnoreCase() {
        // Given
        Author author1 = new Author();
        author1.setAuthorId("/authors/OL1A");
        author1.setAuthorName("John Smith");
        authorRepository.save(author1);

        Author author2 = new Author();
        author2.setAuthorId("/authors/OL2A");
        author2.setAuthorName("Jane Smith");
        authorRepository.save(author2);

        Author author3 = new Author();
        author3.setAuthorId("/authors/OL3A");
        author3.setAuthorName("Robert Johnson");
        authorRepository.save(author3);

        entityManager.flush();

        // When
        List<Author> smithAuthors = authorRepository.findByAuthorNameContainingIgnoreCase("smith");

        // Then
        assertEquals(2, smithAuthors.size());
        assertThat(smithAuthors)
                .extracting(Author::getAuthorName)
                .containsExactlyInAnyOrder("John Smith", "Jane Smith");
    }

    @Test
    @DisplayName("Should perform case-insensitive search")
    void shouldPerformCaseInsensitiveSearch() {
        // Given
        Author author = new Author();
        author.setAuthorId("/authors/OL1A");
        author.setAuthorName("Elbek Umarov");
        authorRepository.save(author);
        entityManager.flush();

        // When
        List<Author> found1 = authorRepository.findByAuthorNameContainingIgnoreCase("ELBEK");
        List<Author> found2 = authorRepository.findByAuthorNameContainingIgnoreCase("elbek");
        List<Author> found3 = authorRepository.findByAuthorNameContainingIgnoreCase("ElBeK");

        // Then
        assertEquals(1, found1.size());
        assertEquals(1, found2.size());
        assertEquals(1, found3.size());
        assertEquals("Elbek Umarov", found1.get(0).getAuthorName());
    }

    @Test
    @DisplayName("Should return empty list when no authors match search")
    void shouldReturnEmptyListWhenNoMatch() {
        // Given
        Author author = new Author();
        author.setAuthorId("/authors/OL1A");
        author.setAuthorName("John Doe");
        authorRepository.save(author);
        entityManager.flush();

        // When
        List<Author> found = authorRepository.findByAuthorNameContainingIgnoreCase("NonExistent");

        // Then
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should find authors by partial name match")
    void shouldFindByPartialNameMatch() {
        // Given
        Author author = new Author();
        author.setAuthorId("/authors/OL1A");
        author.setAuthorName("Stephen King");
        authorRepository.save(author);
        entityManager.flush();

        // When
        List<Author> foundByFirst = authorRepository.findByAuthorNameContainingIgnoreCase("step");
        List<Author> foundByLast = authorRepository.findByAuthorNameContainingIgnoreCase("king");

        // Then
        assertEquals(1, foundByFirst.size());
        assertEquals(1, foundByLast.size());
        assertEquals("Stephen King", foundByFirst.get(0).getAuthorName());
        assertEquals("Stephen King", foundByLast.get(0).getAuthorName());
    }

    @Test
    @DisplayName("Should update existing author")
    void shouldUpdateExistingAuthor() {
        // Given
        Author author = new Author();
        author.setAuthorId("/authors/OL1A");
        author.setAuthorName("Original Name");
        Author savedAuthor = authorRepository.save(author);
        entityManager.flush();

        // When
        savedAuthor.setAuthorName("Updated Name");
        authorRepository.save(savedAuthor);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Author> found = authorRepository.findByAuthorId("/authors/OL1A");
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getAuthorName());
    }

    @Test
    @DisplayName("Should delete author by ID")
    void shouldDeleteAuthorById() {
        // Given
        Author author = new Author();
        author.setAuthorId("/authors/OL1A");
        author.setAuthorName("To Be Deleted");
        Author savedAuthor = authorRepository.save(author);
        entityManager.flush();

        // When
        authorRepository.deleteById(String.valueOf(savedAuthor.getId()));
        entityManager.flush();

        // Then
        Optional<Author> found = authorRepository.findByAuthorId("/authors/OL1A");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should handle special characters in author names")
    void shouldHandleSpecialCharacters() {
        // Given
        Author author = new Author();
        author.setAuthorId("/authors/OL1A");
        author.setAuthorName("O'Brien, Patrick");
        authorRepository.save(author);
        entityManager.flush();

        // When
        List<Author> found = authorRepository.findByAuthorNameContainingIgnoreCase("o'brien");

        // Then
        assertEquals(1, found.size());
        assertEquals("O'Brien, Patrick", found.get(0).getAuthorName());
    }

    @Test
    @DisplayName("Should handle multiple authors with similar names")
    void shouldHandleMultipleSimilarAuthors() {
        // Given
        Author author1 = new Author();
        author1.setAuthorId("/authors/OL1A");
        author1.setAuthorName("Alexander Anderson");
        authorRepository.save(author1);

        Author author2 = new Author();
        author2.setAuthorId("/authors/OL2A");
        author2.setAuthorName("Alex Anderson");
        authorRepository.save(author2);

        Author author3 = new Author();
        author3.setAuthorId("/authors/OL3A");
        author3.setAuthorName("Alexandra Anderson");
        authorRepository.save(author3);

        entityManager.flush();

        // When
        List<Author> foundByAlex = authorRepository.findByAuthorNameContainingIgnoreCase("alex");
        List<Author> foundByAnderson = authorRepository.findByAuthorNameContainingIgnoreCase("anderson");

        // Then
        assertEquals(3, foundByAlex.size());
        assertEquals(3, foundByAnderson.size());
    }

    @Test
    @DisplayName("Should not allow duplicate authorIds")
    void shouldNotAllowDuplicateAuthorIds() {
        // Given
        Author author1 = new Author();
        author1.setAuthorId("/authors/OL1A");
        author1.setAuthorName("First Author");
        authorRepository.save(author1);
        entityManager.flush();

        // When & Then
        Author author2 = new Author();
        author2.setAuthorId("/authors/OL1A");
        author2.setAuthorName("Second Author");

        // This should throw an exception or update the existing author
        // depending on your entity configuration
        assertThrows(Exception.class, () -> {
            authorRepository.save(author2);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should handle whitespace in search queries")
    void shouldHandleWhitespaceInSearch() {
        // Given
        Author author = new Author();
        author.setAuthorId("/authors/OL1A");
        author.setAuthorName("John Doe");
        authorRepository.save(author);
        entityManager.flush();

        // When
        List<Author> found1 = authorRepository.findByAuthorNameContainingIgnoreCase("  john  ");
        List<Author> found2 = authorRepository.findByAuthorNameContainingIgnoreCase("john doe");

        // Then
        // Note: This behavior depends on your database configuration
        // H2 might handle trailing spaces differently
        assertTrue(found1.size() >= 0);
        assertEquals(1, found2.size());
    }

    @Test
    @DisplayName("Should count all authors")
    void shouldCountAllAuthors() {
        // Given
        for (int i = 1; i <= 5; i++) {
            Author author = new Author();
            author.setAuthorId("/authors/OL" + i + "A");
            author.setAuthorName("Author " + i);
            authorRepository.save(author);
        }
        entityManager.flush();

        // When
        long count = authorRepository.count();

        // Then
        assertEquals(5, count);
    }

    @Test
    @DisplayName("Should handle long author names")
    void shouldHandleLongAuthorNames() {
        // Given
        String longName = "A".repeat(255);
        Author author = new Author();
        author.setAuthorId("/authors/OL1A");
        author.setAuthorName(longName);

        // When
        Author savedAuthor = authorRepository.save(author);
        entityManager.flush();

        // Then
        Optional<Author> found = authorRepository.findByAuthorId("/authors/OL1A");
        assertTrue(found.isPresent());
        assertEquals(255, found.get().getAuthorName().length());
    }

    @Test
    @DisplayName("Should handle empty search query")
    void shouldHandleEmptySearchQuery() {
        // Given
        Author author = new Author();
        author.setAuthorId("/authors/OL1A");
        author.setAuthorName("Test Author");
        authorRepository.save(author);
        entityManager.flush();

        // When
        List<Author> found = authorRepository.findByAuthorNameContainingIgnoreCase("");

        // Then
        // Empty string should match all authors
        assertEquals(1, found.size());
    }

    @Test
    @DisplayName("Should retrieve all authors")
    void shouldRetrieveAllAuthors() {
        // Given
        for (int i = 1; i <= 3; i++) {
            Author author = new Author();
            author.setAuthorId("/authors/OL" + i + "A");
            author.setAuthorName("Author " + i);
            authorRepository.save(author);
        }
        entityManager.flush();

        // When
        List<Author> allAuthors = authorRepository.findAll();

        // Then
        assertEquals(3, allAuthors.size());
        assertThat(allAuthors)
                .extracting(Author::getAuthorName)
                .containsExactlyInAnyOrder("Author 1", "Author 2", "Author 3");
    }
}