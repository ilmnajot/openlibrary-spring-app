package uz.ilmnajot.openlibraryspringapp.service;

import uz.ilmnajot.openlibraryspringapp.model.response.AuthorResponse;
import uz.ilmnajot.openlibraryspringapp.model.response.OpenLibrarySearchResponse;

import java.util.List;

public interface AuthorService {
    List<AuthorResponse> searchAuthor(String name);
}
