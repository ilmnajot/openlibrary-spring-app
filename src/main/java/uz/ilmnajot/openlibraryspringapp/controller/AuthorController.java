package uz.ilmnajot.openlibraryspringapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.ilmnajot.openlibraryspringapp.model.AuthorResponse;
import uz.ilmnajot.openlibraryspringapp.service.AuthorService;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {
    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    /**
     * Searches for authors by a given name (or partial name).
     *
     * @param name the search query to find authors by name
     * @return a ResponseEntity containing a list of AuthorResponse objects
     */
//    @GetMapping("/search")
//    public ResponseEntity<List<AuthorResponse>> searchAuthor(@RequestParam("q") String name) {
//        List<AuthorResponse> authors = authorService.searchAuthor(name);
//        return ResponseEntity.ok(authors);
//    }
    @GetMapping("/search")
    public ResponseEntity<List<AuthorResponse>> searchAuthor(@RequestParam("q") String name) {
        try {
            List<AuthorResponse> authors = authorService.searchAuthor(name);
            return ResponseEntity.ok(authors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
