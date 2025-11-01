package uz.ilmnajot.openlibraryspringapp.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenLibrarySearchResponse {
    private int numFound;
    private List<OpenLibraryAuthorDoc> docs;
}