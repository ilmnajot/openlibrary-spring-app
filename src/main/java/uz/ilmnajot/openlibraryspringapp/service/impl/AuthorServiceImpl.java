package uz.ilmnajot.openlibraryspringapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.ilmnajot.openlibraryspringapp.entity.Author;
import uz.ilmnajot.openlibraryspringapp.model.AuthorResponse;
import uz.ilmnajot.openlibraryspringapp.model.OpenLibraryAuthorDoc;
import uz.ilmnajot.openlibraryspringapp.model.OpenLibrarySearchResponse;
import uz.ilmnajot.openlibraryspringapp.repository.AuthorRepository;
import uz.ilmnajot.openlibraryspringapp.service.AuthorService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AuthorServiceImpl implements AuthorService {

    private final RestTemplate restTemplate;
    private final AuthorRepository authorRepository;


    @Value("${openlibrary.api.base-url}")
    private String baseUrl;

    public AuthorServiceImpl(RestTemplate restTemplate, AuthorRepository authorRepository) {
        this.restTemplate = restTemplate;
        this.authorRepository = authorRepository;
    }


    /**
     * Search author by name in OpenLibrary API
     * API: <a href="https://openlibrary.org/search/authors.json?q=">...</a>{name}
     */
    @Override
    public List<AuthorResponse> searchAuthor(String name) {
        List<Author> authorsFromBD = this.authorRepository
                .findByAuthorNameContainingIgnoreCase(name);

        //if there is exists in DB
        if (!authorsFromBD.isEmpty()) {
            log.info("Found {} authors in local database", authorsFromBD.size());
            return authorsFromBD
                    .stream()
                    .map(AuthorResponse::from)
                    .toList();


        }
        //if no in localBD, and search from outer API
        log.info("No authors found in local database, searching from OpenLibrary API");

        return searchFromAPIAndSave(name);
    }

    private List<AuthorResponse> searchFromAPIAndSave(String name) {
        List<Author> authors = new ArrayList<>();
        String url = String.format("%s/search/authors.json?q=%s",
                baseUrl,
                name.replace(" ", "%20"));

        try {
            OpenLibrarySearchResponse response = restTemplate.getForObject(url, OpenLibrarySearchResponse.class);
            if (response == null || response.getNumFound() == 0) {
                log.warn("No authors found in OpenLibrary API for name: {}", name);
                return List.of();
            }
            List<Author> savedAuthors = response.getDocs()
                    .stream()
                    .map(this::mapAndSaveAuthor)
                    .toList();
            log.info("Saved {} authors from API", savedAuthors.size());
            return savedAuthors
                    .stream()
                    .map(AuthorResponse::from)
                    .toList();
        } catch (Exception e) {
            log.error("Error searching author: {} ", e.getMessage());
            throw new RuntimeException("Failed to search author", e);
        }

    }

    private Author mapAndSaveAuthor(OpenLibraryAuthorDoc doc) {
        Author author = new Author();
        author.setAuthorId(doc.getKey());
        author.setAuthorName(doc.getName());
        return authorRepository.save(author);
    }


//            String encodedName = name.replace(" ", "%20");
//            String url = baseUrl + "/search/authors.json?q=" + encodedName;
//            try {
//                OpenLibrarySearchResponse response = restTemplate.getForObject(
//                        url,
//                        OpenLibrarySearchResponse.class
//                );
//
//                if (response != null && response.getNumFound() > 0 && !response.getDocs().isEmpty()) {
//                    List<OpenLibraryAuthorDoc> docList = response.getDocs();
//                    for (OpenLibraryAuthorDoc doc : docList) {
//                        Author author = new Author();
//                        author.setAuthorId(doc.getKey());
//                        author.setAuthorName(doc.getName());
//                        this.authorRepository.save(author);
//                    }
//                    return response;
//                }
//            } catch (Exception e) {
//                log.error("Error searching author: {} ", e.getMessage());
//            }
//        }
//        List<OpenLibraryAuthorDoc> docs = new ArrayList<>();
//        for (Author author : authorList) {
//            OpenLibraryAuthorDoc doc = new OpenLibraryAuthorDoc();
//            doc.setKey(author.getAuthorId());
//            doc.setName(author.getAuthorName());
//            docs.add(doc);
//        }
//        OpenLibrarySearchResponse response = new OpenLibrarySearchResponse();
//        response.setNumFound(authorList.size());
//        response.setDocs(docs);
//        return response;

}
