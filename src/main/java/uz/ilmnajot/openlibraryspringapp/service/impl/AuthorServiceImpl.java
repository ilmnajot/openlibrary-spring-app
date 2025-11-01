package uz.ilmnajot.openlibraryspringapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.ilmnajot.openlibraryspringapp.entity.Author;
import uz.ilmnajot.openlibraryspringapp.model.response.OpenLibraryAuthorDoc;
import uz.ilmnajot.openlibraryspringapp.model.response.OpenLibrarySearchResponse;
import uz.ilmnajot.openlibraryspringapp.repository.AuthorRepository;
import uz.ilmnajot.openlibraryspringapp.service.AuthorService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthorServiceImpl implements AuthorService {

    private final RestTemplate restTemplate;
    private final AuthorRepository authorRepository;


    @Value("${openlibrary.api.base-url}")
    private String baseUrl;


    /**
     * Search author by name in OpenLibrary API
     * API: https://openlibrary.org/search/authors.json?q={name}
     */
    @Override
    public OpenLibrarySearchResponse searchAuthor(String name) {
        List<Author> authorList = this.authorRepository.findByAuthorNameContainingIgnoreCase(name);
        if (authorList.isEmpty()) {
            String encodedName = name.replace(" ", "%20");
            String url = baseUrl + "/search/authors.json?q=" + encodedName;
            try {
                OpenLibrarySearchResponse response = restTemplate.getForObject(
                        url,
                        OpenLibrarySearchResponse.class
                );

                if (response != null && response.getNumFound() > 0 && !response.getDocs().isEmpty()) {
                    List<OpenLibraryAuthorDoc> docList = response.getDocs();
                    for (OpenLibraryAuthorDoc doc : docList) {
                        Author author = new Author();
                        author.setAuthorId(doc.getKey());
                        author.setAuthorName(doc.getName());
                        this.authorRepository.save(author);
                    }
                    return response;
                }
            } catch (Exception e) {
                System.err.println("Error searching author: " + e.getMessage());
            }
        }
        List<OpenLibraryAuthorDoc> docs = new ArrayList<>();
        for (Author author : authorList) {
            OpenLibraryAuthorDoc doc = new OpenLibraryAuthorDoc();
            doc.setKey(author.getAuthorId());
            doc.setName(author.getAuthorName());
            docs.add(doc);
        }
        OpenLibrarySearchResponse response = new OpenLibrarySearchResponse();
        response.setNumFound(authorList.size());
        response.setDocs(docs);
        return response;
    }
}
