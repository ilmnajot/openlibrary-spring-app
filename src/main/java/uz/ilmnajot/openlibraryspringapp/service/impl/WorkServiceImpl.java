package uz.ilmnajot.openlibraryspringapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.ilmnajot.openlibraryspringapp.entity.Author;
import uz.ilmnajot.openlibraryspringapp.entity.Work;
import uz.ilmnajot.openlibraryspringapp.model.WorkResponseDto;
import uz.ilmnajot.openlibraryspringapp.model.response.AuthorResponse;
import uz.ilmnajot.openlibraryspringapp.repository.AuthorRepository;
import uz.ilmnajot.openlibraryspringapp.repository.WorkRepository;
import uz.ilmnajot.openlibraryspringapp.service.WorkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class WorkServiceImpl implements WorkService {
    private final WorkRepository workRepository;
    private final AuthorRepository authorRepository;
    private final RestTemplate restTemplate;

    //    private static final String AUTHOR_DETAIL_API = "https://openlibrary.org/"; // Add trailing slash
    @Value("${openlibrary.api.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public List<WorkResponseDto> getWorksByAuthor(String authorId) {
        log.info("Getting works for author: {}", authorId);

        // Check if author exists in DB
        Optional<Author> authorOpt = authorRepository.findByAuthorId(authorId);

        if (authorOpt.isPresent() && !authorOpt.get().getWorks().isEmpty()) {
            log.info("Found works in local database");
            return authorOpt.get().getWorks().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }

        // Fetch from OpenLibrary API
        return fetchAndSaveWorksFromApi(authorId);
    }

    @Transactional
    public List<WorkResponseDto> fetchAndSaveWorksFromApi(String authorId) {
        if (authorId == null || authorId.trim().isEmpty()) {
            log.error("Author ID cannot be null or empty");
            throw new IllegalArgumentException("Author ID is required");
        }

        try {
            log.info("Author id {}", authorId);
            String cleanAuthorId = authorId.startsWith("/")
                    ? authorId.substring(1)
                    : authorId;
            log.info("Author id {}", cleanAuthorId);
            String url = baseUrl + "/authors/" + cleanAuthorId + "/works.json";
            log.info("Fetching works from: {}", url);

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null) {
                log.warn("No response received from OpenLibrary API for author: {}", authorId);
                return new ArrayList<>();
            }

            // Get or create author
            Author author = authorRepository.findByAuthorId(authorId)
                    .orElseGet(() -> {
                        Author newAuthor = fetchAuthorDetails(authorId);
                        return authorRepository.save(newAuthor);
                    });

            List<WorkResponseDto> results = new ArrayList<>();
            JsonNode entries = response.get("entries");

            for (JsonNode entry : entries) {
                try {
                    String workKey = entry.get("key").asText();

                    // Check if work already exists
                    Optional<Work> existingWork = workRepository.findByWorkId(workKey);
                    if (existingWork.isPresent()) {
                        results.add(convertToDto(existingWork.get()));
                        continue;
                    }

                    // Create new work
                    Work work = new Work();
                    work.setWorkId(workKey);
                    work.setTitle(entry.has("title") ? entry.get("title").asText() : "Unknown");

                    // Extract description
                    if (entry.has("description")) {
                        JsonNode desc = entry.get("description");
                        if (desc.isTextual()) {
                            work.setDescription(desc.asText());
                        } else if (desc.has("value")) {
                            work.setDescription(desc.get("value").asText());
                        }
                    }

                    // Extract subjects
                    if (entry.has("subjects")) {
                        List<String> subjects = new ArrayList<>();
                        for (JsonNode subject : entry.get("subjects")) {
                            subjects.add(subject.asText());
                        }
                        work.setSubjects(subjects);
                    }

                    // Extract covers
                    if (entry.has("covers")) {
                        List<Long> covers = new ArrayList<>();
                        for (JsonNode cover : entry.get("covers")) {
                            covers.add(cover.asLong());
                        }
                        work.setCovers(covers);
                    }

                    // Add author to work
                    work.getAuthors().add(author);

                    // Save work
                    Work savedWork = workRepository.save(work);
                    log.info("Saved work: {} - {}", savedWork.getWorkId(), savedWork.getTitle());

                    results.add(convertToDto(savedWork));

                } catch (Exception e) {
                    log.error("Error processing work entry", e);
                }
            }

            return results;

        } catch (Exception e) {
            log.error("Error fetching works from OpenLibrary", e);
            throw new RuntimeException("Failed to fetch works", e);
        }
    }

    private Author fetchAuthorDetails(String authorId) {
        try {
            String url = baseUrl + authorId + ".json";
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            String name = response != null && response.has("name")
                    ? response.get("name").asText()
                    : "Unknown Author";

            return new Author(authorId, name);

        } catch (Exception e) {
            log.error("Error fetching author details", e);
            return new Author(authorId, "Unknown Author");
        }
    }

    private WorkResponseDto convertToDto(Work work) {
        WorkResponseDto dto = new WorkResponseDto();
        dto.setWorkId(work.getWorkId());
        dto.setTitle(work.getTitle());
        dto.setDescription(work.getDescription());
        dto.setSubjects(work.getSubjects());

        List<AuthorResponse> authors = work.getAuthors().stream()
                .map(a -> new AuthorResponse(a.getAuthorId(), a.getAuthorName()))
                .collect(Collectors.toList());
        dto.setAuthors(authors);

        return dto;
    }

}
