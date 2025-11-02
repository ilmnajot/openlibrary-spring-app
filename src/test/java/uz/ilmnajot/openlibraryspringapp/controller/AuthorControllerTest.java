package uz.ilmnajot.openlibraryspringapp.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uz.ilmnajot.openlibraryspringapp.model.AuthorResponse;
import uz.ilmnajot.openlibraryspringapp.service.AuthorService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthorController
 * Tests REST API endpoints using MockMvc
 */
@WebMvcTest(controllers = AuthorController.class)
@DisplayName("Author Controller Unit Tests")
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;

    @Test
    @DisplayName("Should return list of authors when search query is provided")
    void shouldReturnAuthorsWhenSearchQueryGiven() throws Exception {
        // Given
        String searchName = "Elbek";
        AuthorResponse author = new AuthorResponse("/authors/A1", "Elbek Umarov");
        when(authorService.searchAuthor(searchName))
                .thenReturn(List.of(author));

        // When + Then
        mockMvc.perform(get("/api/authors/search")
                        .param("q", searchName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].authorName").value("Elbek Umarov"))
                .andExpect(jsonPath("$[0].authorId").value("/authors/A1"));

        verify(authorService, times(1)).searchAuthor(searchName);
    }

    @Test
    @DisplayName("Should return multiple authors when multiple results found")
    void shouldReturnMultipleAuthors() throws Exception {
        // Given
        String searchName = "Smith";
        AuthorResponse author1 = new AuthorResponse("/authors/A1", "John Smith");
        AuthorResponse author2 = new AuthorResponse("/authors/A2", "Jane Smith");
        when(authorService.searchAuthor(searchName))
                .thenReturn(List.of(author1, author2));

        // When + Then
        mockMvc.perform(get("/api/authors/search")
                        .param("q", searchName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].authorName").value("John Smith"))
                .andExpect(jsonPath("$[0].authorId").value("/authors/A1"))
                .andExpect(jsonPath("$[1].authorName").value("Jane Smith"))
                .andExpect(jsonPath("$[1].authorId").value("/authors/A2"));

        verify(authorService, times(1)).searchAuthor(searchName);
    }

    @Test
    @DisplayName("Should return empty list when no authors found")
    void shouldReturnEmptyListWhenNoAuthorsFound() throws Exception {
        // Given
        String searchName = "NonExistentAuthor";
        when(authorService.searchAuthor(searchName))
                .thenReturn(Collections.emptyList());

        // When + Then
        mockMvc.perform(get("/api/authors/search")
                        .param("q", searchName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(authorService, times(1)).searchAuthor(searchName);
    }

    @Test
    @DisplayName("Should handle case-insensitive search")
    void shouldHandleCaseInsensitiveSearch() throws Exception {
        // Given
        String searchName = "ELBEK";
        AuthorResponse author = new AuthorResponse("/authors/A1", "Elbek Umarov");
        when(authorService.searchAuthor(searchName))
                .thenReturn(List.of(author));

        // When + Then
        mockMvc.perform(get("/api/authors/search")
                        .param("q", searchName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].authorName").value("Elbek Umarov"));

        verify(authorService, times(1)).searchAuthor(searchName);
    }

    @Test
    @DisplayName("Should return 400 when search query parameter is missing")
    void shouldReturn400WhenQueryParameterMissing() throws Exception {
        // When + Then
        mockMvc.perform(get("/api/authors/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authorService, never()).searchAuthor(any());
    }

    @Test
    @DisplayName("Should handle empty search query")
    void shouldHandleEmptySearchQuery() throws Exception {
        // Given
        String emptyQuery = "";
        when(authorService.searchAuthor(emptyQuery))
                .thenReturn(Collections.emptyList());

        // When + Then
        mockMvc.perform(get("/api/authors/search")
                        .param("q", emptyQuery)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(authorService, times(1)).searchAuthor(emptyQuery);
    }

    @Test
    @DisplayName("Should handle special characters in search query")
    void shouldHandleSpecialCharactersInQuery() throws Exception {
        // Given
        String searchName = "O'Brien";
        AuthorResponse author = new AuthorResponse("/authors/A1", "Tim O'Brien");
        when(authorService.searchAuthor(searchName))
                .thenReturn(List.of(author));

        // When + Then
        mockMvc.perform(get("/api/authors/search")
                        .param("q", searchName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].authorName").value("Tim O'Brien"));

        verify(authorService, times(1)).searchAuthor(searchName);
    }

    @Test
    @DisplayName("Should handle search query with whitespace")
    void shouldHandleSearchQueryWithWhitespace() throws Exception {
        // Given
        String searchName = "  Elbek  ";
        AuthorResponse author = new AuthorResponse("/authors/A1", "Elbek Umarov");
        when(authorService.searchAuthor(searchName))
                .thenReturn(List.of(author));

        // When + Then
        mockMvc.perform(get("/api/authors/search")
                        .param("q", searchName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(authorService, times(1)).searchAuthor(searchName);
    }

    @Test
    @DisplayName("Should return 500 when service throws exception")
    void shouldReturn500WhenServiceThrowsException() throws Exception {
        // Given
        String searchName = "ErrorAuthor";
        when(authorService.searchAuthor(searchName))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When + Then
        mockMvc.perform(get("/api/authors/search")
                        .param("q", searchName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(authorService, times(1)).searchAuthor(searchName);
    }

    @Test
    @DisplayName("Should handle long search queries")
    void shouldHandleLongSearchQuery() throws Exception {
        // Given
        String longSearchName = "A".repeat(100);
        when(authorService.searchAuthor(longSearchName))
                .thenReturn(Collections.emptyList());

        // When + Then
        mockMvc.perform(get("/api/authors/search")
                        .param("q", longSearchName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(authorService, times(1)).searchAuthor(longSearchName);
    }

    @Test
    @DisplayName("Should accept GET request only")
    void shouldAcceptGetRequestOnly() throws Exception {
        // When + Then - POST should fail
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/authors/search")
                        .param("q", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());

        verify(authorService, never()).searchAuthor(any());
    }
}