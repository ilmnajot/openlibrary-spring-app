package uz.ilmnajot.openlibraryspringapp.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenLibraryWorkEntry {
    private String key;          // e.g., "/works/OL45804W"
    private String title;
}