package uz.ilmnajot.openlibraryspringapp.model.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenLibraryAuthorWorksResponse {
    private int size;
    private OpenLibraryWorkEntry[] entries;
}
