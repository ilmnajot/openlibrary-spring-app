package uz.ilmnajot.openlibraryspringapp.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uz.ilmnajot.openlibraryspringapp.entity.Author;

import uz.ilmnajot.openlibraryspringapp.model.AuthorResponse;
import uz.ilmnajot.openlibraryspringapp.model.OpenLibraryAuthorDoc;
import uz.ilmnajot.openlibraryspringapp.model.OpenLibrarySearchResponse;
import uz.ilmnajot.openlibraryspringapp.repository.AuthorRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthorServiceImpl
 * Uses pure mocking without Spring context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Author Service Unit Tests")
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthorServiceImpl authorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authorService, "baseUrl", "https://openlibrary.org");
    }

    @Test
    @DisplayName("Should return authors from database when found")
    void shouldReturnAuthorsFromDBWhenExists() {

        String searchName = "Elbek";
        Author author = new Author();
        author.setAuthorId("/authors/A1");
        author.setAuthorName("Elbek Umarov");
        when(authorRepository.findByAuthorNameContainingIgnoreCase(searchName))
                .thenReturn(List.of(author));

        // When
        List<AuthorResponse> result = authorService.searchAuthor(searchName);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should return 1 author");
        assertEquals("Elbek Umarov", result.get(0).getAuthorName());
        assertEquals("/authors/A1", result.get(0).getAuthorId());

        // Verify interactions
        verify(authorRepository, times(1))
                .findByAuthorNameContainingIgnoreCase(searchName);
        verify(restTemplate, never()).getForObject(anyString(), any());
        verify(authorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fetch from API when database is empty")
    void shouldFetchFromApiWhenDBIsEmpty() {
        // Given
        String searchName = "Jack";

        // Mock repository to return empty list
        when(authorRepository.findByAuthorNameContainingIgnoreCase(searchName))
                .thenReturn(Collections.emptyList());

        // Mock API response
        OpenLibraryAuthorDoc doc = new OpenLibraryAuthorDoc();
        doc.setKey("/authors/OL123A");
        doc.setName("Jack Anderson");

        OpenLibrarySearchResponse apiResponse = new OpenLibrarySearchResponse();
        apiResponse.setNumFound(1);
        apiResponse.setDocs(List.of(doc));

        when(restTemplate.getForObject(anyString(), eq(OpenLibrarySearchResponse.class)))
                .thenReturn(apiResponse);

        // Mock save to return the same author
        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<AuthorResponse> result = authorService.searchAuthor(searchName);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jack Anderson", result.get(0).getAuthorName());
        assertEquals("/authors/OL123A", result.get(0).getAuthorId());

        // Verify
        verify(authorRepository, times(1))
                .findByAuthorNameContainingIgnoreCase(searchName);
        verify(restTemplate, times(1))
                .getForObject(anyString(), eq(OpenLibrarySearchResponse.class));
        verify(authorRepository, times(1)).save(any(Author.class));
    }

    @Test
    @DisplayName("Should return empty list when API returns no results")
    void shouldReturnEmptyListWhenApiReturnsNoResults() {
        // Given
        String searchName = "NonExistentAuthor";

        when(authorRepository.findByAuthorNameContainingIgnoreCase(searchName))
                .thenReturn(Collections.emptyList());

        OpenLibrarySearchResponse emptyResponse = new OpenLibrarySearchResponse();
        emptyResponse.setNumFound(0);
        emptyResponse.setDocs(Collections.emptyList());

        when(restTemplate.getForObject(anyString(), eq(OpenLibrarySearchResponse.class)))
                .thenReturn(emptyResponse);

        // When
        List<AuthorResponse> result = authorService.searchAuthor(searchName);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Result should be empty");
        verify(authorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle API failure gracefully")
    void shouldThrowExceptionWhenApiFails() {
        // Given
        String searchName = "UnknownAuthor";

        when(authorRepository.findByAuthorNameContainingIgnoreCase(searchName))
                .thenReturn(Collections.emptyList());

        when(restTemplate.getForObject(anyString(), eq(OpenLibrarySearchResponse.class)))
                .thenThrow(new RuntimeException("API connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authorService.searchAuthor(searchName),
                "Should throw RuntimeException when API fails");

        assertTrue(exception.getMessage().contains("Failed to search author") ||
                exception.getMessage().contains("API connection failed"));

        verify(authorRepository, times(1))
                .findByAuthorNameContainingIgnoreCase(searchName);
        verify(restTemplate, times(1))
                .getForObject(anyString(), eq(OpenLibrarySearchResponse.class));
        verify(authorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle multiple authors from API")
    void shouldHandleMultipleAuthorsFromApi() {
        // Given
        String searchName = "Smith";

        when(authorRepository.findByAuthorNameContainingIgnoreCase(searchName))
                .thenReturn(Collections.emptyList());

        OpenLibraryAuthorDoc doc1 = new OpenLibraryAuthorDoc();
        doc1.setKey("/authors/OL1A");
        doc1.setName("John Smith");

        OpenLibraryAuthorDoc doc2 = new OpenLibraryAuthorDoc();
        doc2.setKey("/authors/OL2A");
        doc2.setName("Jane Smith");

        OpenLibrarySearchResponse response = new OpenLibrarySearchResponse();
        response.setNumFound(2);
        response.setDocs(List.of(doc1, doc2));

        when(restTemplate.getForObject(anyString(), eq(OpenLibrarySearchResponse.class)))
                .thenReturn(response);

        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<AuthorResponse> result = authorService.searchAuthor(searchName);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size(), "Should return 2 authors");
        verify(authorRepository, times(2)).save(any(Author.class));
    }

    @Test
    @DisplayName("Should handle null API response")
    void shouldHandleNullApiResponse() {
        // Given
        String searchName = "Test";

        when(authorRepository.findByAuthorNameContainingIgnoreCase(searchName))
                .thenReturn(Collections.emptyList());

        when(restTemplate.getForObject(anyString(), eq(OpenLibrarySearchResponse.class)))
                .thenReturn(null);

        // When
        List<AuthorResponse> result = authorService.searchAuthor(searchName);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(authorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should perform case-insensitive search")
    void shouldPerformCaseInsensitiveSearch() {
        // Given
        Author author = new Author();
        author.setAuthorId("/authors/A1");
        author.setAuthorName("Elbek Umarov");

        // Mock for different cases
        when(authorRepository.findByAuthorNameContainingIgnoreCase("ELBEK"))
                .thenReturn(List.of(author));
        when(authorRepository.findByAuthorNameContainingIgnoreCase("elbek"))
                .thenReturn(List.of(author));
        when(authorRepository.findByAuthorNameContainingIgnoreCase("ElBeK"))
                .thenReturn(List.of(author));

        // When
        List<AuthorResponse> result1 = authorService.searchAuthor("ELBEK");
        List<AuthorResponse> result2 = authorService.searchAuthor("elbek");
        List<AuthorResponse> result3 = authorService.searchAuthor("ElBeK");

        // Then
        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(1, result3.size());
        assertEquals("Elbek Umarov", result1.get(0).getAuthorName());
        assertEquals("Elbek Umarov", result2.get(0).getAuthorName());
        assertEquals("Elbek Umarov", result3.get(0).getAuthorName());
    }
}