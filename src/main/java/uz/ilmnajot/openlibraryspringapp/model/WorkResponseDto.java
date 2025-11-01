package uz.ilmnajot.openlibraryspringapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.ilmnajot.openlibraryspringapp.entity.Author;
import uz.ilmnajot.openlibraryspringapp.model.response.AuthorResponse;

import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkResponseDto {
    private Long id;
    private String workId;
    private String title;
    private String description;
    private List<String> subjects;
    private List<Long> covers;
    private List<AuthorResponse> authors ;
}
