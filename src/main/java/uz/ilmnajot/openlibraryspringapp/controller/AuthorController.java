package uz.ilmnajot.openlibraryspringapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.ilmnajot.openlibraryspringapp.model.response.AuthorResponse;
import uz.ilmnajot.openlibraryspringapp.model.response.OpenLibrarySearchResponse;
import uz.ilmnajot.openlibraryspringapp.service.AuthorService;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {
    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<AuthorResponse>> searchAuthor(@RequestParam("q") String name) {
        List<AuthorResponse> authors = authorService.searchAuthor(name);
        return ResponseEntity.ok(authors);
    }
}
