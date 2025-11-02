package uz.ilmnajot.openlibraryspringapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenLibraryWorkEntry {
    //key is coming as id
    private String key;
    private String title;
}