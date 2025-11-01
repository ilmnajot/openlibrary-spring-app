package uz.ilmnajot.openlibraryspringapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.ilmnajot.openlibraryspringapp.model.WorkResponseDto;
import uz.ilmnajot.openlibraryspringapp.service.WorkService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/works")
public class WorkController {

    private final WorkService workService;

    /**
     * Get all works by author ID
     * Example: /api/works/by-author?authorId=/authors/OL1394244A
     */
    @GetMapping("/by-author")
    public ResponseEntity<List<WorkResponseDto>> getWorksByAuthor(
            @RequestParam String authorId) {
        List<WorkResponseDto> works = workService.getWorksByAuthor(authorId);
        return ResponseEntity.ok(works);
    }
}
