package uz.ilmnajot.openlibraryspringapp.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uz.ilmnajot.openlibraryspringapp.model.WorkResponse;
import uz.ilmnajot.openlibraryspringapp.service.WorkService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for WorkController
 * Tests REST API endpoints using MockMvc
 */
@WebMvcTest(controllers = WorkController.class)
@DisplayName("Work Controller Unit Tests")
class WorkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkService workService;

    @Test
    @DisplayName("Should return list of works for valid author ID")
    void shouldReturnWorksForValidAuthorId() throws Exception {
        // Given
        String authorId = "OL123A";

        WorkResponse work1 = new WorkResponse();
        work1.setWorkId("/works/OL1W");
        work1.setTitle("Test Work 1");
        work1.setDescription("Description 1");

        WorkResponse work2 = new WorkResponse();
        work2.setWorkId("/works/OL2W");
        work2.setTitle("Test Work 2");
        work2.setDescription("Description 2");

        when(workService.getWorksByAuthor(authorId))
                .thenReturn(Arrays.asList(work1, work2));

        // When + Then
        mockMvc.perform(get("/api/works/author/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].workId").value("/works/OL1W"))
                .andExpect(jsonPath("$[0].title").value("Test Work 1"))
                .andExpect(jsonPath("$[0].description").value("Description 1"))
                .andExpect(jsonPath("$[1].workId").value("/works/OL2W"))
                .andExpect(jsonPath("$[1].title").value("Test Work 2"))
                .andExpect(jsonPath("$[1].description").value("Description 2"));

        verify(workService, times(1)).getWorksByAuthor(authorId);
    }

    @Test
    @DisplayName("Should return empty list when no works found for author")
    void shouldReturnEmptyListWhenNoWorksFound() throws Exception {
        // Given
        String authorId = "OL999A";
        when(workService.getWorksByAuthor(authorId))
                .thenReturn(Collections.emptyList());

        // When + Then
        mockMvc.perform(get("/api/works/author/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(workService, times(1)).getWorksByAuthor(authorId);
    }

    @Test
    @DisplayName("Should handle author ID with /authors/ prefix")
    void shouldHandleAuthorIdWithPrefix() throws Exception {
        // Given
        String authorId = "/authors/OL123A";

        WorkResponse work = new WorkResponse();
        work.setWorkId("/works/OL1W");
        work.setTitle("Test Work");

        when(workService.getWorksByAuthor(authorId))
                .thenReturn(List.of(work));

        // When + Then
        mockMvc.perform(get("/api/works/author/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Work"));

        verify(workService, times(1)).getWorksByAuthor(authorId);
    }

    @Test
    @DisplayName("Should handle author ID without prefix")
    void shouldHandleAuthorIdWithoutPrefix() throws Exception {
        // Given
        String authorId = "OL123A";

        WorkResponse work = new WorkResponse();
        work.setWorkId("/works/OL1W");
        work.setTitle("Test Work");

        when(workService.getWorksByAuthor(authorId))
                .thenReturn(List.of(work));

        // When + Then
        mockMvc.perform(get("/api/works/author/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(workService, times(1)).getWorksByAuthor(authorId);
    }

    @Test
    @DisplayName("Should return works with all fields populated")
    void shouldReturnWorksWithAllFields() throws Exception {
        // Given
        String authorId = "OL123A";

        WorkResponse work = new WorkResponse();
        work.setWorkId("/works/OL1W");
        work.setTitle("Complete Work");
        work.setDescription("Full description");
        work.setSubjects(Arrays.asList("Fiction", "Adventure", "Science Fiction"));
        work.setCovers(Arrays.asList(12345L, 67890L));

        when(workService.getWorksByAuthor(authorId))
                .thenReturn(List.of(work));

        // When + Then
        mockMvc.perform(get("/api/works/author/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workId").value("/works/OL1W"))
                .andExpect(jsonPath("$[0].title").value("Complete Work"))
                .andExpect(jsonPath("$[0].description").value("Full description"))
                .andExpect(jsonPath("$[0].subjects", hasSize(3)))
                .andExpect(jsonPath("$[0].subjects[0]").value("Fiction"))
                .andExpect(jsonPath("$[0].subjects[1]").value("Adventure"))
                .andExpect(jsonPath("$[0].subjects[2]").value("Science Fiction"))
                .andExpect(jsonPath("$[0].covers", hasSize(2)))
                .andExpect(jsonPath("$[0].covers[0]").value(12345))
                .andExpect(jsonPath("$[0].covers[1]").value(67890));

        verify(workService, times(1)).getWorksByAuthor(authorId);
    }

    @Test
    @DisplayName("Should return 500 when service throws exception")
    void shouldReturn500WhenServiceThrowsException() throws Exception {
        // Given
        String authorId = "OL123A";
        when(workService.getWorksByAuthor(authorId))
                .thenThrow(new RuntimeException("Failed to fetch works"));

        // When + Then
        mockMvc.perform(get("/api/works/author/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").exists());

        verify(workService, times(1)).getWorksByAuthor(authorId);
    }

    @Test
    @DisplayName("Should return 400 when author ID is invalid")
    void shouldReturn400WhenAuthorIdIsInvalid() throws Exception {
        // Given
        String invalidAuthorId = "";
        when(workService.getWorksByAuthor(invalidAuthorId))
                .thenThrow(new IllegalArgumentException("Author ID cannot be null or empty"));

        // When + Then
        mockMvc.perform(get("/api/works/author/{authorId}", invalidAuthorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(workService, times(1)).getWorksByAuthor(invalidAuthorId);
    }

    @Test
    @DisplayName("Should handle special characters in author ID")
    void shouldHandleSpecialCharactersInAuthorId() throws Exception {
        // Given
        String authorId = "OL123-A";

        WorkResponse work = new WorkResponse();
        work.setWorkId("/works/OL1W");
        work.setTitle("Test Work");

        when(workService.getWorksByAuthor(authorId))
                .thenReturn(List.of(work));

        // When + Then
        mockMvc.perform(get("/api/works/author/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(workService, times(1)).getWorksByAuthor(authorId);
    }

    @Test
    @DisplayName("Should return many works for prolific author")
    void shouldReturnManyWorksForProlificAuthor() throws Exception {
        // Given
        String authorId = "OL123A";

        List<WorkResponse> works = new java.util.ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            WorkResponse work = new WorkResponse();
            work.setWorkId("/works/OL" + i + "W");
            work.setTitle("Work " + i);
            works.add(work);
        }

        when(workService.getWorksByAuthor(authorId))
                .thenReturn(works);

        // When + Then
        mockMvc.perform(get("/api/works/author/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(50)))
                .andExpect(jsonPath("$[0].title").value("Work 1"))
                .andExpect(jsonPath("$[49].title").value("Work 50"));

        verify(workService, times(1)).getWorksByAuthor(authorId);
    }

    @Test
    @DisplayName("Should accept GET request only")
    void shouldAcceptGetRequestOnly() throws Exception {
        // When + Then - POST should fail
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/works/author/{authorId}", "OL123A")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());

        verify(workService, never()).getWorksByAuthor(any());
    }

    @Test
    @DisplayName("Should handle works with null descriptions")
    void shouldHandleWorksWithNullDescriptions() throws Exception {
        // Given
        String authorId = "OL123A";

        WorkResponse work = new WorkResponse();
        work.setWorkId("/works/OL1W");
        work.setTitle("Work Without Description");
        work.setDescription(null);

        when(workService.getWorksByAuthor(authorId))
                .thenReturn(List.of(work));

        // When + Then
        mockMvc.perform(get("/api/works/author/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workId").value("/works/OL1W"))
                .andExpect(jsonPath("$[0].title").value("Work Without Description"))
                .andExpect(jsonPath("$[0].description").doesNotExist());

        verify(workService, times(1)).getWorksByAuthor(authorId);
    }

    @Test
    @DisplayName("Should handle works with empty subjects and covers")
    void shouldHandleWorksWithEmptyCollections() throws Exception {
        // Given
        String authorId = "OL123A";

        WorkResponse work = new WorkResponse();
        work.setWorkId("/works/OL1W");
        work.setTitle("Minimal Work");
        work.setSubjects(Collections.emptyList());
        work.setCovers(Collections.emptyList());

        when(workService.getWorksByAuthor(authorId))
                .thenReturn(List.of(work));

        // When + Then
        mockMvc.perform(get("/api/works/author/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subjects", hasSize(0)))
                .andExpect(jsonPath("$[0].covers", hasSize(0)));

        verify(workService, times(1)).getWorksByAuthor(authorId);
    }
}