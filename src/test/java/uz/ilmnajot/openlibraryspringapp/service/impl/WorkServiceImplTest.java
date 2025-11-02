package uz.ilmnajot.openlibraryspringapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uz.ilmnajot.openlibraryspringapp.entity.Work;
import uz.ilmnajot.openlibraryspringapp.mapper.WorkMapper;
import uz.ilmnajot.openlibraryspringapp.model.WorkResponse;
import uz.ilmnajot.openlibraryspringapp.repository.AuthorRepository;
import uz.ilmnajot.openlibraryspringapp.repository.WorkRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkServiceImpl
 * Uses pure mocking without Spring context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Work Service Unit Tests")
class WorkServiceImplTest {

    @Mock
    private WorkRepository workRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WorkMapper workMapper;

    @InjectMocks
    private WorkServiceImpl workService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workService, "baseUrl", "https://openlibrary.org");
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should return works from database when found")
    void shouldReturnWorksFromDBWhenExists() {
        // Given
        String authorId = "/authors/OL123A";

        Author author = new Author();
        author.setAuthorId(authorId);
        author.setAuthorName("Test Author");

        Work work = new Work();
        work.setWorkId("/works/OL456W");
        work.setTitle("Test Work");
        work.getAuthors().add(author);

        WorkResponse workResponse = new WorkResponse();
        workResponse.setWorkId("/works/OL456W");
        workResponse.setTitle("Test Work");

        when(workRepository.findAllByAuthors_AuthorId(authorId))
                .thenReturn(List.of(work));
        when(workMapper.toDto(work)).thenReturn(workResponse);

        // When
        List<WorkResponse> result = workService.getWorksByAuthor(authorId);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should return 1 work");
        assertEquals("Test Work", result.get(0).getTitle());
        assertEquals("/works/OL456W", result.get(0).getWorkId());

        // Verify interactions
        verify(workRepository, times(1))
                .findAllByAuthors_AuthorId(authorId);
        verify(workMapper, times(1)).toDto(work);
        verify(restTemplate, never()).getForObject(anyString(), any());
        verify(workRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should normalize author ID correctly")
    void shouldNormalizeAuthorId() {
        // Given
        Work work = new Work();
        work.setWorkId("/works/OL456W");
        work.setTitle("Test Work");

        WorkResponse workResponse = new WorkResponse();
        workResponse.setWorkId("/works/OL456W");
        workResponse.setTitle("Test Work");

        // Test different formats
        when(workRepository.findAllByAuthors_AuthorId("/authors/OL123A"))
                .thenReturn(List.of(work));
        when(workMapper.toDto(work)).thenReturn(workResponse);

        // When - Test with "OL123A"
        List<WorkResponse> result1 = workService.getWorksByAuthor("OL123A");

        // When - Test with "authors/OL123A"
        List<WorkResponse> result2 = workService.getWorksByAuthor("authors/OL123A");

        // When - Test with "/authors/OL123A"
        List<WorkResponse> result3 = workService.getWorksByAuthor("/authors/OL123A");

        // Then
        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(1, result3.size());

        verify(workRepository, times(3))
                .findAllByAuthors_AuthorId("/authors/OL123A");
    }

    @Test
    @DisplayName("Should fetch from API when database is empty")
    void shouldFetchFromApiWhenDBIsEmpty() throws Exception {
        // Given
        String authorId = "/authors/OL123A";

        when(workRepository.findAllByAuthors_AuthorId(authorId))
                .thenReturn(Collections.emptyList());

        // Mock author
        Author author = new Author();
        author.setAuthorId(authorId);
        author.setAuthorName("Test Author");
        when(authorRepository.findByAuthorId(authorId))
                .thenReturn(Optional.of(author));

        // Mock API response
        String apiResponse = """
                {
                    "entries": [
                        {
                            "key": "/works/OL456W",
                            "title": "Test Work from API",
                            "description": "Test description",
                            "subjects": ["Fiction", "Adventure"],
                            "covers": [12345, 67890]
                        }
                    ]
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(apiResponse);
        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
                .thenReturn(jsonNode);

        // Mock work save
        Work savedWork = new Work();
        savedWork.setWorkId("/works/OL456W");
        savedWork.setTitle("Test Work from API");
        savedWork.getAuthors().add(author);
        when(workRepository.findByWorkId("/works/OL456W"))
                .thenReturn(Optional.empty());
        when(workRepository.save(any(Work.class)))
                .thenReturn(savedWork);

        WorkResponse workResponse = new WorkResponse();
        workResponse.setWorkId("/works/OL456W");
        workResponse.setTitle("Test Work from API");
        when(workMapper.toDto(any(Work.class)))
                .thenReturn(workResponse);

        // When
        List<WorkResponse> result = workService.getWorksByAuthor(authorId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Work from API", result.get(0).getTitle());

        verify(workRepository, times(1))
                .findAllByAuthors_AuthorId(authorId);
        verify(restTemplate, times(1))
                .getForObject(anyString(), eq(JsonNode.class));
        verify(workRepository, times(1)).save(any(Work.class));
    }

    @Test
    @DisplayName("Should return empty list when API returns no works")
    void shouldReturnEmptyListWhenApiReturnsNoWorks() throws Exception {
        // Given
        String authorId = "/authors/OL123A";

        when(workRepository.findAllByAuthors_AuthorId(authorId))
                .thenReturn(Collections.emptyList());

        Author author = new Author();
        author.setAuthorId(authorId);
        author.setAuthorName("Test Author");
        when(authorRepository.findByAuthorId(authorId))
                .thenReturn(Optional.of(author));

        String emptyApiResponse = """
                {
                    "entries": []
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(emptyApiResponse);
        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
                .thenReturn(jsonNode);

        // When
        List<WorkResponse> result = workService.getWorksByAuthor(authorId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Result should be empty");
        verify(workRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle API failure gracefully")
    void shouldThrowExceptionWhenApiFails() {
        // Given
        String authorId = "/authors/OL123A";

        when(workRepository.findAllByAuthors_AuthorId(authorId))
                .thenReturn(Collections.emptyList());

        // Mock RestTemplate to throw exception
        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
                .thenThrow(new RuntimeException("API connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> workService.getWorksByAuthor(authorId),
                "Should throw RuntimeException when API fails");

        assertTrue(exception.getMessage().contains("API connection failed"));

        verify(workRepository, times(1))
                .findAllByAuthors_AuthorId(authorId);
        verify(restTemplate, times(1))
                .getForObject(anyString(), eq(JsonNode.class));
        verify(workRepository, never()).save(any());
        verify(authorRepository, never()).findByAuthorId(anyString());
    }

    @Test
    @DisplayName("Should handle multiple works from API")
    void shouldHandleMultipleWorksFromApi() throws Exception {
        // Given
        String authorId = "/authors/OL123A";

        when(workRepository.findAllByAuthors_AuthorId(authorId))
                .thenReturn(Collections.emptyList());

        Author author = new Author();
        author.setAuthorId(authorId);
        author.setAuthorName("Test Author");
        when(authorRepository.findByAuthorId(authorId))
                .thenReturn(Optional.of(author));

        String apiResponse = """
                {
                    "entries": [
                        {
                            "key": "/works/OL1W",
                            "title": "Work One",
                            "description": "First work"
                        },
                        {
                            "key": "/works/OL2W",
                            "title": "Work Two",
                            "description": "Second work"
                        }
                    ]
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(apiResponse);
        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
                .thenReturn(jsonNode);

        when(workRepository.findByWorkId(anyString()))
                .thenReturn(Optional.empty());
        when(workRepository.save(any(Work.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkResponse response1 = new WorkResponse();
        response1.setWorkId("/works/OL1W");
        response1.setTitle("Work One");

        WorkResponse response2 = new WorkResponse();
        response2.setWorkId("/works/OL2W");
        response2.setTitle("Work Two");

        when(workMapper.toDto(any(Work.class)))
                .thenReturn(response1, response2);

        // When
        List<WorkResponse> result = workService.getWorksByAuthor(authorId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size(), "Should return 2 works");
        verify(workRepository, times(2)).save(any(Work.class));
    }

    @Test
    @DisplayName("Should handle null API response")
    void shouldHandleNullApiResponse() {
        // Given
        String authorId = "/authors/OL123A";

        when(workRepository.findAllByAuthors_AuthorId(authorId))
                .thenReturn(Collections.emptyList());

        // Mock RestTemplate to return null
        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
                .thenReturn(null);

        // When
        List<WorkResponse> result = workService.getWorksByAuthor(authorId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(workRepository, never()).save(any());
        verify(authorRepository, never()).findByAuthorId(anyString());
    }
    @Test
    @DisplayName("Should create author if not exists in DB")
    void shouldCreateAuthorIfNotExists() throws Exception {
        // Given
        String authorId = "/authors/OL123A";

        when(workRepository.findAllByAuthors_AuthorId(authorId))
                .thenReturn(Collections.emptyList());

        // Author not found initially
        when(authorRepository.findByAuthorId(authorId))
                .thenReturn(Optional.empty());

        Author newAuthor = new Author();
        newAuthor.setAuthorId(authorId);
        newAuthor.setAuthorName("New Author Name");
        when(authorRepository.save(any(Author.class)))
                .thenReturn(newAuthor);

        // Mock author details API response first
        String authorDetailsResponse = """
                {
                    "name": "New Author Name"
                }
                """;
        JsonNode authorNode = objectMapper.readTree(authorDetailsResponse);

        // Mock works API response second (empty works)
        String worksResponse = """
                {
                    "entries": []
                }
                """;
        JsonNode worksNode = objectMapper.readTree(worksResponse);

        // Setup multiple return values: first author details, then works
        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
                .thenReturn(authorNode)  // First call for author details
                .thenReturn(worksNode);  // Second call for works

        // When
        List<WorkResponse> result = workService.getWorksByAuthor(authorId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(authorRepository, times(1)).findByAuthorId(authorId);
        verify(authorRepository, times(1)).save(any(Author.class));
        verify(restTemplate, times(2)).getForObject(anyString(), eq(JsonNode.class));
    }

    @Test
    @DisplayName("Should throw exception for null or empty author ID")
    void shouldThrowExceptionForInvalidAuthorId() {
        // When & Then - null
        assertThrows(IllegalArgumentException.class,
                () -> workService.getWorksByAuthor(null),
                "Should throw IllegalArgumentException for null author ID");

        // When & Then - empty
        assertThrows(IllegalArgumentException.class,
                () -> workService.getWorksByAuthor(""),
                "Should throw IllegalArgumentException for empty author ID");

        // When & Then - blank
        assertThrows(IllegalArgumentException.class,
                () -> workService.getWorksByAuthor("   "),
                "Should throw IllegalArgumentException for blank author ID");
    }

    @Test
    @DisplayName("Should not duplicate work if already exists for author")
    void shouldNotDuplicateWorkIfExists() throws Exception {
        // Given
        String authorId = "/authors/OL123A";

        when(workRepository.findAllByAuthors_AuthorId(authorId))
                .thenReturn(Collections.emptyList());

        Author author = new Author();
        author.setAuthorId(authorId);
        author.setAuthorName("Test Author");
        when(authorRepository.findByAuthorId(authorId))
                .thenReturn(Optional.of(author));

        // Existing work in DB
        Work existingWork = new Work();
        existingWork.setWorkId("/works/OL456W");
        existingWork.setTitle("Existing Work");
        existingWork.getAuthors().add(author);

        when(workRepository.findByWorkId("/works/OL456W"))
                .thenReturn(Optional.of(existingWork));

        String apiResponse = """
                {
                    "entries": [
                        {
                            "key": "/works/OL456W",
                            "title": "Existing Work"
                        }
                    ]
                }
                """;
        JsonNode jsonNode = objectMapper.readTree(apiResponse);
        when(restTemplate.getForObject(anyString(), eq(JsonNode.class)))
                .thenReturn(jsonNode);

        WorkResponse workResponse = new WorkResponse();
        workResponse.setWorkId("/works/OL456W");
        workResponse.setTitle("Existing Work");
        when(workMapper.toDto(existingWork))
                .thenReturn(workResponse);

        // When
        List<WorkResponse> result = workService.getWorksByAuthor(authorId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        // Should not create new work, just return existing
        verify(workRepository, never()).save(any(Work.class));
    }
}