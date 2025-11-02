package uz.ilmnajot.openlibraryspringapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.ilmnajot.openlibraryspringapp.entity.Author;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorResponse {

    private String authorId;
    private String authorName;

    public static AuthorResponse from(Author author) {
        return new AuthorResponse(author.getAuthorId(), author.getAuthorName());
    }
}
