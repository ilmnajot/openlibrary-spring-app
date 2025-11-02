package uz.ilmnajot.openlibraryspringapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkResponse {
    private String workId;
    private String title;
    private String description;
    private List<String> subjects;
    private List<Long> covers;
    private List<AuthorResponse> authors;

}
