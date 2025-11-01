package uz.ilmnajot.openlibraryspringapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.ilmnajot.openlibraryspringapp.model.response.OpenLibraryAuthorDoc;
import uz.ilmnajot.openlibraryspringapp.model.response.OpenLibraryAuthorWorksResponse;
import uz.ilmnajot.openlibraryspringapp.model.response.OpenLibrarySearchResponse;
import uz.ilmnajot.openlibraryspringapp.model.response.OpenLibraryWorkEntry;
@RequiredArgsConstructor
@Service
public class OpenLibraryClient {

    @Value("${openlibrary.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    /**
     * Search author by name in OpenLibrary API
     * API: https://openlibrary.org/search/authors.json?q={name}
     */
//    public OpenLibraryAuthorDoc searchAuthorByName(String name) {
//        String url = baseUrl + "/search/authors.json?q=" + name;
//
//        try {
//            OpenLibrarySearchResponse response = restTemplate.getForObject(
//                    url,
//                    OpenLibrarySearchResponse.class
//            );
//
//            if (response != null && response.getNumFound() > 0 && response.getDocs().length > 0) {
//                return response.getDocs()[0]; // Return first match
//            }
//        } catch (Exception e) {
//            System.err.println("Error searching author: " + e.getMessage());
//        }
//
//        return null;
//    }
    /**
     * Get author's works from OpenLibrary API
     * API: https://openlibrary.org/authors/{authorId}/works.json
     */
    public OpenLibraryWorkEntry[] getAuthorWorks(String authorId) {
        // authorId format: "OL23919A" or "/authors/OL23919A"
        String cleanId = authorId.replace("/authors/", "");
        String url = baseUrl + "/authors/" + cleanId + "/works.json";

        try {
            OpenLibraryAuthorWorksResponse response = restTemplate.getForObject(
                    url,
                    OpenLibraryAuthorWorksResponse.class
            );

            if (response != null && response.getEntries() != null) {
                return response.getEntries();
            }
        } catch (Exception e) {
            System.err.println("Error fetching author works: " + e.getMessage());
        }

        return new OpenLibraryWorkEntry[0];
    }
}
