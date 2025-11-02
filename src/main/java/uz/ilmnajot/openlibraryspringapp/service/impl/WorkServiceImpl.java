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
import uz.ilmnajot.openlibraryspringapp.mapper.WorkMapper;
import uz.ilmnajot.openlibraryspringapp.model.WorkResponse;
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
    private final WorkMapper workMapper;

    @Value("${openlibrary.api.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public List<WorkResponse> getWorksByAuthor(String authorId) {
        log.info("Getting works for author: {}", authorId);
        String normalizeAuthorId = this.normalizeAuthorId(authorId);
        log.info("Normalized author id: {}", normalizeAuthorId);


        // Check if author exists in DB
        List<Work> worksFromBD = this.workRepository.findAllByAuthors_AuthorId(normalizeAuthorId);
        if (!worksFromBD.isEmpty()) {
            log.info("Found {} works in local database", worksFromBD.size());
            return worksFromBD.stream()
                    .map(this.workMapper::toDto)
                    .toList();
        }
        //let's fetch if not found in localdb, from outer api
        log.info("No works found in local database, searching from OpenLibrary API");

        // Fetch from OpenLibrary API
        return fetchAndSaveWorksFromApi(authorId);
    }

    private String normalizeAuthorId(String authorId) {
        if (authorId == null || authorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Author ID cannot be null or empty");
        }
        authorId = authorId.trim();
        if (authorId.startsWith("/authors/")) {
            return authorId;
        }
        if (authorId.startsWith("authors/")) {
            return "/" + authorId;

        }
        return "/authors/" + authorId;
    }

    @Transactional
    public List<WorkResponse> fetchAndSaveWorksFromApi(String authorId) {
        try {
//            String apiPath = authorId + "/works/json";
//            String url = baseUrl + apiPath;
//            log.info("Fetching works from OpenLibrary API: {}", url);
            String normalizedAuthorId = normalizeAuthorId(authorId);
            String apiPath = normalizedAuthorId + "/works.json";
            String url = baseUrl.endsWith("/") ? baseUrl + apiPath.substring(1) : baseUrl + apiPath;
            log.info("Fetching works from OpenLibrary API: {}", url);

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null) {
                log.warn("No response received from OpenLibrary API for author: {}", authorId);
                return List.of();
            }

            // Get or create author
            Author author = this.getOrCreateAuthor(authorId);


            List<WorkResponse> results = new ArrayList<>();
            JsonNode entries = response.get("entries");
            if (!entries.isArray() || entries.isEmpty()) {
                log.warn("No works found in OpenLibrary API for author: {}", authorId);
                return List.of();
            }

            for (JsonNode entry : entries) {
                try {
                    Work work = this.processWorkEntry(entry, author);
                    if (work != null) {
                        results.add(this.workMapper.toDto(work));
                    }
                } catch (Exception e) {
                    log.error("Error processing work entry", e);
                }
            }
            log.info("Fetched {} works from OpenLibrary API for author: {}", results.size(), authorId);
            return results;

        } catch (Exception e) {
            log.error("Error fetching works from OpenLibrary", e);
            throw new RuntimeException("Failed to fetch works", e);
        }
    }

    private Work processWorkEntry(JsonNode entry, Author author) {
        if (!entry.has("key")) {
            log.warn("No key found in work entry");
            return null;
        }
        String workId = entry.get("key").asText();
        //we need to check if already exists in DB
        Optional<Work> existingWork = this.workRepository.findByWorkId(workId);
        if (existingWork.isPresent()) {
            Work work = existingWork.get();
            if (!work.getAuthors().contains(author)) {
                work.getAuthors().add(author);
                return this.workRepository.save(work);
            }
            return work;
        }
        Work work = new Work();
        work.setWorkId(workId);
        work.setTitle(extractTitle(entry));
        work.setDescription(extractDescription(entry));
        work.setSubjects(extractSubjects(entry));
        work.setCovers(extractCovers(entry));
        work.getAuthors().add(author);
        Work savedWork = this.workRepository.save(work);
        log.info("Saved work: {} - {}", savedWork.getWorkId(), savedWork.getTitle());
        return savedWork;
    }

    private List<Long> extractCovers(JsonNode entry) {
        if (!entry.has("covers")) {
            return new ArrayList<>();
        }

        List<Long> covers = new ArrayList<>();
        JsonNode coversNode = entry.get("covers");

        if (coversNode.isArray()) {
            for (JsonNode cover : coversNode) {
                if (cover.isNumber()) {
                    covers.add(cover.asLong());
                }
            }
        }
        return covers;
    }

    private List<String> extractSubjects(JsonNode entry) {
        if (!entry.has("subjects")) {
            return new ArrayList<>();
        }

        List<String> subjects = new ArrayList<>();
        JsonNode subjectsNode = entry.get("subjects");

        if (subjectsNode.isArray()) {
            for (JsonNode subject : subjectsNode) {
                if (subject.isTextual()) {
                    subjects.add(subject.asText());
                }
            }
        }
        return subjects;
    }

    private String extractDescription(JsonNode entry) {
        if (!entry.has("description")) {
            return null;
        }

        JsonNode desc = entry.get("description");
        if (desc.isTextual()) {
            return desc.asText();
        } else if (desc.isObject() && desc.has("value")) {
            return desc.get("value").asText();
        }
        return null;
    }

    private String extractTitle(JsonNode entry) {
        return entry.has("title")
                ? entry.get("title").asText()
                : "Unknown Title";
    }

    private Author fetchAuthorDetails(String authorId) {
        try {
            String url = baseUrl + authorId + ".json";
            log.info("Fetching author details from OpenLibrary API: {}", url);
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

    private Author getOrCreateAuthor(String authorId) {
        return authorRepository.findByAuthorId(authorId)
                .orElseGet(() -> {
                    log.info("Author not found in local DB, fetching from OpenLibrary API: {}", authorId);
                    Author newAuthor = fetchAuthorDetails(authorId);
                    return authorRepository.save(newAuthor);
                });
    }

}
